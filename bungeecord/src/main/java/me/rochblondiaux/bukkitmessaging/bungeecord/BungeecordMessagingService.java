package me.rochblondiaux.bukkitmessaging.bungeecord;

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
import me.rochblondiaux.bukkitmessaging.bungeecord.adapter.BungeecordMessagingAdapter;
import net.md_5.bungee.api.plugin.Plugin;

/**
 * BukkitMessaging
 * 23/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class BungeecordMessagingService extends MessagingService {

    private final Plugin plugin;
    private final MessagingAdapter adapter;

    public BungeecordMessagingService(Plugin plugin, @NotNull Type type, @Nullable RedisCredentials credentials, @Nullable StorageCredentials storageCredentials) {
        super("bungeecord-proxy", type, credentials, storageCredentials);
        this.plugin = plugin;
        this.adapter = this.buildAdapter();
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
                return new BungeecordMessagingAdapter(this);
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

    @Override
    public @NotNull MessagingAdapter adapter() {
        return adapter;
    }

    public Plugin plugin() {
        return plugin;
    }
}
