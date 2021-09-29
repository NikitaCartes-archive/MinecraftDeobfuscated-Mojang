/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class BirchTreeGrower
extends AbstractTreeGrower {
    @Override
    protected ConfiguredFeature<?, ?> getConfiguredFeature(Random random, boolean bl) {
        return bl ? Features.BIRCH_BEES_005 : Features.BIRCH;
    }
}

