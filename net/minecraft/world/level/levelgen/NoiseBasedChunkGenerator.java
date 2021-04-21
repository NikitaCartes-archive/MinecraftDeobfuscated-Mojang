/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
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
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.BaseStoneSource;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.Cavifier;
import net.minecraft.world.level.levelgen.DepthBasedReplacingBaseStoneSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseInterpolator;
import net.minecraft.world.level.levelgen.NoiseSampler;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.OreVeinifier;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.SimpleRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.level.levelgen.synth.SurfaceNoise;
import org.jetbrains.annotations.Nullable;

public final class NoiseBasedChunkGenerator
extends ChunkGenerator {
    public static final Codec<NoiseBasedChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)BiomeSource.CODEC.fieldOf("biome_source")).forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.biomeSource), ((MapCodec)Codec.LONG.fieldOf("seed")).stable().forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.seed), ((MapCodec)NoiseGeneratorSettings.CODEC.fieldOf("settings")).forGetter(noiseBasedChunkGenerator -> noiseBasedChunkGenerator.settings)).apply((Applicative<NoiseBasedChunkGenerator, ?>)instance, instance.stable(NoiseBasedChunkGenerator::new)));
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState[] EMPTY_COLUMN = new BlockState[0];
    private final int cellHeight;
    private final int cellWidth;
    private final int cellCountX;
    private final int cellCountY;
    private final int cellCountZ;
    private final SurfaceNoise surfaceNoise;
    private final NormalNoise barrierNoise;
    private final NormalNoise waterLevelNoise;
    private final NormalNoise lavaNoise;
    protected final BlockState defaultBlock;
    protected final BlockState defaultFluid;
    private final long seed;
    protected final Supplier<NoiseGeneratorSettings> settings;
    private final int height;
    private final NoiseSampler sampler;
    private final BaseStoneSource baseStoneSource;
    private final OreVeinifier oreVeinifier;

    public NoiseBasedChunkGenerator(BiomeSource biomeSource, long l, Supplier<NoiseGeneratorSettings> supplier) {
        this(biomeSource, biomeSource, l, supplier);
    }

    private NoiseBasedChunkGenerator(BiomeSource biomeSource, BiomeSource biomeSource2, long l, Supplier<NoiseGeneratorSettings> supplier) {
        super(biomeSource, biomeSource2, supplier.get().structureSettings(), l);
        SimplexNoise simplexNoise;
        this.seed = l;
        NoiseGeneratorSettings noiseGeneratorSettings = supplier.get();
        this.settings = supplier;
        NoiseSettings noiseSettings = noiseGeneratorSettings.noiseSettings();
        this.height = noiseSettings.height();
        this.cellHeight = QuartPos.toBlock(noiseSettings.noiseSizeVertical());
        this.cellWidth = QuartPos.toBlock(noiseSettings.noiseSizeHorizontal());
        this.defaultBlock = noiseGeneratorSettings.getDefaultBlock();
        this.defaultFluid = noiseGeneratorSettings.getDefaultFluid();
        this.cellCountX = 16 / this.cellWidth;
        this.cellCountY = noiseSettings.height() / this.cellHeight;
        this.cellCountZ = 16 / this.cellWidth;
        WorldgenRandom worldgenRandom = new WorldgenRandom(l);
        BlendedNoise blendedNoise = new BlendedNoise(worldgenRandom);
        this.surfaceNoise = noiseSettings.useSimplexSurfaceNoise() ? new PerlinSimplexNoise((RandomSource)worldgenRandom, IntStream.rangeClosed(-3, 0)) : new PerlinNoise((RandomSource)worldgenRandom, IntStream.rangeClosed(-3, 0));
        worldgenRandom.consumeCount(2620);
        PerlinNoise perlinNoise = new PerlinNoise((RandomSource)worldgenRandom, IntStream.rangeClosed(-15, 0));
        if (noiseSettings.islandNoiseOverride()) {
            WorldgenRandom worldgenRandom2 = new WorldgenRandom(l);
            worldgenRandom2.consumeCount(17292);
            simplexNoise = new SimplexNoise(worldgenRandom2);
        } else {
            simplexNoise = null;
        }
        this.barrierNoise = NormalNoise.create((RandomSource)new SimpleRandomSource(worldgenRandom.nextLong()), -3, 1.0);
        this.waterLevelNoise = NormalNoise.create((RandomSource)new SimpleRandomSource(worldgenRandom.nextLong()), -3, 1.0, 0.0, 2.0);
        this.lavaNoise = NormalNoise.create((RandomSource)new SimpleRandomSource(worldgenRandom.nextLong()), -1, 1.0, 0.0);
        Cavifier cavifier = noiseGeneratorSettings.isNoiseCavesEnabled() ? new Cavifier(worldgenRandom, noiseSettings.minY() / this.cellHeight) : null;
        this.sampler = new NoiseSampler(biomeSource, this.cellWidth, this.cellHeight, this.cellCountY, noiseSettings, blendedNoise, simplexNoise, perlinNoise, cavifier);
        this.baseStoneSource = new DepthBasedReplacingBaseStoneSource(l, this.defaultBlock, Blocks.DEEPSLATE.defaultBlockState(), noiseGeneratorSettings);
        this.oreVeinifier = new OreVeinifier(l, this.defaultBlock, this.cellWidth, this.cellHeight, noiseGeneratorSettings.noiseSettings().minY());
    }

    private boolean isAquifersEnabled() {
        return this.settings.get().isAquifersEnabled();
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long l) {
        return new NoiseBasedChunkGenerator(this.biomeSource.withSeed(l), l, this.settings);
    }

    public boolean stable(long l, ResourceKey<NoiseGeneratorSettings> resourceKey) {
        return this.seed == l && this.settings.get().stable(resourceKey);
    }

    private double[] makeAndFillNoiseColumn(int i, int j, int k, int l) {
        double[] ds = new double[l + 1];
        this.fillNoiseColumn(ds, i, j, k, l);
        return ds;
    }

    private void fillNoiseColumn(double[] ds, int i, int j, int k, int l) {
        NoiseSettings noiseSettings = this.settings.get().noiseSettings();
        this.sampler.fillNoiseColumn(ds, i, j, noiseSettings, this.getSeaLevel(), k, l);
    }

    @Override
    public int getBaseHeight(int i, int j, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor) {
        int k = Math.max(this.settings.get().noiseSettings().minY(), levelHeightAccessor.getMinBuildHeight());
        int l = Math.min(this.settings.get().noiseSettings().minY() + this.settings.get().noiseSettings().height(), levelHeightAccessor.getMaxBuildHeight());
        int m = Mth.intFloorDiv(k, this.cellHeight);
        int n = Mth.intFloorDiv(l - k, this.cellHeight);
        if (n <= 0) {
            return levelHeightAccessor.getMinBuildHeight();
        }
        return this.iterateNoiseColumn(i, j, null, types.isOpaque(), m, n).orElse(levelHeightAccessor.getMinBuildHeight());
    }

    @Override
    public NoiseColumn getBaseColumn(int i, int j, LevelHeightAccessor levelHeightAccessor) {
        int k = Math.max(this.settings.get().noiseSettings().minY(), levelHeightAccessor.getMinBuildHeight());
        int l = Math.min(this.settings.get().noiseSettings().minY() + this.settings.get().noiseSettings().height(), levelHeightAccessor.getMaxBuildHeight());
        int m = Mth.intFloorDiv(k, this.cellHeight);
        int n = Mth.intFloorDiv(l - k, this.cellHeight);
        if (n <= 0) {
            return new NoiseColumn(k, EMPTY_COLUMN);
        }
        BlockState[] blockStates = new BlockState[n * this.cellHeight];
        this.iterateNoiseColumn(i, j, blockStates, null, m, n);
        return new NoiseColumn(k, blockStates);
    }

    @Override
    public BaseStoneSource getBaseStoneSource() {
        return this.baseStoneSource;
    }

    private OptionalInt iterateNoiseColumn(int i, int j, @Nullable BlockState[] blockStates, @Nullable Predicate<BlockState> predicate, int k, int l) {
        int m = SectionPos.blockToSectionCoord(i);
        int n = SectionPos.blockToSectionCoord(j);
        int o = Math.floorDiv(i, this.cellWidth);
        int p = Math.floorDiv(j, this.cellWidth);
        int q = Math.floorMod(i, this.cellWidth);
        int r = Math.floorMod(j, this.cellWidth);
        double d = (double)q / (double)this.cellWidth;
        double e = (double)r / (double)this.cellWidth;
        double[][] ds = new double[][]{this.makeAndFillNoiseColumn(o, p, k, l), this.makeAndFillNoiseColumn(o, p + 1, k, l), this.makeAndFillNoiseColumn(o + 1, p, k, l), this.makeAndFillNoiseColumn(o + 1, p + 1, k, l)};
        Aquifer aquifer = this.getAquifer(k, l, new ChunkPos(m, n));
        for (int s = l - 1; s >= 0; --s) {
            double f = ds[0][s];
            double g = ds[1][s];
            double h = ds[2][s];
            double t = ds[3][s];
            double u = ds[0][s + 1];
            double v = ds[1][s + 1];
            double w = ds[2][s + 1];
            double x = ds[3][s + 1];
            for (int y = this.cellHeight - 1; y >= 0; --y) {
                double z = (double)y / (double)this.cellHeight;
                double aa = Mth.lerp3(z, d, e, f, u, h, w, g, v, t, x);
                int ab = s * this.cellHeight + y;
                int ac = ab + k * this.cellHeight;
                BlockState blockState = this.updateNoiseAndGenerateBaseState(Beardifier.NO_BEARDS, aquifer, this.baseStoneSource, i, ac, j, aa);
                if (blockStates != null) {
                    blockStates[ab] = blockState;
                }
                if (predicate == null || !predicate.test(blockState)) continue;
                return OptionalInt.of(ac + 1);
            }
        }
        return OptionalInt.empty();
    }

    private Aquifer getAquifer(int i, int j, ChunkPos chunkPos) {
        if (!this.isAquifersEnabled()) {
            return Aquifer.createDisabled(this.getSeaLevel(), this.defaultFluid);
        }
        return Aquifer.create(chunkPos, this.barrierNoise, this.waterLevelNoise, this.lavaNoise, this.settings.get(), this.sampler, i * this.cellHeight, j * this.cellHeight);
    }

    protected BlockState updateNoiseAndGenerateBaseState(Beardifier beardifier, Aquifer aquifer, BaseStoneSource baseStoneSource, int i, int j, int k, double d) {
        double e = Mth.clamp(d / 200.0, -1.0, 1.0);
        e = e / 2.0 - e * e * e / 24.0;
        return aquifer.computeState(baseStoneSource, i, j, k, e += beardifier.beardifyOrBury(i, j, k));
    }

    @Override
    public void buildSurfaceAndBedrock(WorldGenRegion worldGenRegion, ChunkAccess chunkAccess) {
        ChunkPos chunkPos = chunkAccess.getPos();
        int i = chunkPos.x;
        int j = chunkPos.z;
        WorldgenRandom worldgenRandom = new WorldgenRandom();
        worldgenRandom.setBaseChunkSeed(i, j);
        ChunkPos chunkPos2 = chunkAccess.getPos();
        int k = chunkPos2.getMinBlockX();
        int l = chunkPos2.getMinBlockZ();
        double d = 0.0625;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int m = 0; m < 16; ++m) {
            for (int n = 0; n < 16; ++n) {
                int o = k + m;
                int p = l + n;
                int q = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, m, n) + 1;
                double e = this.surfaceNoise.getSurfaceNoiseValue((double)o * 0.0625, (double)p * 0.0625, 0.0625, (double)m * 0.0625) * 15.0;
                int r = this.settings.get().getMinSurfaceLevel();
                worldGenRegion.getBiome(mutableBlockPos.set(k + m, q, l + n)).buildSurfaceAt(worldgenRandom, chunkAccess, o, p, q, e, this.defaultBlock, this.defaultFluid, this.getSeaLevel(), r, worldGenRegion.getSeed());
            }
        }
        this.setBedrock(chunkAccess, worldgenRandom);
    }

    private void setBedrock(ChunkAccess chunkAccess, Random random) {
        boolean bl2;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int i = chunkAccess.getPos().getMinBlockX();
        int j = chunkAccess.getPos().getMinBlockZ();
        NoiseGeneratorSettings noiseGeneratorSettings = this.settings.get();
        int k = noiseGeneratorSettings.noiseSettings().minY();
        int l = k + noiseGeneratorSettings.getBedrockFloorPosition();
        int m = this.height - 1 + k - noiseGeneratorSettings.getBedrockRoofPosition();
        int n = 5;
        int o = chunkAccess.getMinBuildHeight();
        int p = chunkAccess.getMaxBuildHeight();
        boolean bl = m + 5 - 1 >= o && m < p;
        boolean bl3 = bl2 = l + 5 - 1 >= o && l < p;
        if (!bl && !bl2) {
            return;
        }
        for (BlockPos blockPos : BlockPos.betweenClosed(i, 0, j, i + 15, 0, j + 15)) {
            int q;
            if (bl) {
                for (q = 0; q < 5; ++q) {
                    if (q > random.nextInt(5)) continue;
                    chunkAccess.setBlockState(mutableBlockPos.set(blockPos.getX(), m - q, blockPos.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
                }
            }
            if (!bl2) continue;
            for (q = 4; q >= 0; --q) {
                if (q > random.nextInt(5)) continue;
                chunkAccess.setBlockState(mutableBlockPos.set(blockPos.getX(), l + q, blockPos.getZ()), Blocks.BEDROCK.defaultBlockState(), false);
            }
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess2) {
        NoiseSettings noiseSettings = this.settings.get().noiseSettings();
        int i = Math.max(noiseSettings.minY(), chunkAccess2.getMinBuildHeight());
        int j = Math.min(noiseSettings.minY() + noiseSettings.height(), chunkAccess2.getMaxBuildHeight());
        int k = Mth.intFloorDiv(i, this.cellHeight);
        int l = Mth.intFloorDiv(j - i, this.cellHeight);
        if (l <= 0) {
            return CompletableFuture.completedFuture(chunkAccess2);
        }
        int m = chunkAccess2.getSectionIndex(l * this.cellHeight - 1 + i);
        int n = chunkAccess2.getSectionIndex(i);
        HashSet<LevelChunkSection> set = Sets.newHashSet();
        for (int o = m; o >= n; --o) {
            LevelChunkSection levelChunkSection = chunkAccess2.getOrCreateSection(o);
            levelChunkSection.acquire();
            set.add(levelChunkSection);
        }
        return CompletableFuture.supplyAsync(() -> this.doFill(structureFeatureManager, chunkAccess2, k, l), Util.backgroundExecutor()).thenApplyAsync(chunkAccess -> {
            for (LevelChunkSection levelChunkSection : set) {
                levelChunkSection.release();
            }
            return chunkAccess;
        }, executor);
    }

    private ChunkAccess doFill(StructureFeatureManager structureFeatureManager, ChunkAccess chunkAccess, int i, int j) {
        Heightmap heightmap = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        ChunkPos chunkPos = chunkAccess.getPos();
        int k = chunkPos.getMinBlockX();
        int l = chunkPos.getMinBlockZ();
        Beardifier beardifier = new Beardifier(structureFeatureManager, chunkAccess);
        Aquifer aquifer = this.getAquifer(i, j, chunkPos);
        NoiseInterpolator noiseInterpolator2 = new NoiseInterpolator(this.cellCountX, j, this.cellCountZ, chunkPos, i, this::fillNoiseColumn);
        ArrayList<NoiseInterpolator> list = Lists.newArrayList(noiseInterpolator2);
        Pair<BaseStoneSource, DoubleConsumer> pair = this.createBaseStoneSource(i, chunkPos, list::add);
        BaseStoneSource baseStoneSource = pair.getFirst();
        DoubleConsumer doubleConsumer = pair.getSecond();
        list.forEach(NoiseInterpolator::initializeForFirstCellX);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int m = 0; m < this.cellCountX; ++m) {
            int n = m;
            list.forEach(noiseInterpolator -> noiseInterpolator.advanceCellX(n));
            for (int o = 0; o < this.cellCountZ; ++o) {
                LevelChunkSection levelChunkSection = chunkAccess.getOrCreateSection(chunkAccess.getSectionsCount() - 1);
                for (int p = j - 1; p >= 0; --p) {
                    int q = o;
                    int r = p;
                    list.forEach(noiseInterpolator -> noiseInterpolator.selectCellYZ(r, q));
                    for (int s = this.cellHeight - 1; s >= 0; --s) {
                        int t = (i + p) * this.cellHeight + s;
                        int u = t & 0xF;
                        int v = chunkAccess.getSectionIndex(t);
                        if (chunkAccess.getSectionIndex(levelChunkSection.bottomBlockY()) != v) {
                            levelChunkSection = chunkAccess.getOrCreateSection(v);
                        }
                        double d = (double)s / (double)this.cellHeight;
                        list.forEach(noiseInterpolator -> noiseInterpolator.updateForY(d));
                        for (int w = 0; w < this.cellWidth; ++w) {
                            int x = k + m * this.cellWidth + w;
                            int y = x & 0xF;
                            double e = (double)w / (double)this.cellWidth;
                            list.forEach(noiseInterpolator -> noiseInterpolator.updateForX(e));
                            for (int z = 0; z < this.cellWidth; ++z) {
                                int aa = l + o * this.cellWidth + z;
                                int ab = aa & 0xF;
                                double f = (double)z / (double)this.cellWidth;
                                double g = noiseInterpolator2.calculateValue(f);
                                doubleConsumer.accept(f);
                                BlockState blockState = this.updateNoiseAndGenerateBaseState(beardifier, aquifer, baseStoneSource, x, t, aa, g);
                                if (blockState == AIR) continue;
                                if (blockState.getLightEmission() != 0 && chunkAccess instanceof ProtoChunk) {
                                    mutableBlockPos.set(x, t, aa);
                                    ((ProtoChunk)chunkAccess).addLight(mutableBlockPos);
                                }
                                levelChunkSection.setBlockState(y, u, ab, blockState, false);
                                heightmap.update(y, t, ab, blockState);
                                heightmap2.update(y, t, ab, blockState);
                                if (!aquifer.shouldScheduleFluidUpdate() || blockState.getFluidState().isEmpty()) continue;
                                mutableBlockPos.set(x, t, aa);
                                chunkAccess.getLiquidTicks().scheduleTick(mutableBlockPos, blockState.getFluidState().getType(), 0);
                            }
                        }
                    }
                }
            }
            list.forEach(NoiseInterpolator::swapSlices);
        }
        return chunkAccess;
    }

    private Pair<BaseStoneSource, DoubleConsumer> createBaseStoneSource(int i2, ChunkPos chunkPos, Consumer<NoiseInterpolator> consumer) {
        if (!this.settings.get().isOreVeinsEnabled()) {
            return Pair.of(this.baseStoneSource, d -> {});
        }
        OreVeinNoiseSource oreVeinNoiseSource = new OreVeinNoiseSource(chunkPos, i2, this.seed + 1L);
        oreVeinNoiseSource.listInterpolators(consumer);
        BaseStoneSource baseStoneSource = (i, j, k) -> {
            BlockState blockState = oreVeinNoiseSource.getBaseBlock(i, j, k);
            if (blockState != this.defaultBlock) {
                return blockState;
            }
            return this.baseStoneSource.getBaseBlock(i, j, k);
        };
        return Pair.of(baseStoneSource, oreVeinNoiseSource::prepare);
    }

    @Override
    protected Aquifer createAquifer(ChunkAccess chunkAccess) {
        ChunkPos chunkPos = chunkAccess.getPos();
        int i = Math.max(this.settings.get().noiseSettings().minY(), chunkAccess.getMinBuildHeight());
        int j = Mth.intFloorDiv(i, this.cellHeight);
        return this.getAquifer(j, this.cellCountY, chunkPos);
    }

    @Override
    public int getGenDepth() {
        return this.height;
    }

    @Override
    public int getSeaLevel() {
        return this.settings.get().seaLevel();
    }

    @Override
    public int getMinY() {
        return this.settings.get().noiseSettings().minY();
    }

    @Override
    public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Biome biome, StructureFeatureManager structureFeatureManager, MobCategory mobCategory, BlockPos blockPos) {
        if (structureFeatureManager.getStructureAt(blockPos, true, StructureFeature.SWAMP_HUT).isValid()) {
            if (mobCategory == MobCategory.MONSTER) {
                return StructureFeature.SWAMP_HUT.getSpecialEnemies();
            }
            if (mobCategory == MobCategory.CREATURE) {
                return StructureFeature.SWAMP_HUT.getSpecialAnimals();
            }
        }
        if (mobCategory == MobCategory.MONSTER) {
            if (structureFeatureManager.getStructureAt(blockPos, false, StructureFeature.PILLAGER_OUTPOST).isValid()) {
                return StructureFeature.PILLAGER_OUTPOST.getSpecialEnemies();
            }
            if (structureFeatureManager.getStructureAt(blockPos, false, StructureFeature.OCEAN_MONUMENT).isValid()) {
                return StructureFeature.OCEAN_MONUMENT.getSpecialEnemies();
            }
            if (structureFeatureManager.getStructureAt(blockPos, true, StructureFeature.NETHER_BRIDGE).isValid()) {
                return StructureFeature.NETHER_BRIDGE.getSpecialEnemies();
            }
        }
        if (mobCategory == MobCategory.UNDERGROUND_WATER_CREATURE && structureFeatureManager.getStructureAt(blockPos, false, StructureFeature.OCEAN_MONUMENT).isValid()) {
            return StructureFeature.OCEAN_MONUMENT.getSpecialUndergroundWaterAnimals();
        }
        return super.getMobsAt(biome, structureFeatureManager, mobCategory, blockPos);
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
        if (this.settings.get().disableMobGeneration()) {
            return;
        }
        ChunkPos chunkPos = worldGenRegion.getCenter();
        Biome biome = worldGenRegion.getBiome(chunkPos.getWorldPosition());
        WorldgenRandom worldgenRandom = new WorldgenRandom();
        worldgenRandom.setDecorationSeed(worldGenRegion.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());
        NaturalSpawner.spawnMobsForChunkGeneration(worldGenRegion, biome, chunkPos, worldgenRandom);
    }

    class OreVeinNoiseSource
    implements BaseStoneSource {
        private final NoiseInterpolator veininess;
        private final NoiseInterpolator veinA;
        private final NoiseInterpolator veinB;
        private double factorZ;
        private final long seed;
        private final WorldgenRandom random = new WorldgenRandom();

        public OreVeinNoiseSource(ChunkPos chunkPos, int i, long l) {
            this.veininess = new NoiseInterpolator(NoiseBasedChunkGenerator.this.cellCountX, NoiseBasedChunkGenerator.this.cellCountY, NoiseBasedChunkGenerator.this.cellCountZ, chunkPos, i, NoiseBasedChunkGenerator.this.oreVeinifier::fillVeininessNoiseColumn);
            this.veinA = new NoiseInterpolator(NoiseBasedChunkGenerator.this.cellCountX, NoiseBasedChunkGenerator.this.cellCountY, NoiseBasedChunkGenerator.this.cellCountZ, chunkPos, i, NoiseBasedChunkGenerator.this.oreVeinifier::fillNoiseColumnA);
            this.veinB = new NoiseInterpolator(NoiseBasedChunkGenerator.this.cellCountX, NoiseBasedChunkGenerator.this.cellCountY, NoiseBasedChunkGenerator.this.cellCountZ, chunkPos, i, NoiseBasedChunkGenerator.this.oreVeinifier::fillNoiseColumnB);
            this.seed = l;
        }

        public void listInterpolators(Consumer<NoiseInterpolator> consumer) {
            consumer.accept(this.veininess);
            consumer.accept(this.veinA);
            consumer.accept(this.veinB);
        }

        public void prepare(double d) {
            this.factorZ = d;
        }

        @Override
        public BlockState getBaseBlock(int i, int j, int k) {
            double d = this.veininess.calculateValue(this.factorZ);
            double e = this.veinA.calculateValue(this.factorZ);
            double f = this.veinB.calculateValue(this.factorZ);
            this.random.setBaseStoneSeed(this.seed, i, j, k);
            return NoiseBasedChunkGenerator.this.oreVeinifier.oreVeinify(this.random, j, d, e, f);
        }
    }
}

