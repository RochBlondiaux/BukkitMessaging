package me.rochblondiaux.bukkitmessaging.bukkit.adapter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import me.rochblondiaux.bukkitmessaging.api.Constants;
import me.rochblondiaux.bukkitmessaging.api.adapter.MessagingAdapter;
import me.rochblondiaux.bukkitmessaging.api.cache.CacheService;
import me.rochblondiaux.bukkitmessaging.api.cache.CachedObject;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisCredentials;
import me.rochblondiaux.bukkitmessaging.bukkit.BukkitMessagingService;
import me.rochblondiaux.bukkitmessaging.bukkit.ProxyType;
import me.rochblondiaux.bukkitmessaging.bukkit.listener.ProxyListener;

/**
 * BukkitMessaging
 * 23/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class BukkitMessagingAdapter implements MessagingAdapter {

    private final BukkitMessagingService service;
    private final ProxyListener listener;
    @Getter
    private final CacheService cacheService;
    private BukkitTask task;

    public BukkitMessagingAdapter(BukkitMessagingService service) {
        this.service = service;
        this.cacheService = new CacheService(data -> {
            if (this.service.proxyType().equals(ProxyType.NONE)
                || !this.service.plugin().isEnabled()) return;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(stream);
            try {
                out.writeUTF(Constants.CACHE_SUB_CHANNEL);
                out.writeUTF(data);
                Bukkit.getServer().sendPluginMessage(this.service.plugin(), Constants.CHANNEL, stream.toByteArray());
            } catch (IOException ex) {
                service.plugin().getLogger().severe("An error occurred when attempting to communicate with the proxy! Code: M-0001");
            }
        });
        this.listener = new ProxyListener(this, cacheService);
    }

    @Override
    public void init(@Nullable RedisCredentials credentials) {
        if (this.service.proxyType().equals(ProxyType.NONE)) return;

        // Register BungeeCord the channel
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(service.plugin(), Constants.CHANNEL);
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(service.plugin(), Constants.CHANNEL, this.listener);

        // Start task
        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(service().plugin(), () -> {
            this.cacheService.tick();

            if (this.cacheService.getQueue().isEmpty()) return;
            CachedObject object = this.cacheService.getQueue().poll();
            if (object == null) return;
            this.cacheService.sendMessage("set", object);
        }, 0, 40L);
    }

    @Override
    public void publish(String message) {
        if (this.service.proxyType().equals(ProxyType.NONE)
            || !this.service.plugin().isEnabled()) return;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
            out.writeUTF(Constants.SUB_CHANNEL);
            out.writeUTF(message);
            Bukkit.getServer().sendPluginMessage(this.service.plugin(), Constants.CHANNEL, stream.toByteArray());
        } catch (IOException ex) {
            service.plugin().getLogger().severe("An error occurred when attempting to communicate with the proxy! Code: M-0002");
        }
    }

    @Override
    public void set(String key, String value) {
        this.cacheService.set(key, value);
    }

    @Override
    public void set(String key, String value, int ttl) {
        this.cacheService.set(key, value, ttl);
    }

    @Override
    public <T> Optional<T> get(String key) {
        return this.cacheService.get(key);
    }

    @Override
    public void remove(String key) {
        this.cacheService.remove(key);
    }

    @Override
    public boolean has(String key) {
        return this.cacheService.has(key);
    }

    @Override
    public Set<String> keys(String pattern) {
        return this.cacheService.keys(pattern);
    }

    @Override
    public void unload() {
        if (this.service.proxyType().equals(ProxyType.NONE)) return;

        // Unregister the channel
        Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(service.plugin(), Constants.CHANNEL);
        Bukkit.getServer().getMessenger().unregisterOutgoingPluginChannel(service.plugin(), Constants.CHANNEL);

        // End task
        if (this.task != null) this.task.cancel();
    }

    public BukkitMessagingService service() {
        return this.service;
    }
}
