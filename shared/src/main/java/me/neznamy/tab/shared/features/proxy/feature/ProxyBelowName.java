package me.neznamy.tab.shared.features.proxy.feature;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.features.BelowName;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.message.ProxyMessage;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ProxyBelowName extends ProxyFeature {

    private final ProxySupport proxySupport;
    @Getter private final BelowName belowName;

    public ProxyBelowName(@NotNull ProxySupport proxySupport, @NotNull BelowName belowName) {
        this.proxySupport = proxySupport;
        this.belowName = belowName;
        proxySupport.registerMessage("belowname", Update.class, Update::new);
    }

    @Override
    public void onJoin(@NotNull TabPlayer player) {
        for (ProxyPlayer proxied : proxySupport.getProxyPlayers().values()) {
            player.getScoreboard().setScore(
                    BelowName.OBJECTIVE_NAME,
                    proxied.getNickname(),
                    proxied.getBelowNameNumber(),
                    null, // Unused by this objective slot
                    proxied.getBelowNameFancy()
            );
        }
    }

    @Override
    public void onJoin(@NotNull ProxyPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            viewer.getScoreboard().setScore(
                    BelowName.OBJECTIVE_NAME,
                    player.getNickname(),
                    player.getBelowNameNumber(),
                    null, // Unused by this objective slot
                   player.getBelowNameFancy()
            );
        }
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out, @NotNull TabPlayer player) {
        out.writeInt(belowName.getValue(player));
        out.writeUTF(player.getProperty(belowName.getFANCY_FORMAT_PROPERTY()).get());
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in, @NotNull ProxyPlayer player) {
        player.setBelowNameNumber(in.readInt());
        player.setBelowNameFancy(TabComponent.optimized(in.readUTF()));
    }

    @Override
    public void onLoginPacket(@NotNull TabPlayer player) {
        onJoin(player);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public class Update extends ProxyMessage {

        private UUID playerId;
        private int value;
        private String fancyValue;

        @Override
        public void write(@NotNull ByteArrayDataOutput out) {
            writeUUID(out, playerId);
            out.writeInt(value);
            out.writeUTF(fancyValue);
        }

        @Override
        public void read(@NotNull ByteArrayDataInput in) {
            playerId = readUUID(in);
            value = in.readInt();
            fancyValue = in.readUTF();
        }

        @Override
        public void process(@NotNull ProxySupport proxySupport) {
            ProxyPlayer target = proxySupport.getProxyPlayers().get(playerId);
            if (target == null) return; // Print warn?
            // Below name is already being processed by connected player
            if (TAB.getInstance().isPlayerConnected(target.getUniqueId())) {
                TAB.getInstance().debug("The player " + target.getName() + " is already connected");
                return;
            }
            target.setBelowNameNumber(value);
            target.setBelowNameFancy(TabComponent.optimized(fancyValue));
            onJoin(target);
        }
    }
}
