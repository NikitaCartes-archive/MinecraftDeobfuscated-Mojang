package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

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
	private static final BlockState SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();
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
	public static final SurfaceBuilderBaseConfiguration CONFIG_GROVE = new SurfaceBuilderBaseConfiguration(SNOW_BLOCK, DIRT, GRAVEL);
	public static final SurfaceBuilderBaseConfiguration CONFIG_SNOWY_SLOPES = new SurfaceBuilderBaseConfiguration(SNOW_BLOCK, SNOW_BLOCK, GRAVEL);
	public static final SurfaceBuilderBaseConfiguration CONFIG_LOFTY_PEAKS = new SurfaceBuilderBaseConfiguration(SNOW_BLOCK, STONE, STONE);
	public static final SurfaceBuilderBaseConfiguration CONFIG_SNOWCAPPED_PEAKS = new SurfaceBuilderBaseConfiguration(SNOW_BLOCK, SNOW_BLOCK, STONE);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> DEFAULT = register(
		"default", new DefaultSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> MOUNTAIN = register(
		"mountain", new MountainSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> SHATTERED_SAVANNA = register(
		"shattered_savanna", new ShatteredSavanaSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> GRAVELLY_MOUNTAIN = register(
		"gravelly_mountain", new GravellyMountainSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> GIANT_TREE_TAIGA = register(
		"giant_tree_taiga", new GiantTreeTaigaSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> SWAMP = register("swamp", new SwampSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> BADLANDS = register(
		"badlands", new BadlandsSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> WOODED_BADLANDS = register(
		"wooded_badlands", new WoodedBadlandsSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> ERODED_BADLANDS = register(
		"eroded_badlands", new ErodedBadlandsSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> FROZEN_OCEAN = register(
		"frozen_ocean", new FrozenOceanSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> NETHER = register(
		"nether", new NetherSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> NETHER_FOREST = register(
		"nether_forest", new NetherForestSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> SOUL_SAND_VALLEY = register(
		"soul_sand_valley", new SoulSandValleySurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> BASALT_DELTAS = register(
		"basalt_deltas", new BasaltDeltasSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> GROVE = register("grove", new GroveSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> SNOWCAPPED_PEAKS = register(
		"snowcapped_peaks", new SnowcappedPeaksSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> NOPE = register("nope", new NopeSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC));
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> SNOWY_SLOPES = register(
		"snowy_slopes", new SnowySlopesSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> LOFTY_PEAKS = register(
		"lofty_peaks", new LoftyPeaksSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC)
	);
	public static final SurfaceBuilder<SurfaceBuilderBaseConfiguration> STONY_PEAKS = register(
		"stony_peaks", new StonyPeaksSurfaceBuilder(SurfaceBuilderBaseConfiguration.CODEC)
	);
	private final Codec<ConfiguredSurfaceBuilder<C>> configuredCodec;

	private static <C extends SurfaceBuilderConfiguration, F extends SurfaceBuilder<C>> F register(String string, F surfaceBuilder) {
		return Registry.register(Registry.SURFACE_BUILDER, string, surfaceBuilder);
	}

	public SurfaceBuilder(Codec<C> codec) {
		this.configuredCodec = codec.fieldOf("config").<ConfiguredSurfaceBuilder<C>>xmap(this::configured, ConfiguredSurfaceBuilder::config).codec();
	}

	public Codec<ConfiguredSurfaceBuilder<C>> configuredCodec() {
		return this.configuredCodec;
	}

	public ConfiguredSurfaceBuilder<C> configured(C surfaceBuilderConfiguration) {
		return new ConfiguredSurfaceBuilder<>(this, surfaceBuilderConfiguration);
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
		int m,
		long n,
		C surfaceBuilderConfiguration
	);

	public void initNoise(long l) {
	}
}
