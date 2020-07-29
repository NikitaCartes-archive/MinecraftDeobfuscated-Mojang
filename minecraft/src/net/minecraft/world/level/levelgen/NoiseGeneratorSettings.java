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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

public final class NoiseGeneratorSettings {
	public static final Codec<NoiseGeneratorSettings> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					StructureSettings.CODEC.fieldOf("structures").forGetter(NoiseGeneratorSettings::structureSettings),
					NoiseSettings.CODEC.fieldOf("noise").forGetter(NoiseGeneratorSettings::noiseSettings),
					BlockState.CODEC.fieldOf("default_block").forGetter(NoiseGeneratorSettings::getDefaultBlock),
					BlockState.CODEC.fieldOf("default_fluid").forGetter(NoiseGeneratorSettings::getDefaultFluid),
					Codec.intRange(-20, 276).fieldOf("bedrock_roof_position").forGetter(NoiseGeneratorSettings::getBedrockRoofPosition),
					Codec.intRange(-20, 276).fieldOf("bedrock_floor_position").forGetter(NoiseGeneratorSettings::getBedrockFloorPosition),
					Codec.intRange(0, 255).fieldOf("sea_level").forGetter(NoiseGeneratorSettings::seaLevel),
					Codec.BOOL.fieldOf("disable_mob_generation").forGetter(NoiseGeneratorSettings::disableMobGeneration)
				)
				.apply(instance, NoiseGeneratorSettings::new)
	);
	public static final Codec<Supplier<NoiseGeneratorSettings>> CODEC = RegistryFileCodec.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, DIRECT_CODEC);
	private final StructureSettings structureSettings;
	private final NoiseSettings noiseSettings;
	private final BlockState defaultBlock;
	private final BlockState defaultFluid;
	private final int bedrockRoofPosition;
	private final int bedrockFloorPosition;
	private final int seaLevel;
	private final boolean disableMobGeneration;
	private final Optional<ResourceLocation> preset;
	public static final NoiseGeneratorSettings OVERWORLD = register(overworld(new StructureSettings(true), false, new ResourceLocation("overworld")));
	public static final NoiseGeneratorSettings AMPLIFIED = register(overworld(new StructureSettings(true), true, new ResourceLocation("amplified")));
	public static final NoiseGeneratorSettings NETHER = register(
		nether(new StructureSettings(false), Blocks.NETHERRACK.defaultBlockState(), Blocks.LAVA.defaultBlockState(), new ResourceLocation("nether"))
	);
	public static final NoiseGeneratorSettings END = register(
		end(new StructureSettings(false), Blocks.END_STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), new ResourceLocation("end"), true, true)
	);
	public static final NoiseGeneratorSettings CAVES = register(
		nether(new StructureSettings(false), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), new ResourceLocation("caves"))
	);
	public static final NoiseGeneratorSettings FLOATING_ISLANDS = register(
		end(new StructureSettings(false), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), new ResourceLocation("floating_islands"), false, false)
	);

	private NoiseGeneratorSettings(
		StructureSettings structureSettings, NoiseSettings noiseSettings, BlockState blockState, BlockState blockState2, int i, int j, int k, boolean bl
	) {
		this(structureSettings, noiseSettings, blockState, blockState2, i, j, k, bl, Optional.empty());
	}

	private NoiseGeneratorSettings(
		StructureSettings structureSettings,
		NoiseSettings noiseSettings,
		BlockState blockState,
		BlockState blockState2,
		int i,
		int j,
		int k,
		boolean bl,
		Optional<ResourceLocation> optional
	) {
		this.structureSettings = structureSettings;
		this.noiseSettings = noiseSettings;
		this.defaultBlock = blockState;
		this.defaultFluid = blockState2;
		this.bedrockRoofPosition = i;
		this.bedrockFloorPosition = j;
		this.seaLevel = k;
		this.disableMobGeneration = bl;
		this.preset = optional;
	}

	public StructureSettings structureSettings() {
		return this.structureSettings;
	}

	public NoiseSettings noiseSettings() {
		return this.noiseSettings;
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

	public boolean stable(NoiseGeneratorSettings noiseGeneratorSettings) {
		return Objects.equals(this.preset, noiseGeneratorSettings.preset);
	}

	private static NoiseGeneratorSettings register(NoiseGeneratorSettings noiseGeneratorSettings) {
		BuiltinRegistries.register(
			BuiltinRegistries.NOISE_GENERATOR_SETTINGS, (ResourceLocation)noiseGeneratorSettings.preset.orElseThrow(IllegalStateException::new), noiseGeneratorSettings
		);
		return noiseGeneratorSettings;
	}

	private static NoiseGeneratorSettings end(
		StructureSettings structureSettings, BlockState blockState, BlockState blockState2, ResourceLocation resourceLocation, boolean bl, boolean bl2
	) {
		return new NoiseGeneratorSettings(
			structureSettings,
			new NoiseSettings(
				128,
				new NoiseSamplingSettings(2.0, 1.0, 80.0, 160.0),
				new NoiseSlideSettings(-3000, 64, -46),
				new NoiseSlideSettings(-30, 7, 1),
				2,
				1,
				0.0,
				0.0,
				true,
				false,
				bl2,
				false
			),
			blockState,
			blockState2,
			-10,
			-10,
			0,
			bl,
			Optional.of(resourceLocation)
		);
	}

	private static NoiseGeneratorSettings nether(
		StructureSettings structureSettings, BlockState blockState, BlockState blockState2, ResourceLocation resourceLocation
	) {
		Map<StructureFeature<?>, StructureFeatureConfiguration> map = Maps.<StructureFeature<?>, StructureFeatureConfiguration>newHashMap(StructureSettings.DEFAULTS);
		map.put(StructureFeature.RUINED_PORTAL, new StructureFeatureConfiguration(25, 10, 34222645));
		return new NoiseGeneratorSettings(
			new StructureSettings(Optional.ofNullable(structureSettings.stronghold()), map),
			new NoiseSettings(
				128,
				new NoiseSamplingSettings(1.0, 3.0, 80.0, 60.0),
				new NoiseSlideSettings(120, 3, 0),
				new NoiseSlideSettings(320, 4, -1),
				1,
				2,
				0.0,
				0.019921875,
				false,
				false,
				false,
				false
			),
			blockState,
			blockState2,
			0,
			0,
			32,
			false,
			Optional.of(resourceLocation)
		);
	}

	private static NoiseGeneratorSettings overworld(StructureSettings structureSettings, boolean bl, ResourceLocation resourceLocation) {
		double d = 0.9999999814507745;
		return new NoiseGeneratorSettings(
			structureSettings,
			new NoiseSettings(
				256,
				new NoiseSamplingSettings(0.9999999814507745, 0.9999999814507745, 80.0, 160.0),
				new NoiseSlideSettings(-10, 3, 0),
				new NoiseSlideSettings(-30, 0, 0),
				1,
				2,
				1.0,
				-0.46875,
				true,
				true,
				false,
				bl
			),
			Blocks.STONE.defaultBlockState(),
			Blocks.WATER.defaultBlockState(),
			-10,
			0,
			63,
			false,
			Optional.of(resourceLocation)
		);
	}
}
