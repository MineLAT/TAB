package me.neznamy.tab.shared.features.proxy.feature;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.features.PlayerList;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.message.ProxyMessage;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ProxyPlayerList extends ProxyFeature {

    private final ProxySupport proxySupport;
    @Getter private final PlayerList playerList;

    public ProxyPlayerList(@NotNull ProxySupport proxySupport, @NotNull PlayerList playerList) {
        this.proxySupport = proxySupport;
        this.playerList = playerList;
        proxySupport.registerMessage("tabformat", Update.class, Update::new);
    }

    @Override
    public void onJoin(@NotNull TabPlayer player) {
        if (player.getVersion().getMinorVersion() < 8) return;
        for (ProxyPlayer proxied : proxySupport.getProxyPlayers().values()) {
            if (TAB.getInstance().getPlatform().isProxy()) {
                player.getTabList().updateDisplayName(proxied.getUniqueId(), proxied.getTabFormat());
            } else {
                player.getTabList().addEntry(proxied.asEntry());
            }
        }
    }

    @Override
    public void onJoin(@NotNull ProxyPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.getVersion().getMinorVersion() < 8) continue;
            if (viewer.getUniqueId().equals(player.getUniqueId())) continue;
            if (TAB.getInstance().getPlatform().isProxy()) {
                viewer.getTabList().updateDisplayName(player.getUniqueId(), player.getTabFormat());
            } else {
                viewer.getTabList().addEntry(player.asEntry());
            }
        }
    }

    @Override
    public void onServerSwitch(@NotNull TabPlayer player) {
        onJoin(player);
    }

    @Override
    public void onQuit(@NotNull ProxyPlayer player) {
        if (TAB.getInstance().getPlatform().isProxy()) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.getVersion().getMinorVersion() < 8) continue;
            if (viewer.getUniqueId().equals(player.getUniqueId())) continue;
            viewer.getTabList().removeEntry(player.getUniqueId());
        }
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out, @NotNull TabPlayer player) {
        out.writeUTF(player.getProperty(TabConstants.Property.TABPREFIX).get() +
                player.getProperty(TabConstants.Property.CUSTOMTABNAME).get() +
                player.getProperty(TabConstants.Property.TABSUFFIX).get());
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in, @NotNull ProxyPlayer player) {
        player.setTabFormat(TabComponent.optimized(in.readUTF()));
    }

    @Override
    public void onVanishStatusChange(@NotNull ProxyPlayer player) {
        if (player.isVanished()) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.getVersion().getMinorVersion() < 8) continue;
            viewer.getTabList().updateDisplayName(player.getUniqueId(), player.getTabFormat());
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public class Update extends ProxyMessage {

        private UUID playerId;
        private String format;

        @Override
        public void write(@NotNull ByteArrayDataOutput out) {
            writeUUID(out, playerId);
            out.writeUTF(format);
        }

        @Override
        public void read(@NotNull ByteArrayDataInput in) {
            playerId = readUUID(in);
            format = in.readUTF();
        }

        @Override
        public void process(@NotNull ProxySupport proxySupport) {
            ProxyPlayer target = proxySupport.getProxyPlayers().get(playerId);
            if (target == null) return; // Print warn?
            target.setTabFormat(TabComponent.optimized(format));
            onJoin(target);
        }
    }
}
