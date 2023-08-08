package me.rochblondiaux.bukkitmessaging.api.storage;

import java.awt.*;
import java.util.Collections;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import me.rochblondiaux.bukkitmessaging.api.adapter.MessagingAdapter;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisCredentials;

/**
 * BukkitMessaging
 * 08/08/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public interface StorageImplementation {

    String getImplementationName();

    void init() throws Exception;

    void shutdown();

    default Map<Component, Component> getMeta() {
        return Collections.emptyMap();
    }


    @Override
    default void init(@Nullable RedisCredentials credentials) {
    }

    @Override
    default void unload() {
    }
}