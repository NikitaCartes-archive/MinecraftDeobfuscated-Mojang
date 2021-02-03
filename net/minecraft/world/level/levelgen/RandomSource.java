/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

public interface RandomSource {
    public int nextInt();

    public int nextInt(int var1);

    public double nextDouble();

    default public void consumeCount(int i) {
        for (int j = 0; j < i; ++j) {
            this.nextInt();
        }
    }
}

