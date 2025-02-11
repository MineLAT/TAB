package me.neznamy.component.shared.hook;

import me.neznamy.component.shared.ChatModifier;
import me.neznamy.component.shared.StructuredComponentConverter;
import me.neznamy.component.shared.component.KeybindComponent;
import me.neznamy.component.shared.component.TabComponent;
import me.neznamy.component.shared.component.TextComponent;
import me.neznamy.component.shared.component.TranslatableComponent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Class for Adventure component conversion.
 */
public class AdventureComponentConverter extends StructuredComponentConverter<Component> {

    /** Flag for tracking presence of shadow color parameter in current included adventure library (added in 1.21.4) */
    private static final boolean SHADOW_COLOR_AVAILABLE;

    static {
        boolean value;
        try {
            Component.class.getDeclaredMethod("shadowColor");
            value = true;
        } catch (Throwable t) {
            value = false;
        }
        SHADOW_COLOR_AVAILABLE = value;
    }

    @Override
    @NotNull
    public Component newTextComponent(@NotNull String text) {
        return Component.text(text);
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
    @NotNull
    public Component applyStyle(@NotNull Component component, @NotNull ChatModifier style) {
        Style.Builder builder = Style.style()
                .color(style.getColor() == null ? null : TextColor.color(style.getColor().getRgb()))
                .decoration(TextDecoration.BOLD, getDecoration(style.getBold()))
                .decoration(TextDecoration.ITALIC, getDecoration(style.getItalic()))
                .decoration(TextDecoration.UNDERLINED, getDecoration(style.getUnderlined()))
                .decoration(TextDecoration.STRIKETHROUGH, getDecoration(style.getStrikethrough()))
                .decoration(TextDecoration.OBFUSCATED, getDecoration(style.getObfuscated()))
                .font(style.getFont() == null ? null : Key.key(style.getFont()));
        if (SHADOW_COLOR_AVAILABLE) {
            AdventureShadowHook.setShadowColor(builder, style.getShadowColor());
        }
        return component.style(builder.build());
    }

    @Override
    @NotNull
    public Component setExtra(@NotNull Component parent, @NotNull List<Component> children) {
        return parent.children(children);
    }

    @NotNull
    private TextDecoration.State getDecoration(@Nullable Boolean state) {
        if (state == null) return TextDecoration.State.NOT_SET;
        return state ? TextDecoration.State.TRUE : TextDecoration.State.FALSE;
    }

    /**
     * Converts adventure component to TAB component
     *
     * @param   component
     *          Component to convert
     * @return  TAB component from adventure component.
     */
    @NotNull
    public static TabComponent convert(@NotNull Component component) {
        // Component type
        TabComponent tabComponent;
        if (component instanceof net.kyori.adventure.text.TextComponent) {
            tabComponent = new TextComponent(((net.kyori.adventure.text.TextComponent) component).content());
        } else if (component instanceof net.kyori.adventure.text.TranslatableComponent) {
            tabComponent = new TranslatableComponent(((net.kyori.adventure.text.TranslatableComponent) component).key());
        } else if (component instanceof net.kyori.adventure.text.KeybindComponent) {
            tabComponent = new KeybindComponent(((net.kyori.adventure.text.KeybindComponent) component).keybind());
        } else {
            throw new UnsupportedOperationException(component.getClass().getName() + " component type is not supported");
        }

        // Component style
        Map<TextDecoration, TextDecoration.State> decorations = component.style().decorations();
        tabComponent.setModifier(new ChatModifier(
                component.color() == null ? null : new me.neznamy.component.shared.TextColor(component.color().value()),
                SHADOW_COLOR_AVAILABLE ? AdventureShadowHook.getShadowColor(component) : null,
                getDecoration(decorations.get(TextDecoration.BOLD)),
                getDecoration(decorations.get(TextDecoration.ITALIC)),
                getDecoration(decorations.get(TextDecoration.UNDERLINED)),
                getDecoration(decorations.get(TextDecoration.STRIKETHROUGH)),
                getDecoration(decorations.get(TextDecoration.OBFUSCATED)),
                component.font() == null ? null : component.font().asString()
        ));

        // Extra
        for (Component extra : component.children()) {
            tabComponent.addExtra(convert(extra));
        }

        // Save original component to prevent potential data loss and avoid redundant conversion
        tabComponent.setAdventureComponent(component);

        return tabComponent;
    }

    @Nullable
    private static Boolean getDecoration(@Nullable TextDecoration.State state) {
        if (state == null || state == TextDecoration.State.NOT_SET) return null;
        return state == TextDecoration.State.TRUE;
    }
}
