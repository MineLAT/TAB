package me.neznamy.tab.shared.features.proxy.feature;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.GlobalPlayerList;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class ProxyGlobalPlayerList extends ProxyFeature {

    private final ProxySupport proxySupport;
    private final GlobalPlayerList globalPlayerList;

    @Override
    public void onJoin(@NotNull TabPlayer player) {
        for (ProxyPlayer proxied : proxySupport.getProxyPlayers().values()) {
            if (!proxied.getServer().equals(player.getServer()) && shouldSee(player, proxied)) {
                player.getTabList().addEntry(proxied.asEntry());
            }
        }
    }

    @Override
    public void onJoin(@NotNull ProxyPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (shouldSee(viewer, player) && !viewer.getServer().equals(player.getServer())) {
                viewer.getTabList().addEntry(player.asEntry());
            }
        }
    }

    @Override
    public void onServerSwitch(@NotNull ProxyPlayer player) {
        TAB.getInstance().getCPUManager().runTaskLater(200, proxySupport.getFeatureName(), TabConstants.CpuUsageCategory.SERVER_SWITCH, () -> {
            for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                if (viewer.getServer().equals(player.getServer())) continue;
                if (shouldSee(viewer, player)) {
                    viewer.getTabList().addEntry(player.asEntry());
                } else {
                    viewer.getTabList().removeEntry(player.getUniqueId());
                }
            }
        });
    }

    @Override
    public void onQuit(@NotNull ProxyPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (!player.getServer().equals(viewer.getServer())) {
                viewer.getTabList().removeEntry(player.getUniqueId());
            }
        }
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out, @NotNull TabPlayer player) {
        out.writeBoolean(player.getSkin() != null);
        if (player.getSkin() != null) {
            out.writeUTF(player.getSkin().getValue());
            out.writeBoolean(player.getSkin().getSignature() != null);
            if (player.getSkin().getSignature() != null) {
                out.writeUTF(player.getSkin().getSignature());
            }
        }
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in, @NotNull ProxyPlayer player) {
        if (in.readBoolean()) {
            String value = in.readUTF();
            String signature = null;
            if (in.readBoolean()) {
                signature = in.readUTF();
            }
            player.setSkin(new TabList.Skin(value, signature));
        }
    }

    @Override
    public void onTabListClear(@NotNull TabPlayer player) {
        onJoin(player);
    }

    private boolean shouldSee(@NotNull TabPlayer viewer, @NotNull ProxyPlayer target) {
        if (target.isVanished() && !viewer.hasPermission(TabConstants.Permission.SEE_VANISHED)) return false;
        // Do not show duplicate player that will be removed in a sec
        if (TAB.getInstance().isPlayerConnected(target.getUniqueId())) return false;
        if (globalPlayerList.isSpyServer(viewer.getServer())) return true;
        return globalPlayerList.getServerGroup(viewer.getServer()).equals(globalPlayerList.getServerGroup(target.getServer()));
    }

    @Override
    public void onVanishStatusChange(@NotNull ProxyPlayer player) {
        if (player.isVanished()) {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (!shouldSee(all, player)) {
                    all.getTabList().removeEntry(player.getUniqueId());
                }
            }
        } else {
            for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                if (shouldSee(viewer, player)) {
                    viewer.getTabList().addEntry(player.asEntry());
                }
            }
        }
    }
}
