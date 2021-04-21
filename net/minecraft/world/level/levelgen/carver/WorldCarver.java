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
import net.minecraft.core.Vec3i;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.BaseStoneSource;
import net.minecraft.world.level.levelgen.SingleBaseStoneSource;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CanyonWorldCarver;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.NetherWorldCarver;
import net.minecraft.world.level.levelgen.carver.UnderwaterCanyonWorldCarver;
import net.minecraft.world.level.levelgen.carver.UnderwaterCaveWorldCarver;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

public abstract class WorldCarver<C extends CarverConfiguration> {
    public static final WorldCarver<CaveCarverConfiguration> CAVE = WorldCarver.register("cave", new CaveWorldCarver(CaveCarverConfiguration.CODEC));
    public static final WorldCarver<CaveCarverConfiguration> NETHER_CAVE = WorldCarver.register("nether_cave", new NetherWorldCarver(CaveCarverConfiguration.CODEC));
    public static final WorldCarver<CanyonCarverConfiguration> CANYON = WorldCarver.register("canyon", new CanyonWorldCarver(CanyonCarverConfiguration.CODEC));
    public static final WorldCarver<CanyonCarverConfiguration> UNDERWATER_CANYON = WorldCarver.register("underwater_canyon", new UnderwaterCanyonWorldCarver(CanyonCarverConfiguration.CODEC));
    public static final WorldCarver<CaveCarverConfiguration> UNDERWATER_CAVE = WorldCarver.register("underwater_cave", new UnderwaterCaveWorldCarver(CaveCarverConfiguration.CODEC));
    protected static final BaseStoneSource STONE_SOURCE = new SingleBaseStoneSource(Blocks.STONE.defaultBlockState());
    protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
    protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
    protected static final FluidState WATER = Fluids.WATER.defaultFluidState();
    protected static final FluidState LAVA = Fluids.LAVA.defaultFluidState();
    protected Set<Block> replaceableBlocks = ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.DIRT, Blocks.COARSE_DIRT, new Block[]{Blocks.PODZOL, Blocks.GRASS_BLOCK, Blocks.TERRACOTTA, Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA, Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.BLACK_TERRACOTTA, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.MYCELIUM, Blocks.SNOW, Blocks.PACKED_ICE, Blocks.DEEPSLATE, Blocks.TUFF, Blocks.GRANITE, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.COPPER_ORE});
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

    protected boolean carveEllipsoid(CarvingContext carvingContext, C carverConfiguration, ChunkAccess chunkAccess, Function<BlockPos, Biome> function, long l, Aquifer aquifer, double d, double e, double f, double g, double h, BitSet bitSet, CarveSkipChecker carveSkipChecker) {
        ChunkPos chunkPos = chunkAccess.getPos();
        int i = chunkPos.x;
        int j = chunkPos.z;
        Random random = new Random(l + (long)i + (long)j);
        double k = chunkPos.getMiddleBlockX();
        double m = chunkPos.getMiddleBlockZ();
        double n = 16.0 + g * 2.0;
        if (Math.abs(d - k) > n || Math.abs(f - m) > n) {
            return false;
        }
        int o = chunkPos.getMinBlockX();
        int p = chunkPos.getMinBlockZ();
        int q = Math.max(Mth.floor(d - g) - o - 1, 0);
        int r = Math.min(Mth.floor(d + g) - o, 15);
        int s = Math.max(Mth.floor(e - h) - 1, carvingContext.getMinGenY() + 1);
        int t = Math.min(Mth.floor(e + h) + 1, carvingContext.getMinGenY() + carvingContext.getGenDepth() - 8);
        int u = Math.max(Mth.floor(f - g) - p - 1, 0);
        int v = Math.min(Mth.floor(f + g) - p, 15);
        if (!((CarverConfiguration)carverConfiguration).aquifersEnabled && this.hasDisallowedLiquid(chunkAccess, q, r, s, t, u, v)) {
            return false;
        }
        boolean bl = false;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        for (int w = q; w <= r; ++w) {
            int x = chunkPos.getBlockX(w);
            double y = ((double)x + 0.5 - d) / g;
            for (int z = u; z <= v; ++z) {
                int aa = chunkPos.getBlockZ(z);
                double ab = ((double)aa + 0.5 - f) / g;
                if (y * y + ab * ab >= 1.0) continue;
                MutableBoolean mutableBoolean = new MutableBoolean(false);
                for (int ac = t; ac > s; --ac) {
                    int ae;
                    int af;
                    double ad = ((double)ac - 0.5 - e) / h;
                    if (carveSkipChecker.shouldSkip(carvingContext, y, ad, ab, ac) || bitSet.get(af = w | z << 4 | (ae = ac - carvingContext.getMinGenY()) << 8) && !WorldCarver.isDebugEnabled(carverConfiguration)) continue;
                    bitSet.set(af);
                    mutableBlockPos.set(x, ac, aa);
                    bl |= this.carveBlock(carvingContext, carverConfiguration, chunkAccess, function, bitSet, random, mutableBlockPos, mutableBlockPos2, aquifer, mutableBoolean);
                }
            }
        }
        return bl;
    }

    protected boolean carveBlock(CarvingContext carvingContext, C carverConfiguration, ChunkAccess chunkAccess, Function<BlockPos, Biome> function, BitSet bitSet, Random random, BlockPos.MutableBlockPos mutableBlockPos, BlockPos.MutableBlockPos mutableBlockPos2, Aquifer aquifer, MutableBoolean mutableBoolean) {
        BlockState blockState = chunkAccess.getBlockState(mutableBlockPos);
        BlockState blockState2 = chunkAccess.getBlockState(mutableBlockPos2.setWithOffset((Vec3i)mutableBlockPos, Direction.UP));
        if (blockState.is(Blocks.GRASS_BLOCK) || blockState.is(Blocks.MYCELIUM)) {
            mutableBoolean.setTrue();
        }
        if (!this.canReplaceBlock(blockState, blockState2) && !WorldCarver.isDebugEnabled(carverConfiguration)) {
            return false;
        }
        BlockState blockState3 = this.getCarveState(carvingContext, carverConfiguration, mutableBlockPos, aquifer);
        if (blockState3 == null) {
            return false;
        }
        chunkAccess.setBlockState(mutableBlockPos, blockState3, false);
        if (mutableBoolean.isTrue()) {
            mutableBlockPos2.setWithOffset((Vec3i)mutableBlockPos, Direction.DOWN);
            if (chunkAccess.getBlockState(mutableBlockPos2).is(Blocks.DIRT)) {
                chunkAccess.setBlockState(mutableBlockPos2, function.apply(mutableBlockPos).getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial(), false);
            }
        }
        return true;
    }

    @Nullable
    private BlockState getCarveState(CarvingContext carvingContext, C carverConfiguration, BlockPos blockPos, Aquifer aquifer) {
        if (blockPos.getY() <= ((CarverConfiguration)carverConfiguration).lavaLevel.resolveY(carvingContext)) {
            return LAVA.createLegacyBlock();
        }
        if (!((CarverConfiguration)carverConfiguration).aquifersEnabled) {
            return WorldCarver.isDebugEnabled(carverConfiguration) ? WorldCarver.getDebugState(carverConfiguration, AIR) : AIR;
        }
        BlockState blockState = aquifer.computeState(STONE_SOURCE, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0.0);
        if (blockState == Blocks.STONE.defaultBlockState()) {
            return WorldCarver.isDebugEnabled(carverConfiguration) ? ((CarverConfiguration)carverConfiguration).debugSettings.getBarrierState() : null;
        }
        return WorldCarver.isDebugEnabled(carverConfiguration) ? WorldCarver.getDebugState(carverConfiguration, blockState) : blockState;
    }

    private static BlockState getDebugState(CarverConfiguration carverConfiguration, BlockState blockState) {
        if (blockState.is(Blocks.AIR)) {
            return carverConfiguration.debugSettings.getAirState();
        }
        if (blockState.is(Blocks.WATER)) {
            BlockState blockState2 = carverConfiguration.debugSettings.getWaterState();
            if (blockState2.hasProperty(BlockStateProperties.WATERLOGGED)) {
                return (BlockState)blockState2.setValue(BlockStateProperties.WATERLOGGED, true);
            }
            return blockState2;
        }
        if (blockState.is(Blocks.LAVA)) {
            return carverConfiguration.debugSettings.getLavaState();
        }
        return blockState;
    }

    public abstract boolean carve(CarvingContext var1, C var2, ChunkAccess var3, Function<BlockPos, Biome> var4, Random var5, Aquifer var6, ChunkPos var7, BitSet var8);

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
        return carverConfiguration.debugSettings.isDebugMode();
    }

    public static interface CarveSkipChecker {
        public boolean shouldSkip(CarvingContext var1, double var2, double var4, double var6, int var8);
    }
}

