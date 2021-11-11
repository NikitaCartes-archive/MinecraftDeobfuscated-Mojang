package net.minecraft.util.random;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.util.ExtraCodecs;

public class SimpleWeightedRandomList<E> extends WeightedRandomList<WeightedEntry.Wrapper<E>> {
	public static <E> Codec<SimpleWeightedRandomList<E>> wrappedCodecAllowingEmpty(Codec<E> codec) {
		return WeightedEntry.Wrapper.codec(codec).listOf().xmap(SimpleWeightedRandomList::new, WeightedRandomList::unwrap);
	}

	public static <E> Codec<SimpleWeightedRandomList<E>> wrappedCodec(Codec<E> codec) {
		return ExtraCodecs.nonEmptyList(WeightedEntry.Wrapper.codec(codec).listOf()).xmap(SimpleWeightedRandomList::new, WeightedRandomList::unwrap);
	}

	SimpleWeightedRandomList(List<? extends WeightedEntry.Wrapper<E>> list) {
		super(list);
	}

	public static <E> SimpleWeightedRandomList.Builder<E> builder() {
		return new SimpleWeightedRandomList.Builder<>();
	}

	public static <E> SimpleWeightedRandomList<E> empty() {
		return new SimpleWeightedRandomList<>(List.of());
	}

	public static <E> SimpleWeightedRandomList<E> single(E object) {
		return new SimpleWeightedRandomList<>(List.of(WeightedEntry.wrap(object, 1)));
	}

	public Optional<E> getRandomValue(Random random) {
		return this.getRandom(random).map(WeightedEntry.Wrapper::getData);
	}

	public static class Builder<E> {
		private final ImmutableList.Builder<WeightedEntry.Wrapper<E>> result = ImmutableList.builder();

		public SimpleWeightedRandomList.Builder<E> add(E object, int i) {
			this.result.add(WeightedEntry.wrap(object, i));
			return this;
		}

		public SimpleWeightedRandomList<E> build() {
			return new SimpleWeightedRandomList<>(this.result.build());
		}
	}
}
