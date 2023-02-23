package me.rochblondiaux.bukkitmessaging.api.message;

import com.google.gson.Gson;

/**
 * BukkitMessaging
 * 23/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public interface BukkitMessage {

    Gson GSON = new Gson();

    default String toJson() {
        return GSON.toJson(this);
    }

    static <T extends BukkitMessage> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }
}
