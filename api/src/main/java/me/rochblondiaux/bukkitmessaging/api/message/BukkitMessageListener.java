package me.rochblondiaux.bukkitmessaging.api.message;

import me.rochblondiaux.bukkitmessaging.api.ServerIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * BukkitMessaging
 * 23/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
@FunctionalInterface
public interface BukkitMessageListener<M extends BukkitMessage> {

    void onMessage(@NotNull ServerIdentifier senderId, @NotNull M message);


}
