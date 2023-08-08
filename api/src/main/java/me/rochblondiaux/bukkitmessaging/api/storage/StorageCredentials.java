package me.rochblondiaux.bukkitmessaging.api.storage;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * BukkitMessaging
 * 08/08/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
@Getter
@NoArgsConstructor
@Setter
public class StorageCredentials {

    @SerializedName("type")
    private StorageType type;
    @SerializedName("host")
    private String address;
    @SerializedName("database")
    private String database;
    @SerializedName("username")
    private String username;
    @SerializedName("password")
    private String password;
    @SerializedName("pool-settings")
    @Getter
    private StoragePoolSettings poolSettings;

    public String getAddress() {
        return Objects.requireNonNull(this.address, "address");
    }

    public String getDatabase() {
        return Objects.requireNonNull(this.database, "database");
    }

    public String getUsername() {
        return Objects.requireNonNull(this.username, "username");
    }

    public String getPassword() {
        return Objects.requireNonNull(this.password, "password");
    }
}
