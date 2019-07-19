package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class WeightedList<U> {
	private final List<WeightedList<U>.WeightedEntry<? extends U>> entries = Lists.<WeightedList<U>.WeightedEntry<? extends U>>newArrayList();
	private final Random random;

	public WeightedList() {
		this(new Random());
	}

	public WeightedList(Random random) {
		this.random = random;
	}

	public void add(U object, int i) {
		this.entries.add(new WeightedList.WeightedEntry(object, i));
	}

	public void shuffle() {
		this.entries.forEach(weightedEntry -> weightedEntry.setRandom(this.random.nextFloat()));
		this.entries.sort(Comparator.comparingDouble(WeightedList.WeightedEntry::getRandWeight));
	}

	public Stream<? extends U> stream() {
		return this.entries.stream().map(WeightedList.WeightedEntry::getData);
	}

	public String toString() {
		return "WeightedList[" + this.entries + "]";
	}

	class WeightedEntry<T> {
		private final T data;
		private final int weight;
		private double randWeight;

		private WeightedEntry(T object, int i) {
			this.weight = i;
			this.data = object;
		}

		public double getRandWeight() {
			return this.randWeight;
		}

		public void setRandom(float f) {
			this.randWeight = -Math.pow((double)f, (double)(1.0F / (float)this.weight));
		}

		public T getData() {
			return this.data;
		}

		public String toString() {
			return "" + this.weight + ":" + this.data;
		}
	}
}
