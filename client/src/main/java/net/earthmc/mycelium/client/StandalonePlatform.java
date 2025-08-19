package net.earthmc.mycelium.client;

public class StandalonePlatform extends Platform {
    @Override
    public String platformIdentifier() {
        return "standalone";
    }

    @Override
    public Type type() {
        return Type.STANDALONE;
    }
}
