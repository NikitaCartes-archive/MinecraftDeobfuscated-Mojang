/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

public interface RandomSource {
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

