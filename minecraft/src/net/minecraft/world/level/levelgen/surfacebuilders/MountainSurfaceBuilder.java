package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class MountainSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	public MountainSurfaceBuilder(
		Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration> function, Function<Random, ? extends SurfaceBuilderBaseConfiguration> function2
	) {
		super(function, function2);
	}

	public void apply(
		Random random,
		ChunkAccess chunkAccess,
		Biome biome,
		int i,
		int j,
		int k,
		double d,
		BlockState blockState,
		BlockState blockState2,
		int l,
		long m,
		SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration
	) {
		if (d > 1.0) {
			SurfaceBuilder.DEFAULT.apply(random, chunkAccess, biome, i, j, k, d, blockState, blockState2, l, m, SurfaceBuilder.CONFIG_STONE);
		} else {
			SurfaceBuilder.DEFAULT.apply(random, chunkAccess, biome, i, j, k, d, blockState, blockState2, l, m, SurfaceBuilder.CONFIG_GRASS);
		}
	}
}
