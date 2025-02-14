package me.neznamy.tab.platforms.bukkit.entity;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.version.Types1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.Protocol1_15_2To1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ViaEntityView extends PacketEntityView {

    private static final int ARMOR_STAND = 1;

    private final UserConnection connection;

    public ViaEntityView(BukkitTabPlayer player) {
        super(player);
        this.connection = Via.getManager().getConnectionManager().getConnectedClient(player.getUniqueId());
    }

    @Override
    public void spawnEntity(int entityId, @NotNull UUID id, @NotNull Object entityType, @NotNull Location location, @NotNull EntityData data) {
        final PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_16.ADD_ENTITY, null, connection);

        packet.write(Types.VAR_INT, entityId);
        packet.write(Types.UUID, id);
        packet.write(Types.VAR_INT, ARMOR_STAND);
        packet.write(Types.DOUBLE, location.getX());
        packet.write(Types.DOUBLE, location.getY());
        packet.write(Types.DOUBLE, location.getZ());
        packet.write(Types.BYTE, (byte) 0); // pitch
        packet.write(Types.BYTE, (byte) 0); // yaw
        packet.write(Types.INT, 0); // data
        packet.write(Types.SHORT, (short) 0); // velocity x
        packet.write(Types.SHORT, (short) 0); // velocity y
        packet.write(Types.SHORT, (short) 0); // velocity z

        final PacketWrapper metadata = getMetadataPacket(entityId, (ViaEntityData) data);

        packet.scheduleSend(Protocol1_15_2To1_16.class);
        metadata.scheduleSend(Protocol1_15_2To1_16.class);
    }

    @Override
    public void updateEntityMetadata(int entityId, @NotNull EntityData data) {
        getMetadataPacket(entityId, (ViaEntityData) data).scheduleSend(Protocol1_15_2To1_16.class);
    }

    @NotNull
    private PacketWrapper getMetadataPacket(int entityId, @NotNull ViaEntityData data) {
        final PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_16.SET_ENTITY_DATA, null, connection);

        packet.write(Types.VAR_INT, entityId);
        packet.write(Types1_16.ENTITY_DATA_LIST, data.build());

        return packet;
    }

    @Override
    public void teleportEntity(int entityId, @NotNull Location location) {
        final PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_16.TELEPORT_ENTITY, null, connection);

        packet.write(Types.VAR_INT, entityId);
        packet.write(Types.DOUBLE, location.getX());
        packet.write(Types.DOUBLE, location.getY());
        packet.write(Types.DOUBLE, location.getZ());
        packet.write(Types.BYTE, (byte) 0); // yaw
        packet.write(Types.BYTE, (byte) 0); // pitch
        packet.write(Types.BOOLEAN, false); // on ground

        packet.scheduleSend(Protocol1_15_2To1_16.class);
    }

    @Override
    public void destroyEntities(int... entities) {
        final PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_16.REMOVE_ENTITIES, null, connection);

        packet.write(Types.VAR_INT_ARRAY_PRIMITIVE, entities);

        packet.scheduleSend(Protocol1_15_2To1_16.class);
    }

    @Override
    public void moveEntity(int entityId, @NotNull Location moveDiff) {
        final PacketWrapper packet = PacketWrapper.create(ClientboundPackets1_16.MOVE_ENTITY_POS, null, connection);

        packet.write(Types.VAR_INT, entityId);
        packet.write(Types.SHORT, (short) moveDiff.getX());
        packet.write(Types.SHORT, (short) moveDiff.getY());
        packet.write(Types.SHORT, (short) moveDiff.getZ());
        packet.write(Types.BOOLEAN, false); // on ground

        packet.scheduleSend(Protocol1_15_2To1_16.class);
    }
}
