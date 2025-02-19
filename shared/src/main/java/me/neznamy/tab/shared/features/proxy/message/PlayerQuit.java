package me.neznamy.tab.shared.features.proxy.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class PlayerQuit extends ProxyMessage {

    private UUID playerId;

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        writeUUID(out, playerId);
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        playerId = readUUID(in);
    }

    @Override
    public void process(@NotNull ProxySupport proxySupport) {
        ProxyPlayer target = proxySupport.getProxyPlayers().get(playerId);
        if (target == null) return; // Print warn?
        // Do not remove connected player
        if (!TAB.getInstance().isPlayerConnected(target.getUniqueId())) {
            proxySupport.getFeatures().forEach(f -> f.onQuit(target));
        }
        proxySupport.getProxyPlayers().remove(target.getUniqueId());
    }
}
