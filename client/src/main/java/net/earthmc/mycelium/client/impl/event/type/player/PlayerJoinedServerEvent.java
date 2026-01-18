package net.earthmc.mycelium.client.impl.event.type.player;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import net.earthmc.mycelium.api.Mycelium;
import net.earthmc.mycelium.api.event.Event;
import net.earthmc.mycelium.api.network.Network;
import net.earthmc.mycelium.api.network.Player;
import net.earthmc.mycelium.api.network.Server;
import net.earthmc.mycelium.api.serialization.JsonCodec;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.UUID;

@NullMarked
public class PlayerJoinedServerEvent implements net.earthmc.mycelium.api.event.player.PlayerJoinedServerEvent {
    private final Player player;
    private final @Nullable Server previousServer;
    private final Server server;

    public PlayerJoinedServerEvent(Player player, @Nullable Server previousServer, Server server) {
        this.player = player;
        this.previousServer = previousServer;
        this.server = server;
    }

    @Override
    public @Nullable Server previousServer() {
        return this.previousServer;
    }

    @Override
    public Player player() {
        return this.player;
    }

    @Override
    public Server server() {
        return this.server;
    }

    public static JsonCodec<? extends Event> jsonCodec() {
        return Codec.INSTANCE;
    }

    private static final class Codec implements JsonCodec<PlayerJoinedServerEvent> {
        private static final JsonCodec<PlayerJoinedServerEvent> INSTANCE = new Codec();

        @Override
        public Type type() {
            return PlayerJoinedServerEvent.class;
        }

        @Override
        public PlayerJoinedServerEvent deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (!(jsonElement instanceof JsonObject object)) {
                throw new JsonParseException("Expected an object, got '" + jsonElement + "' instead.");
            }

            final Network network = Mycelium.api().network();
            final Player player = network.getPlayerByUUID(UUID.fromString(object.get("player_uuid").getAsString()));
            final Server server = network.getServerById(object.get("server_id").getAsString());
            Server previousServer = null;

            if (object.has("previous_server_id")) {
                previousServer = network.getServerById(object.get("previous_server_id").getAsString());
            }

            if (player == null || server == null) {
                throw new JsonParseException("Unknown player or server for event '" + jsonElement + "', ignoring event.");
            }

            return new PlayerJoinedServerEvent(player, previousServer, server);
        }

        @Override
        public JsonElement serialize(PlayerJoinedServerEvent event, Type type, JsonSerializationContext jsonSerializationContext) {
            final JsonObject object = new JsonObject();
            object.addProperty("player_uuid", event.player.uuid().toString());
            object.addProperty("server_id", event.server.name());
            if (event.previousServer != null) {
                object.addProperty("previous_server_id", event.previousServer.name());
            }

            return object;
        }
    }
}
