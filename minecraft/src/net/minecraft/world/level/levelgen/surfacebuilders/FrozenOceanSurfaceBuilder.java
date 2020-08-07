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
		long m,
		SurfaceBuilderBaseConfiguration surfaceBuilderBaseConfiguration
	) {
		double e = 0.0;
		double f = 0.0;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		float g = biome.getTemperature(mutableBlockPos.set(i, 63, j));
		double h = Math.min(Math.abs(d), this.icebergNoise.getValue((double)i * 0.1, (double)j * 0.1, false) * 15.0);
		if (h > 1.8) {
			double n = 0.09765625;
			double o = Math.abs(this.icebergRoofNoise.getValue((double)i * 0.09765625, (double)j * 0.09765625, false));
			e = h * h * 1.2;
			double p = Math.ceil(o * 40.0) + 14.0;
			if (e > p) {
				e = p;
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

		int q = i & 15;
		int r = j & 15;
		SurfaceBuilderConfiguration surfaceBuilderConfiguration = biome.getGenerationSettings().getSurfaceBuilderConfig();
		BlockState blockState3 = surfaceBuilderConfiguration.getUnderMaterial();
		BlockState blockState4 = surfaceBuilderConfiguration.getTopMaterial();
		BlockState blockState5 = blockState3;
		BlockState blockState6 = blockState4;
		int s = (int)(d / 3.0 + 3.0 + random.nextDouble() * 0.25);
		int t = -1;
		int u = 0;
		int v = 2 + random.nextInt(4);
		int w = l + 18 + random.nextInt(10);

		for (int x = Math.max(k, (int)e + 1); x >= 0; x--) {
			mutableBlockPos.set(q, x, r);
			if (chunkAccess.getBlockState(mutableBlockPos).isAir() && x < (int)e && random.nextDouble() > 0.01) {
				chunkAccess.setBlockState(mutableBlockPos, PACKED_ICE, false);
			} else if (chunkAccess.getBlockState(mutableBlockPos).getMaterial() == Material.WATER && x > (int)f && x < l && f != 0.0 && random.nextDouble() > 0.15) {
				chunkAccess.setBlockState(mutableBlockPos, PACKED_ICE, false);
			}

			BlockState blockState7 = chunkAccess.getBlockState(mutableBlockPos);
			if (blockState7.isAir()) {
				t = -1;
			} else if (blockState7.is(blockState.getBlock())) {
				if (t == -1) {
					if (s <= 0) {
						blockState6 = AIR;
						blockState5 = blockState;
					} else if (x >= l - 4 && x <= l + 1) {
						blockState6 = blockState4;
						blockState5 = blockState3;
					}

					if (x < l && (blockState6 == null || blockState6.isAir())) {
						if (biome.getTemperature(mutableBlockPos.set(i, x, j)) < 0.15F) {
							blockState6 = ICE;
						} else {
							blockState6 = blockState2;
						}
					}

					t = s;
					if (x >= l - 1) {
						chunkAccess.setBlockState(mutableBlockPos, blockState6, false);
					} else if (x < l - 7 - s) {
						blockState6 = AIR;
						blockState5 = blockState;
						chunkAccess.setBlockState(mutableBlockPos, GRAVEL, false);
					} else {
						chunkAccess.setBlockState(mutableBlockPos, blockState5, false);
					}
				} else if (t > 0) {
					t--;
					chunkAccess.setBlockState(mutableBlockPos, blockState5, false);
					if (t == 0 && blockState5.is(Blocks.SAND) && s > 1) {
						t = random.nextInt(4) + Math.max(0, x - 63);
						blockState5 = blockState5.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
					}
				}
			} else if (blockState7.is(Blocks.PACKED_ICE) && u <= v && x > w) {
				chunkAccess.setBlockState(mutableBlockPos, SNOW_BLOCK, false);
				u++;
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
