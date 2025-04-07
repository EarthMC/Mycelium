package net.earthmc.mycelium.client.impl.api;

import io.lettuce.core.api.StatefulRedisConnection;
import net.earthmc.mycelium.api.network.Player;
import net.earthmc.mycelium.api.network.PlayerList;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.redis.RedisKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public interface PlayerListImpl extends PlayerList {
    String playerSetKey();

    MyceliumClient client();

    @Override
    default @NonNull Collection<Player> players() {
        final Set<Player> players = new HashSet<>();

        try (final StatefulRedisConnection<String, String> connection = client().client().connect()) {
            final Set<String> onlineUUIDs = connection.sync().smembers(playerSetKey());

            for (final String uuid : onlineUUIDs) {
                final String username = connection.sync().hget(RedisKey.create(client().network().id(), "player", uuid), "name");

                if (username == null) {
                    continue;
                }

                players.add(new PlayerImpl(username, UUID.fromString(uuid), client()));
            }
        }

        return Set.copyOf(players);
    }

    @Override
    default int playerCount() {
        try (final StatefulRedisConnection<String, String> connection = client().client().connect()) {
            return Math.toIntExact(connection.sync().scard(playerSetKey()));
        }
    }

    @Override
    default @Nullable Player getPlayerByName(String name) {
        try (final StatefulRedisConnection<String, String> connection = client().client().connect()) {
            final String uuid = connection.sync().get(RedisKey.create(client().network().id(), "name2uuid", name.toLowerCase(Locale.ROOT)));
            if (uuid == null) {
                return null;
            }

            if (!connection.sync().sismember(playerSetKey(), uuid)) {
                return null;
            }

            // Lookup exact name
            final String accurateName = connection.sync().hget(RedisKey.create(client().network().id(), "player", uuid), "name");
            if (accurateName == null) {
                return null;
            }

            return new PlayerImpl(accurateName, UUID.fromString(uuid), client());
        }
    }

    @Override
    default @Nullable Player getPlayerByUUID(UUID uuid) {
        try (final StatefulRedisConnection<String, String> connection = client().client().connect()) {
            // Not online on this network/server/proxy
            if (!connection.sync().sismember(playerSetKey(), uuid.toString())) {
                return null;
            }

            final String name = connection.sync().hget(RedisKey.create(client().network().id(), "player", uuid.toString()), "name");
            if (name == null) {
                return null;
            }

            return new PlayerImpl(name, uuid, client());
        }
    }
}
