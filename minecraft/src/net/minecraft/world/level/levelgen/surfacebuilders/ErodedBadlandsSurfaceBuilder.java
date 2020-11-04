package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ErodedBadlandsSurfaceBuilder extends BadlandsSurfaceBuilder {
	private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
	private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
	private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();

	public ErodedBadlandsSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
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
		long m,
		SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration
	) {
		double e = 0.0;
		double f = Math.min(Math.abs(d), this.pillarNoise.getValue((double)i * 0.25, (double)j * 0.25, false) * 15.0);
		if (f > 0.0) {
			double g = 0.001953125;
			double h = Math.abs(this.pillarRoofNoise.getValue((double)i * 0.001953125, (double)j * 0.001953125, false));
			e = f * f * 2.5;
			double n = Math.ceil(h * 50.0) + 14.0;
			if (e > n) {
				e = n;
			}

			e += 64.0;
		}

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
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int s = Math.max(k, (int)e + 1); s >= 0; s--) {
			mutableBlockPos.set(o, s, p);
			if (chunkAccess.getBlockState(mutableBlockPos).isAir() && s < (int)e) {
				chunkAccess.setBlockState(mutableBlockPos, blockState, false);
			}

			BlockState blockState7 = chunkAccess.getBlockState(mutableBlockPos);
			if (blockState7.isAir()) {
				r = -1;
			} else if (blockState7.is(blockState.getBlock())) {
				if (r == -1) {
					bl2 = false;
					if (q <= 0) {
						blockState3 = Blocks.AIR.defaultBlockState();
						blockState6 = blockState;
					} else if (s >= l - 4 && s <= l + 1) {
						blockState3 = WHITE_TERRACOTTA;
						blockState6 = blockState4;
					}

					if (s < l && (blockState3 == null || blockState3.isAir())) {
						blockState3 = blockState2;
					}

					r = q + Math.max(0, s - l);
					if (s >= l - 1) {
						if (s > l + 3 + q) {
							BlockState blockState8;
							if (s < 64 || s > 127) {
								blockState8 = ORANGE_TERRACOTTA;
							} else if (bl) {
								blockState8 = TERRACOTTA;
							} else {
								blockState8 = this.getBand(i, s, j);
							}

							chunkAccess.setBlockState(mutableBlockPos, blockState8, false);
						} else {
							chunkAccess.setBlockState(mutableBlockPos, blockState5, false);
							bl2 = true;
						}
					} else {
						chunkAccess.setBlockState(mutableBlockPos, blockState6, false);
						if (blockState6.is(Blocks.WHITE_TERRACOTTA)
							|| blockState6.is(Blocks.ORANGE_TERRACOTTA)
							|| blockState6.is(Blocks.MAGENTA_TERRACOTTA)
							|| blockState6.is(Blocks.LIGHT_BLUE_TERRACOTTA)
							|| blockState6.is(Blocks.YELLOW_TERRACOTTA)
							|| blockState6.is(Blocks.LIME_TERRACOTTA)
							|| blockState6.is(Blocks.PINK_TERRACOTTA)
							|| blockState6.is(Blocks.GRAY_TERRACOTTA)
							|| blockState6.is(Blocks.LIGHT_GRAY_TERRACOTTA)
							|| blockState6.is(Blocks.CYAN_TERRACOTTA)
							|| blockState6.is(Blocks.PURPLE_TERRACOTTA)
							|| blockState6.is(Blocks.BLUE_TERRACOTTA)
							|| blockState6.is(Blocks.BROWN_TERRACOTTA)
							|| blockState6.is(Blocks.GREEN_TERRACOTTA)
							|| blockState6.is(Blocks.RED_TERRACOTTA)
							|| blockState6.is(Blocks.BLACK_TERRACOTTA)) {
							chunkAccess.setBlockState(mutableBlockPos, ORANGE_TERRACOTTA, false);
						}
					}
				} else if (r > 0) {
					r--;
					if (bl2) {
						chunkAccess.setBlockState(mutableBlockPos, ORANGE_TERRACOTTA, false);
					} else {
						chunkAccess.setBlockState(mutableBlockPos, this.getBand(i, s, j), false);
					}
				}
			}
		}
	}
}
