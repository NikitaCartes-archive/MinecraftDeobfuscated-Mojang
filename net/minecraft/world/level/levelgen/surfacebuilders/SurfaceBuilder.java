/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.surfacebuilders.BadlandsSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.BasaltDeltasSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.DefaultSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.ErodedBadlandsSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.FrozenOceanSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.GiantTreeTaigaSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.GravellyMountainSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.MountainSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.NetherForestSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.NetherSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.NopeSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.ShatteredSavanaSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SoulSandValleySurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.SwampSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.WoodedBadlandsSurfaceBuilder;

public abstract class SurfaceBuilder<C extends SurfaceBuilderConfiguration> {
    private static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
    private static final BlockState GRASS_BLOCK = Blocks.GRASS_BLOCK.defaultBlockState();
    private static final BlockState PODZOL = Blocks.PODZOL.defaultBlockState();
    private static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState COARSE_DIRT = Blocks.COARSE_DIRT.defaultBlockState();
    private static final BlockState SAND = Blocks.SAND.defaultBlockState();
    private static final BlockState RED_SAND = Blocks.RED_SAND.defaultBlockState();
    private static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    private static final BlockState MYCELIUM = Blocks.MYCELIUM.defaultBlockState();
    private static final BlockState SOUL_SAND = Blocks.SOUL_SAND.defaultBlockState();
    private static final BlockState NETHERRACK = Blocks.NETHERRACK.defaultBlockState();
    private static final BlockState ENDSTONE = Blocks.END_STONE.defaultBlockState();
    private static final BlockState CRIMSON_NYLIUM = Blocks.CRIMSON_NYLIUM.defaultBlockState();
    private static final BlockState WARPED_NYLIUM = Blocks.WARPED_NYLIUM.defaultBlockState();
    private static final BlockState NETHER_WART_BLOCK = Blocks.NETHER_WART_BLOCK.defaultBlockState();
    private static final BlockState WARPED_WART_BLOCK = Blocks.WARPED_WART_BLOCK.defaultBlockState();
    private static final BlockState BLACKSTONE = Blocks.BLACKSTONE.defaultBlockState();
    private static final BlockState BASALT = Blocks.BASALT.defaultBlockState();
    private static final BlockState MAGMA = Blocks.MAGMA_BLOCK.defaultBlockState();
    public static final SurfaceBuilderBaseConfiguration CONFIG_PODZOL = new SurfaceBuilderBaseConfiguration(PODZOL, DIRT, GRAVEL);
    public static final SurfaceBuilderBaseConfiguration CONFIG_GRAVEL = new SurfaceBuilderBaseConfiguration(GRAVEL, GRAVEL, GRAVEL);
    public static final SurfaceBuilderBaseConfiguration CONFIG_GRASS = new SurfaceBuilderBaseConfiguration(GRASS_BLOCK, DIRT, GRAVEL);
    public static final SurfaceBuilderBaseConfiguration CONFIG_STONE = new SurfaceBuilderBaseConfiguration(STONE, STONE, GRAVEL);
    public static final SurfaceBuilderBaseConfiguration CONFIG_COARSE_DIRT = new SurfaceBuilderBaseConfiguration(COARSE_DIRT, DIRT, GRAVEL);
    public static final SurfaceBuilderBaseConfiguration CONFIG_DESERT = new SurfaceBuilderBaseConfiguration(SAND, SAND, GRAVEL);
    public static final SurfaceBuilderBaseConfiguration CONFIG_OCEAN_SAND = new SurfaceBuilderBaseConfiguration(GRASS_BLOCK, DIRT, SAND);
    public static final SurfaceBuilderBaseConfiguration CONFIG_FULL_SAND = new SurfaceBuilderBaseConfiguration(SAND, SAND, SAND);
    public static final SurfaceBuilderBaseConfiguration CONFIG_BADLANDS = new SurfaceBuilderBaseConfiguration(RED_SAND, WHITE_TERRACOTTA, GRAVEL);
    public static final SurfaceBuilderBaseConfiguration CONFIG_MYCELIUM = new SurfaceBuilderBaseConfiguration(MYCELIUM, DIRT, GRAVEL);
    public static final SurfaceBuilderBaseConfiguration CONFIG_HELL = new SurfaceBuilderBaseConfiguration(NETHERRACK, NETHERRACK, NETHERRACK);
    public static final SurfaceBuilderBaseConfiguration CONFIG_SOUL_SAND_VALLEY = new SurfaceBuilderBaseConfiguration(SOUL_SAND, SOUL_SAND, SOUL_SAND);
    public static final SurfaceBuilderBaseConfiguration CONFIG_THEEND = new SurfaceBuilderBaseConfiguration(ENDSTONE, ENDSTONE, ENDSTONE);
    public static final SurfaceBuilderBaseConfiguration CONFIG_CRIMSON_FOREST = new SurfaceBuilderBaseConfiguration(CRIMSON_NYLIUM, NETHERRACK, NETHER_WART_BLOCK);
    public static final SurfaceBuilderBaseConfiguration CONFIG_WARPED_FOREST = new SurfaceBuilderBaseConfiguration(WARPED_NYLIUM, NETHERRACK, WARPED_WART_BLOCK);
    public static final SurfaceBuilderBaseConfiguration CONFIG_BASALT_DELTAS = new SurfaceBuilderBaseConfiguration(BLACKSTONE, BASALT, MAGMA);
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> DEFAULT = SurfaceBuilder.register("default", new DefaultSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> MOUNTAIN = SurfaceBuilder.register("mountain", new MountainSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> SHATTERED_SAVANNA = SurfaceBuilder.register("shattered_savanna", new ShatteredSavanaSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> GRAVELLY_MOUNTAIN = SurfaceBuilder.register("gravelly_mountain", new GravellyMountainSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> GIANT_TREE_TAIGA = SurfaceBuilder.register("giant_tree_taiga", new GiantTreeTaigaSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> SWAMP = SurfaceBuilder.register("swamp", new SwampSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> BADLANDS = SurfaceBuilder.register("badlands", new BadlandsSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> WOODED_BADLANDS = SurfaceBuilder.register("wooded_badlands", new WoodedBadlandsSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> ERODED_BADLANDS = SurfaceBuilder.register("eroded_badlands", new ErodedBadlandsSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> FROZEN_OCEAN = SurfaceBuilder.register("frozen_ocean", new FrozenOceanSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> NETHER = SurfaceBuilder.register("nether", new NetherSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> NETHER_FOREST = SurfaceBuilder.register("nether_forest", new NetherForestSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> SOUL_SAND_VALLEY = SurfaceBuilder.register("soul_sand_valley", new SoulSandValleySurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> BASALT_DELTAS = SurfaceBuilder.register("basalt_deltas", new BasaltDeltasSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> NOPE = SurfaceBuilder.register("nope", new NopeSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
    private final Codec<ConfiguredSurfaceBuilder<C>> configuredCodec;

    private static <C extends SurfaceBuilderConfiguration, F extends SurfaceBuilder<C>> F register(String string, F surfaceBuilder) {
        return (F)Registry.register(Registry.SURFACE_BUILDER, string, surfaceBuilder);
    }

    public SurfaceBuilder(Codec<C> codec) {
        this.configuredCodec = ((MapCodec)codec.fieldOf("config")).xmap(this::configured, ConfiguredSurfaceBuilder::config).codec();
    }

    public Codec<ConfiguredSurfaceBuilder<C>> configuredCodec() {
        return this.configuredCodec;
    }

    public ConfiguredSurfaceBuilder<C> configured(C surfaceBuilderConfiguration) {
        return new ConfiguredSurfaceBuilder<C>(this, surfaceBuilderConfiguration);
    }

    public abstract void apply(Random var1, ChunkAccess var2, Biome var3, int var4, int var5, int var6, double var7, BlockState var9, BlockState var10, int var11, int var12, long var13, C var15);

    public void initNoise(long l) {
    }
}

