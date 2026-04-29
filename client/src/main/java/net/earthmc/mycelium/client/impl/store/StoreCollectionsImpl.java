package net.earthmc.mycelium.client.impl.store;

import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.api.store.StoreCollections;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.redis.collection.RedisRemoteDeque;
import net.earthmc.mycelium.client.redis.collection.RedisRemoteSet;
import org.jspecify.annotations.NullMarked;

import java.util.Set;
import java.util.concurrent.BlockingDeque;

@NullMarked
public class StoreCollectionsImpl implements StoreCollections {
    private final MyceliumClient client;
    private final String keyPrefix;

    public StoreCollectionsImpl(final MyceliumClient client, final String keyPrefix) {
        this.client = client;
        this.keyPrefix = keyPrefix + "col:"; // m:prod:store:col: + key
    }

    @Override
    public <T> Set<T> set(String key, JsonCodec<T> codec) {
        return new RedisRemoteSet<>(this.client, this.keyPrefix + key, codec);
    }

    @Override
    public <T> BlockingDeque<T> blockingDeque(String key, JsonCodec<T> codec) {
        return new RedisRemoteDeque<>(this.client, this.keyPrefix + key, codec);
    }
}
