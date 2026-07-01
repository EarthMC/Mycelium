package net.earthmc.mycelium.api.store;

import net.earthmc.mycelium.api.serialization.JsonCodec;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.time.temporal.TemporalAmount;
import java.util.function.Function;

public class SetOptions {
    private @Nullable SetKeyword keyword = null;
    private @Nullable TemporalAmount expiration = null;
    private @Nullable ValueEq<?> condition = null;

    public static SetOptions setOptions() {
        return new SetOptions();
    }

    private SetOptions() {}

    public SetOptions expiration(final @Nullable TemporalAmount expiration) {
        this.expiration = expiration;
        return this;
    }

    public SetOptions xx() {
        this.keyword = SetKeyword.XX;
        return this;
    }

    public SetOptions nx() {
        this.keyword = SetKeyword.NX;
        return this;
    }

    public <T> SetOptions valueEq(JsonCodec<T> codec, T value) {
        this.condition = new ValueEq<>(codec, value);
        return this;
    }

    @ApiStatus.Internal
    public @Nullable TemporalAmount expiration() {
        return this.expiration;
    }

    @ApiStatus.Internal
    public boolean isNX() {
        return this.keyword == SetKeyword.NX;
    }

    @ApiStatus.Internal
    public boolean isXX() {
        return this.keyword == SetKeyword.XX;
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

    private enum SetKeyword {
        NX,
        XX
    }
}
