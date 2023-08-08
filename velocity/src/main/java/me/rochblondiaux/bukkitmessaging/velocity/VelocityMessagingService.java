package me.rochblondiaux.bukkitmessaging.velocity;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import me.rochblondiaux.bukkitmessaging.api.Constants;
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
import me.rochblondiaux.bukkitmessaging.velocity.adapter.VelocityMessagingAdapter;

/**
 * BukkitMessaging
 * 22/03/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class VelocityMessagingService extends MessagingService {
    public static final MinecraftChannelIdentifier CHANNEL = MinecraftChannelIdentifier.from(Constants.CHANNEL);

    private final Object plugin;
    private final ProxyServer server;
    private final MessagingAdapter adapter;
    private final Queue<CachedMessage> cachedMessages = new ConcurrentLinkedQueue<>();

    public VelocityMessagingService(Object plugin, @NotNull ProxyServer server, @NotNull Type type, @Nullable RedisCredentials credentials, @Nullable StorageCredentials storageCredentials) {
        super("velocity-proxy", type, credentials, storageCredentials);
        this.plugin = plugin;
        this.server = server;
        this.adapter = this.buildAdapter();
    }

    @Override
    public @NotNull MessagingAdapter adapter() {
        return adapter;
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
                return new VelocityMessagingAdapter(this);
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


    public ProxyServer server() {
        return server;
    }

    public Object plugin() {
        return plugin;
    }

    public void cacheMessage(RegisteredServer server, String payload) {
        cachedMessages.add(new CachedMessage(server.getServerInfo().getName(), payload));
    }

    public boolean isCached(RegisteredServer server, String payload) {
        return cachedMessages.stream().anyMatch(message -> message.getPayload().equals(payload) && message.getServerName().equals(server.getServerInfo().getName()));
    }

    public Queue<CachedMessage> cachedMessages() {
        return cachedMessages;
    }
}
