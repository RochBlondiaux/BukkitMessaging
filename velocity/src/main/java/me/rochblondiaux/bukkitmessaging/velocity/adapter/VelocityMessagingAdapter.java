package me.rochblondiaux.bukkitmessaging.velocity.adapter;

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
import me.rochblondiaux.bukkitmessaging.velocity.VelocityMessagingService;
import me.rochblondiaux.bukkitmessaging.velocity.listener.MessageListener;

/**
 * BukkitMessaging
 * 22/03/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class VelocityMessagingAdapter implements MessagingAdapter {

    private final VelocityMessagingService service;
    private final MessageListener listener;
    @Getter
    private final CacheService cacheService;

    public VelocityMessagingAdapter(VelocityMessagingService service) {
        this.service = service;
        this.cacheService = new CacheService(data -> {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(stream);
            try {
                out.writeUTF(Constants.CACHE_SUB_CHANNEL);
                out.writeUTF(data);
                service.server()
                        .getAllServers()
                        .stream()
                        .filter(server -> !server.sendPluginMessage(VelocityMessagingService.CHANNEL, stream.toByteArray()))
                        .filter(server -> !this.service.isCached(server, data))
                        .forEach(s -> this.service.cacheMessage(s, data));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        this.listener = new MessageListener(service, cacheService);
    }

    @Override
    public void init(@Nullable RedisCredentials credentials) {
        // Register channel
        service.server().getChannelRegistrar().register(VelocityMessagingService.CHANNEL);

        // Register listener
        service.server().getEventManager().register(service.plugin(), listener);
    }

    @Override
    public void unload() {
        // Unregister channel
        service.server().getChannelRegistrar().unregister(VelocityMessagingService.CHANNEL);

        // Unregister listener
        service.server().getEventManager().unregisterListener(service.plugin(), listener);
    }

    @Override
    public void publish(String message) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
            out.writeUTF(Constants.SUB_CHANNEL);
            out.writeUTF(message);

            service.server()
                    .getAllServers()
                    .stream()
                    .filter(server -> !server.sendPluginMessage(VelocityMessagingService.CHANNEL, stream.toByteArray()))
                    .filter(server -> !this.service.isCached(server, message))
                    .forEach(s -> this.service.cacheMessage(s, message));
        } catch (IOException ex) {
            ex.printStackTrace();
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

    public void publish(String name, String payload) {
        service.server()
                .getServer(name)
                .ifPresent(server -> {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(stream);
                    try {
                        out.writeUTF(Constants.SUB_CHANNEL);
                        out.writeUTF(payload);
                        server.sendPluginMessage(VelocityMessagingService.CHANNEL, stream.toByteArray());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
    }
}
