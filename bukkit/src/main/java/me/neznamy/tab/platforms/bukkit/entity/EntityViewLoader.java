package me.neznamy.tab.platforms.bukkit.entity;

import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.backend.entityview.DummyEntityView;
import me.neznamy.tab.shared.backend.entityview.EntityView;
import me.neznamy.tab.shared.hook.ViaVersionHook;
import org.jetbrains.annotations.NotNull;

/**
 * Class for finding EntityView implementation based on server and client version.
 */
public class EntityViewLoader {

    /**
     * Finds best available instance for given player.
     *
     * @param   player
     *          Player to find instance for
     * @return  EntityView instance for player
     */
    @NotNull
    public static EntityView findInstance(@NotNull BukkitTabPlayer player) {
        if (PacketEntityView.isAvailable()) {
            if (ViaVersionHook.getInstance().isInstalled() && BukkitReflection.getMinorVersion() < 16 && player.getVersion().supportsRGB()) {
                return new ViaEntityView(player);
            } else {
                return new PacketEntityView(player);
            }
        } else {
            return new DummyEntityView();
        }
    }
}
