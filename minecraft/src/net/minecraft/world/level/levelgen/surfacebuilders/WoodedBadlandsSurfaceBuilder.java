package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class WoodedBadlandsSurfaceBuilder extends BadlandsSurfaceBuilder {
	private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
	private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
	private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();

	public WoodedBadlandsSurfaceBuilder(
		Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration> function, Function<Random, ? extends SurfaceBuilderBaseConfiguration> function2
	) {
		super(function, function2);
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
		int n = i & 15;
		int o = j & 15;
		BlockState blockState3 = WHITE_TERRACOTTA;
		BlockState blockState4 = biome.getSurfaceBuilderConfig().getUnderMaterial();
		int p = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
		boolean bl = Math.cos(d / 3.0 * Math.PI) > 0.0;
		int q = -1;
		boolean bl2 = false;
		int r = 0;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int s = k; s >= 0; s--) {
			if (r < 15) {
				mutableBlockPos.set(n, s, o);
				BlockState blockState5 = chunkAccess.getBlockState(mutableBlockPos);
				if (blockState5.isAir()) {
					q = -1;
				} else if (blockState5.getBlock() == blockState.getBlock()) {
					if (q == -1) {
						bl2 = false;
						if (p <= 0) {
							blockState3 = Blocks.AIR.defaultBlockState();
							blockState4 = blockState;
						} else if (s >= l - 4 && s <= l + 1) {
							blockState3 = WHITE_TERRACOTTA;
							blockState4 = biome.getSurfaceBuilderConfig().getUnderMaterial();
						}

						if (s < l && (blockState3 == null || blockState3.isAir())) {
							blockState3 = blockState2;
						}

						q = p + Math.max(0, s - l);
						if (s < l - 1) {
							chunkAccess.setBlockState(mutableBlockPos, blockState4, false);
							if (blockState4 == WHITE_TERRACOTTA) {
								chunkAccess.setBlockState(mutableBlockPos, ORANGE_TERRACOTTA, false);
							}
						} else if (s > 86 + p * 2) {
							if (bl) {
								chunkAccess.setBlockState(mutableBlockPos, Blocks.COARSE_DIRT.defaultBlockState(), false);
							} else {
								chunkAccess.setBlockState(mutableBlockPos, Blocks.GRASS_BLOCK.defaultBlockState(), false);
							}
						} else if (s <= l + 3 + p) {
							chunkAccess.setBlockState(mutableBlockPos, biome.getSurfaceBuilderConfig().getTopMaterial(), false);
							bl2 = true;
						} else {
							BlockState blockState6;
							if (s < 64 || s > 127) {
								blockState6 = ORANGE_TERRACOTTA;
							} else if (bl) {
								blockState6 = TERRACOTTA;
							} else {
								blockState6 = this.getBand(i, s, j);
							}

							chunkAccess.setBlockState(mutableBlockPos, blockState6, false);
						}
					} else if (q > 0) {
						q--;
						if (bl2) {
							chunkAccess.setBlockState(mutableBlockPos, ORANGE_TERRACOTTA, false);
						} else {
							chunkAccess.setBlockState(mutableBlockPos, this.getBand(i, s, j), false);
						}
					}

					r++;
				}
			}
		}
	}
}
