package me.neznamy.tab.platforms.bukkit.tablist;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.Protocol1_15_2To1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.hook.ViaVersionHook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public class ViaTabList extends TabListBase<JsonElement> {

    private static final int ADD_PLAYER = 0;
    private static final int UPDATE_GAME_MODE = 1;
    private static final int UPDATE_LATENCY = 2;
    private static final int UPDATE_DISPLAY_NAME = 3;
    private static final int REMOVE_PLAYER = 4;

    private final UserConnection connection;

    /**
     *  Constructs new instance with given player.
     *
     * @param player Player this tablist will belong to
     */
    protected ViaTabList(@NotNull BukkitTabPlayer player) {
        super(player);
        this.connection = Via.getManager().getConnectionManager().getConnectedClient(player.getUniqueId());
    }

    @Override
    public void removeEntries(@NonNull Collection<UUID> entries) {
        final PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_16.PLAYER_INFO, null, connection);

        // Action
        packet.write(Types.VAR_INT, REMOVE_PLAYER);
        // Players size
        packet.write(Types.VAR_INT, entries.size());
        // Players
        for (UUID entry : entries) {
            packet.write(Types.UUID, entry);
        }

        packet.scheduleSend(Protocol1_15_2To1_16.class);
    }

    @Override
    public void removeEntry(@NonNull UUID entry) {
        final PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_16.PLAYER_INFO, null, connection);

        // Action
        packet.write(Types.VAR_INT, REMOVE_PLAYER);
        // Players size
        packet.write(Types.VAR_INT, 1);
        // Players
        packet.write(Types.UUID, entry);

        packet.scheduleSend(Protocol1_15_2To1_16.class);
    }

    @Override
    public void updateDisplayName0(@NonNull UUID entry, @Nullable JsonElement displayName) {
        final PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_16.PLAYER_INFO, null, connection);

        // Action
        packet.write(Types.VAR_INT, UPDATE_DISPLAY_NAME);
        // Players size
        packet.write(Types.VAR_INT, 1);
        // Players
        packet.write(Types.UUID, entry);
        packet.write(Types.OPTIONAL_COMPONENT, displayName);

        packet.scheduleSend(Protocol1_15_2To1_16.class);
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        updateInteger(entry, UPDATE_LATENCY, latency);
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        updateInteger(entry, UPDATE_GAME_MODE, gameMode);
    }

    private void updateInteger(@NonNull UUID entry, int action, int value) {
        final PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_16.PLAYER_INFO, null, connection);

        // Action
        packet.write(Types.VAR_INT, action);
        // Players size
        packet.write(Types.VAR_INT, 1);
        // Players
        packet.write(Types.UUID, entry);
        packet.write(Types.VAR_INT, value);

        packet.scheduleSend(Protocol1_15_2To1_16.class);
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        // Added in 1.19.3
    }

    @Override
    public void addEntries(@NonNull Collection<Entry> entries) {
        final PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_16.PLAYER_INFO, null, connection);

        // Action
        packet.write(Types.VAR_INT, ADD_PLAYER);
        // Players size
        packet.write(Types.VAR_INT, entries.size());
        // Players
        for (Entry entry : entries) {
            packet.write(Types.UUID, entry.getUniqueId());
            packet.write(Types.STRING, entry.getName());
            if (entry.getSkin() == null) {
                // Properties size
                packet.write(Types.VAR_INT, 0);
            } else {
                // Properties size
                packet.write(Types.VAR_INT, 1);
                // Properties
                packet.write(Types.STRING, entry.getName());
                packet.write(Types.STRING, entry.getSkin().getValue());
                packet.write(Types.OPTIONAL_STRING, entry.getSkin().getSignature());
            }
            packet.write(Types.VAR_INT, entry.getGameMode());
            packet.write(Types.VAR_INT, entry.getLatency());

            final JsonElement displayName = entry.getDisplayName() == null ? null : toComponent(entry.getDisplayName());
            setExpectedDisplayName(entry.getUniqueId(), displayName);
            packet.write(Types.OPTIONAL_COMPONENT, displayName);
        }

        packet.scheduleSend(Protocol1_15_2To1_16.class);
    }

    @Override
    public void addEntry0(@NonNull UUID id, @NonNull String name, @Nullable Skin skin, boolean listed, int latency, int gameMode, @Nullable JsonElement displayName) {
        final PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_16.PLAYER_INFO, null, connection);

        // Action
        packet.write(Types.VAR_INT, ADD_PLAYER);
        // Players size
        packet.write(Types.VAR_INT, 1);
        // Players
        packet.write(Types.UUID, id);
        packet.write(Types.STRING, name);
        if (skin == null) {
            // Properties size
            packet.write(Types.VAR_INT, 0);
        } else {
            // Properties size
            packet.write(Types.VAR_INT, 1);
            // Properties
            packet.write(Types.STRING, name);
            packet.write(Types.STRING, skin.getValue());
            packet.write(Types.OPTIONAL_STRING, skin.getSignature());
        }
        packet.write(Types.VAR_INT, gameMode);
        packet.write(Types.VAR_INT, latency);
        packet.write(Types.OPTIONAL_COMPONENT, displayName);

        packet.scheduleSend(Protocol1_15_2To1_16.class);
    }

    @Override
    public void setPlayerListHeaderFooter0(@NonNull JsonElement header, @NonNull JsonElement footer) {
        final PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_16.TAB_LIST, null, connection);

        packet.write(Types.COMPONENT, header);
        packet.write(Types.COMPONENT, footer);

        packet.scheduleSend(Protocol1_15_2To1_16.class);
    }

    @Override
    public JsonElement toComponent(@NonNull TabComponent component) {
        return ViaVersionHook.getInstance().getJson(component);
    }
}
