package net.minecraft.data.worldgen;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class WinterDropBiomes {
	public static final ResourceKey<Biome> PALE_GARDEN = createKey("pale_garden");

	public static ResourceKey<Biome> createKey(String string) {
		return ResourceKey.create(Registries.BIOME, ResourceLocation.withDefaultNamespace(string));
	}

	public static void register(BootstrapContext<Biome> bootstrapContext, String string, Biome biome) {
		bootstrapContext.register(createKey(string), biome);
	}

	public static void bootstrap(BootstrapContext<Biome> bootstrapContext) {
		HolderGetter<PlacedFeature> holderGetter = bootstrapContext.lookup(Registries.PLACED_FEATURE);
		HolderGetter<ConfiguredWorldCarver<?>> holderGetter2 = bootstrapContext.lookup(Registries.CONFIGURED_CARVER);
		bootstrapContext.register(PALE_GARDEN, OverworldBiomes.darkForest(holderGetter, holderGetter2, true));
	}
}
