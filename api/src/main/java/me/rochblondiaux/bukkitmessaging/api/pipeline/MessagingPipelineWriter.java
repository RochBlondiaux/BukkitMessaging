package me.rochblondiaux.bukkitmessaging.api.pipeline;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import me.rochblondiaux.bukkitmessaging.api.MessagingService;
import me.rochblondiaux.bukkitmessaging.api.message.BukkitMessage;
import org.jetbrains.annotations.NotNull;

/**
 * BukkitMessaging
 * 23/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
@RequiredArgsConstructor
public class MessagingPipelineWriter {

    private final MessagingService service;

    public String write(@NotNull BukkitMessage message) {
        final JsonObject label = new JsonObject();
        label.addProperty("class", message.getClass().getName());
        label.add("sender", JsonParser.parseString(this.service.getUniqueId().toString()));
        label.add("content", JsonParser.parseString(message.toJson()));
        return label.toString();
    }
}
