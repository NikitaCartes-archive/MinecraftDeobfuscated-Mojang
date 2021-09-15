package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;

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
		double e = 0.0;
		double f = Math.min(Math.abs(d), this.pillarNoise.getValue((double)i * 0.25, (double)j * 0.25, false) * 15.0);
		if (f > 0.0) {
			double g = 0.001953125;
			double h = Math.abs(this.pillarRoofNoise.getValue((double)i * 0.001953125, (double)j * 0.001953125, false));
			e = f * f * 2.5;
			double o = Math.ceil(h * 50.0) + 14.0;
			if (e > o) {
				e = o;
			}

			e += 64.0;
		}

		BlockState blockState3 = WHITE_TERRACOTTA;
		SurfaceBuilderConfiguration surfaceBuilderConfiguration = biome.getGenerationSettings().getSurfaceBuilderConfig();
		BlockState blockState4 = surfaceBuilderConfiguration.getUnderMaterial();
		BlockState blockState5 = surfaceBuilderConfiguration.getTopMaterial();
		BlockState blockState6 = blockState4;
		int p = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
		boolean bl = Math.cos(d / 3.0 * Math.PI) > 0.0;
		int q = -1;
		boolean bl2 = false;

		for (int r = Math.max(k, (int)e + 1); r >= m; r--) {
			BlockState blockState7 = blockColumn.getBlock(r);
			if (blockState7.is(blockState.getBlock())) {
				break;
			}

			if (blockState7.is(Blocks.WATER)) {
				return;
			}
		}

		for (int r = Math.max(k, (int)e + 1); r >= m; r--) {
			if (blockColumn.getBlock(r).isAir() && r < (int)e) {
				blockColumn.setBlock(r, blockState);
			}

			BlockState blockState7x = blockColumn.getBlock(r);
			if (blockState7x.isAir()) {
				q = -1;
			} else if (blockState7x.is(blockState.getBlock())) {
				if (q == -1) {
					bl2 = false;
					if (p <= 0) {
						blockState3 = Blocks.AIR.defaultBlockState();
						blockState6 = blockState;
					} else if (r >= l - 4 && r <= l + 1) {
						blockState3 = WHITE_TERRACOTTA;
						blockState6 = blockState4;
					}

					if (r < l && (blockState3 == null || blockState3.isAir())) {
						blockState3 = blockState2;
					}

					q = p + Math.max(0, r - l);
					if (r >= l - 1) {
						if (r > l + 10 + p) {
							BlockState blockState8;
							if (r < 64 || r > 159) {
								blockState8 = ORANGE_TERRACOTTA;
							} else if (bl) {
								blockState8 = TERRACOTTA;
							} else {
								blockState8 = this.getBand(i, r, j);
							}

							blockColumn.setBlock(r, blockState8);
						} else {
							blockColumn.setBlock(r, blockState5);
							bl2 = true;
						}
					} else {
						blockColumn.setBlock(r, blockState6);
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
							blockColumn.setBlock(r, ORANGE_TERRACOTTA);
						}
					}
				} else if (q > 0) {
					q--;
					if (bl2) {
						blockColumn.setBlock(r, ORANGE_TERRACOTTA);
					} else {
						blockColumn.setBlock(r, this.getBand(i, r, j));
					}
				}
			}
		}
	}
}
