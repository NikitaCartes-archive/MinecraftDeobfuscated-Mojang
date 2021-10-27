package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.material.Material;

public class SurfaceSystem {
	private static final int HOW_FAR_BELOW_PRELIMINARY_SURFACE_LEVEL_TO_BUILD_SURFACE = 8;
	private static final int MAX_CLAY_DEPTH = 15;
	private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
	private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
	private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();
	private static final BlockState YELLOW_TERRACOTTA = Blocks.YELLOW_TERRACOTTA.defaultBlockState();
	private static final BlockState BROWN_TERRACOTTA = Blocks.BROWN_TERRACOTTA.defaultBlockState();
	private static final BlockState RED_TERRACOTTA = Blocks.RED_TERRACOTTA.defaultBlockState();
	private static final BlockState LIGHT_GRAY_TERRACOTTA = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
	private static final BlockState PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
	private static final BlockState SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();
	private final NoiseSampler sampler;
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
	private final Registry<NormalNoise.NoiseParameters> noises;
	private final Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise> noiseIntances = new ConcurrentHashMap();
	private final PositionalRandomFactory randomFactory;
	private final NormalNoise surfaceNoise;

	public SurfaceSystem(
		NoiseSampler noiseSampler, Registry<NormalNoise.NoiseParameters> registry, BlockState blockState, int i, long l, WorldgenRandom.Algorithm algorithm
	) {
		this.sampler = noiseSampler;
		this.noises = registry;
		this.defaultBlock = blockState;
		this.seaLevel = i;
		this.randomFactory = algorithm.newInstance(l).forkPositional();
		this.clayBandsOffsetNoise = Noises.instantiate(registry, this.randomFactory, Noises.CLAY_BANDS_OFFSET);
		this.clayBands = generateBands(this.randomFactory.fromHashOf(new ResourceLocation("clay_bands")));
		this.surfaceNoise = Noises.instantiate(registry, this.randomFactory, Noises.SURFACE);
		this.badlandsPillarNoise = Noises.instantiate(registry, this.randomFactory, Noises.BADLANDS_PILLAR);
		this.badlandsPillarRoofNoise = Noises.instantiate(registry, this.randomFactory, Noises.BADLANDS_PILLAR_ROOF);
		this.badlandsSurfaceNoise = Noises.instantiate(registry, this.randomFactory, Noises.BADLANDS_SURFACE);
		this.icebergPillarNoise = Noises.instantiate(registry, this.randomFactory, Noises.ICEBERG_PILLAR);
		this.icebergPillarRoofNoise = Noises.instantiate(registry, this.randomFactory, Noises.ICEBERG_PILLAR_ROOF);
		this.icebergSurfaceNoise = Noises.instantiate(registry, this.randomFactory, Noises.ICEBERG_SURFACE);
	}

	protected NormalNoise getOrCreateNoise(ResourceKey<NormalNoise.NoiseParameters> resourceKey) {
		return (NormalNoise)this.noiseIntances.computeIfAbsent(resourceKey, resourceKey2 -> Noises.instantiate(this.noises, this.randomFactory, resourceKey));
	}

	public void buildSurface(
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
				chunkAccess.setBlockState(mutableBlockPos.setY(i), blockState, false);
			}

			public String toString() {
				return "ChunkBlockColumn " + chunkPos;
			}
		};
		SurfaceRules.Context context = new SurfaceRules.Context(this, worldGenerationContext);
		SurfaceRules.SurfaceRule surfaceRule = (SurfaceRules.SurfaceRule)ruleSource.apply(context);
		BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();

		for (int k = 0; k < 16; k++) {
			for (int l = 0; l < 16; l++) {
				int m = i + k;
				int n = j + l;
				int o = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, k, l) + 1;
				RandomSource randomSource = this.randomFactory.at(m, 0, n);
				double d = this.surfaceNoise.getValue((double)m, 0.0, (double)n);
				mutableBlockPos.setX(m).setZ(n);
				int p = this.sampler.getPreliminarySurfaceLevel(m, n, noiseChunk.terrainInfoInterpolated(m, n));
				int q = p - 8;
				Biome biome = biomeManager.getBiome(mutableBlockPos2.set(m, bl ? 0 : o, n));
				ResourceKey<Biome> resourceKey = (ResourceKey<Biome>)registry.getResourceKey(biome)
					.orElseThrow(() -> new IllegalStateException("Unregistered biome: " + biome));
				if (resourceKey == Biomes.ERODED_BADLANDS) {
					this.erodedBadlandsExtension(q, d, blockColumn, m, n, o, chunkAccess);
				}

				int r = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, k, l) + 1;
				int s = (int)(d * 2.75 + 3.0 + randomSource.nextDouble() * 0.25);
				int t;
				int u;
				if (resourceKey != Biomes.BASALT_DELTAS
					&& resourceKey != Biomes.SOUL_SAND_VALLEY
					&& resourceKey != Biomes.WARPED_FOREST
					&& resourceKey != Biomes.CRIMSON_FOREST
					&& resourceKey != Biomes.NETHER_WASTES) {
					t = r;
					u = q;
				} else {
					t = 127;
					u = 0;
				}

				int v = resourceKey != Biomes.WOODED_BADLANDS && resourceKey != Biomes.BADLANDS ? Integer.MAX_VALUE : 15;
				context.updateXZ(chunkAccess, m, n, s);
				int w = 0;
				int x = 0;
				int y = Integer.MIN_VALUE;
				int z = Integer.MAX_VALUE;

				for (int aa = t; aa >= u && x < v; aa--) {
					BlockState blockState = blockColumn.getBlock(aa);
					if (blockState.isAir()) {
						w = 0;
						y = Integer.MIN_VALUE;
					} else if (!blockState.getFluidState().isEmpty()) {
						if (y == Integer.MIN_VALUE) {
							y = aa + 1;
						}
					} else {
						if (z >= aa) {
							z = DimensionType.WAY_BELOW_MIN_Y;

							for (int ab = aa - 1; ab >= u - 1; ab--) {
								BlockState blockState2 = blockColumn.getBlock(ab);
								if (!this.isStone(blockState2)) {
									z = ab + 1;
									break;
								}
							}
						}

						w++;
						x++;
						int abx = aa - z + 1;
						Biome biome2 = biomeManager.getBiome(mutableBlockPos2.set(m, aa, n));
						ResourceKey<Biome> resourceKey2 = (ResourceKey<Biome>)registry.getResourceKey(biome2)
							.orElseThrow(() -> new IllegalStateException("Unregistered biome: " + biome));
						context.updateY(resourceKey2, biome2, s, w, abx, y, m, aa, n);
						BlockState blockState3 = surfaceRule.tryApply(m, aa, n);
						if (blockState3 != null) {
							blockColumn.setBlock(aa, blockState3);
						}
					}
				}

				if (resourceKey == Biomes.FROZEN_OCEAN || resourceKey == Biomes.DEEP_FROZEN_OCEAN) {
					this.frozenOceanExtension(q, biome, d, blockColumn, mutableBlockPos2, m, n, o);
				}
			}
		}
	}

	private boolean isStone(BlockState blockState) {
		return !blockState.isAir() && blockState.getFluidState().isEmpty();
	}

	@Deprecated
	public Optional<BlockState> topMaterial(
		SurfaceRules.RuleSource ruleSource,
		CarvingContext carvingContext,
		Biome biome,
		ResourceKey<Biome> resourceKey,
		ChunkAccess chunkAccess,
		BlockPos blockPos,
		boolean bl
	) {
		SurfaceRules.Context context = new SurfaceRules.Context(this, carvingContext);
		SurfaceRules.SurfaceRule surfaceRule = (SurfaceRules.SurfaceRule)ruleSource.apply(context);
		RandomSource randomSource = this.randomFactory.at(blockPos.getX(), 0, blockPos.getZ());
		double d = this.surfaceNoise.getValue((double)blockPos.getX(), 0.0, (double)blockPos.getZ());
		int i = (int)(d * 2.75 + 3.0 + randomSource.nextDouble() * 0.25);
		context.updateXZ(chunkAccess, blockPos.getX(), blockPos.getZ(), i);
		context.updateY(resourceKey, biome, i, 1, 1, bl ? blockPos.getY() + 1 : Integer.MIN_VALUE, blockPos.getX(), blockPos.getY(), blockPos.getZ());
		BlockState blockState = surfaceRule.tryApply(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		return Optional.ofNullable(blockState);
	}

	private void erodedBadlandsExtension(int i, double d, BlockColumn blockColumn, int j, int k, int l, LevelHeightAccessor levelHeightAccessor) {
		double e = 0.2;
		double f = Math.min(
			Math.abs(this.badlandsSurfaceNoise.getValue((double)j, 0.0, (double)k) * 8.25),
			this.badlandsPillarNoise.getValue((double)j * 0.2, 0.0, (double)k * 0.2) * 15.0
		);
		if (!(f <= 0.0)) {
			double g = 0.75;
			double h = 1.5;
			double m = Math.abs(this.badlandsPillarRoofNoise.getValue((double)j * 0.75, 0.0, (double)k * 0.75) * 1.5);
			double n = 64.0 + Math.min(f * f * 2.5, Math.ceil(m * 50.0) + 24.0);
			int o = Mth.floor(n);
			if (l <= o) {
				for (int p = o; p >= levelHeightAccessor.getMinBuildHeight(); p--) {
					BlockState blockState = blockColumn.getBlock(p);
					if (blockState.is(this.defaultBlock.getBlock())) {
						break;
					}

					if (blockState.is(Blocks.WATER)) {
						return;
					}
				}

				for (int p = o; p >= levelHeightAccessor.getMinBuildHeight() && blockColumn.getBlock(p).isAir(); p--) {
					blockColumn.setBlock(p, this.defaultBlock);
				}
			}
		}
	}

	private void frozenOceanExtension(int i, Biome biome, double d, BlockColumn blockColumn, BlockPos.MutableBlockPos mutableBlockPos, int j, int k, int l) {
		float f = biome.getTemperature(mutableBlockPos.set(j, 63, k));
		double e = 1.28;
		double g = Math.min(
			Math.abs(this.icebergSurfaceNoise.getValue((double)j, 0.0, (double)k) * 8.25),
			this.icebergPillarNoise.getValue((double)j * 1.28, 0.0, (double)k * 1.28) * 15.0
		);
		if (!(g <= 1.8)) {
			double h = 1.17;
			double m = 1.5;
			double n = Math.abs(this.icebergPillarRoofNoise.getValue((double)j * 1.17, 0.0, (double)k * 1.17) * 1.5);
			double o = Math.min(g * g * 1.2, Math.ceil(n * 40.0) + 14.0);
			if (f > 0.1F) {
				o -= 2.0;
			}

			double p;
			if (o > 2.0) {
				o += (double)this.seaLevel;
				p = (double)this.seaLevel - o - 7.0;
			} else {
				o = 0.0;
				p = 0.0;
			}

			double q = o;
			RandomSource randomSource = this.randomFactory.at(j, 0, k);
			int r = 2 + randomSource.nextInt(4);
			int s = this.seaLevel + 18 + randomSource.nextInt(10);
			int t = 0;

			for (int u = Math.max(l, (int)o + 1); u >= i; u--) {
				if (blockColumn.getBlock(u).isAir() && u < (int)q && randomSource.nextDouble() > 0.01
					|| blockColumn.getBlock(u).getMaterial() == Material.WATER && u > (int)p && u < this.seaLevel && p != 0.0 && randomSource.nextDouble() > 0.15) {
					if (t <= r && u > s) {
						blockColumn.setBlock(u, SNOW_BLOCK);
						t++;
					} else {
						blockColumn.setBlock(u, PACKED_ICE);
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
