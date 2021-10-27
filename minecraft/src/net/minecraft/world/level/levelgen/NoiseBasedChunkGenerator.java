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
import net.minecraft.world.ticks.ScheduledTick;

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
	private static final int BEDROCK_LAYER_HEIGHT = 5;
	private final int cellHeight;
	private final int cellWidth;
	private final int cellCountX;
	private final int cellCountY;
	private final int cellCountZ;
	protected final BlockState defaultBlock;
	private final Registry<NormalNoise.NoiseParameters> noises;
	private final long seed;
	protected final Supplier<NoiseGeneratorSettings> settings;
	private final int height;
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
		NoiseGeneratorSettings noiseGeneratorSettings = (NoiseGeneratorSettings)supplier.get();
		this.settings = supplier;
		NoiseSettings noiseSettings = noiseGeneratorSettings.noiseSettings();
		this.height = noiseSettings.height();
		this.cellHeight = QuartPos.toBlock(noiseSettings.noiseSizeVertical());
		this.cellWidth = QuartPos.toBlock(noiseSettings.noiseSizeHorizontal());
		this.defaultBlock = noiseGeneratorSettings.getDefaultBlock();
		this.cellCountX = 16 / this.cellWidth;
		this.cellCountY = noiseSettings.height() / this.cellHeight;
		this.cellCountZ = 16 / this.cellWidth;
		this.sampler = new NoiseSampler(
			this.cellWidth,
			this.cellHeight,
			this.cellCountY,
			noiseSettings,
			noiseGeneratorSettings.isNoiseCavesEnabled(),
			l,
			registry,
			noiseGeneratorSettings.getRandomSource()
		);
		Builder<WorldGenMaterialRule> builder = ImmutableList.builder();
		RandomSource randomSource = noiseGeneratorSettings.createRandomSource(l);
		int i = noiseGeneratorSettings.getBedrockFloorPosition();
		if (i > -5 && i < this.height) {
			int j = this.getMinY() + i;
			builder.add(new VerticalGradientRule(randomSource.forkPositional(), Blocks.BEDROCK.defaultBlockState(), null, j, j + 5));
		}

		int j = noiseGeneratorSettings.getBedrockRoofPosition();
		if (j > -5 && j < this.height) {
			int k = this.getMinY() + this.height - 1 + j;
			builder.add(new VerticalGradientRule(randomSource.forkPositional(), null, Blocks.BEDROCK.defaultBlockState(), k - 5, k));
		}

		builder.add(NoiseChunk::updateNoiseAndGenerateBaseState);
		builder.add(NoiseChunk::oreVeinify);
		if (noiseGeneratorSettings.isDeepslateEnabled()) {
			builder.add(new VerticalGradientRule(this.sampler.getDepthBasedLayerPositionalRandom(), Blocks.DEEPSLATE.defaultBlockState(), null, -8, 0));
		}

		this.materialRule = new MaterialRuleList(builder.build());
		Aquifer.FluidStatus fluidStatus = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
		Aquifer.FluidStatus fluidStatus2 = new Aquifer.FluidStatus(noiseGeneratorSettings.seaLevel(), noiseGeneratorSettings.getDefaultFluid());
		Aquifer.FluidStatus fluidStatus3 = new Aquifer.FluidStatus(noiseGeneratorSettings.noiseSettings().minY() - 1, Blocks.AIR.defaultBlockState());
		this.globalFluidPicker = (ix, jx, k) -> jx < -54 ? fluidStatus : fluidStatus2;
		this.surfaceSystem = new SurfaceSystem(
			this.sampler, registry, this.defaultBlock, noiseGeneratorSettings.seaLevel(), l, noiseGeneratorSettings.getRandomSource()
		);
	}

	@Override
	public CompletableFuture<ChunkAccess> createBiomes(
		Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess
	) {
		return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", (Supplier)(() -> {
			this.doCreateBiomes(blender, structureFeatureManager, chunkAccess);
			return chunkAccess;
		})), Util.backgroundExecutor());
	}

	private void doCreateBiomes(Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
		ChunkPos chunkPos = chunkAccess.getPos();
		int i = Math.max(((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY(), chunkAccess.getMinBuildHeight());
		int j = Math.min(
			((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY() + ((NoiseGeneratorSettings)this.settings.get()).noiseSettings().height(),
			chunkAccess.getMaxBuildHeight()
		);
		int k = Mth.intFloorDiv(i, this.cellHeight);
		int l = Mth.intFloorDiv(j - i, this.cellHeight);
		NoiseChunk noiseChunk = chunkAccess.noiseChunk(
			k,
			l,
			chunkPos.getMinBlockX(),
			chunkPos.getMinBlockZ(),
			this.cellWidth,
			this.cellHeight,
			this.sampler,
			() -> new Beardifier(structureFeatureManager, chunkAccess),
			this.settings,
			this.globalFluidPicker,
			blender
		);
		chunkAccess.fillBiomesFromNoise(this.runtimeBiomeSource, (ix, jx, kx) -> this.sampler.target(ix, jx, kx, noiseChunk.noiseData(ix, kx)));
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

	private OptionalInt iterateNoiseColumn(int i, int j, @Nullable BlockState[] blockStates, @Nullable Predicate<BlockState> predicate, int k, int l) {
		int m = Math.floorDiv(i, this.cellWidth);
		int n = Math.floorDiv(j, this.cellWidth);
		int o = Math.floorMod(i, this.cellWidth);
		int p = Math.floorMod(j, this.cellWidth);
		int q = m * this.cellWidth;
		int r = n * this.cellWidth;
		double d = (double)o / (double)this.cellWidth;
		double e = (double)p / (double)this.cellWidth;
		NoiseChunk noiseChunk = new NoiseChunk(
			this.cellWidth, this.cellHeight, 1, l, k, this.sampler, q, r, (ix, jx, kx) -> 0.0, this.settings, this.globalFluidPicker, Blender.empty()
		);
		noiseChunk.initializeForFirstCellX();
		noiseChunk.advanceCellX(0);

		for (int s = l - 1; s >= 0; s--) {
			noiseChunk.selectCellYZ(s, 0);

			for (int t = this.cellHeight - 1; t >= 0; t--) {
				int u = (k + s) * this.cellHeight + t;
				double f = (double)t / (double)this.cellHeight;
				noiseChunk.updateForY(f);
				noiseChunk.updateForX(d);
				noiseChunk.updateForZ(e);
				BlockState blockState = this.materialRule.apply(noiseChunk, i, u, j);
				BlockState blockState2 = blockState == null ? this.defaultBlock : blockState;
				if (blockStates != null) {
					int v = s * this.cellHeight + t;
					blockStates[v] = blockState2;
				}

				if (predicate != null && predicate.test(blockState2)) {
					return OptionalInt.of(u + 1);
				}
			}
		}

		return OptionalInt.empty();
	}

	@Override
	public void buildSurface(WorldGenRegion worldGenRegion, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
		ChunkPos chunkPos = chunkAccess.getPos();
		if (!SharedConstants.debugVoidTerrain(chunkPos.getMinBlockX(), chunkPos.getMinBlockZ())) {
			int i = chunkPos.getMinBlockX();
			int j = chunkPos.getMinBlockZ();
			int k = Math.max(((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY(), chunkAccess.getMinBuildHeight());
			int l = Math.min(
				((NoiseGeneratorSettings)this.settings.get()).noiseSettings().minY() + ((NoiseGeneratorSettings)this.settings.get()).noiseSettings().height(),
				chunkAccess.getMaxBuildHeight()
			);
			int m = Mth.intFloorDiv(k, this.cellHeight);
			int n = Mth.intFloorDiv(l - k, this.cellHeight);
			WorldGenerationContext worldGenerationContext = new WorldGenerationContext(this, worldGenRegion);
			NoiseChunk noiseChunk = chunkAccess.noiseChunk(
				m,
				n,
				i,
				j,
				this.cellWidth,
				this.cellHeight,
				this.sampler,
				() -> new Beardifier(structureFeatureManager, chunkAccess),
				this.settings,
				this.globalFluidPicker,
				Blender.of(worldGenRegion)
			);
			this.surfaceSystem
				.buildSurface(
					worldGenRegion.getBiomeManager(),
					worldGenRegion.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY),
					((NoiseGeneratorSettings)this.settings.get()).useLegacyRandomSource(),
					worldGenerationContext,
					chunkAccess,
					noiseChunk,
					((NoiseGeneratorSettings)this.settings.get()).surfaceRule()
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
		CarvingContext carvingContext = new CarvingContext(this, worldGenRegion.registryAccess(), chunkAccess);
		ChunkPos chunkPos2 = chunkAccess.getPos();
		NoiseSettings noiseSettings = ((NoiseGeneratorSettings)this.settings.get()).noiseSettings();
		int j = Math.max(noiseSettings.minY(), chunkAccess.getMinBuildHeight());
		int k = Math.min(noiseSettings.minY() + noiseSettings.height(), chunkAccess.getMaxBuildHeight());
		int m = Mth.intFloorDiv(j, this.cellHeight);
		int n = Mth.intFloorDiv(k - j, this.cellHeight);
		NoiseChunk noiseChunk = chunkAccess.noiseChunk(
			m,
			n,
			chunkPos2.getMinBlockX(),
			chunkPos2.getMinBlockZ(),
			this.cellWidth,
			this.cellHeight,
			this.sampler,
			() -> new Beardifier(structureFeatureManager, chunkAccess),
			this.settings,
			this.globalFluidPicker,
			Blender.of(worldGenRegion)
		);
		Aquifer aquifer = noiseChunk.aquifer();
		CarvingMask carvingMask = ((ProtoChunk)chunkAccess).getOrCreateCarvingMask(carving);

		for (int o = -8; o <= 8; o++) {
			for (int p = -8; p <= 8; p++) {
				ChunkPos chunkPos3 = new ChunkPos(chunkPos.x + o, chunkPos.z + p);
				ChunkAccess chunkAccess2 = worldGenRegion.getChunk(chunkPos3.x, chunkPos3.z);
				BiomeGenerationSettings biomeGenerationSettings = chunkAccess2.carverBiome(
						() -> this.biomeSource
								.getNoiseBiome(QuartPos.fromBlock(chunkPos3.getMinBlockX()), 0, QuartPos.fromBlock(chunkPos3.getMinBlockZ()), this.climateSampler())
					)
					.getGenerationSettings();
				List<Supplier<ConfiguredWorldCarver<?>>> list = biomeGenerationSettings.getCarvers(carving);
				ListIterator<Supplier<ConfiguredWorldCarver<?>>> listIterator = list.listIterator();

				while (listIterator.hasNext()) {
					int q = listIterator.nextIndex();
					ConfiguredWorldCarver<?> configuredWorldCarver = (ConfiguredWorldCarver<?>)((Supplier)listIterator.next()).get();
					worldgenRandom.setLargeFeatureSeed(l + (long)q, chunkPos3.x, chunkPos3.z);
					if (configuredWorldCarver.isStartChunk(worldgenRandom)) {
						configuredWorldCarver.carve(carvingContext, chunkAccess, biomeManager2::getBiome, worldgenRandom, aquifer, chunkPos3, carvingMask);
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
		Heightmap heightmap = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
		Heightmap heightmap2 = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
		ChunkPos chunkPos = chunkAccess.getPos();
		int k = chunkPos.getMinBlockX();
		int l = chunkPos.getMinBlockZ();
		NoiseChunk noiseChunk = chunkAccess.noiseChunk(
			i,
			j,
			k,
			l,
			this.cellWidth,
			this.cellHeight,
			this.sampler,
			() -> new Beardifier(structureFeatureManager, chunkAccess),
			this.settings,
			this.globalFluidPicker,
			blender
		);
		Aquifer aquifer = noiseChunk.aquifer();
		noiseChunk.initializeForFirstCellX();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int m = 0; m < this.cellCountX; m++) {
			noiseChunk.advanceCellX(m);

			for (int n = 0; n < this.cellCountZ; n++) {
				LevelChunkSection levelChunkSection = chunkAccess.getSection(chunkAccess.getSectionsCount() - 1);

				for (int o = j - 1; o >= 0; o--) {
					noiseChunk.selectCellYZ(o, n);

					for (int p = this.cellHeight - 1; p >= 0; p--) {
						int q = (i + o) * this.cellHeight + p;
						int r = q & 15;
						int s = chunkAccess.getSectionIndex(q);
						if (chunkAccess.getSectionIndex(levelChunkSection.bottomBlockY()) != s) {
							levelChunkSection = chunkAccess.getSection(s);
						}

						double d = (double)p / (double)this.cellHeight;
						noiseChunk.updateForY(d);

						for (int t = 0; t < this.cellWidth; t++) {
							int u = k + m * this.cellWidth + t;
							int v = u & 15;
							double e = (double)t / (double)this.cellWidth;
							noiseChunk.updateForX(e);

							for (int w = 0; w < this.cellWidth; w++) {
								int x = l + n * this.cellWidth + w;
								int y = x & 15;
								double f = (double)w / (double)this.cellWidth;
								noiseChunk.updateForZ(f);
								BlockState blockState = this.materialRule.apply(noiseChunk, u, q, x);
								if (blockState == null) {
									blockState = this.defaultBlock;
								}

								blockState = this.debugPreliminarySurfaceLevel(q, u, x, blockState);
								if (blockState != AIR && !SharedConstants.debugVoidTerrain(u, x)) {
									if (blockState.getLightEmission() != 0 && chunkAccess instanceof ProtoChunk) {
										mutableBlockPos.set(u, q, x);
										((ProtoChunk)chunkAccess).addLight(mutableBlockPos);
									}

									levelChunkSection.setBlockState(v, r, y, blockState, false);
									heightmap.update(v, q, y, blockState);
									heightmap2.update(v, q, y, blockState);
									if (aquifer.shouldScheduleFluidUpdate() && !blockState.getFluidState().isEmpty()) {
										mutableBlockPos.set(u, q, x);
										chunkAccess.getFluidTicks().schedule(ScheduledTick.worldgen(blockState.getFluidState().getType(), mutableBlockPos, 0L));
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

	private BlockState debugPreliminarySurfaceLevel(int i, int j, int k, BlockState blockState) {
		return blockState;
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
		if (!structureFeatureManager.hasAnyStructureAt(blockPos)) {
			return super.getMobsAt(biome, structureFeatureManager, mobCategory, blockPos);
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
			Biome biome = worldGenRegion.getBiome(chunkPos.getWorldPosition());
			WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
			worldgenRandom.setDecorationSeed(worldGenRegion.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
			NaturalSpawner.spawnMobsForChunkGeneration(worldGenRegion, biome, chunkPos, worldgenRandom);
		}
	}

	@Deprecated
	public Optional<BlockState> topMaterial(CarvingContext carvingContext, Biome biome, ChunkAccess chunkAccess, BlockPos blockPos, boolean bl) {
		ResourceKey<Biome> resourceKey = (ResourceKey<Biome>)carvingContext.registryAccess()
			.registryOrThrow(Registry.BIOME_REGISTRY)
			.getResourceKey(biome)
			.orElseThrow(() -> new IllegalStateException("Unregistered biome: " + biome));
		return this.surfaceSystem
			.topMaterial(((NoiseGeneratorSettings)this.settings.get()).surfaceRule(), carvingContext, biome, resourceKey, chunkAccess, blockPos, bl);
	}
}
