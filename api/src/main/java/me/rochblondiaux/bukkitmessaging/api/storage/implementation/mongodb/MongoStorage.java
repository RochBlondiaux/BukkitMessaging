package me.rochblondiaux.bukkitmessaging.api.storage.implementation.mongodb;

import org.bson.UuidRepresentation;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;

import jdk.internal.joptsimple.internal.Strings;
import me.rochblondiaux.bukkitmessaging.api.storage.StorageCredentials;
import me.rochblondiaux.bukkitmessaging.api.storage.StorageImplementation;

/**
 * BukkitMessaging
 * 08/08/2023
 *
 * @author Roch Blondiaux (Kiwix).
 */
public class MongoStorage implements StorageImplementation {

    private final StorageCredentials configuration;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private final String prefix;
    private final String connectionUri;

    public MongoStorage(StorageCredentials configuration, String prefix, String connectionUri) {
        this.configuration = configuration;
        this.prefix = prefix;
        this.connectionUri = connectionUri;
    }


    @Override
    public String getImplementationName() {
        return "MongoDB";
    }

    @Override
    public void init() {
        MongoClientOptions.Builder options = MongoClientOptions.builder()
                .uuidRepresentation(UuidRepresentation.JAVA_LEGACY);

        if (!Strings.isNullOrEmpty(this.connectionUri)) {
            this.mongoClient = new MongoClient(new MongoClientURI(this.connectionUri, options));
        } else {
            MongoCredential credential = null;
            if (!Strings.isNullOrEmpty(this.configuration.getUsername())) {
                credential = MongoCredential.createCredential(
                        this.configuration.getUsername(),
                        this.configuration.getDatabase(),
                        Strings.isNullOrEmpty(this.configuration.getPassword()) ? null : this.configuration.getPassword().toCharArray()
                );
            }

            String[] addressSplit = this.configuration.getAddress().split(":");
            String host = addressSplit[0];
            int port = addressSplit.length > 1 ? Integer.parseInt(addressSplit[1]) : 27017;
            ServerAddress address = new ServerAddress(host, port);

            if (credential == null) {
                this.mongoClient = new MongoClient(address, options.build());
            } else {
                this.mongoClient = new MongoClient(address, credential, options.build());
            }
        }

        this.database = this.mongoClient.getDatabase(this.configuration.getDatabase());
    }

    @Override
    public void shutdown() {
        if (this.mongoClient != null) {
            this.mongoClient.close();
        }
    }

}
