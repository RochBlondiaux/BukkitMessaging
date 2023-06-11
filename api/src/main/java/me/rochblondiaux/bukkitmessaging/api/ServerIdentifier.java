package me.rochblondiaux.bukkitmessaging.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

/**
 * BukkitMessaging
 * 14/05/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
@ToString
@EqualsAndHashCode
public class ServerIdentifier {

    private final UUID uniqueId;
    private final String name;

    public ServerIdentifier(UUID uniqueId, String name) {
        this.uniqueId = uniqueId;
        this.name = name;
    }

    public UUID uniqueId() {
        return uniqueId;
    }

    public String name() {
        return name;
    }

}
