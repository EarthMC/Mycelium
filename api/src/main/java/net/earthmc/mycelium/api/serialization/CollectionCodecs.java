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
 * <p>
 * Example:
 * <pre>
 * {@code
 * JsonCodec<List<String>> stringCodec = CollectionCodecs.list(Codecs.STRING));
 * }
 * </pre>
 */
public final class CollectionCodecs {
    public static <T> JsonCodec<List<T>> list(JsonCodec<T> codec) {
        return collection(codec, List.class, ArrayList::new);
    }

    public static <T> JsonCodec<Set<T>> set(JsonCodec<T> codec) {
        return collection(codec, Set.class, HashSet::new);
    }

    public static <T, C extends Collection<T>> JsonCodec<C> collection(JsonCodec<T> codec, Class<?> collectionClass, Supplier<C> collectionSupplier) {
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
