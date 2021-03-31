package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class DefaultSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	private static final int LOWEST_Y_TO_BUILD_SURFACE_ON = 50;

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
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int m = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
		if (m == 0) {
			boolean bl = false;

			for (int n = k; n >= 50; n--) {
				mutableBlockPos.set(i, n, j);
				BlockState blockState6 = chunkAccess.getBlockState(mutableBlockPos);
				if (blockState6.isAir()) {
					bl = false;
				} else if (blockState6.is(blockState.getBlock())) {
					if (!bl) {
						BlockState blockState7;
						if (n >= l) {
							blockState7 = Blocks.AIR.defaultBlockState();
						} else if (n == l - 1) {
							blockState7 = biome.getTemperature(mutableBlockPos) < 0.15F ? Blocks.ICE.defaultBlockState() : blockState2;
						} else if (n >= l - (7 + m)) {
							blockState7 = blockState;
						} else {
							blockState7 = blockState5;
						}

						chunkAccess.setBlockState(mutableBlockPos, blockState7, false);
					}

					bl = true;
				}
			}
		} else {
			BlockState blockState8 = blockState4;
			int nx = -1;

			for (int o = k; o >= 50; o--) {
				mutableBlockPos.set(i, o, j);
				BlockState blockState7 = chunkAccess.getBlockState(mutableBlockPos);
				if (blockState7.isAir()) {
					nx = -1;
				} else if (blockState7.is(blockState.getBlock())) {
					if (nx == -1) {
						nx = m;
						BlockState blockState9;
						if (o >= l + 2) {
							blockState9 = blockState3;
						} else if (o >= l - 1) {
							blockState8 = blockState4;
							blockState9 = blockState3;
						} else if (o >= l - 4) {
							blockState8 = blockState4;
							blockState9 = blockState4;
						} else if (o >= l - (7 + m)) {
							blockState9 = blockState8;
						} else {
							blockState8 = blockState;
							blockState9 = blockState5;
						}

						chunkAccess.setBlockState(mutableBlockPos, blockState9, false);
					} else if (nx > 0) {
						nx--;
						chunkAccess.setBlockState(mutableBlockPos, blockState8, false);
						if (nx == 0 && blockState8.is(Blocks.SAND) && m > 1) {
							nx = random.nextInt(4) + Math.max(0, o - l);
							blockState8 = blockState8.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
						}
					}
				}
			}
		}
	}
}
