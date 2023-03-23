package me.rochblondiaux.bukkitmessaging.velocity.adapter;

import me.rochblondiaux.bukkitmessaging.api.Constants;
import me.rochblondiaux.bukkitmessaging.api.adapter.MessagingAdapter;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisCredentials;
import me.rochblondiaux.bukkitmessaging.velocity.VelocityMessagingService;
import me.rochblondiaux.bukkitmessaging.velocity.listener.MessageListener;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * BukkitMessaging
 * 22/03/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class VelocityMessagingAdapter implements MessagingAdapter {

    private final VelocityMessagingService service;
    private final MessageListener listener;

    public VelocityMessagingAdapter(VelocityMessagingService service) {
        this.service = service;
        this.listener = new MessageListener(service);
    }

    @Override
    public void init(@Nullable RedisCredentials credentials) {
        // Register listener
        service.server().getEventManager().register(service.plugin(), listener);
    }

    @Override
    public void unload() {
        // Unregister listener
        service.server().getEventManager().unregisterListener(service.plugin(), listener);
    }

    @Override
    public void publish(String message) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);

        try {
            out.writeUTF(Constants.SUB_CHANNEL);
            out.writeUTF(message);
            service.server().getAllServers().forEach(s -> s.sendPluginMessage(VelocityMessagingService.CHANNEL, stream.toByteArray()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
