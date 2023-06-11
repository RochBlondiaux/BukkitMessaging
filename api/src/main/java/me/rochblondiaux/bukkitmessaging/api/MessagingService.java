package me.rochblondiaux.bukkitmessaging.api;

import me.rochblondiaux.bukkitmessaging.api.adapter.MessagingAdapter;
import me.rochblondiaux.bukkitmessaging.api.message.BukkitMessage;
import me.rochblondiaux.bukkitmessaging.api.message.BukkitMessageListener;
import me.rochblondiaux.bukkitmessaging.api.pipeline.MessagingPipeline;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisCredentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * BukkitMessaging
 * 23/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public abstract class MessagingService {

    protected final ServerIdentifier identifier;
    protected final Type type;
    protected final RedisCredentials credentials;
    protected final Map<Class<? extends BukkitMessage>, List<BukkitMessageListener>> listeners;
    protected final MessagingPipeline pipeline;

    public MessagingService(@NotNull String name, @NotNull Type type, @Nullable RedisCredentials credentials) {
        this.identifier = new ServerIdentifier(UUID.randomUUID(), name);
        this.type = type;
        this.credentials = credentials;
        this.pipeline = new MessagingPipeline(this);
        this.listeners = new HashMap<>();
    }

    public UUID getUniqueId() {
        return this.identifier.uniqueId();
    }

    public String name() {
        return this.identifier.name();
    }

    public ServerIdentifier identifier() {
        return this.identifier;
    }

    public void publish(@NotNull BukkitMessage message) {
        this.adapter().publish(this.pipeline.write(message));
    }

    public <T extends BukkitMessage> void register(@NotNull Class<T> messageClass, @NotNull BukkitMessageListener<T> listener) {
        this.listeners.computeIfAbsent(messageClass, k -> new ArrayList<>()).add(listener);
    }

    public void unregister(@NotNull BukkitMessageListener<?> listener) {
        this.listeners.values().forEach(listeners -> listeners.remove(listener));
    }

    public abstract @NotNull MessagingAdapter adapter();

    public MessagingPipeline pipeline() {
        return this.pipeline;
    }

    public Map<Class<? extends BukkitMessage>, List<BukkitMessageListener>> listeners() {
        return this.listeners;
    }

    public enum Type {
        PROXY,
        REDIS
    }
}
