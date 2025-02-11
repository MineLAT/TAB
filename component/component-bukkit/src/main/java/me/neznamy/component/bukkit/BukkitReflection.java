package me.neznamy.component.bukkit;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Class containing main NMS-related information and methods.
 */
public class BukkitReflection {

    /** Server minor version */
    @Getter
    private static final int minorVersion;

    /** Server version data */
    private static Function<String, Class<?>> classFunction;

    static {
        classFunction = BukkitReflection::modernClass;
        String[] array =  Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        if (array.length > 3) {
            // Normal packaging
            String serverPackage = array[3];
            minorVersion = Integer.parseInt(serverPackage.split("_")[1]);
            if (minorVersion < 17) {
                classFunction = name -> legacyClass(serverPackage, name);
            }
        } else {
            // Paper without CB relocation
            minorVersion = Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]);
        }
    }

    @NotNull
    @SneakyThrows
    private static Class<?> modernClass(@NotNull String name) {
        return Class.forName("net.minecraft." + name);
    }

    @NotNull
    @SneakyThrows
    private static Class<?> legacyClass(@NotNull String serverPackage, @NotNull String name) {
        return BukkitReflection.class.getClassLoader().loadClass("net.minecraft.server." + serverPackage + "." + name);
    }

    /**
     * Returns class with given potential names in same order. For 1.17+ it takes packaged class names
     * without "net.minecraft." prefix, for <1.17 it takes class name only.
     *
     * @param   names
     *          possible class names
     * @return  class for specified names
     */
    @SneakyThrows
    public static Class<?> getClass(@NotNull String... names) {
        for (String name : names) {
            try {
                return classFunction.apply(name);
            } catch (Exception ignored) {
                // not the first class name in array
            }
        }
        throw new ClassNotFoundException("No class found with possible names " + Arrays.toString(names));
    }
}
