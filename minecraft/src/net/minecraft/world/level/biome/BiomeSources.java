package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;

public class BiomeSources {
	public static Codec<? extends BiomeSource> bootstrap(Registry<Codec<? extends BiomeSource>> registry) {
		Registry.register(registry, "fixed", FixedBiomeSource.CODEC);
		Registry.register(registry, "multi_noise", MultiNoiseBiomeSource.CODEC);
		Registry.register(registry, "checkerboard", CheckerboardColumnBiomeSource.CODEC);
		Registry.register(registry, "the_moon", TheMoonBiomeSource.CODEC);
		return Registry.register(registry, "the_end", TheEndBiomeSource.CODEC);
	}
}
