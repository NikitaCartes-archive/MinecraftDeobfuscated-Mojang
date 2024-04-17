package net.minecraft.data.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class UpdateOneTwentyOneBiomeData {
	public static void bootstrap(BootstrapContext<Biome> bootstrapContext) {
		HolderGetter<PlacedFeature> holderGetter = bootstrapContext.lookup(Registries.PLACED_FEATURE);
		HolderGetter<ConfiguredWorldCarver<?>> holderGetter2 = bootstrapContext.lookup(Registries.CONFIGURED_CARVER);
		MobSpawnSettings.SpawnerData spawnerData = new MobSpawnSettings.SpawnerData(EntityType.BOGGED, 50, 4, 4);
		bootstrapContext.register(
			Biomes.MANGROVE_SWAMP, OverworldBiomes.mangroveSwamp(holderGetter, holderGetter2, builder -> builder.addSpawn(MobCategory.MONSTER, spawnerData))
		);
		bootstrapContext.register(Biomes.SWAMP, OverworldBiomes.swamp(holderGetter, holderGetter2, builder -> builder.addSpawn(MobCategory.MONSTER, spawnerData)));
	}
}
