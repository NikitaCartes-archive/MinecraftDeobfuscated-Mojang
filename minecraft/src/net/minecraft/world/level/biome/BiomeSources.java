package net.minecraft.world.level.biome;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;

public class BiomeSources {
	public static MapCodec<? extends BiomeSource> bootstrap(Registry<MapCodec<? extends BiomeSource>> registry) {
		Registry.register(registry, "fixed", FixedBiomeSource.CODEC);
		Registry.register(registry, "multi_noise", MultiNoiseBiomeSource.CODEC);
		Registry.register(registry, "checkerboard", CheckerboardColumnBiomeSource.CODEC);
		return Registry.register(registry, "the_end", TheEndBiomeSource.CODEC);
	}
}
