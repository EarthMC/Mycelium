package net.earthmc.mycelium.api.serialization;

/**
 * A class holding simple codecs for common and otherwise primitive types.
 */
public final class Codecs {
    private Codecs() {}

    /**
     * A standard {@link String} codec.
     */
    public static final JsonCodec<String> STRING = JsonCodec.simple(String.class);

    /**
     * A standard {@link Integer} codec
     */
    public static final JsonCodec<Integer> INTEGER = JsonCodec.simple(Integer.class);
}
