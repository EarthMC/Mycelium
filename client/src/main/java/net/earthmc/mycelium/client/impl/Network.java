package net.earthmc.mycelium.client.impl;

import io.lettuce.core.codec.RedisCodec;
import net.earthmc.mycelium.api.Player;
import net.earthmc.mycelium.api.Proxy;
import net.earthmc.mycelium.api.Server;
import net.earthmc.mycelium.client.redis.codec.RedisSerializable;
import net.earthmc.mycelium.client.redis.codec.StringKeyedCodec;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Network implements net.earthmc.mycelium.api.Network {
    @Override
    public Collection<Proxy> proxies() {
        return List.of();
    }

    @Override
    public @Nullable Proxy getProxyById(String id) {
        return null;
    }

    @Override
    public Collection<Server> backends() {
        return List.of();
    }

    @Override
    public Collection<Player> players() {
        return List.of();
    }

    @Override
    public @Nullable Player getPlayerByName(String name) {
        return null;
    }

    @Override
    public @Nullable Player getPlayerByUUID(UUID uuid) {
        return null;
    }

}
