package me.rochblondiaux.bukkitmessaging.api.redis;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.rochblondiaux.bukkitmessaging.api.MessagingService;
import me.rochblondiaux.bukkitmessaging.api.adapter.MessagingAdapter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * BukkitMessaging
 * 23/02/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
@RequiredArgsConstructor
public class RedisMessagingAdapter implements MessagingAdapter {

    private final MessagingService service;
    @Getter
    private JedisPool pool;
    private Thread thread;
    private RedisListener listener;

    @Override
    public void init(@Nullable RedisCredentials credentials) {
        if (credentials == null)
            throw new RuntimeException("Please provide redis credentials in order to use the redis adapter");

        final JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(128);
        config.setMaxIdle(16);
        config.setMinIdle(8);
        config.setBlockWhenExhausted(true);
        config.setMaxWait(Duration.ofSeconds(5));
        config.setTestWhileIdle(true);
        config.setMinEvictableIdleTime(Duration.ofSeconds(30));
        config.setTimeBetweenEvictionRuns(Duration.ofSeconds(15));
        config.setNumTestsPerEvictionRun(-1);

        String host = credentials.getHost();
        int port = credentials.getPort();
        String password = credentials.getPassword();
        if (host.isEmpty())
            throw new RuntimeException("Please provide a redis host in order to use the redis adapter");

        if (password.isEmpty())
            pool = new JedisPool(config, host, port);
        else
            pool = new JedisPool(config, host, port, 0, password);

        if (this.pool.isClosed())
            throw new RuntimeException("Failed to connect to redis server");

        this.listener = new RedisListener(this.service);
        this.thread = new Thread(() -> {
            try (final Jedis jedis = this.pool.getResource()) {
                jedis.subscribe(RedisMessagingAdapter.this.listener, "bukkit-messaging");
            } catch (JedisConnectionException exception) {
                exception.printStackTrace();
                RedisMessagingAdapter.this.unload();
                RedisMessagingAdapter.this.init(credentials);
            }
        }, "bukkit-jedis-thread");
        this.thread.setDaemon(true);
        this.thread.start();
    }

    @Override
    public void publish(String message) {
        if (!this.thread.isAlive())
            return;
        if (this.pool.isClosed())
            throw new RuntimeException("Failed to connect to redis server");

        try (final Jedis jedis = this.pool.getResource()) {
            jedis.publish("bukkit-messaging", message);
        }
    }

    @Override
    public void set(String key, String value) {
        try (final Jedis jedis = this.pool.getResource()) {
            jedis.set(key, value);
        }
    }

    @Override
    public void set(String key, String value, int ttl) {
        try (final Jedis jedis = this.pool.getResource()) {
            jedis.setex(key, ttl, value);
        }
    }

    @Override
    public <T> Optional<T> get(String key) {
        try (final Jedis jedis = this.pool.getResource()) {
            return Optional.ofNullable((T) jedis.get(key));
        }
    }

    @Override
    public void remove(String key) {
        try (final Jedis jedis = this.pool.getResource()) {
            jedis.del(key);
        }
    }

    @Override
    public boolean has(String key) {
        try (final Jedis jedis = this.pool.getResource()) {
            return jedis.exists(key);
        }
    }

    @Override
    public Set<String> keys(String pattern) {
        try (final Jedis jedis = this.pool.getResource()) {
            return jedis.keys(pattern);
        }
    }

    @Override
    public void unload() {
        if (this.listener.isSubscribed())
            this.listener.unsubscribe();

        if (this.pool != null)
            this.pool.close();

        if (this.thread != null && this.thread.isAlive())
            this.thread.interrupt();
    }

    @RequiredArgsConstructor
    private static class RedisListener extends JedisPubSub {
        private final MessagingService service;

        @Override
        public void onMessage(String channel, String message) {
            this.service.pipeline().read(message);
        }
    }
}
