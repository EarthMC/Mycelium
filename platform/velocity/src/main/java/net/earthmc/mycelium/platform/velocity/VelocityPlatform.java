package net.earthmc.mycelium.platform.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.earthmc.mycelium.api.Platform;
import net.earthmc.mycelium.api.Proxy;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.redis.collection.RedisMirroredSet;
import org.jetbrains.annotations.NotNull;

@Plugin(name = "Mycelium", id = "mycelium", version = "0.0.1", authors = "Warriorrr")
public class VelocityPlatform extends Platform {
    @Inject
    private ProxyServer proxy;

    private MyceliumClient client = MyceliumClient.newBuilder().build();

    private RedisMirroredSet<Proxy> proxies = new RedisMirroredSet<>(client.client(), key("proxies"), net.earthmc.mycelium.client.impl.Proxy.CODEC);

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        proxies.add(new net.earthmc.mycelium.client.impl.Proxy(this.id()));
    }

    @Subscribe
    public void onPlayerJoin(LoginEvent event) {
        client.client().connect().sync().sadd(key("players"), event.getPlayer().getUniqueId().toString());
    }

    @Subscribe
    public void onPlayerQuit(DisconnectEvent event) {
        client.client().connect().sync().srem(key("players"), event.getPlayer().getUniqueId().toString());
    }

    @Override
    public @NotNull String platformIdentifier() {
        return "proxies";
    }
}
