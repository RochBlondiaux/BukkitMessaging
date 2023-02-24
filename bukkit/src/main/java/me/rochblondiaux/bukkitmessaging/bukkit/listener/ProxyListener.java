package me.rochblondiaux.bukkitmessaging.bukkit.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import me.rochblondiaux.bukkitmessaging.bukkit.adapter.BukkitMessagingAdapter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * BukkitMessaging
 * 23/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
@RequiredArgsConstructor
public class ProxyListener implements PluginMessageListener {

    private final BukkitMessagingAdapter adapter;

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (!channel.equals("BungeeCord"))
            return;
        ByteArrayInputStream stream = new ByteArrayInputStream(message);
        DataInputStream in = new DataInputStream(stream);

        try {
            String subchannel = in.readUTF();
            if (!subchannel.equals(BukkitMessagingAdapter.SUB_CHANNEL))
                return;
            String data = in.readUTF();
            adapter.service().pipeline().read(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
