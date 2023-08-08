package me.rochblondiaux.bukkitmessaging.api.storage.implementation.sql.connection;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * BukkitMessaging
 * 08/08/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public interface ConnectionFactory {

    String getImplementationName();

    void init();

    void shutdown() throws Exception;

    default Map<Component, Component> getMeta() {
        return new LinkedHashMap<>();
    }

    Function<String, String> getStatementProcessor();

    Connection getConnection() throws SQLException;

}