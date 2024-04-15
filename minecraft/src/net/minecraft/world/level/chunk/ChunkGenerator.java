package net.minecraft.world.level.chunk;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.apache.commons.lang3.mutable.MutableBoolean;

public abstract class ChunkGenerator {
	public static final Codec<ChunkGenerator> CODEC = BuiltInRegistries.CHUNK_GENERATOR.byNameCodec().dispatchStable(ChunkGenerator::codec, Function.identity());
	protected final BiomeSource biomeSource;
	private final Supplier<List<FeatureSorter.StepFeatureData>> featuresPerStep;
	private final Function<Holder<Biome>, BiomeGenerationSettings> generationSettingsGetter;

	public ChunkGenerator(BiomeSource biomeSource) {
		this(biomeSource, holder -> ((Biome)holder.value()).getGenerationSettings());
	}

	public ChunkGenerator(BiomeSource biomeSource, Function<Holder<Biome>, BiomeGenerationSettings> function) {
		this.biomeSource = biomeSource;
		this.generationSettingsGetter = function;
		this.featuresPerStep = Suppliers.memoize(
			() -> FeatureSorter.buildFeaturesPerStep(
					List.copyOf(biomeSource.possibleBiomes()), holder -> ((BiomeGenerationSettings)function.apply(holder)).features(), true
				)
		);
	}

	public void validate() {
		this.featuresPerStep.get();
	}

	protected abstract MapCodec<? extends ChunkGenerator> codec();

	public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> holderLookup, RandomState randomState, long l) {
		return ChunkGeneratorStructureState.createForNormal(randomState, l, this.biomeSource, holderLookup);
	}

	public Optional<ResourceKey<MapCodec<? extends ChunkGenerator>>> getTypeNameForDataFixer() {
		return BuiltInRegistries.CHUNK_GENERATOR.getResourceKey(this.codec());
	}

	public CompletableFuture<ChunkAccess> createBiomes(
		Executor executor, RandomState randomState, Blender blender, StructureManager structureManager, ChunkAccess chunkAccess
	) {
		return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", (Supplier)(() -> {
			chunkAccess.fillBiomesFromNoise(this.biomeSource, randomState.sampler());
			return chunkAccess;
		})), Util.backgroundExecutor());
	}

	public abstract void applyCarvers(
		WorldGenRegion worldGenRegion,
		long l,
		RandomState randomState,
		BiomeManager biomeManager,
		StructureManager structureManager,
		ChunkAccess chunkAccess,
		GenerationStep.Carving carving
	);

	@Nullable
	public Pair<BlockPos, Holder<Structure>> findNearestMapStructure(ServerLevel serverLevel, HolderSet<Structure> holderSet, BlockPos blockPos, int i, boolean bl) {
		ChunkGeneratorStructureState chunkGeneratorStructureState = serverLevel.getChunkSource().getGeneratorState();
		Map<StructurePlacement, Set<Holder<Structure>>> map = new Object2ObjectArrayMap<>();

		for (Holder<Structure> holder : holderSet) {
			for (StructurePlacement structurePlacement : chunkGeneratorStructureState.getPlacementsForStructure(holder)) {
				((Set)map.computeIfAbsent(structurePlacement, structurePlacementx -> new ObjectArraySet())).add(holder);
			}
		}

		if (map.isEmpty()) {
			return null;
		} else {
			Pair<BlockPos, Holder<Structure>> pair = null;
			double d = Double.MAX_VALUE;
			StructureManager structureManager = serverLevel.structureManager();
			List<Entry<StructurePlacement, Set<Holder<Structure>>>> list = new ArrayList(map.size());

			for (Entry<StructurePlacement, Set<Holder<Structure>>> entry : map.entrySet()) {
				StructurePlacement structurePlacement2 = (StructurePlacement)entry.getKey();
				if (structurePlacement2 instanceof ConcentricRingsStructurePlacement) {
					ConcentricRingsStructurePlacement concentricRingsStructurePlacement = (ConcentricRingsStructurePlacement)structurePlacement2;
					Pair<BlockPos, Holder<Structure>> pair2 = this.getNearestGeneratedStructure(
						(Set<Holder<Structure>>)entry.getValue(), serverLevel, structureManager, blockPos, bl, concentricRingsStructurePlacement
					);
					if (pair2 != null) {
						BlockPos blockPos2 = pair2.getFirst();
						double e = blockPos.distSqr(blockPos2);
						if (e < d) {
							d = e;
							pair = pair2;
						}
					}
				} else if (structurePlacement2 instanceof RandomSpreadStructurePlacement) {
					list.add(entry);
				}
			}

			if (!list.isEmpty()) {
				int j = SectionPos.blockToSectionCoord(blockPos.getX());
				int k = SectionPos.blockToSectionCoord(blockPos.getZ());

				for (int l = 0; l <= i; l++) {
					boolean bl2 = false;

					for (Entry<StructurePlacement, Set<Holder<Structure>>> entry2 : list) {
						RandomSpreadStructurePlacement randomSpreadStructurePlacement = (RandomSpreadStructurePlacement)entry2.getKey();
						Pair<BlockPos, Holder<Structure>> pair3 = getNearestGeneratedStructure(
							(Set<Holder<Structure>>)entry2.getValue(),
							serverLevel,
							structureManager,
							j,
							k,
							l,
							bl,
							chunkGeneratorStructureState.getLevelSeed(),
							randomSpreadStructurePlacement
						);
						if (pair3 != null) {
							bl2 = true;
							double f = blockPos.distSqr(pair3.getFirst());
							if (f < d) {
								d = f;
								pair = pair3;
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

	@Nullable
	private Pair<BlockPos, Holder<Structure>> getNearestGeneratedStructure(
		Set<Holder<Structure>> set,
		ServerLevel serverLevel,
		StructureManager structureManager,
		BlockPos blockPos,
		boolean bl,
		ConcentricRingsStructurePlacement concentricRingsStructurePlacement
	) {
		List<ChunkPos> list = serverLevel.getChunkSource().getGeneratorState().getRingPositionsFor(concentricRingsStructurePlacement);
		if (list == null) {
			throw new IllegalStateException("Somehow tried to find structures for a placement that doesn't exist");
		} else {
			Pair<BlockPos, Holder<Structure>> pair = null;
			double d = Double.MAX_VALUE;
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (ChunkPos chunkPos : list) {
				mutableBlockPos.set(SectionPos.sectionToBlockCoord(chunkPos.x, 8), 32, SectionPos.sectionToBlockCoord(chunkPos.z, 8));
				double e = mutableBlockPos.distSqr(blockPos);
				boolean bl2 = pair == null || e < d;
				if (bl2) {
					Pair<BlockPos, Holder<Structure>> pair2 = getStructureGeneratingAt(set, serverLevel, structureManager, bl, concentricRingsStructurePlacement, chunkPos);
					if (pair2 != null) {
						pair = pair2;
						d = e;
					}
				}
			}

			return pair;
		}
	}

	@Nullable
	private static Pair<BlockPos, Holder<Structure>> getNearestGeneratedStructure(
		Set<Holder<Structure>> set,
		LevelReader levelReader,
		StructureManager structureManager,
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
					ChunkPos chunkPos = randomSpreadStructurePlacement.getPotentialStructureChunk(l, p, q);
					Pair<BlockPos, Holder<Structure>> pair = getStructureGeneratingAt(set, levelReader, structureManager, bl, randomSpreadStructurePlacement, chunkPos);
					if (pair != null) {
						return pair;
					}
				}
			}
		}

		return null;
	}

	@Nullable
	private static Pair<BlockPos, Holder<Structure>> getStructureGeneratingAt(
		Set<Holder<Structure>> set, LevelReader levelReader, StructureManager structureManager, boolean bl, StructurePlacement structurePlacement, ChunkPos chunkPos
	) {
		for (Holder<Structure> holder : set) {
			StructureCheckResult structureCheckResult = structureManager.checkStructurePresence(chunkPos, holder.value(), structurePlacement, bl);
			if (structureCheckResult != StructureCheckResult.START_NOT_PRESENT) {
				if (!bl && structureCheckResult == StructureCheckResult.START_PRESENT) {
					return Pair.of(structurePlacement.getLocatePos(chunkPos), holder);
				}

				ChunkAccess chunkAccess = levelReader.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
				StructureStart structureStart = structureManager.getStartForStructure(SectionPos.bottomOf(chunkAccess), holder.value(), chunkAccess);
				if (structureStart != null && structureStart.isValid() && (!bl || tryAddReference(structureManager, structureStart))) {
					return Pair.of(structurePlacement.getLocatePos(structureStart.getChunkPos()), holder);
				}
			}
		}

		return null;
	}

	private static boolean tryAddReference(StructureManager structureManager, StructureStart structureStart) {
		if (structureStart.canBeReferenced()) {
			structureManager.addReference(structureStart);
			return true;
		} else {
			return false;
		}
	}

	public void applyBiomeDecoration(WorldGenLevel worldGenLevel, ChunkAccess chunkAccess, StructureManager structureManager) {
		ChunkPos chunkPos = chunkAccess.getPos();
		if (!SharedConstants.debugVoidTerrain(chunkPos)) {
			SectionPos sectionPos = SectionPos.of(chunkPos, worldGenLevel.getMinSection());
			BlockPos blockPos = sectionPos.origin();
			Registry<Structure> registry = worldGenLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
			Map<Integer, List<Structure>> map = (Map<Integer, List<Structure>>)registry.stream().collect(Collectors.groupingBy(structure -> structure.step().ordinal()));
			List<FeatureSorter.StepFeatureData> list = (List<FeatureSorter.StepFeatureData>)this.featuresPerStep.get();
			WorldgenRandom worldgenRandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.generateUniqueSeed()));
			long l = worldgenRandom.setDecorationSeed(worldGenLevel.getSeed(), blockPos.getX(), blockPos.getZ());
			Set<Holder<Biome>> set = new ObjectArraySet<>();
			ChunkPos.rangeClosed(sectionPos.chunk(), 1).forEach(chunkPosx -> {
				ChunkAccess chunkAccessx = worldGenLevel.getChunk(chunkPosx.x, chunkPosx.z);

				for (LevelChunkSection levelChunkSection : chunkAccessx.getSections()) {
					levelChunkSection.getBiomes().getAll(set::add);
				}
			});
			set.retainAll(this.biomeSource.possibleBiomes());
			int i = list.size();

			try {
				Registry<PlacedFeature> registry2 = worldGenLevel.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
				int j = Math.max(GenerationStep.Decoration.values().length, i);

				for (int k = 0; k < j; k++) {
					int m = 0;
					if (structureManager.shouldGenerateStructures()) {
						for (Structure structure : (List)map.getOrDefault(k, Collections.emptyList())) {
							worldgenRandom.setFeatureSeed(l, m, k);
							Supplier<String> supplier = () -> (String)registry.getResourceKey(structure).map(Object::toString).orElseGet(structure::toString);

							try {
								worldGenLevel.setCurrentlyGenerating(supplier);
								structureManager.startsForStructure(sectionPos, structure)
									.forEach(structureStart -> structureStart.placeInChunk(worldGenLevel, structureManager, this, worldgenRandom, getWritableArea(chunkAccess), chunkPos));
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

						for (Holder<Biome> holder : set) {
							List<HolderSet<PlacedFeature>> list3 = ((BiomeGenerationSettings)this.generationSettingsGetter.apply(holder)).features();
							if (k < list3.size()) {
								HolderSet<PlacedFeature> holderSet = (HolderSet<PlacedFeature>)list3.get(k);
								FeatureSorter.StepFeatureData stepFeatureData = (FeatureSorter.StepFeatureData)list.get(k);
								holderSet.stream().map(Holder::value).forEach(placedFeaturex -> intSet.add(stepFeatureData.indexMapping().applyAsInt(placedFeaturex)));
							}
						}

						int n = intSet.size();
						int[] is = intSet.toIntArray();
						Arrays.sort(is);
						FeatureSorter.StepFeatureData stepFeatureData2 = (FeatureSorter.StepFeatureData)list.get(k);

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
				crashReport3.addCategory("Generation").setDetail("CenterX", chunkPos.x).setDetail("CenterZ", chunkPos.z).setDetail("Decoration Seed", l);
				throw new ReportedException(crashReport3);
			}
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

	public abstract void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess);

	public abstract void spawnOriginalMobs(WorldGenRegion worldGenRegion);

	public int getSpawnHeight(LevelHeightAccessor levelHeightAccessor) {
		return 64;
	}

	public BiomeSource getBiomeSource() {
		return this.biomeSource;
	}

	public abstract int getGenDepth();

	public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(
		Holder<Biome> holder, StructureManager structureManager, MobCategory mobCategory, BlockPos blockPos
	) {
		Map<Structure, LongSet> map = structureManager.getAllStructuresAt(blockPos);

		for (Entry<Structure, LongSet> entry : map.entrySet()) {
			Structure structure = (Structure)entry.getKey();
			StructureSpawnOverride structureSpawnOverride = (StructureSpawnOverride)structure.spawnOverrides().get(mobCategory);
			if (structureSpawnOverride != null) {
				MutableBoolean mutableBoolean = new MutableBoolean(false);
				Predicate<StructureStart> predicate = structureSpawnOverride.boundingBox() == StructureSpawnOverride.BoundingBoxType.PIECE
					? structureStart -> structureManager.structureHasPieceAt(blockPos, structureStart)
					: structureStart -> structureStart.getBoundingBox().isInside(blockPos);
				structureManager.fillStartsForStructure(structure, (LongSet)entry.getValue(), structureStart -> {
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

	public void createStructures(
		RegistryAccess registryAccess,
		ChunkGeneratorStructureState chunkGeneratorStructureState,
		StructureManager structureManager,
		ChunkAccess chunkAccess,
		StructureTemplateManager structureTemplateManager
	) {
		ChunkPos chunkPos = chunkAccess.getPos();
		SectionPos sectionPos = SectionPos.bottomOf(chunkAccess);
		RandomState randomState = chunkGeneratorStructureState.randomState();
		chunkGeneratorStructureState.possibleStructureSets()
			.forEach(
				holder -> {
					StructurePlacement structurePlacement = ((StructureSet)holder.value()).placement();
					List<StructureSet.StructureSelectionEntry> list = ((StructureSet)holder.value()).structures();

					for (StructureSet.StructureSelectionEntry structureSelectionEntry : list) {
						StructureStart structureStart = structureManager.getStartForStructure(sectionPos, structureSelectionEntry.structure().value(), chunkAccess);
						if (structureStart != null && structureStart.isValid()) {
							return;
						}
					}

					if (structurePlacement.isStructureChunk(chunkGeneratorStructureState, chunkPos.x, chunkPos.z)) {
						if (list.size() == 1) {
							this.tryGenerateStructure(
								(StructureSet.StructureSelectionEntry)list.get(0),
								structureManager,
								registryAccess,
								randomState,
								structureTemplateManager,
								chunkGeneratorStructureState.getLevelSeed(),
								chunkAccess,
								chunkPos,
								sectionPos
							);
						} else {
							ArrayList<StructureSet.StructureSelectionEntry> arrayList = new ArrayList(list.size());
							arrayList.addAll(list);
							WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
							worldgenRandom.setLargeFeatureSeed(chunkGeneratorStructureState.getLevelSeed(), chunkPos.x, chunkPos.z);
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
								if (this.tryGenerateStructure(
									structureSelectionEntry4,
									structureManager,
									registryAccess,
									randomState,
									structureTemplateManager,
									chunkGeneratorStructureState.getLevelSeed(),
									chunkAccess,
									chunkPos,
									sectionPos
								)) {
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
		StructureManager structureManager,
		RegistryAccess registryAccess,
		RandomState randomState,
		StructureTemplateManager structureTemplateManager,
		long l,
		ChunkAccess chunkAccess,
		ChunkPos chunkPos,
		SectionPos sectionPos
	) {
		Structure structure = structureSelectionEntry.structure().value();
		int i = fetchReferences(structureManager, chunkAccess, sectionPos, structure);
		HolderSet<Biome> holderSet = structure.biomes();
		Predicate<Holder<Biome>> predicate = holderSet::contains;
		StructureStart structureStart = structure.generate(
			registryAccess, this, this.biomeSource, randomState, structureTemplateManager, l, chunkPos, i, chunkAccess, predicate
		);
		if (structureStart.isValid()) {
			structureManager.setStartForStructure(sectionPos, structure, structureStart, chunkAccess);
			return true;
		} else {
			return false;
		}
	}

	private static int fetchReferences(StructureManager structureManager, ChunkAccess chunkAccess, SectionPos sectionPos, Structure structure) {
		StructureStart structureStart = structureManager.getStartForStructure(sectionPos, structure, chunkAccess);
		return structureStart != null ? structureStart.getReferences() : 0;
	}

	public void createReferences(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkAccess chunkAccess) {
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
							structureManager.addReferenceForStructure(sectionPos, structureStart.getStructure(), p, chunkAccess);
							DebugPackets.sendStructurePacket(worldGenLevel, structureStart);
						}
					} catch (Exception var21) {
						CrashReport crashReport = CrashReport.forThrowable(var21, "Generating structure reference");
						CrashReportCategory crashReportCategory = crashReport.addCategory("Structure");
						Optional<? extends Registry<Structure>> optional = worldGenLevel.registryAccess().registry(Registries.STRUCTURE);
						crashReportCategory.setDetail(
							"Id", (CrashReportDetail<String>)(() -> (String)optional.map(registry -> registry.getKey(structureStart.getStructure()).toString()).orElse("UNKNOWN"))
						);
						crashReportCategory.setDetail(
							"Name", (CrashReportDetail<String>)(() -> BuiltInRegistries.STRUCTURE_TYPE.getKey(structureStart.getStructure().type()).toString())
						);
						crashReportCategory.setDetail("Class", (CrashReportDetail<String>)(() -> structureStart.getStructure().getClass().getCanonicalName()));
						throw new ReportedException(crashReport);
					}
				}
			}
		}
	}

	public abstract CompletableFuture<ChunkAccess> fillFromNoise(
		Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess
	);

	public abstract int getSeaLevel();

	public abstract int getMinY();

	public abstract int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState);

	public abstract NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor, RandomState randomState);

	public int getFirstFreeHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
		return this.getBaseHeight(i, j, types, levelHeightAccessor, randomState);
	}

	public int getFirstOccupiedHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
		return this.getBaseHeight(i, j, types, levelHeightAccessor, randomState) - 1;
	}

	public abstract void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos);

	@Deprecated
	public BiomeGenerationSettings getBiomeGenerationSettings(Holder<Biome> holder) {
		return (BiomeGenerationSettings)this.generationSettingsGetter.apply(holder);
	}
}
