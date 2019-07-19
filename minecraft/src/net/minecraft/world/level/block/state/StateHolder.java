package net.minecraft.world.level.block.state;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface StateHolder<C> {
	Logger LOGGER = LogManager.getLogger();

	<T extends Comparable<T>> T getValue(Property<T> property);

	<T extends Comparable<T>, V extends T> C setValue(Property<T> property, V comparable);

	ImmutableMap<Property<?>, Comparable<?>> getValues();

	static <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
		return property.getName((T)comparable);
	}

	static <S extends StateHolder<S>, T extends Comparable<T>> S setValueHelper(S stateHolder, Property<T> property, String string, String string2, String string3) {
		Optional<T> optional = property.getValue(string3);
		if (optional.isPresent()) {
			return stateHolder.setValue(property, (Comparable)optional.get());
		} else {
			LOGGER.warn("Unable to read property: {} with value: {} for input: {}", string, string3, string2);
			return stateHolder;
		}
	}
}
