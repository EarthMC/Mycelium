package net.earthmc.mycelium.client.redis;

public class RedisKey {
    public static String create(final String first, final String... more) {
        return "m:" + first + ":" + String.join(":", more);
    }
}
