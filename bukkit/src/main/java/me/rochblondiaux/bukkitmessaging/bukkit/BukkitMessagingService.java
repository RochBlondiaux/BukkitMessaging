package me.rochblondiaux.bukkitmessaging.bukkit;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.rochblondiaux.bukkitmessaging.api.MessagingService;
import me.rochblondiaux.bukkitmessaging.api.adapter.MessagingAdapter;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisCredentials;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisMessagingAdapter;
import me.rochblondiaux.bukkitmessaging.api.storage.StorageCredentials;
import me.rochblondiaux.bukkitmessaging.api.storage.StorageType;
import me.rochblondiaux.bukkitmessaging.api.storage.implementation.sql.SQLMessagingAdapter;
import me.rochblondiaux.bukkitmessaging.api.storage.implementation.sql.SqlStorage;
import me.rochblondiaux.bukkitmessaging.api.storage.implementation.sql.connection.MariaDbConnectionFactory;
import me.rochblondiaux.bukkitmessaging.api.storage.implementation.sql.connection.MySqlConnectionFactory;
import me.rochblondiaux.bukkitmessaging.api.storage.implementation.sql.connection.PostgresConnectionFactory;
import me.rochblondiaux.bukkitmessaging.bukkit.adapter.BukkitMessagingAdapter;

/**
 * BukkitMessaging
 * 23/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class BukkitMessagingService extends MessagingService {

    private final JavaPlugin plugin;
    private final MessagingAdapter adapter;
    private final ProxyType proxyType;

    public BukkitMessagingService(@NotNull JavaPlugin plugin, @NotNull String name, @NotNull Type type, @Nullable RedisCredentials credentials, @Nullable StorageCredentials storageCredentials) {
        super(name, type, credentials, storageCredentials);
        this.plugin = plugin;
        this.adapter = this.buildAdapter();
        this.proxyType = this.detectProxyType();
    }

    public void load() {
        this.adapter().init(this.credentials);
    }

    public void unload() {
        this.adapter().unload();
    }


    private MessagingAdapter buildAdapter() {
        switch (this.type) {
            case PROXY:
                return new BukkitMessagingAdapter(this);
            case REDIS:
                return new RedisMessagingAdapter(this);
            case DATABASE:
                if (this.storageCredentials == null)
                    throw new IllegalStateException("Storage credentials are not defined.");

                final ClassLoader classLoader = this.plugin.getClass().getClassLoader();
                final SqlStorage sqlStorage;

                if (this.storageCredentials.getType() == StorageType.MARIADB)
                    sqlStorage = new SqlStorage("schema", classLoader, new MariaDbConnectionFactory(this.storageCredentials), "playerpoof_");
                else if (this.storageCredentials.getType() == StorageType.MONGODB)
                    throw new IllegalStateException("MongoDB is not implemented yet.");
                else if (this.storageCredentials.getType() == StorageType.POSTGRESQL)
                    sqlStorage = new SqlStorage("schema", classLoader, new PostgresConnectionFactory(this.storageCredentials), "playerpoof_");
                else if (this.storageCredentials.getType() == StorageType.MYSQL)
                    sqlStorage = new SqlStorage("schema", classLoader, new MySqlConnectionFactory(this.storageCredentials), "playerpoof_");
                else
                    throw new IllegalStateException("Unexpected value: " + this.storageCredentials.getType());

                return new SQLMessagingAdapter(this, sqlStorage, "playerpoof_messages", "playerpoof_cache");
            case CLOUD:
                throw new IllegalStateException("Cloud messaging is not implemented yet.");
            default:
                throw new IllegalStateException("Unexpected value: " + this.type);
        }
    }

    private ProxyType detectProxyType() {
        // BungeeCord/Waterfall
        if (Bukkit.getServer().spigot().getConfig().getBoolean("settings.bungeecord", false))
            return ProxyType.BUNGEECORD;

        // Velocity
        YamlConfiguration paperConfiguration = paperConfiguration();
        if (paperConfiguration != null)
            return paperConfiguration.getBoolean("proxies.velocity.enabled", false) ? ProxyType.VELOCITY : ProxyType.NONE;

        // None
        return ProxyType.NONE;
    }

    @Override
    public @NotNull MessagingAdapter adapter() {
        return adapter;
    }

    public JavaPlugin plugin() {
        return this.plugin;
    }

    public ProxyType proxyType() {
        return this.proxyType;
    }

    private static Method PAPER_CONFIGURATION_METHOD;

    static {
        try {
            PAPER_CONFIGURATION_METHOD = Server.Spigot.class.getMethod("getPaperConfig");
        } catch (NoSuchMethodException ignored) {
        }
    }

    private @Nullable YamlConfiguration paperConfiguration() {
        if (PAPER_CONFIGURATION_METHOD == null) return null;
        try {
            return (YamlConfiguration) PAPER_CONFIGURATION_METHOD.invoke(Bukkit.getServer().spigot());
        } catch (Exception e) {
            return null;
        }
    }
}
