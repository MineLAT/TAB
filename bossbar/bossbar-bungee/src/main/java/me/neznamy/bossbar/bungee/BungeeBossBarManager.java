package me.neznamy.bossbar.bungee;

import me.neznamy.bossbar.shared.SafeBossBarManager;
import me.neznamy.component.bungee.BungeeComponentConverter;
import me.neznamy.component.shared.StructuredComponentConverter;
import me.neznamy.component.shared.component.TabComponent;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.BossBar;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * BossBar handler for BungeeCord. It uses packets, since
 * BungeeCord does not have a BossBar API. Only supports
 * 1.9+ players, as dealing with entities would be simply impossible.
 */
public class BungeeBossBarManager extends SafeBossBarManager<UUID> {

    /**
     * Constructs new instance for given player.
     *
     * @param   player
     *          Player this Boss bar will belong to
     */
    public BungeeBossBarManager(@NotNull ProxiedPlayer player) {
        super(player);
    }

    @Override
    @NotNull
    public UUID constructBossBar(@NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        return UUID.randomUUID();
    }

    @Override
    public void create(@NotNull BossBarInfo bar) {
        sendPacket(bar, 0);
    }

    @Override
    public void updateTitle(@NotNull BossBarInfo bar) {
        sendPacket(bar, 3);
    }

    @Override
    public void updateProgress(@NotNull BossBarInfo bar) {
        sendPacket(bar, 2);
    }

    @Override
    public void updateStyle(@NotNull BossBarInfo bar) {
        sendPacket(bar, 4);
    }

    @Override
    public void updateColor(@NotNull BossBarInfo bar) {
        sendPacket(bar, 4);
    }

    @Override
    public void remove(@NotNull BossBarInfo bar) {
        sendPacket(bar, 1);
    }

    private void sendPacket(@NotNull BossBarInfo bar, int action) {
        BossBar packet = new BossBar(bar.getBossBar(), action);
        packet.setHealth(bar.getProgress());
        packet.setTitle(convert(bar.getTitle()));
        packet.setColor(bar.getColor().ordinal());
        packet.setDivision(bar.getStyle().ordinal());
        ((UserConnection)player).sendPacketQueued(packet);
    }

    @NotNull
    private BaseComponent convert(@NotNull TabComponent component) {
        if (((ProxiedPlayer)player).getPendingConnection().getVersion() >= 735) { // 1.16
            return component.convert();
        }
        return ((BungeeComponentConverter) StructuredComponentConverter.getInstance()).legacyComponent(component);
    }
}
