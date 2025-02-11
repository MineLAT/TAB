package me.neznamy.component.sponge7;

import me.neznamy.component.shared.ComponentConverter;
import me.neznamy.component.shared.component.TabComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.text.Text;

/**
 * Component converter implementation for Sponge 7.
 */
public class Sponge7ComponentConverter extends ComponentConverter<Text> {

    @Override
    @NotNull
    public Text convert(@NotNull TabComponent component) {
        return Text.of(component.toLegacyText());
    }
}
