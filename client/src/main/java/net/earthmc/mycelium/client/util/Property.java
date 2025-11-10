package net.earthmc.mycelium.client.util;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.function.Function;

/**
 * Utility class for looking up system properties and environment variables with consistent property names.
 * <p>
 * Property keys use the format of mycelium.key, while environment variables use MYCELIUM_KEY
 */
@NullMarked
public class Property {
    private static final Logger LOGGER = LoggerFactory.getLogger(Property.class);

    public static @Nullable String property(String name) {
        return property(name, (String) null);
    }

    @Contract("_, !null -> !null")
    public static @Nullable String property(String name, @Nullable String defaultValue) {
        final String property = System.getProperty(name, System.getenv(name.replaceAll("\\.", "_").toUpperCase(Locale.ROOT)));

        return property != null ? property : defaultValue;
    }

    public static <T> @Nullable T property(String name, Function<String, T> mapper) {
        return property(name, mapper, null);
    }

    @Contract("_, _, !null -> !null")
    public static <T> @Nullable T property(String name, Function<String, @Nullable T> mapper, @Nullable T defaultValue) {
        final String property = System.getProperty(name, System.getenv(name.replaceAll("\\.", "_").toUpperCase(Locale.ROOT)));

        if (property == null) {
            return defaultValue;
        }

        try {
            final T mapped = mapper.apply(property);
            return mapped != null ? mapped : defaultValue;
        } catch (Exception e) {
            LOGGER.warn("Invalid value '{}' for property '{}': {}", property, name, e.getMessage());
            return defaultValue;
        }
    }
}
