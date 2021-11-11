/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

public class DarkOakTreeGrower
extends AbstractMegaTreeGrower {
    @Override
    @Nullable
    protected ConfiguredFeature<?, ?> getConfiguredFeature(Random random, boolean bl) {
        return null;
    }

    @Override
    @Nullable
    protected ConfiguredFeature<?, ?> getConfiguredMegaFeature(Random random) {
        return TreeFeatures.DARK_OAK;
    }
}

