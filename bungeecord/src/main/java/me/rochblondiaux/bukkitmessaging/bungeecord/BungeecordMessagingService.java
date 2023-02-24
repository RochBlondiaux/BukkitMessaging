package me.rochblondiaux.bukkitmessaging.bungeecord;

import me.rochblondiaux.bukkitmessaging.api.MessagingService;
import me.rochblondiaux.bukkitmessaging.api.adapter.MessagingAdapter;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisCredentials;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisMessagingAdapter;
import me.rochblondiaux.bukkitmessaging.bungeecord.adapter.BungeecordMessagingAdapter;
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

    public static final String SUB_CHANNEL = "playerpoof";

    private final Plugin plugin;
    private final MessagingAdapter adapter;
    public BungeecordMessagingService(Plugin plugin, @NotNull Type type, @Nullable RedisCredentials credentials) {
        super(type, credentials);
        this.plugin = plugin;
        this.adapter = this.type == Type.PROXY ? new BungeecordMessagingAdapter(this) : new RedisMessagingAdapter(this);
    }

    public void load() {
        this.adapter().init(this.credentials);
    }

    public void unload() {
        this.adapter().unload();
    }

    @Override
    public @NotNull MessagingAdapter adapter() {
        return adapter;
    }

    public Plugin plugin() {
        return plugin;
    }
}
