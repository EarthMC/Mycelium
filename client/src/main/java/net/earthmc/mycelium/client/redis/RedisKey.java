package net.earthmc.mycelium.client.redis;

import net.earthmc.mycelium.client.MyceliumClient;

/**
 * Utility class for constructing unique keys for Redis
 */
public class RedisKey {
    public static String create(final MyceliumClient client, String... more) {
        return create(client.network().id(), more);
    }

    public static String create(final String first, final String... more) {
        return "m:" + first + ":" + String.join(":", more);
    }
}
