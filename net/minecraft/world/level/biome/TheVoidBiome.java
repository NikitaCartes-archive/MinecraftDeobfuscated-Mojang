/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;

public final class TheVoidBiome
extends Biome {
    public TheVoidBiome() {
        super(new Biome.BiomeBuilder().surfaceBuilder(SurfaceBuilder.NOPE, SurfaceBuilder.CONFIG_STONE).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.NONE).depth(0.1f).scale(0.2f).temperature(0.5f).downfall(0.5f).waterColor(4159204).waterFogColor(329011).parent(null));
        this.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, TheVoidBiome.makeComposite(Feature.VOID_START_PLATFORM, FeatureConfiguration.NONE, FeatureDecorator.NOPE, DecoratorConfiguration.NONE));
    }
}

