package net.earthmc.mycelium.api.store.params;

import org.jspecify.annotations.Nullable;

import java.time.temporal.TemporalAmount;

public class HSetOptions extends BaseOptions {
    private HSetOptions() {}

    public HSetOptions hSetOptions() {
        return new HSetOptions();
    }

    @Override
    public HSetOptions expiration(final @Nullable TemporalAmount expiration) {
        super.expiration(expiration);
        return this;
    }

    @Override
    public HSetOptions xx() {
        super.xx();
        return this;
    }

    @Override
    public HSetOptions nx() {
        super.nx();
        return this;
    }
}
