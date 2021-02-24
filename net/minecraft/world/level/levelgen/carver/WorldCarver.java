/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.BitSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CanyonWorldCarver;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.NetherWorldCarver;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

public abstract class WorldCarver<C extends CarverConfiguration> {
    public static final WorldCarver<CarverConfiguration> CAVE = WorldCarver.register("cave", new CaveWorldCarver(CarverConfiguration.CODEC));
    public static final WorldCarver<CarverConfiguration> NETHER_CAVE = WorldCarver.register("nether_cave", new NetherWorldCarver(CarverConfiguration.CODEC));
    public static final WorldCarver<CanyonCarverConfiguration> CANYON = WorldCarver.register("canyon", new CanyonWorldCarver(CanyonCarverConfiguration.CODEC));
    protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
    protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
    protected static final FluidState WATER = Fluids.WATER.defaultFluidState();
    protected static final FluidState LAVA = Fluids.LAVA.defaultFluidState();
    protected Set<Block> replaceableBlocks = ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.DIRT, Blocks.COARSE_DIRT, new Block[]{Blocks.PODZOL, Blocks.GRASS_BLOCK, Blocks.TERRACOTTA, Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA, Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.BLACK_TERRACOTTA, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.MYCELIUM, Blocks.SNOW, Blocks.PACKED_ICE, Blocks.DEEPSLATE});
    protected Set<Fluid> liquids = ImmutableSet.of(Fluids.WATER);
    private final Codec<ConfiguredWorldCarver<C>> configuredCodec;

    private static <C extends CarverConfiguration, F extends WorldCarver<C>> F register(String string, F worldCarver) {
        return (F)Registry.register(Registry.CARVER, string, worldCarver);
    }

    public WorldCarver(Codec<C> codec) {
        this.configuredCodec = ((MapCodec)codec.fieldOf("config")).xmap(this::configured, ConfiguredWorldCarver::config).codec();
    }

    public ConfiguredWorldCarver<C> configured(C carverConfiguration) {
        return new ConfiguredWorldCarver<C>(this, carverConfiguration);
    }

    public Codec<ConfiguredWorldCarver<C>> configuredCodec() {
        return this.configuredCodec;
    }

    public int getRange() {
        return 4;
    }

    protected boolean carveEllipsoid(CarvingContext carvingContext, C carverConfiguration, ChunkAccess chunkAccess, Function<BlockPos, Biome> function, long l, int i, double d, double e, double f, double g, double h, BitSet bitSet, CarveSkipChecker carveSkipChecker) {
        int w;
        int v;
        int u;
        int t;
        int s;
        ChunkPos chunkPos = chunkAccess.getPos();
        int j = chunkPos.x;
        int k = chunkPos.z;
        Random random = new Random(l + (long)j + (long)k);
        double m = chunkPos.getMiddleBlockX();
        double n = chunkPos.getMiddleBlockZ();
        double o = 16.0 + g * 2.0;
        if (Math.abs(d - m) > o || Math.abs(f - n) > o) {
            return false;
        }
        int p = chunkPos.getMinBlockX();
        int q = chunkPos.getMinBlockZ();
        int r = Math.max(Mth.floor(d - g) - p - 1, 0);
        if (this.hasDisallowedLiquid(chunkAccess, r, s = Math.min(Mth.floor(d + g) - p, 15), t = Math.max(Mth.floor(e - h) - 1, carvingContext.getMinGenY() + 1), u = Math.min(Mth.floor(e + h) + 1, carvingContext.getMinGenY() + carvingContext.getGenDepth() - 8), v = Math.max(Mth.floor(f - g) - q - 1, 0), w = Math.min(Mth.floor(f + g) - q, 15))) {
            return false;
        }
        boolean bl = false;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        for (int x = r; x <= s; ++x) {
            int y = chunkPos.getBlockX(x);
            double z = ((double)y + 0.5 - d) / g;
            for (int aa = v; aa <= w; ++aa) {
                int ab = chunkPos.getBlockZ(aa);
                double ac = ((double)ab + 0.5 - f) / g;
                if (z * z + ac * ac >= 1.0) continue;
                MutableBoolean mutableBoolean = new MutableBoolean(false);
                for (int ad = u; ad > t; --ad) {
                    int af;
                    int ag;
                    double ae = ((double)ad - 0.5 - e) / h;
                    if (carveSkipChecker.shouldSkip(carvingContext, z, ae, ac, ad) || bitSet.get(ag = x | aa << 4 | (af = ad - carvingContext.getMinGenY()) << 8) && !WorldCarver.isDebugEnabled(carverConfiguration)) continue;
                    bitSet.set(ag);
                    mutableBlockPos.set(y, ad, ab);
                    bl |= this.carveBlock(carvingContext, carverConfiguration, chunkAccess, function, bitSet, random, mutableBlockPos, mutableBlockPos2, i, mutableBoolean);
                }
            }
        }
        return bl;
    }

    protected boolean carveBlock(CarvingContext carvingContext, C carverConfiguration, ChunkAccess chunkAccess, Function<BlockPos, Biome> function, BitSet bitSet, Random random, BlockPos.MutableBlockPos mutableBlockPos, BlockPos.MutableBlockPos mutableBlockPos2, int i, MutableBoolean mutableBoolean) {
        BlockState blockState = chunkAccess.getBlockState(mutableBlockPos);
        BlockState blockState2 = chunkAccess.getBlockState(mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.UP));
        if (blockState.is(Blocks.GRASS_BLOCK) || blockState.is(Blocks.MYCELIUM)) {
            mutableBoolean.setTrue();
        }
        if (!this.canReplaceBlock(blockState, blockState2) && !WorldCarver.isDebugEnabled(carverConfiguration)) {
            return false;
        }
        if (mutableBlockPos.getY() < carvingContext.getMinGenY() + 11 && !WorldCarver.isDebugEnabled(carverConfiguration)) {
            chunkAccess.setBlockState(mutableBlockPos, LAVA.createLegacyBlock(), false);
        } else {
            chunkAccess.setBlockState(mutableBlockPos, WorldCarver.getCaveAirState(carverConfiguration), false);
            if (mutableBoolean.isTrue()) {
                mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.DOWN);
                if (chunkAccess.getBlockState(mutableBlockPos2).is(Blocks.DIRT)) {
                    chunkAccess.setBlockState(mutableBlockPos2, function.apply(mutableBlockPos).getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial(), false);
                }
            }
        }
        return true;
    }

    private static BlockState getCaveAirState(CarverConfiguration carverConfiguration) {
        return WorldCarver.isDebugEnabled(carverConfiguration) ? carverConfiguration.getDebugSettings().getAirState() : CAVE_AIR;
    }

    public abstract boolean carve(CarvingContext var1, C var2, ChunkAccess var3, Function<BlockPos, Biome> var4, Random var5, int var6, ChunkPos var7, BitSet var8);

    public abstract boolean isStartChunk(C var1, Random var2);

    protected boolean canReplaceBlock(BlockState blockState) {
        return this.replaceableBlocks.contains(blockState.getBlock());
    }

    protected boolean canReplaceBlock(BlockState blockState, BlockState blockState2) {
        return this.canReplaceBlock(blockState) || (blockState.is(Blocks.SAND) || blockState.is(Blocks.GRAVEL)) && !blockState2.getFluidState().is(FluidTags.WATER);
    }

    protected boolean hasDisallowedLiquid(ChunkAccess chunkAccess, int i, int j, int k, int l, int m, int n) {
        ChunkPos chunkPos = chunkAccess.getPos();
        int o = chunkPos.getMinBlockX();
        int p = chunkPos.getMinBlockZ();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int q = i; q <= j; ++q) {
            for (int r = m; r <= n; ++r) {
                for (int s = k - 1; s <= l + 1; ++s) {
                    mutableBlockPos.set(o + q, s, p + r);
                    if (this.liquids.contains(chunkAccess.getFluidState(mutableBlockPos).getType())) {
                        return true;
                    }
                    if (s == l + 1 || WorldCarver.isEdge(q, r, i, j, m, n)) continue;
                    s = l;
                }
            }
        }
        return false;
    }

    private static boolean isEdge(int i, int j, int k, int l, int m, int n) {
        return i == k || i == l || j == m || j == n;
    }

    protected static boolean canReach(ChunkPos chunkPos, double d, double e, int i, int j, float f) {
        double n;
        double m;
        double h;
        double l;
        double g = chunkPos.getMiddleBlockX();
        double k = d - g;
        return k * k + (l = e - (h = (double)chunkPos.getMiddleBlockZ())) * l - (m = (double)(j - i)) * m <= (n = (double)(f + 2.0f + 16.0f)) * n;
    }

    private static boolean isDebugEnabled(CarverConfiguration carverConfiguration) {
        return carverConfiguration.getDebugSettings().isDebugMode();
    }

    public static interface CarveSkipChecker {
        public boolean shouldSkip(CarvingContext var1, double var2, double var4, double var6, int var8);
    }
}

