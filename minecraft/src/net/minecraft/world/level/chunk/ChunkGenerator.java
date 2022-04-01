package net.minecraft.world.level.chunk;

import com.google.common.base.Stopwatch;
import com.mojang.datafixers.Products.P1;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public abstract class ChunkGenerator implements BiomeManager.NoiseBiomeSource {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Codec<ChunkGenerator> CODEC = Registry.CHUNK_GENERATOR.byNameCodec().dispatchStable(ChunkGenerator::codec, Function.identity());
	protected final Registry<StructureSet> structureSets;
	protected final BiomeSource biomeSource;
	protected final BiomeSource runtimeBiomeSource;
	protected final Optional<HolderSet<StructureSet>> structureOverrides;
	private final Map<ConfiguredStructureFeature<?, ?>, List<StructurePlacement>> placementsForFeature = new Object2ObjectOpenHashMap<>();
	private final Map<ConcentricRingsStructurePlacement, CompletableFuture<List<ChunkPos>>> ringPositions = new Object2ObjectArrayMap<>();
	private boolean hasGeneratedPositions;
	@Deprecated
	private final long ringPlacementSeed;

	protected static final <T extends ChunkGenerator> P1<Mu<T>, Registry<StructureSet>> commonCodec(Instance<T> instance) {
		return instance.group(RegistryOps.retrieveRegistry(Registry.STRUCTURE_SET_REGISTRY).forGetter(chunkGenerator -> chunkGenerator.structureSets));
	}

	public ChunkGenerator(Registry<StructureSet> registry, Optional<HolderSet<StructureSet>> optional, BiomeSource biomeSource) {
		this(registry, optional, biomeSource, biomeSource, 0L);
	}

	public ChunkGenerator(Registry<StructureSet> registry, Optional<HolderSet<StructureSet>> optional, BiomeSource biomeSource, BiomeSource biomeSource2, long l) {
		this.structureSets = registry;
		this.biomeSource = biomeSource;
		this.runtimeBiomeSource = biomeSource2;
		this.structureOverrides = optional;
		this.ringPlacementSeed = l;
	}

	public Stream<Holder<StructureSet>> possibleStructureSets() {
		return this.structureOverrides.isPresent() ? ((HolderSet)this.structureOverrides.get()).stream() : this.structureSets.holders().map(Holder::hackyErase);
	}

	private void generatePositions() {
		Set<Holder<Biome>> set = this.runtimeBiomeSource.possibleBiomes();
		this.possibleStructureSets()
			.forEach(
				holder -> {
					StructureSet structureSet = (StructureSet)holder.value();

					for (StructureSet.StructureSelectionEntry structureSelectionEntry : structureSet.structures()) {
						((List)this.placementsForFeature.computeIfAbsent(structureSelectionEntry.structure().value(), configuredStructureFeature -> new ArrayList()))
							.add(structureSet.placement());
					}

					if (structureSet.placement() instanceof ConcentricRingsStructurePlacement concentricRingsStructurePlacement
						&& structureSet.structures().stream().anyMatch(structureSelectionEntry -> structureSelectionEntry.generatesInMatchingBiome(set::contains))) {
						this.ringPositions.put(concentricRingsStructurePlacement, this.generateRingPositions(holder, concentricRingsStructurePlacement));
					}
				}
			);
	}

	private CompletableFuture<List<ChunkPos>> generateRingPositions(
		Holder<StructureSet> holder, ConcentricRingsStructurePlacement concentricRingsStructurePlacement
	) {
		return concentricRingsStructurePlacement.count() == 0
			? CompletableFuture.completedFuture(List.of())
			: CompletableFuture.supplyAsync(
				Util.wrapThreadWithTaskName(
					"placement calculation",
					(Supplier)(() -> {
						Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
						List<ChunkPos> list = new ArrayList();
						Set<Holder<Biome>> set = (Set<Holder<Biome>>)holder.value()
							.structures()
							.stream()
							.flatMap(structureSelectionEntry -> structureSelectionEntry.structure().value().biomes().stream())
							.collect(Collectors.toSet());
						int i = concentricRingsStructurePlacement.distance();
						int j = concentricRingsStructurePlacement.count();
						int k = concentricRingsStructurePlacement.spread();
						Random random = new Random();
						random.setSeed(this.ringPlacementSeed);
						double d = random.nextDouble() * Math.PI * 2.0;
						int l = 0;
						int m = 0;

						for (int n = 0; n < j; n++) {
							double e = (double)(4 * i + i * m * 6) + (random.nextDouble() - 0.5) * (double)i * 2.5;
							int o = (int)Math.round(Math.cos(d) * e);
							int p = (int)Math.round(Math.sin(d) * e);
							Pair<BlockPos, Holder<Biome>> pair = this.biomeSource
								.findBiomeHorizontal(SectionPos.sectionToBlockCoord(o, 8), 0, SectionPos.sectionToBlockCoord(p, 8), 112, set::contains, random, this.climateSampler());
							if (pair != null) {
								BlockPos blockPos = pair.getFirst();
								o = SectionPos.blockToSectionCoord(blockPos.getX());
								p = SectionPos.blockToSectionCoord(blockPos.getZ());
							}

							list.add(new ChunkPos(o, p));
							d += (Math.PI * 2) / (double)k;
							if (++l == k) {
								m++;
								l = 0;
								k += 2 * k / (m + 1);
								k = Math.min(k, j - n);
								d += random.nextDouble() * Math.PI * 2.0;
							}
						}

						double f = (double)stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) / 1000.0;
						LOGGER.debug("Calculation for {} took {}s", holder, f);
						return list;
					})
				),
				Util.backgroundExecutor()
			);
	}

	protected abstract Codec<? extends ChunkGenerator> codec();

	public Optional<ResourceKey<Codec<? extends ChunkGenerator>>> getTypeNameForDataFixer() {
		return Registry.CHUNK_GENERATOR.getResourceKey(this.codec());
	}

	public abstract ChunkGenerator withSeed(long l);

	public CompletableFuture<ChunkAccess> createBiomes(
		Registry<Biome> registry, Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess
	) {
		return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", (Supplier)(() -> {
			chunkAccess.fillBiomesFromNoise(this.runtimeBiomeSource::getNoiseBiome, this.climateSampler());
			return chunkAccess;
		})), Util.backgroundExecutor());
	}

	public abstract Climate.Sampler climateSampler();

	@Override
	public Holder<Biome> getNoiseBiome(int i, int j, int k) {
		return this.getBiomeSource().getNoiseBiome(i, j, k, this.climateSampler());
	}

	public abstract void applyCarvers(
		WorldGenRegion worldGenRegion,
		long l,
		BiomeManager biomeManager,
		StructureFeatureManager structureFeatureManager,
		ChunkAccess chunkAccess,
		GenerationStep.Carving carving
	);

	@Nullable
	public Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> findNearestMapFeature(
		ServerLevel serverLevel, HolderSet<ConfiguredStructureFeature<?, ?>> holderSet, BlockPos blockPos, int i, boolean bl
	) {
		Set<Holder<Biome>> set = (Set<Holder<Biome>>)holderSet.stream()
			.flatMap(holder -> ((ConfiguredStructureFeature)holder.value()).biomes().stream())
			.collect(Collectors.toSet());
		if (set.isEmpty()) {
			return null;
		} else {
			Set<Holder<Biome>> set2 = this.runtimeBiomeSource.possibleBiomes();
			if (Collections.disjoint(set2, set)) {
				return null;
			} else {
				Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> pair = null;
				double d = Double.MAX_VALUE;
				Map<StructurePlacement, Set<Holder<ConfiguredStructureFeature<?, ?>>>> map = new Object2ObjectArrayMap<>();

				for (Holder<ConfiguredStructureFeature<?, ?>> holder : holderSet) {
					if (!set2.stream().noneMatch(holder.value().biomes()::contains)) {
						for (StructurePlacement structurePlacement : this.getPlacementsForFeature(holder)) {
							((Set)map.computeIfAbsent(structurePlacement, structurePlacementx -> new ObjectArraySet())).add(holder);
						}
					}
				}

				List<Entry<StructurePlacement, Set<Holder<ConfiguredStructureFeature<?, ?>>>>> list = new ArrayList(map.size());

				for (Entry<StructurePlacement, Set<Holder<ConfiguredStructureFeature<?, ?>>>> entry : map.entrySet()) {
					StructurePlacement structurePlacement = (StructurePlacement)entry.getKey();
					if (structurePlacement instanceof ConcentricRingsStructurePlacement) {
						ConcentricRingsStructurePlacement concentricRingsStructurePlacement = (ConcentricRingsStructurePlacement)structurePlacement;
						BlockPos blockPos2 = this.getNearestGeneratedStructure(blockPos, concentricRingsStructurePlacement);
						double e = blockPos.distSqr(blockPos2);
						if (e < d) {
							d = e;
							pair = Pair.of(blockPos2, (Holder<ConfiguredStructureFeature<?, ?>>)((Set)entry.getValue()).iterator().next());
						}
					} else if (structurePlacement instanceof RandomSpreadStructurePlacement) {
						list.add(entry);
					}
				}

				if (!list.isEmpty()) {
					int j = SectionPos.blockToSectionCoord(blockPos.getX());
					int k = SectionPos.blockToSectionCoord(blockPos.getZ());

					for (int l = 0; l <= i; l++) {
						boolean bl2 = false;

						for (Entry<StructurePlacement, Set<Holder<ConfiguredStructureFeature<?, ?>>>> entry2 : list) {
							RandomSpreadStructurePlacement randomSpreadStructurePlacement = (RandomSpreadStructurePlacement)entry2.getKey();
							Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> pair2 = getNearestGeneratedStructure(
								(Set<Holder<ConfiguredStructureFeature<?, ?>>>)entry2.getValue(),
								serverLevel,
								serverLevel.structureFeatureManager(),
								j,
								k,
								l,
								bl,
								serverLevel.getSeed(),
								randomSpreadStructurePlacement
							);
							if (pair2 != null) {
								bl2 = true;
								double f = blockPos.distSqr(pair2.getFirst());
								if (f < d) {
									d = f;
									pair = pair2;
								}
							}
						}

						if (bl2) {
							return pair;
						}
					}
				}

				return pair;
			}
		}
	}

	@Nullable
	private BlockPos getNearestGeneratedStructure(BlockPos blockPos, ConcentricRingsStructurePlacement concentricRingsStructurePlacement) {
		List<ChunkPos> list = this.getRingPositionsFor(concentricRingsStructurePlacement);
		if (list == null) {
			throw new IllegalStateException("Somehow tried to find structures for a placement that doesn't exist");
		} else {
			BlockPos blockPos2 = null;
			double d = Double.MAX_VALUE;
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (ChunkPos chunkPos : list) {
				mutableBlockPos.set(SectionPos.sectionToBlockCoord(chunkPos.x, 8), 32, SectionPos.sectionToBlockCoord(chunkPos.z, 8));
				double e = mutableBlockPos.distSqr(blockPos);
				if (blockPos2 == null) {
					blockPos2 = new BlockPos(mutableBlockPos);
					d = e;
				} else if (e < d) {
					blockPos2 = new BlockPos(mutableBlockPos);
					d = e;
				}
			}

			return blockPos2;
		}
	}

	@Nullable
	private static Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> getNearestGeneratedStructure(
		Set<Holder<ConfiguredStructureFeature<?, ?>>> set,
		LevelReader levelReader,
		StructureFeatureManager structureFeatureManager,
		int i,
		int j,
		int k,
		boolean bl,
		long l,
		RandomSpreadStructurePlacement randomSpreadStructurePlacement
	) {
		int m = randomSpreadStructurePlacement.spacing();

		for (int n = -k; n <= k; n++) {
			boolean bl2 = n == -k || n == k;

			for (int o = -k; o <= k; o++) {
				boolean bl3 = o == -k || o == k;
				if (bl2 || bl3) {
					int p = i + m * n;
					int q = j + m * o;
					ChunkPos chunkPos = randomSpreadStructurePlacement.getPotentialFeatureChunk(l, p, q);

					for (Holder<ConfiguredStructureFeature<?, ?>> holder : set) {
						StructureCheckResult structureCheckResult = structureFeatureManager.checkStructurePresence(chunkPos, holder.value(), bl);
						if (structureCheckResult != StructureCheckResult.START_NOT_PRESENT) {
							if (!bl && structureCheckResult == StructureCheckResult.START_PRESENT) {
								return Pair.of(StructureFeature.getLocatePos(randomSpreadStructurePlacement, chunkPos), holder);
							}

							ChunkAccess chunkAccess = levelReader.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
							StructureStart structureStart = structureFeatureManager.getStartForFeature(SectionPos.bottomOf(chunkAccess), holder.value(), chunkAccess);
							if (structureStart != null && structureStart.isValid()) {
								if (bl && structureStart.canBeReferenced()) {
									structureFeatureManager.addReference(structureStart);
									return Pair.of(StructureFeature.getLocatePos(randomSpreadStructurePlacement, structureStart.getChunkPos()), holder);
								}

								if (!bl) {
									return Pair.of(StructureFeature.getLocatePos(randomSpreadStructurePlacement, structureStart.getChunkPos()), holder);
								}
							}
						}
					}
				}
			}
		}

		return null;
	}

	public void applyBiomeDecoration(WorldGenLevel worldGenLevel, ChunkAccess chunkAccess, StructureFeatureManager structureFeatureManager) {
		ChunkPos chunkPos = chunkAccess.getPos();
		if (!SharedConstants.debugVoidTerrain(chunkPos)) {
			SectionPos sectionPos = SectionPos.of(chunkPos, worldGenLevel.getMinSection());
			BlockPos blockPos = sectionPos.origin();
			Registry<ConfiguredStructureFeature<?, ?>> registry = worldGenLevel.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
			Map<Integer, List<ConfiguredStructureFeature<?, ?>>> map = (Map<Integer, List<ConfiguredStructureFeature<?, ?>>>)registry.stream()
				.collect(Collectors.groupingBy(configuredStructureFeature -> configuredStructureFeature.feature.step().ordinal()));
			List<BiomeSource.StepFeatureData> list = this.biomeSource.featuresPerStep();
			WorldgenRandom worldgenRandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.seedUniquifier()));
			long l = worldgenRandom.setDecorationSeed(worldGenLevel.getSeed(), blockPos.getX(), blockPos.getZ());
			Set<Biome> set = new ObjectArraySet<>();
			if (this instanceof FlatLevelSource) {
				this.biomeSource.possibleBiomes().stream().map(Holder::value).forEach(set::add);
			} else {
				ChunkPos.rangeClosed(sectionPos.chunk(), 1).forEach(chunkPosx -> {
					ChunkAccess chunkAccessx = worldGenLevel.getChunk(chunkPosx.x, chunkPosx.z);

					for (LevelChunkSection levelChunkSection : chunkAccessx.getSections()) {
						levelChunkSection.getBiomes().getAll(holder -> set.add((Biome)holder.value()));
					}
				});
				set.retainAll((Collection)this.biomeSource.possibleBiomes().stream().map(Holder::value).collect(Collectors.toSet()));
			}

			int i = list.size();

			try {
				Registry<PlacedFeature> registry2 = worldGenLevel.registryAccess().registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
				int j = Math.max(GenerationStep.Decoration.values().length, i);

				for (int k = 0; k < j; k++) {
					int m = 0;
					if (structureFeatureManager.shouldGenerateFeatures()) {
						for (ConfiguredStructureFeature<?, ?> configuredStructureFeature : (List)map.getOrDefault(k, Collections.emptyList())) {
							worldgenRandom.setFeatureSeed(l, m, k);
							Supplier<String> supplier = () -> (String)registry.getResourceKey(configuredStructureFeature)
									.map(Object::toString)
									.orElseGet(configuredStructureFeature::toString);

							try {
								worldGenLevel.setCurrentlyGenerating(supplier);
								structureFeatureManager.startsForFeature(sectionPos, configuredStructureFeature)
									.forEach(
										structureStart -> structureStart.placeInChunk(worldGenLevel, structureFeatureManager, this, worldgenRandom, getWritableArea(chunkAccess), chunkPos)
									);
							} catch (Exception var29) {
								CrashReport crashReport = CrashReport.forThrowable(var29, "Feature placement");
								crashReport.addCategory("Feature").setDetail("Description", supplier::get);
								throw new ReportedException(crashReport);
							}

							m++;
						}
					}

					if (k < i) {
						IntSet intSet = new IntArraySet();

						for (Biome biome : set) {
							List<HolderSet<PlacedFeature>> list3 = biome.getGenerationSettings().features();
							if (k < list3.size()) {
								HolderSet<PlacedFeature> holderSet = (HolderSet<PlacedFeature>)list3.get(k);
								BiomeSource.StepFeatureData stepFeatureData = (BiomeSource.StepFeatureData)list.get(k);
								holderSet.stream().map(Holder::value).forEach(placedFeaturex -> intSet.add(stepFeatureData.indexMapping().applyAsInt(placedFeaturex)));
							}
						}

						int n = intSet.size();
						int[] is = intSet.toIntArray();
						Arrays.sort(is);
						BiomeSource.StepFeatureData stepFeatureData2 = (BiomeSource.StepFeatureData)list.get(k);

						for (int o = 0; o < n; o++) {
							int p = is[o];
							PlacedFeature placedFeature = (PlacedFeature)stepFeatureData2.features().get(p);
							Supplier<String> supplier2 = () -> (String)registry2.getResourceKey(placedFeature).map(Object::toString).orElseGet(placedFeature::toString);
							worldgenRandom.setFeatureSeed(l, p, k);

							try {
								worldGenLevel.setCurrentlyGenerating(supplier2);
								placedFeature.placeWithBiomeCheck(worldGenLevel, this, worldgenRandom, blockPos);
							} catch (Exception var30) {
								CrashReport crashReport2 = CrashReport.forThrowable(var30, "Feature placement");
								crashReport2.addCategory("Feature").setDetail("Description", supplier2::get);
								throw new ReportedException(crashReport2);
							}
						}
					}
				}

				worldGenLevel.setCurrentlyGenerating(null);
			} catch (Exception var31) {
				CrashReport crashReport3 = CrashReport.forThrowable(var31, "Biome decoration");
				crashReport3.addCategory("Generation").setDetail("CenterX", chunkPos.x).setDetail("CenterZ", chunkPos.z).setDetail("Seed", l);
				throw new ReportedException(crashReport3);
			}
		}
	}

	public boolean hasFeatureChunkInRange(ResourceKey<StructureSet> resourceKey, long l, int i, int j, int k) {
		StructureSet structureSet = this.structureSets.get(resourceKey);
		if (structureSet == null) {
			return false;
		} else {
			StructurePlacement structurePlacement = structureSet.placement();

			for (int m = i - k; m <= i + k; m++) {
				for (int n = j - k; n <= j + k; n++) {
					if (structurePlacement.isFeatureChunk(this, l, m, n)) {
						return true;
					}
				}
			}

			return false;
		}
	}

	private static BoundingBox getWritableArea(ChunkAccess chunkAccess) {
		ChunkPos chunkPos = chunkAccess.getPos();
		int i = chunkPos.getMinBlockX();
		int j = chunkPos.getMinBlockZ();
		LevelHeightAccessor levelHeightAccessor = chunkAccess.getHeightAccessorForGeneration();
		int k = levelHeightAccessor.getMinBuildHeight() + 1;
		int l = levelHeightAccessor.getMaxBuildHeight() - 1;
		return new BoundingBox(i, k, j, i + 15, l, j + 15);
	}

	public abstract void buildSurface(WorldGenRegion worldGenRegion, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess);

	public abstract void spawnOriginalMobs(WorldGenRegion worldGenRegion);

	public int getSpawnHeight(LevelHeightAccessor levelHeightAccessor) {
		return 64;
	}

	public BiomeSource getBiomeSource() {
		return this.runtimeBiomeSource;
	}

	public abstract int getGenDepth();

	public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(
		Holder<Biome> holder, StructureFeatureManager structureFeatureManager, MobCategory mobCategory, BlockPos blockPos
	) {
		Map<ConfiguredStructureFeature<?, ?>, LongSet> map = structureFeatureManager.getAllStructuresAt(blockPos);

		for (Entry<ConfiguredStructureFeature<?, ?>, LongSet> entry : map.entrySet()) {
			ConfiguredStructureFeature<?, ?> configuredStructureFeature = (ConfiguredStructureFeature<?, ?>)entry.getKey();
			StructureSpawnOverride structureSpawnOverride = (StructureSpawnOverride)configuredStructureFeature.spawnOverrides.get(mobCategory);
			if (structureSpawnOverride != null) {
				MutableBoolean mutableBoolean = new MutableBoolean(false);
				Predicate<StructureStart> predicate = structureSpawnOverride.boundingBox() == StructureSpawnOverride.BoundingBoxType.PIECE
					? structureStart -> structureFeatureManager.structureHasPieceAt(blockPos, structureStart)
					: structureStart -> structureStart.getBoundingBox().isInside(blockPos);
				structureFeatureManager.fillStartsForFeature(configuredStructureFeature, (LongSet)entry.getValue(), structureStart -> {
					if (mutableBoolean.isFalse() && predicate.test(structureStart)) {
						mutableBoolean.setTrue();
					}
				});
				if (mutableBoolean.isTrue()) {
					return structureSpawnOverride.spawns();
				}
			}
		}

		return holder.value().getMobSettings().getMobs(mobCategory);
	}

	public static Stream<ConfiguredStructureFeature<?, ?>> allConfigurations(
		Registry<ConfiguredStructureFeature<?, ?>> registry, StructureFeature<?> structureFeature
	) {
		return registry.stream().filter(configuredStructureFeature -> configuredStructureFeature.feature == structureFeature);
	}

	public void createStructures(
		RegistryAccess registryAccess, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, StructureManager structureManager, long l
	) {
		ChunkPos chunkPos = chunkAccess.getPos();
		SectionPos sectionPos = SectionPos.bottomOf(chunkAccess);
		this.possibleStructureSets()
			.forEach(
				holder -> {
					StructurePlacement structurePlacement = ((StructureSet)holder.value()).placement();
					List<StructureSet.StructureSelectionEntry> list = ((StructureSet)holder.value()).structures();

					for (StructureSet.StructureSelectionEntry structureSelectionEntry : list) {
						StructureStart structureStart = structureFeatureManager.getStartForFeature(sectionPos, structureSelectionEntry.structure().value(), chunkAccess);
						if (structureStart != null && structureStart.isValid()) {
							return;
						}
					}

					if (structurePlacement.isFeatureChunk(this, l, chunkPos.x, chunkPos.z)) {
						if (list.size() == 1) {
							this.tryGenerateStructure(
								(StructureSet.StructureSelectionEntry)list.get(0), structureFeatureManager, registryAccess, structureManager, l, chunkAccess, chunkPos, sectionPos
							);
						} else {
							ArrayList<StructureSet.StructureSelectionEntry> arrayList = new ArrayList(list.size());
							arrayList.addAll(list);
							WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
							worldgenRandom.setLargeFeatureSeed(l, chunkPos.x, chunkPos.z);
							int i = 0;

							for (StructureSet.StructureSelectionEntry structureSelectionEntry2 : arrayList) {
								i += structureSelectionEntry2.weight();
							}

							while (!arrayList.isEmpty()) {
								int j = worldgenRandom.nextInt(i);
								int k = 0;

								for (StructureSet.StructureSelectionEntry structureSelectionEntry3 : arrayList) {
									j -= structureSelectionEntry3.weight();
									if (j < 0) {
										break;
									}

									k++;
								}

								StructureSet.StructureSelectionEntry structureSelectionEntry4 = (StructureSet.StructureSelectionEntry)arrayList.get(k);
								if (this.tryGenerateStructure(structureSelectionEntry4, structureFeatureManager, registryAccess, structureManager, l, chunkAccess, chunkPos, sectionPos)
									)
								 {
									return;
								}

								arrayList.remove(k);
								i -= structureSelectionEntry4.weight();
							}
						}
					}
				}
			);
	}

	private boolean tryGenerateStructure(
		StructureSet.StructureSelectionEntry structureSelectionEntry,
		StructureFeatureManager structureFeatureManager,
		RegistryAccess registryAccess,
		StructureManager structureManager,
		long l,
		ChunkAccess chunkAccess,
		ChunkPos chunkPos,
		SectionPos sectionPos
	) {
		ConfiguredStructureFeature<?, ?> configuredStructureFeature = structureSelectionEntry.structure().value();
		int i = fetchReferences(structureFeatureManager, chunkAccess, sectionPos, configuredStructureFeature);
		HolderSet<Biome> holderSet = configuredStructureFeature.biomes();
		Predicate<Holder<Biome>> predicate = holder -> holderSet.contains(this.adjustBiome(holder));
		StructureStart structureStart = configuredStructureFeature.generate(
			registryAccess, this, this.biomeSource, structureManager, l, chunkPos, i, chunkAccess, predicate
		);
		if (structureStart.isValid()) {
			structureFeatureManager.setStartForFeature(sectionPos, configuredStructureFeature, structureStart, chunkAccess);
			return true;
		} else {
			return false;
		}
	}

	private static int fetchReferences(
		StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, SectionPos sectionPos, ConfiguredStructureFeature<?, ?> configuredStructureFeature
	) {
		StructureStart structureStart = structureFeatureManager.getStartForFeature(sectionPos, configuredStructureFeature, chunkAccess);
		return structureStart != null ? structureStart.getReferences() : 0;
	}

	protected Holder<Biome> adjustBiome(Holder<Biome> holder) {
		return holder;
	}

	public void createReferences(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
		int i = 8;
		ChunkPos chunkPos = chunkAccess.getPos();
		int j = chunkPos.x;
		int k = chunkPos.z;
		int l = chunkPos.getMinBlockX();
		int m = chunkPos.getMinBlockZ();
		SectionPos sectionPos = SectionPos.bottomOf(chunkAccess);

		for (int n = j - 8; n <= j + 8; n++) {
			for (int o = k - 8; o <= k + 8; o++) {
				long p = ChunkPos.asLong(n, o);

				for (StructureStart structureStart : worldGenLevel.getChunk(n, o).getAllStarts().values()) {
					try {
						if (structureStart.isValid() && structureStart.getBoundingBox().intersects(l, m, l + 15, m + 15)) {
							structureFeatureManager.addReferenceForFeature(sectionPos, structureStart.getFeature(), p, chunkAccess);
							DebugPackets.sendStructurePacket(worldGenLevel, structureStart);
						}
					} catch (Exception var21) {
						CrashReport crashReport = CrashReport.forThrowable(var21, "Generating structure reference");
						CrashReportCategory crashReportCategory = crashReport.addCategory("Structure");
						Optional<? extends Registry<ConfiguredStructureFeature<?, ?>>> optional = worldGenLevel.registryAccess()
							.registry(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
						crashReportCategory.setDetail(
							"Id", (CrashReportDetail<String>)(() -> (String)optional.map(registry -> registry.getKey(structureStart.getFeature()).toString()).orElse("UNKNOWN"))
						);
						crashReportCategory.setDetail(
							"Name", (CrashReportDetail<String>)(() -> Registry.STRUCTURE_FEATURE.getKey(structureStart.getFeature().feature).toString())
						);
						crashReportCategory.setDetail("Class", (CrashReportDetail<String>)(() -> structureStart.getFeature().getClass().getCanonicalName()));
						throw new ReportedException(crashReport);
					}
				}
			}
		}
	}

	public abstract CompletableFuture<ChunkAccess> fillFromNoise(
		Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess
	);

	public abstract int getSeaLevel();

	public abstract int getMinY();

	public abstract int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor);

	public abstract NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor);

	public int getFirstFreeHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor) {
		return this.getBaseHeight(i, j, types, levelHeightAccessor);
	}

	public int getFirstOccupiedHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor) {
		return this.getBaseHeight(i, j, types, levelHeightAccessor) - 1;
	}

	public void ensureStructuresGenerated() {
		if (!this.hasGeneratedPositions) {
			this.generatePositions();
			this.hasGeneratedPositions = true;
		}
	}

	@Nullable
	public List<ChunkPos> getRingPositionsFor(ConcentricRingsStructurePlacement concentricRingsStructurePlacement) {
		this.ensureStructuresGenerated();
		CompletableFuture<List<ChunkPos>> completableFuture = (CompletableFuture<List<ChunkPos>>)this.ringPositions.get(concentricRingsStructurePlacement);
		return completableFuture != null ? (List)completableFuture.join() : null;
	}

	private List<StructurePlacement> getPlacementsForFeature(Holder<ConfiguredStructureFeature<?, ?>> holder) {
		this.ensureStructuresGenerated();
		return (List<StructurePlacement>)this.placementsForFeature.getOrDefault(holder.value(), List.of());
	}

	public abstract void addDebugScreenInfo(List<String> list, BlockPos blockPos);

	static {
		Registry.register(Registry.CHUNK_GENERATOR, "noise", NoiseBasedChunkGenerator.CODEC);
		Registry.register(Registry.CHUNK_GENERATOR, "flat", FlatLevelSource.CODEC);
		Registry.register(Registry.CHUNK_GENERATOR, "debug", DebugLevelSource.CODEC);
	}
}
