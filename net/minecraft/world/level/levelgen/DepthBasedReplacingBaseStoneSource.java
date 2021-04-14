/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import java.util.function.Supplier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.BaseStoneSource;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class DepthBasedReplacingBaseStoneSource
implements BaseStoneSource {
    private static final int ALWAYS_REPLACE_BELOW_Y = -8;
    private static final int NEVER_REPLACE_ABOVE_Y = 0;
    private final WorldgenRandom random;
    private final long seed;
    private final BlockState normalBlock;
    private final BlockState replacementBlock;
    private final Supplier<NoiseGeneratorSettings> settings;

    public DepthBasedReplacingBaseStoneSource(long l, BlockState blockState, BlockState blockState2, Supplier<NoiseGeneratorSettings> supplier) {
        this.random = new WorldgenRandom(l);
        this.seed = l;
        this.normalBlock = blockState;
        this.replacementBlock = blockState2;
        this.settings = supplier;
    }

    @Override
    public BlockState getBaseStone(int i, int j, int k) {
        if (!this.settings.get().isDeepslateEnabled()) {
            return this.normalBlock;
        }
        if (j < -8) {
            return this.replacementBlock;
        }
        if (j > 0) {
            return this.normalBlock;
        }
        double d = Mth.map(j, -8.0, 0.0, 1.0, 0.0);
        this.random.setBaseStoneSeed(this.seed, i, j, k);
        return (double)this.random.nextFloat() < d ? this.replacementBlock : this.normalBlock;
    }
}

