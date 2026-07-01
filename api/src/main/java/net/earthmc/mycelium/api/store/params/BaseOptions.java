package net.earthmc.mycelium.api.store.params;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.time.temporal.TemporalAmount;

public class BaseOptions {
    private @Nullable SetKeyword keyword = null;
    private @Nullable TemporalAmount expiration = null;

    protected BaseOptions() {
    }

    public BaseOptions expiration(final @Nullable TemporalAmount expiration) {
        this.expiration = expiration;
        return this;
    }

    public BaseOptions xx() {
        this.keyword = SetKeyword.XX;
        return this;
    }

    public BaseOptions nx() {
        this.keyword = SetKeyword.NX;
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

    private enum SetKeyword {
        NX,
        XX
    }
}
