package me.rochblondiaux.bukkitmessaging.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import lombok.RequiredArgsConstructor;
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
            String data = in.readUTF();
            service.pipeline().read(data);
        } catch (IOException ex) {
            //service.server().(Level.SEVERE, "Unable to decode bukkit server message! Code: L-0001", ex);
        }
    }

}
