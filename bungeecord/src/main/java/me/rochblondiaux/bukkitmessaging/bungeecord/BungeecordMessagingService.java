package me.rochblondiaux.bukkitmessaging.bungeecord;

import me.rochblondiaux.bukkitmessaging.api.Constants;
import me.rochblondiaux.bukkitmessaging.api.MessagingService;
import me.rochblondiaux.bukkitmessaging.api.adapter.MessagingAdapter;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisCredentials;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisMessagingAdapter;
import me.rochblondiaux.bukkitmessaging.bungeecord.adapter.BungeecordMessagingAdapter;
import me.rochblondiaux.bukkitmessaging.bungeecord.listener.MessageListener;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * BukkitMessaging
 * 23/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class BungeecordMessagingService extends MessagingService {

    private final Plugin plugin;
    private final MessagingAdapter adapter;
    private MessageListener listener;

    public BungeecordMessagingService(Plugin plugin, @NotNull Type type, @Nullable RedisCredentials credentials) {
        super("bungeecord-proxy", type, credentials);
        this.plugin = plugin;
        this.adapter = this.type == Type.PROXY ? new BungeecordMessagingAdapter(this) : new RedisMessagingAdapter(this);
        this.plugin.getProxy().registerChannel(Constants.SUB_CHANNEL);
    }

    public void load() {
        this.adapter().init(this.credentials);
        this.plugin.getProxy().getPluginManager().registerListener(plugin, listener = new MessageListener(this));
    }

    public void unload() {
        this.adapter().unload();
        this.plugin.getProxy().getPluginManager().unregisterListener(listener);
    }

    @Override
    public @NotNull MessagingAdapter adapter() {
        return adapter;
    }

    public Plugin plugin() {
        return plugin;
    }
}
