package me.rochblondiaux.bukkitmessaging.bukkit;

import me.rochblondiaux.bukkitmessaging.api.MessagingService;
import me.rochblondiaux.bukkitmessaging.api.adapter.MessagingAdapter;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisCredentials;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisMessagingAdapter;
import me.rochblondiaux.bukkitmessaging.bukkit.adapter.BukkitMessagingAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

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

    public BukkitMessagingService(@NotNull JavaPlugin plugin, @NotNull String name, @NotNull Type type, @Nullable RedisCredentials credentials) {
        super(name, type, credentials);
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

        YamlConfiguration paperConfiguration = paperConfiguration();
        if (paperConfiguration != null)
            return paperConfiguration.getBoolean("proxies.velocity.enabled", false) ? ProxyType.VELOCITY : ProxyType.NONE;
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
