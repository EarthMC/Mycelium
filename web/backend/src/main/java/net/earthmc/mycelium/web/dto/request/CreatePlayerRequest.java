package net.earthmc.mycelium.web.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreatePlayerRequest(@Size(min = 3) @NotNull String name, @NotNull UUID uuid, @Nullable String server, @Nullable String proxy) {
}
