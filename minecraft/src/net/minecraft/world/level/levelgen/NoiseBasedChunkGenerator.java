package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.level.levelgen.synth.SurfaceNoise;

public final class NoiseBasedChunkGenerator extends ChunkGenerator {
	public static final Codec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BiomeSource.CODEC.fieldOf("biome_source").forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.biomeSource),
					Codec.LONG.fieldOf("seed").stable().forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.seed),
					NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.settings)
				)
				.apply(instance, instance.stable(NoiseBasedChunkGenerator::new))
	);
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private static final BlockState[] EMPTY_COLUMN = new BlockState[0];
	private final int cellHeight;
	private final int cellWidth;
	private final int cellCountX;
	private final int cellCountY;
	private final int cellCountZ;
	private final SurfaceNoise surfaceNoise;
	private final NormalNoise barrierNoise;
	private final NormalNoise waterLevelNoise;
	protected final BlockState defaultBlock;
	protected final BlockState defaultFluid;
	private final long seed;
	protected final Supplier<NoiseGeneratorSettings> settings;
	private final int height;
	private final NoiseSampler sampler;
	private final boolean aquifersEnabled;
	private final BaseStoneSource baseStoneSource;

	public NoiseBasedChunkGenerator(BiomeSource biomeSource, long l, Supplier<NoiseGeneratorSettings> supplier) {
		this(biomeSource, biomeSource, l, supplier);
	}

	private NoiseBasedChunkGenerator(BiomeSource biomeSource, BiomeSource biomeSource2, long l, Supplier<NoiseGeneratorSettings> supplier) {
		super(biomeSource, biomeSource2, ((NoiseGeneratorSettings)supplier.get()).structureSettings(), l);
		this.seed = l;
		NoiseGeneratorSettings noiseGeneratorSettings = (NoiseGeneratorSettings)supplier.get();
		this.settings = supplier;
		NoiseSettings noiseSettings = noiseGeneratorSettings.noiseSettings();
		this.height = noiseSettings.height();
		this.cellHeight = QuartPos.toBlock(noiseSettings.noiseSizeVertical());
		this.cellWidth = QuartPos.toBlock(noiseSettings.noiseSizeHorizontal());
		this.defaultBlock = noiseGeneratorSettings.getDefaultBlock();
		this.defaultFluid = noiseGeneratorSettings.getDefaultFluid();
		this.cellCountX = 16 / this.cellWidth;
		this.cellCountY = noiseSettings.height() / this.cellHeight;
		this.cellCountZ = 16 / this.cellWidth;
		WorldgenRandom worldgenRandom = new WorldgenRandom(l);
		BlendedNoise blendedNoise = new BlendedNoise(worldgenRandom);
		this.surfaceNoise = (SurfaceNoise)(noiseSettings.useSimplexSurfaceNoise()
			? new PerlinSimplexNoise(worldgenRandom, IntStream.rangeClosed(-3, 0))
			: new PerlinNoise(worldgenRandom, IntStream.rangeClosed(-3, 0)));
		worldgenRandom.consumeCount(2620);
		PerlinNoise perlinNoise = new PerlinNoise(worldgenRandom, IntStream.rangeClosed(-15, 0));
		SimplexNoise simplexNoise;
		if (noiseSettings.islandNoiseOverride()) {
			WorldgenRandom worldgenRandom2 = new WorldgenRandom(l);
			worldgenRandom2.consumeCount(17292);
			simplexNoise = new SimplexNoise(worldgenRandom2);
		} else {
			simplexNoise = null;
		}

		this.barrierNoise = NormalNoise.create(new SimpleRandomSource(worldgenRandom.nextLong()), -3, 1.0);
		this.waterLevelNoise = NormalNoise.create(new SimpleRandomSource(worldgenRandom.nextLong()), -3, 1.0, 0.0, 2.0);
		Cavifier cavifier = noiseGeneratorSettings.isNoiseCavesEnabled() ? new Cavifier(worldgenRandom, noiseSettings.minY() / this.cellHeight) : null;
		this.sampler = new NoiseSampler(
			biomeSource, this.cellWidth, this.cellHeight, this.cellCountY, noiseSettings, blendedNoise, simplexNoise, perlinNoise, cavifier
		);
		this.aquifersEnabled = noiseGeneratorSettings.isAquifersEnabled();
		this.baseStoneSource = new DepthBasedReplacingBaseStoneSource(l, this.defaultBlock, Blocks.DEEPSLATE.defaultBlockState(), this.settings);
	}

	@Override
	protected Codec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	@Override
	public ChunkGenerator withSeed(long l) {
		return new NoiseBasedChunkGenerator(this.biomeSource.withSeed(l), l, this.settings);
	}

	public boolean stable(long l, ResourceKey<NoiseGeneratorSettings> resourceKey) {
		return this.seed == l && ((NoiseGeneratorSettings)this.settings.get()).stable(resourceKey);
	}

	private double[] makeAndFillNoiseColumn(int i, int j, int k, int l) {
		double[] ds = new double[l + 1];
		this.fillNoiseColumn(ds, i, j, k, l);
		return ds;
	}

	private void fillNoiseColumn(double[] ds, int i, int j, int k, int l) {
		NoiseSettings noiseSettings = ((NoiseGeneratorSettings)this.settings.get()).noiseSettings();
		this.sampler.fillNoiseColumn(ds, i, j, noiseSettings, this.getSeaLevel(), k, l);
	}

	@Override
	public int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor) {
		int k = Math.max(((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY(), levelHeightAccessor.getMinBuildHeight());
		int l = Math.min(
			((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY() + ((NoiseGeneratorSettings)this.settings.get()).noiseSettings().height(),
			levelHeightAccessor.getMaxBuildHeight()
		);
		int m = Mth.intFloorDiv(k, this.cellHeight);
		int n = Mth.intFloorDiv(l - k, this.cellHeight);
		return n <= 0
			? levelHeightAccessor.getMinBuildHeight()
			: this.iterateNoiseColumn(i, j, null, types.isOpaque(), m, n).orElse(levelHeightAccessor.getMinBuildHeight());
	}

	@Override
	public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor) {
		int k = Math.max(((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY(), levelHeightAccessor.getMinBuildHeight());
		int l = Math.min(
			((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY() + ((NoiseGeneratorSettings)this.settings.get()).noiseSettings().height(),
			levelHeightAccessor.getMaxBuildHeight()
		);
		int m = Mth.intFloorDiv(k, this.cellHeight);
		int n = Mth.intFloorDiv(l - k, this.cellHeight);
		if (n <= 0) {
			return new NoiseColumn(k, EMPTY_COLUMN);
		} else {
			BlockState[] blockStates = new BlockState[n * this.cellHeight];
			this.iterateNoiseColumn(i, j, blockStates, null, m, n);
			return new NoiseColumn(k, blockStates);
		}
	}

	@Override
	public BaseStoneSource getBaseStoneSource() {
		return this.baseStoneSource;
	}

	private OptionalInt iterateNoiseColumn(int i, int j, @Nullable BlockState[] blockStates, @Nullable Predicate<BlockState> predicate, int k, int l) {
		int m = SectionPos.blockToSectionCoord(i);
		int n = SectionPos.blockToSectionCoord(j);
		int o = Math.floorDiv(i, this.cellWidth);
		int p = Math.floorDiv(j, this.cellWidth);
		int q = Math.floorMod(i, this.cellWidth);
		int r = Math.floorMod(j, this.cellWidth);
		double d = (double)q / (double)this.cellWidth;
		double e = (double)r / (double)this.cellWidth;
		double[][] ds = new double[][]{
			this.makeAndFillNoiseColumn(o, p, k, l),
			this.makeAndFillNoiseColumn(o, p + 1, k, l),
			this.makeAndFillNoiseColumn(o + 1, p, k, l),
			this.makeAndFillNoiseColumn(o + 1, p + 1, k, l)
		};
		Aquifer aquifer = this.aquifersEnabled
			? new Aquifer(
				m, n, this.barrierNoise, this.waterLevelNoise, (NoiseGeneratorSettings)this.settings.get(), this.sampler, k * this.cellHeight, l * this.cellHeight
			)
			: null;

		for (int s = l - 1; s >= 0; s--) {
			double f = ds[0][s];
			double g = ds[1][s];
			double h = ds[2][s];
			double t = ds[3][s];
			double u = ds[0][s + 1];
			double v = ds[1][s + 1];
			double w = ds[2][s + 1];
			double x = ds[3][s + 1];

			for (int y = this.cellHeight - 1; y >= 0; y--) {
				double z = (double)y / (double)this.cellHeight;
				double aa = Mth.lerp3(z, d, e, f, u, h, w, g, v, t, x);
				int ab = s * this.cellHeight + y;
				int ac = ab + k * this.cellHeight;
				BlockState blockState = this.updateNoiseAndGenerateBaseState(Beardifier.NO_BEARDS, aquifer, this.baseStoneSource, i, ac, j, aa);
				if (blockStates != null) {
					blockStates[ab] = blockState;
				}

				if (predicate != null && predicate.test(blockState)) {
					return OptionalInt.of(ac + 1);
				}
			}
		}

		return OptionalInt.empty();
	}

	protected BlockState updateNoiseAndGenerateBaseState(
		Beardifier beardifier, @Nullable Aquifer aquifer, BaseStoneSource baseStoneSource, int i, int j, int k, double d
	) {
		double e = Mth.clamp(d / 200.0, -1.0, 1.0);
		e = e / 2.0 - e * e * e / 24.0;
		e += beardifier.beardifyOrBury(i, j, k);
		if (aquifer != null && e < 0.0) {
			aquifer.computeAt(i, j, k);
			e += aquifer.getLastBarrierDensity();
		}

		BlockState blockState;
		if (e > 0.0) {
			blockState = baseStoneSource.getBaseStone(i, j, k);
		} else if (this.aquifersEnabled && Aquifer.isLavaLevel(j - this.getMinY())) {
			blockState = Blocks.LAVA.defaultBlockState();
		} else {
			int l = aquifer == null ? this.getSeaLevel() : aquifer.getLastWaterLevel();
			if (j < l) {
				blockState = this.defaultFluid;
			} else {
				blockState = AIR;
			}
		}

		return blockState;
	}

	@Override
	public void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
		ChunkPos chunkPos = chunkAccess.getPos();
		int i = chunkPos.x;
		int j = chunkPos.z;
		WorldgenRandom worldgenRandom = new WorldgenRandom();
		worldgenRandom.setBaseChunkSeed(i, j);
		ChunkPos chunkPos2 = chunkAccess.getPos();
		int k = chunkPos2.getMinBlockX();
		int l = chunkPos2.getMinBlockZ();
		double d = 0.0625;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int m = 0; m < 16; m++) {
			for (int n = 0; n < 16; n++) {
				int o = k + m;
				int p = l + n;
				int q = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, m, n) + 1;
				double e = this.surfaceNoise.getSurfaceNoiseValue((double)o * 0.0625, (double)p * 0.0625, 0.0625, (double)m * 0.0625) * 15.0;
				int r = ((NoiseGeneratorSettings)this.settings.get()).getMinSurfaceLevel();
				worldGenRegion.getBiome(mutableBlockPos.set(k + m, q, l + n))
					.buildSurfaceAt(worldgenRandom, chunkAccess, o, p, q, e, this.defaultBlock, this.defaultFluid, this.getSeaLevel(), r, worldGenRegion.getSeed());
			}
		}

		this.setBedrock(chunkAccess, worldgenRandom);
	}

	private void setBedrock(ChunkAccess chunkAccess, Random random) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int i = chunkAccess.getPos().getMinBlockX();
		int j = chunkAccess.getPos().getMinBlockZ();
		NoiseGeneratorSettings noiseGeneratorSettings = (NoiseGeneratorSettings)this.settings.get();
		int k = noiseGeneratorSettings.noiseSettings().minY();
		int l = k + noiseGeneratorSettings.getBedrockFloorPosition();
		int m = this.height - 1 + k - noiseGeneratorSettings.getBedrockRoofPosition();
		int n = 5;
		int o = chunkAccess.getMinBuildHeight();
		int p = chunkAccess.getMaxBuildHeight();
		boolean bl = m + 5 - 1 >= o && m < p;
		boolean bl2 = l + 5 - 1 >= o && l < p;
		if (bl || bl2) {
			for (BlockPos blockPos : BlockPos.betweenClosed(i, 0, j, i + 15, 0, j + 15)) {
				if (bl) {
					for (int q = 0; q < 5; q++) {
						if (q <= random.nextInt(5)) {
							chunkAccess.setBlockState(mutableBlockPos.set(blockPos.getX(), m - q, blockPos.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
						}
					}
				}

				if (bl2) {
					for (int qx = 4; qx >= 0; qx--) {
						if (qx <= random.nextInt(5)) {
							chunkAccess.setBlockState(mutableBlockPos.set(blockPos.getX(), l + qx, blockPos.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
						}
					}
				}
			}
		}
	}

	@Override
	public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
		NoiseSettings noiseSettings = ((NoiseGeneratorSettings)this.settings.get()).noiseSettings();
		int i = Math.max(noiseSettings.minY(), chunkAccess.getMinBuildHeight());
		int j = Math.min(noiseSettings.minY() + noiseSettings.height(), chunkAccess.getMaxBuildHeight());
		int k = Mth.intFloorDiv(i, this.cellHeight);
		int l = Mth.intFloorDiv(j - i, this.cellHeight);
		if (l <= 0) {
			return CompletableFuture.completedFuture(chunkAccess);
		} else {
			int m = chunkAccess.getSectionIndex(l * this.cellHeight - 1 + i);
			int n = chunkAccess.getSectionIndex(i);
			Set<LevelChunkSection> set = Sets.<LevelChunkSection>newHashSet();

			for (int o = m; o >= n; o--) {
				LevelChunkSection levelChunkSection = chunkAccess.getOrCreateSection(o);
				levelChunkSection.acquire();
				set.add(levelChunkSection);
			}

			return CompletableFuture.supplyAsync(() -> this.doFill(structureFeatureManager, chunkAccess, k, l), Util.backgroundExecutor())
				.thenApplyAsync(chunkAccessx -> {
					for (LevelChunkSection levelChunkSectionx : set) {
						levelChunkSectionx.release();
					}

					return chunkAccessx;
				}, executor);
		}
	}

	private ChunkAccess doFill(StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, int i, int j) {
		Heightmap heightmap = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap heightmap2 = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
		ChunkPos chunkPos = chunkAccess.getPos();
		int k = chunkPos.x;
		int l = chunkPos.z;
		int m = chunkPos.getMinBlockX();
		int n = chunkPos.getMinBlockZ();
		Beardifier beardifier = new Beardifier(structureFeatureManager, chunkAccess);
		Aquifer aquifer = this.aquifersEnabled
			? new Aquifer(
				k, l, this.barrierNoise, this.waterLevelNoise, (NoiseGeneratorSettings)this.settings.get(), this.sampler, i * this.cellHeight, j * this.cellHeight
			)
			: null;
		NoiseInterpolator noiseInterpolator = new NoiseInterpolator(this.cellCountX, j, this.cellCountZ, k, l, i, this::fillNoiseColumn);
		List<NoiseInterpolator> list = ImmutableList.of(noiseInterpolator);
		list.forEach(NoiseInterpolator::initializeForFirstCellX);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int o = 0; o < this.cellCountX; o++) {
			int p = o;
			list.forEach(noiseInterpolatorx -> noiseInterpolatorx.advanceCellX(p));

			for (int q = 0; q < this.cellCountZ; q++) {
				LevelChunkSection levelChunkSection = chunkAccess.getOrCreateSection(chunkAccess.getSectionsCount() - 1);

				for (int r = j - 1; r >= 0; r--) {
					int s = q;
					int t = r;
					list.forEach(noiseInterpolatorx -> noiseInterpolatorx.selectCellYZ(t, s));

					for (int u = this.cellHeight - 1; u >= 0; u--) {
						int v = (i + r) * this.cellHeight + u;
						int w = v & 15;
						int x = chunkAccess.getSectionIndex(v);
						if (chunkAccess.getSectionIndex(levelChunkSection.bottomBlockY()) != x) {
							levelChunkSection = chunkAccess.getOrCreateSection(x);
						}

						double d = (double)u / (double)this.cellHeight;
						list.forEach(noiseInterpolatorx -> noiseInterpolatorx.updateForY(d));

						for (int y = 0; y < this.cellWidth; y++) {
							int z = m + o * this.cellWidth + y;
							int aa = z & 15;
							double e = (double)y / (double)this.cellWidth;
							list.forEach(noiseInterpolatorx -> noiseInterpolatorx.updateForX(e));

							for (int ab = 0; ab < this.cellWidth; ab++) {
								int ac = n + q * this.cellWidth + ab;
								int ad = ac & 15;
								double f = (double)ab / (double)this.cellWidth;
								double g = noiseInterpolator.calculateValue(f);
								BlockState blockState = this.updateNoiseAndGenerateBaseState(beardifier, aquifer, this.baseStoneSource, z, v, ac, g);
								if (blockState != AIR) {
									if (blockState.getLightEmission() != 0 && chunkAccess instanceof ProtoChunk) {
										mutableBlockPos.set(z, v, ac);
										((ProtoChunk)chunkAccess).addLight(mutableBlockPos);
									}

									levelChunkSection.setBlockState(aa, w, ad, blockState, false);
									heightmap.update(aa, v, ad, blockState);
									heightmap2.update(aa, v, ad, blockState);
									if (aquifer != null && aquifer.shouldScheduleWaterUpdate() && !blockState.getFluidState().isEmpty()) {
										mutableBlockPos.set(z, v, ac);
										chunkAccess.getLiquidTicks().scheduleTick(mutableBlockPos, blockState.getFluidState().getType(), 0);
									}
								}
							}
						}
					}
				}
			}

			list.forEach(NoiseInterpolator::swapSlices);
		}

		return chunkAccess;
	}

	@Override
	public int getGenDepth() {
		return this.height;
	}

	@Override
	public int getSeaLevel() {
		return ((NoiseGeneratorSettings)this.settings.get()).seaLevel();
	}

	@Override
	public int getMinY() {
		return ((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY();
	}

	@Override
	public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(
		Biome biome, StructureFeatureManager structureFeatureManager, MobCategory mobCategory, BlockPos blockPos
	) {
		if (structureFeatureManager.getStructureAt(blockPos, true, StructureFeature.SWAMP_HUT).isValid()) {
			if (mobCategory == MobCategory.MONSTER) {
				return StructureFeature.SWAMP_HUT.getSpecialEnemies();
			}

			if (mobCategory == MobCategory.CREATURE) {
				return StructureFeature.SWAMP_HUT.getSpecialAnimals();
			}
		}

		if (mobCategory == MobCategory.MONSTER) {
			if (structureFeatureManager.getStructureAt(blockPos, false, StructureFeature.PILLAGER_OUTPOST).isValid()) {
				return StructureFeature.PILLAGER_OUTPOST.getSpecialEnemies();
			}

			if (structureFeatureManager.getStructureAt(blockPos, false, StructureFeature.OCEAN_MONUMENT).isValid()) {
				return StructureFeature.OCEAN_MONUMENT.getSpecialEnemies();
			}

			if (structureFeatureManager.getStructureAt(blockPos, true, StructureFeature.NETHER_BRIDGE).isValid()) {
				return StructureFeature.NETHER_BRIDGE.getSpecialEnemies();
			}
		}

		return super.getMobsAt(biome, structureFeatureManager, mobCategory, blockPos);
	}

	@Override
	public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
		if (!((NoiseGeneratorSettings)this.settings.get()).disableMobGeneration()) {
			ChunkPos chunkPos = worldGenRegion.getCenter();
			Biome biome = worldGenRegion.getBiome(chunkPos.getWorldPosition());
			WorldgenRandom worldgenRandom = new WorldgenRandom();
			worldgenRandom.setDecorationSeed(worldGenRegion.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
			NaturalSpawner.spawnMobsForChunkGeneration(worldGenRegion, biome, chunkPos, worldgenRandom);
		}
	}
}
