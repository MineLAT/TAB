package me.neznamy.component.shared;

import me.neznamy.component.shared.component.KeybindComponent;
import me.neznamy.component.shared.component.TabComponent;
import me.neznamy.component.shared.component.TextComponent;
import me.neznamy.component.shared.component.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Component converter class that breaks down convert method into multiple smaller ones.
 *
 * @param   <C>
 *          Platform's component type
 */
public abstract class StructuredComponentConverter<C> extends ComponentConverter<C> {

    @Override
    @NotNull
    public C convert(@NotNull TabComponent component) {
        // Component type
        C converted;
        if (component instanceof TextComponent) {
            converted = newTextComponent(((TextComponent) component).getText());
        } else if (component instanceof TranslatableComponent) {
            converted = newTranslatableComponent(((TranslatableComponent) component).getKey());
        } else if (component instanceof KeybindComponent) {
            converted = newKeybindComponent(((KeybindComponent)component).getKeybind());
        } else {
            throw new IllegalArgumentException("Unexpected component type: " + component.getClass().getName());
        }

        // Component style
        converted = applyStyle(converted, component.getModifier());

        // Extra
        List<C> children = new ArrayList<>();
        for (TabComponent extra : component.getExtra()) {
            children.add(convert(extra));
        }
        if (!children.isEmpty()) converted = setExtra(converted, children);

        return converted;
    }

    /**
     * Creates a new text component with given text.
     *
     * @param   text
     *          Text to display
     * @return  Text component with given text
     */
    @NotNull
    public abstract C newTextComponent(@NotNull String text);

    /**
     * Creates a new translatable component with the given key.
     *
     * @param   key
     *          Key to translate
     * @return  Translatable component with the given key
     */
    @NotNull
    public abstract C newTranslatableComponent(@NotNull String key);

    /**
     * Creates a new keybind component with given keybind.
     *
     * @param   keybind
     *          Keybind to show
     * @return  Keybind component with given keybind
     */
    @NotNull
    public abstract C newKeybindComponent(@NotNull String keybind);

    /**
     * Converts given chat modifier to minecraft style and applies it to the component.
     *
     * @param   component
     *          Component to apply style to
     * @param   style
     *          Style to convert and apply
     * @return  Component with applied style (may or may not be the inputted one)
     */
    @NotNull
    public abstract C applyStyle(@NotNull C component, @NotNull ChatModifier style);

    /**
     * Appends children to the given parent component.
     *
     * @param   parent
     *          Parent to append the child to
     * @param   children
     *          Children components to append
     * @return  Component with applied child (may or may not be the inputted one)
     */
    @NotNull
    public abstract C setExtra(@NotNull C parent, @NotNull List<C> children);
}
