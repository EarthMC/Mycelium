package net.earthmc.mycelium.client.impl.api;

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

        final Set<String> onlineUUIDs = client().client().smembers(playerSetKey());

        for (final String uuid : onlineUUIDs) {
            final String username = client().client().hget(RedisKey.create(client().network().id(), "player", uuid), "name");

            if (username == null) {
                continue;
            }

            players.add(new PlayerImpl(username, UUID.fromString(uuid), client()));
        }

        return Set.copyOf(players);
    }

    @Override
    default int playerCount() {
        return Math.toIntExact(client().client().scard(playerSetKey()));
    }

    @Override
    default @Nullable Player getPlayerByName(String name) {
        final String uuid = client().client().get(RedisKey.create(client().network().id(), "name2uuid", name.toLowerCase(Locale.ROOT)));
        if (uuid == null) {
            return null;
        }

        if (!client().client().sismember(playerSetKey(), uuid)) {
            return null;
        }

        // Lookup exact name
        final String accurateName = client().client().hget(RedisKey.create(client().network().id(), "player", uuid), "name");
        if (accurateName == null) {
            return null;
        }

        return new PlayerImpl(accurateName, UUID.fromString(uuid), client());
    }

    @Override
    default @Nullable Player getPlayerByUUID(UUID uuid) {
        // Not online on this network/server/proxy
        if (!client().client().sismember(playerSetKey(), uuid.toString())) {
            return null;
        }

        final String name = client().client().hget(RedisKey.create(client().network().id(), "player", uuid.toString()), "name");
        if (name == null) {
            return null;
        }

        return new PlayerImpl(name, uuid, client());
    }
}
