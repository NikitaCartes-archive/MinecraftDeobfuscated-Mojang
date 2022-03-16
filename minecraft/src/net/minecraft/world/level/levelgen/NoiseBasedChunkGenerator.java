package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
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
import org.apache.commons.lang3.mutable.MutableObject;

public final class NoiseBasedChunkGenerator extends ChunkGenerator {
	public static final Codec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create(
		instance -> commonCodec(instance)
				.and(
					instance.group(
						RegistryOps.retrieveRegistry(Registry.NOISE_REGISTRY).forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.noises),
						BiomeSource.CODEC.fieldOf("biome_source").forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.biomeSource),
						NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.settings)
					)
				)
				.apply(instance, instance.stable(NoiseBasedChunkGenerator::new))
	);
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	protected final BlockState defaultBlock;
	private final Registry<NormalNoise.NoiseParameters> noises;
	protected final Holder<NoiseGeneratorSettings> settings;
	private final Aquifer.FluidPicker globalFluidPicker;

	public NoiseBasedChunkGenerator(
		Registry<StructureSet> registry, Registry<NormalNoise.NoiseParameters> registry2, BiomeSource biomeSource, Holder<NoiseGeneratorSettings> holder
	) {
		this(registry, registry2, biomeSource, biomeSource, holder);
	}

	private NoiseBasedChunkGenerator(
		Registry<StructureSet> registry,
		Registry<NormalNoise.NoiseParameters> registry2,
		BiomeSource biomeSource,
		BiomeSource biomeSource2,
		Holder<NoiseGeneratorSettings> holder
	) {
		super(registry, Optional.empty(), biomeSource, biomeSource2);
		this.noises = registry2;
		this.settings = holder;
		NoiseGeneratorSettings noiseGeneratorSettings = this.settings.value();
		this.defaultBlock = noiseGeneratorSettings.defaultBlock();
		Aquifer.FluidStatus fluidStatus = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
		int i = noiseGeneratorSettings.seaLevel();
		Aquifer.FluidStatus fluidStatus2 = new Aquifer.FluidStatus(i, noiseGeneratorSettings.defaultFluid());
		Aquifer.FluidStatus fluidStatus3 = new Aquifer.FluidStatus(noiseGeneratorSettings.noiseSettings().minY() - 1, Blocks.AIR.defaultBlockState());
		this.globalFluidPicker = (j, k, l) -> k < Math.min(-54, i) ? fluidStatus : fluidStatus2;
	}

	@Override
	public CompletableFuture<ChunkAccess> createBiomes(
		Registry<Biome> registry, Executor executor, RandomState randomState, Blender blender, StructureManager structureManager, ChunkAccess chunkAccess
	) {
		return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", (Supplier)(() -> {
			this.doCreateBiomes(blender, randomState, structureManager, chunkAccess);
			return chunkAccess;
		})), Util.backgroundExecutor());
	}

	private void doCreateBiomes(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {
		NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(chunkAccessx -> this.createNoiseChunk(chunkAccessx, structureManager, blender, randomState));
		BiomeResolver biomeResolver = BelowZeroRetrogen.getBiomeResolver(blender.getBiomeResolver(this.runtimeBiomeSource), chunkAccess);
		chunkAccess.fillBiomesFromNoise(biomeResolver, noiseChunk.cachedClimateSampler(randomState.router(), this.settings.value().spawnTarget()));
	}

	private NoiseChunk createNoiseChunk(ChunkAccess chunkAccess, StructureManager structureManager, Blender blender, RandomState randomState) {
		return NoiseChunk.forChunk(
			chunkAccess,
			randomState.router(),
			new Beardifier(structureManager, chunkAccess),
			this.settings.value(),
			this.globalFluidPicker,
			blender,
			randomState.aquiferRandom(),
			randomState.oreRandom()
		);
	}

	@Override
	protected Codec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	public Holder<NoiseGeneratorSettings> generatorSettings() {
		return this.settings;
	}

	public boolean stable(ResourceKey<NoiseGeneratorSettings> resourceKey) {
		return this.settings.is(resourceKey);
	}

	@Override
	public int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
		return this.iterateNoiseColumn(levelHeightAccessor, randomState, i, j, null, types.isOpaque()).orElse(levelHeightAccessor.getMinBuildHeight());
	}

	@Override
	public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
		MutableObject<NoiseColumn> mutableObject = new MutableObject<>();
		this.iterateNoiseColumn(levelHeightAccessor, randomState, i, j, mutableObject, null);
		return mutableObject.getValue();
	}

	@Override
	public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {
		DecimalFormat decimalFormat = new DecimalFormat("0.000");
		NoiseRouter noiseRouter = randomState.router();
		DensityFunction.SinglePointContext singlePointContext = new DensityFunction.SinglePointContext(blockPos.getX(), blockPos.getY(), blockPos.getZ());
		double d = noiseRouter.ridges().compute(singlePointContext);
		list.add(
			"NoiseRouter T: "
				+ decimalFormat.format(noiseRouter.temperature().compute(singlePointContext))
				+ " V: "
				+ decimalFormat.format(noiseRouter.vegetation().compute(singlePointContext))
				+ " C: "
				+ decimalFormat.format(noiseRouter.continents().compute(singlePointContext))
				+ " E: "
				+ decimalFormat.format(noiseRouter.erosion().compute(singlePointContext))
				+ " D: "
				+ decimalFormat.format(noiseRouter.depth().compute(singlePointContext))
				+ " W: "
				+ decimalFormat.format(d)
				+ " PV: "
				+ decimalFormat.format((double)NoiseRouterData.peaksAndValleys((float)d))
				+ " AS: "
				+ decimalFormat.format(noiseRouter.initialDensityWithoutJaggedness().compute(singlePointContext))
				+ " N: "
				+ decimalFormat.format(noiseRouter.finalDensity().compute(singlePointContext))
		);
	}

	private OptionalInt iterateNoiseColumn(
		LevelHeightAccessor levelHeightAccessor,
		RandomState randomState,
		int i,
		int j,
		@Nullable MutableObject<NoiseColumn> mutableObject,
		@Nullable Predicate<BlockState> predicate
	) {
		NoiseSettings noiseSettings = this.settings.value().noiseSettings();
		int k = Math.max(noiseSettings.minY(), levelHeightAccessor.getMinBuildHeight());
		int l = Math.min(noiseSettings.minY() + noiseSettings.height(), levelHeightAccessor.getMaxBuildHeight());
		int m = Mth.intFloorDiv(k, noiseSettings.getCellHeight());
		int n = Mth.intFloorDiv(l - k, noiseSettings.getCellHeight());
		if (n <= 0) {
			return OptionalInt.empty();
		} else {
			BlockState[] blockStates;
			if (mutableObject == null) {
				blockStates = null;
			} else {
				blockStates = new BlockState[n * noiseSettings.getCellHeight()];
				mutableObject.setValue(new NoiseColumn(k, blockStates));
			}

			int o = noiseSettings.getCellWidth();
			int p = noiseSettings.getCellHeight();
			int q = Math.floorDiv(i, o);
			int r = Math.floorDiv(j, o);
			int s = Math.floorMod(i, o);
			int t = Math.floorMod(j, o);
			int u = q * o;
			int v = r * o;
			double d = (double)s / (double)o;
			double e = (double)t / (double)o;
			NoiseChunk noiseChunk = new NoiseChunk(
				1,
				levelHeightAccessor,
				randomState.router(),
				u,
				v,
				DensityFunctions.BeardifierMarker.INSTANCE,
				this.settings.value(),
				this.globalFluidPicker,
				Blender.empty(),
				randomState.aquiferRandom(),
				randomState.oreRandom()
			);
			noiseChunk.initializeForFirstCellX();
			noiseChunk.advanceCellX(0);

			for (int w = n - 1; w >= 0; w--) {
				noiseChunk.selectCellYZ(w, 0);

				for (int x = p - 1; x >= 0; x--) {
					int y = (m + w) * p + x;
					double f = (double)x / (double)p;
					noiseChunk.updateForY(y, f);
					noiseChunk.updateForX(i, d);
					noiseChunk.updateForZ(j, e);
					BlockState blockState = noiseChunk.getInterpolatedState();
					BlockState blockState2 = blockState == null ? this.defaultBlock : blockState;
					if (blockStates != null) {
						int z = w * p + x;
						blockStates[z] = blockState2;
					}

					if (predicate != null && predicate.test(blockState2)) {
						noiseChunk.stopInterpolation();
						return OptionalInt.of(y + 1);
					}
				}
			}

			noiseChunk.stopInterpolation();
			return OptionalInt.empty();
		}
	}

	@Override
	public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess) {
		if (!SharedConstants.debugVoidTerrain(chunkAccess.getPos())) {
			WorldGenerationContext worldGenerationContext = new WorldGenerationContext(this, worldGenRegion);
			this.buildSurface(
				chunkAccess,
				worldGenerationContext,
				randomState,
				structureManager,
				worldGenRegion.getBiomeManager(),
				worldGenRegion.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY),
				Blender.of(worldGenRegion)
			);
		}
	}

	@VisibleForTesting
	public void buildSurface(
		ChunkAccess chunkAccess,
		WorldGenerationContext worldGenerationContext,
		RandomState randomState,
		StructureManager structureManager,
		BiomeManager biomeManager,
		Registry<Biome> registry,
		Blender blender
	) {
		NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(chunkAccessx -> this.createNoiseChunk(chunkAccessx, structureManager, blender, randomState));
		NoiseGeneratorSettings noiseGeneratorSettings = this.settings.value();
		randomState.surfaceSystem()
			.buildSurface(
				randomState,
				biomeManager,
				registry,
				noiseGeneratorSettings.useLegacyRandomSource(),
				worldGenerationContext,
				chunkAccess,
				noiseChunk,
				noiseGeneratorSettings.surfaceRule()
			);
	}

	@Override
	public void applyCarvers(
		WorldGenRegion worldGenRegion,
		long l,
		RandomState randomState,
		BiomeManager biomeManager,
		StructureManager structureManager,
		ChunkAccess chunkAccess,
		GenerationStep.Carving carving
	) {
		BiomeManager biomeManager2 = biomeManager.withDifferentSource((ix, jx, kx) -> this.biomeSource.getNoiseBiome(ix, jx, kx, randomState.sampler()));
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
		int i = 8;
		ChunkPos chunkPos = chunkAccess.getPos();
		NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(
			chunkAccessx -> this.createNoiseChunk(chunkAccessx, structureManager, Blender.of(worldGenRegion), randomState)
		);
		Aquifer aquifer = noiseChunk.aquifer();
		CarvingContext carvingContext = new CarvingContext(
			this, worldGenRegion.registryAccess(), chunkAccess.getHeightAccessorForGeneration(), noiseChunk, randomState, this.settings.value().surfaceRule()
		);
		CarvingMask carvingMask = ((ProtoChunk)chunkAccess).getOrCreateCarvingMask(carving);

		for (int j = -8; j <= 8; j++) {
			for (int k = -8; k <= 8; k++) {
				ChunkPos chunkPos2 = new ChunkPos(chunkPos.x + j, chunkPos.z + k);
				ChunkAccess chunkAccess2 = worldGenRegion.getChunk(chunkPos2.x, chunkPos2.z);
				BiomeGenerationSettings biomeGenerationSettings = chunkAccess2.carverBiome(
						() -> this.biomeSource
								.getNoiseBiome(QuartPos.fromBlock(chunkPos2.getMinBlockX()), 0, QuartPos.fromBlock(chunkPos2.getMinBlockZ()), randomState.sampler())
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
		Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess
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
					Util.wrapThreadWithTaskName("wgen_fill_noise", (Supplier)(() -> this.doFill(blender, structureManager, randomState, chunkAccess, k, l))),
					Util.backgroundExecutor()
				)
				.whenCompleteAsync((chunkAccessx, throwable) -> {
					for (LevelChunkSection levelChunkSectionx : set) {
						levelChunkSectionx.release();
					}
				}, executor);
		}
	}

	private ChunkAccess doFill(Blender blender, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess, int i, int j) {
		NoiseGeneratorSettings noiseGeneratorSettings = this.settings.value();
		NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(chunkAccessx -> this.createNoiseChunk(chunkAccessx, structureManager, blender, randomState));
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
}
