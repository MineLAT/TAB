package me.neznamy.component.bukkit;

import lombok.Getter;
import me.neznamy.component.shared.ComponentConverter;
import me.neznamy.component.shared.component.TabComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Component converter class for Bukkit.
 */
public class BukkitComponentConverter {

    @Nullable
    @Getter
    private static Exception exception;

    /**
     * Attempts to load component converter.
     *
     * @return  An instance for current server version
     */
    @NotNull
    public static ComponentConverter<?> findInstance() {
        try {
            if (BukkitReflection.getMinorVersion() >= 19) {
                // 1.19+
                return new ModernComponentConverter();
            } else if (BukkitReflection.getMinorVersion() >= 16) {
                // 1.16 - 1.18.2
                return new ModerateComponentConverter();
            } else if (BukkitReflection.getMinorVersion() >= 7) {
                // 1.7 - 1.15.2
                return new LegacyComponentConverter();
            }
        } catch (Exception e) {
            exception = e;
        }
        return new Empty();
    }

    private static class Empty extends ComponentConverter<Object> {

        @Override
        @NotNull
        public Object convert(@NotNull TabComponent component) {
            throw new UnsupportedOperationException("Not available");
        }
    }
}
