package net.earthmc.mycelium.api.store;

import org.jspecify.annotations.Nullable;

import java.time.temporal.TemporalAmount;

public class SetOptions {
    private @Nullable SetKeyword keyword = null;
    private @Nullable TemporalAmount expiration = null;

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

    public @Nullable TemporalAmount expiration() {
        return this.expiration;
    }

    public boolean isNX() {
        return this.keyword == SetKeyword.NX;
    }

    public boolean isXX() {
        return this.keyword == SetKeyword.XX;
    }

    private enum SetKeyword {
        NX,
        XX
    }
}
