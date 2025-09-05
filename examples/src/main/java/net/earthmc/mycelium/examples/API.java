package net.earthmc.mycelium.examples;

import net.earthmc.mycelium.api.Mycelium;
import net.earthmc.mycelium.api.network.Player;
import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.api.network.command.Command;
import net.earthmc.mycelium.client.MyceliumClient;

public class API {
    public static void main(String[] args) {
        // Create & register an instance of the client.
        MyceliumClient.standalone().autoregister().build();

        // obtain an instance of the API, could be used from anywhere
        final Mycelium api = Mycelium.api();

        // get a specific server by its id
        final Server server = api.network().getServerById("server1");

        // look up a specific player on this server, returns null if the player isn't online on it
        final Player playerOnServer = server.getPlayerByName("Steve");

        // look up a player on the whole network, returns the player if they're online anywhere
        final Player playerOnNetwork = api.network().getPlayerByName("Steve2");

        playerOnNetwork.sendRichMessage("<rainbow>Hello from Mycelium!");

        // Run commands on the backend & proxy
        playerOnNetwork.runCommand(Command.backend("/spawn"));
        playerOnNetwork.runCommand(Command.proxy("/glist all"));

        // Send the player to another server on the same proxy
        playerOnNetwork.transferToServer(api.network().getServerById("server2"));
    }
}
