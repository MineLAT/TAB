package me.neznamy.component.shared;

import lombok.Getter;
import lombok.Setter;
import me.neznamy.component.shared.component.TabComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Basic class for converting TAB components to minecraft components.
 *
 * @param   <C>
 *          Platform's component type
 */
public abstract class ComponentConverter<C> {

    /** Instance of this class */
    @Getter
    @Setter
    private static ComponentConverter<?> instance;

    /**
     * Converts TAB component to NMS component.
     *
     * @param   component
     *          Component to convert
     * @return  Converted component
     */
    @NotNull
    public abstract C convert(@NotNull TabComponent component);
}
