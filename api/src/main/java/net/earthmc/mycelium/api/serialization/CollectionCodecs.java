package net.earthmc.mycelium.api.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Provides utility functions for transforming codecs into a collection-based variant.
 *
 * <p>
 * Example:
 * <pre>
 * {@code
 * JsonCodec<List<String>> stringListCodec = CollectionCodecs.list(Codecs.STRING));
 * }
 * </pre>
 *
 * @see MapCodecs
 */
public final class CollectionCodecs {
    /**
     * Creates a new array list based collection codec.
     *
     * @param codec The codec to use for serializing collection elements.
     * @return A new list codec.
     * @param <T> The type of the collection elements.
     */
    public static <T> JsonCodec<List<T>> list(JsonCodec<T> codec) {
        return create(codec, List.class, ArrayList::new);
    }

    /**
     * Creates a new set based collection codec.
     *
     * @param codec The codec to use for serializing collection elements.
     * @return A new set codec.
     * @param <T> The type of the collection elements.
     */
    public static <T> JsonCodec<Set<T>> set(JsonCodec<T> codec) {
        return create(codec, Set.class, HashSet::new);
    }

    /**
     * Creates a new collection codec that wraps the given codec for (de)serialization.
     *
     * @param codec The codec to use for serializing collection elements.
     * @param collectionClass The class of the collection implementation used.
     * @param collectionSupplier A supplier that creates a new instance of the collection when called.
     * @return A codec for the given collection implementation.
     * @param <T> The type of the collection elements.
     * @param <C> The type of the collection class.
     */
    public static <T, C extends Collection<T>> JsonCodec<C> create(JsonCodec<T> codec, Class<?> collectionClass, Supplier<C> collectionSupplier) {
        return new CollectionCodec<>(codec, TypeToken.getParameterized(collectionClass, codec.type()).getType(), collectionSupplier);
    }

    private record CollectionCodec<T, C extends Collection<T>>(JsonCodec<T> codec, Type type, Supplier<C> collectionSupplier) implements JsonCodec<C> {
        @Override
        public C deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
            if (!jsonElement.isJsonArray()) {
                throw new JsonParseException("Supplied json element is not a json element (got '" + jsonElement + "')");
            }

            final C collection = this.collectionSupplier.get();

            for (final JsonElement element : jsonElement.getAsJsonArray()) {
                collection.add(context.deserialize(element, this.codec.type()));
            }

            return collection;
        }

        @Override
        public JsonElement serialize(C list, Type type, JsonSerializationContext context) {
            final JsonArray array = new JsonArray();

            for (final T element : list) {
                array.add(context.serialize(element, this.codec.type()));
            }

            return array;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{" + this.type().getTypeName() + "}";
        }
    }
}
