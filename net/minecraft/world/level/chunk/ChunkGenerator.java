/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArraySet;
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
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.jetbrains.annotations.Nullable;

public abstract class ChunkGenerator
implements BiomeManager.NoiseBiomeSource {
    public static final Codec<ChunkGenerator> CODEC;
    protected final BiomeSource biomeSource;
    protected final BiomeSource runtimeBiomeSource;
    private final StructureSettings settings;
    private final long strongholdSeed;
    private final List<ChunkPos> strongholdPositions = Lists.newArrayList();

    public ChunkGenerator(BiomeSource biomeSource, StructureSettings structureSettings) {
        this(biomeSource, biomeSource, structureSettings, 0L);
    }

    public ChunkGenerator(BiomeSource biomeSource, BiomeSource biomeSource2, StructureSettings structureSettings, long l) {
        this.biomeSource = biomeSource;
        this.runtimeBiomeSource = biomeSource2;
        this.settings = structureSettings;
        this.strongholdSeed = l;
    }

    private void generateStrongholds() {
        if (!this.strongholdPositions.isEmpty()) {
            return;
        }
        StrongholdConfiguration strongholdConfiguration = this.settings.stronghold();
        if (strongholdConfiguration == null || strongholdConfiguration.count() == 0) {
            return;
        }
        ArrayList<Biome> list = Lists.newArrayList();
        for (Biome biome : this.biomeSource.possibleBiomes()) {
            if (!ChunkGenerator.validStrongholdBiome(biome)) continue;
            list.add(biome);
        }
        int i = strongholdConfiguration.distance();
        int j = strongholdConfiguration.count();
        int k = strongholdConfiguration.spread();
        Random random = new Random();
        random.setSeed(this.strongholdSeed);
        double d = random.nextDouble() * Math.PI * 2.0;
        int l = 0;
        int m = 0;
        for (int n = 0; n < j; ++n) {
            double e = (double)(4 * i + i * m * 6) + (random.nextDouble() - 0.5) * ((double)i * 2.5);
            int o = (int)Math.round(Math.cos(d) * e);
            int p = (int)Math.round(Math.sin(d) * e);
            BlockPos blockPos = this.biomeSource.findBiomeHorizontal(SectionPos.sectionToBlockCoord(o, 8), 0, SectionPos.sectionToBlockCoord(p, 8), 112, list::contains, random, this.climateSampler());
            if (blockPos != null) {
                o = SectionPos.blockToSectionCoord(blockPos.getX());
                p = SectionPos.blockToSectionCoord(blockPos.getZ());
            }
            this.strongholdPositions.add(new ChunkPos(o, p));
            d += Math.PI * 2 / (double)k;
            if (++l != k) continue;
            l = 0;
            k += 2 * k / (++m + 1);
            k = Math.min(k, j - n);
            d += random.nextDouble() * Math.PI * 2.0;
        }
    }

    private static boolean validStrongholdBiome(Biome biome) {
        Biome.BiomeCategory biomeCategory = biome.getBiomeCategory();
        return biomeCategory != Biome.BiomeCategory.OCEAN && biomeCategory != Biome.BiomeCategory.RIVER && biomeCategory != Biome.BiomeCategory.BEACH && biomeCategory != Biome.BiomeCategory.SWAMP && biomeCategory != Biome.BiomeCategory.NETHER && biomeCategory != Biome.BiomeCategory.THEEND;
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
    public Biome getNoiseBiome(int i, int j, int k) {
        return this.getBiomeSource().getNoiseBiome(i, j, k, this.climateSampler());
    }

    public abstract void applyCarvers(WorldGenRegion var1, long var2, BiomeManager var4, StructureFeatureManager var5, ChunkAccess var6, GenerationStep.Carving var7);

    @Nullable
    public BlockPos findNearestMapFeature(ServerLevel serverLevel, StructureFeature<?> structureFeature, BlockPos blockPos, int i, boolean bl) {
        if (structureFeature == StructureFeature.STRONGHOLD) {
            this.generateStrongholds();
            BlockPos blockPos2 = null;
            double d = Double.MAX_VALUE;
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (ChunkPos chunkPos : this.strongholdPositions) {
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
        StructureFeatureConfiguration structureFeatureConfiguration = this.settings.getConfig(structureFeature);
        ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> immutableMultimap = this.settings.structures(structureFeature);
        if (structureFeatureConfiguration == null || immutableMultimap.isEmpty()) {
            return null;
        }
        Registry<Biome> registry = serverLevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        Set set = this.runtimeBiomeSource.possibleBiomes().stream().flatMap(biome -> registry.getResourceKey((Biome)biome).stream()).collect(Collectors.toSet());
        if (immutableMultimap.values().stream().noneMatch(set::contains)) {
            return null;
        }
        return structureFeature.getNearestGeneratedFeature(serverLevel, serverLevel.structureFeatureManager(), blockPos, i, bl, serverLevel.getSeed(), structureFeatureConfiguration);
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
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.seedUniquifier()));
        long l = worldgenRandom.setDecorationSeed(worldGenLevel.getSeed(), blockPos.getX(), blockPos.getZ());
        ObjectArraySet set = new ObjectArraySet();
        if (this instanceof FlatLevelSource) {
            set.addAll(this.biomeSource.possibleBiomes());
        } else {
            ChunkPos.rangeClosed(sectionPos.chunk(), 1).forEach(chunkPos -> {
                ChunkAccess chunkAccess = worldGenLevel.getChunk(chunkPos.x, chunkPos.z);
                for (LevelChunkSection levelChunkSection : chunkAccess.getSections()) {
                    levelChunkSection.getBiomes().getAll(set::add);
                }
            });
            set.retainAll(this.biomeSource.possibleBiomes());
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
                    List<List<Supplier<PlacedFeature>>> list3 = biome.getGenerationSettings().features();
                    if (k >= list3.size()) continue;
                    List<Supplier<PlacedFeature>> list4 = list3.get(k);
                    BiomeSource.StepFeatureData stepFeatureData = list.get(k);
                    list4.stream().map(Supplier::get).forEach(placedFeature -> intSet.add(stepFeatureData.indexMapping().applyAsInt((PlacedFeature)placedFeature)));
                }
                int n = intSet.size();
                int[] is = intSet.toIntArray();
                Arrays.sort(is);
                BiomeSource.StepFeatureData stepFeatureData2 = list.get(k);
                for (int o = 0; o < n; ++o) {
                    PlacedFeature placedFeature2 = stepFeatureData2.features().get(is[o]);
                    Supplier<String> supplier2 = () -> registry.getResourceKey(placedFeature2).map(Object::toString).orElseGet(placedFeature2::toString);
                    worldgenRandom.setFeatureSeed(l, m, k);
                    try {
                        worldGenLevel.setCurrentlyGenerating(supplier2);
                        placedFeature2.placeWithBiomeCheck(worldGenLevel, this, worldgenRandom, blockPos);
                    } catch (Exception exception2) {
                        CrashReport crashReport2 = CrashReport.forThrowable(exception2, "Feature placement");
                        crashReport2.addCategory("Feature").setDetail("Description", supplier2::get);
                        throw new ReportedException(crashReport2);
                    }
                    ++m;
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

    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Biome biome, StructureFeatureManager structureFeatureManager, MobCategory mobCategory, BlockPos blockPos) {
        return biome.getMobSettings().getMobs(mobCategory);
    }

    public void createStructures(RegistryAccess registryAccess, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, StructureManager structureManager, long l) {
        StructureStart<?> structureStart;
        ChunkPos chunkPos = chunkAccess.getPos();
        SectionPos sectionPos = SectionPos.bottomOf(chunkAccess);
        StructureFeatureConfiguration structureFeatureConfiguration = this.settings.getConfig(StructureFeature.STRONGHOLD);
        if (!(structureFeatureConfiguration == null || (structureStart = structureFeatureManager.getStartForFeature(sectionPos, StructureFeature.STRONGHOLD, chunkAccess)) != null && structureStart.isValid())) {
            StructureStart<?> structureStart2 = StructureFeatures.STRONGHOLD.generate(registryAccess, this, this.biomeSource, structureManager, l, chunkPos, ChunkGenerator.fetchReferences(structureFeatureManager, chunkAccess, sectionPos, StructureFeature.STRONGHOLD), structureFeatureConfiguration, chunkAccess, ChunkGenerator::validStrongholdBiome);
            structureFeatureManager.setStartForFeature(sectionPos, StructureFeature.STRONGHOLD, structureStart2, chunkAccess);
        }
        Registry<Biome> registry = registryAccess.registryOrThrow(Registry.BIOME_REGISTRY);
        block0: for (StructureFeature structureFeature : Registry.STRUCTURE_FEATURE) {
            StructureStart<?> structureStart3;
            StructureFeatureConfiguration structureFeatureConfiguration2;
            if (structureFeature == StructureFeature.STRONGHOLD || (structureFeatureConfiguration2 = this.settings.getConfig(structureFeature)) == null || (structureStart3 = structureFeatureManager.getStartForFeature(sectionPos, structureFeature, chunkAccess)) != null && structureStart3.isValid()) continue;
            int i = ChunkGenerator.fetchReferences(structureFeatureManager, chunkAccess, sectionPos, structureFeature);
            for (Map.Entry entry : ((ImmutableMap)this.settings.structures(structureFeature).asMap()).entrySet()) {
                StructureStart<?> structureStart4 = ((ConfiguredStructureFeature)entry.getKey()).generate(registryAccess, this, this.biomeSource, structureManager, l, chunkPos, i, structureFeatureConfiguration2, chunkAccess, biome -> this.validBiome(registry, ((Collection)entry.getValue())::contains, (Biome)biome));
                if (!structureStart4.isValid()) continue;
                structureFeatureManager.setStartForFeature(sectionPos, structureFeature, structureStart4, chunkAccess);
                continue block0;
            }
            structureFeatureManager.setStartForFeature(sectionPos, structureFeature, StructureStart.INVALID_START, chunkAccess);
        }
    }

    private static int fetchReferences(StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, SectionPos sectionPos, StructureFeature<?> structureFeature) {
        StructureStart<?> structureStart = structureFeatureManager.getStartForFeature(sectionPos, structureFeature, chunkAccess);
        return structureStart != null ? structureStart.getReferences() : 0;
    }

    protected boolean validBiome(Registry<Biome> registry, Predicate<ResourceKey<Biome>> predicate, Biome biome) {
        return registry.getResourceKey(biome).filter(predicate).isPresent();
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

    public boolean hasStronghold(ChunkPos chunkPos) {
        this.generateStrongholds();
        return this.strongholdPositions.contains(chunkPos);
    }

    static {
        Registry.register(Registry.CHUNK_GENERATOR, "noise", NoiseBasedChunkGenerator.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, "flat", FlatLevelSource.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, "debug", DebugLevelSource.CODEC);
        CODEC = Registry.CHUNK_GENERATOR.byNameCodec().dispatchStable(ChunkGenerator::codec, Function.identity());
    }
}

