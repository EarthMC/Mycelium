package net.earthmc.mycelium.api.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Type;

/**
 * Describes a codec that is capable of (de)serializing an object to/from JSON.
 *
 * @param <T> The object type.
 */
public interface JsonCodec<T> extends JsonSerializer<T>, JsonDeserializer<T> {
    /**
     * @return The type for {@link T}
     */
    Type type();

    /**
     * Creates a new instance of a 'simple' json codec, meant for objects that do not have complex serialization requirements.
     *
     * @param typeClass The class to make the codec for.
     * @return A new simple json codec.
     * @param <T> The class this codec is for.
     */
    static <T> JsonCodec<T> simple(Class<T> typeClass) {
        return new Simple<>(typeClass);
    }

    /**
     * Creates a new instance of a 'simple' json codec, meant for objects that do not have complex serialization requirements.
     * <p>
     * This method is caller sensitive, so it is not allowed to call this method from outside the class it is constructing the codec for.
     *
     * @return A new simple json codec.
     * @param <T> The class this codec is for.
     */
    @SuppressWarnings("unchecked")
    static <T> JsonCodec<T> simple() {
        final Class<?> callingClass = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();

        return simple((Class<T>) callingClass);
    }

    @ApiStatus.Internal
    final class Simple<T> implements JsonCodec<T> {
        private final Class<T> typeClass;

        private Simple(Class<T> typeClass) {
            this.typeClass = typeClass;
        }

        @Override
        public T deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            throw new UnsupportedOperationException("Simple");
        }

        @Override
        public JsonElement serialize(T t, Type type, JsonSerializationContext jsonSerializationContext) {
            throw new UnsupportedOperationException("Simple");
        }

        @Override
        public Type type() {
            return this.typeClass;
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{" + this.type().getTypeName() + "}";
        }
    }
}
