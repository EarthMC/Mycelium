package net.earthmc.mycelium.web.api;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import net.earthmc.mycelium.api.network.Proxy;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.redis.RedisKey;
import net.earthmc.mycelium.web.MyceliumApplication;
import net.earthmc.mycelium.web.dto.response.ProxyListResponse;
import net.earthmc.mycelium.web.dto.response.ProxyDataResponse;
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
@RequestMapping("/proxy")
public class ProxyController {
    private final MyceliumClient client;

    public ProxyController(final MyceliumApplication mycelium) {
        this.client = mycelium.client();
    }

    @GetMapping("/list")
    public ProxyListResponse listProxies() {
        final List<ProxyDataResponse> proxies = new ArrayList<>();
        int totalPlayers = 0;

        for (final Proxy proxy : client.network().proxies()) {
            int playerCount = proxy.playerCount();
            totalPlayers += playerCount;

            proxies.add(new ProxyDataResponse(proxy.id(), playerCount));
        }

        return new ProxyListResponse(totalPlayers, proxies);
    }

    @PostMapping("/create/{proxy}")
    public ServerResponse createProxy(final @PathVariable String proxy) {
        if (client.redis().sadd(RedisKey.create(client, "proxies"), proxy) == 0) {
            return ServerResponse.status(HttpStatus.CONFLICT).build();
        }

        return ServerResponse.ok().build();
    }

    @DeleteMapping("/delete/{proxy}")
    public ServerResponse deleteProxy(final @PathVariable String proxy) {
        if (client.redis().srem(RedisKey.create(client, "proxies"), proxy) == 0) {
            return ServerResponse.notFound().build();
        }

        final String proxyPlayersKey = RedisKey.create(client, "proxy", proxy, "players");

        Set<String> playersOnProxy;
        try (final AbstractPipeline pipe = client.redis().pipelined()) {
            Response<Set<String>> playersResponse = pipe.smembers(proxyPlayersKey);
            pipe.unlink(proxyPlayersKey);

            pipe.sync();

            playersOnProxy = playersResponse.get();
        }

        if (!playersOnProxy.isEmpty()) {
            Hdeleq.del(client.redis(), playersOnProxy.stream().map(uuid -> RedisKey.create(client, "player", uuid)).toList(), "proxy", proxy);
        }

        return ServerResponse.ok().build();
    }

    @DeleteMapping("/delete/bulk")
    public ServerResponse bulkDelete(final @RequestBody @NotEmpty @Size(max = 100) List<String> proxies) {
        for (final String proxy : proxies) {
            deleteProxy(proxy);
        }

        return ServerResponse.ok().build();
    }
}
