package net.earthmc.mycelium.client.impl.api;

import net.earthmc.mycelium.api.serialization.JsonCodec;
import net.earthmc.mycelium.api.serialization.JsonSerializable;

public class TestSerializable implements JsonSerializable<TestSerializable> {
    public static final JsonCodec<TestSerializable> CODEC = JsonCodec.simple();

    public String test;

    @Override
    public JsonCodec<TestSerializable> codec() {
        return CODEC;
    }
}
