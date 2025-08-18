package net.earthmc.mycelium.platform.paper;

import net.earthmc.mycelium.api.Mycelium;
import net.earthmc.mycelium.client.MyceliumClient;
import net.earthmc.mycelium.platform.paper.impl.NativeServer;
import org.bukkit.plugin.java.JavaPlugin;

public class PaperLoader extends JavaPlugin {
    private final PaperPlatform platform = new PaperPlatform(this);
    private final MyceliumClient client = MyceliumClient.forPlatform(this.platform).autoregister().nativeServer(() -> new NativeServer(platform.id(), (MyceliumClient) Mycelium.get(), this)).build();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this.platform, this);
        this.platform.enable();
    }

    @Override
    public void onDisable() {
        this.platform.disable();
    }

    public MyceliumClient client() {
        return this.client;
    }
}
