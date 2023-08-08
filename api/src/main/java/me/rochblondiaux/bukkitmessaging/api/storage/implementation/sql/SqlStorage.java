package me.rochblondiaux.bukkitmessaging.api.storage.implementation.sql;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.java.Log;
import me.rochblondiaux.bukkitmessaging.api.storage.StorageImplementation;
import me.rochblondiaux.bukkitmessaging.api.storage.implementation.sql.connection.ConnectionFactory;

/**
 * BukkitMessaging
 * 08/08/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
@Log
public class SqlStorage implements StorageImplementation {

    private final String schemaPath;
    private final ClassLoader classLoader;
    private final ConnectionFactory connectionFactory;
    private final Function<String, String> statementProcessor;

    public SqlStorage(String schemaPath, ClassLoader classLoader, ConnectionFactory connectionFactory, String tablePrefix) {
        this.schemaPath = schemaPath;
        this.classLoader = classLoader;
        this.connectionFactory = connectionFactory;
        this.statementProcessor = connectionFactory.getStatementProcessor().compose(s -> s.replace("{prefix}", tablePrefix));
    }

    @Override
    public String getImplementationName() {
        return this.connectionFactory.getImplementationName();
    }

    @Override
    public void init() throws Exception {
        this.connectionFactory.init();

        boolean tableExists;
        try (Connection c = this.connectionFactory.getConnection()) {
            tableExists = tableExists(c, this.statementProcessor.apply("{prefix}user_permissions"));
        }

        if (!tableExists) {
            applySchema(this.schemaPath, this.classLoader);
        }
    }

    @Override
    public void shutdown() {
        try {
            this.connectionFactory.shutdown();
        } catch (Exception e) {
            log.severe("Exception whilst disabling SQL storage");
        }
    }

    private void applySchema(String path, ClassLoader classLoader) throws IOException, SQLException {
        List<String> statements;

        String schemaFileName = path + this.connectionFactory.getImplementationName().toLowerCase(Locale.ROOT) + ".sql";
        try (InputStream is = classLoader.getResourceAsStream(schemaFileName)) {
            if (is == null) {
                throw new IOException("Couldn't locate schema file for " + this.connectionFactory.getImplementationName());
            }

            statements = SchemaReader.getStatements(is).stream()
                    .map(this.statementProcessor)
                    .collect(Collectors.toList());
        }

        try (Connection connection = this.connectionFactory.getConnection()) {
            boolean utf8mb4Unsupported = false;

            try (Statement s = connection.createStatement()) {
                for (String query : statements) {
                    s.addBatch(query);
                }

                try {
                    s.executeBatch();
                } catch (BatchUpdateException e) {
                    if (e.getMessage().contains("Unknown character set")) {
                        utf8mb4Unsupported = true;
                    } else {
                        throw e;
                    }
                }
            }

            // try again
            if (utf8mb4Unsupported) {
                try (Statement s = connection.createStatement()) {
                    for (String query : statements) {
                        s.addBatch(query.replace("utf8mb4", "utf8"));
                    }

                    s.executeBatch();
                }
            }
        }
    }

    private static boolean tableExists(Connection connection, String table) throws SQLException {
        try (ResultSet rs = connection.getMetaData().getTables(connection.getCatalog(), null, "%", null)) {
            while (rs.next()) {
                if (rs.getString(3).equalsIgnoreCase(table)) {
                    return true;
                }
            }
            return false;
        }
    }

    public Connection getConnection() throws SQLException {
        return this.connectionFactory.getConnection();
    }
}
