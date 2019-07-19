/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.state;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface StateHolder<C> {
    public static final Logger LOGGER = LogManager.getLogger();

    public <T extends Comparable<T>> T getValue(Property<T> var1);

    public <T extends Comparable<T>, V extends T> C setValue(Property<T> var1, V var2);

    public ImmutableMap<Property<?>, Comparable<?>> getValues();

    public static <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
        return property.getName(comparable);
    }

    public static <S extends StateHolder<S>, T extends Comparable<T>> S setValueHelper(S stateHolder, Property<T> property, String string, String string2, String string3) {
        Optional<T> optional = property.getValue(string3);
        if (optional.isPresent()) {
            return (S)((StateHolder)stateHolder.setValue(property, (Comparable)((Comparable)optional.get())));
        }
        LOGGER.warn("Unable to read property: {} with value: {} for input: {}", (Object)string, (Object)string3, (Object)string2);
        return stateHolder;
    }
}

