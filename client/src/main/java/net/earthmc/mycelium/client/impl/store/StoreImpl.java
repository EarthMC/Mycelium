package net.earthmc.mycelium.client.impl.store;

import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.api.store.Store;
import net.earthmc.mycelium.api.store.StoreCollections;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.serialization.RedisCodec;
import net.earthmc.mycelium.client.redis.RedisKey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import redis.clients.jedis.params.SetParams;

import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@NullMarked
public class StoreImpl implements Store {
    private final Map<String, String> LIVE_VIEW = new MapViewImpl(this);
    private final MyceliumClient client;
    private final String keyPrefix;

    private final StoreCollections collections;

    public StoreImpl(final MyceliumClient client) {
        this.client = client;
        this.keyPrefix = RedisKey.create(client, "store") + ":";
        this.collections = new StoreCollectionsImpl(client, this.keyPrefix);
    }

    @Override
    public @Nullable String get(String key) {
        return client.redis().get(keyPrefix + key);
    }

    @Override
    public <T> @Nullable T get(String key, JsonCodec<T> codec) {
        return Optional.ofNullable(this.get(key)).map(value -> RedisCodec.codecFor(codec).deserialize(value)).orElse(null);
    }

    @Override
    public void set(String key, String value) {
        client.redis().set(keyPrefix + key, value);
    }

    @Override
    public void set(String key, String value, TemporalAmount expiration) {
        client.redis().set(keyPrefix + key, value, SetParams.setParams().ex(Duration.from(expiration).getSeconds()));
    }

    @Override
    public <T> void set(String key, JsonCodec<T> codec, T value) {
        client.redis().set(keyPrefix + key, RedisCodec.codecFor(codec).serialize(value));
    }

    @Override
    public <T> void set(String key, JsonCodec<T> codec, T value, TemporalAmount expiration) {
        this.set(key, RedisCodec.codecFor(codec).serialize(value), expiration);
    }

    @Override
    public boolean remove(String key) {
        return client.redis().del(keyPrefix + key) > 0;
    }

    @Override
    public Map<String, String> asMap() {
        return LIVE_VIEW;
    }

    @Override
    public StoreCollections collections() {
        return this.collections;
    }

    private static final class MapViewImpl extends AbstractMap<String, String> {
        private final StoreImpl impl;

        public MapViewImpl(StoreImpl impl) {
            this.impl = impl;
        }

        @Override
        public Set<Entry<String, String>> entrySet() {
            throw new UnsupportedOperationException("Not yet supported."); // FIXME: implement using scan to find all keys in storage
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException("Not yet supported.");
        }

        @Override
        public @Nullable String put(String key, String value) {
            final String prev = impl.get(key);
            impl.set(key, value);

            return prev;
        }

        @Override
        public @Nullable String get(Object key) {
            return impl.get((String) key);
        }

        @Override
        public boolean equals(Object o) {
            return this == o; // Only one instance of this class exists
        }
    }
}
