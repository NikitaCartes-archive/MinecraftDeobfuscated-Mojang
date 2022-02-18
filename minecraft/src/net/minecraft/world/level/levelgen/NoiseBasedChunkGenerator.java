package net.minecraft.world.level.levelgen;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class NoiseBasedChunkGenerator extends ChunkGenerator {
	public static final Codec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create(
		instance -> commonCodec(instance)
				.and(
					instance.group(
						RegistryOps.retrieveRegistry(Registry.NOISE_REGISTRY).forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.noises),
						BiomeSource.CODEC.fieldOf("biome_source").forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.biomeSource),
						Codec.LONG.fieldOf("seed").stable().forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.seed),
						NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.settings)
					)
				)
				.apply(instance, instance.stable(NoiseBasedChunkGenerator::new))
	);
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private static final BlockState[] EMPTY_COLUMN = new BlockState[0];
	protected final BlockState defaultBlock;
	private final Registry<NormalNoise.NoiseParameters> noises;
	private final long seed;
	protected final Holder<NoiseGeneratorSettings> settings;
	private final NoiseRouter router;
	private final Climate.Sampler sampler;
	private final SurfaceSystem surfaceSystem;
	private final Aquifer.FluidPicker globalFluidPicker;

	public NoiseBasedChunkGenerator(
		Registry<StructureSet> registry, Registry<NormalNoise.NoiseParameters> registry2, BiomeSource biomeSource, long l, Holder<NoiseGeneratorSettings> holder
	) {
		this(registry, registry2, biomeSource, biomeSource, l, holder);
	}

	private NoiseBasedChunkGenerator(
		Registry<StructureSet> registry,
		Registry<NormalNoise.NoiseParameters> registry2,
		BiomeSource biomeSource,
		BiomeSource biomeSource2,
		long l,
		Holder<NoiseGeneratorSettings> holder
	) {
		super(registry, Optional.empty(), biomeSource, biomeSource2, l);
		this.noises = registry2;
		this.seed = l;
		this.settings = holder;
		NoiseGeneratorSettings noiseGeneratorSettings = this.settings.value();
		this.defaultBlock = noiseGeneratorSettings.defaultBlock();
		NoiseSettings noiseSettings = noiseGeneratorSettings.noiseSettings();
		this.router = noiseGeneratorSettings.createNoiseRouter(registry2, l);
		this.sampler = new Climate.Sampler(
			this.router.temperature(),
			this.router.humidity(),
			this.router.continents(),
			this.router.erosion(),
			this.router.depth(),
			this.router.ridges(),
			this.router.spawnTarget()
		);
		Aquifer.FluidStatus fluidStatus = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
		int i = noiseGeneratorSettings.seaLevel();
		Aquifer.FluidStatus fluidStatus2 = new Aquifer.FluidStatus(i, noiseGeneratorSettings.defaultFluid());
		Aquifer.FluidStatus fluidStatus3 = new Aquifer.FluidStatus(noiseSettings.minY() - 1, Blocks.AIR.defaultBlockState());
		this.globalFluidPicker = (j, k, lx) -> k < Math.min(-54, i) ? fluidStatus : fluidStatus2;
		this.surfaceSystem = new SurfaceSystem(registry2, this.defaultBlock, i, l, noiseGeneratorSettings.getRandomSource());
	}

	@Override
	public CompletableFuture<ChunkAccess> createBiomes(
		Registry<Biome> registry, Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess
	) {
		return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", (Supplier)(() -> {
			this.doCreateBiomes(blender, structureFeatureManager, chunkAccess);
			return chunkAccess;
		})), Util.backgroundExecutor());
	}

	private void doCreateBiomes(Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
		NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(
			this.router, () -> new Beardifier(structureFeatureManager, chunkAccess), this.settings.value(), this.globalFluidPicker, blender
		);
		BiomeResolver biomeResolver = BelowZeroRetrogen.getBiomeResolver(blender.getBiomeResolver(this.runtimeBiomeSource), chunkAccess);
		chunkAccess.fillBiomesFromNoise(biomeResolver, noiseChunk.cachedClimateSampler(this.router));
	}

	@VisibleForDebug
	public NoiseRouter router() {
		return this.router;
	}

	@Override
	public Climate.Sampler climateSampler() {
		return this.sampler;
	}

	@Override
	protected Codec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	@Override
	public ChunkGenerator withSeed(long l) {
		return new NoiseBasedChunkGenerator(this.structureSets, this.noises, this.biomeSource.withSeed(l), l, this.settings);
	}

	public boolean stable(long l, ResourceKey<NoiseGeneratorSettings> resourceKey) {
		return this.seed == l && this.settings.value().stable(resourceKey);
	}

	@Override
	public int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor) {
		NoiseSettings noiseSettings = this.settings.value().noiseSettings();
		int k = Math.max(noiseSettings.minY(), levelHeightAccessor.getMinBuildHeight());
		int l = Math.min(noiseSettings.minY() + noiseSettings.height(), levelHeightAccessor.getMaxBuildHeight());
		int m = Mth.intFloorDiv(k, noiseSettings.getCellHeight());
		int n = Mth.intFloorDiv(l - k, noiseSettings.getCellHeight());
		return n <= 0
			? levelHeightAccessor.getMinBuildHeight()
			: this.iterateNoiseColumn(i, j, null, types.isOpaque(), m, n).orElse(levelHeightAccessor.getMinBuildHeight());
	}

	@Override
	public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor) {
		NoiseSettings noiseSettings = this.settings.value().noiseSettings();
		int k = Math.max(noiseSettings.minY(), levelHeightAccessor.getMinBuildHeight());
		int l = Math.min(noiseSettings.minY() + noiseSettings.height(), levelHeightAccessor.getMaxBuildHeight());
		int m = Mth.intFloorDiv(k, noiseSettings.getCellHeight());
		int n = Mth.intFloorDiv(l - k, noiseSettings.getCellHeight());
		if (n <= 0) {
			return new NoiseColumn(k, EMPTY_COLUMN);
		} else {
			BlockState[] blockStates = new BlockState[n * noiseSettings.getCellHeight()];
			this.iterateNoiseColumn(i, j, blockStates, null, m, n);
			return new NoiseColumn(k, blockStates);
		}
	}

	@Override
	public void addDebugScreenInfo(List<String> list, BlockPos blockPos) {
		DecimalFormat decimalFormat = new DecimalFormat("0.000");
		DensityFunction.SinglePointContext singlePointContext = new DensityFunction.SinglePointContext(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		double d = this.router.ridges().compute(singlePointContext);
		list.add(
			"NoiseRouter T: "
				+ decimalFormat.format(this.router.temperature().compute(singlePointContext))
				+ " H: "
				+ decimalFormat.format(this.router.humidity().compute(singlePointContext))
				+ " C: "
				+ decimalFormat.format(this.router.continents().compute(singlePointContext))
				+ " E: "
				+ decimalFormat.format(this.router.erosion().compute(singlePointContext))
				+ " D: "
				+ decimalFormat.format(this.router.depth().compute(singlePointContext))
				+ " W: "
				+ decimalFormat.format(d)
				+ " PV: "
				+ decimalFormat.format((double)TerrainShaper.peaksAndValleys((float)d))
				+ " AS: "
				+ decimalFormat.format(this.router.initialDensityWithoutJaggedness().compute(singlePointContext))
				+ " N: "
				+ decimalFormat.format(this.router.finalDensity().compute(singlePointContext))
		);
	}

	private OptionalInt iterateNoiseColumn(int i, int j, @Nullable BlockState[] blockStates, @Nullable Predicate<BlockState> predicate, int k, int l) {
		NoiseSettings noiseSettings = this.settings.value().noiseSettings();
		int m = noiseSettings.getCellWidth();
		int n = noiseSettings.getCellHeight();
		int o = Math.floorDiv(i, m);
		int p = Math.floorDiv(j, m);
		int q = Math.floorMod(i, m);
		int r = Math.floorMod(j, m);
		int s = o * m;
		int t = p * m;
		double d = (double)q / (double)m;
		double e = (double)r / (double)m;
		NoiseChunk noiseChunk = NoiseChunk.forColumn(s, t, k, l, this.router, this.settings.value(), this.globalFluidPicker);
		noiseChunk.initializeForFirstCellX();
		noiseChunk.advanceCellX(0);

		for (int u = l - 1; u >= 0; u--) {
			noiseChunk.selectCellYZ(u, 0);

			for (int v = n - 1; v >= 0; v--) {
				int w = (k + u) * n + v;
				double f = (double)v / (double)n;
				noiseChunk.updateForY(w, f);
				noiseChunk.updateForX(i, d);
				noiseChunk.updateForZ(j, e);
				BlockState blockState = noiseChunk.getInterpolatedState();
				BlockState blockState2 = blockState == null ? this.defaultBlock : blockState;
				if (blockStates != null) {
					int x = u * n + v;
					blockStates[x] = blockState2;
				}

				if (predicate != null && predicate.test(blockState2)) {
					noiseChunk.stopInterpolation();
					return OptionalInt.of(w + 1);
				}
			}
		}

		noiseChunk.stopInterpolation();
		return OptionalInt.empty();
	}

	@Override
	public void buildSurface(WorldGenRegion worldGenRegion, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
		if (!SharedConstants.debugVoidTerrain(chunkAccess.getPos())) {
			WorldGenerationContext worldGenerationContext = new WorldGenerationContext(this, worldGenRegion);
			NoiseGeneratorSettings noiseGeneratorSettings = this.settings.value();
			NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(
				this.router, () -> new Beardifier(structureFeatureManager, chunkAccess), noiseGeneratorSettings, this.globalFluidPicker, Blender.of(worldGenRegion)
			);
			this.surfaceSystem
				.buildSurface(
					worldGenRegion.getBiomeManager(),
					worldGenRegion.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY),
					noiseGeneratorSettings.useLegacyRandomSource(),
					worldGenerationContext,
					chunkAccess,
					noiseChunk,
					noiseGeneratorSettings.surfaceRule()
				);
		}
	}

	@Override
	public void applyCarvers(
		WorldGenRegion worldGenRegion,
		long l,
		BiomeManager biomeManager,
		StructureFeatureManager structureFeatureManager,
		ChunkAccess chunkAccess,
		GenerationStep.Carving carving
	) {
		BiomeManager biomeManager2 = biomeManager.withDifferentSource((ix, jx, kx) -> this.biomeSource.getNoiseBiome(ix, jx, kx, this.climateSampler()));
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
		int i = 8;
		ChunkPos chunkPos = chunkAccess.getPos();
		NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(
			this.router, () -> new Beardifier(structureFeatureManager, chunkAccess), this.settings.value(), this.globalFluidPicker, Blender.of(worldGenRegion)
		);
		Aquifer aquifer = noiseChunk.aquifer();
		CarvingContext carvingContext = new CarvingContext(this, worldGenRegion.registryAccess(), chunkAccess.getHeightAccessorForGeneration(), noiseChunk);
		CarvingMask carvingMask = ((ProtoChunk)chunkAccess).getOrCreateCarvingMask(carving);

		for (int j = -8; j <= 8; j++) {
			for (int k = -8; k <= 8; k++) {
				ChunkPos chunkPos2 = new ChunkPos(chunkPos.x + j, chunkPos.z + k);
				ChunkAccess chunkAccess2 = worldGenRegion.getChunk(chunkPos2.x, chunkPos2.z);
				BiomeGenerationSettings biomeGenerationSettings = chunkAccess2.carverBiome(
						() -> this.biomeSource
								.getNoiseBiome(QuartPos.fromBlock(chunkPos2.getMinBlockX()), 0, QuartPos.fromBlock(chunkPos2.getMinBlockZ()), this.climateSampler())
					)
					.value()
					.getGenerationSettings();
				Iterable<Holder<ConfiguredWorldCarver<?>>> iterable = biomeGenerationSettings.getCarvers(carving);
				int m = 0;

				for (Holder<ConfiguredWorldCarver<?>> holder : iterable) {
					ConfiguredWorldCarver<?> configuredWorldCarver = holder.value();
					worldgenRandom.setLargeFeatureSeed(l + (long)m, chunkPos2.x, chunkPos2.z);
					if (configuredWorldCarver.isStartChunk(worldgenRandom)) {
						configuredWorldCarver.carve(carvingContext, chunkAccess, biomeManager2::getBiome, worldgenRandom, aquifer, chunkPos2, carvingMask);
					}

					m++;
				}
			}
		}
	}

	@Override
	public CompletableFuture<ChunkAccess> fillFromNoise(
		Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess
	) {
		NoiseSettings noiseSettings = this.settings.value().noiseSettings();
		LevelHeightAccessor levelHeightAccessor = chunkAccess.getHeightAccessorForGeneration();
		int i = Math.max(noiseSettings.minY(), levelHeightAccessor.getMinBuildHeight());
		int j = Math.min(noiseSettings.minY() + noiseSettings.height(), levelHeightAccessor.getMaxBuildHeight());
		int k = Mth.intFloorDiv(i, noiseSettings.getCellHeight());
		int l = Mth.intFloorDiv(j - i, noiseSettings.getCellHeight());
		if (l <= 0) {
			return CompletableFuture.completedFuture(chunkAccess);
		} else {
			int m = chunkAccess.getSectionIndex(l * noiseSettings.getCellHeight() - 1 + i);
			int n = chunkAccess.getSectionIndex(i);
			Set<LevelChunkSection> set = Sets.<LevelChunkSection>newHashSet();

			for (int o = m; o >= n; o--) {
				LevelChunkSection levelChunkSection = chunkAccess.getSection(o);
				levelChunkSection.acquire();
				set.add(levelChunkSection);
			}

			return CompletableFuture.supplyAsync(
					Util.wrapThreadWithTaskName("wgen_fill_noise", (Supplier)(() -> this.doFill(blender, structureFeatureManager, chunkAccess, k, l))),
					Util.backgroundExecutor()
				)
				.whenCompleteAsync((chunkAccessx, throwable) -> {
					for (LevelChunkSection levelChunkSectionx : set) {
						levelChunkSectionx.release();
					}
				}, executor);
		}
	}

	private ChunkAccess doFill(Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, int i, int j) {
		NoiseGeneratorSettings noiseGeneratorSettings = this.settings.value();
		NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(
			this.router, () -> new Beardifier(structureFeatureManager, chunkAccess), noiseGeneratorSettings, this.globalFluidPicker, blender
		);
		Heightmap heightmap = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap heightmap2 = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
		ChunkPos chunkPos = chunkAccess.getPos();
		int k = chunkPos.getMinBlockX();
		int l = chunkPos.getMinBlockZ();
		Aquifer aquifer = noiseChunk.aquifer();
		noiseChunk.initializeForFirstCellX();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		NoiseSettings noiseSettings = noiseGeneratorSettings.noiseSettings();
		int m = noiseSettings.getCellWidth();
		int n = noiseSettings.getCellHeight();
		int o = 16 / m;
		int p = 16 / m;

		for (int q = 0; q < o; q++) {
			noiseChunk.advanceCellX(q);

			for (int r = 0; r < p; r++) {
				LevelChunkSection levelChunkSection = chunkAccess.getSection(chunkAccess.getSectionsCount() - 1);

				for (int s = j - 1; s >= 0; s--) {
					noiseChunk.selectCellYZ(s, r);

					for (int t = n - 1; t >= 0; t--) {
						int u = (i + s) * n + t;
						int v = u & 15;
						int w = chunkAccess.getSectionIndex(u);
						if (chunkAccess.getSectionIndex(levelChunkSection.bottomBlockY()) != w) {
							levelChunkSection = chunkAccess.getSection(w);
						}

						double d = (double)t / (double)n;
						noiseChunk.updateForY(u, d);

						for (int x = 0; x < m; x++) {
							int y = k + q * m + x;
							int z = y & 15;
							double e = (double)x / (double)m;
							noiseChunk.updateForX(y, e);

							for (int aa = 0; aa < m; aa++) {
								int ab = l + r * m + aa;
								int ac = ab & 15;
								double f = (double)aa / (double)m;
								noiseChunk.updateForZ(ab, f);
								BlockState blockState = noiseChunk.getInterpolatedState();
								if (blockState == null) {
									blockState = this.defaultBlock;
								}

								blockState = this.debugPreliminarySurfaceLevel(noiseChunk, y, u, ab, blockState);
								if (blockState != AIR && !SharedConstants.debugVoidTerrain(chunkAccess.getPos())) {
									if (blockState.getLightEmission() != 0 && chunkAccess instanceof ProtoChunk) {
										mutableBlockPos.set(y, u, ab);
										((ProtoChunk)chunkAccess).addLight(mutableBlockPos);
									}

									levelChunkSection.setBlockState(z, v, ac, blockState, false);
									heightmap.update(z, u, ac, blockState);
									heightmap2.update(z, u, ac, blockState);
									if (aquifer.shouldScheduleFluidUpdate() && !blockState.getFluidState().isEmpty()) {
										mutableBlockPos.set(y, u, ab);
										chunkAccess.markPosForPostprocessing(mutableBlockPos);
									}
								}
							}
						}
					}
				}
			}

			noiseChunk.swapSlices();
		}

		noiseChunk.stopInterpolation();
		return chunkAccess;
	}

	private BlockState debugPreliminarySurfaceLevel(NoiseChunk noiseChunk, int i, int j, int k, BlockState blockState) {
		return blockState;
	}

	@Override
	public int getGenDepth() {
		return this.settings.value().noiseSettings().height();
	}

	@Override
	public int getSeaLevel() {
		return this.settings.value().seaLevel();
	}

	@Override
	public int getMinY() {
		return this.settings.value().noiseSettings().minY();
	}

	@Override
	public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
		if (!this.settings.value().disableMobGeneration()) {
			ChunkPos chunkPos = worldGenRegion.getCenter();
			Holder<Biome> holder = worldGenRegion.getBiome(chunkPos.getWorldPosition().atY(worldGenRegion.getMaxBuildHeight() - 1));
			WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
			worldgenRandom.setDecorationSeed(worldGenRegion.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
			NaturalSpawner.spawnMobsForChunkGeneration(worldGenRegion, holder, chunkPos, worldgenRandom);
		}
	}

	@Deprecated
	public Optional<BlockState> topMaterial(
		CarvingContext carvingContext, Function<BlockPos, Holder<Biome>> function, ChunkAccess chunkAccess, NoiseChunk noiseChunk, BlockPos blockPos, boolean bl
	) {
		return this.surfaceSystem.topMaterial(this.settings.value().surfaceRule(), carvingContext, function, chunkAccess, noiseChunk, blockPos, bl);
	}
}
