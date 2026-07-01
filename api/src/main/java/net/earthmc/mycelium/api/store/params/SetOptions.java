package net.earthmc.mycelium.api.store.params;

import net.earthmc.mycelium.api.serialization.JsonCodec;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.time.temporal.TemporalAmount;
import java.util.function.Function;

public class SetOptions extends BaseOptions {
    private @Nullable ValueEq<?> condition = null;

    public static SetOptions setOptions() {
        return new SetOptions();
    }

    private SetOptions() {}

    @Override
    public SetOptions expiration(final @Nullable TemporalAmount expiration) {
        super.expiration(expiration);
        return this;
    }

    @Override
    public SetOptions xx() {
        super.xx();
        return this;
    }

    @Override
    public SetOptions nx() {
        super.nx();
        return this;
    }

    public <T> SetOptions valueEq(JsonCodec<T> codec, T value) {
        this.condition = new ValueEq<>(codec, value);
        return this;
    }

    @ApiStatus.Internal
    public @Nullable ValueEq<?> condition() {
        return this.condition;
    }

    @ApiStatus.Internal
    public record ValueEq<T>(JsonCodec<T> codec, T value) {
        public String serialize(Function<JsonCodec<T>, Function<T, String>> functionFunction) {
            return functionFunction.apply(codec).apply(value);
        }
    }
}
