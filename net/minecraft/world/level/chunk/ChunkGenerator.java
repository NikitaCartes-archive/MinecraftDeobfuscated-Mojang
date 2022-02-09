/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.jetbrains.annotations.Nullable;

public abstract class ChunkGenerator
implements BiomeManager.NoiseBiomeSource {
    public static final Codec<ChunkGenerator> CODEC;
    protected final BiomeSource biomeSource;
    protected final BiomeSource runtimeBiomeSource;
    private final StructureSettings settings;
    private final Map<ConcentricRingsStructurePlacement, ArrayList<ChunkPos>> ringPositions;
    private final long seed;

    public ChunkGenerator(BiomeSource biomeSource, StructureSettings structureSettings) {
        this(biomeSource, biomeSource, structureSettings, 0L);
    }

    public ChunkGenerator(BiomeSource biomeSource, BiomeSource biomeSource2, StructureSettings structureSettings, long l) {
        this.biomeSource = biomeSource;
        this.runtimeBiomeSource = biomeSource2;
        this.settings = structureSettings;
        this.seed = l;
        this.ringPositions = new Object2ObjectArrayMap<ConcentricRingsStructurePlacement, ArrayList<ChunkPos>>();
        for (Map.Entry<StructureFeature<?>, StructurePlacement> entry : structureSettings.structureConfig().entrySet()) {
            StructurePlacement structurePlacement = entry.getValue();
            if (!(structurePlacement instanceof ConcentricRingsStructurePlacement)) continue;
            ConcentricRingsStructurePlacement concentricRingsStructurePlacement = (ConcentricRingsStructurePlacement)structurePlacement;
            this.ringPositions.put(concentricRingsStructurePlacement, new ArrayList());
        }
    }

    protected void postInit() {
        for (Map.Entry<StructureFeature<?>, StructurePlacement> entry : this.settings.structureConfig().entrySet()) {
            StructurePlacement structurePlacement = entry.getValue();
            if (!(structurePlacement instanceof ConcentricRingsStructurePlacement)) continue;
            ConcentricRingsStructurePlacement concentricRingsStructurePlacement = (ConcentricRingsStructurePlacement)structurePlacement;
            this.generateRingPositions(entry.getKey(), concentricRingsStructurePlacement);
        }
    }

    private void generateRingPositions(StructureFeature<?> structureFeature, ConcentricRingsStructurePlacement concentricRingsStructurePlacement) {
        if (concentricRingsStructurePlacement.count() == 0) {
            return;
        }
        Predicate<ResourceKey> predicate = this.settings.structures(structureFeature).values().stream().collect(Collectors.toUnmodifiableSet())::contains;
        List<ChunkPos> list = this.getRingPositionsFor(concentricRingsStructurePlacement);
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
            BlockPos blockPos = this.biomeSource.findBiomeHorizontal(SectionPos.sectionToBlockCoord(o, 8), 0, SectionPos.sectionToBlockCoord(p, 8), 112, holder -> holder.is(predicate), random, this.climateSampler());
            if (blockPos != null) {
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
    public BlockPos findNearestMapFeature(ServerLevel serverLevel, StructureFeature<?> structureFeature, BlockPos blockPos, int i, boolean bl) {
        StructurePlacement structurePlacement = this.settings.getConfig(structureFeature);
        Collection collection = this.settings.structures(structureFeature).values();
        if (structurePlacement == null || collection.isEmpty()) {
            return null;
        }
        Set set = this.runtimeBiomeSource.possibleBiomes().flatMap(holder -> holder.unwrapKey().stream()).collect(Collectors.toSet());
        if (collection.stream().noneMatch(set::contains)) {
            return null;
        }
        if (structurePlacement instanceof ConcentricRingsStructurePlacement) {
            ConcentricRingsStructurePlacement concentricRingsStructurePlacement = (ConcentricRingsStructurePlacement)structurePlacement;
            return this.getNearestGeneratedStructure(blockPos, concentricRingsStructurePlacement);
        }
        if (structurePlacement instanceof RandomSpreadStructurePlacement) {
            RandomSpreadStructurePlacement randomSpreadStructurePlacement = (RandomSpreadStructurePlacement)structurePlacement;
            return ChunkGenerator.getNearestGeneratedStructure(structureFeature, serverLevel, serverLevel.structureFeatureManager(), blockPos, i, bl, serverLevel.getSeed(), randomSpreadStructurePlacement);
        }
        throw new IllegalStateException("Invalid structure placement type");
    }

    @Nullable
    private BlockPos getNearestGeneratedStructure(BlockPos blockPos, ConcentricRingsStructurePlacement concentricRingsStructurePlacement) {
        List<ChunkPos> list = this.getRingPositionsFor(concentricRingsStructurePlacement);
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
    private static BlockPos getNearestGeneratedStructure(StructureFeature<?> structureFeature, LevelReader levelReader, StructureFeatureManager structureFeatureManager, BlockPos blockPos, int i, boolean bl, long l, RandomSpreadStructurePlacement randomSpreadStructurePlacement) {
        int j = randomSpreadStructurePlacement.spacing();
        int k = SectionPos.blockToSectionCoord(blockPos.getX());
        int m = SectionPos.blockToSectionCoord(blockPos.getZ());
        block0: for (int n = 0; n <= i; ++n) {
            for (int o = -n; o <= n; ++o) {
                boolean bl2 = o == -n || o == n;
                for (int p = -n; p <= n; ++p) {
                    int r;
                    int q;
                    ChunkPos chunkPos;
                    StructureCheckResult structureCheckResult;
                    boolean bl3;
                    boolean bl4 = bl3 = p == -n || p == n;
                    if (!bl2 && !bl3 || (structureCheckResult = structureFeatureManager.checkStructurePresence(chunkPos = randomSpreadStructurePlacement.getPotentialFeatureChunk(l, q = k + j * o, r = m + j * p), structureFeature, bl)) == StructureCheckResult.START_NOT_PRESENT) continue;
                    if (!bl && structureCheckResult == StructureCheckResult.START_PRESENT) {
                        return StructureFeature.getLocatePos(randomSpreadStructurePlacement, chunkPos);
                    }
                    ChunkAccess chunkAccess = levelReader.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_STARTS);
                    StructureStart<?> structureStart = structureFeatureManager.getStartForFeature(SectionPos.bottomOf(chunkAccess), structureFeature, chunkAccess);
                    if (structureStart != null && structureStart.isValid()) {
                        if (bl && structureStart.canBeReferenced()) {
                            structureFeatureManager.addReference(structureStart);
                            return StructureFeature.getLocatePos(randomSpreadStructurePlacement, structureStart.getChunkPos());
                        }
                        if (!bl) {
                            return StructureFeature.getLocatePos(randomSpreadStructurePlacement, structureStart.getChunkPos());
                        }
                    }
                    if (n == 0) break;
                }
                if (n == 0) continue block0;
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
        Map<Integer, List<StructureFeature>> map = Registry.STRUCTURE_FEATURE.stream().collect(Collectors.groupingBy(structureFeature -> structureFeature.step().ordinal()));
        List<BiomeSource.StepFeatureData> list = this.biomeSource.featuresPerStep();
        WorldgenRandom worldgenRandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.seedUniquifier()));
        long l = worldgenRandom.setDecorationSeed(worldGenLevel.getSeed(), blockPos.getX(), blockPos.getZ());
        ObjectArraySet set = new ObjectArraySet();
        if (this instanceof FlatLevelSource) {
            this.biomeSource.possibleBiomes().map(Holder::value).forEach(set::add);
        } else {
            ChunkPos.rangeClosed(sectionPos.chunk(), 1).forEach(chunkPos -> {
                ChunkAccess chunkAccess = worldGenLevel.getChunk(chunkPos.x, chunkPos.z);
                for (LevelChunkSection levelChunkSection : chunkAccess.getSections()) {
                    levelChunkSection.getBiomes().getAll(holder -> set.add((Biome)holder.value()));
                }
            });
            set.retainAll(this.biomeSource.possibleBiomes().map(Holder::value).collect(Collectors.toSet()));
        }
        int i = list.size();
        try {
            Registry<PlacedFeature> registry = worldGenLevel.registryAccess().registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
            Registry<StructureFeature<?>> registry2 = worldGenLevel.registryAccess().registryOrThrow(Registry.STRUCTURE_FEATURE_REGISTRY);
            int j = Math.max(GenerationStep.Decoration.values().length, i);
            for (int k = 0; k < j; ++k) {
                int m = 0;
                if (structureFeatureManager.shouldGenerateFeatures()) {
                    List list2 = map.getOrDefault(k, Collections.emptyList());
                    for (StructureFeature structureFeature2 : list2) {
                        worldgenRandom.setFeatureSeed(l, m, k);
                        Supplier<String> supplier = () -> registry2.getResourceKey(structureFeature2).map(Object::toString).orElseGet(structureFeature2::toString);
                        try {
                            worldGenLevel.setCurrentlyGenerating(supplier);
                            structureFeatureManager.startsForFeature(sectionPos, structureFeature2).forEach(structureStart -> structureStart.placeInChunk(worldGenLevel, structureFeatureManager, this, worldgenRandom, ChunkGenerator.getWritableArea(chunkAccess), chunkPos2));
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
                    Supplier<String> supplier2 = () -> registry.getResourceKey(placedFeature2).map(Object::toString).orElseGet(placedFeature2::toString);
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

    public StructureSettings getSettings() {
        return this.settings;
    }

    public int getSpawnHeight(LevelHeightAccessor levelHeightAccessor) {
        return 64;
    }

    public BiomeSource getBiomeSource() {
        return this.runtimeBiomeSource;
    }

    public abstract int getGenDepth();

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Holder<Biome> holder, StructureFeatureManager structureFeatureManager, MobCategory mobCategory, BlockPos blockPos) {
        return holder.value().getMobSettings().getMobs(mobCategory);
    }

    public void createStructures(RegistryAccess registryAccess, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, StructureManager structureManager, long l) {
        ChunkPos chunkPos = chunkAccess.getPos();
        SectionPos sectionPos = SectionPos.bottomOf(chunkAccess);
        Registry<ConfiguredStructureFeature<?, ?>> registry = registryAccess.registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
        block0: for (StructureFeature structureFeature : Registry.STRUCTURE_FEATURE) {
            StructureStart<?> structureStart;
            StructurePlacement structurePlacement = this.settings.getConfig(structureFeature);
            if (structurePlacement == null || (structureStart = structureFeatureManager.getStartForFeature(sectionPos, structureFeature, chunkAccess)) != null && structureStart.isValid()) continue;
            int i = ChunkGenerator.fetchReferences(structureFeatureManager, chunkAccess, sectionPos, structureFeature);
            if (structurePlacement.isFeatureChunk(this, chunkPos.x, chunkPos.z)) {
                for (Map.Entry entry : ((ImmutableMap)this.settings.structures(structureFeature).asMap()).entrySet()) {
                    Optional<ConfiguredStructureFeature<?, ?>> optional = registry.getOptional((ResourceKey)entry.getKey());
                    if (optional.isEmpty()) continue;
                    Predicate<ResourceKey> predicate = Set.copyOf((Collection)entry.getValue())::contains;
                    StructureStart<?> structureStart2 = optional.get().generate(registryAccess, this, this.biomeSource, structureManager, l, chunkPos, i, chunkAccess, holder -> this.adjustBiome((Holder<Biome>)holder).is(predicate));
                    if (!structureStart2.isValid()) continue;
                    structureFeatureManager.setStartForFeature(sectionPos, structureFeature, structureStart2, chunkAccess);
                    continue block0;
                }
            }
            structureFeatureManager.setStartForFeature(sectionPos, structureFeature, StructureStart.INVALID_START, chunkAccess);
        }
    }

    private static int fetchReferences(StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, SectionPos sectionPos, StructureFeature<?> structureFeature) {
        StructureStart<?> structureStart = structureFeatureManager.getStartForFeature(sectionPos, structureFeature, chunkAccess);
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
                for (StructureStart<?> structureStart : worldGenLevel.getChunk(n, o).getAllStarts().values()) {
                    try {
                        if (!structureStart.isValid() || !structureStart.getBoundingBox().intersects(l, m, l + 15, m + 15)) continue;
                        structureFeatureManager.addReferenceForFeature(sectionPos, structureStart.getFeature(), p, chunkAccess);
                        DebugPackets.sendStructurePacket(worldGenLevel, structureStart);
                    } catch (Exception exception) {
                        CrashReport crashReport = CrashReport.forThrowable(exception, "Generating structure reference");
                        CrashReportCategory crashReportCategory = crashReport.addCategory("Structure");
                        crashReportCategory.setDetail("Id", () -> Registry.STRUCTURE_FEATURE.getKey(structureStart.getFeature()).toString());
                        crashReportCategory.setDetail("Name", () -> structureStart.getFeature().getFeatureName());
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

    public List<ChunkPos> getRingPositionsFor(ConcentricRingsStructurePlacement concentricRingsStructurePlacement) {
        return this.ringPositions.get(concentricRingsStructurePlacement);
    }

    public long seed() {
        return this.seed;
    }

    static {
        Registry.register(Registry.CHUNK_GENERATOR, "noise", NoiseBasedChunkGenerator.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, "flat", FlatLevelSource.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, "debug", DebugLevelSource.CODEC);
        CODEC = Registry.CHUNK_GENERATOR.byNameCodec().dispatchStable(ChunkGenerator::codec, Function.identity());
    }
}

