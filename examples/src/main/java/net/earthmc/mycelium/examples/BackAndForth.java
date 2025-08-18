package net.earthmc.mycelium.examples;

import net.earthmc.mycelium.api.Mycelium;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
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

public class BackAndForth {
    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger(BackAndForth.class);

        // Spawn a thread that does nothing to prevent the process from exiting
        new Thread(() -> {
            try {
                Thread.sleep(10000000000L);
            } catch (InterruptedException ignored) {}
        }).start();

        // Create a new client instance using the builder, autoregister allows it to be retrieved using the static Mycelium#get method.
        MyceliumClient instance = MyceliumClient.standalone().autoregister().build();

        final Mycelium api = Mycelium.get();

        // A simple record that we're going to be making a codec for
        record TestObject(String name, String nameTwo) {}

        // Construct a codec for a map that uses our test object as values
        // Since Gson can automatically (de)serialize uuids and simple records, we don't need to write any serialization logic ourselves.
        final JsonCodec<Map<UUID, TestObject>> mapCodec = MapCodecs.map(JsonCodec.simple(UUID.class), JsonCodec.simple(TestObject.class));

        final MessagingRegistrar registrar = api.messaging();

        // Attach a channel identifier to our codec so we can start sending & receiving data
        final ChannelIdentifier.Bound<Map<UUID, TestObject>> mapIdentifier = registrar.bind(ChannelIdentifier.identifier("incoming"), mapCodec);

        final boolean sendMessage = args.length > 0;
        if (sendMessage) {
            logger.info("Sending message");

            Map<UUID, TestObject> map = new HashMap<>();
            map.put(UUID.randomUUID(), new TestObject("one", "two"));
            map.put(UUID.randomUUID(), new TestObject("three", "four"));

            registrar.message(mapIdentifier, map).callback(CollectionCodecs.list(Codecs.STRING), response -> {
                final List<String> responseData = response.data();
                logger.info("The message I sent got a response: {}", responseData);

                response.buildResponse("thanks for responding!").send();
            }).send();
        } else {
            logger.info("Receiving messages");

            registrar.registerChannel(mapIdentifier, message -> {
                final Map<UUID, TestObject> received = message.data();
                assert received.size() == 2;

                logger.info("Message received with data: {}", received);

                final String response = "hi, I received your message (%s)".formatted(received);

                // Send a List<String> back as a response
                // The type of data being sent does have to be known between both sides, using the wrong codec or none at all may result in it not being deserializable.
                message.buildResponse(CollectionCodecs.list(Codecs.STRING), List.of(response, "line two")).callback(Codecs.STRING, responseResponse -> {
                    logger.info("My response got a response: {}", responseResponse.data());
                }).send();
            });
        }
    }
}
