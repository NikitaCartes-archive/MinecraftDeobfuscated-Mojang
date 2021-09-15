package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;

public class GiantTreeTaigaSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	public GiantTreeTaigaSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
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
		if (d > 1.75) {
			SurfaceBuilder.DEFAULT.apply(random, blockColumn, biome, i, j, k, d, blockState, blockState2, l, m, n, SurfaceBuilder.CONFIG_COARSE_DIRT);
		} else if (d > -0.95) {
			SurfaceBuilder.DEFAULT.apply(random, blockColumn, biome, i, j, k, d, blockState, blockState2, l, m, n, SurfaceBuilder.CONFIG_PODZOL);
		} else {
			SurfaceBuilder.DEFAULT.apply(random, blockColumn, biome, i, j, k, d, blockState, blockState2, l, m, n, SurfaceBuilder.CONFIG_GRASS);
		}
	}
}
