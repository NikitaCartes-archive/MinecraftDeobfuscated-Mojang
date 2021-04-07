package net.minecraft.world.level.levelgen.surfacebuilders;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

public class BadlandsSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
	protected static final int MAX_CLAY_DEPTH = 15;
	private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
	private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
	private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();
	private static final BlockState YELLOW_TERRACOTTA = Blocks.YELLOW_TERRACOTTA.defaultBlockState();
	private static final BlockState BROWN_TERRACOTTA = Blocks.BROWN_TERRACOTTA.defaultBlockState();
	private static final BlockState RED_TERRACOTTA = Blocks.RED_TERRACOTTA.defaultBlockState();
	private static final BlockState LIGHT_GRAY_TERRACOTTA = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
	protected BlockState[] clayBands;
	protected long seed;
	protected PerlinSimplexNoise pillarNoise;
	protected PerlinSimplexNoise pillarRoofNoise;
	protected PerlinSimplexNoise clayBandsOffsetNoise;

	public BadlandsSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> codec) {
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
						if (t >= l - 1) {
							if (t <= l + 3 + q) {
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
							chunkAccess.setBlockState(mutableBlockPos, this.getBand(i, t, j), false);
						}
					}

					s++;
				}
			}
		}
	}

	@Override
	public void initNoise(long l) {
		if (this.seed != l || this.clayBands == null) {
			this.generateBands(l);
		}

		if (this.seed != l || this.pillarNoise == null || this.pillarRoofNoise == null) {
			WorldgenRandom worldgenRandom = new WorldgenRandom(l);
			this.pillarNoise = new PerlinSimplexNoise(worldgenRandom, IntStream.rangeClosed(-3, 0));
			this.pillarRoofNoise = new PerlinSimplexNoise(worldgenRandom, ImmutableList.of(0));
		}

		this.seed = l;
	}

	protected void generateBands(long l) {
		this.clayBands = new BlockState[64];
		Arrays.fill(this.clayBands, TERRACOTTA);
		WorldgenRandom worldgenRandom = new WorldgenRandom(l);
		this.clayBandsOffsetNoise = new PerlinSimplexNoise(worldgenRandom, ImmutableList.of(0));

		for (int i = 0; i < 64; i++) {
			i += worldgenRandom.nextInt(5) + 1;
			if (i < 64) {
				this.clayBands[i] = ORANGE_TERRACOTTA;
			}
		}

		int ix = worldgenRandom.nextInt(4) + 2;

		for (int j = 0; j < ix; j++) {
			int k = worldgenRandom.nextInt(3) + 1;
			int m = worldgenRandom.nextInt(64);

			for (int n = 0; m + n < 64 && n < k; n++) {
				this.clayBands[m + n] = YELLOW_TERRACOTTA;
			}
		}

		int j = worldgenRandom.nextInt(4) + 2;

		for (int k = 0; k < j; k++) {
			int m = worldgenRandom.nextInt(3) + 2;
			int n = worldgenRandom.nextInt(64);

			for (int o = 0; n + o < 64 && o < m; o++) {
				this.clayBands[n + o] = BROWN_TERRACOTTA;
			}
		}

		int k = worldgenRandom.nextInt(4) + 2;

		for (int m = 0; m < k; m++) {
			int n = worldgenRandom.nextInt(3) + 1;
			int o = worldgenRandom.nextInt(64);

			for (int p = 0; o + p < 64 && p < n; p++) {
				this.clayBands[o + p] = RED_TERRACOTTA;
			}
		}

		int m = worldgenRandom.nextInt(3) + 3;
		int n = 0;

		for (int o = 0; o < m; o++) {
			int p = 1;
			n += worldgenRandom.nextInt(16) + 4;

			for (int q = 0; n + q < 64 && q < 1; q++) {
				this.clayBands[n + q] = WHITE_TERRACOTTA;
				if (n + q > 1 && worldgenRandom.nextBoolean()) {
					this.clayBands[n + q - 1] = LIGHT_GRAY_TERRACOTTA;
				}

				if (n + q < 63 && worldgenRandom.nextBoolean()) {
					this.clayBands[n + q + 1] = LIGHT_GRAY_TERRACOTTA;
				}
			}
		}
	}

	protected BlockState getBand(int i, int j, int k) {
		int l = (int)Math.round(this.clayBandsOffsetNoise.getValue((double)i / 512.0, (double)k / 512.0, false) * 2.0);
		return this.clayBands[(j + l + 64) % 64];
	}
}
