/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.function.Supplier;
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
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

public final class NoiseBasedChunkGenerator
extends ChunkGenerator {
    public static final Codec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BiomeSource.CODEC.fieldOf("biome_source")).forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.biomeSource), ((MapCodec)NoiseGeneratorSettings.CODEC.fieldOf("settings")).forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.settings)).apply((Applicative<NoiseBasedChunkGenerator, ?>)instance, instance.stable(NoiseBasedChunkGenerator::new)));
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private final Holder<NoiseGeneratorSettings> settings;
    private final Supplier<Aquifer.FluidPicker> globalFluidPicker;

    public NoiseBasedChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> holder) {
        super(biomeSource);
        this.settings = holder;
        this.globalFluidPicker = Suppliers.memoize(() -> NoiseBasedChunkGenerator.createFluidPicker((NoiseGeneratorSettings)holder.value()));
    }

    private static Aquifer.FluidPicker createFluidPicker(NoiseGeneratorSettings noiseGeneratorSettings) {
        Aquifer.FluidStatus fluidStatus = new Aquifer.FluidStatus(-54, Blocks.LAVA.defaultBlockState());
        int i = noiseGeneratorSettings.seaLevel();
        Aquifer.FluidStatus fluidStatus2 = new Aquifer.FluidStatus(i, noiseGeneratorSettings.defaultFluid());
        Aquifer.FluidStatus fluidStatus3 = new Aquifer.FluidStatus(DimensionType.MIN_Y * 2, Blocks.AIR.defaultBlockState());
        return (j, k, l) -> {
            if (k < Math.min(-54, i)) {
                return fluidStatus;
            }
            return fluidStatus2;
        };
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(Executor executor, RandomState randomState, Blender blender, StructureManager structureManager, ChunkAccess chunkAccess) {
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("init_biomes", () -> {
            this.doCreateBiomes(blender, randomState, structureManager, chunkAccess);
            return chunkAccess;
        }), Util.backgroundExecutor());
    }

    private void doCreateBiomes(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess2) {
        NoiseChunk noiseChunk = chunkAccess2.getOrCreateNoiseChunk(chunkAccess -> this.createNoiseChunk((ChunkAccess)chunkAccess, structureManager, blender, randomState));
        BiomeResolver biomeResolver = BelowZeroRetrogen.getBiomeResolver(blender.getBiomeResolver(this.biomeSource), chunkAccess2);
        chunkAccess2.fillBiomesFromNoise(biomeResolver, noiseChunk.cachedClimateSampler(randomState.router(), this.settings.value().spawnTarget()));
    }

    private NoiseChunk createNoiseChunk(ChunkAccess chunkAccess, StructureManager structureManager, Blender blender, RandomState randomState) {
        return NoiseChunk.forChunk(chunkAccess, randomState, Beardifier.forStructuresInChunk(structureManager, chunkAccess.getPos()), this.settings.value(), this.globalFluidPicker.get(), blender);
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
        MutableObject<NoiseColumn> mutableObject = new MutableObject<NoiseColumn>();
        this.iterateNoiseColumn(levelHeightAccessor, randomState, i, j, mutableObject, null);
        return mutableObject.getValue();
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        NoiseRouter noiseRouter = randomState.router();
        DensityFunction.SinglePointContext singlePointContext = new DensityFunction.SinglePointContext(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        double d = noiseRouter.ridges().compute(singlePointContext);
        list.add("NoiseRouter T: " + decimalFormat.format(noiseRouter.temperature().compute(singlePointContext)) + " V: " + decimalFormat.format(noiseRouter.vegetation().compute(singlePointContext)) + " C: " + decimalFormat.format(noiseRouter.continents().compute(singlePointContext)) + " E: " + decimalFormat.format(noiseRouter.erosion().compute(singlePointContext)) + " D: " + decimalFormat.format(noiseRouter.depth().compute(singlePointContext)) + " W: " + decimalFormat.format(d) + " PV: " + decimalFormat.format(NoiseRouterData.peaksAndValleys((float)d)) + " AS: " + decimalFormat.format(noiseRouter.initialDensityWithoutJaggedness().compute(singlePointContext)) + " N: " + decimalFormat.format(noiseRouter.finalDensity().compute(singlePointContext)));
    }

    private OptionalInt iterateNoiseColumn(LevelHeightAccessor levelHeightAccessor, RandomState randomState, int i, int j, @Nullable MutableObject<NoiseColumn> mutableObject, @Nullable Predicate<BlockState> predicate) {
        BlockState[] blockStates;
        NoiseSettings noiseSettings = this.settings.value().noiseSettings().clampToHeightAccessor(levelHeightAccessor);
        int k = noiseSettings.getCellHeight();
        int l = noiseSettings.minY();
        int m = Mth.floorDiv(l, k);
        int n = Mth.floorDiv(noiseSettings.height(), k);
        if (n <= 0) {
            return OptionalInt.empty();
        }
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
        NoiseChunk noiseChunk = new NoiseChunk(1, randomState, t, u, noiseSettings, DensityFunctions.BeardifierMarker.INSTANCE, this.settings.value(), this.globalFluidPicker.get(), Blender.empty());
        noiseChunk.initializeForFirstCellX();
        noiseChunk.advanceCellX(0);
        for (int v = n - 1; v >= 0; --v) {
            noiseChunk.selectCellYZ(v, 0);
            for (int w = k - 1; w >= 0; --w) {
                BlockState blockState2;
                int x = (m + v) * k + w;
                double f = (double)w / (double)k;
                noiseChunk.updateForY(x, f);
                noiseChunk.updateForX(i, d);
                noiseChunk.updateForZ(j, e);
                BlockState blockState = noiseChunk.getInterpolatedState();
                BlockState blockState3 = blockState2 = blockState == null ? this.settings.value().defaultBlock() : blockState;
                if (blockStates != null) {
                    int y = v * k + w;
                    blockStates[y] = blockState2;
                }
                if (predicate == null || !predicate.test(blockState2)) continue;
                noiseChunk.stopInterpolation();
                return OptionalInt.of(x + 1);
            }
        }
        noiseChunk.stopInterpolation();
        return OptionalInt.empty();
    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess) {
        if (SharedConstants.debugVoidTerrain(chunkAccess.getPos())) {
            return;
        }
        WorldGenerationContext worldGenerationContext = new WorldGenerationContext(this, worldGenRegion);
        this.buildSurface(chunkAccess, worldGenerationContext, randomState, structureManager, worldGenRegion.getBiomeManager(), worldGenRegion.registryAccess().registryOrThrow(Registries.BIOME), Blender.of(worldGenRegion));
    }

    @VisibleForTesting
    public void buildSurface(ChunkAccess chunkAccess2, WorldGenerationContext worldGenerationContext, RandomState randomState, StructureManager structureManager, BiomeManager biomeManager, Registry<Biome> registry, Blender blender) {
        NoiseChunk noiseChunk = chunkAccess2.getOrCreateNoiseChunk(chunkAccess -> this.createNoiseChunk((ChunkAccess)chunkAccess, structureManager, blender, randomState));
        NoiseGeneratorSettings noiseGeneratorSettings = this.settings.value();
        randomState.surfaceSystem().buildSurface(randomState, biomeManager, registry, noiseGeneratorSettings.useLegacyRandomSource(), worldGenerationContext, chunkAccess2, noiseChunk, noiseGeneratorSettings.surfaceRule());
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess2, GenerationStep.Carving carving) {
        BiomeManager biomeManager2 = biomeManager.withDifferentSource((i, j, k) -> this.biomeSource.getNoiseBiome(i, j, k, randomState.sampler()));
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        int i2 = 8;
        ChunkPos chunkPos = chunkAccess2.getPos();
        NoiseChunk noiseChunk = chunkAccess2.getOrCreateNoiseChunk(chunkAccess -> this.createNoiseChunk((ChunkAccess)chunkAccess, structureManager, Blender.of(worldGenRegion), randomState));
        Aquifer aquifer = noiseChunk.aquifer();
        CarvingContext carvingContext = new CarvingContext(this, worldGenRegion.registryAccess(), chunkAccess2.getHeightAccessorForGeneration(), noiseChunk, randomState, this.settings.value().surfaceRule());
        CarvingMask carvingMask = ((ProtoChunk)chunkAccess2).getOrCreateCarvingMask(carving);
        for (int j2 = -8; j2 <= 8; ++j2) {
            for (int k2 = -8; k2 <= 8; ++k2) {
                ChunkPos chunkPos2 = new ChunkPos(chunkPos.x + j2, chunkPos.z + k2);
                ChunkAccess chunkAccess22 = worldGenRegion.getChunk(chunkPos2.x, chunkPos2.z);
                BiomeGenerationSettings biomeGenerationSettings = chunkAccess22.carverBiome(() -> this.getBiomeGenerationSettings(this.biomeSource.getNoiseBiome(QuartPos.fromBlock(chunkPos2.getMinBlockX()), 0, QuartPos.fromBlock(chunkPos2.getMinBlockZ()), randomState.sampler())));
                Iterable<Holder<ConfiguredWorldCarver<?>>> iterable = biomeGenerationSettings.getCarvers(carving);
                int m = 0;
                for (Holder<ConfiguredWorldCarver<?>> holder : iterable) {
                    ConfiguredWorldCarver<?> configuredWorldCarver = holder.value();
                    worldgenRandom.setLargeFeatureSeed(l + (long)m, chunkPos2.x, chunkPos2.z);
                    if (configuredWorldCarver.isStartChunk(worldgenRandom)) {
                        configuredWorldCarver.carve(carvingContext, chunkAccess2, biomeManager2::getBiome, worldgenRandom, aquifer, chunkPos2, carvingMask);
                    }
                    ++m;
                }
            }
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess2) {
        NoiseSettings noiseSettings = this.settings.value().noiseSettings().clampToHeightAccessor(chunkAccess2.getHeightAccessorForGeneration());
        int i = noiseSettings.minY();
        int j = Mth.floorDiv(i, noiseSettings.getCellHeight());
        int k = Mth.floorDiv(noiseSettings.height(), noiseSettings.getCellHeight());
        if (k <= 0) {
            return CompletableFuture.completedFuture(chunkAccess2);
        }
        int l = chunkAccess2.getSectionIndex(k * noiseSettings.getCellHeight() - 1 + i);
        int m = chunkAccess2.getSectionIndex(i);
        HashSet<LevelChunkSection> set = Sets.newHashSet();
        for (int n = l; n >= m; --n) {
            LevelChunkSection levelChunkSection = chunkAccess2.getSection(n);
            levelChunkSection.acquire();
            set.add(levelChunkSection);
        }
        return CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName("wgen_fill_noise", () -> this.doFill(blender, structureManager, randomState, chunkAccess2, j, k)), Util.backgroundExecutor()).whenCompleteAsync((chunkAccess, throwable) -> {
            for (LevelChunkSection levelChunkSection : set) {
                levelChunkSection.release();
            }
        }, executor);
    }

    private ChunkAccess doFill(Blender blender, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess2, int i, int j) {
        NoiseChunk noiseChunk = chunkAccess2.getOrCreateNoiseChunk(chunkAccess -> this.createNoiseChunk((ChunkAccess)chunkAccess, structureManager, blender, randomState));
        Heightmap heightmap = chunkAccess2.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunkAccess2.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        ChunkPos chunkPos = chunkAccess2.getPos();
        int k = chunkPos.getMinBlockX();
        int l = chunkPos.getMinBlockZ();
        Aquifer aquifer = noiseChunk.aquifer();
        noiseChunk.initializeForFirstCellX();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int m = noiseChunk.cellWidth();
        int n = noiseChunk.cellHeight();
        int o = 16 / m;
        int p = 16 / m;
        for (int q = 0; q < o; ++q) {
            noiseChunk.advanceCellX(q);
            for (int r = 0; r < p; ++r) {
                LevelChunkSection levelChunkSection = chunkAccess2.getSection(chunkAccess2.getSectionsCount() - 1);
                for (int s = j - 1; s >= 0; --s) {
                    noiseChunk.selectCellYZ(s, r);
                    for (int t = n - 1; t >= 0; --t) {
                        int u = (i + s) * n + t;
                        int v = u & 0xF;
                        int w = chunkAccess2.getSectionIndex(u);
                        if (chunkAccess2.getSectionIndex(levelChunkSection.bottomBlockY()) != w) {
                            levelChunkSection = chunkAccess2.getSection(w);
                        }
                        double d = (double)t / (double)n;
                        noiseChunk.updateForY(u, d);
                        for (int x = 0; x < m; ++x) {
                            int y = k + q * m + x;
                            int z = y & 0xF;
                            double e = (double)x / (double)m;
                            noiseChunk.updateForX(y, e);
                            for (int aa = 0; aa < m; ++aa) {
                                int ab = l + r * m + aa;
                                int ac = ab & 0xF;
                                double f = (double)aa / (double)m;
                                noiseChunk.updateForZ(ab, f);
                                BlockState blockState = noiseChunk.getInterpolatedState();
                                if (blockState == null) {
                                    blockState = this.settings.value().defaultBlock();
                                }
                                if ((blockState = this.debugPreliminarySurfaceLevel(noiseChunk, y, u, ab, blockState)) == AIR || SharedConstants.debugVoidTerrain(chunkAccess2.getPos())) continue;
                                if (blockState.getLightEmission() != 0 && chunkAccess2 instanceof ProtoChunk) {
                                    mutableBlockPos.set(y, u, ab);
                                    ((ProtoChunk)chunkAccess2).addLight(mutableBlockPos);
                                }
                                levelChunkSection.setBlockState(z, v, ac, blockState, false);
                                heightmap.update(z, u, ac, blockState);
                                heightmap2.update(z, u, ac, blockState);
                                if (!aquifer.shouldScheduleFluidUpdate() || blockState.getFluidState().isEmpty()) continue;
                                mutableBlockPos.set(y, u, ab);
                                chunkAccess2.markPosForPostprocessing(mutableBlockPos);
                            }
                        }
                    }
                }
            }
            noiseChunk.swapSlices();
        }
        noiseChunk.stopInterpolation();
        return chunkAccess2;
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
        if (this.settings.value().disableMobGeneration()) {
            return;
        }
        ChunkPos chunkPos = worldGenRegion.getCenter();
        Holder<Biome> holder = worldGenRegion.getBiome(chunkPos.getWorldPosition().atY(worldGenRegion.getMaxBuildHeight() - 1));
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        worldgenRandom.setDecorationSeed(worldGenRegion.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
        NaturalSpawner.spawnMobsForChunkGeneration(worldGenRegion, holder, chunkPos, worldgenRandom);
    }
}

