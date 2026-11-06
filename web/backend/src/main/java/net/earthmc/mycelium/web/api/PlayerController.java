package net.earthmc.mycelium.web.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import net.earthmc.mycelium.api.network.Proxy;
import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.redis.RedisKey;
import net.earthmc.mycelium.web.MyceliumApplication;
import net.earthmc.mycelium.web.dto.request.CreatePlayerRequest;
import net.earthmc.mycelium.web.dto.response.PlayerResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.ServerResponse;
import redis.clients.jedis.AbstractPipeline;
import redis.clients.jedis.Response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/player")
public class PlayerController {
    private final MyceliumClient client;

    public PlayerController(final MyceliumApplication mycelium) {
        this.client = mycelium.client();
    }

    @GetMapping("/list")
    public List<PlayerResponse> listPlayers() {
        final List<PlayerResponse> players = new ArrayList<>();

        final Collection<String> onlinePlayerUUIDs = client.redis().smembers(RedisKey.create(client, "players"));

        try (final AbstractPipeline pipe = client.redis().pipelined()) {
            Map<String, Response<List<String>>> playerData = new HashMap<>();

            for (final String uuid : onlinePlayerUUIDs) {
                final Response<List<String>> fields = pipe.hmget(RedisKey.create(client, "player", uuid), "name", "proxy", "server");

                playerData.put(uuid, fields);
            }

            pipe.sync();

            for (final Map.Entry<String, Response<List<String>>> entry : playerData.entrySet()) {
                final String uuid = entry.getKey();
                final List<String> fields = entry.getValue().get();

                if (fields.size() != 3) {
                    continue;
                }

                final String name = fields.getFirst();
                final String proxy = fields.get(1);
                final String server = fields.getLast();

                players.add(new PlayerResponse(name, uuid, proxy, server));
            }
        }

        return players;
    }

    @PutMapping("/create")
    public ServerResponse createPlayer(@RequestBody @Valid CreatePlayerRequest request) {
        final String uuid = request.uuid().toString();

        // TODO: check proxy/server validity if present

        try (final AbstractPipeline pipe = client.redis().pipelined()) {
            pipe.set(RedisKey.create(client, "name2uuid", request.name().toLowerCase(Locale.ROOT)), uuid);

            final String playerHashKey = RedisKey.create(client, "player", uuid);
            final Response<String> prevServer = pipe.hget(playerHashKey, "server");
            final Response<String> prevProxy = pipe.hget(playerHashKey, "proxy");
            final Response<String> prevName = pipe.hget(playerHashKey, "name");

            pipe.hset(playerHashKey, "name", request.name());
            if (request.proxy() == null) {
                pipe.hdel(playerHashKey, "proxy");
            } else {
                pipe.hset(playerHashKey, "proxy", request.proxy());
            }

            if (request.server() == null) {
                pipe.hdel(playerHashKey, "server");
            } else {
                pipe.hset(playerHashKey, "server", request.server());
            }

            if (request.proxy() != null) {
                pipe.sadd(RedisKey.create(client, "proxy", request.proxy(), "players"), uuid);
            }

            if (request.server() != null) {
                pipe.sadd(RedisKey.create(client, "server", request.server(), "players"), uuid);
            }

            pipe.sadd(RedisKey.create(client, "players"), uuid);

            pipe.sync();

            Optional.ofNullable(prevServer.get()).filter(oldServer -> !oldServer.equals(request.server())).ifPresent(oldServer -> pipe.srem(RedisKey.create(client, "server", oldServer, "players"), uuid));
            Optional.ofNullable(prevProxy.get()).filter(oldProxy -> !oldProxy.equals(request.proxy())).ifPresent(oldProxy -> pipe.srem(RedisKey.create(client, "proxy", oldProxy, "players"), uuid));
            Optional.ofNullable(prevName.get()).filter(oldName -> !oldName.equals(request.name())).ifPresent(oldName -> pipe.unlink(RedisKey.create(client, "name2uuid", oldName.toLowerCase(Locale.ROOT))));
        }

        return ServerResponse.ok().build();
    }

    @DeleteMapping("/delete/{uuid}")
    public ServerResponse deletePlayer(@PathVariable UUID uuid) {
        return doBulkDelete(List.of(uuid));
    }

    @DeleteMapping("/delete/bulk")
    public ServerResponse bulkDelete(@NotEmpty @RequestBody List<UUID> uuids) {
        return doBulkDelete(uuids);
    }

    private ServerResponse doBulkDelete(final List<UUID> uuids) {
        final Map<String, Response<String>> usernameResponses = new HashMap<>();

        final List<String> serverNames = client.network().servers().stream().map(Server::name).toList();
        final List<String> proxyNames = client.network().proxies().stream().map(Proxy::id).toList();

        try (final AbstractPipeline pipe = client.redis().pipelined()) {
            for (final UUID uuid : uuids) {
                final String uuidStr = uuid.toString();
                usernameResponses.put(uuidStr, pipe.hget(RedisKey.create(client, "player", uuidStr), "name"));
            }

            pipe.sync();

            for (final Map.Entry<String, Response<String>> entry : usernameResponses.entrySet()) {
                final String uuid = entry.getKey();
                final String username = entry.getValue().get();

                pipe.unlink(RedisKey.create(client, "name2uuid", username.toLowerCase(Locale.ROOT)));

                pipe.srem(RedisKey.create(client, "players"), uuid);
                pipe.unlink(RedisKey.create(client, "player", uuid));

                for (final String server : serverNames) {
                    pipe.srem(RedisKey.create(client, "server", server, "players"), uuid);
                }

                for (final String proxy : proxyNames) {
                    pipe.srem(RedisKey.create(client, "proxy", proxy, "players"), uuid);
                }
            }
        }

        return ServerResponse.ok().build();
    }
}
