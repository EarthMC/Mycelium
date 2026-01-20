import org.jspecify.annotations.NullMarked;

/**
 * Mycelium, a communications library for Minecraft.
 */
@NullMarked
open module net.earthmc.mycelium.api {
    requires com.google.gson;
    requires static org.jspecify;
    requires static org.jetbrains.annotations;
}
