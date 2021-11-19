/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseSamplingSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.NoiseSlider;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

public final class NoiseGeneratorSettings {
    public static final Codec<NoiseGeneratorSettings> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)StructureSettings.CODEC.fieldOf("structures")).forGetter(NoiseGeneratorSettings::structureSettings), ((MapCodec)NoiseSettings.CODEC.fieldOf("noise")).forGetter(NoiseGeneratorSettings::noiseSettings), ((MapCodec)BlockState.CODEC.fieldOf("default_block")).forGetter(NoiseGeneratorSettings::getDefaultBlock), ((MapCodec)BlockState.CODEC.fieldOf("default_fluid")).forGetter(NoiseGeneratorSettings::getDefaultFluid), ((MapCodec)SurfaceRules.RuleSource.CODEC.fieldOf("surface_rule")).forGetter(NoiseGeneratorSettings::surfaceRule), ((MapCodec)Codec.INT.fieldOf("sea_level")).forGetter(NoiseGeneratorSettings::seaLevel), ((MapCodec)Codec.BOOL.fieldOf("disable_mob_generation")).forGetter(NoiseGeneratorSettings::disableMobGeneration), ((MapCodec)Codec.BOOL.fieldOf("aquifers_enabled")).forGetter(NoiseGeneratorSettings::isAquifersEnabled), ((MapCodec)Codec.BOOL.fieldOf("noise_caves_enabled")).forGetter(NoiseGeneratorSettings::isNoiseCavesEnabled), ((MapCodec)Codec.BOOL.fieldOf("ore_veins_enabled")).forGetter(NoiseGeneratorSettings::isOreVeinsEnabled), ((MapCodec)Codec.BOOL.fieldOf("noodle_caves_enabled")).forGetter(NoiseGeneratorSettings::isNoodleCavesEnabled), ((MapCodec)Codec.BOOL.fieldOf("legacy_random_source")).forGetter(NoiseGeneratorSettings::useLegacyRandomSource)).apply((Applicative<NoiseGeneratorSettings, ?>)instance, NoiseGeneratorSettings::new));
    public static final Codec<Supplier<NoiseGeneratorSettings>> CODEC = RegistryFileCodec.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, DIRECT_CODEC);
    private final WorldgenRandom.Algorithm randomSource;
    private final StructureSettings structureSettings;
    private final NoiseSettings noiseSettings;
    private final BlockState defaultBlock;
    private final BlockState defaultFluid;
    private final SurfaceRules.RuleSource surfaceRule;
    private final int seaLevel;
    private final boolean disableMobGeneration;
    private final boolean aquifersEnabled;
    private final boolean noiseCavesEnabled;
    private final boolean oreVeinsEnabled;
    private final boolean noodleCavesEnabled;
    public static final ResourceKey<NoiseGeneratorSettings> OVERWORLD = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<NoiseGeneratorSettings> LARGE_BIOMES = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("large_biomes"));
    public static final ResourceKey<NoiseGeneratorSettings> AMPLIFIED = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("amplified"));
    public static final ResourceKey<NoiseGeneratorSettings> NETHER = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("nether"));
    public static final ResourceKey<NoiseGeneratorSettings> END = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("end"));
    public static final ResourceKey<NoiseGeneratorSettings> CAVES = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("caves"));
    public static final ResourceKey<NoiseGeneratorSettings> FLOATING_ISLANDS = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("floating_islands"));

    private NoiseGeneratorSettings(StructureSettings structureSettings, NoiseSettings noiseSettings, BlockState blockState, BlockState blockState2, SurfaceRules.RuleSource ruleSource, int i, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6) {
        this.structureSettings = structureSettings;
        this.noiseSettings = noiseSettings;
        this.defaultBlock = blockState;
        this.defaultFluid = blockState2;
        this.surfaceRule = ruleSource;
        this.seaLevel = i;
        this.disableMobGeneration = bl;
        this.aquifersEnabled = bl2;
        this.noiseCavesEnabled = bl3;
        this.oreVeinsEnabled = bl4;
        this.noodleCavesEnabled = bl5;
        this.randomSource = bl6 ? WorldgenRandom.Algorithm.LEGACY : WorldgenRandom.Algorithm.XOROSHIRO;
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

    public SurfaceRules.RuleSource surfaceRule() {
        return this.surfaceRule;
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

    public boolean isOreVeinsEnabled() {
        return this.oreVeinsEnabled;
    }

    public boolean isNoodleCavesEnabled() {
        return this.noodleCavesEnabled;
    }

    public boolean useLegacyRandomSource() {
        return this.randomSource == WorldgenRandom.Algorithm.LEGACY;
    }

    public RandomSource createRandomSource(long l) {
        return this.getRandomSource().newInstance(l);
    }

    public WorldgenRandom.Algorithm getRandomSource() {
        return this.randomSource;
    }

    public boolean stable(ResourceKey<NoiseGeneratorSettings> resourceKey) {
        return Objects.equals(this, BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(resourceKey));
    }

    private static void register(ResourceKey<NoiseGeneratorSettings> resourceKey, NoiseGeneratorSettings noiseGeneratorSettings) {
        BuiltinRegistries.register(BuiltinRegistries.NOISE_GENERATOR_SETTINGS, resourceKey.location(), noiseGeneratorSettings);
    }

    public static NoiseGeneratorSettings bootstrap() {
        return (NoiseGeneratorSettings)BuiltinRegistries.NOISE_GENERATOR_SETTINGS.iterator().next();
    }

    private static NoiseGeneratorSettings end() {
        return new NoiseGeneratorSettings(new StructureSettings(false), NoiseSettings.create(0, 128, new NoiseSamplingSettings(2.0, 1.0, 80.0, 160.0), new NoiseSlider(-23.4375, 64, -46), new NoiseSlider(-0.234375, 7, 1), 2, 1, true, false, false, TerrainProvider.end()), Blocks.END_STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), SurfaceRuleData.end(), 0, true, false, false, false, false, true);
    }

    private static NoiseGeneratorSettings nether() {
        HashMap<StructureFeature<?>, StructureFeatureConfiguration> map = Maps.newHashMap(StructureSettings.DEFAULTS);
        map.put(StructureFeature.RUINED_PORTAL, new StructureFeatureConfiguration(25, 10, 34222645));
        return new NoiseGeneratorSettings(new StructureSettings(Optional.empty(), map), NoiseSettings.create(0, 128, new NoiseSamplingSettings(1.0, 3.0, 80.0, 60.0), new NoiseSlider(0.9375, 3, 0), new NoiseSlider(2.5, 4, -1), 1, 2, false, false, false, TerrainProvider.nether()), Blocks.NETHERRACK.defaultBlockState(), Blocks.LAVA.defaultBlockState(), SurfaceRuleData.nether(), 32, false, false, false, false, false, true);
    }

    private static NoiseGeneratorSettings overworld(boolean bl, boolean bl2) {
        return new NoiseGeneratorSettings(new StructureSettings(true), NoiseSettings.create(-64, 384, new NoiseSamplingSettings(1.0, 1.0, 80.0, 160.0), new NoiseSlider(-0.078125, 2, bl ? 0 : 8), new NoiseSlider(0.1171875, 3, 0), 1, 2, false, bl, bl2, TerrainProvider.overworld(bl)), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), SurfaceRuleData.overworld(), 63, false, true, true, true, true, false);
    }

    private static NoiseGeneratorSettings caves() {
        return new NoiseGeneratorSettings(new StructureSettings(false), NoiseSettings.create(-64, 192, new NoiseSamplingSettings(1.0, 3.0, 80.0, 60.0), new NoiseSlider(0.9375, 3, 0), new NoiseSlider(2.5, 4, -1), 1, 2, false, false, false, TerrainProvider.caves()), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), SurfaceRuleData.overworldLike(false, true, true), 32, false, false, false, false, false, true);
    }

    private static NoiseGeneratorSettings floatingIslands() {
        return new NoiseGeneratorSettings(new StructureSettings(true), NoiseSettings.create(0, 256, new NoiseSamplingSettings(2.0, 1.0, 80.0, 160.0), new NoiseSlider(-23.4375, 64, -46), new NoiseSlider(-0.234375, 7, 1), 2, 1, false, false, false, TerrainProvider.floatingIslands()), Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), SurfaceRuleData.overworldLike(false, false, false), -64, false, false, false, false, false, true);
    }

    static {
        NoiseGeneratorSettings.register(OVERWORLD, NoiseGeneratorSettings.overworld(false, false));
        NoiseGeneratorSettings.register(LARGE_BIOMES, NoiseGeneratorSettings.overworld(false, true));
        NoiseGeneratorSettings.register(AMPLIFIED, NoiseGeneratorSettings.overworld(true, false));
        NoiseGeneratorSettings.register(NETHER, NoiseGeneratorSettings.nether());
        NoiseGeneratorSettings.register(END, NoiseGeneratorSettings.end());
        NoiseGeneratorSettings.register(CAVES, NoiseGeneratorSettings.caves());
        NoiseGeneratorSettings.register(FLOATING_ISLANDS, NoiseGeneratorSettings.floatingIslands());
    }
}

