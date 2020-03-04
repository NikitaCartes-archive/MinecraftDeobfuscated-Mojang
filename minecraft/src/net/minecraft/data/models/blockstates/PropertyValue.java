package net.minecraft.data.models.blockstates;

import java.util.stream.Stream;
import net.minecraft.world.level.block.state.properties.Property;

public final class PropertyValue<T extends Comparable<T>> {
	private final Property<T> property;
	private final T value;

	public PropertyValue(Property<T> property, T comparable) {
		if (!property.getPossibleValues().contains(comparable)) {
			throw new IllegalArgumentException("Value " + comparable + " does not belong to property " + property);
		} else {
			this.property = property;
			this.value = comparable;
		}
	}

	public Property<T> getProperty() {
		return this.property;
	}

	public String toString() {
		return this.property.getName() + "=" + this.property.getName(this.value);
	}

	public static <T extends Comparable<T>> Stream<PropertyValue<T>> getAll(Property<T> property) {
		return property.getPossibleValues().stream().map(comparable -> new PropertyValue<>(property, (T)comparable));
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof PropertyValue)) {
			return false;
		} else {
			PropertyValue<?> propertyValue = (PropertyValue<?>)object;
			return this.property == propertyValue.property && this.value.equals(propertyValue.value);
		}
	}

	public int hashCode() {
		int i = this.property.hashCode();
		return 31 * i + this.value.hashCode();
	}
}
