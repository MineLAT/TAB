package me.neznamy.tab.shared.hook;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.util.ComponentUtil;
import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.ChatModifier;
import me.neznamy.tab.shared.chat.SimpleComponent;
import me.neznamy.tab.shared.chat.StructuredComponent;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.chat.TextColor;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Class for hooking into ViaVersion to get protocol version of players
 * to adapt visuals for best experience respecting limits of individual versions.
 */
public class ViaVersionHook {

    /** Instance of the class */
    @Getter
    private static final ViaVersionHook instance = new ViaVersionHook();

    /** Flag tracking if ViaVersion is installed or not */
    @Getter
    private final boolean installed = ReflectionUtils.classExists("com.viaversion.viaversion.api.Via");

    /**
     * Gets player's network version using ViaVersion API
     *
     * @param   player
     *          Player's UUID
     * @param   playerName
     *          Player's name for debug messages
     * @param   serverVersion
     *          Server version to return if Via is not installed or something went wrong
     * @return  Player's network version
     */
    public int getPlayerVersion(@NotNull UUID player, @NotNull String playerName, int serverVersion) {
        if (!installed) return serverVersion;
        int version;
        try {
            version = Via.getAPI().getPlayerVersion(player);
        } catch (IllegalArgumentException e) {
            // java.lang.IllegalArgumentException: ViaVersion has not loaded the platform yet
            // Most likely another plugin shading Via API, just ignore it
            return serverVersion;
        }
        if (version == -1) {
            // Player got instantly disconnected with a packet error
            return serverVersion;
        }
        TAB.getInstance().debug("ViaVersion returned protocol version " + version + " for " + playerName);
        return version;
    }

    @ApiStatus.Internal
    @SuppressWarnings("unchecked")
    public <T> T getJson(@NotNull TabComponent component) {
        if (component instanceof SimpleComponent) {
            return (T) ComponentUtil.legacyToJson(component.toLegacyText());
        } else if (component instanceof StructuredComponent) {
            return (T) toJson((StructuredComponent) component);
        } else {
            throw new IllegalStateException("Unknown component type: " + component.getClass().getName());
        }
    }

    private Object toJson(@NotNull StructuredComponent component) {
        final JsonObject object = new JsonObject();
        
        object.addProperty("text", component.getText());
        
        final ChatModifier modifier = component.getModifier();
        if (modifier.getColor() != null) {
            final TextColor color = modifier.getColor();
            if (color.isLegacyColorForced()) {
                object.addProperty("color", color.getLegacyColor().name());
            } else {
                object.addProperty("color", "#" + color.getHexCode());
            }
        }
        if (modifier.isBold()) {
            object.addProperty("bold", true);
        }
        object.addProperty("italic", modifier.isItalic());
        if (modifier.isObfuscated()) {
            object.addProperty("obfuscated", true);
        }
        if (modifier.isStrikethrough()) {
            object.addProperty("strikethrough", true);
        }
        if (modifier.isUnderlined()) {
            object.addProperty("underlined", true);
        }
        if (modifier.getFont() != null) {
            object.addProperty("font", modifier.getFont());
        }

        if (!component.getExtra().isEmpty()) {
            final JsonArray extra = new JsonArray();
            for (StructuredComponent sub : component.getExtra()) {
                extra.add((JsonObject) toJson(sub));
            }
            object.add("extra", extra);
        }

        return object;
    }
}
