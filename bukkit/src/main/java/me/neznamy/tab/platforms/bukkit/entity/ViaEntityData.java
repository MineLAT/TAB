package me.neznamy.tab.platforms.bukkit.entity;

import com.viaversion.viaversion.api.minecraft.entitydata.EntityData;
import com.viaversion.viaversion.api.minecraft.entitydata.EntityDataType;
import com.viaversion.viaversion.api.type.types.version.Types1_16;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.hook.ViaVersionHook;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ViaEntityData implements EntityDataBase {

    private final List<EntityData> items = new ArrayList<>();

    private void item(int index, @NotNull EntityDataType dataType, @Nullable Object value) {
        items.add(new EntityData(index, dataType, value));
    }

    @Override
    public void setEntityFlags(byte flags) {
        item(0, Types1_16.ENTITY_DATA_TYPES.byteType, flags);
    }

    @Override
    public void setCustomName(@NotNull String customName, @NotNull ProtocolVersion clientVersion) {
        item(2, Types1_16.ENTITY_DATA_TYPES.optionalComponentType, ViaVersionHook.getInstance().getJson(TabComponent.optimized(customName)));
    }

    @Override
    public void setCustomNameVisible(boolean visible) {
        item(3, Types1_16.ENTITY_DATA_TYPES.booleanType, visible);
    }

    @Override
    public void setHealth(float health) {
        item(8, Types1_16.ENTITY_DATA_TYPES.floatType, health);
    }

    @Override
    public void setArmorStandFlags(byte flags) {
        item(14, Types1_16.ENTITY_DATA_TYPES.byteType, flags);
    }

    @Override
    public void setWitherInvulnerableTime(int time) {
        item(18, Types1_16.ENTITY_DATA_TYPES.varIntType, time);
    }

    @Override
    public @NotNull List<EntityData> build() {
        return items;
    }
}
