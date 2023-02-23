package me.rochblondiaux.bukkitmessaging.api.adapter;

import me.rochblondiaux.bukkitmessaging.api.redis.RedisCredentials;
import org.jetbrains.annotations.Nullable;

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
}
