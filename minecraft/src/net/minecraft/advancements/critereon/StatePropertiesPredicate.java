package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;

public record StatePropertiesPredicate(List<StatePropertiesPredicate.PropertyMatcher> properties) {
	private static final Codec<List<StatePropertiesPredicate.PropertyMatcher>> PROPERTIES_CODEC = Codec.unboundedMap(
			Codec.STRING, StatePropertiesPredicate.ValueMatcher.CODEC
		)
		.xmap(
			map -> map.entrySet()
					.stream()
					.map(entry -> new StatePropertiesPredicate.PropertyMatcher((String)entry.getKey(), (StatePropertiesPredicate.ValueMatcher)entry.getValue()))
					.toList(),
			list -> (Map)list.stream().collect(Collectors.toMap(StatePropertiesPredicate.PropertyMatcher::name, StatePropertiesPredicate.PropertyMatcher::valueMatcher))
		);
	public static final Codec<StatePropertiesPredicate> CODEC = PROPERTIES_CODEC.xmap(StatePropertiesPredicate::new, StatePropertiesPredicate::properties);
	public static final StreamCodec<ByteBuf, StatePropertiesPredicate> STREAM_CODEC = StatePropertiesPredicate.PropertyMatcher.STREAM_CODEC
		.apply(ByteBufCodecs.list())
		.map(StatePropertiesPredicate::new, StatePropertiesPredicate::properties);

	public <S extends StateHolder<?, S>> boolean matches(StateDefinition<?, S> stateDefinition, S stateHolder) {
		for (StatePropertiesPredicate.PropertyMatcher propertyMatcher : this.properties) {
			if (!propertyMatcher.match(stateDefinition, stateHolder)) {
				return false;
			}
		}

		return true;
	}

	public boolean matches(BlockState blockState) {
		return this.matches(blockState.getBlock().getStateDefinition(), blockState);
	}

	public boolean matches(FluidState fluidState) {
		return this.matches(fluidState.getType().getStateDefinition(), fluidState);
	}

	public Optional<String> checkState(StateDefinition<?, ?> stateDefinition) {
		for (StatePropertiesPredicate.PropertyMatcher propertyMatcher : this.properties) {
			Optional<String> optional = propertyMatcher.checkState(stateDefinition);
			if (optional.isPresent()) {
				return optional;
			}
		}

		return Optional.empty();
	}

	public static class Builder {
		private final ImmutableList.Builder<StatePropertiesPredicate.PropertyMatcher> matchers = ImmutableList.builder();

		private Builder() {
		}

		public static StatePropertiesPredicate.Builder properties() {
			return new StatePropertiesPredicate.Builder();
		}

		public StatePropertiesPredicate.Builder hasProperty(Property<?> property, String string) {
			this.matchers.add(new StatePropertiesPredicate.PropertyMatcher(property.getName(), new StatePropertiesPredicate.ExactMatcher(string)));
			return this;
		}

		public StatePropertiesPredicate.Builder hasProperty(Property<Integer> property, int i) {
			return this.hasProperty(property, Integer.toString(i));
		}

		public StatePropertiesPredicate.Builder hasProperty(Property<Boolean> property, boolean bl) {
			return this.hasProperty(property, Boolean.toString(bl));
		}

		public <T extends Comparable<T> & StringRepresentable> StatePropertiesPredicate.Builder hasProperty(Property<T> property, T comparable) {
			return this.hasProperty(property, comparable.getSerializedName());
		}

		public Optional<StatePropertiesPredicate> build() {
			return Optional.of(new StatePropertiesPredicate(this.matchers.build()));
		}
	}

	static record ExactMatcher(String value) implements StatePropertiesPredicate.ValueMatcher {
		public static final Codec<StatePropertiesPredicate.ExactMatcher> CODEC = Codec.STRING
			.xmap(StatePropertiesPredicate.ExactMatcher::new, StatePropertiesPredicate.ExactMatcher::value);
		public static final StreamCodec<ByteBuf, StatePropertiesPredicate.ExactMatcher> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
			.map(StatePropertiesPredicate.ExactMatcher::new, StatePropertiesPredicate.ExactMatcher::value);

		@Override
		public <T extends Comparable<T>> boolean match(StateHolder<?, ?> stateHolder, Property<T> property) {
			T comparable = stateHolder.getValue(property);
			Optional<T> optional = property.getValue(this.value);
			return optional.isPresent() && comparable.compareTo((Comparable)optional.get()) == 0;
		}
	}

	static record PropertyMatcher(String name, StatePropertiesPredicate.ValueMatcher valueMatcher) {
		public static final StreamCodec<ByteBuf, StatePropertiesPredicate.PropertyMatcher> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8,
			StatePropertiesPredicate.PropertyMatcher::name,
			StatePropertiesPredicate.ValueMatcher.STREAM_CODEC,
			StatePropertiesPredicate.PropertyMatcher::valueMatcher,
			StatePropertiesPredicate.PropertyMatcher::new
		);

		public <S extends StateHolder<?, S>> boolean match(StateDefinition<?, S> stateDefinition, S stateHolder) {
			Property<?> property = stateDefinition.getProperty(this.name);
			return property != null && this.valueMatcher.match(stateHolder, property);
		}

		public Optional<String> checkState(StateDefinition<?, ?> stateDefinition) {
			Property<?> property = stateDefinition.getProperty(this.name);
			return property != null ? Optional.empty() : Optional.of(this.name);
		}
	}

	static record RangedMatcher(Optional<String> minValue, Optional<String> maxValue) implements StatePropertiesPredicate.ValueMatcher {
		public static final Codec<StatePropertiesPredicate.RangedMatcher> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.STRING.optionalFieldOf("min").forGetter(StatePropertiesPredicate.RangedMatcher::minValue),
						Codec.STRING.optionalFieldOf("max").forGetter(StatePropertiesPredicate.RangedMatcher::maxValue)
					)
					.apply(instance, StatePropertiesPredicate.RangedMatcher::new)
		);
		public static final StreamCodec<ByteBuf, StatePropertiesPredicate.RangedMatcher> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
			StatePropertiesPredicate.RangedMatcher::minValue,
			ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
			StatePropertiesPredicate.RangedMatcher::maxValue,
			StatePropertiesPredicate.RangedMatcher::new
		);

		@Override
		public <T extends Comparable<T>> boolean match(StateHolder<?, ?> stateHolder, Property<T> property) {
			T comparable = stateHolder.getValue(property);
			if (this.minValue.isPresent()) {
				Optional<T> optional = property.getValue((String)this.minValue.get());
				if (optional.isEmpty() || comparable.compareTo((Comparable)optional.get()) < 0) {
					return false;
				}
			}

			if (this.maxValue.isPresent()) {
				Optional<T> optional = property.getValue((String)this.maxValue.get());
				if (optional.isEmpty() || comparable.compareTo((Comparable)optional.get()) > 0) {
					return false;
				}
			}

			return true;
		}
	}

	interface ValueMatcher {
		Codec<StatePropertiesPredicate.ValueMatcher> CODEC = Codec.either(StatePropertiesPredicate.ExactMatcher.CODEC, StatePropertiesPredicate.RangedMatcher.CODEC)
			.xmap(Either::unwrap, valueMatcher -> {
				if (valueMatcher instanceof StatePropertiesPredicate.ExactMatcher exactMatcher) {
					return Either.left(exactMatcher);
				} else if (valueMatcher instanceof StatePropertiesPredicate.RangedMatcher rangedMatcher) {
					return Either.right(rangedMatcher);
				} else {
					throw new UnsupportedOperationException();
				}
			});
		StreamCodec<ByteBuf, StatePropertiesPredicate.ValueMatcher> STREAM_CODEC = ByteBufCodecs.either(
				StatePropertiesPredicate.ExactMatcher.STREAM_CODEC, StatePropertiesPredicate.RangedMatcher.STREAM_CODEC
			)
			.map(Either::unwrap, valueMatcher -> {
				if (valueMatcher instanceof StatePropertiesPredicate.ExactMatcher exactMatcher) {
					return Either.left(exactMatcher);
				} else if (valueMatcher instanceof StatePropertiesPredicate.RangedMatcher rangedMatcher) {
					return Either.right(rangedMatcher);
				} else {
					throw new UnsupportedOperationException();
				}
			});

		<T extends Comparable<T>> boolean match(StateHolder<?, ?> stateHolder, Property<T> property);
	}
}
