package me.rochblondiaux.bukkitmessaging.api.cache;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import lombok.Getter;

/**
 * BukkitMessaging
 * 30/07/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class CacheService {

    @Getter
    private final List<CachedObject> data = new CopyOnWriteArrayList<>();
    @Getter
    private final Queue<CachedObject> queue = new ConcurrentLinkedQueue<>();
    private final Consumer<String> sendMessage;

    public CacheService(Consumer<String> sendMessage) {
        this.sendMessage = sendMessage;
    }

    public void set(String key, String value) {
        this.data.removeIf(object -> object.getKey().equals(key));
        this.data.add(new CachedObject(key, value));
        sendMessage("set", new CachedObject(key, value));
    }

    public void set(String key, String value, int ttl) {
        this.data.removeIf(object -> object.getKey().equals(key));
        this.data.add(new CachedObject(key, value, ttl));
        sendMessage("set", new CachedObject(key, value, ttl));
    }

    public <T> Optional<T> get(String key) {
        Optional<T> opt = this.data.stream()
                .filter(object -> object.getKey().equals(key))
                .map(object -> (T) object.getValue())
                .findFirst();
        if (!opt.isPresent()) {
            queue.add(new CachedObject(key, null));
        }
        return opt;
    }

    public void remove(String key) {
        this.data.removeIf(object -> object.getKey().equals(key));
        sendMessage("remove", new CachedObject(key, null));
    }

    public Set<String> keys(String pattern) {
        return this.data.stream()
                .map(CachedObject::getKey)
                .filter(key -> key.startsWith(pattern.replace("*", "")))
                .collect(Collectors.toSet());
    }

    public boolean has(String key) {
        return this.data.stream().anyMatch(object -> object.getKey().equals(key));
    }

    public void tick() {
        this.data.removeIf(object -> object.isExpired() && object.getTtl() > 0);
    }

    public void sendMessage(String prefix, CachedObject object) {
        sendMessage.accept(String.format("%s|%s|%s|%d|%d", prefix, object.getKey(), object.getValue(), object.getCreationTime(), object.getTtl()));
    }

    public void handleMessage(String data) {
        String[] args = data.split("\\|");
        if (args.length != 5) {
            return;
        }
        String prefix = args[0];
        String key = args[1];
        String value = args[2];
        long creationTime = Long.parseLong(args[3]);
        int ttl = Integer.parseInt(args[4]);

        if (prefix.equals("set")) {
            this.data.removeIf(object -> object.getKey().equals(key));
            if (value != null && !value.equals("null"))
                this.data.add(new CachedObject(key, value, creationTime, ttl));
        } else if (prefix.equals("remove")) {
            this.data.removeIf(object -> object.getKey().equals(key));
        }
    }
}
