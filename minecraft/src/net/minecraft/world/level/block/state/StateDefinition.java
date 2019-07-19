package net.minecraft.world.level.block.state;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.MapFiller;
import net.minecraft.world.level.block.state.properties.Property;

public class StateDefinition<O, S extends StateHolder<S>> {
	private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
	private final O owner;
	private final ImmutableSortedMap<String, Property<?>> propertiesByName;
	private final ImmutableList<S> states;

	protected <A extends AbstractStateHolder<O, S>> StateDefinition(O object, StateDefinition.Factory<O, S, A> factory, Map<String, Property<?>> map) {
		this.owner = object;
		this.propertiesByName = ImmutableSortedMap.copyOf(map);
		Map<Map<Property<?>, Comparable<?>>, A> map2 = Maps.<Map<Property<?>, Comparable<?>>, A>newLinkedHashMap();
		List<A> list = Lists.<A>newArrayList();
		Stream<List<Comparable<?>>> stream = Stream.of(Collections.emptyList());

		for (Property<?> property : this.propertiesByName.values()) {
			stream = stream.flatMap(listx -> property.getPossibleValues().stream().map(comparable -> {
					List<Comparable<?>> list2 = Lists.<Comparable<?>>newArrayList(listx);
					list2.add(comparable);
					return list2;
				}));
		}

		stream.forEach(list2 -> {
			Map<Property<?>, Comparable<?>> map2x = MapFiller.linkedHashMapFrom(this.propertiesByName.values(), list2);
			A abstractStateHolderx = factory.create(object, ImmutableMap.copyOf(map2x));
			map2.put(map2x, abstractStateHolderx);
			list.add(abstractStateHolderx);
		});

		for (A abstractStateHolder : list) {
			abstractStateHolder.populateNeighbours(map2);
		}

		this.states = ImmutableList.copyOf(list);
	}

	public ImmutableList<S> getPossibleStates() {
		return this.states;
	}

	public S any() {
		return (S)this.states.get(0);
	}

	public O getOwner() {
		return this.owner;
	}

	public Collection<Property<?>> getProperties() {
		return this.propertiesByName.values();
	}

	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("block", this.owner)
			.add("properties", this.propertiesByName.values().stream().map(Property::getName).collect(Collectors.toList()))
			.toString();
	}

	@Nullable
	public Property<?> getProperty(String string) {
		return this.propertiesByName.get(string);
	}

	public static class Builder<O, S extends StateHolder<S>> {
		private final O owner;
		private final Map<String, Property<?>> properties = Maps.<String, Property<?>>newHashMap();

		public Builder(O object) {
			this.owner = object;
		}

		public StateDefinition.Builder<O, S> add(Property<?>... propertys) {
			for (Property<?> property : propertys) {
				this.validateProperty(property);
				this.properties.put(property.getName(), property);
			}

			return this;
		}

		private <T extends Comparable<T>> void validateProperty(Property<T> property) {
			String string = property.getName();
			if (!StateDefinition.NAME_PATTERN.matcher(string).matches()) {
				throw new IllegalArgumentException(this.owner + " has invalidly named property: " + string);
			} else {
				Collection<T> collection = property.getPossibleValues();
				if (collection.size() <= 1) {
					throw new IllegalArgumentException(this.owner + " attempted use property " + string + " with <= 1 possible values");
				} else {
					for (T comparable : collection) {
						String string2 = property.getName(comparable);
						if (!StateDefinition.NAME_PATTERN.matcher(string2).matches()) {
							throw new IllegalArgumentException(this.owner + " has property: " + string + " with invalidly named value: " + string2);
						}
					}

					if (this.properties.containsKey(string)) {
						throw new IllegalArgumentException(this.owner + " has duplicate property: " + string);
					}
				}
			}
		}

		public <A extends AbstractStateHolder<O, S>> StateDefinition<O, S> create(StateDefinition.Factory<O, S, A> factory) {
			return new StateDefinition<>(this.owner, factory, this.properties);
		}
	}

	public interface Factory<O, S extends StateHolder<S>, A extends AbstractStateHolder<O, S>> {
		A create(O object, ImmutableMap<Property<?>, Comparable<?>> immutableMap);
	}
}
