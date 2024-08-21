package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class SurfaceSystem {
	private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
	private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
	private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();
	private static final BlockState YELLOW_TERRACOTTA = Blocks.YELLOW_TERRACOTTA.defaultBlockState();
	private static final BlockState BROWN_TERRACOTTA = Blocks.BROWN_TERRACOTTA.defaultBlockState();
	private static final BlockState RED_TERRACOTTA = Blocks.RED_TERRACOTTA.defaultBlockState();
	private static final BlockState LIGHT_GRAY_TERRACOTTA = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
	private static final BlockState PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
	private static final BlockState SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();
	private final BlockState defaultBlock;
	private final int seaLevel;
	private final BlockState[] clayBands;
	private final NormalNoise clayBandsOffsetNoise;
	private final NormalNoise badlandsPillarNoise;
	private final NormalNoise badlandsPillarRoofNoise;
	private final NormalNoise badlandsSurfaceNoise;
	private final NormalNoise icebergPillarNoise;
	private final NormalNoise icebergPillarRoofNoise;
	private final NormalNoise icebergSurfaceNoise;
	private final PositionalRandomFactory noiseRandom;
	private final NormalNoise surfaceNoise;
	private final NormalNoise surfaceSecondaryNoise;

	public SurfaceSystem(RandomState randomState, BlockState blockState, int i, PositionalRandomFactory positionalRandomFactory) {
		this.defaultBlock = blockState;
		this.seaLevel = i;
		this.noiseRandom = positionalRandomFactory;
		this.clayBandsOffsetNoise = randomState.getOrCreateNoise(Noises.CLAY_BANDS_OFFSET);
		this.clayBands = generateBands(positionalRandomFactory.fromHashOf(ResourceLocation.withDefaultNamespace("clay_bands")));
		this.surfaceNoise = randomState.getOrCreateNoise(Noises.SURFACE);
		this.surfaceSecondaryNoise = randomState.getOrCreateNoise(Noises.SURFACE_SECONDARY);
		this.badlandsPillarNoise = randomState.getOrCreateNoise(Noises.BADLANDS_PILLAR);
		this.badlandsPillarRoofNoise = randomState.getOrCreateNoise(Noises.BADLANDS_PILLAR_ROOF);
		this.badlandsSurfaceNoise = randomState.getOrCreateNoise(Noises.BADLANDS_SURFACE);
		this.icebergPillarNoise = randomState.getOrCreateNoise(Noises.ICEBERG_PILLAR);
		this.icebergPillarRoofNoise = randomState.getOrCreateNoise(Noises.ICEBERG_PILLAR_ROOF);
		this.icebergSurfaceNoise = randomState.getOrCreateNoise(Noises.ICEBERG_SURFACE);
	}

	public void buildSurface(
		RandomState randomState,
		BiomeManager biomeManager,
		Registry<Biome> registry,
		boolean bl,
		WorldGenerationContext worldGenerationContext,
		ChunkAccess chunkAccess,
		NoiseChunk noiseChunk,
		SurfaceRules.RuleSource ruleSource
	) {
		final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		final ChunkPos chunkPos = chunkAccess.getPos();
		int i = chunkPos.getMinBlockX();
		int j = chunkPos.getMinBlockZ();
		BlockColumn blockColumn = new BlockColumn() {
			@Override
			public BlockState getBlock(int i) {
				return chunkAccess.getBlockState(mutableBlockPos.setY(i));
			}

			@Override
			public void setBlock(int i, BlockState blockState) {
				LevelHeightAccessor levelHeightAccessor = chunkAccess.getHeightAccessorForGeneration();
				if (levelHeightAccessor.isInsideBuildHeight(i)) {
					chunkAccess.setBlockState(mutableBlockPos.setY(i), blockState, false);
					if (!blockState.getFluidState().isEmpty()) {
						chunkAccess.markPosForPostprocessing(mutableBlockPos);
					}
				}
			}

			public String toString() {
				return "ChunkBlockColumn " + chunkPos;
			}
		};
		SurfaceRules.Context context = new SurfaceRules.Context(this, randomState, chunkAccess, noiseChunk, biomeManager::getBiome, registry, worldGenerationContext);
		SurfaceRules.SurfaceRule surfaceRule = (SurfaceRules.SurfaceRule)ruleSource.apply(context);
		BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();

		for (int k = 0; k < 16; k++) {
			for (int l = 0; l < 16; l++) {
				int m = i + k;
				int n = j + l;
				int o = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, k, l) + 1;
				mutableBlockPos.setX(m).setZ(n);
				Holder<Biome> holder = biomeManager.getBiome(mutableBlockPos2.set(m, bl ? 0 : o, n));
				if (holder.is(Biomes.ERODED_BADLANDS)) {
					this.erodedBadlandsExtension(blockColumn, m, n, o, chunkAccess);
				}

				int p = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, k, l) + 1;
				context.updateXZ(m, n);
				int q = 0;
				int r = Integer.MIN_VALUE;
				int s = Integer.MAX_VALUE;
				int t = chunkAccess.getMinY();

				for (int u = p; u >= t; u--) {
					BlockState blockState = blockColumn.getBlock(u);
					if (blockState.isAir()) {
						q = 0;
						r = Integer.MIN_VALUE;
					} else if (!blockState.getFluidState().isEmpty()) {
						if (r == Integer.MIN_VALUE) {
							r = u + 1;
						}
					} else {
						if (s >= u) {
							s = DimensionType.WAY_BELOW_MIN_Y;

							for (int v = u - 1; v >= t - 1; v--) {
								BlockState blockState2 = blockColumn.getBlock(v);
								if (!this.isStone(blockState2)) {
									s = v + 1;
									break;
								}
							}
						}

						q++;
						int vx = u - s + 1;
						context.updateY(q, vx, r, m, u, n);
						if (blockState == this.defaultBlock) {
							BlockState blockState2 = surfaceRule.tryApply(m, u, n);
							if (blockState2 != null) {
								blockColumn.setBlock(u, blockState2);
							}
						}
					}
				}

				if (holder.is(Biomes.FROZEN_OCEAN) || holder.is(Biomes.DEEP_FROZEN_OCEAN)) {
					this.frozenOceanExtension(context.getMinSurfaceLevel(), holder.value(), blockColumn, mutableBlockPos2, m, n, o);
				}
			}
		}
	}

	protected int getSurfaceDepth(int i, int j) {
		double d = this.surfaceNoise.getValue((double)i, 0.0, (double)j);
		return (int)(d * 2.75 + 3.0 + this.noiseRandom.at(i, 0, j).nextDouble() * 0.25);
	}

	protected double getSurfaceSecondary(int i, int j) {
		return this.surfaceSecondaryNoise.getValue((double)i, 0.0, (double)j);
	}

	private boolean isStone(BlockState blockState) {
		return !blockState.isAir() && blockState.getFluidState().isEmpty();
	}

	public int getSeaLevel() {
		return this.seaLevel;
	}

	@Deprecated
	public Optional<BlockState> topMaterial(
		SurfaceRules.RuleSource ruleSource,
		CarvingContext carvingContext,
		Function<BlockPos, Holder<Biome>> function,
		ChunkAccess chunkAccess,
		NoiseChunk noiseChunk,
		BlockPos blockPos,
		boolean bl
	) {
		SurfaceRules.Context context = new SurfaceRules.Context(
			this, carvingContext.randomState(), chunkAccess, noiseChunk, function, carvingContext.registryAccess().registryOrThrow(Registries.BIOME), carvingContext
		);
		SurfaceRules.SurfaceRule surfaceRule = (SurfaceRules.SurfaceRule)ruleSource.apply(context);
		int i = blockPos.getX();
		int j = blockPos.getY();
		int k = blockPos.getZ();
		context.updateXZ(i, k);
		context.updateY(1, 1, bl ? j + 1 : Integer.MIN_VALUE, i, j, k);
		BlockState blockState = surfaceRule.tryApply(i, j, k);
		return Optional.ofNullable(blockState);
	}

	private void erodedBadlandsExtension(BlockColumn blockColumn, int i, int j, int k, LevelHeightAccessor levelHeightAccessor) {
		double d = 0.2;
		double e = Math.min(
			Math.abs(this.badlandsSurfaceNoise.getValue((double)i, 0.0, (double)j) * 8.25),
			this.badlandsPillarNoise.getValue((double)i * 0.2, 0.0, (double)j * 0.2) * 15.0
		);
		if (!(e <= 0.0)) {
			double f = 0.75;
			double g = 1.5;
			double h = Math.abs(this.badlandsPillarRoofNoise.getValue((double)i * 0.75, 0.0, (double)j * 0.75) * 1.5);
			double l = 64.0 + Math.min(e * e * 2.5, Math.ceil(h * 50.0) + 24.0);
			int m = Mth.floor(l);
			if (k <= m) {
				for (int n = m; n >= levelHeightAccessor.getMinY(); n--) {
					BlockState blockState = blockColumn.getBlock(n);
					if (blockState.is(this.defaultBlock.getBlock())) {
						break;
					}

					if (blockState.is(Blocks.WATER)) {
						return;
					}
				}

				for (int n = m; n >= levelHeightAccessor.getMinY() && blockColumn.getBlock(n).isAir(); n--) {
					blockColumn.setBlock(n, this.defaultBlock);
				}
			}
		}
	}

	private void frozenOceanExtension(int i, Biome biome, BlockColumn blockColumn, BlockPos.MutableBlockPos mutableBlockPos, int j, int k, int l) {
		double d = 1.28;
		double e = Math.min(
			Math.abs(this.icebergSurfaceNoise.getValue((double)j, 0.0, (double)k) * 8.25),
			this.icebergPillarNoise.getValue((double)j * 1.28, 0.0, (double)k * 1.28) * 15.0
		);
		if (!(e <= 1.8)) {
			double f = 1.17;
			double g = 1.5;
			double h = Math.abs(this.icebergPillarRoofNoise.getValue((double)j * 1.17, 0.0, (double)k * 1.17) * 1.5);
			double m = Math.min(e * e * 1.2, Math.ceil(h * 40.0) + 14.0);
			if (biome.shouldMeltFrozenOceanIcebergSlightly(mutableBlockPos.set(j, this.seaLevel, k), this.seaLevel)) {
				m -= 2.0;
			}

			double n;
			if (m > 2.0) {
				n = (double)this.seaLevel - m - 7.0;
				m += (double)this.seaLevel;
			} else {
				m = 0.0;
				n = 0.0;
			}

			double o = m;
			RandomSource randomSource = this.noiseRandom.at(j, 0, k);
			int p = 2 + randomSource.nextInt(4);
			int q = this.seaLevel + 18 + randomSource.nextInt(10);
			int r = 0;

			for (int s = Math.max(l, (int)m + 1); s >= i; s--) {
				if (blockColumn.getBlock(s).isAir() && s < (int)o && randomSource.nextDouble() > 0.01
					|| blockColumn.getBlock(s).is(Blocks.WATER) && s > (int)n && s < this.seaLevel && n != 0.0 && randomSource.nextDouble() > 0.15) {
					if (r <= p && s > q) {
						blockColumn.setBlock(s, SNOW_BLOCK);
						r++;
					} else {
						blockColumn.setBlock(s, PACKED_ICE);
					}
				}
			}
		}
	}

	private static BlockState[] generateBands(RandomSource randomSource) {
		BlockState[] blockStates = new BlockState[192];
		Arrays.fill(blockStates, TERRACOTTA);

		for (int i = 0; i < blockStates.length; i++) {
			i += randomSource.nextInt(5) + 1;
			if (i < blockStates.length) {
				blockStates[i] = ORANGE_TERRACOTTA;
			}
		}

		makeBands(randomSource, blockStates, 1, YELLOW_TERRACOTTA);
		makeBands(randomSource, blockStates, 2, BROWN_TERRACOTTA);
		makeBands(randomSource, blockStates, 1, RED_TERRACOTTA);
		int ix = randomSource.nextIntBetweenInclusive(9, 15);
		int j = 0;

		for (int k = 0; j < ix && k < blockStates.length; k += randomSource.nextInt(16) + 4) {
			blockStates[k] = WHITE_TERRACOTTA;
			if (k - 1 > 0 && randomSource.nextBoolean()) {
				blockStates[k - 1] = LIGHT_GRAY_TERRACOTTA;
			}

			if (k + 1 < blockStates.length && randomSource.nextBoolean()) {
				blockStates[k + 1] = LIGHT_GRAY_TERRACOTTA;
			}

			j++;
		}

		return blockStates;
	}

	private static void makeBands(RandomSource randomSource, BlockState[] blockStates, int i, BlockState blockState) {
		int j = randomSource.nextIntBetweenInclusive(6, 15);

		for (int k = 0; k < j; k++) {
			int l = i + randomSource.nextInt(3);
			int m = randomSource.nextInt(blockStates.length);

			for (int n = 0; m + n < blockStates.length && n < l; n++) {
				blockStates[m + n] = blockState;
			}
		}
	}

	protected BlockState getBand(int i, int j, int k) {
		int l = (int)Math.round(this.clayBandsOffsetNoise.getValue((double)i, 0.0, (double)k) * 4.0);
		return this.clayBands[(j + l + this.clayBands.length) % this.clayBands.length];
	}
}
