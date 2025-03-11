package net.earthmc.mycelium.platform.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import net.earthmc.mycelium.api.network.Platform;
import net.earthmc.mycelium.client.MyceliumClient;
import org.jetbrains.annotations.NotNull;

@Plugin(name = "Mycelium", id = "mycelium", version = "0.0.1", authors = "Warriorrr")
public class VelocityPlatform extends Platform {
    @Inject
    private ProxyServer proxy;

    private MyceliumClient client = MyceliumClient.newBuilder().build();

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

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
