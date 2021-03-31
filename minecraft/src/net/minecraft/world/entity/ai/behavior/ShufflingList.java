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

public class ShufflingList<U> {
	protected final List<ShufflingList.WeightedEntry<U>> entries;
	private final Random random = new Random();

	public ShufflingList() {
		this.entries = Lists.<ShufflingList.WeightedEntry<U>>newArrayList();
	}

	private ShufflingList(List<ShufflingList.WeightedEntry<U>> list) {
		this.entries = Lists.<ShufflingList.WeightedEntry<U>>newArrayList(list);
	}

	public static <U> Codec<ShufflingList<U>> codec(Codec<U> codec) {
		return ShufflingList.WeightedEntry.codec(codec).listOf().xmap(ShufflingList::new, shufflingList -> shufflingList.entries);
	}

	public ShufflingList<U> add(U object, int i) {
		this.entries.add(new ShufflingList.WeightedEntry(object, i));
		return this;
	}

	public ShufflingList<U> shuffle() {
		this.entries.forEach(weightedEntry -> weightedEntry.setRandom(this.random.nextFloat()));
		this.entries.sort(Comparator.comparingDouble(object -> ((ShufflingList.WeightedEntry)object).getRandWeight()));
		return this;
	}

	public Stream<U> stream() {
		return this.entries.stream().map(ShufflingList.WeightedEntry::getData);
	}

	public String toString() {
		return "ShufflingList[" + this.entries + "]";
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

		public int getWeight() {
			return this.weight;
		}

		public String toString() {
			return this.weight + ":" + this.data;
		}

		public static <E> Codec<ShufflingList.WeightedEntry<E>> codec(Codec<E> codec) {
			return new Codec<ShufflingList.WeightedEntry<E>>() {
				@Override
				public <T> DataResult<Pair<ShufflingList.WeightedEntry<E>, T>> decode(DynamicOps<T> dynamicOps, T object) {
					Dynamic<T> dynamic = new Dynamic<>(dynamicOps, object);
					return dynamic.get("data")
						.flatMap(codec::parse)
						.map(objectx -> new ShufflingList.WeightedEntry(objectx, dynamic.get("weight").asInt(1)))
						.map(weightedEntry -> Pair.of(weightedEntry, dynamicOps.empty()));
				}

				public <T> DataResult<T> encode(ShufflingList.WeightedEntry<E> weightedEntry, DynamicOps<T> dynamicOps, T object) {
					return dynamicOps.mapBuilder()
						.add("weight", dynamicOps.createInt(weightedEntry.weight))
						.add("data", codec.encodeStart(dynamicOps, weightedEntry.data))
						.build(object);
				}
			};
		}
	}
}
