package me.neznamy.component.fabric;

import me.neznamy.component.shared.ComponentConverter;
import org.jetbrains.annotations.NotNull;

/**
 * Instance finder for Fabric.
 */
public class FabricComponentConverter {

    /**
     * Returns an instance based on the server minor version.
     *
     * @param   minorVersion
     *          Server minor version
     * @return  Instance based on the server version
     */
    @NotNull
    public static ComponentConverter<?> findInstance(int minorVersion) {
        if (minorVersion >= 19) {
            // 1.19+
            return new ModernComponentConverter();
        } else if (minorVersion >= 16) {
            // 1.16 - 1.18.2
            return new ModerateComponentConverter();
        } else {
            // 1.14 - 1.15.2
            return new LegacyComponentConverter();
        }
    }
}
