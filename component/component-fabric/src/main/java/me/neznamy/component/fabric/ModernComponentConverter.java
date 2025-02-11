package me.neznamy.component.fabric;

import lombok.SneakyThrows;
import me.neznamy.component.shared.ChatModifier;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * Class for converting TAB components into NMS components for versions 1.19 and up.
 */
public class ModernComponentConverter extends ModerateComponentConverter {

    private Constructor<Style> newStyle;
    private boolean is1_21_4Plus;

    /**
     * Constructs new instance and loads style constructor.
     */
    @SneakyThrows
    public ModernComponentConverter() {
        try {
            // 1.21.4+
            newStyle = Style.class.getDeclaredConstructor(TextColor.class, Integer.class, Boolean.class, Boolean.class,
                    Boolean.class, Boolean.class, Boolean.class, ClickEvent.class, HoverEvent.class, String.class, ResourceLocation.class);
            is1_21_4Plus = true;
        } catch (ReflectiveOperationException e) {
            // 1.21.3-
            newStyle = Style.class.getDeclaredConstructor(TextColor.class, Boolean.class, Boolean.class, Boolean.class,
                    Boolean.class, Boolean.class, ClickEvent.class, HoverEvent.class, String.class, ResourceLocation.class);
            is1_21_4Plus = false;
        }
        newStyle.setAccessible(true);
    }

    @Override
    @NotNull
    public Component newTextComponent(@NotNull String text) {
        return Component.literal(text);
    }

    @Override
    @NotNull
    public Component newTranslatableComponent(@NotNull String key) {
        return Component.translatable(key);
    }

    @Override
    @NotNull
    public Component newKeybindComponent(@NotNull String keybind) {
        return Component.keybind(keybind);
    }

    @Override
    @SneakyThrows
    @NotNull
    public Component applyStyle(@NotNull Component component, @NotNull ChatModifier style) {
        if (is1_21_4Plus) {
            ((MutableComponent)component).setStyle(newStyle.newInstance(
                    style.getColor() == null ? null : TextColor.fromRgb(style.getColor().getRgb()),
                    style.getShadowColor(),
                    style.getBold(),
                    style.getItalic(),
                    style.getUnderlined(),
                    style.getStrikethrough(),
                    style.getObfuscated(),
                    null,
                    null,
                    null,
                    style.getFont() == null ? null : ResourceLocation.tryParse(style.getFont())
            ));
        } else {
            ((MutableComponent)component).setStyle(newStyle.newInstance(
                    style.getColor() == null ? null : TextColor.fromRgb(style.getColor().getRgb()),
                    style.getBold(),
                    style.getItalic(),
                    style.getUnderlined(),
                    style.getStrikethrough(),
                    style.getObfuscated(),
                    null,
                    null,
                    null,
                    style.getFont() == null ? null : ResourceLocation.tryParse(style.getFont())
            ));
        }
        return component;
    }
}
