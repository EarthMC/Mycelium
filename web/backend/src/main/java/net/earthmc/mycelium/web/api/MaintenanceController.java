package net.earthmc.mycelium.web.api;

import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.web.MyceliumApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

@RestController
@RequestMapping("/maintenance")
public class MaintenanceController {
    private final MyceliumClient client;

    public MaintenanceController(final MyceliumApplication mycelium) {
        this.client = mycelium.client();
    }

    @GetMapping("/reset")
    public void reset() {
        final ScanParams params = new ScanParams().count(500).match("m:*");

        String current = ScanParams.SCAN_POINTER_START;
        do {
            ScanResult<String> scanResult = client.redis().scan(current, params);

            if (!scanResult.getResult().isEmpty()) {
                client.redis().unlink(scanResult.getResult().toArray(new String[0]));
            }
            current = scanResult.getCursor();
        } while (!current.equals(ScanParams.SCAN_POINTER_START));
    }
}
