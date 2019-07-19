package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

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
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> DEFAULT = register(
		"default", new DefaultSurfaceBuilder(SurfaceBuilderBaseConfiguration::deserialize)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> MOUNTAIN = register(
		"mountain", new MountainSurfaceBuilder(SurfaceBuilderBaseConfiguration::deserialize)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> SHATTERED_SAVANNA = register(
		"shattered_savanna", new ShatteredSavanaSurfaceBuilder(SurfaceBuilderBaseConfiguration::deserialize)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> GRAVELLY_MOUNTAIN = register(
		"gravelly_mountain", new GravellyMountainSurfaceBuilder(SurfaceBuilderBaseConfiguration::deserialize)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> GIANT_TREE_TAIGA = register(
		"giant_tree_taiga", new GiantTreeTaigaSurfaceBuilder(SurfaceBuilderBaseConfiguration::deserialize)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> SWAMP = register(
		"swamp", new SwampSurfaceBuilder(SurfaceBuilderBaseConfiguration::deserialize)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> BADLANDS = register(
		"badlands", new BadlandsSurfaceBuilder(SurfaceBuilderBaseConfiguration::deserialize)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> WOODED_BADLANDS = register(
		"wooded_badlands", new WoodedBadlandsSurfaceBuilder(SurfaceBuilderBaseConfiguration::deserialize)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> ERODED_BADLANDS = register(
		"eroded_badlands", new ErodedBadlandsSurfaceBuilder(SurfaceBuilderBaseConfiguration::deserialize)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> FROZEN_OCEAN = register(
		"frozen_ocean", new FrozenOceanSurfaceBuilder(SurfaceBuilderBaseConfiguration::deserialize)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> NETHER = register(
		"nether", new NetherSurfaceBuilder(SurfaceBuilderBaseConfiguration::deserialize)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> NOPE = register(
		"nope", new NopeSurfaceBuilder(SurfaceBuilderBaseConfiguration::deserialize)
	);
	private final Function<Dynamic<?>, ? extends C> configurationFactory;

	private static <C extends SurfaceBuilderConfiguration, F extends SurfaceBuilder<C>> F register(String string, F surfaceBuilder) {
		return Registry.register(Registry.SURFACE_BUILDER, string, surfaceBuilder);
	}

	public SurfaceBuilder(Function<Dynamic<?>, ? extends C> function) {
		this.configurationFactory = function;
	}

	public abstract void apply(
		Random random,
		ChunkAccess chunkAccess,
		Biome biome,
		int i,
		int j,
		int k,
		double d,
		BlockState blockState,
		BlockState blockState2,
		int l,
		long m,
		C surfaceBuilderConfiguration
	);

	public void initNoise(long l) {
	}
}
