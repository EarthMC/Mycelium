package net.earthmc.mycelium.web.dto.response;

import org.jspecify.annotations.Nullable;

public record PlayerResponse(String name, String uuid, @Nullable String proxy, @Nullable String server) {
}
