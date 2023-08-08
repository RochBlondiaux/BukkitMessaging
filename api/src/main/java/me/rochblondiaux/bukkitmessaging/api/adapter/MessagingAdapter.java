package me.rochblondiaux.bukkitmessaging.api.adapter;

import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import me.rochblondiaux.bukkitmessaging.api.redis.RedisCredentials;

/**
 * BukkitMessaging
 * 23/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public interface MessagingAdapter {

    void init(@Nullable RedisCredentials credentials);

    void unload();

    void publish(String message);

    void set(String key, String value);

    void set(String key, String value, int ttl);

    <T> Optional<T> get(String key);

    void remove(String key);

    boolean has(String key);

    Set<String> keys(String pattern);

}
