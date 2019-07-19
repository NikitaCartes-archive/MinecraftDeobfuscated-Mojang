package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.datafixers.Dynamic;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

public class BadlandsSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
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

	public BadlandsSurfaceBuilder(Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration> function) {
		super(function);
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
						if (s >= l - 1) {
							if (s <= l + 3 + p) {
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
						} else {
							chunkAccess.setBlockState(mutableBlockPos, blockState4, false);
							Block block = blockState4.getBlock();
							if (block == Blocks.WHITE_TERRACOTTA
								|| block == Blocks.ORANGE_TERRACOTTA
								|| block == Blocks.MAGENTA_TERRACOTTA
								|| block == Blocks.LIGHT_BLUE_TERRACOTTA
								|| block == Blocks.YELLOW_TERRACOTTA
								|| block == Blocks.LIME_TERRACOTTA
								|| block == Blocks.PINK_TERRACOTTA
								|| block == Blocks.GRAY_TERRACOTTA
								|| block == Blocks.LIGHT_GRAY_TERRACOTTA
								|| block == Blocks.CYAN_TERRACOTTA
								|| block == Blocks.PURPLE_TERRACOTTA
								|| block == Blocks.BLUE_TERRACOTTA
								|| block == Blocks.BROWN_TERRACOTTA
								|| block == Blocks.GREEN_TERRACOTTA
								|| block == Blocks.RED_TERRACOTTA
								|| block == Blocks.BLACK_TERRACOTTA) {
								chunkAccess.setBlockState(mutableBlockPos, ORANGE_TERRACOTTA, false);
							}
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

	@Override
	public void initNoise(long l) {
		if (this.seed != l || this.clayBands == null) {
			this.generateBands(l);
		}

		if (this.seed != l || this.pillarNoise == null || this.pillarRoofNoise == null) {
			Random random = new WorldgenRandom(l);
			this.pillarNoise = new PerlinSimplexNoise(random, 4);
			this.pillarRoofNoise = new PerlinSimplexNoise(random, 1);
		}

		this.seed = l;
	}

	protected void generateBands(long l) {
		this.clayBands = new BlockState[64];
		Arrays.fill(this.clayBands, TERRACOTTA);
		Random random = new WorldgenRandom(l);
		this.clayBandsOffsetNoise = new PerlinSimplexNoise(random, 1);

		for (int i = 0; i < 64; i++) {
			i += random.nextInt(5) + 1;
			if (i < 64) {
				this.clayBands[i] = ORANGE_TERRACOTTA;
			}
		}

		int ix = random.nextInt(4) + 2;

		for (int j = 0; j < ix; j++) {
			int k = random.nextInt(3) + 1;
			int m = random.nextInt(64);

			for (int n = 0; m + n < 64 && n < k; n++) {
				this.clayBands[m + n] = YELLOW_TERRACOTTA;
			}
		}

		int j = random.nextInt(4) + 2;

		for (int k = 0; k < j; k++) {
			int m = random.nextInt(3) + 2;
			int n = random.nextInt(64);

			for (int o = 0; n + o < 64 && o < m; o++) {
				this.clayBands[n + o] = BROWN_TERRACOTTA;
			}
		}

		int k = random.nextInt(4) + 2;

		for (int m = 0; m < k; m++) {
			int n = random.nextInt(3) + 1;
			int o = random.nextInt(64);

			for (int p = 0; o + p < 64 && p < n; p++) {
				this.clayBands[o + p] = RED_TERRACOTTA;
			}
		}

		int m = random.nextInt(3) + 3;
		int n = 0;

		for (int o = 0; o < m; o++) {
			int p = 1;
			n += random.nextInt(16) + 4;

			for (int q = 0; n + q < 64 && q < 1; q++) {
				this.clayBands[n + q] = WHITE_TERRACOTTA;
				if (n + q > 1 && random.nextBoolean()) {
					this.clayBands[n + q - 1] = LIGHT_GRAY_TERRACOTTA;
				}

				if (n + q < 63 && random.nextBoolean()) {
					this.clayBands[n + q + 1] = LIGHT_GRAY_TERRACOTTA;
				}
			}
		}
	}

	protected BlockState getBand(int i, int j, int k) {
		int l = (int)Math.round(this.clayBandsOffsetNoise.getValue((double)i / 512.0, (double)k / 512.0) * 2.0);
		return this.clayBands[(j + l + 64) % 64];
	}
}
