package me.rochblondiaux.bukkitmessaging.bungeecord.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import me.rochblondiaux.bukkitmessaging.bungeecord.BungeecordMessagingService;
import net.md_5.bungee.api.connection.ProxiedPlayer;
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
            if (!subChannel.equalsIgnoreCase(BungeecordMessagingService.SUB_CHANNEL))
                return;
            String data = in.readUTF();
            service.pipeline().read(data);
        } catch (IOException ex) {
            service.plugin().getLogger().log(Level.SEVERE, "Unable to decode bukkit server message! Code: L-0001", ex);
        }
    }
}
