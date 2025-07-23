package me.neznamy.tab.platforms.bukkit.scoreboard;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.Protocol1_15_2To1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.hook.ViaVersionHook;
import org.jetbrains.annotations.Nullable;

public class ViaScoreboard116 extends ViaScoreboard<JsonElement> {

    /**
     * Constructs new instance with given player.
     *
     * @param player          Player this scoreboard will belong to
     */
    public ViaScoreboard116(@NonNull BukkitTabPlayer player) {
        this(player, Protocol1_15_2To1_16.class, ClientboundPackets1_16.SET_DISPLAY_OBJECTIVE, ClientboundPackets1_16.SET_OBJECTIVE, ClientboundPackets1_16.SET_SCORE, ClientboundPackets1_16.SET_PLAYER_TEAM);
    }

    protected ViaScoreboard116(@NonNull BukkitTabPlayer player, @NonNull Class<? extends Protocol> protocol, @NonNull PacketType setDisplayObjective, @NonNull PacketType setObjective, @NonNull PacketType setScore, @NonNull PacketType setPlayerTeam) {
        super(player, protocol, setDisplayObjective, setObjective, setScore, setPlayerTeam);
    }

    @Override
    protected void setDisplaySlot0(int slot, @NonNull String objective) {
        PacketWrapper packet = PacketWrapper.create(setDisplayObjective, null, connection);

        packet.write(Types.BYTE, (byte) slot);
        packet.write(Types.STRING, objective);

        send(packet);
    }

    @Override
    protected void setScore0(@NonNull String objective, @NonNull String scoreHolder, int score, @Nullable JsonElement displayName, @Nullable JsonElement numberFormat) {
        sendScoreUpdate(ScoreAction.CHANGE, objective, scoreHolder, score);
    }

    @Override
    protected void removeScore0(@NonNull String objective, @NonNull String scoreHolder) {
        sendScoreUpdate(ScoreAction.REMOVE, objective, scoreHolder, null);
    }

    protected void sendScoreUpdate(int action, @NonNull String objective, @NonNull String scoreHolder, Integer score) {
        final PacketWrapper packet = PacketWrapper.create(setScore, null, connection);

        // Entity name (player username in this case)
        packet.write(Types.STRING, scoreHolder);
        // Action
        packet.write(Types.VAR_INT, action);
        // Objective name (max 16 characters)
        packet.write(Types.STRING, objective);
        if (action == ScoreAction.CHANGE) {
            // Value
            packet.write(Types.VAR_INT, score);
        }

        send(packet);
    }

    @Override
    protected void writeComponent(@NonNull PacketWrapper packet, @NonNull JsonElement component) {
        packet.write(Types.COMPONENT, component);
    }

    @Override
    protected void writeObjectiveDisplay(@NonNull PacketWrapper packet, @NonNull String title, int display, @Nullable JsonElement numberFormat) {
        // Objective value
        writeComponent(packet, stringToComponent(title));
        // Type
        packet.write(Types.VAR_INT, display == HealthDisplay.INTEGER ? 0 : 1);
    }

    @Override
    public JsonElement toComponent(@NonNull TabComponent component) {
        return ViaVersionHook.getInstance().getJson(component);
    }
}
