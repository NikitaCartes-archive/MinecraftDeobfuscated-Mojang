package net.minecraft.world.level.levelgen;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class NoiseGeneratorSettings {
	public static final Codec<NoiseGeneratorSettings> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					StructureSettings.CODEC.fieldOf("structures").forGetter(NoiseGeneratorSettings::structureSettings),
					NoiseSettings.CODEC.fieldOf("noise").forGetter(NoiseGeneratorSettings::noiseSettings),
					NoiseOctaves.CODEC.fieldOf("octaves").forGetter(NoiseGeneratorSettings::noiseOctaves),
					BlockState.CODEC.fieldOf("default_block").forGetter(NoiseGeneratorSettings::getDefaultBlock),
					BlockState.CODEC.fieldOf("default_fluid").forGetter(NoiseGeneratorSettings::getDefaultFluid),
					Codec.INT.fieldOf("bedrock_roof_position").forGetter(NoiseGeneratorSettings::getBedrockRoofPosition),
					Codec.INT.fieldOf("bedrock_floor_position").forGetter(NoiseGeneratorSettings::getBedrockFloorPosition),
					Codec.INT.fieldOf("sea_level").forGetter(NoiseGeneratorSettings::seaLevel),
					Codec.BOOL.fieldOf("disable_mob_generation").forGetter(NoiseGeneratorSettings::disableMobGeneration),
					Codec.BOOL.fieldOf("aquifers_enabled").forGetter(NoiseGeneratorSettings::isAquifersEnabled),
					Codec.BOOL.fieldOf("noise_caves_enabled").forGetter(NoiseGeneratorSettings::isNoiseCavesEnabled),
					Codec.BOOL.fieldOf("deepslate_enabled").forGetter(NoiseGeneratorSettings::isDeepslateEnabled),
					Codec.BOOL.fieldOf("ore_veins_enabled").forGetter(NoiseGeneratorSettings::isOreVeinsEnabled),
					Codec.BOOL.fieldOf("noodle_caves_enabled").forGetter(NoiseGeneratorSettings::isNoodleCavesEnabled)
				)
				.apply(instance, NoiseGeneratorSettings::new)
	);
	public static final Codec<Supplier<NoiseGeneratorSettings>> CODEC = RegistryFileCodec.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, DIRECT_CODEC);
	private final StructureSettings structureSettings;
	private final NoiseSettings noiseSettings;
	private final NoiseOctaves noiseOctaves;
	private final BlockState defaultBlock;
	private final BlockState defaultFluid;
	private final int bedrockRoofPosition;
	private final int bedrockFloorPosition;
	private final int seaLevel;
	private final boolean disableMobGeneration;
	private final boolean aquifersEnabled;
	private final boolean noiseCavesEnabled;
	private final boolean deepslateEnabled;
	private final boolean oreVeinsEnabled;
	private final boolean noodleCavesEnabled;
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
	private static final NoiseGeneratorSettings BUILTIN_OVERWORLD = register(OVERWORLD, overworld(new StructureSettings(true), false, false));

	private NoiseGeneratorSettings(
		StructureSettings structureSettings,
		NoiseSettings noiseSettings,
		NoiseOctaves noiseOctaves,
		BlockState blockState,
		BlockState blockState2,
		int i,
		int j,
		int k,
		boolean bl,
		boolean bl2,
		boolean bl3,
		boolean bl4,
		boolean bl5,
		boolean bl6
	) {
		this.structureSettings = structureSettings;
		this.noiseSettings = noiseSettings;
		this.noiseOctaves = noiseOctaves;
		this.defaultBlock = blockState;
		this.defaultFluid = blockState2;
		this.bedrockRoofPosition = i;
		this.bedrockFloorPosition = j;
		this.seaLevel = k;
		this.disableMobGeneration = bl;
		this.aquifersEnabled = bl2;
		this.noiseCavesEnabled = bl3;
		this.deepslateEnabled = bl4;
		this.oreVeinsEnabled = bl5;
		this.noodleCavesEnabled = bl6;
	}

	public StructureSettings structureSettings() {
		return this.structureSettings;
	}

	public NoiseSettings noiseSettings() {
		return this.noiseSettings;
	}

	public NoiseOctaves noiseOctaves() {
		return this.noiseOctaves;
	}

	public BlockState getDefaultBlock() {
		return this.defaultBlock;
	}

	public BlockState getDefaultFluid() {
		return this.defaultFluid;
	}

	public int getBedrockRoofPosition() {
		return this.bedrockRoofPosition;
	}

	public int getBedrockFloorPosition() {
		return this.bedrockFloorPosition;
	}

	public int seaLevel() {
		return this.seaLevel;
	}

	@Deprecated
	protected boolean disableMobGeneration() {
		return this.disableMobGeneration;
	}

	public boolean isAquifersEnabled() {
		return this.aquifersEnabled;
	}

	public boolean isNoiseCavesEnabled() {
		return this.noiseCavesEnabled;
	}

	public boolean isDeepslateEnabled() {
		return this.deepslateEnabled;
	}

	public boolean isOreVeinsEnabled() {
		return this.oreVeinsEnabled;
	}

	public boolean isNoodleCavesEnabled() {
		return this.noodleCavesEnabled;
	}

	public boolean stable(ResourceKey<NoiseGeneratorSettings> resourceKey) {
		return Objects.equals(this, BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(resourceKey));
	}

	private static NoiseGeneratorSettings register(ResourceKey<NoiseGeneratorSettings> resourceKey, NoiseGeneratorSettings noiseGeneratorSettings) {
		return BuiltinRegistries.register(BuiltinRegistries.NOISE_GENERATOR_SETTINGS, resourceKey.location(), noiseGeneratorSettings);
	}

	public static NoiseGeneratorSettings bootstrap() {
		return BUILTIN_OVERWORLD;
	}

	private static NoiseGeneratorSettings endLikePreset(
		StructureSettings structureSettings, BlockState blockState, BlockState blockState2, boolean bl, boolean bl2
	) {
		return new NoiseGeneratorSettings(
			structureSettings,
			NoiseSettings.create(
				0,
				128,
				new NoiseSamplingSettings(2.0, 1.0, 80.0, 160.0),
				new NoiseSlider(-23.4375, 64, -46),
				new NoiseSlider(-0.234375, 7, 1),
				2,
				1,
				0.0,
				0.0,
				true,
				false,
				bl2,
				false,
				true
			),
			new NoiseOctaves(
				new NormalNoise.NoiseParameters(0, 0.0),
				new NormalNoise.NoiseParameters(0, 0.0),
				new NormalNoise.NoiseParameters(0, 0.0),
				new NormalNoise.NoiseParameters(0, 0.0),
				new NormalNoise.NoiseParameters(0, 0.0),
				new NormalNoise.NoiseParameters(0, 0.0)
			),
			blockState,
			blockState2,
			Integer.MIN_VALUE,
			Integer.MIN_VALUE,
			0,
			bl,
			false,
			false,
			false,
			false,
			false
		);
	}

	private static NoiseGeneratorSettings netherLikePreset(StructureSettings structureSettings, BlockState blockState, BlockState blockState2) {
		Map<StructureFeature<?>, StructureFeatureConfiguration> map = Maps.<StructureFeature<?>, StructureFeatureConfiguration>newHashMap(StructureSettings.DEFAULTS);
		map.put(StructureFeature.RUINED_PORTAL, new StructureFeatureConfiguration(25, 10, 34222645));
		return new NoiseGeneratorSettings(
			new StructureSettings(Optional.ofNullable(structureSettings.stronghold()), map),
			NoiseSettings.create(
				0,
				128,
				new NoiseSamplingSettings(1.0, 3.0, 80.0, 60.0),
				new NoiseSlider(0.9375, 3, 0),
				new NoiseSlider(2.5, 4, -1),
				1,
				2,
				0.0,
				-0.030078125,
				false,
				false,
				false,
				false,
				true
			),
			new NoiseOctaves(
				new NormalNoise.NoiseParameters(-7, 1.0, 1.0),
				new NormalNoise.NoiseParameters(-7, 1.0, 1.0),
				new NormalNoise.NoiseParameters(-7, 1.0, 1.0),
				new NormalNoise.NoiseParameters(-7, 1.0, 1.0),
				new NormalNoise.NoiseParameters(-7, 1.0, 1.0),
				new NormalNoise.NoiseParameters(0, 0.0)
			),
			blockState,
			blockState2,
			0,
			0,
			32,
			false,
			false,
			false,
			false,
			false,
			false
		);
	}

	private static NoiseGeneratorSettings overworld(StructureSettings structureSettings, boolean bl, boolean bl2) {
		int i = bl2 ? -2 : 0;
		double d = 0.9999999814507745;
		return new NoiseGeneratorSettings(
			structureSettings,
			NoiseSettings.create(
				-64,
				384,
				new NoiseSamplingSettings(0.9999999814507745, 0.9999999814507745, 80.0, 160.0),
				new NoiseSlider(-0.078125, 2, 8),
				new NoiseSlider(0.1171875, 3, 0),
				1,
				2,
				1.0,
				-0.51875,
				true,
				true,
				false,
				bl,
				false
			),
			new NoiseOctaves(
				new NormalNoise.NoiseParameters(-9 + i, 1.5, 0.0, 1.0, 0.0, 0.0, 0.0),
				new NormalNoise.NoiseParameters(-7 + i, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0),
				new NormalNoise.NoiseParameters(-9 + i, 1.0, 1.0, 2.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0),
				new NormalNoise.NoiseParameters(-9 + i, 1.0, 1.0, 0.0, 1.0, 1.0),
				new NormalNoise.NoiseParameters(-7 + i, 1.0, 2.0, 1.0, 0.0, 0.0, 0.0),
				new NormalNoise.NoiseParameters(-3 + i, 1.0, 1.0, 1.0, 0.0)
			),
			Blocks.STONE.defaultBlockState(),
			Blocks.WATER.defaultBlockState(),
			Integer.MIN_VALUE,
			0,
			63,
			false,
			true,
			true,
			true,
			true,
			true
		);
	}

	static {
		register(LARGE_BIOMES, overworld(new StructureSettings(true), false, true));
		register(AMPLIFIED, overworld(new StructureSettings(true), true, false));
		register(NETHER, netherLikePreset(new StructureSettings(false), Blocks.NETHERRACK.defaultBlockState(), Blocks.LAVA.defaultBlockState()));
		register(END, endLikePreset(new StructureSettings(false), Blocks.END_STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), true, true));
		register(CAVES, netherLikePreset(new StructureSettings(true), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState()));
		register(FLOATING_ISLANDS, endLikePreset(new StructureSettings(true), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), false, false));
	}
}
