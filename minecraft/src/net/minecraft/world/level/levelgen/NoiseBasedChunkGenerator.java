package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import org.apache.commons.lang3.mutable.MutableObject;

public final class NoiseBasedChunkGenerator extends ChunkGenerator {
	public static final MapCodec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					BiomeSource.CODEC.fieldOf("biome_source").forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.biomeSource),
					NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.settings)
				)
				.apply(instance, instance.stable(NoiseBasedChunkGenerator::new))
	);
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private final Holder<NoiseGeneratorSettings> settings;
	private final Supplier<Aquifer.FluidPicker> globalFluidPicker;

	public NoiseBasedChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> holder) {
		super(biomeSource);
		this.settings = holder;
		this.globalFluidPicker = Suppliers.memoize(() -> createFluidPicker(holder.value()));
	}

	private static Aquifer.FluidPicker createFluidPicker(NoiseGeneratorSettings noiseGeneratorSettings) {
		Aquifer.FluidStatus fluidStatus = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
		int i = noiseGeneratorSettings.seaLevel();
		Aquifer.FluidStatus fluidStatus2 = new Aquifer.FluidStatus(i, noiseGeneratorSettings.defaultFluid());
		Aquifer.FluidStatus fluidStatus3 = new Aquifer.FluidStatus(DimensionType.MIN_Y * 2, Blocks.AIR.defaultBlockState());
		return (j, k, l) -> k < Math.min(-54, i) ? fluidStatus : fluidStatus2;
	}

	@Override
	public CompletableFuture<ChunkAccess> createBiomes(RandomState randomState, Blender blender, StructureManager structureManager, ChunkAccess chunkAccess) {
		return CompletableFuture.supplyAsync(() -> {
			this.doCreateBiomes(blender, randomState, structureManager, chunkAccess);
			return chunkAccess;
		}, Util.backgroundExecutor().forName("init_biomes"));
	}

	private void doCreateBiomes(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {
		NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(chunkAccessx -> this.createNoiseChunk(chunkAccessx, structureManager, blender, randomState));
		BiomeResolver biomeResolver = BelowZeroRetrogen.getBiomeResolver(blender.getBiomeResolver(this.biomeSource), chunkAccess);
		chunkAccess.fillBiomesFromNoise(biomeResolver, noiseChunk.cachedClimateSampler(randomState.router(), this.settings.value().spawnTarget()));
	}

	private NoiseChunk createNoiseChunk(ChunkAccess chunkAccess, StructureManager structureManager, Blender blender, RandomState randomState) {
		return NoiseChunk.forChunk(
			chunkAccess,
			randomState,
			Beardifier.forStructuresInChunk(structureManager, chunkAccess.getPos()),
			this.settings.value(),
			(Aquifer.FluidPicker)this.globalFluidPicker.get(),
			blender
		);
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> codec() {
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
		return this.iterateNoiseColumn(levelHeightAccessor, randomState, i, j, null, types.isOpaque()).orElse(levelHeightAccessor.getMinY());
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
		NoiseSettings noiseSettings = this.settings.value().noiseSettings().clampToHeightAccessor(levelHeightAccessor);
		int k = noiseSettings.getCellHeight();
		int l = noiseSettings.minY();
		int m = Mth.floorDiv(l, k);
		int n = Mth.floorDiv(noiseSettings.height(), k);
		if (n <= 0) {
			return OptionalInt.empty();
		} else {
			BlockState[] blockStates;
			if (mutableObject == null) {
				blockStates = null;
			} else {
				blockStates = new BlockState[noiseSettings.height()];
				mutableObject.setValue(new NoiseColumn(l, blockStates));
			}

			int o = noiseSettings.getCellWidth();
			int p = Math.floorDiv(i, o);
			int q = Math.floorDiv(j, o);
			int r = Math.floorMod(i, o);
			int s = Math.floorMod(j, o);
			int t = p * o;
			int u = q * o;
			double d = (double)r / (double)o;
			double e = (double)s / (double)o;
			NoiseChunk noiseChunk = new NoiseChunk(
				1,
				randomState,
				t,
				u,
				noiseSettings,
				DensityFunctions.BeardifierMarker.INSTANCE,
				this.settings.value(),
				(Aquifer.FluidPicker)this.globalFluidPicker.get(),
				Blender.empty()
			);
			noiseChunk.initializeForFirstCellX();
			noiseChunk.advanceCellX(0);

			for (int v = n - 1; v >= 0; v--) {
				noiseChunk.selectCellYZ(v, 0);

				for (int w = k - 1; w >= 0; w--) {
					int x = (m + v) * k + w;
					double f = (double)w / (double)k;
					noiseChunk.updateForY(x, f);
					noiseChunk.updateForX(i, d);
					noiseChunk.updateForZ(j, e);
					BlockState blockState = noiseChunk.getInterpolatedState();
					BlockState blockState2 = blockState == null ? this.settings.value().defaultBlock() : blockState;
					if (blockStates != null) {
						int y = v * k + w;
						blockStates[y] = blockState2;
					}

					if (predicate != null && predicate.test(blockState2)) {
						noiseChunk.stopInterpolation();
						return OptionalInt.of(x + 1);
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
				worldGenRegion.registryAccess().lookupOrThrow(Registries.BIOME),
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
		WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess
	) {
		BiomeManager biomeManager2 = biomeManager.withDifferentSource((ix, jx, kx) -> this.biomeSource.getNoiseBiome(ix, jx, kx, randomState.sampler()));
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
		int i = 8;
		ChunkPos chunkPos = chunkAccess.getPos();
		NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(
			chunkAccessx -> this.createNoiseChunk(chunkAccessx, structureManager, Blender.of(worldGenRegion), randomState)
		);
		Aquifer aquifer = noiseChunk.aquifer();
		CarvingContext carvingContext = new CarvingContext(
			this, worldGenRegion.registryAccess(), chunkAccess.getHeightAccessorForGeneration(), noiseChunk, randomState, this.settings.value().surfaceRule()
		);
		CarvingMask carvingMask = ((ProtoChunk)chunkAccess).getOrCreateCarvingMask();

		for (int j = -8; j <= 8; j++) {
			for (int k = -8; k <= 8; k++) {
				ChunkPos chunkPos2 = new ChunkPos(chunkPos.x + j, chunkPos.z + k);
				ChunkAccess chunkAccess2 = worldGenRegion.getChunk(chunkPos2.x, chunkPos2.z);
				BiomeGenerationSettings biomeGenerationSettings = chunkAccess2.carverBiome(
					() -> this.getBiomeGenerationSettings(
							this.biomeSource.getNoiseBiome(QuartPos.fromBlock(chunkPos2.getMinBlockX()), 0, QuartPos.fromBlock(chunkPos2.getMinBlockZ()), randomState.sampler())
						)
				);
				Iterable<Holder<ConfiguredWorldCarver<?>>> iterable = biomeGenerationSettings.getCarvers();
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
	public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {
		NoiseSettings noiseSettings = this.settings.value().noiseSettings().clampToHeightAccessor(chunkAccess.getHeightAccessorForGeneration());
		int i = noiseSettings.minY();
		int j = Mth.floorDiv(i, noiseSettings.getCellHeight());
		int k = Mth.floorDiv(noiseSettings.height(), noiseSettings.getCellHeight());
		return k <= 0 ? CompletableFuture.completedFuture(chunkAccess) : CompletableFuture.supplyAsync(() -> {
			int l = chunkAccess.getSectionIndex(k * noiseSettings.getCellHeight() - 1 + i);
			int m = chunkAccess.getSectionIndex(i);
			Set<LevelChunkSection> set = Sets.<LevelChunkSection>newHashSet();

			for (int n = l; n >= m; n--) {
				LevelChunkSection levelChunkSection = chunkAccess.getSection(n);
				levelChunkSection.acquire();
				set.add(levelChunkSection);
			}

			ChunkAccess var20;
			try {
				var20 = this.doFill(blender, structureManager, randomState, chunkAccess, j, k);
			} finally {
				for (LevelChunkSection levelChunkSection3 : set) {
					levelChunkSection3.release();
				}
			}

			return var20;
		}, Util.backgroundExecutor().forName("wgen_fill_noise"));
	}

	private ChunkAccess doFill(Blender blender, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess, int i, int j) {
		NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(chunkAccessx -> this.createNoiseChunk(chunkAccessx, structureManager, blender, randomState));
		Heightmap heightmap = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap heightmap2 = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
		ChunkPos chunkPos = chunkAccess.getPos();
		int k = chunkPos.getMinBlockX();
		int l = chunkPos.getMinBlockZ();
		Aquifer aquifer = noiseChunk.aquifer();
		noiseChunk.initializeForFirstCellX();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		int m = noiseChunk.cellWidth();
		int n = noiseChunk.cellHeight();
		int o = 16 / m;
		int p = 16 / m;

		for (int q = 0; q < o; q++) {
			noiseChunk.advanceCellX(q);

			for (int r = 0; r < p; r++) {
				int s = chunkAccess.getSectionsCount() - 1;
				LevelChunkSection levelChunkSection = chunkAccess.getSection(s);

				for (int t = j - 1; t >= 0; t--) {
					noiseChunk.selectCellYZ(t, r);

					for (int u = n - 1; u >= 0; u--) {
						int v = (i + t) * n + u;
						int w = v & 15;
						int x = chunkAccess.getSectionIndex(v);
						if (s != x) {
							s = x;
							levelChunkSection = chunkAccess.getSection(x);
						}

						double d = (double)u / (double)n;
						noiseChunk.updateForY(v, d);

						for (int y = 0; y < m; y++) {
							int z = k + q * m + y;
							int aa = z & 15;
							double e = (double)y / (double)m;
							noiseChunk.updateForX(z, e);

							for (int ab = 0; ab < m; ab++) {
								int ac = l + r * m + ab;
								int ad = ac & 15;
								double f = (double)ab / (double)m;
								noiseChunk.updateForZ(ac, f);
								BlockState blockState = noiseChunk.getInterpolatedState();
								if (blockState == null) {
									blockState = this.settings.value().defaultBlock();
								}

								blockState = this.debugPreliminarySurfaceLevel(noiseChunk, z, v, ac, blockState);
								if (blockState != AIR && !SharedConstants.debugVoidTerrain(chunkAccess.getPos())) {
									levelChunkSection.setBlockState(aa, w, ad, blockState, false);
									heightmap.update(aa, v, ad, blockState);
									heightmap2.update(aa, v, ad, blockState);
									if (aquifer.shouldScheduleFluidUpdate() && !blockState.getFluidState().isEmpty()) {
										mutableBlockPos.set(z, v, ac);
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
			Holder<Biome> holder = worldGenRegion.getBiome(chunkPos.getWorldPosition().atY(worldGenRegion.getMaxY()));
			WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
			worldgenRandom.setDecorationSeed(worldGenRegion.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
			NaturalSpawner.spawnMobsForChunkGeneration(worldGenRegion, holder, chunkPos, worldgenRandom);
		}
	}
}
