package me.neznamy.tab.platforms.bukkit.tablist;

import com.viaversion.viaversion.api.minecraft.GameProfile;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.Protocol1_19_1To1_19_3;
import com.viaversion.viaversion.protocols.v1_19_1to1_19_3.packet.ClientboundPackets1_19_3;
import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.UUID;

public class ViaTabList1193 extends ViaTabList116 {

    private static final BitSet ADD_PLAYER = bitSet(6, 0, 6);
    //private static final BitSet INITIALIZE_CHAT = bitSet(6, 1);
    private static final BitSet UPDATE_GAME_MODE = bitSet(6, 2);
    private static final BitSet UPDATE_LISTED = bitSet(6, 3);
    private static final BitSet UPDATE_LATENCY = bitSet(6, 4);
    private static final BitSet UPDATE_DISPLAY_NAME = bitSet(6, 5);

    private final PacketType playerInfoRemove;

    public ViaTabList1193(@NonNull BukkitTabPlayer player) {
        this(player, Protocol1_19_1To1_19_3.class, ClientboundPackets1_19_3.PLAYER_INFO_REMOVE, ClientboundPackets1_19_3.PLAYER_INFO_UPDATE, ClientboundPackets1_19_3.TAB_LIST);
    }

    protected ViaTabList1193(@NonNull BukkitTabPlayer player, @NonNull Class<? extends Protocol> protocol, @Nullable PacketType playerInfoRemove, @NonNull PacketType playerInfoUpdate, @NonNull PacketType tabList) {
        super(player, protocol, playerInfoUpdate, tabList);
        this.playerInfoRemove = playerInfoRemove;
    }

    @Override
    public void removeEntry(@NonNull UUID entry) {
        final PacketWrapper packet = PacketWrapper.create(playerInfoRemove, null, connection);

        // Players
        packet.write(Types.UUID_ARRAY, new UUID[] { entry });

        send(packet);
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        sendInfoUpdate(Action.UPDATE_LISTED, entry, listed);
    }

    @Override
    protected void writeAction(@NonNull PacketWrapper packet, @NonNull Action action) {
        switch (action) {
            case ADD_PLAYER:
                packet.write(Types.PROFILE_ACTIONS_ENUM1_19_3, ADD_PLAYER);
                break;
            case UPDATE_DISPLAY_NAME:
                packet.write(Types.PROFILE_ACTIONS_ENUM1_19_3, UPDATE_DISPLAY_NAME);
                break;
            case UPDATE_LATENCY:
                packet.write(Types.PROFILE_ACTIONS_ENUM1_19_3, UPDATE_LATENCY);
                break;
            case UPDATE_GAME_MODE:
                packet.write(Types.PROFILE_ACTIONS_ENUM1_19_3, UPDATE_GAME_MODE);
                break;
            case UPDATE_LISTED:
                packet.write(Types.PROFILE_ACTIONS_ENUM1_19_3, UPDATE_LISTED);
                break;
            default:
                throw new IllegalStateException("Cannot write " + action.name() + " for 1.19.3 packet");
        }
    }

    @Override
    protected void writeEntry(@NonNull PacketWrapper packet, @NonNull Entry entry, @Nullable JsonElement displayName) {
        packet.write(Types.STRING, entry.getName());
        // Properties
        if (entry.getSkin() == null) {
            packet.write(Types.PROFILE_PROPERTY_ARRAY, new GameProfile.Property[0]);
        } else {
            packet.write(Types.PROFILE_PROPERTY_ARRAY, new GameProfile.Property[] {
                    new GameProfile.Property(TEXTURES_PROPERTY, entry.getSkin().getValue(), entry.getSkin().getSignature())
            });
        }
        packet.write(Types.BOOLEAN, false); // No chat session data
        packet.write(Types.VAR_INT, entry.getGameMode());
        packet.write(Types.BOOLEAN, entry.isListed());
        packet.write(Types.VAR_INT, entry.getLatency());
        writeOptionalComponent(packet, displayName);
    }
}
