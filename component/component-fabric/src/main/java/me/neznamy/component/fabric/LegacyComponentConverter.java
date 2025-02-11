package me.neznamy.component.fabric;

import lombok.SneakyThrows;
import me.neznamy.component.shared.ChatModifier;
import me.neznamy.component.shared.StructuredComponentConverter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Class for converting TAB components into NMS components for versions 1.14 - 1.15.2.
 */
public class LegacyComponentConverter extends StructuredComponentConverter<Component> {

    @Override
    @NotNull
    @SneakyThrows
    public Component newTextComponent(@NotNull String text) {
        return (Component) Class.forName("net.minecraft.class_2585").getConstructor(String.class).newInstance(text);
    }

    @Override
    @NotNull
    @SneakyThrows
    public Component newTranslatableComponent(@NotNull String key) {
        return (Component) Class.forName("net.minecraft.class_2588").getConstructor(String.class, Object[].class).newInstance(key, new Object[0]);
    }

    @Override
    @NotNull
    @SneakyThrows
    public Component newKeybindComponent(@NotNull String keybind) {
        return (Component) Class.forName("net.minecraft.class_2572").getConstructor(String.class).newInstance(keybind);
    }

    @Override
    @SneakyThrows
    @NotNull
    public Component applyStyle(@NotNull Component component, @NotNull ChatModifier modifier) {
        Style style = Style.class.getConstructor().newInstance();
        if (modifier.getColor() != null) {
            style.getClass().getMethod("method_10977", ChatFormatting.class).invoke(style, ChatFormatting.valueOf(modifier.getColor().getLegacyColor().name()));
        }
        style.getClass().getMethod("method_10982", Boolean.class).invoke(style, modifier.getBold());
        style.getClass().getMethod("method_10978", Boolean.class).invoke(style, modifier.getItalic());
        style.getClass().getMethod("method_10959", Boolean.class).invoke(style, modifier.getStrikethrough());
        style.getClass().getMethod("method_10968", Boolean.class).invoke(style, modifier.getUnderlined());
        style.getClass().getMethod("method_10948", Boolean.class).invoke(style, modifier.getObfuscated());
        component.getClass().getMethod("method_10862", Style.class).invoke(component, style);
        return component;
    }

    @Override
    @SneakyThrows
    @NotNull
    @SuppressWarnings("unchecked")
    public Component setExtra(@NotNull Component parent, @NotNull List<Component> children) {
        List<Component> siblings = (List<Component>) parent.getClass().getMethod("method_10855").invoke(parent);
        siblings.addAll(children);
        return parent;
    }
}
