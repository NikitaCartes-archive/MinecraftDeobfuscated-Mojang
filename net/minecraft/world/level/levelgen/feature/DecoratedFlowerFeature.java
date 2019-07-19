/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.world.level.levelgen.feature.DecoratedFeature;
import net.minecraft.world.level.levelgen.feature.DecoratedFeatureConfiguration;

public class DecoratedFlowerFeature
extends DecoratedFeature {
    public DecoratedFlowerFeature(Function<Dynamic<?>, ? extends DecoratedFeatureConfiguration> function) {
        super(function);
    }
}

