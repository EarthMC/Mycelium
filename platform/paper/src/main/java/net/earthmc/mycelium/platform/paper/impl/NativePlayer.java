package net.earthmc.mycelium.platform.paper.impl;

import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.api.network.command.Command;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.client.impl.api.PlayerImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class NativePlayer extends PlayerImpl {
    private final NativeServer server;
    private final org.bukkit.Server bukkitServer;

    public NativePlayer(String username, UUID uuid, MyceliumClient client, NativeServer server) {
        super(username, uuid, client);
        this.server = server;
        this.bukkitServer = server.plugin.getServer();
    }

    @Override
    public @Nullable Server server() {
        return this.server;
    }

    @Override
    public boolean isOnline() {
        return bukkitPlayer() != null;
    }

    @Override
    public void runCommand(Command command) {
        if (command.target() != Command.Target.BACKEND) {
            super.runCommand(command);
            return;
        }

        final Player player = bukkitPlayer();
        if (player != null) {
            player.getScheduler().execute(this.server.plugin, () -> bukkitServer.dispatchCommand(player, command.command()), null, 1L);
        }
    }

    @Override
    public void sendRichMessage(String message) {
        final Player player = bukkitPlayer();
        if (player != null) {
            player.sendRichMessage(message);
        }
    }

    @Nullable
    private Player bukkitPlayer() {
        return Bukkit.getServer().getPlayer(this.uuid());
    }
}
