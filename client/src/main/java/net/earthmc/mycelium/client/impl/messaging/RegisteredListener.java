package net.earthmc.mycelium.client.impl.messaging;

import com.google.gson.JsonParseException;
import net.earthmc.mycelium.api.messaging.IncomingMessage;
import net.earthmc.mycelium.api.messaging.Listener;
import net.earthmc.mycelium.client.MyceliumClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public record RegisteredListener<T>(MessagingRegistrarImpl registrar, BoundChannelIdentifier<T> identifier, Consumer<IncomingMessage<T>> consumer) implements Listener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredListener.class);

    public void processIncoming(MyceliumClient client, InternalMessage incoming) {
        try {
            final T deserialized = identifier.gson().fromJson(incoming.payload, identifier.codec().type());

            this.consumer.accept(new IncomingMessageImpl<>(client, incoming, this.identifier().codec(), deserialized));
        } catch (JsonParseException e) {
            LOGGER.info("Failed to deserialize message payload {} for a registered listener on channel {} (using codec: {})", incoming.payload, this.identifier.channel(), this.identifier.codec());
        }
    }

    @Override
    public boolean unregister() {
        return registrar.unregisterListener(this);
    }
}
