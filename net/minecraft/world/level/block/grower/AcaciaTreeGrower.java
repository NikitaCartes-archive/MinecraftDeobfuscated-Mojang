/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.grower;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.SavannaTreeFeature;
import org.jetbrains.annotations.Nullable;

public class AcaciaTreeGrower
extends AbstractTreeGrower {
    @Override
    @Nullable
    protected AbstractTreeFeature<NoneFeatureConfiguration> getFeature(Random random) {
        return new SavannaTreeFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize), true);
    }
}

