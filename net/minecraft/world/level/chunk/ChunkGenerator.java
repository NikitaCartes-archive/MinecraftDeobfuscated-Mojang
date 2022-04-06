/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import com.google.common.base.Stopwatch;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
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
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class ChunkGenerator {
    private static final Logger LOGGER;
    public static final Codec<ChunkGenerator> CODEC;
    protected final Registry<StructureSet> structureSets;
    protected final BiomeSource biomeSource;
    protected final BiomeSource runtimeBiomeSource;
    protected final Optional<HolderSet<StructureSet>> structureOverrides;
    private final Map<Structure, List<StructurePlacement>> placementsForStructure = new Object2ObjectOpenHashMap<Structure, List<StructurePlacement>>();
    private final Map<ConcentricRingsStructurePlacement, CompletableFuture<List<ChunkPos>>> ringPositions = new Object2ObjectArrayMap<ConcentricRingsStructurePlacement, CompletableFuture<List<ChunkPos>>>();
    private boolean hasGeneratedPositions;

    protected static final <T extends ChunkGenerator> Products.P1<RecordCodecBuilder.Mu<T>, Registry<StructureSet>> commonCodec(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(RegistryOps.retrieveRegistry(Registry.STRUCTURE_SET_REGISTRY).forGetter(chunkGenerator -> chunkGenerator.structureSets));
    }

    public ChunkGenerator(Registry<StructureSet> registry, Optional<HolderSet<StructureSet>> optional, BiomeSource biomeSource) {
        this(registry, optional, biomeSource, biomeSource);
    }

    public ChunkGenerator(Registry<StructureSet> registry, Optional<HolderSet<StructureSet>> optional, BiomeSource biomeSource, BiomeSource biomeSource2) {
        this.structureSets = registry;
        this.biomeSource = biomeSource;
        this.runtimeBiomeSource = biomeSource2;
        this.structureOverrides = optional;
    }

    public Stream<Holder<StructureSet>> possibleStructureSets() {
        if (this.structureOverrides.isPresent()) {
            return this.structureOverrides.get().stream();
        }
        return this.structureSets.holders().map(Holder::hackyErase);
    }

    private void generatePositions(RandomState randomState) {
        Set<Holder<Biome>> set = this.runtimeBiomeSource.possibleBiomes();
        this.possibleStructureSets().forEach(holder -> {
            StructureSet structureSet = (StructureSet)holder.value();
            for (StructureSet.StructureSelectionEntry structureSelectionEntry : structureSet.structures()) {
                this.placementsForStructure.computeIfAbsent(structureSelectionEntry.structure().value(), structure -> new ArrayList()).add(structureSet.placement());
            }
            StructurePlacement structurePlacement = structureSet.placement();
            if (structurePlacement instanceof ConcentricRingsStructurePlacement) {
                ConcentricRingsStructurePlacement concentricRingsStructurePlacement = (ConcentricRingsStructurePlacement)structurePlacement;
                if (set.stream().anyMatch(concentricRingsStructurePlacement.preferredBiomes()::contains)) {
                    this.ringPositions.put(concentricRingsStructurePlacement, this.generateRingPositions((Holder<StructureSet>)holder, randomState, concentricRingsStructurePlacement));
                }
            }
        });
    }

    private CompletableFuture<List<ChunkPos>> generateRingPositions(Holder<StructureSet> holder, RandomState randomState, ConcentricRingsStructurePlacement concentricRingsStructurePlacement) {
        if (concentricRingsStructurePlacement.count() == 0) {
            return CompletableFuture.completedFuture(List.of());
        }
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("placement calculation", () -> {
            Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
            ArrayList<ChunkPos> list = new ArrayList<ChunkPos>();
            int i = concentricRingsStructurePlacement.distance();
            int j = concentricRingsStructurePlacement.count();
            int k = concentricRingsStructurePlacement.spread();
            HolderSet<Biome> holderSet = concentricRingsStructurePlacement.preferredBiomes();
            RandomSource randomSource = RandomSource.create();
            randomSource.setSeed(this instanceof FlatLevelSource ? 0L : randomState.legacyLevelSeed());
            double d = randomSource.nextDouble() * Math.PI * 2.0;
            int l = 0;
            int m = 0;
            for (int n = 0; n < j; ++n) {
                double e = (double)(4 * i + i * m * 6) + (randomSource.nextDouble() - 0.5) * ((double)i * 2.5);
                int o = (int)Math.round(Math.cos(d) * e);
                int p = (int)Math.round(Math.sin(d) * e);
                Pair<BlockPos, Holder<Biome>> pair = this.biomeSource.findBiomeHorizontal(SectionPos.sectionToBlockCoord(o, 8), 0, SectionPos.sectionToBlockCoord(p, 8), 112, holderSet::contains, randomSource, randomState.sampler());
                if (pair != null) {
                    BlockPos blockPos = pair.getFirst();
                    o = SectionPos.blockToSectionCoord(blockPos.getX());
                    p = SectionPos.blockToSectionCoord(blockPos.getZ());
                }
                list.add(new ChunkPos(o, p));
                d += Math.PI * 2 / (double)k;
                if (++l != k) continue;
                l = 0;
                k += 2 * k / (++m + 1);
                k = Math.min(k, j - n);
                d += randomSource.nextDouble() * Math.PI * 2.0;
            }
            double f = (double)stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) / 1000.0;
            LOGGER.debug("Calculation for {} took {}s", (Object)holder, (Object)f);
            return list;
        }), Util.backgroundExecutor());
    }

    protected abstract Codec<? extends ChunkGenerator> codec();

    public Optional<ResourceKey<Codec<? extends ChunkGenerator>>> getTypeNameForDataFixer() {
        return Registry.CHUNK_GENERATOR.getResourceKey(this.codec());
    }

    public CompletableFuture<ChunkAccess> createBiomes(Registry<Biome> registry, Executor executor, RandomState randomState, Blender blender, StructureManager structureManager, ChunkAccess chunkAccess) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
            chunkAccess.fillBiomesFromNoise(this.runtimeBiomeSource, randomState.sampler());
            return chunkAccess;
        }), Util.backgroundExecutor());
    }

    public abstract void applyCarvers(WorldGenRegion var1, long var2, RandomState var4, BiomeManager var5, StructureManager var6, ChunkAccess var7, GenerationStep.Carving var8);

    @Nullable
    public Pair<BlockPos, Holder<Structure>> findNearestMapStructure(ServerLevel serverLevel, HolderSet<Structure> holderSet, BlockPos blockPos, int i, boolean bl) {
        Set set = holderSet.stream().flatMap(holder -> ((Structure)holder.value()).biomes().stream()).collect(Collectors.toSet());
        if (set.isEmpty()) {
            return null;
        }
        Set<Holder<Biome>> set2 = this.runtimeBiomeSource.possibleBiomes();
        if (Collections.disjoint(set2, set)) {
            return null;
        }
        Pair<BlockPos, Holder<Structure>> pair = null;
        double d = Double.MAX_VALUE;
        Object2ObjectArrayMap<StructurePlacement, Set> map = new Object2ObjectArrayMap<StructurePlacement, Set>();
        for (Holder holder2 : holderSet) {
            if (set2.stream().noneMatch(((Structure)holder2.value()).biomes()::contains)) continue;
            for (StructurePlacement structurePlacement2 : this.getPlacementsForStructure(holder2, serverLevel.getChunkSource().randomState())) {
                map.computeIfAbsent(structurePlacement2, structurePlacement -> new ObjectArraySet()).add(holder2);
            }
        }
        StructureManager structureManager = serverLevel.structureManager();
        ArrayList arrayList = new ArrayList(map.size());
        for (Map.Entry entry : map.entrySet()) {
            StructurePlacement structurePlacement2 = (StructurePlacement)entry.getKey();
            if (structurePlacement2 instanceof ConcentricRingsStructurePlacement) {
                ConcentricRingsStructurePlacement concentricRingsStructurePlacement = (ConcentricRingsStructurePlacement)structurePlacement2;
                Pair<BlockPos, Holder<Structure>> pair2 = this.getNearestGeneratedStructure((Set)entry.getValue(), serverLevel, structureManager, blockPos, bl, concentricRingsStructurePlacement);
                BlockPos blockPos2 = pair2.getFirst();
                double e = blockPos.distSqr(blockPos2);
                if (!(e < d)) continue;
                d = e;
                pair = pair2;
                continue;
            }
            if (!(structurePlacement2 instanceof RandomSpreadStructurePlacement)) continue;
            arrayList.add(entry);
        }
        if (!arrayList.isEmpty()) {
            int j = SectionPos.blockToSectionCoord(blockPos.getX());
            int k = SectionPos.blockToSectionCoord(blockPos.getZ());
            for (int l = 0; l <= i; ++l) {
                boolean bl2 = false;
                for (Map.Entry entry : arrayList) {
                    RandomSpreadStructurePlacement randomSpreadStructurePlacement = (RandomSpreadStructurePlacement)entry.getKey();
                    Pair<BlockPos, Holder<Structure>> pair3 = ChunkGenerator.getNearestGeneratedStructure((Set)entry.getValue(), serverLevel, structureManager, j, k, l, bl, serverLevel.getSeed(), randomSpreadStructurePlacement);
                    if (pair3 == null) continue;
                    bl2 = true;
                    double f = blockPos.distSqr(pair3.getFirst());
                    if (!(f < d)) continue;
                    d = f;
                    pair = pair3;
                }
                if (!bl2) continue;
                return pair;
            }
        }
        return pair;
    }

    @Nullable
    private Pair<BlockPos, Holder<Structure>> getNearestGeneratedStructure(Set<Holder<Structure>> set, ServerLevel serverLevel, StructureManager structureManager, BlockPos blockPos, boolean bl, ConcentricRingsStructurePlacement concentricRingsStructurePlacement) {
        List<ChunkPos> list = this.getRingPositionsFor(concentricRingsStructurePlacement, serverLevel.getChunkSource().randomState());
        if (list == null) {
            throw new IllegalStateException("Somehow tried to find structures for a placement that doesn't exist");
        }
        Pair<BlockPos, Holder<Structure>> pair = null;
        double d = Double.MAX_VALUE;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (ChunkPos chunkPos : list) {
            Pair<BlockPos, Holder<Structure>> pair2;
            mutableBlockPos.set(SectionPos.sectionToBlockCoord(chunkPos.x, 8), 32, SectionPos.sectionToBlockCoord(chunkPos.z, 8));
            double e = mutableBlockPos.distSqr(blockPos);
            boolean bl2 = pair == null || e < d;
            if (!bl2 || (pair2 = ChunkGenerator.getStructureGeneratingAt(set, serverLevel, structureManager, bl, concentricRingsStructurePlacement, chunkPos)) == null) continue;
            pair = pair2;
            d = e;
        }
        return pair;
    }

    @Nullable
    private static Pair<BlockPos, Holder<Structure>> getNearestGeneratedStructure(Set<Holder<Structure>> set, LevelReader levelReader, StructureManager structureManager, int i, int j, int k, boolean bl, long l, RandomSpreadStructurePlacement randomSpreadStructurePlacement) {
        int m = randomSpreadStructurePlacement.spacing();
        for (int n = -k; n <= k; ++n) {
            boolean bl2 = n == -k || n == k;
            for (int o = -k; o <= k; ++o) {
                int q;
                int p;
                ChunkPos chunkPos;
                Pair<BlockPos, Holder<Structure>> pair;
                boolean bl3;
                boolean bl4 = bl3 = o == -k || o == k;
                if (!bl2 && !bl3 || (pair = ChunkGenerator.getStructureGeneratingAt(set, levelReader, structureManager, bl, randomSpreadStructurePlacement, chunkPos = randomSpreadStructurePlacement.getPotentialStructureChunk(l, p = i + m * n, q = j + m * o))) == null) continue;
                return pair;
            }
        }
        return null;
    }

    @Nullable
    private static Pair<BlockPos, Holder<Structure>> getStructureGeneratingAt(Set<Holder<Structure>> set, LevelReader levelReader, StructureManager structureManager, boolean bl, StructurePlacement structurePlacement, ChunkPos chunkPos) {
        for (Holder<Structure> holder : set) {
            StructureCheckResult structureCheckResult = structureManager.checkStructurePresence(chunkPos, holder.value(), bl);
            if (structureCheckResult == StructureCheckResult.START_NOT_PRESENT) continue;
            if (!bl && structureCheckResult == StructureCheckResult.START_PRESENT) {
                return Pair.of(structurePlacement.getLocatePos(chunkPos), holder);
            }
            ChunkAccess chunkAccess = levelReader.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
            StructureStart structureStart = structureManager.getStartForStructure(SectionPos.bottomOf(chunkAccess), holder.value(), chunkAccess);
            if (structureStart == null || !structureStart.isValid() || bl && !ChunkGenerator.tryAddReference(structureManager, structureStart)) continue;
            return Pair.of(structurePlacement.getLocatePos(structureStart.getChunkPos()), holder);
        }
        return null;
    }

    private static boolean tryAddReference(StructureManager structureManager, StructureStart structureStart) {
        if (structureStart.canBeReferenced()) {
            structureManager.addReference(structureStart);
            return true;
        }
        return false;
    }

    public void applyBiomeDecoration(WorldGenLevel worldGenLevel, ChunkAccess chunkAccess, StructureManager structureManager) {
        ChunkPos chunkPos2 = chunkAccess.getPos();
        if (SharedConstants.debugVoidTerrain(chunkPos2)) {
            return;
        }
        SectionPos sectionPos = SectionPos.of(chunkPos2, worldGenLevel.getMinSection());
        BlockPos blockPos = sectionPos.origin();
        Registry<Structure> registry = worldGenLevel.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);
        Map<Integer, List<Structure>> map = registry.stream().collect(Collectors.groupingBy(structure -> structure.step().ordinal()));
        List<BiomeSource.StepFeatureData> list = this.biomeSource.featuresPerStep();
        WorldgenRandom worldgenRandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.seedUniquifier()));
        long l = worldgenRandom.setDecorationSeed(worldGenLevel.getSeed(), blockPos.getX(), blockPos.getZ());
        ObjectArraySet set = new ObjectArraySet();
        if (this instanceof FlatLevelSource) {
            this.biomeSource.possibleBiomes().stream().map(Holder::value).forEach(set::add);
        } else {
            ChunkPos.rangeClosed(sectionPos.chunk(), 1).forEach(chunkPos -> {
                ChunkAccess chunkAccess = worldGenLevel.getChunk(chunkPos.x, chunkPos.z);
                for (LevelChunkSection levelChunkSection : chunkAccess.getSections()) {
                    levelChunkSection.getBiomes().getAll(holder -> set.add((Biome)holder.value()));
                }
            });
            set.retainAll(this.biomeSource.possibleBiomes().stream().map(Holder::value).collect(Collectors.toSet()));
        }
        int i = list.size();
        try {
            Registry<PlacedFeature> registry2 = worldGenLevel.registryAccess().registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
            int j = Math.max(GenerationStep.Decoration.values().length, i);
            for (int k = 0; k < j; ++k) {
                int m = 0;
                if (structureManager.shouldGenerateStructures()) {
                    List list2 = map.getOrDefault(k, Collections.emptyList());
                    for (Structure structure2 : list2) {
                        worldgenRandom.setFeatureSeed(l, m, k);
                        Supplier<String> supplier = () -> registry.getResourceKey(structure2).map(Object::toString).orElseGet(structure2::toString);
                        try {
                            worldGenLevel.setCurrentlyGenerating(supplier);
                            structureManager.startsForStructure(sectionPos, structure2).forEach(structureStart -> structureStart.placeInChunk(worldGenLevel, structureManager, this, worldgenRandom, ChunkGenerator.getWritableArea(chunkAccess), chunkPos2));
                        } catch (Exception exception) {
                            CrashReport crashReport = CrashReport.forThrowable(exception, "Feature placement");
                            crashReport.addCategory("Feature").setDetail("Description", supplier::get);
                            throw new ReportedException(crashReport);
                        }
                        ++m;
                    }
                }
                if (k >= i) continue;
                IntArraySet intSet = new IntArraySet();
                for (Biome biome : set) {
                    List<HolderSet<PlacedFeature>> list3 = biome.getGenerationSettings().features();
                    if (k >= list3.size()) continue;
                    HolderSet<PlacedFeature> holderSet = list3.get(k);
                    BiomeSource.StepFeatureData stepFeatureData = list.get(k);
                    holderSet.stream().map(Holder::value).forEach(placedFeature -> intSet.add(stepFeatureData.indexMapping().applyAsInt((PlacedFeature)placedFeature)));
                }
                int n = intSet.size();
                int[] is = intSet.toIntArray();
                Arrays.sort(is);
                BiomeSource.StepFeatureData stepFeatureData2 = list.get(k);
                for (int o = 0; o < n; ++o) {
                    int p = is[o];
                    PlacedFeature placedFeature2 = stepFeatureData2.features().get(p);
                    Supplier<String> supplier2 = () -> registry2.getResourceKey(placedFeature2).map(Object::toString).orElseGet(placedFeature2::toString);
                    worldgenRandom.setFeatureSeed(l, p, k);
                    try {
                        worldGenLevel.setCurrentlyGenerating(supplier2);
                        placedFeature2.placeWithBiomeCheck(worldGenLevel, this, worldgenRandom, blockPos);
                        continue;
                    } catch (Exception exception2) {
                        CrashReport crashReport2 = CrashReport.forThrowable(exception2, "Feature placement");
                        crashReport2.addCategory("Feature").setDetail("Description", supplier2::get);
                        throw new ReportedException(crashReport2);
                    }
                }
            }
            worldGenLevel.setCurrentlyGenerating(null);
        } catch (Exception exception3) {
            CrashReport crashReport3 = CrashReport.forThrowable(exception3, "Biome decoration");
            crashReport3.addCategory("Generation").setDetail("CenterX", chunkPos2.x).setDetail("CenterZ", chunkPos2.z).setDetail("Seed", l);
            throw new ReportedException(crashReport3);
        }
    }

    public boolean hasStructureChunkInRange(Holder<StructureSet> holder, RandomState randomState, long l, int i, int j, int k) {
        StructureSet structureSet = holder.value();
        if (structureSet == null) {
            return false;
        }
        StructurePlacement structurePlacement = structureSet.placement();
        for (int m = i - k; m <= i + k; ++m) {
            for (int n = j - k; n <= j + k; ++n) {
                if (!structurePlacement.isStructureChunk(this, randomState, l, m, n)) continue;
                return true;
            }
        }
        return false;
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

    public abstract void buildSurface(WorldGenRegion var1, StructureManager var2, RandomState var3, ChunkAccess var4);

    public abstract void spawnOriginalMobs(WorldGenRegion var1);

    public int getSpawnHeight(LevelHeightAccessor levelHeightAccessor) {
        return 64;
    }

    public BiomeSource getBiomeSource() {
        return this.runtimeBiomeSource;
    }

    public abstract int getGenDepth();

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Holder<Biome> holder, StructureManager structureManager, MobCategory mobCategory, BlockPos blockPos) {
        Map<Structure, LongSet> map = structureManager.getAllStructuresAt(blockPos);
        for (Map.Entry<Structure, LongSet> entry : map.entrySet()) {
            Structure structure = entry.getKey();
            StructureSpawnOverride structureSpawnOverride = structure.spawnOverrides().get(mobCategory);
            if (structureSpawnOverride == null) continue;
            MutableBoolean mutableBoolean = new MutableBoolean(false);
            Predicate<StructureStart> predicate = structureSpawnOverride.boundingBox() == StructureSpawnOverride.BoundingBoxType.PIECE ? structureStart -> structureManager.structureHasPieceAt(blockPos, (StructureStart)structureStart) : structureStart -> structureStart.getBoundingBox().isInside(blockPos);
            structureManager.fillStartsForStructure(structure, entry.getValue(), structureStart -> {
                if (mutableBoolean.isFalse() && predicate.test((StructureStart)structureStart)) {
                    mutableBoolean.setTrue();
                }
            });
            if (!mutableBoolean.isTrue()) continue;
            return structureSpawnOverride.spawns();
        }
        return holder.value().getMobSettings().getMobs(mobCategory);
    }

    public void createStructures(RegistryAccess registryAccess, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess, StructureTemplateManager structureTemplateManager, long l) {
        ChunkPos chunkPos = chunkAccess.getPos();
        SectionPos sectionPos = SectionPos.bottomOf(chunkAccess);
        this.possibleStructureSets().forEach(holder -> {
            StructurePlacement structurePlacement = ((StructureSet)holder.value()).placement();
            List<StructureSet.StructureSelectionEntry> list = ((StructureSet)holder.value()).structures();
            for (StructureSet.StructureSelectionEntry structureSelectionEntry : list) {
                StructureStart structureStart = structureManager.getStartForStructure(sectionPos, structureSelectionEntry.structure().value(), chunkAccess);
                if (structureStart == null || !structureStart.isValid()) continue;
                return;
            }
            if (!structurePlacement.isStructureChunk(this, randomState, l, chunkPos.x, chunkPos.z)) {
                return;
            }
            if (list.size() == 1) {
                this.tryGenerateStructure(list.get(0), structureManager, registryAccess, randomState, structureTemplateManager, l, chunkAccess, chunkPos, sectionPos);
                return;
            }
            ArrayList<StructureSet.StructureSelectionEntry> arrayList = new ArrayList<StructureSet.StructureSelectionEntry>(list.size());
            arrayList.addAll(list);
            WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
            worldgenRandom.setLargeFeatureSeed(l, chunkPos.x, chunkPos.z);
            int i = 0;
            for (StructureSet.StructureSelectionEntry structureSelectionEntry2 : arrayList) {
                i += structureSelectionEntry2.weight();
            }
            while (!arrayList.isEmpty()) {
                StructureSet.StructureSelectionEntry structureSelectionEntry3;
                int j = worldgenRandom.nextInt(i);
                int k = 0;
                Iterator iterator = arrayList.iterator();
                while (iterator.hasNext() && (j -= (structureSelectionEntry3 = (StructureSet.StructureSelectionEntry)iterator.next()).weight()) >= 0) {
                    ++k;
                }
                StructureSet.StructureSelectionEntry structureSelectionEntry4 = (StructureSet.StructureSelectionEntry)arrayList.get(k);
                if (this.tryGenerateStructure(structureSelectionEntry4, structureManager, registryAccess, randomState, structureTemplateManager, l, chunkAccess, chunkPos, sectionPos)) {
                    return;
                }
                arrayList.remove(k);
                i -= structureSelectionEntry4.weight();
            }
        });
    }

    private boolean tryGenerateStructure(StructureSet.StructureSelectionEntry structureSelectionEntry, StructureManager structureManager, RegistryAccess registryAccess, RandomState randomState, StructureTemplateManager structureTemplateManager, long l, ChunkAccess chunkAccess, ChunkPos chunkPos, SectionPos sectionPos) {
        HolderSet<Biome> holderSet;
        Predicate<Holder<Biome>> predicate;
        int i;
        Structure structure = structureSelectionEntry.structure().value();
        StructureStart structureStart = structure.generate(registryAccess, this, this.biomeSource, randomState, structureTemplateManager, l, chunkPos, i = ChunkGenerator.fetchReferences(structureManager, chunkAccess, sectionPos, structure), chunkAccess, predicate = arg_0 -> this.method_41048(holderSet = structure.biomes(), arg_0));
        if (structureStart.isValid()) {
            structureManager.setStartForStructure(sectionPos, structure, structureStart, chunkAccess);
            return true;
        }
        return false;
    }

    private static int fetchReferences(StructureManager structureManager, ChunkAccess chunkAccess, SectionPos sectionPos, Structure structure) {
        StructureStart structureStart = structureManager.getStartForStructure(sectionPos, structure, chunkAccess);
        return structureStart != null ? structureStart.getReferences() : 0;
    }

    protected Holder<Biome> adjustBiome(Holder<Biome> holder) {
        return holder;
    }

    public void createReferences(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkAccess chunkAccess) {
        int i = 8;
        ChunkPos chunkPos = chunkAccess.getPos();
        int j = chunkPos.x;
        int k = chunkPos.z;
        int l = chunkPos.getMinBlockX();
        int m = chunkPos.getMinBlockZ();
        SectionPos sectionPos = SectionPos.bottomOf(chunkAccess);
        for (int n = j - 8; n <= j + 8; ++n) {
            for (int o = k - 8; o <= k + 8; ++o) {
                long p = ChunkPos.asLong(n, o);
                for (StructureStart structureStart : worldGenLevel.getChunk(n, o).getAllStarts().values()) {
                    try {
                        if (!structureStart.isValid() || !structureStart.getBoundingBox().intersects(l, m, l + 15, m + 15)) continue;
                        structureManager.addReferenceForStructure(sectionPos, structureStart.getStructure(), p, chunkAccess);
                        DebugPackets.sendStructurePacket(worldGenLevel, structureStart);
                    } catch (Exception exception) {
                        CrashReport crashReport = CrashReport.forThrowable(exception, "Generating structure reference");
                        CrashReportCategory crashReportCategory = crashReport.addCategory("Structure");
                        Optional<Registry<Structure>> optional = worldGenLevel.registryAccess().registry(Registry.STRUCTURE_REGISTRY);
                        crashReportCategory.setDetail("Id", () -> optional.map(registry -> registry.getKey(structureStart.getStructure()).toString()).orElse("UNKNOWN"));
                        crashReportCategory.setDetail("Name", () -> Registry.STRUCTURE_TYPES.getKey(structureStart.getStructure().type()).toString());
                        crashReportCategory.setDetail("Class", () -> structureStart.getStructure().getClass().getCanonicalName());
                        throw new ReportedException(crashReport);
                    }
                }
            }
        }
    }

    public abstract CompletableFuture<ChunkAccess> fillFromNoise(Executor var1, Blender var2, RandomState var3, StructureManager var4, ChunkAccess var5);

    public abstract int getSeaLevel();

    public abstract int getMinY();

    public abstract int getBaseHeight(int var1, int var2, Heightmap.Types var3, LevelHeightAccessor var4, RandomState var5);

    public abstract NoiseColumn getBaseColumn(int var1, int var2, LevelHeightAccessor var3, RandomState var4);

    public int getFirstFreeHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return this.getBaseHeight(i, j, types, levelHeightAccessor, randomState);
    }

    public int getFirstOccupiedHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return this.getBaseHeight(i, j, types, levelHeightAccessor, randomState) - 1;
    }

    public void ensureStructuresGenerated(RandomState randomState) {
        if (!this.hasGeneratedPositions) {
            this.generatePositions(randomState);
            this.hasGeneratedPositions = true;
        }
    }

    @Nullable
    public List<ChunkPos> getRingPositionsFor(ConcentricRingsStructurePlacement concentricRingsStructurePlacement, RandomState randomState) {
        this.ensureStructuresGenerated(randomState);
        CompletableFuture<List<ChunkPos>> completableFuture = this.ringPositions.get(concentricRingsStructurePlacement);
        return completableFuture != null ? completableFuture.join() : null;
    }

    private List<StructurePlacement> getPlacementsForStructure(Holder<Structure> holder, RandomState randomState) {
        this.ensureStructuresGenerated(randomState);
        return this.placementsForStructure.getOrDefault(holder.value(), List.of());
    }

    public abstract void addDebugScreenInfo(List<String> var1, RandomState var2, BlockPos var3);

    private /* synthetic */ boolean method_41048(HolderSet holderSet, Holder holder) {
        return holderSet.contains(this.adjustBiome(holder));
    }

    static {
        Registry.register(Registry.CHUNK_GENERATOR, "noise", NoiseBasedChunkGenerator.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, "flat", FlatLevelSource.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, "debug", DebugLevelSource.CODEC);
        LOGGER = LogUtils.getLogger();
        CODEC = Registry.CHUNK_GENERATOR.byNameCodec().dispatchStable(ChunkGenerator::codec, Function.identity());
    }
}

