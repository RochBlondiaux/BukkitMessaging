package me.rochblondiaux.bukkitmessaging.bukkit.listener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import lombok.RequiredArgsConstructor;
import me.rochblondiaux.bukkitmessaging.api.Constants;
import me.rochblondiaux.bukkitmessaging.api.cache.CacheService;
import me.rochblondiaux.bukkitmessaging.bukkit.adapter.BukkitMessagingAdapter;

/**
 * BukkitMessaging
 * 23/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
@RequiredArgsConstructor
public class ProxyListener implements PluginMessageListener {

    private final BukkitMessagingAdapter adapter;
    private final CacheService cacheService;

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (!channel.equals(Constants.CHANNEL))
            return;
        ByteArrayInputStream stream = new ByteArrayInputStream(message);
        DataInputStream in = new DataInputStream(stream);

        try {
            String subChannel = in.readUTF();
            if (subChannel.equalsIgnoreCase(Constants.CACHE_SUB_CHANNEL)) {
                cacheService.handleMessage(in.readUTF());
                return;
            } else if (!subChannel.equals(Constants.SUB_CHANNEL))
                return;
            String data = in.readUTF();
            adapter.service().pipeline().read(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
