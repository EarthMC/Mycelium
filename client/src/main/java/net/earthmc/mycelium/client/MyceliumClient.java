package net.earthmc.mycelium.client;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import net.earthmc.mycelium.api.Mycelium;
import net.earthmc.mycelium.api.MyceliumProvider;
import net.earthmc.mycelium.api.messaging.ChannelIdentifier;
import net.earthmc.mycelium.api.messaging.MessagingRegistrar;
import net.earthmc.mycelium.api.network.Network;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.client.impl.api.TestSerializable;
import net.earthmc.mycelium.client.impl.messaging.MicaMessagingRegistrar;

public class MyceliumClient implements Mycelium {
    private String networkId = "prod";
    private RedisClient client;

    private MicaMessagingRegistrar messagingRegistrar;

    protected MyceliumClient(String redisURI) {
        this.client = RedisClient.create(RedisURI.create(redisURI));
        this.messagingRegistrar = new MicaMessagingRegistrar(this);
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

        registrar.bind(ChannelIdentifier.identifier("aa"), TestSerializable.CODEC).register(s -> {
            System.out.println("message received: " + s.test);
        });
    }

    public RedisClient client() {
        return this.client;
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

    public static class Builder {
        private String redisURI = "redis://localhost:6379/";

        public MyceliumClient build() {
            return new MyceliumClient(this.redisURI);
        }
    }
}
