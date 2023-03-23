package me.rochblondiaux.bukkitmessaging.bungeecord.adapter;

import me.rochblondiaux.bukkitmessaging.api.Constants;
import me.rochblondiaux.bukkitmessaging.api.adapter.MessagingAdapter;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisCredentials;
import me.rochblondiaux.bukkitmessaging.bungeecord.BungeecordMessagingService;
import me.rochblondiaux.bukkitmessaging.bungeecord.listener.MessageListener;
import net.md_5.bungee.api.ProxyServer;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * BukkitMessaging
 * 23/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class BungeecordMessagingAdapter implements MessagingAdapter {


    private final BungeecordMessagingService service;
    private final MessageListener listener;

    public BungeecordMessagingAdapter(BungeecordMessagingService service) {
        this.service = service;
        this.listener = new MessageListener(service);
    }

    @Override
    public void init(@Nullable RedisCredentials credentials) {
        // Register listener
        service.plugin().getProxy().getPluginManager().registerListener(service.plugin(), listener);
    }

    @Override
    public void unload() {
        // Unregister listener
        service.plugin().getProxy().getPluginManager().unregisterListener(listener);
    }

    @Override
    public void publish(String message) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(Constants.SUB_CHANNEL);
            out.writeUTF(message);
            ProxyServer.getInstance().getServers().values().forEach(server -> server.sendData(Constants.CHANNEL, stream.toByteArray()));
        } catch (IOException ex) {
            service.plugin().getLogger().severe("An error occurred when attempting to communicate with proxied server! Code: M-0001");
        }
    }
}
