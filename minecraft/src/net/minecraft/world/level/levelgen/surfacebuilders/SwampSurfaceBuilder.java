package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;

public class SwampSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	public SwampSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
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
		double e = Biome.BIOME_INFO_NOISE.getValue((double)i * 0.25, (double)j * 0.25, false);
		if (e > 0.0) {
			for (int o = k; o >= m; o--) {
				if (!blockColumn.getBlock(o).isAir()) {
					if (o == 62 && !blockColumn.getBlock(o).is(blockState2.getBlock())) {
						blockColumn.setBlock(o, blockState2);
					}
					break;
				}
			}
		}

		SurfaceBuilder.DEFAULT.apply(random, blockColumn, biome, i, j, k, d, blockState, blockState2, l, m, n, surfaceBuilderBaseConfiguration);
	}
}
