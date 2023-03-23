package me.rochblondiaux.bukkitmessaging.bungeecord.listener;

import lombok.RequiredArgsConstructor;
import me.rochblondiaux.bukkitmessaging.bungeecord.BungeecordMessagingService;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;

/**
 * BukkitMessaging
 * 23/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
@RequiredArgsConstructor
public class MessageListener implements Listener {

    private final BungeecordMessagingService service;

    @EventHandler
    public void on(PluginMessageEvent e) {
        if (!e.getTag().equalsIgnoreCase("BungeeCord"))
            return;
        ByteArrayInputStream stream = new ByteArrayInputStream(e.getData());
        DataInputStream in = new DataInputStream(stream);
        try {
            String subChannel = in.readUTF();
            if (!subChannel.equalsIgnoreCase(BungeecordMessagingService.SUB_CHANNEL)) {
                service.plugin().getLogger().warning("Received message on wrong subchannel! Expected: " + BungeecordMessagingService.SUB_CHANNEL + " Received: " + subChannel);
                return;
            }
            String data = in.readUTF();
            service.pipeline().read(data);
        } catch (IOException ex) {
            service.plugin().getLogger().log(Level.SEVERE, "Unable to decode bukkit server message! Code: L-0001", ex);
        }
    }
}
