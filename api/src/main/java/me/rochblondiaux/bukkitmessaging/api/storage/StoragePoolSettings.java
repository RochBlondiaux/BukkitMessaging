package me.rochblondiaux.bukkitmessaging.api.storage;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * BukkitMessaging
 * 08/08/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
@Setter
@Getter
@NoArgsConstructor
public class StoragePoolSettings {

    @SerializedName("maximum-pool-size")
    private int maxPoolSize;
    @SerializedName("minimum-idle")
    private int minIdleConnections;
    @SerializedName("maximum-lifetime")
    private int maxLifetime;
    @SerializedName("keep-alive-time")
    private int keepAliveTime;
    @SerializedName("connection-timeout")
    private int connectionTimeout;
    @SerializedName("properties")
    private Map<String, String> properties;

}
