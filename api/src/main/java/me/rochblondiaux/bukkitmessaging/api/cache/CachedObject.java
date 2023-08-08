package me.rochblondiaux.bukkitmessaging.api.cache;

import lombok.Data;

/**
 * BukkitMessaging
 * 30/07/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
@Data
public class CachedObject {

    private final String key;
    private final String value;
    private final long creationTime;
    private final int ttl;

    public CachedObject(String key, String value, int ttl) {
        this.key = key;
        this.value = value;
        this.creationTime = System.currentTimeMillis();
        this.ttl = ttl;
    }

    public CachedObject(String key, String value) {
        this.key = key;
        this.value = value;
        this.creationTime = System.currentTimeMillis();
        this.ttl = 0;
    }


    public CachedObject(String key, String value, long creationTime, int ttl) {
        this.key = key;
        this.value = value;
        this.creationTime = creationTime;
        this.ttl = ttl;
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - creationTime) > ttl * 1000L;
    }

}
