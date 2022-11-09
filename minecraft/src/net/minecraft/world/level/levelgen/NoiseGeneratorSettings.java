package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public record NoiseGeneratorSettings(
	NoiseSettings noiseSettings,
	BlockState defaultBlock,
	BlockState defaultFluid,
	NoiseRouter noiseRouter,
	SurfaceRules.RuleSource surfaceRule,
	List<Climate.ParameterPoint> spawnTarget,
	int seaLevel,
	@Deprecated boolean disableMobGeneration,
	boolean aquifersEnabled,
	boolean oreVeinsEnabled,
	boolean useLegacyRandomSource
) {
	public static final Codec<NoiseGeneratorSettings> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					NoiseSettings.CODEC.fieldOf("noise").forGetter(NoiseGeneratorSettings::noiseSettings),
					BlockState.CODEC.fieldOf("default_block").forGetter(NoiseGeneratorSettings::defaultBlock),
					BlockState.CODEC.fieldOf("default_fluid").forGetter(NoiseGeneratorSettings::defaultFluid),
					NoiseRouter.CODEC.fieldOf("noise_router").forGetter(NoiseGeneratorSettings::noiseRouter),
					SurfaceRules.RuleSource.CODEC.fieldOf("surface_rule").forGetter(NoiseGeneratorSettings::surfaceRule),
					Climate.ParameterPoint.CODEC.listOf().fieldOf("spawn_target").forGetter(NoiseGeneratorSettings::spawnTarget),
					Codec.INT.fieldOf("sea_level").forGetter(NoiseGeneratorSettings::seaLevel),
					Codec.BOOL.fieldOf("disable_mob_generation").forGetter(NoiseGeneratorSettings::disableMobGeneration),
					Codec.BOOL.fieldOf("aquifers_enabled").forGetter(NoiseGeneratorSettings::isAquifersEnabled),
					Codec.BOOL.fieldOf("ore_veins_enabled").forGetter(NoiseGeneratorSettings::oreVeinsEnabled),
					Codec.BOOL.fieldOf("legacy_random_source").forGetter(NoiseGeneratorSettings::useLegacyRandomSource)
				)
				.apply(instance, NoiseGeneratorSettings::new)
	);
	public static final Codec<Holder<NoiseGeneratorSettings>> CODEC = RegistryFileCodec.create(Registries.NOISE_SETTINGS, DIRECT_CODEC);
	public static final ResourceKey<NoiseGeneratorSettings> OVERWORLD = ResourceKey.create(Registries.NOISE_SETTINGS, new ResourceLocation("overworld"));
	public static final ResourceKey<NoiseGeneratorSettings> LARGE_BIOMES = ResourceKey.create(Registries.NOISE_SETTINGS, new ResourceLocation("large_biomes"));
	public static final ResourceKey<NoiseGeneratorSettings> AMPLIFIED = ResourceKey.create(Registries.NOISE_SETTINGS, new ResourceLocation("amplified"));
	public static final ResourceKey<NoiseGeneratorSettings> NETHER = ResourceKey.create(Registries.NOISE_SETTINGS, new ResourceLocation("nether"));
	public static final ResourceKey<NoiseGeneratorSettings> END = ResourceKey.create(Registries.NOISE_SETTINGS, new ResourceLocation("end"));
	public static final ResourceKey<NoiseGeneratorSettings> CAVES = ResourceKey.create(Registries.NOISE_SETTINGS, new ResourceLocation("caves"));
	public static final ResourceKey<NoiseGeneratorSettings> FLOATING_ISLANDS = ResourceKey.create(
		Registries.NOISE_SETTINGS, new ResourceLocation("floating_islands")
	);

	public boolean isAquifersEnabled() {
		return this.aquifersEnabled;
	}

	public WorldgenRandom.Algorithm getRandomSource() {
		return this.useLegacyRandomSource ? WorldgenRandom.Algorithm.LEGACY : WorldgenRandom.Algorithm.XOROSHIRO;
	}

	public static void bootstrap(BootstapContext<NoiseGeneratorSettings> bootstapContext) {
		bootstapContext.register(OVERWORLD, overworld(bootstapContext, false, false));
		bootstapContext.register(LARGE_BIOMES, overworld(bootstapContext, false, true));
		bootstapContext.register(AMPLIFIED, overworld(bootstapContext, true, false));
		bootstapContext.register(NETHER, nether(bootstapContext));
		bootstapContext.register(END, end(bootstapContext));
		bootstapContext.register(CAVES, caves(bootstapContext));
		bootstapContext.register(FLOATING_ISLANDS, floatingIslands(bootstapContext));
	}

	private static NoiseGeneratorSettings end(BootstapContext<?> bootstapContext) {
		return new NoiseGeneratorSettings(
			NoiseSettings.END_NOISE_SETTINGS,
			Blocks.END_STONE.defaultBlockState(),
			Blocks.AIR.defaultBlockState(),
			NoiseRouterData.end(bootstapContext.lookup(Registries.DENSITY_FUNCTION)),
			SurfaceRuleData.end(),
			List.of(),
			0,
			true,
			false,
			false,
			true
		);
	}

	private static NoiseGeneratorSettings nether(BootstapContext<?> bootstapContext) {
		return new NoiseGeneratorSettings(
			NoiseSettings.NETHER_NOISE_SETTINGS,
			Blocks.NETHERRACK.defaultBlockState(),
			Blocks.LAVA.defaultBlockState(),
			NoiseRouterData.nether(bootstapContext.lookup(Registries.DENSITY_FUNCTION), bootstapContext.lookup(Registries.NOISE)),
			SurfaceRuleData.nether(),
			List.of(),
			32,
			false,
			false,
			false,
			true
		);
	}

	private static NoiseGeneratorSettings overworld(BootstapContext<?> bootstapContext, boolean bl, boolean bl2) {
		return new NoiseGeneratorSettings(
			NoiseSettings.OVERWORLD_NOISE_SETTINGS,
			Blocks.STONE.defaultBlockState(),
			Blocks.WATER.defaultBlockState(),
			NoiseRouterData.overworld(bootstapContext.lookup(Registries.DENSITY_FUNCTION), bootstapContext.lookup(Registries.NOISE), bl2, bl),
			SurfaceRuleData.overworld(),
			new OverworldBiomeBuilder().spawnTarget(),
			63,
			false,
			true,
			true,
			false
		);
	}

	private static NoiseGeneratorSettings caves(BootstapContext<?> bootstapContext) {
		return new NoiseGeneratorSettings(
			NoiseSettings.CAVES_NOISE_SETTINGS,
			Blocks.STONE.defaultBlockState(),
			Blocks.WATER.defaultBlockState(),
			NoiseRouterData.caves(bootstapContext.lookup(Registries.DENSITY_FUNCTION), bootstapContext.lookup(Registries.NOISE)),
			SurfaceRuleData.overworldLike(false, true, true),
			List.of(),
			32,
			false,
			false,
			false,
			true
		);
	}

	private static NoiseGeneratorSettings floatingIslands(BootstapContext<?> bootstapContext) {
		return new NoiseGeneratorSettings(
			NoiseSettings.FLOATING_ISLANDS_NOISE_SETTINGS,
			Blocks.STONE.defaultBlockState(),
			Blocks.WATER.defaultBlockState(),
			NoiseRouterData.floatingIslands(bootstapContext.lookup(Registries.DENSITY_FUNCTION), bootstapContext.lookup(Registries.NOISE)),
			SurfaceRuleData.overworldLike(false, false, false),
			List.of(),
			-64,
			false,
			false,
			false,
			true
		);
	}

	public static NoiseGeneratorSettings dummy() {
		return new NoiseGeneratorSettings(
			NoiseSettings.OVERWORLD_NOISE_SETTINGS,
			Blocks.STONE.defaultBlockState(),
			Blocks.AIR.defaultBlockState(),
			NoiseRouterData.none(),
			SurfaceRuleData.air(),
			List.of(),
			63,
			true,
			false,
			false,
			false
		);
	}
}
