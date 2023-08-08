package me.rochblondiaux.bukkitmessaging.api.storage.implementation.sql;

import java.sql.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jetbrains.annotations.Nullable;

import lombok.RequiredArgsConstructor;
import me.rochblondiaux.bukkitmessaging.api.MessagingService;
import me.rochblondiaux.bukkitmessaging.api.adapter.MessagingAdapter;
import me.rochblondiaux.bukkitmessaging.api.redis.RedisCredentials;

/**
 * BukkitMessaging
 * 08/08/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
@RequiredArgsConstructor
public class SQLMessagingAdapter implements MessagingAdapter {

    private final MessagingService service;
    private final SqlStorage storage;
    private final String messagesTable;
    private final String cacheTable;
    private long lastId = -1;
    private boolean closed;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void init(@Nullable RedisCredentials credentials) {
        try (Connection c = this.storage.getConnection()) {
            // init messages table
            String createStatement = "CREATE TABLE IF NOT EXISTS `" + messagesTable + "` (`id` INT AUTO_INCREMENT NOT NULL, `time` TIMESTAMP NOT NULL, `msg` TEXT NOT NULL, PRIMARY KEY (`id`)) DEFAULT CHARSET = utf8mb4";
            try (Statement s = c.createStatement()) {
                try {
                    s.execute(createStatement);
                } catch (SQLException e) {
                    if (e.getMessage().contains("Unknown character set")) {
                        // try again
                        s.execute(createStatement.replace("utf8mb4", "utf8"));
                    } else {
                        throw e;
                    }
                }
            }

            // init cache table
            createStatement = "CREATE TABLE IF NOT EXISTS `" + cacheTable + "` (`key` VARCHAR(255) NOT NULL, `value` TEXT NOT NULL, `ttl` INT NOT NULL, `created_at` TIMESTAMP NOT NULL, PRIMARY KEY (`key`)) DEFAULT CHARSET = utf8mb4";
            try (Statement s = c.createStatement()) {
                try {
                    s.execute(createStatement);
                } catch (SQLException e) {
                    if (e.getMessage().contains("Unknown character set")) {
                        // try again
                        s.execute(createStatement.replace("utf8mb4", "utf8"));
                    } else {
                        throw e;
                    }
                }
            }

            // pull last id
            try (PreparedStatement ps = c.prepareStatement("SELECT MAX(`id`) as `latest` FROM `" + messagesTable + "`")) {
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        this.lastId = rs.getLong("latest");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unload() {
        this.lock.writeLock().lock();
        try {
            this.closed = true;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public void publish(String message) {
        this.lock.readLock().lock();
        if (this.closed) {
            this.lock.readLock().unlock();
            return;
        }

        try (Connection c = this.storage.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO `" + messagesTable + "` (`time`, `msg`) VALUES(NOW(), ?)")) {
                ps.setString(1, message);
                ps.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void pollMessages() {
        this.lock.readLock().lock();
        if (this.closed) {
            this.lock.readLock().unlock();
            return;
        }

        try (Connection c = this.storage.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT `id`, `msg` FROM `" + messagesTable + "` WHERE `id` > ? AND (NOW() - `time` < 30)")) {
                ps.setLong(1, this.lastId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long id = rs.getLong("id");
                        this.lastId = Math.max(this.lastId, id);

                        String message = rs.getString("msg");
                        this.service.pipeline().read(message);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void runHousekeeping() {
        this.lock.readLock().lock();
        if (this.closed) {
            this.lock.readLock().unlock();
            return;
        }

        try (Connection c = this.storage.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM `" + messagesTable + "` WHERE (NOW() - `time` > 60)")) {
                ps.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public void set(String key, String value) {
        try (Connection connection = this.storage.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("INSERT INTO `" + cacheTable + "` (`key`, `value`, `ttl`, `created_at`) VALUES(?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE `value` = ?, `ttl` = ?, `created_at` = NOW()")) {
                ps.setString(1, key);
                ps.setString(2, value);
                ps.setInt(3, 0);
                ps.setString(4, value);
                ps.setInt(5, 0);
                ps.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void set(String key, String value, int ttl) {
        try (Connection connection = this.storage.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("INSERT INTO `" + cacheTable + "` (`key`, `value`, `ttl`, `created_at`) VALUES(?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE `value` = ?, `ttl` = ?, `created_at` = NOW()")) {
                ps.setString(1, key);
                ps.setString(2, value);
                ps.setInt(3, ttl);
                ps.setString(4, value);
                ps.setInt(5, ttl);
                ps.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> Optional<T> get(String key) {
        try (Connection connection = this.storage.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("SELECT `value`, `ttl`, `created_at` FROM `" + cacheTable + "` WHERE `key` = ?")) {
                ps.setString(1, key);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String value = rs.getString("value");
                        int ttl = rs.getInt("ttl");
                        Timestamp createdAt = rs.getTimestamp("created_at");

                        if (ttl > 0) {
                            if (createdAt.getTime() + (ttl * 1000) < System.currentTimeMillis()) {
                                // expired
                                return Optional.empty();
                            }
                        }

                        return Optional.of((T) value);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public void remove(String key) {
        try (Connection connection = this.storage.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM `" + cacheTable + "` WHERE `key` = ?")) {
                ps.setString(1, key);
                ps.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean has(String key) {
        try (Connection connection = this.storage.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("SELECT `value`, `ttl`, `created_at` FROM `" + cacheTable + "` WHERE `key` = ?")) {
                ps.setString(1, key);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int ttl = rs.getInt("ttl");
                        Timestamp createdAt = rs.getTimestamp("created_at");

                        if (ttl > 0) {
                            if (createdAt.getTime() + (ttl * 1000) < System.currentTimeMillis()) {
                                // expired
                                return false;
                            }
                        }

                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    @Override
    public Set<String> keys(String pattern) {
        Set<String> keys = new HashSet<>();
        try (Connection connection = this.storage.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("SELECT `key` FROM `" + cacheTable + "` WHERE `key` LIKE ?")) {
                ps.setString(1, pattern.replace("*", "%"));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        keys.add(rs.getString("key"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return keys;
    }
}
