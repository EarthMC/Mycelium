package net.earthmc.mycelium.web;

import net.earthmc.mycelium.client.MyceliumClient;
import org.springframework.stereotype.Component;

@Component
public class MyceliumApplication {
    private final MyceliumClient client = MyceliumClient.standalone().autoregister().build();

    public MyceliumClient client() {
        return this.client;
    }
}
