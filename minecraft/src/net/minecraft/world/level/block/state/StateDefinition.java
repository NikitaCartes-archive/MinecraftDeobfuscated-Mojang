package net.minecraft.world.level.block.state;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.properties.Property;

public class StateDefinition<O, S extends StateHolder<O, S>> {
	static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
	private final O owner;
	private final ImmutableSortedMap<String, Property<?>> propertiesByName;
	private final ImmutableList<S> states;

	protected StateDefinition(Function<O, S> function, O object, StateDefinition.Factory<O, S> factory, Map<String, Property<?>> map) {
		this.owner = object;
		this.propertiesByName = ImmutableSortedMap.copyOf(map);
		Supplier<S> supplier = () -> (StateHolder)function.apply(object);
		MapCodec<S> mapCodec = MapCodec.of(Encoder.empty(), Decoder.unit(supplier));

		for (Entry<String, Property<?>> entry : this.propertiesByName.entrySet()) {
			mapCodec = appendPropertyCodec(mapCodec, supplier, (String)entry.getKey(), (Property)entry.getValue());
		}

		MapCodec<S> mapCodec2 = mapCodec;
		Map<Map<Property<?>, Comparable<?>>, S> map2 = Maps.<Map<Property<?>, Comparable<?>>, S>newLinkedHashMap();
		List<S> list = Lists.<S>newArrayList();
		Stream<List<Pair<Property<?>, Comparable<?>>>> stream = Stream.of(Collections.emptyList());

		for (Property<?> property : this.propertiesByName.values()) {
			stream = stream.flatMap(listx -> property.getPossibleValues().stream().map(comparable -> {
					List<Pair<Property<?>, Comparable<?>>> list2 = Lists.<Pair<Property<?>, Comparable<?>>>newArrayList(listx);
					list2.add(Pair.of(property, comparable));
					return list2;
				}));
		}

		stream.forEach(
			list2 -> {
				ImmutableMap<Property<?>, Comparable<?>> immutableMap = (ImmutableMap<Property<?>, Comparable<?>>)list2.stream()
					.collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
				S stateHolderx = factory.create(object, immutableMap, mapCodec2);
				map2.put(immutableMap, stateHolderx);
				list.add(stateHolderx);
			}
		);

		for (S stateHolder : list) {
			stateHolder.populateNeighbours(map2);
		}

		this.states = ImmutableList.copyOf(list);
	}

	private static <S extends StateHolder<?, S>, T extends Comparable<T>> MapCodec<S> appendPropertyCodec(
		MapCodec<S> mapCodec, Supplier<S> supplier, String string, Property<T> property
	) {
		return Codec.mapPair(mapCodec, property.valueCodec().fieldOf(string).orElseGet((Consumer<String>)(stringx -> {
			}), () -> property.value((StateHolder<?, ?>)supplier.get())))
			.xmap(
				pair -> (StateHolder)((StateHolder)pair.getFirst()).setValue(property, ((Property.Value)pair.getSecond()).value()),
				stateHolder -> Pair.of(stateHolder, property.value(stateHolder))
			);
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

	public static class Builder<O, S extends StateHolder<O, S>> {
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

		public StateDefinition<O, S> create(Function<O, S> function, StateDefinition.Factory<O, S> factory) {
			return new StateDefinition<>(function, this.owner, factory, this.properties);
		}
	}

	public interface Factory<O, S> {
		S create(O object, ImmutableMap<Property<?>, Comparable<?>> immutableMap, MapCodec<S> mapCodec);
	}
}
