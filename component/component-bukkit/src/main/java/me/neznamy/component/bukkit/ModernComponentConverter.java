package me.neznamy.component.bukkit;

import lombok.SneakyThrows;
import me.neznamy.component.shared.ChatModifier;
import me.neznamy.component.shared.StructuredComponentConverter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Class for converting TAB components into NMS components for versions 1.19 and up.
 */
public class ModernComponentConverter extends StructuredComponentConverter<Object> {

    private final Class<?> IChatBaseComponent = BukkitReflection.getClass("network.chat.Component", "network.chat.IChatBaseComponent");
    private final Class<?> IChatMutableComponent = BukkitReflection.getClass("network.chat.MutableComponent", "network.chat.IChatMutableComponent");
    private final Class<?> TextColor = BukkitReflection.getClass("network.chat.TextColor", "network.chat.ChatHexColor");
    private final Class<?> ResourceLocation = BukkitReflection.getClass("resources.ResourceLocation", "resources.MinecraftKey");

    private final Method newTextComponent = ReflectionUtils.getMethod(IChatBaseComponent, new String[] {"b", "literal"}, String.class);
    private final Method newTranslatableComponent = ReflectionUtils.getMethod(IChatBaseComponent, new String[] {"c", "translatable"}, String.class);
    private final Method newKeybindComponent = ReflectionUtils.getMethod(IChatBaseComponent, new String[] {"d", "keybind"}, String.class);

    private final Class<?> ChatModifierClass = BukkitReflection.getClass("network.chat.Style", "network.chat.ChatModifier");
    private Constructor<?> newChatModifier;
    private final Field IChatMutableComponent_extra = ReflectionUtils.getOnlyField(IChatMutableComponent, List.class);
    private final Field Component_modifier = ReflectionUtils.getOnlyField(IChatMutableComponent, ChatModifierClass);

    private final Method ChatHexColor_fromRGB = ReflectionUtils.getMethods(TextColor, TextColor, int.class).get(0); // There should only be 1, but some mods add more
    private final Method ResourceLocation_tryParse = ReflectionUtils.getMethod(ResourceLocation, new String[]{"tryParse", "m_135820_", "a"}, String.class);

    private boolean is1_21_4Plus;

    /**
     * Constructs new instance and loads all NMS classes, constructors, and methods.
     *
     * @throws  ReflectiveOperationException
     *          If something failed
     */
    public ModernComponentConverter() throws ReflectiveOperationException {
        Class<?> chatClickable = BukkitReflection.getClass("network.chat.ClickEvent", "network.chat.ChatClickable");
        Class<?> chatHoverable = BukkitReflection.getClass("network.chat.HoverEvent", "network.chat.ChatHoverable");
        try {
            // 1.21.4+
            newChatModifier = ReflectionUtils.setAccessible(ChatModifierClass.getDeclaredConstructor(TextColor, Integer.class, Boolean.class, Boolean.class,
                    Boolean.class, Boolean.class, Boolean.class, chatClickable, chatHoverable, String.class, ResourceLocation));
            is1_21_4Plus = true;
        } catch (ReflectiveOperationException e) {
            // 1.21.3-
            newChatModifier = ReflectionUtils.setAccessible(ChatModifierClass.getDeclaredConstructor(TextColor, Boolean.class, Boolean.class, Boolean.class,
                    Boolean.class, Boolean.class, chatClickable, chatHoverable, String.class, ResourceLocation));
            is1_21_4Plus = false;
        }
    }

    @Override
    @NotNull
    @SneakyThrows
    public Object newTextComponent(@NotNull String text) {
        return newTextComponent.invoke(null, text);
    }

    @Override
    @NotNull
    @SneakyThrows
    public Object newTranslatableComponent(@NotNull String key) {
        return newTranslatableComponent.invoke(null, key);
    }

    @Override
    @NotNull
    @SneakyThrows
    public Object newKeybindComponent(@NotNull String keybind) {
        return newKeybindComponent.invoke(null, keybind);
    }

    @Override
    @SneakyThrows
    @NotNull
    public Object applyStyle(@NotNull Object nmsComponent, @NotNull ChatModifier style) {
        if (is1_21_4Plus) {
            Component_modifier.set(nmsComponent, newChatModifier.newInstance(
                    style.getColor() == null ? null : ChatHexColor_fromRGB.invoke(null, style.getColor().getRgb()),
                    style.getShadowColor(),
                    style.getBold(),
                    style.getItalic(),
                    style.getUnderlined(),
                    style.getStrikethrough(),
                    style.getObfuscated(),
                    null,
                    null,
                    null,
                    style.getFont() == null ? null : ResourceLocation_tryParse.invoke(null, style.getFont())
            ));
        } else {
            Component_modifier.set(nmsComponent, newChatModifier.newInstance(
                    style.getColor() == null ? null : ChatHexColor_fromRGB.invoke(null, style.getColor().getRgb()),
                    style.getBold(),
                    style.getItalic(),
                    style.getUnderlined(),
                    style.getStrikethrough(),
                    style.getObfuscated(),
                    null,
                    null,
                    null,
                    style.getFont() == null ? null : ResourceLocation_tryParse.invoke(null, style.getFont())
            ));
        }
        return nmsComponent;
    }

    @Override
    @SneakyThrows
    @NotNull
    public Object setExtra(@NotNull Object parent, @NotNull List<Object> children) {
        IChatMutableComponent_extra.set(parent, children);
        return parent;
    }
}
