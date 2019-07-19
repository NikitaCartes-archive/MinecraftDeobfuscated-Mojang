/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.grower;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.DarkOakFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;
import org.jetbrains.annotations.Nullable;

public class DarkOakTreeGrower
extends AbstractMegaTreeGrower {
    @Override
    @Nullable
    protected AbstractTreeFeature<NoneFeatureConfiguration> getFeature(Random random) {
        return null;
    }

    @Override
    @Nullable
    protected AbstractTreeFeature<NoneFeatureConfiguration> getMegaFeature(Random random) {
        return new DarkOakFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize), true);
    }
}

