package me.rochblondiaux.bukkitmessaging.velocity;

import lombok.Data;

/**
 * BukkitMessaging
 * 14/05/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
@Data
public class CachedMessage {

    private final String serverName;
    private final String payload;

}
