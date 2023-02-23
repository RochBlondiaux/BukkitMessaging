package me.rochblondiaux.bukkitmessaging.api.pipeline;

import me.rochblondiaux.bukkitmessaging.api.MessagingService;
import me.rochblondiaux.bukkitmessaging.api.message.BukkitMessage;
import org.jetbrains.annotations.NotNull;

/**
 * BukkitMessaging
 * 23/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class MessagingPipeline {

    private final MessagingPipelineWriter writer;
    private final MessagingPipelineReader reader;

    public MessagingPipeline(@NotNull MessagingService service) {
        this.reader = new MessagingPipelineReader(service);
        this.writer = new MessagingPipelineWriter(service);
    }

    public String write(@NotNull BukkitMessage message) {
        return this.writer.write(message);
    }

    public void read(@NotNull String message) {
        this.reader.read(message);
    }
}
