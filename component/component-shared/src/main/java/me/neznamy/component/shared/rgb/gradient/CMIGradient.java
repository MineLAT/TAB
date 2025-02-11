package me.neznamy.component.shared.rgb.gradient;

import me.neznamy.component.shared.TextColor;
import me.neznamy.component.shared.util.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gradient formatter using CMI's syntax.
 */
public class CMIGradient extends CommonGradient {

    //pattern for {#RRGGBB<>}
    private final Pattern shortcutPattern = Pattern.compile("\\{#[0-9a-fA-F]{6}<>}");

    /**
     * Constructs new instance.
     */
    public CMIGradient() {
        super(Pattern.compile("\\{#[0-9a-fA-F]{6}>}[^{]*\\{#[0-9a-fA-F]{6}<}"), "{#", 2, 10, 8);
    }
    
    @Override
    @NotNull
    public String applyPattern(@NotNull String text, @NotNull TriFunction<TextColor, String, TextColor, String> gradientFunction) {
        String replaced = text;
        if (replaced.contains("<>}")) {
            Matcher m = shortcutPattern.matcher(replaced);
            while (m.find()) {
                String format = m.group();
                String code = format.substring(2, 8);
                replaced = replaced.replace(format, "{#" + code + "<}{#" + code + ">}");
            }
        }
        return super.applyPattern(replaced, gradientFunction);
    }
}