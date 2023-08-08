package me.rochblondiaux.bukkitmessaging.api.storage.implementation.sql.connection;

import java.util.function.Function;

import me.rochblondiaux.bukkitmessaging.api.storage.StorageCredentials;

/**
 * BukkitMessaging
 * 08/08/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class MariaDbConnectionFactory extends DriverBasedHikariConnectionFactory {

    public MariaDbConnectionFactory(StorageCredentials configuration) {
        super(configuration);
    }

    @Override
    public String getImplementationName() {
        return "MariaDB";
    }

    @Override
    protected String defaultPort() {
        return "3306";
    }

    @Override
    protected String driverClassName() {
        return "org.mariadb.jdbc.Driver";
    }

    @Override
    protected String driverJdbcIdentifier() {
        return "mariadb";
    }

    @Override
    public Function<String, String> getStatementProcessor() {
        return s -> s.replace('\'', '`'); // use backticks for quotes
    }
}