package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class WeightedList<U> {
	protected final List<WeightedList.WeightedEntry<U>> entries;
	private final Random random = new Random();

	public WeightedList() {
		this(Lists.<WeightedList.WeightedEntry<U>>newArrayList());
	}

	private WeightedList(List<WeightedList.WeightedEntry<U>> list) {
		this.entries = Lists.<WeightedList.WeightedEntry<U>>newArrayList(list);
	}

	public static <U> Codec<WeightedList<U>> codec(Codec<U> codec) {
		return WeightedList.WeightedEntry.codec(codec).listOf().xmap(WeightedList::new, weightedList -> weightedList.entries);
	}

	public WeightedList<U> add(U object, int i) {
		this.entries.add(new WeightedList.WeightedEntry(object, i));
		return this;
	}

	public WeightedList<U> shuffle() {
		return this.shuffle(this.random);
	}

	public WeightedList<U> shuffle(Random random) {
		this.entries.forEach(weightedEntry -> weightedEntry.setRandom(random.nextFloat()));
		this.entries.sort(Comparator.comparingDouble(object -> ((WeightedList.WeightedEntry)object).getRandWeight()));
		return this;
	}

	public boolean isEmpty() {
		return this.entries.isEmpty();
	}

	public Stream<U> stream() {
		return this.entries.stream().map(WeightedList.WeightedEntry::getData);
	}

	public U getOne(Random random) {
		return (U)this.shuffle(random).stream().findFirst().orElseThrow(RuntimeException::new);
	}

	public String toString() {
		return "WeightedList[" + this.entries + "]";
	}

	public static class WeightedEntry<T> {
		private final T data;
		private final int weight;
		private double randWeight;

		private WeightedEntry(T object, int i) {
			this.weight = i;
			this.data = object;
		}

		private double getRandWeight() {
			return this.randWeight;
		}

		private void setRandom(float f) {
			this.randWeight = -Math.pow((double)f, (double)(1.0F / (float)this.weight));
		}

		public T getData() {
			return this.data;
		}

		public String toString() {
			return "" + this.weight + ":" + this.data;
		}

		public static <E> Codec<WeightedList.WeightedEntry<E>> codec(Codec<E> codec) {
			return new Codec<WeightedList.WeightedEntry<E>>() {
				@Override
				public <T> DataResult<Pair<WeightedList.WeightedEntry<E>, T>> decode(DynamicOps<T> dynamicOps, T object) {
					Dynamic<T> dynamic = new Dynamic<>(dynamicOps, object);
					return dynamic.get("data")
						.flatMap(codec::parse)
						.map(objectx -> new WeightedList.WeightedEntry(objectx, dynamic.get("weight").asInt(1)))
						.map(weightedEntry -> Pair.of(weightedEntry, dynamicOps.empty()));
				}

				public <T> DataResult<T> encode(WeightedList.WeightedEntry<E> weightedEntry, DynamicOps<T> dynamicOps, T object) {
					return dynamicOps.mapBuilder()
						.add("weight", dynamicOps.createInt(weightedEntry.weight))
						.add("data", codec.encodeStart(dynamicOps, weightedEntry.data))
						.build(object);
				}
			};
		}
	}
}
