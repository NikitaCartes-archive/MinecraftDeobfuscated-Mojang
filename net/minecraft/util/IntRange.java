/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import java.util.Random;

public class IntRange {
    private final int minInclusive;
    private final int maxInclusive;

    public IntRange(int i, int j) {
        if (j < i) {
            throw new IllegalArgumentException("max must be >= minInclusive! Given minInclusive: " + i + ", Given max: " + j);
        }
        this.minInclusive = i;
        this.maxInclusive = j;
    }

    public static IntRange of(int i, int j) {
        return new IntRange(i, j);
    }

    public int randomValue(Random random) {
        if (this.minInclusive == this.maxInclusive) {
            return this.minInclusive;
        }
        return random.nextInt(this.maxInclusive - this.minInclusive + 1) + this.minInclusive;
    }

    public String toString() {
        return "IntRange[" + this.minInclusive + "-" + this.maxInclusive + "]";
    }
}

