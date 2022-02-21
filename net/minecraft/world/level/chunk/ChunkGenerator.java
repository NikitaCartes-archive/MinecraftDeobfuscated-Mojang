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
import java.util.Random;
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
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class ChunkGenerator
implements BiomeManager.NoiseBiomeSource {
    private static final Logger LOGGER;
    public static final Codec<ChunkGenerator> CODEC;
    protected final Registry<StructureSet> structureSets;
    protected final BiomeSource biomeSource;
    protected final BiomeSource runtimeBiomeSource;
    protected final Optional<HolderSet<StructureSet>> structureOverrides;
    private final Map<ConfiguredStructureFeature<?, ?>, List<StructurePlacement>> placementsForFeature = new Object2ObjectOpenHashMap();
    private final Map<ConcentricRingsStructurePlacement, CompletableFuture<List<ChunkPos>>> ringPositions = new Object2ObjectArrayMap<ConcentricRingsStructurePlacement, CompletableFuture<List<ChunkPos>>>();
    private boolean hasGeneratedPositions;
    private final long seed;

    protected static final <T extends ChunkGenerator> Products.P1<RecordCodecBuilder.Mu<T>, Registry<StructureSet>> commonCodec(RecordCodecBuilder.Instance<T> instance) {
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
        this.seed = l;
    }

    public Stream<Holder<StructureSet>> possibleStructureSets() {
        if (this.structureOverrides.isPresent()) {
            return this.structureOverrides.get().stream();
        }
        return this.structureSets.holders().map(Holder::hackyErase);
    }

    private void generatePositions() {
        Set<Holder<Biome>> set = this.runtimeBiomeSource.possibleBiomes();
        this.possibleStructureSets().forEach(holder -> {
            StructureSet structureSet = (StructureSet)holder.value();
            for (StructureSet.StructureSelectionEntry structureSelectionEntry2 : structureSet.structures()) {
                this.placementsForFeature.computeIfAbsent(structureSelectionEntry2.structure().value(), configuredStructureFeature -> new ArrayList()).add(structureSet.placement());
            }
            StructurePlacement structurePlacement = structureSet.placement();
            if (structurePlacement instanceof ConcentricRingsStructurePlacement) {
                ConcentricRingsStructurePlacement concentricRingsStructurePlacement = (ConcentricRingsStructurePlacement)structurePlacement;
                if (structureSet.structures().stream().anyMatch(structureSelectionEntry -> structureSelectionEntry.generatesInMatchingBiome(set::contains))) {
                    this.ringPositions.put(concentricRingsStructurePlacement, this.generateRingPositions((Holder<StructureSet>)holder, concentricRingsStructurePlacement));
                }
            }
        });
    }

    private CompletableFuture<List<ChunkPos>> generateRingPositions(Holder<StructureSet> holder, ConcentricRingsStructurePlacement concentricRingsStructurePlacement) {
        if (concentricRingsStructurePlacement.count() == 0) {
            return CompletableFuture.completedFuture(List.of());
        }
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("placement calculation", () -> {
            Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
            ArrayList<ChunkPos> list = new ArrayList<ChunkPos>();
            Set set = ((StructureSet)holder.value()).structures().stream().flatMap(structureSelectionEntry -> structureSelectionEntry.structure().value().biomes().stream()).collect(Collectors.toSet());
            int i = concentricRingsStructurePlacement.distance();
            int j = concentricRingsStructurePlacement.count();
            int k = concentricRingsStructurePlacement.spread();
            Random random = new Random();
            random.setSeed(this.seed);
            double d = random.nextDouble() * Math.PI * 2.0;
            int l = 0;
            int m = 0;
            for (int n = 0; n < j; ++n) {
                double e = (double)(4 * i + i * m * 6) + (random.nextDouble() - 0.5) * ((double)i * 2.5);
                int o = (int)Math.round(Math.cos(d) * e);
                int p = (int)Math.round(Math.sin(d) * e);
                Pair<BlockPos, Holder<Biome>> pair = this.biomeSource.findBiomeHorizontal(SectionPos.sectionToBlockCoord(o, 8), 0, SectionPos.sectionToBlockCoord(p, 8), 112, set::contains, random, this.climateSampler());
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
                d += random.nextDouble() * Math.PI * 2.0;
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

    public abstract ChunkGenerator withSeed(long var1);

    public CompletableFuture<ChunkAccess> createBiomes(Registry<Biome> registry, Executor executor, Blender blender, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
            chunkAccess.fillBiomesFromNoise(this.runtimeBiomeSource::getNoiseBiome, this.climateSampler());
            return chunkAccess;
        }), Util.backgroundExecutor());
    }

    public abstract Climate.Sampler climateSampler();

    @Override
    public Holder<Biome> getNoiseBiome(int i, int j, int k) {
        return this.getBiomeSource().getNoiseBiome(i, j, k, this.climateSampler());
    }

    public abstract void applyCarvers(WorldGenRegion var1, long var2, BiomeManager var4, StructureFeatureManager var5, ChunkAccess var6, GenerationStep.Carving var7);

    @Nullable
    public Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> findNearestMapFeature(ServerLevel serverLevel, HolderSet<ConfiguredStructureFeature<?, ?>> holderSet, BlockPos blockPos, int i, boolean bl) {
        Set set = holderSet.stream().flatMap(holder -> ((ConfiguredStructureFeature)holder.value()).biomes().stream()).collect(Collectors.toSet());
        if (set.isEmpty()) {
            return null;
        }
        Set<Holder<Biome>> set2 = this.runtimeBiomeSource.possibleBiomes();
        if (Collections.disjoint(set2, set)) {
            return null;
        }
        Pair<BlockPos, Holder> pair = null;
        double d = Double.MAX_VALUE;
        Object2ObjectArrayMap<StructurePlacement, Set> map = new Object2ObjectArrayMap<StructurePlacement, Set>();
        for (Holder holder2 : holderSet) {
            if (set2.stream().noneMatch(((ConfiguredStructureFeature)holder2.value()).biomes()::contains)) continue;
            for (StructurePlacement structurePlacement2 : this.getPlacementsForFeature(holder2)) {
                map.computeIfAbsent(structurePlacement2, structurePlacement -> new ObjectArraySet()).add(holder2);
            }
        }
        ArrayList list = new ArrayList(map.size());
        for (Map.Entry entry : map.entrySet()) {
            StructurePlacement structurePlacement2 = (StructurePlacement)entry.getKey();
            if (structurePlacement2 instanceof ConcentricRingsStructurePlacement) {
                ConcentricRingsStructurePlacement concentricRingsStructurePlacement = (ConcentricRingsStructurePlacement)structurePlacement2;
                BlockPos blockPos2 = this.getNearestGeneratedStructure(blockPos, concentricRingsStructurePlacement);
                double e = blockPos.distSqr(blockPos2);
                if (!(e < d)) continue;
                d = e;
                pair = Pair.of(blockPos2, (Holder)((Set)entry.getValue()).iterator().next());
                continue;
            }
            if (!(structurePlacement2 instanceof RandomSpreadStructurePlacement)) continue;
            list.add(entry);
        }
        if (!list.isEmpty()) {
            int n = SectionPos.blockToSectionCoord(blockPos.getX());
            int n2 = SectionPos.blockToSectionCoord(blockPos.getZ());
            for (int l = 0; l <= i; ++l) {
                boolean bl2 = false;
                for (Map.Entry entry2 : list) {
                    RandomSpreadStructurePlacement randomSpreadStructurePlacement = (RandomSpreadStructurePlacement)entry2.getKey();
                    Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> pair2 = ChunkGenerator.getNearestGeneratedStructure((Set)entry2.getValue(), serverLevel, serverLevel.structureFeatureManager(), n, n2, l, bl, serverLevel.getSeed(), randomSpreadStructurePlacement);
                    if (pair2 == null) continue;
                    bl2 = true;
                    double f = blockPos.distSqr(pair2.getFirst());
                    if (!(f < d)) continue;
                    d = f;
                    pair = pair2;
                }
                if (!bl2) continue;
                return pair;
            }
        }
        return pair;
    }

    @Nullable
    private BlockPos getNearestGeneratedStructure(BlockPos blockPos, ConcentricRingsStructurePlacement concentricRingsStructurePlacement) {
        List<ChunkPos> list = this.getRingPositionsFor(concentricRingsStructurePlacement);
        if (list == null) {
            throw new IllegalStateException("Somehow tried to find structures for a placement that doesn't exist");
        }
        BlockPos blockPos2 = null;
        double d = Double.MAX_VALUE;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (ChunkPos chunkPos : list) {
            mutableBlockPos.set(SectionPos.sectionToBlockCoord(chunkPos.x, 8), 32, SectionPos.sectionToBlockCoord(chunkPos.z, 8));
            double e = mutableBlockPos.distSqr(blockPos);
            if (blockPos2 == null) {
                blockPos2 = new BlockPos(mutableBlockPos);
                d = e;
                continue;
            }
            if (!(e < d)) continue;
            blockPos2 = new BlockPos(mutableBlockPos);
            d = e;
        }
        return blockPos2;
    }

    @Nullable
    private static Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> getNearestGeneratedStructure(Set<Holder<ConfiguredStructureFeature<?, ?>>> set, LevelReader levelReader, StructureFeatureManager structureFeatureManager, int i, int j, int k, boolean bl, long l, RandomSpreadStructurePlacement randomSpreadStructurePlacement) {
        int m = randomSpreadStructurePlacement.spacing();
        for (int n = -k; n <= k; ++n) {
            boolean bl2 = n == -k || n == k;
            for (int o = -k; o <= k; ++o) {
                boolean bl3;
                boolean bl4 = bl3 = o == -k || o == k;
                if (!bl2 && !bl3) continue;
                int p = i + m * n;
                int q = j + m * o;
                ChunkPos chunkPos = randomSpreadStructurePlacement.getPotentialFeatureChunk(l, p, q);
                for (Holder<ConfiguredStructureFeature<?, ?>> holder : set) {
                    StructureCheckResult structureCheckResult = structureFeatureManager.checkStructurePresence(chunkPos, holder.value(), bl);
                    if (structureCheckResult == StructureCheckResult.START_NOT_PRESENT) continue;
                    if (!bl && structureCheckResult == StructureCheckResult.START_PRESENT) {
                        return Pair.of(StructureFeature.getLocatePos(randomSpreadStructurePlacement, chunkPos), holder);
                    }
                    ChunkAccess chunkAccess = levelReader.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
                    StructureStart structureStart = structureFeatureManager.getStartForFeature(SectionPos.bottomOf(chunkAccess), holder.value(), chunkAccess);
                    if (structureStart == null || !structureStart.isValid()) continue;
                    if (bl && structureStart.canBeReferenced()) {
                        structureFeatureManager.addReference(structureStart);
                        return Pair.of(StructureFeature.getLocatePos(randomSpreadStructurePlacement, structureStart.getChunkPos()), holder);
                    }
                    if (bl) continue;
                    return Pair.of(StructureFeature.getLocatePos(randomSpreadStructurePlacement, structureStart.getChunkPos()), holder);
                }
            }
        }
        return null;
    }

    public void applyBiomeDecoration(WorldGenLevel worldGenLevel, ChunkAccess chunkAccess, StructureFeatureManager structureFeatureManager) {
        ChunkPos chunkPos2 = chunkAccess.getPos();
        if (SharedConstants.debugVoidTerrain(chunkPos2)) {
            return;
        }
        SectionPos sectionPos = SectionPos.of(chunkPos2, worldGenLevel.getMinSection());
        BlockPos blockPos = sectionPos.origin();
        Registry<ConfiguredStructureFeature<?, ?>> registry = worldGenLevel.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
        Map<Integer, List<ConfiguredStructureFeature>> map = registry.stream().collect(Collectors.groupingBy(configuredStructureFeature -> ((StructureFeature)configuredStructureFeature.feature).step().ordinal()));
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
                if (structureFeatureManager.shouldGenerateFeatures()) {
                    List list2 = map.getOrDefault(k, Collections.emptyList());
                    for (ConfiguredStructureFeature configuredStructureFeature2 : list2) {
                        worldgenRandom.setFeatureSeed(l, m, k);
                        Supplier<String> supplier = () -> registry.getResourceKey(configuredStructureFeature2).map(Object::toString).orElseGet(configuredStructureFeature2::toString);
                        try {
                            worldGenLevel.setCurrentlyGenerating(supplier);
                            structureFeatureManager.startsForFeature(sectionPos, configuredStructureFeature2).forEach(structureStart -> structureStart.placeInChunk(worldGenLevel, structureFeatureManager, this, worldgenRandom, ChunkGenerator.getWritableArea(chunkAccess), chunkPos2));
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

    public boolean hasFeatureChunkInRange(ResourceKey<StructureSet> resourceKey, int i, int j, int k) {
        StructureSet structureSet = this.structureSets.get(resourceKey);
        if (structureSet == null) {
            return false;
        }
        StructurePlacement structurePlacement = structureSet.placement();
        for (int l = i - k; l <= i + k; ++l) {
            for (int m = j - k; m <= j + k; ++m) {
                if (!structurePlacement.isFeatureChunk(this, l, m)) continue;
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

    public abstract void buildSurface(WorldGenRegion var1, StructureFeatureManager var2, ChunkAccess var3);

    public abstract void spawnOriginalMobs(WorldGenRegion var1);

    public int getSpawnHeight(LevelHeightAccessor levelHeightAccessor) {
        return 64;
    }

    public BiomeSource getBiomeSource() {
        return this.runtimeBiomeSource;
    }

    public abstract int getGenDepth();

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Holder<Biome> holder, StructureFeatureManager structureFeatureManager, MobCategory mobCategory, BlockPos blockPos) {
        Map<ConfiguredStructureFeature<?, ?>, LongSet> map = structureFeatureManager.getAllStructuresAt(blockPos);
        for (Map.Entry<ConfiguredStructureFeature<?, ?>, LongSet> entry : map.entrySet()) {
            ConfiguredStructureFeature<?, ?> configuredStructureFeature = entry.getKey();
            StructureSpawnOverride structureSpawnOverride = configuredStructureFeature.spawnOverrides.get(mobCategory);
            if (structureSpawnOverride == null) continue;
            MutableBoolean mutableBoolean = new MutableBoolean(false);
            Predicate<StructureStart> predicate = structureSpawnOverride.boundingBox() == StructureSpawnOverride.BoundingBoxType.PIECE ? structureStart -> structureFeatureManager.structureHasPieceAt(blockPos, (StructureStart)structureStart) : structureStart -> structureStart.getBoundingBox().isInside(blockPos);
            structureFeatureManager.fillStartsForFeature(configuredStructureFeature, entry.getValue(), structureStart -> {
                if (mutableBoolean.isFalse() && predicate.test((StructureStart)structureStart)) {
                    mutableBoolean.setTrue();
                }
            });
            if (!mutableBoolean.isTrue()) continue;
            return structureSpawnOverride.spawns();
        }
        return holder.value().getMobSettings().getMobs(mobCategory);
    }

    public static Stream<ConfiguredStructureFeature<?, ?>> allConfigurations(Registry<ConfiguredStructureFeature<?, ?>> registry, StructureFeature<?> structureFeature) {
        return registry.stream().filter(configuredStructureFeature -> configuredStructureFeature.feature == structureFeature);
    }

    public void createStructures(RegistryAccess registryAccess, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, StructureManager structureManager, long l) {
        ChunkPos chunkPos = chunkAccess.getPos();
        SectionPos sectionPos = SectionPos.bottomOf(chunkAccess);
        this.possibleStructureSets().forEach(holder -> {
            StructurePlacement structurePlacement = ((StructureSet)holder.value()).placement();
            List<StructureSet.StructureSelectionEntry> list = ((StructureSet)holder.value()).structures();
            for (StructureSet.StructureSelectionEntry structureSelectionEntry : list) {
                StructureStart structureStart = structureFeatureManager.getStartForFeature(sectionPos, structureSelectionEntry.structure().value(), chunkAccess);
                if (structureStart == null || !structureStart.isValid()) continue;
                return;
            }
            if (!structurePlacement.isFeatureChunk(this, chunkPos.x, chunkPos.z)) {
                return;
            }
            if (list.size() == 1) {
                this.tryGenerateStructure(list.get(0), structureFeatureManager, registryAccess, structureManager, l, chunkAccess, chunkPos, sectionPos);
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
                if (this.tryGenerateStructure(structureSelectionEntry4, structureFeatureManager, registryAccess, structureManager, l, chunkAccess, chunkPos, sectionPos)) {
                    return;
                }
                arrayList.remove(k);
                i -= structureSelectionEntry4.weight();
            }
        });
    }

    private boolean tryGenerateStructure(StructureSet.StructureSelectionEntry structureSelectionEntry, StructureFeatureManager structureFeatureManager, RegistryAccess registryAccess, StructureManager structureManager, long l, ChunkAccess chunkAccess, ChunkPos chunkPos, SectionPos sectionPos) {
        HolderSet<Biome> holderSet;
        Predicate<Holder<Biome>> predicate;
        int i;
        ConfiguredStructureFeature<?, ?> configuredStructureFeature = structureSelectionEntry.structure().value();
        StructureStart structureStart = configuredStructureFeature.generate(registryAccess, this, this.biomeSource, structureManager, l, chunkPos, i = ChunkGenerator.fetchReferences(structureFeatureManager, chunkAccess, sectionPos, configuredStructureFeature), chunkAccess, predicate = arg_0 -> this.method_41048(holderSet = configuredStructureFeature.biomes(), arg_0));
        if (structureStart.isValid()) {
            structureFeatureManager.setStartForFeature(sectionPos, configuredStructureFeature, structureStart, chunkAccess);
            return true;
        }
        return false;
    }

    private static int fetchReferences(StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, SectionPos sectionPos, ConfiguredStructureFeature<?, ?> configuredStructureFeature) {
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
        for (int n = j - 8; n <= j + 8; ++n) {
            for (int o = k - 8; o <= k + 8; ++o) {
                long p = ChunkPos.asLong(n, o);
                for (StructureStart structureStart : worldGenLevel.getChunk(n, o).getAllStarts().values()) {
                    try {
                        if (!structureStart.isValid() || !structureStart.getBoundingBox().intersects(l, m, l + 15, m + 15)) continue;
                        structureFeatureManager.addReferenceForFeature(sectionPos, structureStart.getFeature(), p, chunkAccess);
                        DebugPackets.sendStructurePacket(worldGenLevel, structureStart);
                    } catch (Exception exception) {
                        CrashReport crashReport = CrashReport.forThrowable(exception, "Generating structure reference");
                        CrashReportCategory crashReportCategory = crashReport.addCategory("Structure");
                        Optional<Registry<ConfiguredStructureFeature<?, ?>>> optional = worldGenLevel.registryAccess().registry(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
                        crashReportCategory.setDetail("Id", () -> optional.map(registry -> registry.getKey(structureStart.getFeature()).toString()).orElse("UNKNOWN"));
                        crashReportCategory.setDetail("Name", () -> Registry.STRUCTURE_FEATURE.getKey((StructureFeature<?>)structureStart.getFeature().feature).toString());
                        crashReportCategory.setDetail("Class", () -> structureStart.getFeature().getClass().getCanonicalName());
                        throw new ReportedException(crashReport);
                    }
                }
            }
        }
    }

    public abstract CompletableFuture<ChunkAccess> fillFromNoise(Executor var1, Blender var2, StructureFeatureManager var3, ChunkAccess var4);

    public abstract int getSeaLevel();

    public abstract int getMinY();

    public abstract int getBaseHeight(int var1, int var2, Heightmap.Types var3, LevelHeightAccessor var4);

    public abstract NoiseColumn getBaseColumn(int var1, int var2, LevelHeightAccessor var3);

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
        CompletableFuture<List<ChunkPos>> completableFuture = this.ringPositions.get(concentricRingsStructurePlacement);
        return completableFuture != null ? completableFuture.join() : null;
    }

    private List<StructurePlacement> getPlacementsForFeature(Holder<ConfiguredStructureFeature<?, ?>> holder) {
        this.ensureStructuresGenerated();
        return this.placementsForFeature.getOrDefault(holder.value(), List.of());
    }

    public long seed() {
        return this.seed;
    }

    public abstract void addDebugScreenInfo(List<String> var1, BlockPos var2);

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

