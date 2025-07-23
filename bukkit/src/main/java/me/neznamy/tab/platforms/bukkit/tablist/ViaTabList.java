package me.neznamy.tab.platforms.bukkit.tablist;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.type.Types;
import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.Collection;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class ViaTabList<C> extends TabListBase<C> {

    private static final long CHECK_DELAY = 50;

    protected final Class<? extends Protocol> protocol;
    private final PacketType playerInfoUpdate;
    private final PacketType tabList;
    /** User connection this tablist belongs to */
    protected final UserConnection connection;

    private transient ScheduledFuture<?> task;
    private transient final Queue<PacketWrapper> queuedPackets = new ConcurrentLinkedQueue<>();

    /**
     *
     * @param player
     *        Player this tablist will belong to
     * @param protocol
     *        Protocol to be sent packets through
     * @param playerInfoUpdate
     *        Player information update packet
     * @param tabList
     *        Tab list packet
     */
    protected ViaTabList(@NonNull BukkitTabPlayer player, @NonNull Class<? extends Protocol> protocol, @NonNull PacketType playerInfoUpdate, @NonNull PacketType tabList) {
        super(player);
        this.protocol = protocol;
        this.playerInfoUpdate = playerInfoUpdate;
        this.tabList = tabList;
        this.connection = Via.getManager().getConnectionManager().getConnectedClient(player.getUniqueId());

        // Queue packets until connection is available
        // Is important to use scheduleWithFixedDelay() to avoid task overlap
        task = connection.getChannel().eventLoop().scheduleWithFixedDelay(() -> {
            if (connection.getProtocolInfo().getClientState() == State.PLAY) {
                PacketWrapper packet;
                while ((packet = queuedPackets.poll()) != null) {
                    packet.send(protocol);
                }
                task.cancel(true);
            }
        }, 0L, CHECK_DELAY, TimeUnit.MILLISECONDS);
    }

    @Override
    public void updateDisplayName0(@NonNull UUID entry, @Nullable C displayName) {
        sendInfoUpdate(Action.UPDATE_DISPLAY_NAME, entry, displayName);
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        sendInfoUpdate(Action.UPDATE_LATENCY, entry, latency);
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        sendInfoUpdate(Action.UPDATE_GAME_MODE, entry, gameMode);
    }

    @Override
    public void addEntries(@NonNull Collection<Entry> entries) {
        final PacketWrapper packet = PacketWrapper.create(playerInfoUpdate, null, connection);

        // Action
        writeAction(packet, Action.ADD_PLAYER);
        // Players size
        packet.write(Types.VAR_INT, entries.size());
        // Players
        for (Entry entry : entries) {
            packet.write(Types.UUID, entry.getUniqueId());
            writeEntry(packet, entry, entry.getDisplayName() == null ? null : toComponent(entry.getDisplayName()));
        }

        send(packet);
    }

    @Override
    public void addEntry0(@NonNull Entry entry, @Nullable C displayName) {
        sendInfoUpdate(Action.ADD_PLAYER, entry.getUniqueId(), entry, displayName);
    }

    @Override
    public void setPlayerListHeaderFooter0(@NonNull C header, @NonNull C footer) {
        final PacketWrapper packet = PacketWrapper.create(tabList, null, connection);

        writeComponent(packet, header);
        writeComponent(packet, footer);

        send(packet);
    }

    protected void sendInfoUpdate(@NonNull Action action, @NonNull UUID uniqueId, Object value) {
        sendInfoUpdate(action, uniqueId, value, null);
    }

    @SuppressWarnings("unchecked")
    protected void sendInfoUpdate(@NonNull Action action, @NonNull UUID uniqueId, Object value, @Nullable C displayName) {
        final PacketWrapper packet = PacketWrapper.create(playerInfoUpdate, null, connection);

        // Action
        writeAction(packet, action);
        // Players size
        packet.write(Types.VAR_INT, 1);
        // Players
        packet.write(Types.UUID, uniqueId);
        switch (action) {
            case ADD_PLAYER:
                writeEntry(packet, (Entry) value, displayName);
                break;
            case REMOVE_PLAYER:
                break;
            case UPDATE_GAME_MODE:
            case UPDATE_LATENCY:
            //case UPDATE_LIST_ORDER:
                packet.write(Types.VAR_INT, (int) value);
                break;
            case UPDATE_LISTED:
            //case UPDATE_HAT:
                packet.write(Types.BOOLEAN, (boolean) value);
                break;
            case UPDATE_DISPLAY_NAME:
                writeOptionalComponent(packet, (C) value);
                break;
            default:
                throw new IllegalArgumentException("Cannot send info update with action " + action.name());
        }

        send(packet);
    }

    protected abstract void writeComponent(@NonNull PacketWrapper packet, @NonNull C component);

    protected abstract void writeOptionalComponent(@NonNull PacketWrapper packet, @Nullable C component);

    protected abstract void writeAction(@NonNull PacketWrapper packet, @NonNull Action action);

    protected abstract void writeEntry(@NonNull PacketWrapper packet, @NonNull Entry entry, @Nullable C displayName);

    @NotNull
    protected static BitSet bitSet(int nbits, int action) {
        final BitSet bitSet = new BitSet(nbits);
        bitSet.set(action);
        return bitSet;
    }

    @NotNull
    protected static BitSet bitSet(int nbits, int fromAction, int toAction) {
        final BitSet bit = new BitSet(nbits);
        bit.set(fromAction, toAction);
        return bit;
    }

    protected void send(@NonNull PacketWrapper packet) {
        if (!task.isCancelled()) {
            queuedPackets.add(packet);
        } else {
            packet.scheduleSend(protocol);
        }
    }
}
