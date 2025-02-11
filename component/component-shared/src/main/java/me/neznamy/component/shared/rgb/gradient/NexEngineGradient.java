package me.neznamy.component.shared.rgb.gradient;

import me.neznamy.component.shared.TextColor;
import me.neznamy.component.shared.util.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NexEngineGradient implements GradientPattern {

    //pattern for <gradient:#RRGGBB></gradient:#RRGGBB>
    private final Pattern pattern = Pattern.compile("<gradient:#([A-Fa-f0-9]{6})>(.*?)</gradient:#([A-Fa-f0-9]{6})>");

    @Override
    @NotNull
    public String applyPattern(@NotNull String text, @NotNull TriFunction<TextColor, String, TextColor, String> gradientFunction) {
        if (!text.contains("<grad")) return text;
        String replaced = text;
        Matcher matcher = pattern.matcher(replaced);
        while (matcher.find()) {
            String format = matcher.group();
            TextColor start = new TextColor(matcher.group(1));
            String content = matcher.group(2);
            TextColor end = new TextColor(matcher.group(3));
            replaced = replaced.replace(format, gradientFunction.apply(start, content, end));
        }
        return replaced;
    }
}
