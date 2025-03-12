package net.earthmc.mycelium.cli;

import net.earthmc.mycelium.api.Mycelium;
import net.earthmc.mycelium.api.MyceliumProvider;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.network.Player;
import net.earthmc.mycelium.api.proto.Command;
import net.earthmc.mycelium.api.serialization.Codecs;
import net.earthmc.mycelium.client.MyceliumClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class MyceliumCLI {
    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger(MyceliumCLI.class);

        MyceliumClient instance = MyceliumClient.newBuilder().build();
        MyceliumProvider.register(instance);

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(10000000000L);
            } catch (InterruptedException ignored) {
            }
        });
        t.start();

        final Mycelium api = Mycelium.get();
        final MessagingRegistrar registrar = api.messaging();
        final ChannelIdentifier.Bound<String> identifier = registrar.bind(ChannelIdentifier.identifier("incoming"), Codecs.STRING);

        final boolean sendMessage = args.length > 0;
        if (sendMessage) {
            logger.info("Sending message");

            registrar.message(identifier, "hiiiiii").callback(10L, TimeUnit.SECONDS, response -> {
                logger.info("The message I sent got a response: {}", response.data());

                response.buildResponse("thanks for responding!").send();
            }).send();
        } else {
            logger.info("Receiving messages");

            registrar.registerIncomingChannel(identifier, message -> {
                logger.info("Message received with data: {}", message.data());

                final String response = "hi, I received your message (%s)".formatted(message.data());

                message.buildResponse(response).callback(10L, TimeUnit.SECONDS, responseResponse -> {
                    logger.info("My response got a response: {}", responseResponse.data());
                }).send();
            });
        }
    }
}
