package net.minecraft.util;

import java.util.Random;

public class IntRange {
	private final int minInclusive;
	private final int maxInclusive;

	public IntRange(int i, int j) {
		if (j < i) {
			throw new IllegalArgumentException("max must be >= minInclusive! Given minInclusive: " + i + ", Given max: " + j);
		} else {
			this.minInclusive = i;
			this.maxInclusive = j;
		}
	}

	public static IntRange of(int i, int j) {
		return new IntRange(i, j);
	}

	public int randomValue(Random random) {
		return Mth.nextInt(random, this.minInclusive, this.maxInclusive);
	}

	public int getMinInclusive() {
		return this.minInclusive;
	}

	public int getMaxInclusive() {
		return this.maxInclusive;
	}

	public String toString() {
		return "IntRange[" + this.minInclusive + "-" + this.maxInclusive + "]";
	}
}
