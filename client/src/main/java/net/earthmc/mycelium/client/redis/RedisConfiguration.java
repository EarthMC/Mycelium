package net.earthmc.mycelium.client.redis;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

public record RedisConfiguration(Optional<String> host, Optional<Integer> port, Optional<String> username, Optional<String> password) {
    public static RedisConfiguration fromFile(final Path path) {
        try {
            final Properties properties = new Properties();

            try (final InputStream is = Files.newInputStream(path)) {
                properties.load(is);
            }

            return new RedisConfiguration(
                    Optional.ofNullable(properties.getProperty("host")),
                    Optional.ofNullable(properties.getProperty("port")).map(Integer::parseInt),
                    Optional.ofNullable(properties.getProperty("username")),
                    Optional.ofNullable(properties.getProperty("password"))
            );
        } catch (IOException e) {
            LoggerFactory.getLogger(RedisConfiguration.class).warn("Failed to read {}, using defaults.", path.toAbsolutePath(), e);
            return empty();
        }
    }

    public static RedisConfiguration empty() {
        return new RedisConfiguration(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }
}
