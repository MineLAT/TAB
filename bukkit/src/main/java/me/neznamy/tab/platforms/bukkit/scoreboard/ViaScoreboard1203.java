package me.neznamy.tab.platforms.bukkit.scoreboard;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.Protocol1_20_2To1_20_3;
import com.viaversion.viaversion.protocols.v1_20_2to1_20_3.packet.ClientboundPackets1_20_3;
import com.viaversion.viaversion.util.ComponentUtil;
import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import org.jetbrains.annotations.Nullable;

public class ViaScoreboard1203 extends ViaScoreboard116 {

    private final PacketType resetScore;

    /**
     * Constructs new instance with given player.
     *
     * @param player          Player this scoreboard will belong to
     */
    public ViaScoreboard1203(@NonNull BukkitTabPlayer player) {
        this(player, Protocol1_20_2To1_20_3.class, ClientboundPackets1_20_3.SET_DISPLAY_OBJECTIVE, ClientboundPackets1_20_3.SET_OBJECTIVE, ClientboundPackets1_20_3.SET_SCORE, ClientboundPackets1_20_3.RESET_SCORE, ClientboundPackets1_20_3.SET_PLAYER_TEAM);
    }

    protected ViaScoreboard1203(@NonNull BukkitTabPlayer player, @NonNull Class<? extends Protocol> protocol, @NonNull PacketType setDisplayObjective, @NonNull PacketType setObjective, @NonNull PacketType setScore, @NonNull PacketType resetScore, @NonNull PacketType setPlayerTeam) {
        super(player, protocol, setDisplayObjective, setObjective, setScore, setPlayerTeam);
        this.resetScore = resetScore;
    }

    @Override
    protected void setScore0(@NonNull String objective, @NonNull String scoreHolder, int score, @Nullable JsonElement displayName, @Nullable JsonElement numberFormat) {
        final PacketWrapper packet = PacketWrapper.create(setScore, null, connection);

        // Entity name (player username in this case)
        packet.write(Types.STRING, scoreHolder);
        // Objective name
        packet.write(Types.STRING, objective);
        // Value
        packet.write(Types.VAR_INT, score);
        // Display name
        packet.write(Types.OPTIONAL_TAG, displayName == null ? null : ComponentUtil.jsonToTag(displayName));
        // Number format
        writeNumberFormat(packet, numberFormat);

        send(packet);
    }

    @Override
    protected void removeScore0(@NonNull String objective, @NonNull String scoreHolder) {
        final PacketWrapper packet = PacketWrapper.create(resetScore, null, connection);

        // Entity name (player username in this case)
        packet.write(Types.STRING, scoreHolder);
        // Objective name
        packet.write(Types.OPTIONAL_STRING, objective.isEmpty() ? null : objective);

        send(packet);
    }

    @Override
    protected void writeComponent(@NonNull PacketWrapper packet, @NonNull JsonElement component) {
        packet.write(Types.TAG, ComponentUtil.jsonToTag(component));
    }

    @Override
    protected void writeObjectiveDisplay(@NonNull PacketWrapper packet, @NonNull String title, int display, @Nullable JsonElement numberFormat) {
        super.writeObjectiveDisplay(packet, title, display, numberFormat);
        writeNumberFormat(packet, numberFormat);
    }

    protected void writeNumberFormat(@NonNull PacketWrapper packet, @Nullable JsonElement content) {
        // For now, only fixed format is supported by TAB
        if (content == null) {
            // Has number format
            packet.write(Types.BOOLEAN, false);
        } else {
            // Has number format
            packet.write(Types.BOOLEAN, true);
            // Fixed format
            packet.write(Types.VAR_INT, 2);
            // Content
            packet.write(Types.TAG, ComponentUtil.jsonToTag(content));
        }
    }
}
