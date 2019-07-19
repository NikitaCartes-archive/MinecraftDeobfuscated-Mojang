/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.surfacebuilders.BadlandsSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.DefaultSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.ErodedBadlandsSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.FrozenOceanSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.GiantTreeTaigaSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.GravellyMountainSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.MountainSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.NetherSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.NopeSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.ShatteredSavanaSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.SwampSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.WoodedBadlandsSurfaceBuilder;

public abstract class SurfaceBuilder<C extends SurfaceBuilderConfiguration> {
    public static final BlockState AIR = Blocks.AIR.defaultBlockState();
    public static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
    public static final BlockState GRASS_BLOCK = Blocks.GRASS_BLOCK.defaultBlockState();
    public static final BlockState PODZOL = Blocks.PODZOL.defaultBlockState();
    public static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
    public static final BlockState STONE = Blocks.STONE.defaultBlockState();
    public static final BlockState COARSE_DIRT = Blocks.COARSE_DIRT.defaultBlockState();
    public static final BlockState SAND = Blocks.SAND.defaultBlockState();
    public static final BlockState RED_SAND = Blocks.RED_SAND.defaultBlockState();
    public static final BlockState WHITE_TERRACOTTA = Blocks.WHITE_TERRACOTTA.defaultBlockState();
    public static final BlockState MYCELIUM = Blocks.MYCELIUM.defaultBlockState();
    public static final BlockState NETHERRACK = Blocks.NETHERRACK.defaultBlockState();
    public static final BlockState ENDSTONE = Blocks.END_STONE.defaultBlockState();
    public static final SurfaceBuilderBaseConfiguration CONFIG_EMPTY = new SurfaceBuilderBaseConfiguration(AIR, AIR, AIR);
    public static final SurfaceBuilderBaseConfiguration CONFIG_PODZOL = new SurfaceBuilderBaseConfiguration(PODZOL, DIRT, GRAVEL);
    public static final SurfaceBuilderBaseConfiguration CONFIG_GRAVEL = new SurfaceBuilderBaseConfiguration(GRAVEL, GRAVEL, GRAVEL);
    public static final SurfaceBuilderBaseConfiguration CONFIG_GRASS = new SurfaceBuilderBaseConfiguration(GRASS_BLOCK, DIRT, GRAVEL);
    public static final SurfaceBuilderBaseConfiguration CONFIG_DIRT = new SurfaceBuilderBaseConfiguration(DIRT, DIRT, GRAVEL);
    public static final SurfaceBuilderBaseConfiguration CONFIG_STONE = new SurfaceBuilderBaseConfiguration(STONE, STONE, GRAVEL);
    public static final SurfaceBuilderBaseConfiguration CONFIG_COARSE_DIRT = new SurfaceBuilderBaseConfiguration(COARSE_DIRT, DIRT, GRAVEL);
    public static final SurfaceBuilderBaseConfiguration CONFIG_DESERT = new SurfaceBuilderBaseConfiguration(SAND, SAND, GRAVEL);
    public static final SurfaceBuilderBaseConfiguration CONFIG_OCEAN_SAND = new SurfaceBuilderBaseConfiguration(GRASS_BLOCK, DIRT, SAND);
    public static final SurfaceBuilderBaseConfiguration CONFIG_FULL_SAND = new SurfaceBuilderBaseConfiguration(SAND, SAND, SAND);
    public static final SurfaceBuilderBaseConfiguration CONFIG_BADLANDS = new SurfaceBuilderBaseConfiguration(RED_SAND, WHITE_TERRACOTTA, GRAVEL);
    public static final SurfaceBuilderBaseConfiguration CONFIG_MYCELIUM = new SurfaceBuilderBaseConfiguration(MYCELIUM, DIRT, GRAVEL);
    public static final SurfaceBuilderBaseConfiguration CONFIG_HELL = new SurfaceBuilderBaseConfiguration(NETHERRACK, NETHERRACK, NETHERRACK);
    public static final SurfaceBuilderBaseConfiguration CONFIG_THEEND = new SurfaceBuilderBaseConfiguration(ENDSTONE, ENDSTONE, ENDSTONE);
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> DEFAULT = SurfaceBuilder.register("default", new DefaultSurfaceBuilder((Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration>)((Function<Dynamic<?>, SurfaceBuilderBaseConfiguration>)SurfaceBuilderBaseConfiguration::deserialize)));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> MOUNTAIN = SurfaceBuilder.register("mountain", new MountainSurfaceBuilder((Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration>)((Function<Dynamic<?>, SurfaceBuilderBaseConfiguration>)SurfaceBuilderBaseConfiguration::deserialize)));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> SHATTERED_SAVANNA = SurfaceBuilder.register("shattered_savanna", new ShatteredSavanaSurfaceBuilder((Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration>)((Function<Dynamic<?>, SurfaceBuilderBaseConfiguration>)SurfaceBuilderBaseConfiguration::deserialize)));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> GRAVELLY_MOUNTAIN = SurfaceBuilder.register("gravelly_mountain", new GravellyMountainSurfaceBuilder((Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration>)((Function<Dynamic<?>, SurfaceBuilderBaseConfiguration>)SurfaceBuilderBaseConfiguration::deserialize)));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> GIANT_TREE_TAIGA = SurfaceBuilder.register("giant_tree_taiga", new GiantTreeTaigaSurfaceBuilder((Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration>)((Function<Dynamic<?>, SurfaceBuilderBaseConfiguration>)SurfaceBuilderBaseConfiguration::deserialize)));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> SWAMP = SurfaceBuilder.register("swamp", new SwampSurfaceBuilder((Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration>)((Function<Dynamic<?>, SurfaceBuilderBaseConfiguration>)SurfaceBuilderBaseConfiguration::deserialize)));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> BADLANDS = SurfaceBuilder.register("badlands", new BadlandsSurfaceBuilder((Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration>)((Function<Dynamic<?>, SurfaceBuilderBaseConfiguration>)SurfaceBuilderBaseConfiguration::deserialize)));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> WOODED_BADLANDS = SurfaceBuilder.register("wooded_badlands", new WoodedBadlandsSurfaceBuilder((Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration>)((Function<Dynamic<?>, SurfaceBuilderBaseConfiguration>)SurfaceBuilderBaseConfiguration::deserialize)));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> ERODED_BADLANDS = SurfaceBuilder.register("eroded_badlands", new ErodedBadlandsSurfaceBuilder((Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration>)((Function<Dynamic<?>, SurfaceBuilderBaseConfiguration>)SurfaceBuilderBaseConfiguration::deserialize)));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> FROZEN_OCEAN = SurfaceBuilder.register("frozen_ocean", new FrozenOceanSurfaceBuilder((Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration>)((Function<Dynamic<?>, SurfaceBuilderBaseConfiguration>)SurfaceBuilderBaseConfiguration::deserialize)));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> NETHER = SurfaceBuilder.register("nether", new NetherSurfaceBuilder((Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration>)((Function<Dynamic<?>, SurfaceBuilderBaseConfiguration>)SurfaceBuilderBaseConfiguration::deserialize)));
    public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> NOPE = SurfaceBuilder.register("nope", new NopeSurfaceBuilder((Function<Dynamic<?>, ? extends SurfaceBuilderBaseConfiguration>)((Function<Dynamic<?>, SurfaceBuilderBaseConfiguration>)SurfaceBuilderBaseConfiguration::deserialize)));
    private final Function<Dynamic<?>, ? extends C> configurationFactory;

    private static <C extends SurfaceBuilderConfiguration, F extends SurfaceBuilder<C>> F register(String string, F surfaceBuilder) {
        return (F)Registry.register(Registry.SURFACE_BUILDER, string, surfaceBuilder);
    }

    public SurfaceBuilder(Function<Dynamic<?>, ? extends C> function) {
        this.configurationFactory = function;
    }

    public abstract void apply(Random var1, ChunkAccess var2, Biome var3, int var4, int var5, int var6, double var7, BlockState var9, BlockState var10, int var11, long var12, C var14);

    public void initNoise(long l) {
    }
}

