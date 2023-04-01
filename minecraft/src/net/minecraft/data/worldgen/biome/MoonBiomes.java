package net.minecraft.data.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class MoonBiomes {
	private static Biome baseMoonBiome(BiomeGenerationSettings.Builder builder) {
		MobSpawnSettings.Builder builder2 = new MobSpawnSettings.Builder()
			.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.MOON_COW, 8, 1, 5));
		builder2.creatureGenerationProbability(0.07F);
		return new Biome.BiomeBuilder()
			.hasPrecipitation(false)
			.temperature(-0.5F)
			.downfall(0.5F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(0)
					.waterFogColor(16777215)
					.fogColor(10518688)
					.skyColor(0)
					.foliageColorOverride(16777215)
					.grassColorOverride(6908265)
					.ambientParticle(new AmbientParticleSettings(ParticleTypes.WHITE_ASH, 0.001F))
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder2.build())
			.generationSettings(builder.build())
			.build();
	}

	public static Biome theMoon(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
		builder.addFeature(GenerationStep.Decoration.RAW_GENERATION, CavePlacements.CRATER_MEGA);
		builder.addFeature(GenerationStep.Decoration.RAW_GENERATION, CavePlacements.CRATER_LARGE);
		builder.addFeature(GenerationStep.Decoration.RAW_GENERATION, CavePlacements.CRATER_SMALL);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.LUNAR_BASE);
		return baseMoonBiome(builder);
	}
}
