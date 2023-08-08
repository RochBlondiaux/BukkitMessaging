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

    public VelocityMessagingService(Object plugin, @NotNull ProxyServer server, @NotNull Type type, @Nullable RedisCredentials credentials) {
        super("velocity-proxy", type, credentials);
        this.plugin = plugin;
        this.server = server;
        this.adapter = this.type == Type.PROXY ? new VelocityMessagingAdapter(this) : new RedisMessagingAdapter(this);
    }

    @Override
    public @NotNull MessagingAdapter adapter() {
        return adapter;
    }

    public void load() {
        this.server.getChannelRegistrar().register(CHANNEL);
        this.adapter().init(this.credentials);
    }

    public void unload() {
        this.adapter().unload();
        this.server.getChannelRegistrar().unregister(CHANNEL);
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
