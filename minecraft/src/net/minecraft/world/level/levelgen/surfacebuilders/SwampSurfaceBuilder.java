package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class SwampSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	public SwampSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
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
		double e = Biome.BIOME_INFO_NOISE.getValue((double)i * 0.25, (double)j * 0.25, false);
		if (e > 0.0) {
			int o = i & 15;
			int p = j & 15;
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int q = k; q >= m; q--) {
				mutableBlockPos.set(o, q, p);
				if (!chunkAccess.getBlockState(mutableBlockPos).isAir()) {
					if (q == 62 && !chunkAccess.getBlockState(mutableBlockPos).is(blockState2.getBlock())) {
						chunkAccess.setBlockState(mutableBlockPos, blockState2, false);
					}
					break;
				}
			}
		}

		SurfaceBuilder.DEFAULT.apply(random, chunkAccess, biome, i, j, k, d, blockState, blockState2, l, m, n, surfaceBuilderBaseConfiguration);
	}
}
