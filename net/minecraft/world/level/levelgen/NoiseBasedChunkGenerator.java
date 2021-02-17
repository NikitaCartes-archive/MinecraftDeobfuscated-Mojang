/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.levelgen.NoiseSampler;
import net.minecraft.world.level.levelgen.NoiseSettings;
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
    protected final BlockState defaultBlock;
    protected final BlockState defaultFluid;
    private final long seed;
    protected final Supplier<NoiseGeneratorSettings> settings;
    private final int height;
    private final NoiseSampler sampler;
    private final boolean aquifersEnabled;
    private final BaseStoneSource baseStoneSource;

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
        Cavifier cavifier = noiseGeneratorSettings.isNoiseCavesEnabled() ? new Cavifier(worldgenRandom, noiseSettings.minY() / this.cellHeight) : null;
        this.sampler = new NoiseSampler(biomeSource, this.cellWidth, this.cellHeight, this.cellCountY, noiseSettings, blendedNoise, simplexNoise, perlinNoise, cavifier);
        this.aquifersEnabled = noiseGeneratorSettings.isAquifersEnabled();
        this.baseStoneSource = new DepthBasedReplacingBaseStoneSource(l, this.defaultBlock, Blocks.GRIMSTONE.defaultBlockState());
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public ChunkGenerator withSeed(long l) {
        return new NoiseBasedChunkGenerator(this.biomeSource.withSeed(l), l, this.settings);
    }

    public boolean stable(long l, ResourceKey<NoiseGeneratorSettings> resourceKey) {
        return this.seed == l && this.settings.get().stable(resourceKey);
    }

    private double[] makeAndFillNoiseColumn(int i, int j, int k, int l) {
        double[] ds = new double[l + 1];
        this.sampler.fillNoiseColumn(ds, i, j, this.settings.get().noiseSettings(), this.getSeaLevel(), k, l);
        return ds;
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
        Aquifer aquifer = this.aquifersEnabled ? new Aquifer(m, n, this.barrierNoise, this.waterLevelNoise, this.settings.get(), this.sampler, l * this.cellHeight) : null;
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

    protected BlockState updateNoiseAndGenerateBaseState(Beardifier beardifier, @Nullable Aquifer aquifer, BaseStoneSource baseStoneSource, int i, int j, int k, double d) {
        BlockState blockState;
        double e = Mth.clamp(d / 200.0, -1.0, 1.0);
        e = e / 2.0 - e * e * e / 24.0;
        e += beardifier.beardifyOrBury(i, j, k);
        if (aquifer != null) {
            aquifer.computeAt(i, j, k);
            e += aquifer.getLastBarrierDensity();
        }
        if (e > 0.0) {
            blockState = baseStoneSource.getBaseStone(i, j, k, this.settings.get());
        } else {
            int l = aquifer == null ? this.getSeaLevel() : aquifer.getLastWaterLevel();
            blockState = j < l ? this.defaultFluid : AIR;
        }
        return blockState;
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
                worldGenRegion.getBiome(mutableBlockPos.set(k + m, q, l + n)).buildSurfaceAt(worldgenRandom, chunkAccess, o, p, q, e, this.defaultBlock, this.defaultFluid, this.getSeaLevel(), worldGenRegion.getSeed());
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
        int r;
        int q;
        NoiseSettings noiseSettings = this.settings.get().noiseSettings();
        int k = noiseSettings.minY();
        Heightmap heightmap = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        ChunkPos chunkPos = chunkAccess.getPos();
        int l = chunkPos.x;
        int m = chunkPos.z;
        int n = chunkPos.getMinBlockX();
        int o = chunkPos.getMinBlockZ();
        Beardifier beardifier = new Beardifier(structureFeatureManager, chunkAccess);
        Aquifer aquifer = this.aquifersEnabled ? new Aquifer(l, m, this.barrierNoise, this.waterLevelNoise, this.settings.get(), this.sampler, j * this.cellHeight) : null;
        double[][][] ds = new double[2][this.cellCountZ + 1][j + 1];
        for (int p = 0; p < this.cellCountZ + 1; ++p) {
            ds[0][p] = new double[j + 1];
            double[] es = ds[0][p];
            q = l * this.cellCountX;
            r = m * this.cellCountZ + p;
            this.sampler.fillNoiseColumn(es, q, r, noiseSettings, this.getSeaLevel(), i, j);
            ds[1][p] = new double[j + 1];
        }
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int s = 0; s < this.cellCountX; ++s) {
            int t;
            q = l * this.cellCountX + s + 1;
            for (r = 0; r < this.cellCountZ + 1; ++r) {
                double[] fs = ds[1][r];
                t = m * this.cellCountZ + r;
                this.sampler.fillNoiseColumn(fs, q, t, noiseSettings, this.getSeaLevel(), i, j);
            }
            for (r = 0; r < this.cellCountZ; ++r) {
                LevelChunkSection levelChunkSection = chunkAccess.getOrCreateSection(chunkAccess.getSectionsCount() - 1);
                for (t = j - 1; t >= 0; --t) {
                    double d = ds[0][r][t];
                    double e = ds[0][r + 1][t];
                    double f = ds[1][r][t];
                    double g = ds[1][r + 1][t];
                    double h = ds[0][r][t + 1];
                    double u = ds[0][r + 1][t + 1];
                    double v = ds[1][r][t + 1];
                    double w = ds[1][r + 1][t + 1];
                    for (int x = this.cellHeight - 1; x >= 0; --x) {
                        int y = t * this.cellHeight + x + k;
                        int z = y & 0xF;
                        int aa = chunkAccess.getSectionIndex(y);
                        if (chunkAccess.getSectionIndex(levelChunkSection.bottomBlockY()) != aa) {
                            levelChunkSection = chunkAccess.getOrCreateSection(aa);
                        }
                        double ab = (double)x / (double)this.cellHeight;
                        double ac = Mth.lerp(ab, d, h);
                        double ad = Mth.lerp(ab, f, v);
                        double ae = Mth.lerp(ab, e, u);
                        double af = Mth.lerp(ab, g, w);
                        for (int ag = 0; ag < this.cellWidth; ++ag) {
                            int ah = n + s * this.cellWidth + ag;
                            int ai = ah & 0xF;
                            double aj = (double)ag / (double)this.cellWidth;
                            double ak = Mth.lerp(aj, ac, ad);
                            double al = Mth.lerp(aj, ae, af);
                            for (int am = 0; am < this.cellWidth; ++am) {
                                int an = o + r * this.cellWidth + am;
                                int ao = an & 0xF;
                                double ap = (double)am / (double)this.cellWidth;
                                double aq = Mth.lerp(ap, ak, al);
                                BlockState blockState = this.updateNoiseAndGenerateBaseState(beardifier, aquifer, this.baseStoneSource, ah, y, an, aq);
                                if (blockState == AIR) continue;
                                if (blockState.getLightEmission() != 0 && chunkAccess instanceof ProtoChunk) {
                                    mutableBlockPos.set(ah, y, an);
                                    ((ProtoChunk)chunkAccess).addLight(mutableBlockPos);
                                }
                                levelChunkSection.setBlockState(ai, z, ao, blockState, false);
                                heightmap.update(ai, y, ao, blockState);
                                heightmap2.update(ai, y, ao, blockState);
                                if (aquifer == null || !aquifer.shouldScheduleWaterUpdate() || blockState.getFluidState().isEmpty()) continue;
                                mutableBlockPos.set(ah, y, an);
                                chunkAccess.getLiquidTicks().scheduleTick(mutableBlockPos, blockState.getFluidState().getType(), 0);
                            }
                        }
                    }
                }
            }
            this.swapFirstTwoElements((T[])ds);
        }
        return chunkAccess;
    }

    public <T> void swapFirstTwoElements(T[] objects) {
        T object = objects[0];
        objects[0] = objects[1];
        objects[1] = object;
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
    public List<MobSpawnSettings.SpawnerData> getMobsAt(Biome biome, StructureFeatureManager structureFeatureManager, MobCategory mobCategory, BlockPos blockPos) {
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
}

