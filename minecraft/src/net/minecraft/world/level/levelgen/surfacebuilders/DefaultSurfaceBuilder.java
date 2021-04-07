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
			l,
			m
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
		int l,
		int m
	) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int n = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
		if (n == 0) {
			boolean bl = false;

			for (int o = k; o >= m; o--) {
				mutableBlockPos.set(i, o, j);
				BlockState blockState6 = chunkAccess.getBlockState(mutableBlockPos);
				if (blockState6.isAir()) {
					bl = false;
				} else if (blockState6.is(blockState.getBlock())) {
					if (!bl) {
						BlockState blockState7;
						if (o >= l) {
							blockState7 = Blocks.AIR.defaultBlockState();
						} else if (o == l - 1) {
							blockState7 = biome.getTemperature(mutableBlockPos) < 0.15F ? Blocks.ICE.defaultBlockState() : blockState2;
						} else if (o >= l - (7 + n)) {
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
			int ox = -1;

			for (int p = k; p >= m; p--) {
				mutableBlockPos.set(i, p, j);
				BlockState blockState7 = chunkAccess.getBlockState(mutableBlockPos);
				if (blockState7.isAir()) {
					ox = -1;
				} else if (blockState7.is(blockState.getBlock())) {
					if (ox == -1) {
						ox = n;
						BlockState blockState9;
						if (p >= l + 2) {
							blockState9 = blockState3;
						} else if (p >= l - 1) {
							blockState8 = blockState4;
							blockState9 = blockState3;
						} else if (p >= l - 4) {
							blockState8 = blockState4;
							blockState9 = blockState4;
						} else if (p >= l - (7 + n)) {
							blockState9 = blockState8;
						} else {
							blockState8 = blockState;
							blockState9 = blockState5;
						}

						chunkAccess.setBlockState(mutableBlockPos, blockState9, false);
					} else if (ox > 0) {
						ox--;
						chunkAccess.setBlockState(mutableBlockPos, blockState8, false);
						if (ox == 0 && blockState8.is(Blocks.SAND) && n > 1) {
							ox = random.nextInt(4) + Math.max(0, p - l);
							blockState8 = blockState8.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
						}
					}
				}
			}
		}
	}
}
