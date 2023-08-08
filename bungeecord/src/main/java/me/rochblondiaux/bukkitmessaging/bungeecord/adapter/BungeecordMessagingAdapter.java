package me.rochblondiaux.bukkitmessaging.bungeecord.adapter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import me.rochblondiaux.bukkitmessaging.api.Constants;
import me.rochblondiaux.bukkitmessaging.api.adapter.MessagingAdapter;
import me.rochblondiaux.bukkitmessaging.api.cache.CacheService;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisCredentials;
import me.rochblondiaux.bukkitmessaging.bungeecord.BungeecordMessagingService;
import me.rochblondiaux.bukkitmessaging.bungeecord.listener.MessageListener;
import net.md_5.bungee.api.ProxyServer;

/**
 * BukkitMessaging
 * 23/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class BungeecordMessagingAdapter implements MessagingAdapter {


    private final BungeecordMessagingService service;
    private final MessageListener listener;
    @Getter
    private final CacheService cacheService;

    public BungeecordMessagingAdapter(BungeecordMessagingService service) {
        this.service = service;
        this.listener = new MessageListener(service);
        this.cacheService = new CacheService(bytes -> ProxyServer.getInstance().getServers().values().forEach(server -> server.sendData(Constants.SUB_CHANNEL, bytes.getBytes())));
    }

    @Override
    public void init(@Nullable RedisCredentials credentials) {
        // Register channel
        service.plugin().getProxy().registerChannel(Constants.SUB_CHANNEL);

        // Register listener
        service.plugin().getProxy().getPluginManager().registerListener(service.plugin(), listener);
    }

    @Override
    public void unload() {
        // Unregister channel
        service.plugin().getProxy().unregisterChannel(Constants.SUB_CHANNEL);

        // Unregister listener
        service.plugin().getProxy().getPluginManager().unregisterListener(listener);
    }

    @Override
    public void publish(String message) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(Constants.SUB_CHANNEL);
            out.writeUTF(message);
            ProxyServer.getInstance().getServers().values().forEach(server -> server.sendData(Constants.CHANNEL, stream.toByteArray()));
        } catch (IOException ex) {
            service.plugin().getLogger().severe("An error occurred when attempting to communicate with proxied server! Code: M-0001");
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
}
