package me.rochblondiaux.bukkitmessaging.api.message;

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

    void onMessage(@NotNull UUID sender, @NotNull M message);


}
