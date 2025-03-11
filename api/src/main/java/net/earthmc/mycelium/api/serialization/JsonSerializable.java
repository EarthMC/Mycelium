package net.earthmc.mycelium.api.serialization;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface JsonSerializable<T> {
    JsonCodec<T> codec();
}
