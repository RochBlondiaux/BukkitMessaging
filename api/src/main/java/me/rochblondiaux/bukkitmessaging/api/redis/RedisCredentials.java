package me.rochblondiaux.bukkitmessaging.api.redis;

import lombok.Data;

/**
 * BukkitMessaging
 * 23/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
@Data(staticConstructor = "of")
public class RedisCredentials {

    private final String host;
    private final int port;
    private final String password;

}
