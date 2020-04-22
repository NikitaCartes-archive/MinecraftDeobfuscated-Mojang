package net.minecraft.world.level.biome;

import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;

public final class TheVoidBiome extends Biome {
	public TheVoidBiome() {
		super(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilder.NOPE, SurfaceBuilder.CONFIG_STONE)
				.precipitation(Biome.Precipitation.NONE)
				.biomeCategory(Biome.BiomeCategory.NONE)
				.depth(0.1F)
				.scale(0.2F)
				.temperature(0.5F)
				.downfall(0.5F)
				.specialEffects(
					new BiomeSpecialEffects.Builder()
						.waterColor(4159204)
						.waterFogColor(329011)
						.fogColor(12638463)
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(null)
		);
		this.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Feature.VOID_START_PLATFORM.configured(FeatureConfiguration.NONE));
	}
}
