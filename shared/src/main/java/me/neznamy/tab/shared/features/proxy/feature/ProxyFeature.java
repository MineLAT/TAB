package me.neznamy.tab.shared.features.proxy.feature;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import me.neznamy.tab.shared.features.proxy.ProxyPlayer;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class ProxyFeature {

    public abstract void onJoin(@NotNull TabPlayer player);

    public abstract void onJoin(@NotNull ProxyPlayer player);

    public void onServerSwitch(@NotNull TabPlayer player) {/* Do nothing by default */}

    public void onServerSwitch(@NotNull ProxyPlayer player) {/* Do nothing by default */}

    public void onQuit(@NotNull ProxyPlayer player) {/* Do nothing by default */}

    public abstract void write(@NotNull ByteArrayDataOutput out, @NotNull TabPlayer player);

    public abstract void read(@NotNull ByteArrayDataInput in, @NotNull ProxyPlayer player);

    public void onLoginPacket(@NotNull TabPlayer player) {/* Do nothing by default */}

    public void onTabListClear(@NotNull TabPlayer player) {/* Do nothing by default */}

    public void onVanishStatusChange(@NotNull ProxyPlayer player) {/* Do nothing by default */}
}
