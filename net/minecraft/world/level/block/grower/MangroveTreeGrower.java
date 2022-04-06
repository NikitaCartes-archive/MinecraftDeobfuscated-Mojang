/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.grower;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

public class MangroveTreeGrower
extends AbstractTreeGrower {
    private final float tallProbability;

    public MangroveTreeGrower(float f) {
        this.tallProbability = f;
    }

    @Override
    @Nullable
    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource randomSource, boolean bl) {
        if (randomSource.nextFloat() < this.tallProbability) {
            return TreeFeatures.TALL_MANGROVE;
        }
        return TreeFeatures.MANGROVE;
    }
}

