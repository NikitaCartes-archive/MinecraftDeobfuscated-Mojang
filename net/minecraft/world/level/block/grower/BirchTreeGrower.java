/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.BirchFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;
import org.jetbrains.annotations.Nullable;

public class BirchTreeGrower
extends AbstractTreeGrower {
    @Override
    @Nullable
    protected AbstractTreeFeature<NoneFeatureConfiguration> getFeature(Random random) {
        return new BirchFeature(NoneFeatureConfiguration::deserialize, true, false);
    }
}

