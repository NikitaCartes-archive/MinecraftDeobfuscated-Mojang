package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;

public class GravellyMountainSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	public GravellyMountainSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
		super(codec);
	}

	public void apply(
		Random random,
		BlockColumn blockColumn,
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
		if (d < -1.0 || d > 2.0) {
			SurfaceBuilder.DEFAULT.apply(random, blockColumn, biome, i, j, k, d, blockState, blockState2, l, m, n, SurfaceBuilder.CONFIG_GRAVEL);
		} else if (d > 1.0) {
			SurfaceBuilder.DEFAULT.apply(random, blockColumn, biome, i, j, k, d, blockState, blockState2, l, m, n, SurfaceBuilder.CONFIG_STONE);
		} else {
			SurfaceBuilder.DEFAULT.apply(random, blockColumn, biome, i, j, k, d, blockState, blockState2, l, m, n, SurfaceBuilder.CONFIG_GRASS);
		}
	}
}
