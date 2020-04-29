/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.Dynamic;
import java.util.BitSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.carver.CanyonWorldCarver;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.carver.NetherWorldCarver;
import net.minecraft.world.level.levelgen.carver.UnderwaterCanyonWorldCarver;
import net.minecraft.world.level.levelgen.carver.UnderwaterCaveWorldCarver;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public abstract class WorldCarver<C extends CarverConfiguration> {
    public static final WorldCarver<ProbabilityFeatureConfiguration> CAVE = WorldCarver.register("cave", new CaveWorldCarver((Function<Dynamic<?>, ? extends ProbabilityFeatureConfiguration>)((Function<Dynamic<?>, ProbabilityFeatureConfiguration>)ProbabilityFeatureConfiguration::deserialize), 256));
    public static final WorldCarver<ProbabilityFeatureConfiguration> NETHER_CAVE = WorldCarver.register("nether_cave", new NetherWorldCarver(ProbabilityFeatureConfiguration::deserialize));
    public static final WorldCarver<ProbabilityFeatureConfiguration> CANYON = WorldCarver.register("canyon", new CanyonWorldCarver(ProbabilityFeatureConfiguration::deserialize));
    public static final WorldCarver<ProbabilityFeatureConfiguration> UNDERWATER_CANYON = WorldCarver.register("underwater_canyon", new UnderwaterCanyonWorldCarver(ProbabilityFeatureConfiguration::deserialize));
    public static final WorldCarver<ProbabilityFeatureConfiguration> UNDERWATER_CAVE = WorldCarver.register("underwater_cave", new UnderwaterCaveWorldCarver(ProbabilityFeatureConfiguration::deserialize));
    protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
    protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
    protected static final FluidState WATER = Fluids.WATER.defaultFluidState();
    protected static final FluidState LAVA = Fluids.LAVA.defaultFluidState();
    protected Set<Block> replaceableBlocks = ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.DIRT, Blocks.COARSE_DIRT, new Block[]{Blocks.PODZOL, Blocks.GRASS_BLOCK, Blocks.TERRACOTTA, Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA, Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.BLACK_TERRACOTTA, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.MYCELIUM, Blocks.SNOW, Blocks.PACKED_ICE});
    protected Set<Fluid> liquids = ImmutableSet.of(Fluids.WATER);
    private final Function<Dynamic<?>, ? extends C> configurationFactory;
    protected final int genHeight;

    private static <C extends CarverConfiguration, F extends WorldCarver<C>> F register(String string, F worldCarver) {
        return (F)Registry.register(Registry.CARVER, string, worldCarver);
    }

    public WorldCarver(Function<Dynamic<?>, ? extends C> function, int i) {
        this.configurationFactory = function;
        this.genHeight = i;
    }

    public int getRange() {
        return 4;
    }

    protected boolean carveSphere(ChunkAccess chunkAccess, Function<BlockPos, Biome> function, long l, int i, int j, int k, double d, double e, double f, double g, double h, BitSet bitSet) {
        int t;
        int s;
        int r;
        int q;
        int p;
        Random random = new Random(l + (long)j + (long)k);
        double m = j * 16 + 8;
        double n = k * 16 + 8;
        if (d < m - 16.0 - g * 2.0 || f < n - 16.0 - g * 2.0 || d > m + 16.0 + g * 2.0 || f > n + 16.0 + g * 2.0) {
            return false;
        }
        int o = Math.max(Mth.floor(d - g) - j * 16 - 1, 0);
        if (this.hasWater(chunkAccess, j, k, o, p = Math.min(Mth.floor(d + g) - j * 16 + 1, 16), q = Math.max(Mth.floor(e - h) - 1, 1), r = Math.min(Mth.floor(e + h) + 1, this.genHeight - 8), s = Math.max(Mth.floor(f - g) - k * 16 - 1, 0), t = Math.min(Mth.floor(f + g) - k * 16 + 1, 16))) {
            return false;
        }
        boolean bl = false;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos3 = new BlockPos.MutableBlockPos();
        for (int u = o; u < p; ++u) {
            int v = u + j * 16;
            double w = ((double)v + 0.5 - d) / g;
            for (int x = s; x < t; ++x) {
                int y = x + k * 16;
                double z = ((double)y + 0.5 - f) / g;
                if (w * w + z * z >= 1.0) continue;
                AtomicBoolean atomicBoolean = new AtomicBoolean(false);
                for (int aa = r; aa > q; --aa) {
                    double ab = ((double)aa - 0.5 - e) / h;
                    if (this.skip(w, ab, z, aa)) continue;
                    bl |= this.carveBlock(chunkAccess, function, bitSet, random, mutableBlockPos, mutableBlockPos2, mutableBlockPos3, i, j, k, v, y, u, aa, x, atomicBoolean);
                }
            }
        }
        return bl;
    }

    protected boolean carveBlock(ChunkAccess chunkAccess, Function<BlockPos, Biome> function, BitSet bitSet, Random random, BlockPos.MutableBlockPos mutableBlockPos, BlockPos.MutableBlockPos mutableBlockPos2, BlockPos.MutableBlockPos mutableBlockPos3, int i, int j, int k, int l, int m, int n, int o, int p, AtomicBoolean atomicBoolean) {
        int q = n | p << 4 | o << 8;
        if (bitSet.get(q)) {
            return false;
        }
        bitSet.set(q);
        mutableBlockPos.set(l, o, m);
        BlockState blockState = chunkAccess.getBlockState(mutableBlockPos);
        BlockState blockState2 = chunkAccess.getBlockState(mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.UP));
        if (blockState.is(Blocks.GRASS_BLOCK) || blockState.is(Blocks.MYCELIUM)) {
            atomicBoolean.set(true);
        }
        if (!this.canReplaceBlock(blockState, blockState2)) {
            return false;
        }
        if (o < 11) {
            chunkAccess.setBlockState(mutableBlockPos, LAVA.createLegacyBlock(), false);
        } else {
            chunkAccess.setBlockState(mutableBlockPos, CAVE_AIR, false);
            if (atomicBoolean.get()) {
                mutableBlockPos3.setWithOffset(mutableBlockPos, Direction.DOWN);
                if (chunkAccess.getBlockState(mutableBlockPos3).is(Blocks.DIRT)) {
                    chunkAccess.setBlockState(mutableBlockPos3, function.apply(mutableBlockPos).getSurfaceBuilderConfig().getTopMaterial(), false);
                }
            }
        }
        return true;
    }

    public abstract boolean carve(ChunkAccess var1, Function<BlockPos, Biome> var2, Random var3, int var4, int var5, int var6, int var7, int var8, BitSet var9, C var10);

    public abstract boolean isStartChunk(Random var1, int var2, int var3, C var4);

    protected boolean canReplaceBlock(BlockState blockState) {
        return this.replaceableBlocks.contains(blockState.getBlock());
    }

    protected boolean canReplaceBlock(BlockState blockState, BlockState blockState2) {
        return this.canReplaceBlock(blockState) || (blockState.is(Blocks.SAND) || blockState.is(Blocks.GRAVEL)) && !blockState2.getFluidState().is(FluidTags.WATER);
    }

    protected boolean hasWater(ChunkAccess chunkAccess, int i, int j, int k, int l, int m, int n, int o, int p) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int q = k; q < l; ++q) {
            for (int r = o; r < p; ++r) {
                for (int s = m - 1; s <= n + 1; ++s) {
                    if (this.liquids.contains(chunkAccess.getFluidState(mutableBlockPos.set(q + i * 16, s, r + j * 16)).getType())) {
                        return true;
                    }
                    if (s == n + 1 || this.isEdge(k, l, o, p, q, r)) continue;
                    s = n;
                }
            }
        }
        return false;
    }

    private boolean isEdge(int i, int j, int k, int l, int m, int n) {
        return m == i || m == j - 1 || n == k || n == l - 1;
    }

    protected boolean canReach(int i, int j, double d, double e, int k, int l, float f) {
        double g = i * 16 + 8;
        double m = d - g;
        double h = j * 16 + 8;
        double n = e - h;
        double o = l - k;
        double p = f + 2.0f + 16.0f;
        return m * m + n * n - o * o <= p * p;
    }

    protected abstract boolean skip(double var1, double var3, double var5, int var7);
}

