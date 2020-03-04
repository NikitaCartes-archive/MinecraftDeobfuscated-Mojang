package net.minecraft.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class Selector {
	private static final Selector EMPTY = new Selector(ImmutableList.of());
	private static final Comparator<PropertyValue<?>> COMPARE_BY_NAME = Comparator.comparing(propertyValue -> propertyValue.getProperty().getName());
	private final List<PropertyValue<?>> values;

	public Selector extend(PropertyValue<?> propertyValue) {
		return new Selector(ImmutableList.<PropertyValue<?>>builder().addAll(this.values).add(propertyValue).build());
	}

	public Selector extend(Selector selector) {
		return new Selector(ImmutableList.<PropertyValue<?>>builder().addAll(this.values).addAll(selector.values).build());
	}

	private Selector(List<PropertyValue<?>> list) {
		this.values = list;
	}

	public static Selector empty() {
		return EMPTY;
	}

	public static Selector of(PropertyValue<?>... propertyValues) {
		return new Selector(ImmutableList.copyOf(propertyValues));
	}

	public boolean equals(Object object) {
		return this == object || object instanceof Selector && this.values.equals(((Selector)object).values);
	}

	public int hashCode() {
		return this.values.hashCode();
	}

	public String getKey() {
		return (String)this.values.stream().sorted(COMPARE_BY_NAME).map(PropertyValue::toString).collect(Collectors.joining(","));
	}

	public String toString() {
		return this.getKey();
	}
}
