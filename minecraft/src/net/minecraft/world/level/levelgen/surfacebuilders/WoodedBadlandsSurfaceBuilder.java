package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;

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
		BlockState blockState3 = WHITE_TERRACOTTA;
		SurfaceBuilderConfiguration surfaceBuilderConfiguration = biome.getGenerationSettings().getSurfaceBuilderConfig();
		BlockState blockState4 = surfaceBuilderConfiguration.getUnderMaterial();
		BlockState blockState5 = surfaceBuilderConfiguration.getTopMaterial();
		BlockState blockState6 = blockState4;
		int o = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
		boolean bl = Math.cos(d / 3.0 * Math.PI) > 0.0;
		int p = -1;
		boolean bl2 = false;
		int q = 0;

		for (int r = k; r >= m; r--) {
			if (q < 15) {
				BlockState blockState7 = blockColumn.getBlock(r);
				if (blockState7.isAir()) {
					p = -1;
				} else if (blockState7.is(blockState.getBlock())) {
					if (p == -1) {
						bl2 = false;
						if (o <= 0) {
							blockState3 = Blocks.AIR.defaultBlockState();
							blockState6 = blockState;
						} else if (r >= l - 4 && r <= l + 1) {
							blockState3 = WHITE_TERRACOTTA;
							blockState6 = blockState4;
						}

						if (r < l && (blockState3 == null || blockState3.isAir())) {
							blockState3 = blockState2;
						}

						p = o + Math.max(0, r - l);
						if (r < l - 1) {
							blockColumn.setBlock(r, blockState6);
							if (blockState6 == WHITE_TERRACOTTA) {
								blockColumn.setBlock(r, ORANGE_TERRACOTTA);
							}
						} else if (r > 96 + o * 2) {
							if (bl) {
								blockColumn.setBlock(r, Blocks.COARSE_DIRT.defaultBlockState());
							} else {
								blockColumn.setBlock(r, Blocks.GRASS_BLOCK.defaultBlockState());
							}
						} else if (r <= l + 10 + o) {
							blockColumn.setBlock(r, blockState5);
							bl2 = true;
						} else {
							BlockState blockState8;
							if (r < 64 || r > 159) {
								blockState8 = ORANGE_TERRACOTTA;
							} else if (bl) {
								blockState8 = TERRACOTTA;
							} else {
								blockState8 = this.getBand(i, r, j);
							}

							blockColumn.setBlock(r, blockState8);
						}
					} else if (p > 0) {
						p--;
						if (bl2) {
							blockColumn.setBlock(r, ORANGE_TERRACOTTA);
						} else {
							blockColumn.setBlock(r, this.getBand(i, r, j));
						}
					}

					q++;
				}
			}
		}
	}
}
