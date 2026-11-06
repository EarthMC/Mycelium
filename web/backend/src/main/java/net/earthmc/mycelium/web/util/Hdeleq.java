package net.earthmc.mycelium.web.util;

import redis.clients.jedis.UnifiedJedis;

import java.util.List;

public class Hdeleq {
    public static void del(final UnifiedJedis jedis, final List<String> keys, final String field, final String value) {
        // This unfortunately seems like the best way to atomically remove values in hash fields if they match a certain value

        final String script = """
                for i, key in ipairs(KEYS) do
                    local value = redis.call('HGET', key, ARGV[1])
                    if value == ARGV[2] then
                        redis.call('HDEL', key, ARGV[1])
                    end
                end
                return 0
                """;

        jedis.eval(
            script,
            keys,
            List.of(field, value)
        );
    }
}
