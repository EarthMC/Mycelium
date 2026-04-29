package net.earthmc.mycelium.api.store;

import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.api.store.collection.BlockingRelativeDeque;

import java.util.Deque;
import java.util.Set;

/**
 * Provides data store backed wrappers for
 */
public interface StoreCollections {
    /**
     * Returns a new data store backed set wrapper.
     *
     * @param key The key that uniquely identifies this set across the keyspace.
     * @param codec The codec to use for (de)serializing values.
     * @return A new set wrapper.
     * @param <T> The type for elements contained within the set.
     */
    <T> Set<T> set(final String key, final JsonCodec<T> codec);

    /**
     * Returns a new data store backed blocking deque wrapper.
     *
     * @param key The key that uniquely identifies this deque across the keyspace.
     * @param codec The codec to use for (de)serializing values.
     * @return A new deque wrapper.
     * @param <T> The type for elements contained within the deque.
     */
    <T> BlockingRelativeDeque<T> blockingDeque(final String key, final JsonCodec<T> codec);

    /**
     * Returns a new data store backed deque wrapper.
     *
     * @param key The key that uniquely identifies this deque across the keyspace.
     * @param codec The codec to use for (de)serializing values.
     * @return A new deque wrapper.
     * @param <T> The type for elements contained within the deque.
     */
    default <T> Deque<T> deque(final String key, final JsonCodec<T> codec) {
        return blockingDeque(key, codec);
    }
}
