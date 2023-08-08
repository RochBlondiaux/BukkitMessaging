package me.rochblondiaux.bukkitmessaging.velocity.listener;

import java.io.*;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelMessageSource;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import lombok.RequiredArgsConstructor;
import me.rochblondiaux.bukkitmessaging.api.Constants;
import me.rochblondiaux.bukkitmessaging.api.cache.CacheService;
import me.rochblondiaux.bukkitmessaging.velocity.VelocityMessagingService;

/**
 * BukkitMessaging
 * 22/03/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
@RequiredArgsConstructor
public class MessageListener {

    private final VelocityMessagingService service;
    private final CacheService cacheService;

    @Subscribe
    public void onMessage(PluginMessageEvent e) {
        if (!(e.getSource() instanceof ServerConnection) || !e.getIdentifier().equals(VelocityMessagingService.CHANNEL))
            return;
        ByteArrayInputStream stream = new ByteArrayInputStream(e.getData());
        DataInputStream in = new DataInputStream(stream);
        try {
            String subChannel = in.readUTF();
            if (subChannel.equalsIgnoreCase(Constants.CACHE_SUB_CHANNEL)) {
                String data = in.readUTF();
                // Handle
                cacheService.handleMessage(data);

                // Forward
                this.forwardMessage(e.getSource(), subChannel, data);
                return;
            } else if (!subChannel.equalsIgnoreCase(Constants.SUB_CHANNEL))
                return;

            String data = in.readUTF();

            // Handle
            service.pipeline().read(data);

            // Forward
            this.forwardMessage(e.getSource(), subChannel, data);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean isSender(ChannelMessageSource source, RegisteredServer server) {
        return source instanceof RegisteredServer && ((RegisteredServer) source).getServerInfo().equals(server.getServerInfo());
    }

    private void forwardMessage(ChannelMessageSource source, String channel, String message) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
            out.writeUTF(channel);
            out.writeUTF(message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        service.server()
                .getAllServers()
                .stream()
                .filter(server -> !isSender(source, server))
                .forEach(server -> server.sendPluginMessage(VelocityMessagingService.CHANNEL, stream.toByteArray()));
    }

}
