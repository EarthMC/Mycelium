package net.earthmc.mycelium.api.messaging.callback;

import org.jspecify.annotations.Nullable;

import java.time.temporal.TemporalAmount;

/**
 * A builder for options of callbacks used in the {@link net.earthmc.mycelium.api.messaging.OutgoingMessageBuilder}
 */
public interface CallbackOptionsBuilder {
    /**
     *
     * @param onExpire
     * @return {@code this}
     */
    CallbackOptionsBuilder onExpire(@Nullable Runnable onExpire);

    /**
     * Specifies the lifetime for this callback.
     * @param lifetime
     * @return {@code this}
     */
    CallbackOptionsBuilder lifetime(@Nullable TemporalAmount lifetime);
}
