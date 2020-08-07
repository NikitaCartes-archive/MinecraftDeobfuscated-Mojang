package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class DefaultSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	public DefaultSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
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
		long m,
		SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration
	) {
		this.apply(
			random,
			chunkAccess,
			biome,
			i,
			j,
			k,
			d,
			blockState,
			blockState2,
			surfaceBuilderBaseConfiguration.getTopMaterial(),
			surfaceBuilderBaseConfiguration.getUnderMaterial(),
			surfaceBuilderBaseConfiguration.getUnderwaterMaterial(),
			l
		);
	}

	protected void apply(
		Random random,
		ChunkAccess chunkAccess,
		Biome biome,
		int i,
		int j,
		int k,
		double d,
		BlockState blockState,
		BlockState blockState2,
		BlockState blockState3,
		BlockState blockState4,
		BlockState blockState5,
		int l
	) {
		BlockState blockState6 = blockState3;
		BlockState blockState7 = blockState4;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int m = -1;
		int n = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
		int o = i & 15;
		int p = j & 15;

		for (int q = k; q >= 0; q--) {
			mutableBlockPos.set(o, q, p);
			BlockState blockState8 = chunkAccess.getBlockState(mutableBlockPos);
			if (blockState8.isAir()) {
				m = -1;
			} else if (blockState8.is(blockState.getBlock())) {
				if (m == -1) {
					if (n <= 0) {
						blockState6 = Blocks.AIR.defaultBlockState();
						blockState7 = blockState;
					} else if (q >= l - 4 && q <= l + 1) {
						blockState6 = blockState3;
						blockState7 = blockState4;
					}

					if (q < l && (blockState6 == null || blockState6.isAir())) {
						if (biome.getTemperature(mutableBlockPos.set(i, q, j)) < 0.15F) {
							blockState6 = Blocks.ICE.defaultBlockState();
						} else {
							blockState6 = blockState2;
						}

						mutableBlockPos.set(o, q, p);
					}

					m = n;
					if (q >= l - 1) {
						chunkAccess.setBlockState(mutableBlockPos, blockState6, false);
					} else if (q < l - 7 - n) {
						blockState6 = Blocks.AIR.defaultBlockState();
						blockState7 = blockState;
						chunkAccess.setBlockState(mutableBlockPos, blockState5, false);
					} else {
						chunkAccess.setBlockState(mutableBlockPos, blockState7, false);
					}
				} else if (m > 0) {
					m--;
					chunkAccess.setBlockState(mutableBlockPos, blockState7, false);
					if (m == 0 && blockState7.is(Blocks.SAND) && n > 1) {
						m = random.nextInt(4) + Math.max(0, q - 63);
						blockState7 = blockState7.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
					}
				}
			}
		}
	}
}
