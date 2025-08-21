package net.earthmc.mycelium.client.impl.messaging.callback;

import net.earthmc.mycelium.api.messaging.callback.CallbackOptionsBuilder;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.TemporalAmount;

public record CallbackOptions(TemporalAmount lifetime, Runnable onExpire) {
    private static final Duration DEFAULT_CALLBACK_LIFETIME = Duration.ofMinutes(15);

    public static class Builder implements CallbackOptionsBuilder {
        private TemporalAmount lifetime = DEFAULT_CALLBACK_LIFETIME;
        private Runnable onExpire = null;

        @Override
        public CallbackOptionsBuilder onExpire(@Nullable Runnable onExpire) {
            this.onExpire = onExpire;
            return this;
        }

        @Override
        public CallbackOptionsBuilder lifetime(@Nullable TemporalAmount lifetime) {
            if (lifetime == null) {
                this.lifetime = DEFAULT_CALLBACK_LIFETIME;
            } else {
                this.lifetime = lifetime;
            }

            return this;
        }

        public CallbackOptions build() {
            return new CallbackOptions(this.lifetime, this.onExpire);
        }
    }
}
