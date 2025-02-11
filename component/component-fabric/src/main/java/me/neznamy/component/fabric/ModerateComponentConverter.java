package me.neznamy.component.fabric;

import lombok.SneakyThrows;
import me.neznamy.component.shared.ChatModifier;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * Class for converting TAB components into NMS components for versions 1.16 - 1.18.2.
 */
public class ModerateComponentConverter extends LegacyComponentConverter {

    @Override
    @SneakyThrows
    @NotNull
    public Component applyStyle(@NotNull Component component, @NotNull ChatModifier modifier) {
        Constructor<Style> constructor = Style.class.getDeclaredConstructor(
                TextColor.class,
                Boolean.class,
                Boolean.class,
                Boolean.class,
                Boolean.class,
                Boolean.class,
                ClickEvent.class,
                HoverEvent.class,
                String.class,
                ResourceLocation.class
        );
        constructor.setAccessible(true);
        component.getClass().getMethod("method_10862", Style.class).invoke(component, constructor.newInstance(
                modifier.getColor() == null ? null : TextColor.fromRgb(modifier.getColor().getRgb()),
                modifier.getBold(),
                modifier.getItalic(),
                modifier.getUnderlined(),
                modifier.getStrikethrough(),
                modifier.getObfuscated(),
                null,
                null,
                null,
                modifier.getFont() == null ? null : ResourceLocation.tryParse(modifier.getFont())
        ));
        return component;
    }
}
