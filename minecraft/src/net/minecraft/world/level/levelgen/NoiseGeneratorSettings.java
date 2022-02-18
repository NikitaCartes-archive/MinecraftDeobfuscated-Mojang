package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public record NoiseGeneratorSettings(
	NoiseSettings noiseSettings,
	BlockState defaultBlock,
	BlockState defaultFluid,
	NoiseRouterWithOnlyNoises noiseRouter,
	SurfaceRules.RuleSource surfaceRule,
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
					NoiseRouterWithOnlyNoises.CODEC.fieldOf("noise_router").forGetter(NoiseGeneratorSettings::noiseRouter),
					SurfaceRules.RuleSource.CODEC.fieldOf("surface_rule").forGetter(NoiseGeneratorSettings::surfaceRule),
					Codec.INT.fieldOf("sea_level").forGetter(NoiseGeneratorSettings::seaLevel),
					Codec.BOOL.fieldOf("disable_mob_generation").forGetter(NoiseGeneratorSettings::disableMobGeneration),
					Codec.BOOL.fieldOf("aquifers_enabled").forGetter(NoiseGeneratorSettings::isAquifersEnabled),
					Codec.BOOL.fieldOf("ore_veins_enabled").forGetter(NoiseGeneratorSettings::oreVeinsEnabled),
					Codec.BOOL.fieldOf("legacy_random_source").forGetter(NoiseGeneratorSettings::useLegacyRandomSource)
				)
				.apply(instance, NoiseGeneratorSettings::new)
	);
	public static final Codec<Holder<NoiseGeneratorSettings>> CODEC = RegistryFileCodec.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, DIRECT_CODEC);
	public static final ResourceKey<NoiseGeneratorSettings> OVERWORLD = ResourceKey.create(
		Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("overworld")
	);
	public static final ResourceKey<NoiseGeneratorSettings> LARGE_BIOMES = ResourceKey.create(
		Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("large_biomes")
	);
	public static final ResourceKey<NoiseGeneratorSettings> AMPLIFIED = ResourceKey.create(
		Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("amplified")
	);
	public static final ResourceKey<NoiseGeneratorSettings> NETHER = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("nether"));
	public static final ResourceKey<NoiseGeneratorSettings> END = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("end"));
	public static final ResourceKey<NoiseGeneratorSettings> CAVES = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("caves"));
	public static final ResourceKey<NoiseGeneratorSettings> FLOATING_ISLANDS = ResourceKey.create(
		Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("floating_islands")
	);

	public boolean isAquifersEnabled() {
		return this.aquifersEnabled;
	}

	public WorldgenRandom.Algorithm getRandomSource() {
		return this.useLegacyRandomSource ? WorldgenRandom.Algorithm.LEGACY : WorldgenRandom.Algorithm.XOROSHIRO;
	}

	public NoiseRouter createNoiseRouter(Registry<NormalNoise.NoiseParameters> registry, long l) {
		return NoiseRouterData.createNoiseRouter(this.noiseSettings, l, registry, this.getRandomSource(), this.noiseRouter);
	}

	public boolean stable(ResourceKey<NoiseGeneratorSettings> resourceKey) {
		return Objects.equals(
			this, ((RegistryAccess.Frozen)RegistryAccess.BUILTIN.get()).registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY).get(resourceKey)
		);
	}

	private static void register(ResourceKey<NoiseGeneratorSettings> resourceKey, NoiseGeneratorSettings noiseGeneratorSettings) {
		BuiltinRegistries.register(BuiltinRegistries.NOISE_GENERATOR_SETTINGS, resourceKey.location(), noiseGeneratorSettings);
	}

	public static Holder<NoiseGeneratorSettings> bootstrap() {
		return (Holder<NoiseGeneratorSettings>)BuiltinRegistries.NOISE_GENERATOR_SETTINGS.holders().iterator().next();
	}

	private static NoiseGeneratorSettings end() {
		return new NoiseGeneratorSettings(
			NoiseSettings.END_NOISE_SETTINGS,
			Blocks.END_STONE.defaultBlockState(),
			Blocks.AIR.defaultBlockState(),
			NoiseRouterData.end(NoiseSettings.END_NOISE_SETTINGS),
			SurfaceRuleData.end(),
			0,
			true,
			false,
			false,
			true
		);
	}

	private static NoiseGeneratorSettings nether() {
		return new NoiseGeneratorSettings(
			NoiseSettings.NETHER_NOISE_SETTINGS,
			Blocks.NETHERRACK.defaultBlockState(),
			Blocks.LAVA.defaultBlockState(),
			NoiseRouterData.nether(NoiseSettings.NETHER_NOISE_SETTINGS),
			SurfaceRuleData.nether(),
			32,
			false,
			false,
			false,
			true
		);
	}

	private static NoiseGeneratorSettings overworld(boolean bl, boolean bl2) {
		NoiseSettings noiseSettings = NoiseSettings.overworldNoiseSettings(bl, bl2);
		return new NoiseGeneratorSettings(
			noiseSettings,
			Blocks.STONE.defaultBlockState(),
			Blocks.WATER.defaultBlockState(),
			NoiseRouterData.overworld(noiseSettings),
			SurfaceRuleData.overworld(),
			63,
			false,
			true,
			true,
			false
		);
	}

	private static NoiseGeneratorSettings caves() {
		return new NoiseGeneratorSettings(
			NoiseSettings.CAVES_NOISE_SETTINGS,
			Blocks.STONE.defaultBlockState(),
			Blocks.WATER.defaultBlockState(),
			NoiseRouterData.overworldWithoutCaves(NoiseSettings.CAVES_NOISE_SETTINGS),
			SurfaceRuleData.overworldLike(false, true, true),
			32,
			false,
			false,
			false,
			true
		);
	}

	private static NoiseGeneratorSettings floatingIslands() {
		return new NoiseGeneratorSettings(
			NoiseSettings.FLOATING_ISLANDS_NOISE_SETTINGS,
			Blocks.STONE.defaultBlockState(),
			Blocks.WATER.defaultBlockState(),
			NoiseRouterData.overworldWithoutCaves(NoiseSettings.FLOATING_ISLANDS_NOISE_SETTINGS),
			SurfaceRuleData.overworldLike(false, false, false),
			-64,
			false,
			false,
			false,
			true
		);
	}

	static {
		register(OVERWORLD, overworld(false, false));
		register(LARGE_BIOMES, overworld(false, true));
		register(AMPLIFIED, overworld(true, false));
		register(NETHER, nether());
		register(END, end());
		register(CAVES, caves());
		register(FLOATING_ISLANDS, floatingIslands());
	}
}
