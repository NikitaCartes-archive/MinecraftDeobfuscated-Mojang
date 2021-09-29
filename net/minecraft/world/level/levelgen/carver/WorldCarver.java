/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CanyonWorldCarver;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.NetherWorldCarver;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

public abstract class WorldCarver<C extends CarverConfiguration> {
    public static final WorldCarver<CaveCarverConfiguration> CAVE = WorldCarver.register("cave", new CaveWorldCarver(CaveCarverConfiguration.CODEC));
    public static final WorldCarver<CaveCarverConfiguration> NETHER_CAVE = WorldCarver.register("nether_cave", new NetherWorldCarver(CaveCarverConfiguration.CODEC));
    public static final WorldCarver<CanyonCarverConfiguration> CANYON = WorldCarver.register("canyon", new CanyonWorldCarver(CanyonCarverConfiguration.CODEC));
    protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
    protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
    protected static final FluidState WATER = Fluids.WATER.defaultFluidState();
    protected static final FluidState LAVA = Fluids.LAVA.defaultFluidState();
    protected Set<Block> replaceableBlocks = ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.DIRT, Blocks.COARSE_DIRT, new Block[]{Blocks.PODZOL, Blocks.GRASS_BLOCK, Blocks.TERRACOTTA, Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA, Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.BLACK_TERRACOTTA, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.MYCELIUM, Blocks.SNOW, Blocks.PACKED_ICE, Blocks.DEEPSLATE, Blocks.CALCITE, Blocks.SAND, Blocks.RED_SAND, Blocks.GRAVEL, Blocks.TUFF, Blocks.GRANITE, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.RAW_IRON_BLOCK, Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.RAW_COPPER_BLOCK});
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

    protected boolean carveEllipsoid(CarvingContext carvingContext, C carverConfiguration, ChunkAccess chunkAccess, Function<BlockPos, Biome> function, Aquifer aquifer, double d, double e, double f, double g, double h, CarvingMask carvingMask, CarveSkipChecker carveSkipChecker) {
        ChunkPos chunkPos = chunkAccess.getPos();
        double i = chunkPos.getMiddleBlockX();
        double j = chunkPos.getMiddleBlockZ();
        double k = 16.0 + g * 2.0;
        if (Math.abs(d - i) > k || Math.abs(f - j) > k) {
            return false;
        }
        int l = chunkPos.getMinBlockX();
        int m = chunkPos.getMinBlockZ();
        int n = Math.max(Mth.floor(d - g) - l - 1, 0);
        int o = Math.min(Mth.floor(d + g) - l, 15);
        int p = Math.max(Mth.floor(e - h) - 1, carvingContext.getMinGenY() + 1);
        int q = Math.min(Mth.floor(e + h) + 1, carvingContext.getMinGenY() + carvingContext.getGenDepth() - 8);
        int r = Math.max(Mth.floor(f - g) - m - 1, 0);
        int s = Math.min(Mth.floor(f + g) - m, 15);
        boolean bl = false;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        for (int t = n; t <= o; ++t) {
            int u = chunkPos.getBlockX(t);
            double v = ((double)u + 0.5 - d) / g;
            for (int w = r; w <= s; ++w) {
                int x = chunkPos.getBlockZ(w);
                double y = ((double)x + 0.5 - f) / g;
                if (v * v + y * y >= 1.0) continue;
                MutableBoolean mutableBoolean = new MutableBoolean(false);
                for (int z = q; z > p; --z) {
                    double aa = ((double)z - 0.5 - e) / h;
                    if (carveSkipChecker.shouldSkip(carvingContext, v, aa, y, z) || carvingMask.get(t, z, w) && !WorldCarver.isDebugEnabled(carverConfiguration)) continue;
                    carvingMask.set(t, z, w);
                    mutableBlockPos.set(u, z, x);
                    bl |= this.carveBlock(carvingContext, carverConfiguration, chunkAccess, function, carvingMask, mutableBlockPos, mutableBlockPos2, aquifer, mutableBoolean);
                }
            }
        }
        return bl;
    }

    protected boolean carveBlock(CarvingContext carvingContext, C carverConfiguration, ChunkAccess chunkAccess, Function<BlockPos, Biome> function, CarvingMask carvingMask, BlockPos.MutableBlockPos mutableBlockPos, BlockPos.MutableBlockPos mutableBlockPos2, Aquifer aquifer, MutableBoolean mutableBoolean) {
        BlockState blockState = chunkAccess.getBlockState(mutableBlockPos);
        if (blockState.is(Blocks.GRASS_BLOCK) || blockState.is(Blocks.MYCELIUM)) {
            mutableBoolean.setTrue();
        }
        if (!this.canReplaceBlock(blockState) && !WorldCarver.isDebugEnabled(carverConfiguration)) {
            return false;
        }
        BlockState blockState2 = this.getCarveState(carvingContext, carverConfiguration, mutableBlockPos, aquifer);
        if (blockState2 == null) {
            return false;
        }
        chunkAccess.setBlockState(mutableBlockPos, blockState2, false);
        if (aquifer.shouldScheduleFluidUpdate() && !blockState2.getFluidState().isEmpty()) {
            chunkAccess.getLiquidTicks().scheduleTick(mutableBlockPos, blockState2.getFluidState().getType(), 0);
        }
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
        BlockState blockState = aquifer.computeSubstance(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0.0, 0.0);
        if (blockState == null) {
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

    public abstract boolean carve(CarvingContext var1, C var2, ChunkAccess var3, Function<BlockPos, Biome> var4, Random var5, Aquifer var6, ChunkPos var7, CarvingMask var8);

    public abstract boolean isStartChunk(C var1, Random var2);

    protected boolean canReplaceBlock(BlockState blockState) {
        return this.replaceableBlocks.contains(blockState.getBlock());
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

