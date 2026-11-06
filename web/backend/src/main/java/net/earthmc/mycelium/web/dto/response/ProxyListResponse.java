package net.earthmc.mycelium.web.dto.response;

import java.util.List;

public record ProxyListResponse(int totalPlayers, List<ProxyDataResponse> proxies) {
}
