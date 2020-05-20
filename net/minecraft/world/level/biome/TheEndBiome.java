/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;

public final class TheEndBiome
extends Biome {
    public TheEndBiome() {
        super(new Biome.BiomeBuilder().surfaceBuilder(SurfaceBuilder.DEFAULT, SurfaceBuilder.CONFIG_THEEND).precipitation(Biome.Precipitation.NONE).biomeCategory(Biome.BiomeCategory.THEEND).depth(0.1f).scale(0.2f).temperature(0.5f).downfall(0.5f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(0xA080A0).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).parent(null));
        this.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Feature.END_SPIKE.configured(new SpikeConfiguration(false, (List<SpikeFeature.EndSpike>)ImmutableList.of(), null)));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 10, 4, 4));
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public int getSkyColor() {
        return 0;
    }
}

