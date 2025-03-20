package net.earthmc.mycelium.api.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A utility class for creating codecs for maps with different key & value codecs.
 *
 * @see CollectionCodecs
 */
public final class MapCodecs {
    /**
     * Constructs a new map codec using a default linked map implementation.
     *
     * @param keyCodec The codec to use for serializing keys.
     * @param valueCodec The codec to use for serializing values.
     * @return
     * @param <K> Key type, must be serializable as a json primitive.
     * @param <V> Value type.
     */
    public static <K, V> JsonCodec<Map<K, V>> map(JsonCodec<K> keyCodec, JsonCodec<V> valueCodec) {
        return create(keyCodec, valueCodec, LinkedTreeMap.class, LinkedTreeMap::new);
    }

    /**
     * Constructs a new map codec using a concurrent map implementation.
     *
     * @param keyCodec The codec to use for serializing keys.
     * @param valueCodec The codec to use for serializing values.
     * @return
     * @param <K> Key type, must be serializable as a json primitive.
     * @param <V> Value type.
     */
    public static <K, V> JsonCodec<Map<K, V>> concurrent(JsonCodec<K> keyCodec, JsonCodec<V> valueCodec) {
        return create(keyCodec, valueCodec, ConcurrentHashMap.class, ConcurrentHashMap::new);
    }

    /**
     * Constructs a new map codec.
     *
     * @param keyCodec The codec to use for serializing keys.
     * @param valueCodec The codec to use for serializing values.
     * @param mapType The class of the map implementation.
     * @param mapSupplier A supplier that creates new instances of the map implementation.
     * @return A new map codec for the given codecs.
     * @param <K> Key type, must be serializable as a json primitive.
     * @param <V> Value type.
     * @param <M> Map type.
     */
    public static <K, V, M extends Map<K, V>> JsonCodec<M> create(JsonCodec<K> keyCodec, JsonCodec<V> valueCodec, Class<?> mapType, Supplier<M> mapSupplier) {
        return new MapCodec<>(keyCodec, valueCodec, TypeToken.getParameterized(mapType, keyCodec.type(), valueCodec.type()).getType(), mapSupplier);
    }

    private record MapCodec<K, V, M extends Map<K, V>>(JsonCodec<K> keyCodec, JsonCodec<V> valueCodec, Type type, Supplier<M> mapSupplier) implements JsonCodec<M> {
        @Override
        public M deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!json.isJsonObject()) {
                throw new JsonParseException("Supplied json element is not a json object (got '" + json + "')");
            }

            final M map = mapSupplier.get();

            final JsonObject object = json.getAsJsonObject();

            for (final Map.Entry<String, JsonElement> entry : object.entrySet()) {
                final K key = context.deserialize(new JsonPrimitive(entry.getKey()), this.keyCodec.type());
                final V value = context.deserialize(entry.getValue(), this.valueCodec.type());

                map.put(key, value);
            }

            return map;
        }

        @Override
        public JsonElement serialize(M src, Type typeOfSrc, JsonSerializationContext context) {
            final JsonObject object = new JsonObject();

            for (final Map.Entry<K, V> entry : src.entrySet()) {
                final JsonElement key = context.serialize(entry.getKey(), this.keyCodec.type());
                final JsonElement value = context.serialize(entry.getValue(), this.valueCodec.type());

                object.add(key.getAsString(), value);
            }

            return object;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{" + this.type().getTypeName() + "}";
        }
    }
}
