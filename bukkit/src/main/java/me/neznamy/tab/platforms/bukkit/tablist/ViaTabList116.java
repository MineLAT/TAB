package me.neznamy.tab.platforms.bukkit.tablist;

import com.viaversion.viaversion.api.minecraft.GameProfile;
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

import java.util.UUID;

public class ViaTabList116 extends ViaTabList<JsonElement> {

    private static final int ADD_PLAYER = 0;
    private static final int UPDATE_GAME_MODE = 1;
    private static final int UPDATE_LATENCY = 2;
    private static final int UPDATE_DISPLAY_NAME = 3;
    private static final int REMOVE_PLAYER = 4;

    /**
     * Constructs new instance with given parameters.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    public ViaTabList116(@NonNull BukkitTabPlayer player) {
        this(player, Protocol1_15_2To1_16.class, ClientboundPackets1_16.PLAYER_INFO, ClientboundPackets1_16.TAB_LIST);
    }

    protected ViaTabList116(@NonNull BukkitTabPlayer player, @NonNull Class<? extends Protocol> protocol, @NonNull PacketType playerInfoUpdate, @NonNull PacketType tabList) {
        super(player, protocol, playerInfoUpdate, tabList);
    }

    @Override
    public void removeEntry(@NonNull UUID entry) {
        sendInfoUpdate(Action.REMOVE_PLAYER, entry, null);
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        // Added on 1.19.3
    }

    @Override
    protected void writeComponent(@NonNull PacketWrapper packet, @NonNull JsonElement component) {
        packet.write(Types.COMPONENT, component);
    }

    @Override
    protected void writeOptionalComponent(@NonNull PacketWrapper packet, @Nullable JsonElement component) {
        packet.write(Types.OPTIONAL_COMPONENT, component);
    }

    @Override
    protected void writeAction(@NonNull PacketWrapper packet, @NonNull Action action) {
        switch (action) {
            case ADD_PLAYER:
                packet.write(Types.VAR_INT, ADD_PLAYER);
                break;
            case REMOVE_PLAYER:
                packet.write(Types.VAR_INT, REMOVE_PLAYER);
                break;
            case UPDATE_DISPLAY_NAME:
                packet.write(Types.VAR_INT, UPDATE_DISPLAY_NAME);
                break;
            case UPDATE_LATENCY:
                packet.write(Types.VAR_INT, UPDATE_LATENCY);
                break;
            case UPDATE_GAME_MODE:
                packet.write(Types.VAR_INT, UPDATE_GAME_MODE);
                break;
            default:
                throw new IllegalStateException("Cannot write " + action.name() + " for 1.16 packet");
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
        packet.write(Types.VAR_INT, entry.getGameMode());
        packet.write(Types.VAR_INT, entry.getLatency());
        writeOptionalComponent(packet, displayName);
    }

    @Override
    public JsonElement toComponent(@NonNull TabComponent component) {
        return ViaVersionHook.getInstance().getJson(component);
    }
}
