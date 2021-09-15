/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.SimpleRandomSource;
import net.minecraft.world.level.levelgen.material.WorldGenMaterialRule;
import org.jetbrains.annotations.Nullable;

public class DepthBasedRule
implements WorldGenMaterialRule {
    private static final int ALWAYS_REPLACE_BELOW_Y = -8;
    private static final int NEVER_REPLACE_ABOVE_Y = 0;
    private final PositionalRandomFactory randomFactory;
    private final BlockState state;

    public DepthBasedRule(PositionalRandomFactory positionalRandomFactory, BlockState blockState) {
        this.randomFactory = positionalRandomFactory;
        this.state = blockState;
    }

    @Override
    @Nullable
    public BlockState apply(NoiseChunk noiseChunk, int i, int j, int k) {
        if (j < -8) {
            return this.state;
        }
        if (j > 0) {
            return null;
        }
        double d = Mth.map(j, -8.0f, 0.0f, 1.0f, 0.0f);
        SimpleRandomSource randomSource = this.randomFactory.at(i, j, k);
        return (double)randomSource.nextFloat() < d ? this.state : null;
    }
}

