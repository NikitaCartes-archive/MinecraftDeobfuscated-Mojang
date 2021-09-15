/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.SimpleRandomSource;

public class PositionalRandomFactory {
    private final long seed;

    public PositionalRandomFactory(long l) {
        this.seed = l;
    }

    public SimpleRandomSource at(BlockPos blockPos) {
        return this.at(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public SimpleRandomSource at(int i, int j, int k) {
        long l = Mth.getSeed(i, j, k);
        long m = l ^ this.seed;
        return new SimpleRandomSource(m);
    }
}

