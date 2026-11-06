package net.earthmc.mycelium.web.api;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.redis.RedisKey;
import net.earthmc.mycelium.web.MyceliumApplication;
import net.earthmc.mycelium.web.dto.response.ServerDataResponse;
import net.earthmc.mycelium.web.util.Hdeleq;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.ServerResponse;
import redis.clients.jedis.AbstractPipeline;
import redis.clients.jedis.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/server")
public class ServerController {
    private final MyceliumClient client;

    public ServerController(final MyceliumApplication mycelium) {
        this.client = mycelium.client();
    }

    @GetMapping("/list")
    public List<ServerDataResponse> listServers() {
        final List<ServerDataResponse> servers = new ArrayList<>();

        for (final Server server : client.network().servers()) {
            servers.add(new ServerDataResponse(server.name(), server.playerCount()));
        }

        return servers;
    }

    @PostMapping("/create/{server}")
    public ServerResponse createServer(final @PathVariable String server) {
        if (client.redis().sadd(RedisKey.create(client, "servers"), server) == 0) {
            return ServerResponse.status(HttpStatus.CONFLICT).build();
        }

        return ServerResponse.ok().build();
    }

    @DeleteMapping("/delete/{server}")
    public ServerResponse deleteServer(final @PathVariable String server) {
        if (client.redis().srem(RedisKey.create(client, "servers"), server) == 0) {
            return ServerResponse.notFound().build();
        }

        final String serverPlayersKey = RedisKey.create(client, "server", server, "players");

        Set<String> playersOnServer;
        try (final AbstractPipeline pipe = client.redis().pipelined()) {
            Response<Set<String>> playersResponse = pipe.smembers(serverPlayersKey);
            pipe.unlink(serverPlayersKey);

            pipe.sync();

            playersOnServer = playersResponse.get();
        }

        if (!playersOnServer.isEmpty()) {
            Hdeleq.del(client.redis(), playersOnServer.stream().map(uuid -> RedisKey.create(client, "player", uuid)).toList(), "server", server);
        }

        return ServerResponse.ok().build();
    }

    @DeleteMapping("/delete/bulk")
    public ServerResponse bulkDelete(final @RequestBody @NotEmpty @Size(max = 100) List<String> servers) {
        for (final String server : servers) {
            deleteServer(server);
        }

        return ServerResponse.ok().build();
    }
}
