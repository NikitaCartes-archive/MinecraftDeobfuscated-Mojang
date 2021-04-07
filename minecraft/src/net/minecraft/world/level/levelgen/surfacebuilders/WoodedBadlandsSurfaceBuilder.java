package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class WoodedBadlandsSurfaceBuilder extends BadlandsSurfaceBuilder {
	private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
	private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
	private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();

	public WoodedBadlandsSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
		super(codec);
	}

	@Override
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
		int o = i & 15;
		int p = j & 15;
		BlockState blockState3 = WHITE_TERRACOTTA;
		SurfaceBuilderConfiguration surfaceBuilderConfiguration = biome.getGenerationSettings().getSurfaceBuilderConfig();
		BlockState blockState4 = surfaceBuilderConfiguration.getUnderMaterial();
		BlockState blockState5 = surfaceBuilderConfiguration.getTopMaterial();
		BlockState blockState6 = blockState4;
		int q = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
		boolean bl = Math.cos(d / 3.0 * Math.PI) > 0.0;
		int r = -1;
		boolean bl2 = false;
		int s = 0;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int t = k; t >= m; t--) {
			if (s < 15) {
				mutableBlockPos.set(o, t, p);
				BlockState blockState7 = chunkAccess.getBlockState(mutableBlockPos);
				if (blockState7.isAir()) {
					r = -1;
				} else if (blockState7.is(blockState.getBlock())) {
					if (r == -1) {
						bl2 = false;
						if (q <= 0) {
							blockState3 = Blocks.AIR.defaultBlockState();
							blockState6 = blockState;
						} else if (t >= l - 4 && t <= l + 1) {
							blockState3 = WHITE_TERRACOTTA;
							blockState6 = blockState4;
						}

						if (t < l && (blockState3 == null || blockState3.isAir())) {
							blockState3 = blockState2;
						}

						r = q + Math.max(0, t - l);
						if (t < l - 1) {
							chunkAccess.setBlockState(mutableBlockPos, blockState6, false);
							if (blockState6 == WHITE_TERRACOTTA) {
								chunkAccess.setBlockState(mutableBlockPos, ORANGE_TERRACOTTA, false);
							}
						} else if (t > 86 + q * 2) {
							if (bl) {
								chunkAccess.setBlockState(mutableBlockPos, Blocks.COARSE_DIRT.defaultBlockState(), false);
							} else {
								chunkAccess.setBlockState(mutableBlockPos, Blocks.GRASS_BLOCK.defaultBlockState(), false);
							}
						} else if (t <= l + 3 + q) {
							chunkAccess.setBlockState(mutableBlockPos, blockState5, false);
							bl2 = true;
						} else {
							BlockState blockState8;
							if (t < 64 || t > 127) {
								blockState8 = ORANGE_TERRACOTTA;
							} else if (bl) {
								blockState8 = TERRACOTTA;
							} else {
								blockState8 = this.getBand(i, t, j);
							}

							chunkAccess.setBlockState(mutableBlockPos, blockState8, false);
						}
					} else if (r > 0) {
						r--;
						if (bl2) {
							chunkAccess.setBlockState(mutableBlockPos, ORANGE_TERRACOTTA, false);
						} else {
							chunkAccess.setBlockState(mutableBlockPos, this.getBand(i, t, j), false);
						}
					}

					s++;
				}
			}
		}
	}
}
