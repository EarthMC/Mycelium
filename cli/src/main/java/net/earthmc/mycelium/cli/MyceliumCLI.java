package net.earthmc.mycelium.cli;

import net.earthmc.mycelium.api.Mycelium;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.network.Proxy;
import net.earthmc.mycelium.api.network.command.ConsoleCommand;
import net.earthmc.mycelium.api.serialization.Codecs;
import net.earthmc.mycelium.api.serialization.CollectionCodecs;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.api.serialization.MapCodecs;
import net.earthmc.mycelium.client.MyceliumClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MyceliumCLI {
    public static void main(String[] args) throws InterruptedException {
        final Logger logger = LoggerFactory.getLogger(MyceliumCLI.class);

        MyceliumClient instance = MyceliumClient.standalone().autoregister().build();

        Thread t = new Thread(() -> {
            try {
                Thread.sleep(10000000000L);
            } catch (InterruptedException ignored) {
            }
        });
        t.start();

        final Mycelium api = Mycelium.get();
        final MessagingRegistrar registrar = api.messaging();
        final ChannelIdentifier.Bound<List<String>> identifier = registrar.bind(ChannelIdentifier.identifier("incoming"), CollectionCodecs.list(Codecs.STRING));

        final JsonCodec<Map<UUID, TestObject>> mapCodec = MapCodecs.map(JsonCodec.simple(UUID.class), JsonCodec.simple(TestObject.class));
        final ChannelIdentifier.Bound<Map<UUID, TestObject>> mapIdentifier = registrar.bind(ChannelIdentifier.identifier("incoming"), mapCodec);

        final Proxy proxy = api.network().getProxyById("proxy1");
        if (proxy != null) {
            proxy.runConsoleCommand(ConsoleCommand.command("/alert all <rainbow>hello from mycelium"));
        }

        final boolean sendMessage = args.length > 0;
        if (sendMessage) {
            logger.info("Sending message");

            Map<UUID, TestObject> map = new HashMap<>();
            map.put(UUID.randomUUID(), new TestObject("one", "two"));
            map.put(UUID.randomUUID(), new TestObject("three", "four"));

            registrar.message(mapIdentifier, map).callback(response -> {
                logger.info("The message I sent got a response: {}", response.data());

                response.buildResponse("thanks for responding!").send();
            }).send();
        } else {
            logger.info("Receiving messages");

            registrar.registerChannel(mapIdentifier, message -> {
                logger.info("Message received with data: {}", message.data());

                final String response = "hi, I received your message (%s)".formatted(message.data());

                message.buildResponse(List.of(response, "line two")).callback(Codecs.STRING, responseResponse -> {
                    logger.info("My response got a response: {}", responseResponse.data());
                }).send();
            });
        }
    }
}
