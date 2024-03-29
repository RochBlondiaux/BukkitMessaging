package me.rochblondiaux.bukkitmessaging.api.pipeline;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import lombok.RequiredArgsConstructor;
import me.rochblondiaux.bukkitmessaging.api.MessagingService;
import me.rochblondiaux.bukkitmessaging.api.ServerIdentifier;
import me.rochblondiaux.bukkitmessaging.api.message.BukkitMessage;

import java.util.ArrayList;
import java.util.UUID;

/**
 * BukkitMessaging
 * 23/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
@RequiredArgsConstructor
public class MessagingPipelineReader {

    private static final Gson GSON = new Gson();
    private static final JsonParser parser = new JsonParser();
    private final MessagingService service;

    public void read(String message) {
        try {
            final JsonObject label;
            try {
                label = (JsonObject) parser.parse(message);
            } catch (JsonSyntaxException ignored) {
                return;
            }

            final String clazz = label.get("class").getAsString();
            final UUID senderId = UUID.fromString(label.get("senderId").getAsString());
            final String senderName = label.get("senderName").getAsString();
            final ServerIdentifier sender = new ServerIdentifier(senderId, senderName);

            if (this.service.getUniqueId().equals(senderId))
                return;

            final Class<? extends BukkitMessage> messageClass = (Class<? extends BukkitMessage>) Class.forName(clazz);
            final BukkitMessage msg = GSON.fromJson(label.getAsJsonObject("content"), messageClass);
            this.service.listeners().getOrDefault(messageClass, new ArrayList<>()).forEach(listener -> listener.onMessage(sender, msg));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
