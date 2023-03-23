package me.rochblondiaux.bukkitmessaging.bukkit.adapter;

import me.rochblondiaux.bukkitmessaging.api.adapter.MessagingAdapter;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisCredentials;
import me.rochblondiaux.bukkitmessaging.bukkit.BukkitMessagingService;
import me.rochblondiaux.bukkitmessaging.bukkit.listener.ProxyListener;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * BukkitMessaging
 * 23/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class BukkitMessagingAdapter implements MessagingAdapter {

    public static final String SUB_CHANNEL = "playerpoof";

    private final BukkitMessagingService service;
    private final ProxyListener listener;
    private final boolean proxied;

    public BukkitMessagingAdapter(BukkitMessagingService service) {
        this.service = service;
        this.listener = new ProxyListener(this);
        this.proxied = Bukkit.getServer().spigot().getConfig().getBoolean("settings.bungeecord", false);
    }


    @Override
    public void init(@Nullable RedisCredentials credentials) {
        if (!this.proxied) return;

        // Register the channel
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(service.plugin(), "BungeeCord");
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(service.plugin(), "BungeeCord", this.listener);
    }

    @Override
    public void unload() {
        if (!this.proxied) return;

        // Unregister the channel
        Bukkit.getServer().getMessenger().unregisterOutgoingPluginChannel(service.plugin());
        Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(service.plugin());
    }

    @Override
    public void publish(String message) {
        if (!this.proxied) {
            return;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(SUB_CHANNEL);
            out.writeUTF(message);

            Bukkit.getServer().sendPluginMessage(this.service.plugin(), "BungeeCord", stream.toByteArray());
        } catch (IOException ex) {
            service.plugin().getLogger().severe("An error occurred when attempting to communicate with the proxy! Code: M-0001");
        }
    }

    public BukkitMessagingService service() {
        return this.service;
    }
}
