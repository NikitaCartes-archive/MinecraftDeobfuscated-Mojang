/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class BirchTreeGrower
extends AbstractTreeGrower {
    @Override
    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(Random random, boolean bl) {
        return bl ? TreeFeatures.BIRCH_BEES_005 : TreeFeatures.BIRCH;
    }
}

