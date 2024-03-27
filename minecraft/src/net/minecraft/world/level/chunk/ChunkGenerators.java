package net.minecraft.world.level.chunk;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

public class ChunkGenerators {
	public static MapCodec<? extends ChunkGenerator> bootstrap(Registry<MapCodec<? extends ChunkGenerator>> registry) {
		Registry.register(registry, "noise", NoiseBasedChunkGenerator.CODEC);
		Registry.register(registry, "flat", FlatLevelSource.CODEC);
		return Registry.register(registry, "debug", DebugLevelSource.CODEC);
	}
}
