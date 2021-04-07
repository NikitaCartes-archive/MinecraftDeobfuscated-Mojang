package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.Material;

public class FrozenOceanSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	protected static final BlockState PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
	protected static final BlockState SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
	private static final BlockState ICE = Blocks.ICE.defaultBlockState();
	private PerlinSimplexNoise icebergNoise;
	private PerlinSimplexNoise icebergRoofNoise;
	private long seed;

	public FrozenOceanSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
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
		double e = 0.0;
		double f = 0.0;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		float g = biome.getTemperature(mutableBlockPos.set(i, 63, j));
		double h = Math.min(Math.abs(d), this.icebergNoise.getValue((double)i * 0.1, (double)j * 0.1, false) * 15.0);
		if (h > 1.8) {
			double o = 0.09765625;
			double p = Math.abs(this.icebergRoofNoise.getValue((double)i * 0.09765625, (double)j * 0.09765625, false));
			e = h * h * 1.2;
			double q = Math.ceil(p * 40.0) + 14.0;
			if (e > q) {
				e = q;
			}

			if (g > 0.1F) {
				e -= 2.0;
			}

			if (e > 2.0) {
				f = (double)l - e - 7.0;
				e += (double)l;
			} else {
				e = 0.0;
			}
		}

		int r = i & 15;
		int s = j & 15;
		SurfaceBuilderConfiguration surfaceBuilderConfiguration = biome.getGenerationSettings().getSurfaceBuilderConfig();
		BlockState blockState3 = surfaceBuilderConfiguration.getUnderMaterial();
		BlockState blockState4 = surfaceBuilderConfiguration.getTopMaterial();
		BlockState blockState5 = blockState3;
		BlockState blockState6 = blockState4;
		int t = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
		int u = -1;
		int v = 0;
		int w = 2 + random.nextInt(4);
		int x = l + 18 + random.nextInt(10);

		for (int y = Math.max(k, (int)e + 1); y >= m; y--) {
			mutableBlockPos.set(r, y, s);
			if (chunkAccess.getBlockState(mutableBlockPos).isAir() && y < (int)e && random.nextDouble() > 0.01) {
				chunkAccess.setBlockState(mutableBlockPos, PACKED_ICE, false);
			} else if (chunkAccess.getBlockState(mutableBlockPos).getMaterial() == Material.WATER && y > (int)f && y < l && f != 0.0 && random.nextDouble() > 0.15) {
				chunkAccess.setBlockState(mutableBlockPos, PACKED_ICE, false);
			}

			BlockState blockState7 = chunkAccess.getBlockState(mutableBlockPos);
			if (blockState7.isAir()) {
				u = -1;
			} else if (blockState7.is(blockState.getBlock())) {
				if (u == -1) {
					if (t <= 0) {
						blockState6 = AIR;
						blockState5 = blockState;
					} else if (y >= l - 4 && y <= l + 1) {
						blockState6 = blockState4;
						blockState5 = blockState3;
					}

					if (y < l && (blockState6 == null || blockState6.isAir())) {
						if (biome.getTemperature(mutableBlockPos.set(i, y, j)) < 0.15F) {
							blockState6 = ICE;
						} else {
							blockState6 = blockState2;
						}
					}

					u = t;
					if (y >= l - 1) {
						chunkAccess.setBlockState(mutableBlockPos, blockState6, false);
					} else if (y < l - 7 - t) {
						blockState6 = AIR;
						blockState5 = blockState;
						chunkAccess.setBlockState(mutableBlockPos, GRAVEL, false);
					} else {
						chunkAccess.setBlockState(mutableBlockPos, blockState5, false);
					}
				} else if (u > 0) {
					u--;
					chunkAccess.setBlockState(mutableBlockPos, blockState5, false);
					if (u == 0 && blockState5.is(Blocks.SAND) && t > 1) {
						u = random.nextInt(4) + Math.max(0, y - 63);
						blockState5 = blockState5.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
					}
				}
			} else if (blockState7.is(Blocks.PACKED_ICE) && v <= w && y > x) {
				chunkAccess.setBlockState(mutableBlockPos, SNOW_BLOCK, false);
				v++;
			}
		}
	}

	@Override
	public void initNoise(long l) {
		if (this.seed != l || this.icebergNoise == null || this.icebergRoofNoise == null) {
			WorldgenRandom worldgenRandom = new WorldgenRandom(l);
			this.icebergNoise = new PerlinSimplexNoise(worldgenRandom, IntStream.rangeClosed(-3, 0));
			this.icebergRoofNoise = new PerlinSimplexNoise(worldgenRandom, ImmutableList.of(0));
		}

		this.seed = l;
	}
}
