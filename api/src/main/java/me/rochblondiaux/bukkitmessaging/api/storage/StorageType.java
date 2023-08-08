package me.rochblondiaux.bukkitmessaging.api.storage;

import java.util.Arrays;
import java.util.List;

/**
 * BukkitMessaging
 * 08/08/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public enum StorageType {
    MONGODB("MongoDB", "mongodb"),
    MARIADB("MariaDB", "mariadb"),
    MYSQL("MySQL", "mysql"),
    POSTGRESQL("PostgreSQL", "postgresql");

    private final String name;

    private final List<String> identifiers;

    StorageType(String name, String... identifiers) {
        this.name = name;
        this.identifiers = Arrays.asList(identifiers);
    }

    public static StorageType parse(String name, StorageType def) {
        for (StorageType t : values()) {
            for (String id : t.getIdentifiers()) {
                if (id.equalsIgnoreCase(name)) {
                    return t;
                }
            }
        }
        return def;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getIdentifiers() {
        return this.identifiers;
    }
}
