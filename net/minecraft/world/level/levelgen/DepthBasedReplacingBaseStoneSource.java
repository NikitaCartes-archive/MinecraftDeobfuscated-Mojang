/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.BaseStoneSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class DepthBasedReplacingBaseStoneSource
implements BaseStoneSource {
    private final WorldgenRandom random;
    private final long seed;
    private final BlockState normalBlock;
    private final BlockState replacementBlock;

    public DepthBasedReplacingBaseStoneSource(long l, BlockState blockState, BlockState blockState2) {
        this.random = new WorldgenRandom(l);
        this.seed = l;
        this.normalBlock = blockState;
        this.replacementBlock = blockState2;
    }

    @Override
    public BlockState getBaseStone(int i, int j, int k, NoiseGeneratorSettings noiseGeneratorSettings) {
        if (!noiseGeneratorSettings.isDeepslateEnabled()) {
            return this.normalBlock;
        }
        this.random.setBaseStoneSeed(this.seed, i, j, k);
        double d = Mth.clampedMap(j, -8.0, 0.0, 1.0, 0.0);
        return (double)this.random.nextFloat() < d ? this.replacementBlock : this.normalBlock;
    }
}

