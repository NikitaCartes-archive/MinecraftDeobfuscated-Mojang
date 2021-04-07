package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class MountainSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	public MountainSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
		super(codec);
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
		int m,
		long n,
		SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration
	) {
		if (d > 1.0) {
			SurfaceBuilder.DEFAULT.apply(random, chunkAccess, biome, i, j, k, d, blockState, blockState2, l, m, n, SurfaceBuilder.CONFIG_STONE);
		} else {
			SurfaceBuilder.DEFAULT.apply(random, chunkAccess, biome, i, j, k, d, blockState, blockState2, l, m, n, SurfaceBuilder.CONFIG_GRASS);
		}
	}
}
