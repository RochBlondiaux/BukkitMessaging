package me.rochblondiaux.bukkitmessaging.api.storage;

import java.awt.*;
import java.util.Collections;
import java.util.Map;

/**
 * BukkitMessaging
 * 08/08/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public interface StorageImplementation {

    String getImplementationName();

    void init() throws Exception;

    void shutdown();

    default Map<Component, Component> getMeta() {
        return Collections.emptyMap();
    }
}