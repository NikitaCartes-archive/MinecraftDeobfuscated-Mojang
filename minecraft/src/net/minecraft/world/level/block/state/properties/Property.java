package net.minecraft.world.level.block.state.properties;

import java.util.Collection;
import java.util.Optional;

public interface Property<T extends Comparable<T>> {
	String getName();

	Collection<T> getPossibleValues();

	Class<T> getValueClass();

	Optional<T> getValue(String string);

	String getName(T comparable);
}
