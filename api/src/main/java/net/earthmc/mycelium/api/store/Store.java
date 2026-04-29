package net.earthmc.mycelium.api.store;

import net.earthmc.mycelium.api.serialization.JsonCodec;
import org.jspecify.annotations.Nullable;

import java.time.temporal.TemporalAmount;
import java.util.Map;

/**
 * Provides an interface for persistent and shared storage.
 */
public interface Store {
    /**
     * Gets the value for the given key, or null if unset.
     *
     * @param key The key to get the value for.
     * @return The value at the given key, may be null.
     */
    @Nullable String get(String key);

    /**
     * Gets the value for the given key, and deserializes it using the given codec.
     *
     * @param key The key to get the value for.
     * @param codec The codec to use for deserializing the value, if it is present.
     * @return The deserialized value, or null if it is not present.
     * @param <T> The type for the value.
     */
    @Nullable <T> T get(String key, JsonCodec<T> codec);

    /**
     * Sets a key to the given value in the store.
     *
     * @param key The key to set the value for.
     * @param value The value to set.
     */
    void set(String key, String value);

    /**
     * Sets a key to the given value in the store for up to a maximum amount of time.
     *
     * @param key The key to set the value for.
     * @param value The value to set.
     * @param expiration The maximum lifetime for this value.
     */
    void set(String key, String value, TemporalAmount expiration);

    /**
     * Sets a key to the given value in the store.
     *
     * @param key The key to store the value at.
     * @param codec The codec to use to serialize the value.
     * @param value The value to set in the storage.
     * @param <T> The type for the value.
     */
    <T> void set(String key, JsonCodec<T> codec, T value);

    /**
     * Sets a key to the given value in the store for up to a maximum amount of time.
     *
     * @param key The key to store the value at.
     * @param codec The codec to use to serialize the value.
     * @param value The value to set in the storage.
     * @param expiration The maximum lifetime for this value.
     * @param <T> The type for the value.
     */
    <T> void set(String key, JsonCodec<T> codec, T value, TemporalAmount expiration);

    /**
     * Removes the mapping for the specified key from the storage.
     * @param key The key to remove the mapping for.
     * @return Whether a value was removed as a result of this operation.
     */
    boolean remove(String key);

    /**
     * {@return a map with a live representation of all key-value pairs stored}
     */
    Map<String, String> asMap();

    /**
     * Provides extra store-related methods related to collections.
     *
     * @return The store collections instance.
     */
    StoreCollections collections();
}
