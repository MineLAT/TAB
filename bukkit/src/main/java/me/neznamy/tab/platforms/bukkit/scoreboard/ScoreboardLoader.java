package me.neznamy.tab.platforms.bukkit.scoreboard;

import lombok.Getter;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.scoreboard.packet.PacketScoreboard;
import me.neznamy.tab.platforms.bukkit.tablist.ViaTabList1193;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.hook.ViaVersionHook;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Loader finding the best available Scoreboard implementation.
 */
public class ScoreboardLoader {

    /** Instance function */
    @Getter
    @NotNull
    private static Function<BukkitTabPlayer, Scoreboard<BukkitTabPlayer, ?>> instance = NullScoreboard::new;

    /**
     * Tries to load Scoreboard packets.
     */
    public static void tryLoad() {
        if (PacketScoreboard.isAvailable()) {
            if (BukkitReflection.getMinorVersion() >= 16 || !ViaVersionHook.getInstance().isInstalled()) {
                instance = PacketScoreboard::new;
            } else {
                instance = player -> {
                    if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId()) {
                        return new ViaScoreboard1203(player);
                    } else if (player.getVersion().supportsRGB()) {
                        return new ViaScoreboard116(player);
                    } else {
                        return new PacketScoreboard(player);
                    }
                };
            }
        } else {
            BukkitUtils.compatibilityError(PacketScoreboard.getException(), "Scoreboards", null,
                    "Scoreboard feature will not work",
                    "Belowname feature will not work",
                    "Player objective feature will not work",
                    "Scoreboard teams feature will not work (nametags & sorting)");
        }
    }
}
