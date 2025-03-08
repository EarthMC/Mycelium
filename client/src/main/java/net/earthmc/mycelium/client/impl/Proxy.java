package net.earthmc.mycelium.client.impl;

import io.lettuce.core.codec.RedisCodec;
import net.earthmc.mycelium.api.Player;
import net.earthmc.mycelium.client.redis.codec.KryoCodec;
import net.earthmc.mycelium.client.redis.codec.RedisSerializable;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Proxy implements net.earthmc.mycelium.api.Proxy, RedisSerializable<Proxy> {
    public static final RedisCodec<String, Proxy> CODEC = new KryoCodec<>(Proxy.class);

    private String id;

    private final transient Map<UUID, Player> playersByUUID = new HashMap<>();
    private final transient Map<String, Player> playersByName = new HashMap<>();
    private transient final Collection<Player> players = new HashSet<>();

    public Proxy() {

    }

    public Proxy(String id) {
        this.id = id;
    }

    @Override
    public Collection<Player> players() {
        return Set.copyOf(players);
    }

    @Override
    public @Nullable Player getPlayerByName(String name) {
        return this.playersByName.get(name);
    }

    @Override
    public @Nullable Player getPlayerByUUID(UUID uuid) {
        return this.playersByUUID.get(uuid);
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public RedisCodec<String, Proxy> codec() {
        return CODEC;
    }
}
