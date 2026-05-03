package net.earthmc.mycelium.client.impl.api;

import net.earthmc.mycelium.api.network.Player;
import net.earthmc.mycelium.api.network.PlayerList;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.redis.RedisKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import redis.clients.jedis.AbstractPipeline;
import redis.clients.jedis.Response;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface PlayerListImpl extends PlayerList {
    String playerSetKey();

    MyceliumClient client();

    // Only override on Network to allow passing a native player implementation, the individual platforms can override the other methods.
    default Player createPlayer(String username, UUID uuid) {
        return new PlayerImpl(username, uuid, client());
    }

    @Override
    default @NonNull Collection<Player> players() {
        final Set<Player> players = new HashSet<>();
        final Map<UUID, Response<String>> responses = new HashMap<>();

        final Set<String> onlineUUIDs = client().redis().smembers(playerSetKey());

        try (final AbstractPipeline pipeline = client().redis().pipelined()) {
            for (final String uuid : onlineUUIDs) {
                responses.put(UUID.fromString(uuid), pipeline.hget(RedisKey.create(client(), "player", uuid), "name"));
            }
        }

        for (final Map.Entry<UUID, Response<String>> playerEntry : responses.entrySet()) {
            final String username = playerEntry.getValue().get();

            if (username == null) {
                continue;
            }

            players.add(createPlayer(username, playerEntry.getKey()));
        }

        return Set.copyOf(players);
    }

    @Override
    default int playerCount() {
        return Math.toIntExact(client().redis().scard(playerSetKey()));
    }

    @Override
    default @Nullable Player getPlayerByName(String name) {
        final String uuid = client().redis().get(RedisKey.create(client().network().id(), "name2uuid", name.toLowerCase(Locale.ROOT)));
        if (uuid == null) {
            return null;
        }

        if (!client().redis().sismember(playerSetKey(), uuid)) {
            return null;
        }

        // Lookup exact name
        final String accurateName = client().redis().hget(RedisKey.create(client().network().id(), "player", uuid), "name");
        if (accurateName == null) {
            return null;
        }

        return createPlayer(accurateName, UUID.fromString(uuid));
    }

    @Override
    default @Nullable Player getPlayerByUUID(UUID uuid) {
        // Not online on this network/server/proxy
        if (!client().redis().sismember(playerSetKey(), uuid.toString())) {
            return null;
        }

        final String name = client().redis().hget(RedisKey.create(client().network().id(), "player", uuid.toString()), "name");
        if (name == null) {
            return null;
        }

        return createPlayer(name, uuid);
    }
}
