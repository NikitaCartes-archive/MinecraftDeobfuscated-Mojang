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
		int m,
		long n,
		SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration
	) {
		apply(
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
			m
		);
	}

	protected static void apply(
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
		int m = Integer.MIN_VALUE;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int n = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
		BlockState blockState6 = blockState4;
		int o = -1;

		for (int p = k; p >= l; p--) {
			mutableBlockPos.set(i, p, j);
			BlockState blockState7 = chunkAccess.getBlockState(mutableBlockPos);
			if (blockState7.isAir()) {
				o = -1;
				m = Integer.MIN_VALUE;
			} else if (!blockState7.is(blockState.getBlock())) {
				m = Math.max(p, m);
			} else if (o == -1) {
				o = n;
				BlockState blockState8;
				if (p >= m + 2) {
					blockState8 = blockState3;
				} else if (p >= m - 1) {
					blockState6 = blockState4;
					blockState8 = blockState3;
				} else if (p >= m - 4) {
					blockState6 = blockState4;
					blockState8 = blockState4;
				} else if (p >= m - (7 + n)) {
					blockState8 = blockState6;
				} else {
					blockState6 = blockState;
					blockState8 = blockState5;
				}

				chunkAccess.setBlockState(mutableBlockPos, maybeReplaceState(blockState8, chunkAccess, mutableBlockPos, m), false);
			} else if (o > 0) {
				o--;
				chunkAccess.setBlockState(mutableBlockPos, maybeReplaceState(blockState6, chunkAccess, mutableBlockPos, m), false);
				if (o == 0 && blockState6.is(Blocks.SAND) && n > 1) {
					o = random.nextInt(4) + Math.max(0, p - m);
					blockState6 = blockState6.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
				}
			}
		}
	}

	private static BlockState maybeReplaceState(BlockState blockState, ChunkAccess chunkAccess, BlockPos blockPos, int i) {
		if (blockPos.getY() <= i && blockState.is(Blocks.GRASS_BLOCK)) {
			return Blocks.DIRT.defaultBlockState();
		} else if (blockState.is(Blocks.SAND) && isEmptyBelow(chunkAccess, blockPos)) {
			return Blocks.SANDSTONE.defaultBlockState();
		} else if (blockState.is(Blocks.RED_SAND) && isEmptyBelow(chunkAccess, blockPos)) {
			return Blocks.RED_SANDSTONE.defaultBlockState();
		} else {
			return blockState.is(Blocks.GRAVEL) && isEmptyBelow(chunkAccess, blockPos) ? Blocks.STONE.defaultBlockState() : blockState;
		}
	}

	private static boolean isEmptyBelow(ChunkAccess chunkAccess, BlockPos blockPos) {
		BlockState blockState = chunkAccess.getBlockState(blockPos.below());
		return blockState.isAir() || !blockState.getFluidState().isEmpty();
	}
}
