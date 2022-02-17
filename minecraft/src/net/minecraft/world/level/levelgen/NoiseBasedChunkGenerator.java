package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.ListIterator;
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
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
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
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MobSpawnSettings;
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
import net.minecraft.world.level.levelgen.feature.NetherFortressFeature;
import net.minecraft.world.level.levelgen.feature.OceanMonumentFeature;
import net.minecraft.world.level.levelgen.feature.PillagerOutpostFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.SwamplandHutFeature;
import net.minecraft.world.level.levelgen.material.MaterialRuleList;
import net.minecraft.world.level.levelgen.material.WorldGenMaterialRule;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class NoiseBasedChunkGenerator extends ChunkGenerator {
	public static final Codec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					RegistryLookupCodec.create(Registry.NOISE_REGISTRY).forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.noises),
					BiomeSource.CODEC.fieldOf("biome_source").forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.biomeSource),
					Codec.LONG.fieldOf("seed").stable().forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.seed),
					NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.settings)
				)
				.apply(instance, instance.stable(NoiseBasedChunkGenerator::new))
	);
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private static final BlockState[] EMPTY_COLUMN = new BlockState[0];
	protected final BlockState defaultBlock;
	private final Registry<NormalNoise.NoiseParameters> noises;
	private final long seed;
	protected final Supplier<NoiseGeneratorSettings> settings;
	private final NoiseSampler sampler;
	private final SurfaceSystem surfaceSystem;
	private final WorldGenMaterialRule materialRule;
	private final Aquifer.FluidPicker globalFluidPicker;

	public NoiseBasedChunkGenerator(Registry<NormalNoise.NoiseParameters> registry, BiomeSource biomeSource, long l, Supplier<NoiseGeneratorSettings> supplier) {
		this(registry, biomeSource, biomeSource, l, supplier);
	}

	private NoiseBasedChunkGenerator(
		Registry<NormalNoise.NoiseParameters> registry, BiomeSource biomeSource, BiomeSource biomeSource2, long l, Supplier<NoiseGeneratorSettings> supplier
	) {
		super(biomeSource, biomeSource2, ((NoiseGeneratorSettings)supplier.get()).structureSettings(), l);
		this.noises = registry;
		this.seed = l;
		this.settings = supplier;
		NoiseGeneratorSettings noiseGeneratorSettings = (NoiseGeneratorSettings)this.settings.get();
		this.defaultBlock = noiseGeneratorSettings.getDefaultBlock();
		NoiseSettings noiseSettings = noiseGeneratorSettings.noiseSettings();
		this.sampler = new NoiseSampler(noiseSettings, noiseGeneratorSettings.isNoiseCavesEnabled(), l, registry, noiseGeneratorSettings.getRandomSource());
		Builder<WorldGenMaterialRule> builder = ImmutableList.builder();
		builder.add(NoiseChunk::updateNoiseAndGenerateBaseState);
		builder.add(NoiseChunk::oreVeinify);
		this.materialRule = new MaterialRuleList(builder.build());
		Aquifer.FluidStatus fluidStatus = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
		int i = noiseGeneratorSettings.seaLevel();
		Aquifer.FluidStatus fluidStatus2 = new Aquifer.FluidStatus(i, noiseGeneratorSettings.getDefaultFluid());
		Aquifer.FluidStatus fluidStatus3 = new Aquifer.FluidStatus(noiseSettings.minY() - 1, Blocks.AIR.defaultBlockState());
		this.globalFluidPicker = (j, k, lx) -> k < Math.min(-54, i) ? fluidStatus : fluidStatus2;
		this.surfaceSystem = new SurfaceSystem(registry, this.defaultBlock, i, l, noiseGeneratorSettings.getRandomSource());
	}

	@Override
	public CompletableFuture<ChunkAccess> createBiomes(
		Registry<Biome> registry, Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess
	) {
		return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", (Supplier)(() -> {
			this.doCreateBiomes(registry, blender, structureFeatureManager, chunkAccess);
			return chunkAccess;
		})), Util.backgroundExecutor());
	}

	private void doCreateBiomes(Registry<Biome> registry, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
		NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(
			this.sampler, () -> new Beardifier(structureFeatureManager, chunkAccess), (NoiseGeneratorSettings)this.settings.get(), this.globalFluidPicker, blender
		);
		BiomeResolver biomeResolver = BelowZeroRetrogen.getBiomeResolver(blender.getBiomeResolver(this.runtimeBiomeSource), registry, chunkAccess);
		chunkAccess.fillBiomesFromNoise(biomeResolver, (i, j, k) -> this.sampler.target(i, j, k, noiseChunk.noiseData(i, k)));
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
		return new NoiseBasedChunkGenerator(this.noises, this.biomeSource.withSeed(l), l, this.settings);
	}

	public boolean stable(long l, ResourceKey<NoiseGeneratorSettings> resourceKey) {
		return this.seed == l && ((NoiseGeneratorSettings)this.settings.get()).stable(resourceKey);
	}

	@Override
	public int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor) {
		NoiseSettings noiseSettings = ((NoiseGeneratorSettings)this.settings.get()).noiseSettings();
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
		NoiseSettings noiseSettings = ((NoiseGeneratorSettings)this.settings.get()).noiseSettings();
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

	private OptionalInt iterateNoiseColumn(int i, int j, @Nullable BlockState[] blockStates, @Nullable Predicate<BlockState> predicate, int k, int l) {
		NoiseSettings noiseSettings = ((NoiseGeneratorSettings)this.settings.get()).noiseSettings();
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
		NoiseChunk noiseChunk = NoiseChunk.forColumn(s, t, k, l, this.sampler, (NoiseGeneratorSettings)this.settings.get(), this.globalFluidPicker);
		noiseChunk.initializeForFirstCellX();
		noiseChunk.advanceCellX(0);

		for (int u = l - 1; u >= 0; u--) {
			noiseChunk.selectCellYZ(u, 0);

			for (int v = n - 1; v >= 0; v--) {
				int w = (k + u) * n + v;
				double f = (double)v / (double)n;
				noiseChunk.updateForY(f);
				noiseChunk.updateForX(d);
				noiseChunk.updateForZ(e);
				BlockState blockState = this.materialRule.apply(noiseChunk, i, w, j);
				BlockState blockState2 = blockState == null ? this.defaultBlock : blockState;
				if (blockStates != null) {
					int x = u * n + v;
					blockStates[x] = blockState2;
				}

				if (predicate != null && predicate.test(blockState2)) {
					return OptionalInt.of(w + 1);
				}
			}
		}

		return OptionalInt.empty();
	}

	@Override
	public void buildSurface(WorldGenRegion worldGenRegion, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
		if (!SharedConstants.debugVoidTerrain(chunkAccess.getPos())) {
			WorldGenerationContext worldGenerationContext = new WorldGenerationContext(this, worldGenRegion);
			NoiseGeneratorSettings noiseGeneratorSettings = (NoiseGeneratorSettings)this.settings.get();
			NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(
				this.sampler, () -> new Beardifier(structureFeatureManager, chunkAccess), noiseGeneratorSettings, this.globalFluidPicker, Blender.of(worldGenRegion)
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
			this.sampler,
			() -> new Beardifier(structureFeatureManager, chunkAccess),
			(NoiseGeneratorSettings)this.settings.get(),
			this.globalFluidPicker,
			Blender.of(worldGenRegion)
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
					.getGenerationSettings();
				List<Supplier<ConfiguredWorldCarver<?>>> list = biomeGenerationSettings.getCarvers(carving);
				ListIterator<Supplier<ConfiguredWorldCarver<?>>> listIterator = list.listIterator();

				while (listIterator.hasNext()) {
					int m = listIterator.nextIndex();
					ConfiguredWorldCarver<?> configuredWorldCarver = (ConfiguredWorldCarver<?>)((Supplier)listIterator.next()).get();
					worldgenRandom.setLargeFeatureSeed(l + (long)m, chunkPos2.x, chunkPos2.z);
					if (configuredWorldCarver.isStartChunk(worldgenRandom)) {
						configuredWorldCarver.carve(carvingContext, chunkAccess, biomeManager2::getBiome, worldgenRandom, aquifer, chunkPos2, carvingMask);
					}
				}
			}
		}
	}

	@Override
	public CompletableFuture<ChunkAccess> fillFromNoise(
		Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess
	) {
		NoiseSettings noiseSettings = ((NoiseGeneratorSettings)this.settings.get()).noiseSettings();
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
		NoiseGeneratorSettings noiseGeneratorSettings = (NoiseGeneratorSettings)this.settings.get();
		NoiseChunk noiseChunk = chunkAccess.getOrCreateNoiseChunk(
			this.sampler, () -> new Beardifier(structureFeatureManager, chunkAccess), noiseGeneratorSettings, this.globalFluidPicker, blender
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
						noiseChunk.updateForY(d);

						for (int x = 0; x < m; x++) {
							int y = k + q * m + x;
							int z = y & 15;
							double e = (double)x / (double)m;
							noiseChunk.updateForX(e);

							for (int aa = 0; aa < m; aa++) {
								int ab = l + r * m + aa;
								int ac = ab & 15;
								double f = (double)aa / (double)m;
								noiseChunk.updateForZ(f);
								BlockState blockState = this.materialRule.apply(noiseChunk, y, u, ab);
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

		return chunkAccess;
	}

	private BlockState debugPreliminarySurfaceLevel(NoiseChunk noiseChunk, int i, int j, int k, BlockState blockState) {
		return blockState;
	}

	@Override
	public int getGenDepth() {
		return ((NoiseGeneratorSettings)this.settings.get()).noiseSettings().height();
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
		if (!structureFeatureManager.hasAnyStructureAt(blockPos)) {
			return super.getMobsAt(biome, structureFeatureManager, mobCategory, blockPos);
		} else if (structureFeatureManager.getStructureWithPieceAt(blockPos, StructureFeature.ANCIENT_CITY).isValid()) {
			return MobSpawnSettings.EMPTY_MOB_LIST;
		} else {
			if (structureFeatureManager.getStructureWithPieceAt(blockPos, StructureFeature.SWAMP_HUT).isValid()) {
				if (mobCategory == MobCategory.MONSTER) {
					return SwamplandHutFeature.SWAMPHUT_ENEMIES;
				}

				if (mobCategory == MobCategory.CREATURE) {
					return SwamplandHutFeature.SWAMPHUT_ANIMALS;
				}
			}

			if (mobCategory == MobCategory.MONSTER) {
				if (structureFeatureManager.getStructureAt(blockPos, StructureFeature.PILLAGER_OUTPOST).isValid()) {
					return PillagerOutpostFeature.OUTPOST_ENEMIES;
				}

				if (structureFeatureManager.getStructureAt(blockPos, StructureFeature.OCEAN_MONUMENT).isValid()) {
					return OceanMonumentFeature.MONUMENT_ENEMIES;
				}

				if (structureFeatureManager.getStructureWithPieceAt(blockPos, StructureFeature.NETHER_BRIDGE).isValid()) {
					return NetherFortressFeature.FORTRESS_ENEMIES;
				}
			}

			return (mobCategory == MobCategory.UNDERGROUND_WATER_CREATURE || mobCategory == MobCategory.AXOLOTLS)
					&& structureFeatureManager.getStructureAt(blockPos, StructureFeature.OCEAN_MONUMENT).isValid()
				? MobSpawnSettings.EMPTY_MOB_LIST
				: super.getMobsAt(biome, structureFeatureManager, mobCategory, blockPos);
		}
	}

	@Override
	public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
		if (!((NoiseGeneratorSettings)this.settings.get()).disableMobGeneration()) {
			ChunkPos chunkPos = worldGenRegion.getCenter();
			Biome biome = worldGenRegion.getBiome(chunkPos.getWorldPosition().atY(worldGenRegion.getMaxBuildHeight() - 1));
			WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
			worldgenRandom.setDecorationSeed(worldGenRegion.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
			NaturalSpawner.spawnMobsForChunkGeneration(worldGenRegion, biome, chunkPos, worldgenRandom);
		}
	}

	@Deprecated
	public Optional<BlockState> topMaterial(
		CarvingContext carvingContext, Function<BlockPos, Biome> function, ChunkAccess chunkAccess, NoiseChunk noiseChunk, BlockPos blockPos, boolean bl
	) {
		return this.surfaceSystem
			.topMaterial(((NoiseGeneratorSettings)this.settings.get()).surfaceRule(), carvingContext, function, chunkAccess, noiseChunk, blockPos, bl);
	}
}
