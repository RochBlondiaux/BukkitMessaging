package me.rochblondiaux.bukkitmessaging.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import lombok.RequiredArgsConstructor;
import me.rochblondiaux.bukkitmessaging.api.Constants;
import me.rochblondiaux.bukkitmessaging.velocity.VelocityMessagingService;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * BukkitMessaging
 * 22/03/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
@RequiredArgsConstructor
public class MessageListener {

    private final VelocityMessagingService service;

    @Subscribe
    public void onMessage(PluginMessageEvent e) {
        if (!e.getIdentifier().equals(VelocityMessagingService.CHANNEL))
            return;
        ByteArrayInputStream stream = new ByteArrayInputStream(e.getData());
        DataInputStream in = new DataInputStream(stream);
        try {
            String subChannel = in.readUTF();
            if (!subChannel.equalsIgnoreCase(Constants.SUB_CHANNEL))
                return;
            String data = in.readUTF();
            service.pipeline().read(data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
