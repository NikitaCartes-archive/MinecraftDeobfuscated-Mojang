package net.minecraft.util.random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public interface WeightedEntry {
	Weight getWeight();

	static <T> WeightedEntry.Wrapper<T> wrap(T object, int i) {
		return new WeightedEntry.Wrapper<>(object, Weight.of(i));
	}

	public static class IntrusiveBase implements WeightedEntry {
		private final Weight weight;

		public IntrusiveBase(int i) {
			this.weight = Weight.of(i);
		}

		public IntrusiveBase(Weight weight) {
			this.weight = weight;
		}

		@Override
		public Weight getWeight() {
			return this.weight;
		}
	}

	public static record Wrapper<T>(T data, Weight weight) implements WeightedEntry {
		@Override
		public Weight getWeight() {
			return this.weight;
		}

		public static <E> Codec<WeightedEntry.Wrapper<E>> codec(Codec<E> codec) {
			return RecordCodecBuilder.create(
				instance -> instance.group(
							codec.fieldOf("data").forGetter(WeightedEntry.Wrapper::data), Weight.CODEC.fieldOf("weight").forGetter(WeightedEntry.Wrapper::weight)
						)
						.apply(instance, WeightedEntry.Wrapper::new)
			);
		}
	}
}
