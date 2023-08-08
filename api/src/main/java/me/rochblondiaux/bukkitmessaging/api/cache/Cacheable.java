package me.rochblondiaux.bukkitmessaging.api.cache;

import com.google.gson.Gson;

/**
 * BukkitMessaging
 * 01/08/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public interface Cacheable {

    Gson GSON = new Gson();

    default String toJson() {
        return GSON.toJson(this);
    }

    static <T extends Cacheable> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }
}
