package me.neznamy.component.bungee;

import me.neznamy.component.shared.ChatModifier;
import me.neznamy.component.shared.StructuredComponentConverter;
import me.neznamy.component.shared.component.TabComponent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Component converter for BungeeCord.
 */
public class BungeeComponentConverter extends StructuredComponentConverter<BaseComponent> {
    
    @Override
    @NotNull
    public BaseComponent newTextComponent(@NotNull String text) {
        return new TextComponent(text);
    }

    @Override
    @NotNull
    public BaseComponent newTranslatableComponent(@NotNull String key) {
        return new TranslatableComponent(key);
    }

    @Override
    @NotNull
    public BaseComponent newKeybindComponent(@NotNull String keybind) {
        return new KeybindComponent(keybind);
    }

    @Override
    @NotNull
    public BaseComponent applyStyle(@NotNull BaseComponent component, @NotNull ChatModifier style) {
        component.setStyle(convertStyle(style, true));
        return component;
    }

    @Override
    @NotNull
    public BaseComponent setExtra(@NotNull BaseComponent parent, @NotNull List<BaseComponent> children) {
        parent.setExtra(children);
        return parent;
    }

    @NotNull
    private ComponentStyle convertStyle(@NotNull ChatModifier modifier, boolean rgbSupport) {
        ComponentStyle style = new ComponentStyle();
        if (modifier.getColor() != null) {
            if (rgbSupport) {
                style.setColor(ChatColor.of("#" + modifier.getColor().getHexCode()));
            } else {
                style.setColor(ChatColor.of(modifier.getColor().getLegacyColor().name()));
            }
        }
        style.setShadowColor(modifier.getShadowColor() == null ? null : new Color(
                (modifier.getShadowColor() >> 16) & 0xFF,
                (modifier.getShadowColor() >> 8) & 0xFF,
                (modifier.getShadowColor()) & 0xFF,
                (modifier.getShadowColor() >> 24) & 0xFF
        ));
        style.setBold(modifier.getBold());
        style.setItalic(modifier.getItalic());
        style.setObfuscated(modifier.getObfuscated());
        style.setStrikethrough(modifier.getStrikethrough());
        style.setUnderlined(modifier.getUnderlined());
        style.setFont(modifier.getFont());
        return style;
    }

    /**
     * Creates a bungee component using legacy colors (for <1.16 players).
     *
     * @param   component
     *          Component to convert
     * @return  Converted component
     */
    @NotNull
    public BaseComponent legacyComponent(@NotNull TabComponent component) {
        // Component type
        BaseComponent converted;
        if (component instanceof me.neznamy.component.shared.component.TextComponent) {
            converted = newTextComponent(((me.neznamy.component.shared.component.TextComponent) component).getText());
        } else if (component instanceof me.neznamy.component.shared.component.TranslatableComponent) {
            converted = newTranslatableComponent(((me.neznamy.component.shared.component.TranslatableComponent) component).getKey());
        } else if (component instanceof me.neznamy.component.shared.component.KeybindComponent) {
            converted = newKeybindComponent(((me.neznamy.component.shared.component.KeybindComponent)component).getKeybind());
        } else {
            throw new IllegalArgumentException("Unexpected component type: " + component.getClass().getName());
        }

        // Component style
        converted.setStyle(convertStyle(component.getModifier(), false));

        // Extra
        List<BaseComponent> children = new ArrayList<>();
        for (TabComponent extra : component.getExtra()) {
            children.add(legacyComponent(extra));
        }
        if (!children.isEmpty()) converted = setExtra(converted, children);

        return converted;
    }
}
