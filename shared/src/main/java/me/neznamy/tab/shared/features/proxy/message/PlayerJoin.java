package me.neznamy.tab.shared.features.proxy.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@NoArgsConstructor
public class PlayerJoin extends ProxyMessage {

    private ProxySupport proxySupport;
    @Getter private ProxyPlayer decodedPlayer;
    private TabPlayer encodedPlayer;

    public PlayerJoin(@NotNull ProxySupport proxySupport, @NotNull TabPlayer encodedPlayer) {
        this.proxySupport = proxySupport;
        this.encodedPlayer = encodedPlayer;
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        writeUUID(out, encodedPlayer.getTablistId());
        out.writeUTF(encodedPlayer.getName());
        out.writeUTF(encodedPlayer.getServer());
        out.writeBoolean(encodedPlayer.isVanished());
        out.writeBoolean(encodedPlayer.hasPermission(TabConstants.Permission.STAFF));
        proxySupport.getFeatures().forEach(f -> f.write(out, encodedPlayer));
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        proxySupport = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PROXY_SUPPORT); // this is not ideal
        UUID uniqueId = readUUID(in);
        String name = in.readUTF();
        String server = in.readUTF();
        boolean vanished = in.readBoolean();
        boolean staff = in.readBoolean();
        decodedPlayer = new ProxyPlayer(uniqueId, name, name, server, vanished, staff);
        proxySupport.getFeatures().forEach(f -> f.read(in, decodedPlayer));
    }

    @Override
    public void process(@NotNull ProxySupport proxySupport) {
        // Do not create duplicated player
        if (TAB.getInstance().isPlayerConnected(decodedPlayer.getUniqueId())) {
            TAB.getInstance().debug("The player " + decodedPlayer.getName() + " is already connected");
            return;
        }
        proxySupport.getProxyPlayers().put(decodedPlayer.getUniqueId(), decodedPlayer);
        proxySupport.getFeatures().forEach(f -> f.onJoin(decodedPlayer));
    }
}
