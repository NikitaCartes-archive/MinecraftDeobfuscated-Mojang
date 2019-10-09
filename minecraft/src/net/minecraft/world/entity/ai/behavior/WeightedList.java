package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

public class WeightedList<U> {
	protected final List<WeightedList<U>.WeightedEntry<? extends U>> entries = Lists.<WeightedList<U>.WeightedEntry<? extends U>>newArrayList();
	private final Random random;

	public WeightedList(Random random) {
		this.random = random;
	}

	public WeightedList() {
		this(new Random());
	}

	public <T> WeightedList(Dynamic<T> dynamic, Function<Dynamic<T>, U> function) {
		this();
		dynamic.asStream().forEach(dynamicx -> dynamicx.get("data").map(dynamic2 -> {
				U object = (U)function.apply(dynamic2);
				int i = dynamicx.get("weight").asInt(1);
				return this.add(object, i);
			}));
	}

	public <T> T serialize(DynamicOps<T> dynamicOps, Function<U, Dynamic<T>> function) {
		return dynamicOps.createList(
			this.streamEntries()
				.map(
					weightedEntry -> dynamicOps.createMap(
							ImmutableMap.<T, T>builder()
								.put(dynamicOps.createString("data"), (T)((Dynamic)function.apply(weightedEntry.getData())).getValue())
								.put(dynamicOps.createString("weight"), dynamicOps.createInt(weightedEntry.getWeight()))
								.build()
						)
				)
		);
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

	public Stream<? extends U> stream() {
		return this.entries.stream().map(WeightedList.WeightedEntry::getData);
	}

	public Stream<WeightedList<U>.WeightedEntry<? extends U>> streamEntries() {
		return this.entries.stream();
	}

	public U getOne(Random random) {
		return (U)this.shuffle(random).stream().findFirst().orElseThrow(RuntimeException::new);
	}

	public String toString() {
		return "WeightedList[" + this.entries + "]";
	}

	public class WeightedEntry<T> {
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
			return "" + this.weight + ":" + this.data;
		}
	}
}
