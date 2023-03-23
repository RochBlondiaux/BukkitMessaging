package me.rochblondiaux.bukkitmessaging.bukkit;

import me.rochblondiaux.bukkitmessaging.api.MessagingService;
import me.rochblondiaux.bukkitmessaging.api.adapter.MessagingAdapter;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisCredentials;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisMessagingAdapter;
import me.rochblondiaux.bukkitmessaging.bukkit.adapter.BukkitMessagingAdapter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public BukkitMessagingService(@NotNull JavaPlugin plugin, @NotNull Type type, @Nullable RedisCredentials credentials) {
        super(type, credentials);
        this.plugin = plugin;
        this.adapter = this.type.equals(Type.PROXY) ? new BukkitMessagingAdapter(this) : new RedisMessagingAdapter(this);
        this.proxyType = this.detectProxyType();
    }

    public void load() {
        this.adapter().init(this.credentials);
    }

    public void unload() {
        this.adapter().unload();
    }

    private ProxyType detectProxyType() {
        if (Bukkit.getServer().spigot().getConfig().getBoolean("settings.bungeecord", false))
            return ProxyType.BUNGEECORD;
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
}
