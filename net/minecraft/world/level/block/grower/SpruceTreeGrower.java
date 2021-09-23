/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

public class SpruceTreeGrower
extends AbstractMegaTreeGrower {
    @Override
    @Nullable
    protected ConfiguredFeature<?, ?> getConfiguredFeature(Random random, boolean bl) {
        return Features.SPRUCE;
    }

    @Override
    @Nullable
    protected ConfiguredFeature<?, ?> getConfiguredMegaFeature(Random random) {
        return random.nextBoolean() ? Features.MEGA_SPRUCE : Features.MEGA_PINE;
    }
}

