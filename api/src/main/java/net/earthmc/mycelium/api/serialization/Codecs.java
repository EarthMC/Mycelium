package net.earthmc.mycelium.api.serialization;

/**
 * A class holding simple codecs for common and primitive types.
 */
public final class Codecs {
    private Codecs() {}

    /**
     * A standard {@link String} codec.
     */
    public static final JsonCodec<String> STRING = JsonCodec.simple(String.class);

    /**
     * A standard {@link Integer} codec.
     */
    public static final JsonCodec<Integer> INTEGER = JsonCodec.simple(Integer.class);

    /**
     * A standard {@link Long} codec.
     */
    public static final JsonCodec<Long> LONG = JsonCodec.simple(Long.class);

    /**
     * A standard {@link Boolean} codec.
     */
    public static final JsonCodec<Boolean> BOOLEAN = JsonCodec.simple(Boolean.class);

    /**
     * A standard {@link Short} codec.
     */
    public static final JsonCodec<Short> SHORT = JsonCodec.simple(Short.class);

    /**
     * A standard {@link Double} codec.
     */
    public static final JsonCodec<Double> DOUBLE = JsonCodec.simple(Double.class);

    /**
     * A standard {@link Float} codec.
     */
    public static final JsonCodec<Float> FLOAT = JsonCodec.simple(Float.class);

    /**
     * A standard {@link Character} codec.
     */
    public static final JsonCodec<Character> CHARACTER = JsonCodec.simple(Character.class);
}
