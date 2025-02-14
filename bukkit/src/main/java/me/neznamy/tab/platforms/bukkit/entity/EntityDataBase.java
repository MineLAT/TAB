package me.neznamy.tab.platforms.bukkit.entity;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.backend.EntityData;
import org.jetbrains.annotations.NotNull;

public interface EntityDataBase extends EntityData {

    /**
     * Writes entity byte flags
     *
     * @param   flags
     *          flags to write
     */
    void setEntityFlags(byte flags);

    /**
     * Writes entity custom name with position based on server version and value depending on client version (RGB or not)
     *
     * @param   customName
     *          target custom name
     * @param   clientVersion
     *          client version
     */
    void setCustomName(@NotNull String customName, @NotNull ProtocolVersion clientVersion);

    /**
     * Writes custom name visibility boolean
     *
     * @param   visible
     *          if visible or not
     */
    void setCustomNameVisible(boolean visible);

    /**
     * Writes entity health
     *
     * @param   health
     *          health of entity
     */
    void setHealth(float health);

    /**
     * Writes armor stand flags
     *
     * @param   flags
     *          flags to write
     */
    void setArmorStandFlags(byte flags);

    /**
     * Writes wither invulnerable time
     * @param   time
     *          Time, apparently
     */
    void setWitherInvulnerableTime(int time);
}
