package net.earthmc.mycelium.client;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

public class MyceliumClient {
    private String networkId = "prod";
    private RedisClient client;

    protected MyceliumClient(String redisURI) {
        this.client = RedisClient.create(RedisURI.create(redisURI));
    }

    public static void main(String[] args) {
    }

    public RedisClient client() {
        return this.client;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String redisURI = "redis://localhost:6379/";

        public MyceliumClient build() {
            return new MyceliumClient(this.redisURI);
        }
    }
}
