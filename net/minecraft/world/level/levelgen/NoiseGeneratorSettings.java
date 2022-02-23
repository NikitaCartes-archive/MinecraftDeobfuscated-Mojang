/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.NoiseRouterWithOnlyNoises;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public record NoiseGeneratorSettings(NoiseSettings noiseSettings, BlockState defaultBlock, BlockState defaultFluid, NoiseRouterWithOnlyNoises noiseRouter, SurfaceRules.RuleSource surfaceRule, int seaLevel, boolean disableMobGeneration, boolean aquifersEnabled, boolean oreVeinsEnabled, boolean useLegacyRandomSource) {
    private final boolean oreVeinsEnabled;
    public static final Codec<NoiseGeneratorSettings> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)NoiseSettings.CODEC.fieldOf("noise")).forGetter(NoiseGeneratorSettings::noiseSettings), ((MapCodec)BlockState.CODEC.fieldOf("default_block")).forGetter(NoiseGeneratorSettings::defaultBlock), ((MapCodec)BlockState.CODEC.fieldOf("default_fluid")).forGetter(NoiseGeneratorSettings::defaultFluid), ((MapCodec)NoiseRouterWithOnlyNoises.CODEC.fieldOf("noise_router")).forGetter(NoiseGeneratorSettings::noiseRouter), ((MapCodec)SurfaceRules.RuleSource.CODEC.fieldOf("surface_rule")).forGetter(NoiseGeneratorSettings::surfaceRule), ((MapCodec)Codec.INT.fieldOf("sea_level")).forGetter(NoiseGeneratorSettings::seaLevel), ((MapCodec)Codec.BOOL.fieldOf("disable_mob_generation")).forGetter(NoiseGeneratorSettings::disableMobGeneration), ((MapCodec)Codec.BOOL.fieldOf("aquifers_enabled")).forGetter(NoiseGeneratorSettings::isAquifersEnabled), ((MapCodec)Codec.BOOL.fieldOf("ore_veins_enabled")).forGetter(NoiseGeneratorSettings::oreVeinsEnabled), ((MapCodec)Codec.BOOL.fieldOf("legacy_random_source")).forGetter(NoiseGeneratorSettings::useLegacyRandomSource)).apply((Applicative<NoiseGeneratorSettings, ?>)instance, NoiseGeneratorSettings::new));
    public static final Codec<Holder<NoiseGeneratorSettings>> CODEC = RegistryFileCodec.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, DIRECT_CODEC);
    public static final ResourceKey<NoiseGeneratorSettings> OVERWORLD = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<NoiseGeneratorSettings> LARGE_BIOMES = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("large_biomes"));
    public static final ResourceKey<NoiseGeneratorSettings> AMPLIFIED = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("amplified"));
    public static final ResourceKey<NoiseGeneratorSettings> NETHER = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("nether"));
    public static final ResourceKey<NoiseGeneratorSettings> END = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("end"));
    public static final ResourceKey<NoiseGeneratorSettings> CAVES = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("caves"));
    public static final ResourceKey<NoiseGeneratorSettings> FLOATING_ISLANDS = ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, new ResourceLocation("floating_islands"));

    @Deprecated
    public boolean disableMobGeneration() {
        return this.disableMobGeneration;
    }

    public boolean isAquifersEnabled() {
        return this.aquifersEnabled;
    }

    public boolean oreVeinsEnabled() {
        return this.oreVeinsEnabled;
    }

    public WorldgenRandom.Algorithm getRandomSource() {
        return this.useLegacyRandomSource ? WorldgenRandom.Algorithm.LEGACY : WorldgenRandom.Algorithm.XOROSHIRO;
    }

    public NoiseRouter createNoiseRouter(Registry<NormalNoise.NoiseParameters> registry, long l) {
        return NoiseRouterData.createNoiseRouter(this.noiseSettings, l, registry, this.getRandomSource(), this.noiseRouter);
    }

    private static void register(ResourceKey<NoiseGeneratorSettings> resourceKey, NoiseGeneratorSettings noiseGeneratorSettings) {
        BuiltinRegistries.register(BuiltinRegistries.NOISE_GENERATOR_SETTINGS, resourceKey.location(), noiseGeneratorSettings);
    }

    public static Holder<NoiseGeneratorSettings> bootstrap() {
        return (Holder)BuiltinRegistries.NOISE_GENERATOR_SETTINGS.holders().iterator().next();
    }

    private static NoiseGeneratorSettings end() {
        return new NoiseGeneratorSettings(NoiseSettings.END_NOISE_SETTINGS, Blocks.END_STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), NoiseRouterData.end(NoiseSettings.END_NOISE_SETTINGS), SurfaceRuleData.end(), 0, true, false, false, true);
    }

    private static NoiseGeneratorSettings nether() {
        return new NoiseGeneratorSettings(NoiseSettings.NETHER_NOISE_SETTINGS, Blocks.NETHERRACK.defaultBlockState(), Blocks.LAVA.defaultBlockState(), NoiseRouterData.nether(NoiseSettings.NETHER_NOISE_SETTINGS), SurfaceRuleData.nether(), 32, false, false, false, true);
    }

    private static NoiseGeneratorSettings overworld(boolean bl, boolean bl2) {
        NoiseSettings noiseSettings = NoiseSettings.overworldNoiseSettings(bl);
        return new NoiseGeneratorSettings(noiseSettings, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), NoiseRouterData.overworld(noiseSettings, bl2), SurfaceRuleData.overworld(), 63, false, true, true, false);
    }

    private static NoiseGeneratorSettings caves() {
        return new NoiseGeneratorSettings(NoiseSettings.CAVES_NOISE_SETTINGS, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), NoiseRouterData.overworldWithoutCaves(NoiseSettings.CAVES_NOISE_SETTINGS), SurfaceRuleData.overworldLike(false, true, true), 32, false, false, false, true);
    }

    private static NoiseGeneratorSettings floatingIslands() {
        return new NoiseGeneratorSettings(NoiseSettings.FLOATING_ISLANDS_NOISE_SETTINGS, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), NoiseRouterData.overworldWithoutCaves(NoiseSettings.FLOATING_ISLANDS_NOISE_SETTINGS), SurfaceRuleData.overworldLike(false, false, false), -64, false, false, false, true);
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

