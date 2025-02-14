package me.neznamy.tab.platforms.bukkit.scoreboard;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.Protocol1_15_2To1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.scoreboard.packet.PacketScoreboard;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.hook.ViaVersionHook;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class ViaScoreboard extends Scoreboard<BukkitTabPlayer, JsonElement> {

    private final UserConnection connection;

    /**
     * Constructs new instance with given player.
     *
     * @param   player
     *          Player this scoreboard will belong to
     */
    public ViaScoreboard(BukkitTabPlayer player) {
        super(player);
        this.connection = Via.getManager().getConnectionManager().getConnectedClient(player.getUniqueId());
    }

    @Override
    public void onPacketSend(@NonNull Object packet) {
        PacketScoreboard.onPacketSend(this, player, packet);
    }

    @Override
    protected void setDisplaySlot0(int slot, @NonNull String objective) {
        final PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_16.SET_DISPLAY_OBJECTIVE, null, connection);

        packet.write(Types.BYTE, (byte) slot);
        packet.write(Types.STRING, objective); // 16

        packet.scheduleSend(Protocol1_15_2To1_16.class);
    }

    private void setScore(@NonNull String entity, int action, @NonNull String objective, int score) {
        final PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_16.SET_SCORE, null, connection);

        packet.write(Types.STRING, entity);
        packet.write(Types.VAR_INT, action);
        packet.write(Types.STRING, objective); // 16
        packet.write(Types.VAR_INT, score);

        packet.scheduleSend(Protocol1_15_2To1_16.class);
    }

    @Override
    protected void setScore0(@NonNull String objective, @NonNull String scoreHolder, int score, @Nullable JsonElement displayName, @Nullable JsonElement numberFormat) {
        setScore(scoreHolder, ScoreAction.CHANGE, objective, score);
    }

    @Override
    protected void removeScore0(@NonNull String objective, @NonNull String scoreHolder) {
        setScore(scoreHolder, ScoreAction.REMOVE, objective, 0);
    }

    private void setObjective(@NonNull String objective, int mode, String title, Integer display) {
        final PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_16.SET_OBJECTIVE, null, connection);

        packet.write(Types.STRING, objective);
        packet.write(Types.BYTE, (byte) mode);
        if (mode == ObjectiveAction.REGISTER || mode == ObjectiveAction.UPDATE) {
            packet.write(Types.COMPONENT, toComponent(title));
            packet.write(Types.VAR_INT, display);
        }

        packet.scheduleSend(Protocol1_15_2To1_16.class);
    }

    @Override
    protected void registerObjective0(@NonNull String objectiveName, @NonNull String title, int display, @Nullable JsonElement numberFormat) {
        setObjective(objectiveName, ObjectiveAction.REGISTER, title, display);
    }

    @Override
    protected void unregisterObjective0(@NonNull String objectiveName) {
        setObjective(objectiveName, ObjectiveAction.UNREGISTER, null, null);
    }

    @Override
    protected void updateObjective0(@NonNull String objectiveName, @NonNull String title, int display, @Nullable JsonElement numberFormat) {
        setObjective(objectiveName, ObjectiveAction.UPDATE, title, display);
    }

    @Override
    protected void registerTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix, @NonNull NameVisibility visibility, @NonNull CollisionRule collision, @NonNull Collection<String> players, int options, @NonNull EnumChatFormat color) {
        final PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_16.SET_PLAYER_TEAM, null, connection);

        packet.write(Types.STRING, name);
        packet.write(Types.BYTE, (byte) TeamAction.CREATE);
        packet.write(Types.COMPONENT, toComponent(name));
        packet.write(Types.BYTE, (byte) options);
        packet.write(Types.STRING, visibility.toString());
        packet.write(Types.STRING, collision.toString());
        packet.write(Types.VAR_INT, color.ordinal());
        packet.write(Types.COMPONENT, toComponent(prefix));
        packet.write(Types.COMPONENT, toComponent(suffix));
        packet.write(Types.VAR_INT, players.size());
        for (String entity : players) {
            packet.write(Types.STRING, entity);
        }

        packet.scheduleSend(Protocol1_15_2To1_16.class);
    }

    @Override
    protected void unregisterTeam0(@NonNull String name) {
        final PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_16.SET_PLAYER_TEAM, null, connection);

        packet.write(Types.STRING, name);
        packet.write(Types.BYTE, (byte) TeamAction.REMOVE);

        packet.scheduleSend(Protocol1_15_2To1_16.class);
    }

    @Override
    protected void updateTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix, @NonNull NameVisibility visibility, @NonNull CollisionRule collision, int options, @NonNull EnumChatFormat color) {
        final PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_16.SET_PLAYER_TEAM, null, connection);

        packet.write(Types.STRING, name);
        packet.write(Types.BYTE, (byte) TeamAction.UPDATE);
        packet.write(Types.COMPONENT, toComponent(name));
        packet.write(Types.BYTE, (byte) options);
        packet.write(Types.STRING, visibility.toString());
        packet.write(Types.STRING, collision.toString());
        packet.write(Types.VAR_INT, color.ordinal());
        packet.write(Types.COMPONENT, toComponent(prefix));
        packet.write(Types.COMPONENT, toComponent(suffix));

        packet.scheduleSend(Protocol1_15_2To1_16.class);
    }

    private JsonElement toComponent(@NonNull String component) {
        return toComponent(TabComponent.optimized(component));
    }

    @Override
    public JsonElement toComponent(@NonNull TabComponent component) {
        return ViaVersionHook.getInstance().getJson(component);
    }
}
