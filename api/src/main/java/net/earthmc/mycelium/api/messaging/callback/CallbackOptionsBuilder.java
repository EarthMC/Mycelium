package net.earthmc.mycelium.api.messaging.callback;

import org.jspecify.annotations.Nullable;

import java.time.temporal.TemporalAmount;

/**
 * A builder for options of callbacks used in the {@link net.earthmc.mycelium.api.messaging.OutgoingMessageBuilder}
 */
public interface CallbackOptionsBuilder {
    /**
     * Specifies a runnable for when this callback expires without being used.
     *
     * @param onExpire A runnable to run when this callback expires without being used.
     * @return {@code this}
     */
    CallbackOptionsBuilder onExpire(@Nullable Runnable onExpire);

    /**
     * Specifies the lifetime for this callback.
     *
     * @param lifetime The callback lifetime.
     * @return {@code this}
     */
    CallbackOptionsBuilder lifetime(@Nullable TemporalAmount lifetime);
}
