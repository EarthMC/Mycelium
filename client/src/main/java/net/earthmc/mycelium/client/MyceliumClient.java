package net.earthmc.mycelium.client;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import net.earthmc.mycelium.api.Mycelium;
import net.earthmc.mycelium.api.MyceliumProvider;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.network.Network;
import net.earthmc.mycelium.api.serialization.Codecs;
import net.earthmc.mycelium.client.impl.messaging.CallbackProvider;
import net.earthmc.mycelium.client.impl.messaging.MessagingRegistrarImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MyceliumClient implements Mycelium, Closeable {
    private final Logger logger = LoggerFactory.getLogger(MyceliumClient.class);
    private String networkId = "prod";
    private RedisClient client;

    private final MessagingRegistrarImpl messagingRegistrar;
    private final CallbackProvider callbackProvider;

    private final UUID clientId = UUID.randomUUID();

    protected MyceliumClient(String redisURI) {
        this.client = RedisClient.create(RedisURI.create(redisURI));
        this.messagingRegistrar = new MessagingRegistrarImpl(this);
        this.callbackProvider = new CallbackProvider(this);
    }

    public static void main(String[] args) {
        MyceliumClient instance = MyceliumClient.newBuilder().build();
        MyceliumProvider.register(instance);

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(10000000000L);
            } catch (InterruptedException ignored) {}
        });
        t.start();

        final MessagingRegistrar registrar = Mycelium.get().messaging();
        final ChannelIdentifier.Bound<String> identifier = registrar.bind(ChannelIdentifier.identifier("incoming"), Codecs.STRING);

        final boolean sendMessage = args.length > 0;
        if (sendMessage) {
            instance.logger.info("Sending message");

            registrar.message(identifier, "hiiiiii").callback(10L, TimeUnit.SECONDS, response -> {
                instance.logger.info("The message I sent got a response: {}", response.data());

                response.buildSyncResponse("thanks for responding!").send();
            }).send();
        } else {
            instance.logger.info("Receiving messages");

            registrar.registerIncomingChannel(identifier, message -> {
                instance.logger.info("Message received with data: {}", message.data());

                final String response = "hi, I received your message (%s), thanks for talking to me (%s)".formatted(message.data(), instance.clientId.toString());

                message.buildResponse(response).callback(10L, TimeUnit.SECONDS, responseResponse -> {
                    instance.logger.info("My response got a response: {}", responseResponse.data());
                }).send();
            });
        }
    }

    public RedisClient client() {
        return this.client;
    }

    public UUID clientId() {
        return this.clientId;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public Network network() {
        return null;
    }

    @Override
    public MessagingRegistrar messaging() {
        return this.messagingRegistrar;
    }

    public CallbackProvider callbacks() {
        return this.callbackProvider;
    }

    @Override
    public void close() {
        this.messagingRegistrar.shutdown();
    }

    public static class Builder {
        private String redisURI = "redis://localhost:6379/";

        public MyceliumClient build() {
            return new MyceliumClient(this.redisURI);
        }
    }
}
