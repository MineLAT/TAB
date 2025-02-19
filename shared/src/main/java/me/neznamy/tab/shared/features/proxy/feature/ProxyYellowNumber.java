package me.neznamy.tab.shared.features.proxy.feature;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.features.YellowNumber;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.features.proxy.message.ProxyMessage;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ProxyYellowNumber extends ProxyFeature {

    private final ProxySupport proxySupport;
    @Getter private final YellowNumber yellowNumber;

    public ProxyYellowNumber(@NotNull ProxySupport proxySupport, @NotNull YellowNumber yellowNumber) {
        this.proxySupport = proxySupport;
        this.yellowNumber = yellowNumber;
        proxySupport.registerMessage("yellow-number", Update.class, Update::new);
    }

    @Override
    public void onJoin(@NotNull TabPlayer player) {
        for (ProxyPlayer proxied : proxySupport.getProxyPlayers().values()) {
            player.getScoreboard().setScore(
                    YellowNumber.OBJECTIVE_NAME,
                    proxied.getNickname(),
                    proxied.getPlayerlistNumber(),
                    null, // Unused by this objective slot
                    proxied.getPlayerlistFancy()
            );
        }
    }

    @Override
    public void onJoin(@NotNull ProxyPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            viewer.getScoreboard().setScore(
                    YellowNumber.OBJECTIVE_NAME,
                    player.getNickname(),
                    player.getPlayerlistNumber(),
                    null, // Unused by this objective slot
                    player.getPlayerlistFancy()
            );
        }
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out, @NotNull TabPlayer player) {
        out.writeInt(yellowNumber.getValueNumber(player));
        out.writeUTF(player.getProperty(yellowNumber.getPROPERTY_VALUE_FANCY()).get());
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in, @NotNull ProxyPlayer player) {
        player.setPlayerlistNumber(in.readInt());
        player.setPlayerlistFancy(TabComponent.optimized(in.readUTF()));
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
            // Yellow number is already being processed by connected player
            if (TAB.getInstance().isPlayerConnected(target.getUniqueId())) {
                TAB.getInstance().debug("The player " + target.getName() + " is already connected");
                return;
            }
            target.setPlayerlistNumber(value);
            target.setPlayerlistFancy(TabComponent.optimized(fancyValue));
            onJoin(target);
        }
    }
}
