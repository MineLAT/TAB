package me.neznamy.tab.platforms.bukkit.scoreboard;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.type.Types;
import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.scoreboard.packet.PacketScoreboard;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.Scoreboard;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public abstract class ViaScoreboard<C> extends Scoreboard<BukkitTabPlayer, C> {

    private static final long CHECK_DELAY = 50;
    private static final Object DUMMY = new Object();

    protected final Class<? extends Protocol> protocol;
    protected final PacketType setDisplayObjective;
    private final PacketType setObjective;
    protected final PacketType setScore;
    private final PacketType setPlayerTeam;
    /** User connection this scoreboard belongs to */
    protected final UserConnection connection;

    private transient ScheduledFuture<?> task;
    private transient final Queue<PacketWrapper> queuedPackets = new ConcurrentLinkedQueue<>();

    /**
     * Constructs new instance with given player.
     *
     * @param player
     *        Player this scoreboard will belong to
     * @param protocol
     *        Protocol to be sent packets through
     * @param setDisplayObjective
     *        Objective creation packet
     * @param setObjective
     *        Objective update packet
     * @param setScore
     *        Score update packet
     * @param setPlayerTeam
     *        Player team packet
     */
    public ViaScoreboard(@NonNull BukkitTabPlayer player, @NonNull Class<? extends Protocol> protocol, @NonNull PacketType setDisplayObjective, @NonNull PacketType setObjective, @NonNull PacketType setScore, @NonNull PacketType setPlayerTeam) {
        super(player);
        this.protocol = protocol;
        this.setDisplayObjective = setDisplayObjective;
        this.setObjective = setObjective;
        this.setScore = setScore;
        this.setPlayerTeam = setPlayerTeam;
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
    public void onPacketSend(@NonNull Object packet) {
        PacketScoreboard.onPacketSend(this, player, packet);
    }

    @Override
    protected void registerObjective0(@NonNull String objectiveName, @NonNull String title, int display, @Nullable C numberFormat) {
        sendObjectiveUpdate(ObjectiveAction.REGISTER, objectiveName, title, display, numberFormat);
    }

    @Override
    protected void unregisterObjective0(@NonNull String objectiveName) {
        sendObjectiveUpdate(ObjectiveAction.UNREGISTER, objectiveName);
    }

    @Override
    protected void updateObjective0(@NonNull String objectiveName, @NonNull String title, int display, @Nullable C numberFormat) {
        sendObjectiveUpdate(ObjectiveAction.UPDATE, objectiveName, title, display, numberFormat);
    }

    @Override
    protected void registerTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix, @NonNull NameVisibility visibility, @NonNull CollisionRule collision, @NonNull Collection<String> players, int options, @NonNull EnumChatFormat color) {
        sendTeamPacket(TeamAction.CREATE, name, packet -> {
            writeTeamUpdate(packet, name, prefix, suffix, visibility, collision, options, color);

            // Entities
            packet.write(Types.VAR_INT, players.size());
            for (String entity : players) {
                // Entity (player username in this case) (max 40 characters on pre 1.20.3)
                packet.write(Types.STRING, entity);
            }
        });
    }

    @Override
    protected void unregisterTeam0(@NonNull String name) {
        sendTeamPacket(TeamAction.REMOVE, name);
    }

    @Override
    protected void updateTeam0(@NonNull String name, @NonNull String prefix, @NonNull String suffix, @NonNull NameVisibility visibility, @NonNull CollisionRule collision, int options, @NonNull EnumChatFormat color) {
        sendTeamPacket(TeamAction.UPDATE, name, packet -> {
            writeTeamUpdate(packet, name, prefix, suffix, visibility, collision, options, color);
        });
    }

    protected void sendObjectiveUpdate(int mode, @NonNull String objectiveName) {
        sendObjectiveUpdate(mode, objectiveName, null, null, null);
    }

    protected void sendObjectiveUpdate(int mode, @NonNull String objectiveName, String title, Integer display, C numberFormat) {
        final PacketWrapper packet = PacketWrapper.create(setObjective, null, connection);

        // Objective name
        packet.write(Types.STRING, objectiveName);
        // Mode
        packet.write(Types.BYTE, (byte) mode);
        // Optional
        if (mode == ObjectiveAction.REGISTER || mode == ObjectiveAction.UPDATE) {
            writeObjectiveDisplay(packet, title, display, numberFormat);
        }

        send(packet);
    }

    protected void sendTeamPacket(int mode, @NonNull String name) {
        sendTeamPacket(mode, name, null);
    }

    protected void sendTeamPacket(int mode, @NonNull String name, @Nullable Consumer<PacketWrapper> consumer) {
        final PacketWrapper packet = PacketWrapper.create(setPlayerTeam, null, connection);

        // Team name (max 16 characters on pre 1.20.3)
        packet.write(Types.STRING, name);
        // Method
        packet.write(Types.BYTE, (byte) mode);
        if (consumer != null) {
            consumer.accept(packet);
        }

        send(packet);
    }

    protected abstract void writeComponent(@NonNull PacketWrapper packet, @NonNull C component);

    protected abstract void writeObjectiveDisplay(@NonNull PacketWrapper packet, @NonNull String title, int display, @Nullable C numberFormat);

    protected void writeTeamUpdate(@NonNull PacketWrapper packet, @NonNull String name, @NonNull String prefix, @NonNull String suffix, @NonNull NameVisibility visibility, @NonNull CollisionRule collision, int options, @NonNull EnumChatFormat color) {
        // Team display name
        writeComponent(packet, stringToComponent(name));
        // Friendly flags
        packet.write(Types.BYTE, (byte) options);
        // Name tag visibility
        packet.write(Types.STRING, visibility.toString());
        // Collision rule
        packet.write(Types.STRING, collision.toString());
        // Team color
        packet.write(Types.VAR_INT, color.ordinal());
        // Team prefix
        writeComponent(packet, stringToComponent(prefix));
        // Team suffix
        writeComponent(packet, stringToComponent(suffix));
    }

    protected void send(@NonNull PacketWrapper packet) {
        if (!task.isCancelled()) {
            queuedPackets.add(packet);
        } else {
            packet.scheduleSend(protocol);
        }
    }

    public C stringToComponent(@NonNull String component) {
        return toComponent(TabComponent.optimized(component));
    }
}
