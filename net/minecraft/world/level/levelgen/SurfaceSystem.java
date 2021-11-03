/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseSampler;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.Material;

public class SurfaceSystem {
    private static final int HOW_FAR_BELOW_PRELIMINARY_SURFACE_LEVEL_TO_BUILD_SURFACE = 8;
    private static final int MAX_CLAY_DEPTH = 15;
    private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    private static final BlockState ORANGE_TERRACOTTA = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
    private static final BlockState TERRACOTTA = Blocks.TERRACOTTA.defaultBlockState();
    private static final BlockState YELLOW_TERRACOTTA = Blocks.YELLOW_TERRACOTTA.defaultBlockState();
    private static final BlockState BROWN_TERRACOTTA = Blocks.BROWN_TERRACOTTA.defaultBlockState();
    private static final BlockState RED_TERRACOTTA = Blocks.RED_TERRACOTTA.defaultBlockState();
    private static final BlockState LIGHT_GRAY_TERRACOTTA = Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
    private static final BlockState PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
    private static final BlockState SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();
    private final NoiseSampler sampler;
    private final BlockState defaultBlock;
    private final int seaLevel;
    private final BlockState[] clayBands;
    private final NormalNoise clayBandsOffsetNoise;
    private final NormalNoise badlandsPillarNoise;
    private final NormalNoise badlandsPillarRoofNoise;
    private final NormalNoise badlandsSurfaceNoise;
    private final NormalNoise icebergPillarNoise;
    private final NormalNoise icebergPillarRoofNoise;
    private final NormalNoise icebergSurfaceNoise;
    private final Registry<NormalNoise.NoiseParameters> noises;
    private final Map<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise> noiseIntances = new ConcurrentHashMap<ResourceKey<NormalNoise.NoiseParameters>, NormalNoise>();
    private final Map<ResourceLocation, PositionalRandomFactory> positionalRandoms = new ConcurrentHashMap<ResourceLocation, PositionalRandomFactory>();
    private final PositionalRandomFactory randomFactory;
    private final NormalNoise surfaceNoise;

    public SurfaceSystem(NoiseSampler noiseSampler, Registry<NormalNoise.NoiseParameters> registry, BlockState blockState, int i, long l, WorldgenRandom.Algorithm algorithm) {
        this.sampler = noiseSampler;
        this.noises = registry;
        this.defaultBlock = blockState;
        this.seaLevel = i;
        this.randomFactory = algorithm.newInstance(l).forkPositional();
        this.clayBandsOffsetNoise = Noises.instantiate(registry, this.randomFactory, Noises.CLAY_BANDS_OFFSET);
        this.clayBands = SurfaceSystem.generateBands(this.randomFactory.fromHashOf(new ResourceLocation("clay_bands")));
        this.surfaceNoise = Noises.instantiate(registry, this.randomFactory, Noises.SURFACE);
        this.badlandsPillarNoise = Noises.instantiate(registry, this.randomFactory, Noises.BADLANDS_PILLAR);
        this.badlandsPillarRoofNoise = Noises.instantiate(registry, this.randomFactory, Noises.BADLANDS_PILLAR_ROOF);
        this.badlandsSurfaceNoise = Noises.instantiate(registry, this.randomFactory, Noises.BADLANDS_SURFACE);
        this.icebergPillarNoise = Noises.instantiate(registry, this.randomFactory, Noises.ICEBERG_PILLAR);
        this.icebergPillarRoofNoise = Noises.instantiate(registry, this.randomFactory, Noises.ICEBERG_PILLAR_ROOF);
        this.icebergSurfaceNoise = Noises.instantiate(registry, this.randomFactory, Noises.ICEBERG_SURFACE);
    }

    protected NormalNoise getOrCreateNoise(ResourceKey<NormalNoise.NoiseParameters> resourceKey) {
        return this.noiseIntances.computeIfAbsent(resourceKey, resourceKey2 -> Noises.instantiate(this.noises, this.randomFactory, resourceKey));
    }

    protected PositionalRandomFactory getOrCreateRandomFactory(ResourceLocation resourceLocation) {
        return this.positionalRandoms.computeIfAbsent(resourceLocation, resourceLocation2 -> this.randomFactory.fromHashOf(resourceLocation).forkPositional());
    }

    public void buildSurface(BiomeManager biomeManager, Registry<Biome> registry, boolean bl, WorldGenerationContext worldGenerationContext, final ChunkAccess chunkAccess, NoiseChunk noiseChunk, SurfaceRules.RuleSource ruleSource) {
        final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        final ChunkPos chunkPos = chunkAccess.getPos();
        int i = chunkPos.getMinBlockX();
        int j = chunkPos.getMinBlockZ();
        BlockColumn blockColumn = new BlockColumn(){

            @Override
            public BlockState getBlock(int i) {
                return chunkAccess.getBlockState(mutableBlockPos.setY(i));
            }

            @Override
            public void setBlock(int i, BlockState blockState) {
                chunkAccess.setBlockState(mutableBlockPos.setY(i), blockState, false);
            }

            public String toString() {
                return "ChunkBlockColumn " + chunkPos;
            }
        };
        SurfaceRules.Context context = new SurfaceRules.Context(this, chunkAccess, biomeManager::getBiome, registry, worldGenerationContext);
        SurfaceRules.SurfaceRule surfaceRule = (SurfaceRules.SurfaceRule)ruleSource.apply(context);
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                int m = i + k;
                int n = j + l;
                int o = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, k, l) + 1;
                RandomSource randomSource = this.randomFactory.at(m, 0, n);
                double d = this.surfaceNoise.getValue(m, 0.0, n);
                mutableBlockPos.setX(m).setZ(n);
                int p = this.sampler.getPreliminarySurfaceLevel(m, n, noiseChunk.terrainInfoInterpolated(m, n));
                int q = p - 8;
                Biome biome = biomeManager.getBiome(mutableBlockPos2.set(m, bl ? 0 : o, n));
                ResourceKey<Biome> resourceKey = registry.getResourceKey(biome).orElseThrow(() -> new IllegalStateException("Unregistered biome: " + biome));
                if (resourceKey == Biomes.ERODED_BADLANDS) {
                    this.erodedBadlandsExtension(blockColumn, m, n, o, chunkAccess);
                }
                int r = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, k, l) + 1;
                int s = (int)(d * 2.75 + 3.0 + randomSource.nextDouble() * 0.25);
                int t = resourceKey == Biomes.WOODED_BADLANDS || resourceKey == Biomes.BADLANDS ? 15 : Integer.MAX_VALUE;
                context.updateXZ(m, n, s);
                int u = 0;
                int v = 0;
                int w = Integer.MIN_VALUE;
                int x = Integer.MAX_VALUE;
                int y = chunkAccess.getMinBuildHeight();
                for (int z = r; z >= y && v < t; --z) {
                    BlockState blockState2;
                    int aa;
                    BlockState blockState = blockColumn.getBlock(z);
                    if (blockState.isAir()) {
                        u = 0;
                        w = Integer.MIN_VALUE;
                        continue;
                    }
                    if (!blockState.getFluidState().isEmpty()) {
                        if (w != Integer.MIN_VALUE) continue;
                        w = z + 1;
                        continue;
                    }
                    if (x >= z) {
                        x = DimensionType.WAY_BELOW_MIN_Y;
                        for (aa = z - 1; aa >= y - 1; --aa) {
                            blockState2 = blockColumn.getBlock(aa);
                            if (this.isStone(blockState2)) continue;
                            x = aa + 1;
                            break;
                        }
                    }
                    ++v;
                    aa = z - x + 1;
                    context.updateY(q, ++u, aa, w, m, z, n);
                    blockState2 = surfaceRule.tryApply(m, z, n);
                    if (blockState2 == null) continue;
                    blockColumn.setBlock(z, blockState2);
                }
                if (resourceKey != Biomes.FROZEN_OCEAN && resourceKey != Biomes.DEEP_FROZEN_OCEAN) continue;
                this.frozenOceanExtension(q, biome, blockColumn, mutableBlockPos2, m, n, o);
            }
        }
    }

    private boolean isStone(BlockState blockState) {
        return !blockState.isAir() && blockState.getFluidState().isEmpty();
    }

    @Deprecated
    public Optional<BlockState> topMaterial(SurfaceRules.RuleSource ruleSource, CarvingContext carvingContext, Function<BlockPos, Biome> function, ChunkAccess chunkAccess, BlockPos blockPos, boolean bl) {
        SurfaceRules.Context context = new SurfaceRules.Context(this, chunkAccess, function, carvingContext.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), carvingContext);
        SurfaceRules.SurfaceRule surfaceRule = (SurfaceRules.SurfaceRule)ruleSource.apply(context);
        int i = blockPos.getX();
        int j = blockPos.getY();
        int k = blockPos.getZ();
        RandomSource randomSource = this.randomFactory.at(i, 0, k);
        double d = this.surfaceNoise.getValue(i, 0.0, k);
        int l = (int)(d * 2.75 + 3.0 + randomSource.nextDouble() * 0.25);
        context.updateXZ(i, k, l);
        int m = j - 16;
        context.updateY(m, 1, 1, bl ? j + 1 : Integer.MIN_VALUE, i, j, k);
        BlockState blockState = surfaceRule.tryApply(i, j, k);
        return Optional.ofNullable(blockState);
    }

    private void erodedBadlandsExtension(BlockColumn blockColumn, int i, int j, int k, LevelHeightAccessor levelHeightAccessor) {
        BlockState blockState;
        int n;
        double d = 0.2;
        double e = Math.min(Math.abs(this.badlandsSurfaceNoise.getValue(i, 0.0, j) * 8.25), this.badlandsPillarNoise.getValue((double)i * 0.2, 0.0, (double)j * 0.2) * 15.0);
        if (e <= 0.0) {
            return;
        }
        double f = 0.75;
        double g = 1.5;
        double h = Math.abs(this.badlandsPillarRoofNoise.getValue((double)i * 0.75, 0.0, (double)j * 0.75) * 1.5);
        double l = 64.0 + Math.min(e * e * 2.5, Math.ceil(h * 50.0) + 24.0);
        int m = Mth.floor(l);
        if (k > m) {
            return;
        }
        for (n = m; n >= levelHeightAccessor.getMinBuildHeight() && !(blockState = blockColumn.getBlock(n)).is(this.defaultBlock.getBlock()); --n) {
            if (!blockState.is(Blocks.WATER)) continue;
            return;
        }
        for (n = m; n >= levelHeightAccessor.getMinBuildHeight() && blockColumn.getBlock(n).isAir(); --n) {
            blockColumn.setBlock(n, this.defaultBlock);
        }
    }

    private void frozenOceanExtension(int i, Biome biome, BlockColumn blockColumn, BlockPos.MutableBlockPos mutableBlockPos, int j, int k, int l) {
        double o;
        float f = biome.getTemperature(mutableBlockPos.set(j, 63, k));
        double d = 1.28;
        double e = Math.min(Math.abs(this.icebergSurfaceNoise.getValue(j, 0.0, k) * 8.25), this.icebergPillarNoise.getValue((double)j * 1.28, 0.0, (double)k * 1.28) * 15.0);
        if (e <= 1.8) {
            return;
        }
        double g = 1.17;
        double h = 1.5;
        double m = Math.abs(this.icebergPillarRoofNoise.getValue((double)j * 1.17, 0.0, (double)k * 1.17) * 1.5);
        double n = Math.min(e * e * 1.2, Math.ceil(m * 40.0) + 14.0);
        if (f > 0.1f) {
            n -= 2.0;
        }
        if (n > 2.0) {
            o = (double)this.seaLevel - n - 7.0;
            n += (double)this.seaLevel;
        } else {
            n = 0.0;
            o = 0.0;
        }
        double p = n;
        RandomSource randomSource = this.randomFactory.at(j, 0, k);
        int q = 2 + randomSource.nextInt(4);
        int r = this.seaLevel + 18 + randomSource.nextInt(10);
        int s = 0;
        for (int t = Math.max(l, (int)p + 1); t >= i; --t) {
            if (!(blockColumn.getBlock(t).isAir() && t < (int)p && randomSource.nextDouble() > 0.01) && (blockColumn.getBlock(t).getMaterial() != Material.WATER || t <= (int)o || t >= this.seaLevel || o == 0.0 || !(randomSource.nextDouble() > 0.15))) continue;
            if (s <= q && t > r) {
                blockColumn.setBlock(t, SNOW_BLOCK);
                ++s;
                continue;
            }
            blockColumn.setBlock(t, PACKED_ICE);
        }
    }

    private static BlockState[] generateBands(RandomSource randomSource) {
        int i;
        Object[] blockStates = new BlockState[192];
        Arrays.fill(blockStates, TERRACOTTA);
        for (i = 0; i < blockStates.length; ++i) {
            if ((i += randomSource.nextInt(5) + 1) >= blockStates.length) continue;
            blockStates[i] = ORANGE_TERRACOTTA;
        }
        SurfaceSystem.makeBands(randomSource, (BlockState[])blockStates, 1, YELLOW_TERRACOTTA);
        SurfaceSystem.makeBands(randomSource, (BlockState[])blockStates, 2, BROWN_TERRACOTTA);
        SurfaceSystem.makeBands(randomSource, (BlockState[])blockStates, 1, RED_TERRACOTTA);
        i = randomSource.nextIntBetweenInclusive(9, 15);
        int j = 0;
        for (int k = 0; j < i && k < blockStates.length; ++j, k += randomSource.nextInt(16) + 4) {
            blockStates[k] = WHITE_TERRACOTTA;
            if (k - 1 > 0 && randomSource.nextBoolean()) {
                blockStates[k - 1] = LIGHT_GRAY_TERRACOTTA;
            }
            if (k + 1 >= blockStates.length || !randomSource.nextBoolean()) continue;
            blockStates[k + 1] = LIGHT_GRAY_TERRACOTTA;
        }
        return blockStates;
    }

    private static void makeBands(RandomSource randomSource, BlockState[] blockStates, int i, BlockState blockState) {
        int j = randomSource.nextIntBetweenInclusive(6, 15);
        for (int k = 0; k < j; ++k) {
            int l = i + randomSource.nextInt(3);
            int m = randomSource.nextInt(blockStates.length);
            for (int n = 0; m + n < blockStates.length && n < l; ++n) {
                blockStates[m + n] = blockState;
            }
        }
    }

    protected BlockState getBand(int i, int j, int k) {
        int l = (int)Math.round(this.clayBandsOffsetNoise.getValue(i, 0.0, k) * 4.0);
        return this.clayBands[(j + l + this.clayBands.length) % this.clayBands.length];
    }
}

