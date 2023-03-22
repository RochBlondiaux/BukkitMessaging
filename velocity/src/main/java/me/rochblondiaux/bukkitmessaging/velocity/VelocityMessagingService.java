package me.rochblondiaux.bukkitmessaging.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import me.rochblondiaux.bukkitmessaging.api.MessagingService;
import me.rochblondiaux.bukkitmessaging.api.adapter.MessagingAdapter;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisCredentials;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisMessagingAdapter;
import me.rochblondiaux.bukkitmessaging.velocity.adapter.VelocityMessagingAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * BukkitMessaging
 * 22/03/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class VelocityMessagingService extends MessagingService {

    public static final ChannelIdentifier CHANNEL = () -> "playerpoof";

    private final Object plugin;
    private final ProxyServer server;
    private final MessagingAdapter adapter;

    public VelocityMessagingService(Object plugin, @NotNull ProxyServer server, @NotNull Type type, @Nullable RedisCredentials credentials) {
        super(type, credentials);
        this.plugin = plugin;
        this.server = server;
        this.adapter = this.type == Type.PROXY ? new VelocityMessagingAdapter(this) : new RedisMessagingAdapter(this);
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

    public ProxyServer server() {
        return server;
    }

    public Object plugin() {
        return plugin;
    }
}
