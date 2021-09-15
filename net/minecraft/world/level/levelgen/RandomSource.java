/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.levelgen.PositionalRandomFactory;

public interface RandomSource {
    public RandomSource fork();

    default public PositionalRandomFactory forkPositional() {
        return new PositionalRandomFactory(this.nextLong());
    }

    public void setSeed(long var1);

    public int nextInt();

    public int nextInt(int var1);

    public long nextLong();

    public boolean nextBoolean();

    public float nextFloat();

    public double nextDouble();

    public double nextGaussian();

    default public void consumeCount(int i) {
        for (int j = 0; j < i; ++j) {
            this.nextInt();
        }
    }
}

