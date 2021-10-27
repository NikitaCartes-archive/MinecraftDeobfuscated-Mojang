/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.levelgen.NoiseSampler;
import net.minecraft.world.level.levelgen.TerrainInfo;
import net.minecraft.world.level.levelgen.blending.Blender;

public class MultiNoiseBiomeSource
extends BiomeSource {
    public static final MapCodec<MultiNoiseBiomeSource> DIRECT_CODEC = RecordCodecBuilder.mapCodec(instance2 -> instance2.group(((MapCodec)RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Climate.ParameterPoint.CODEC.fieldOf("parameters")).forGetter(Pair::getFirst), ((MapCodec)Biome.CODEC.fieldOf("biome")).forGetter(Pair::getSecond)).apply((Applicative<Pair, ?>)instance, Pair::of)).listOf().xmap(Climate.ParameterList::new, Climate.ParameterList::biomes).fieldOf("biomes")).forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.parameters)).apply((Applicative<MultiNoiseBiomeSource, ?>)instance2, MultiNoiseBiomeSource::new));
    public static final Codec<MultiNoiseBiomeSource> CODEC = Codec.mapEither(PresetInstance.CODEC, DIRECT_CODEC).xmap(either -> either.map(PresetInstance::biomeSource, Function.identity()), multiNoiseBiomeSource -> multiNoiseBiomeSource.preset().map(Either::left).orElseGet(() -> Either.right(multiNoiseBiomeSource))).codec();
    private final Climate.ParameterList<Biome> parameters;
    private final Optional<Pair<Registry<Biome>, Preset>> preset;

    private MultiNoiseBiomeSource(Climate.ParameterList<Biome> parameterList) {
        this(parameterList, Optional.empty());
    }

    MultiNoiseBiomeSource(Climate.ParameterList<Biome> parameterList, Optional<Pair<Registry<Biome>, Preset>> optional) {
        super(parameterList.biomes().stream().map(Pair::getSecond));
        this.preset = optional;
        this.parameters = parameterList;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public BiomeSource withSeed(long l) {
        return this;
    }

    private Optional<PresetInstance> preset() {
        return this.preset.map(pair -> new PresetInstance((Preset)pair.getSecond(), (Registry)pair.getFirst()));
    }

    public boolean stable(Preset preset) {
        return this.preset.isPresent() && Objects.equals(this.preset.get().getSecond(), preset);
    }

    @Override
    public Biome getNoiseBiome(int i, int j, int k, Climate.Sampler sampler) {
        return this.getNoiseBiome(sampler.sample(i, j, k));
    }

    @VisibleForDebug
    public Biome getNoiseBiome(Climate.TargetPoint targetPoint) {
        return this.parameters.findBiome(targetPoint, () -> net.minecraft.data.worldgen.biome.Biomes.THE_VOID);
    }

    @Override
    public void addMultinoiseDebugInfo(List<String> list, BlockPos blockPos, Climate.Sampler sampler) {
        int i = QuartPos.fromBlock(blockPos.getX());
        int j = QuartPos.fromBlock(blockPos.getY());
        int k = QuartPos.fromBlock(blockPos.getZ());
        Climate.TargetPoint targetPoint = sampler.sample(i, j, k);
        float f = Climate.unquantizeCoord(targetPoint.continentalness());
        float g = Climate.unquantizeCoord(targetPoint.erosion());
        float h = Climate.unquantizeCoord(targetPoint.temperature());
        float l = Climate.unquantizeCoord(targetPoint.humidity());
        float m = Climate.unquantizeCoord(targetPoint.weirdness());
        double d = TerrainShaper.peaksAndValleys(m);
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        list.add("Multinoise C: " + decimalFormat.format(f) + " E: " + decimalFormat.format(g) + " T: " + decimalFormat.format(h) + " H: " + decimalFormat.format(l) + " W: " + decimalFormat.format(m));
        OverworldBiomeBuilder overworldBiomeBuilder = new OverworldBiomeBuilder();
        list.add("Biome builder PV: " + OverworldBiomeBuilder.getDebugStringForPeaksAndValleys(d) + " C: " + overworldBiomeBuilder.getDebugStringForContinentalness(f) + " E: " + overworldBiomeBuilder.getDebugStringForErosion(g) + " T: " + overworldBiomeBuilder.getDebugStringForTemperature(h) + " H: " + overworldBiomeBuilder.getDebugStringForHumidity(l));
        if (!(sampler instanceof NoiseSampler)) {
            return;
        }
        NoiseSampler noiseSampler = (NoiseSampler)sampler;
        TerrainInfo terrainInfo = noiseSampler.terrainInfo(blockPos.getX(), blockPos.getZ(), f, m, g, Blender.empty());
        list.add("Terrain PV: " + decimalFormat.format(d) + " O: " + decimalFormat.format(terrainInfo.offset()) + " F: " + decimalFormat.format(terrainInfo.factor()) + " JA: " + decimalFormat.format(terrainInfo.jaggedness()));
    }

    static final class PresetInstance {
        public static final MapCodec<PresetInstance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)ResourceLocation.CODEC.flatXmap(resourceLocation -> Optional.ofNullable(Preset.BY_NAME.get(resourceLocation)).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown preset: " + resourceLocation)), preset -> DataResult.success(preset.name)).fieldOf("preset")).stable().forGetter(PresetInstance::preset), RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(PresetInstance::biomes)).apply((Applicative<PresetInstance, ?>)instance, instance.stable(PresetInstance::new)));
        private final Preset preset;
        private final Registry<Biome> biomes;

        PresetInstance(Preset preset, Registry<Biome> registry) {
            this.preset = preset;
            this.biomes = registry;
        }

        public Preset preset() {
            return this.preset;
        }

        public Registry<Biome> biomes() {
            return this.biomes;
        }

        public MultiNoiseBiomeSource biomeSource() {
            return this.preset.biomeSource(this.biomes);
        }
    }

    public static class Preset {
        static final Map<ResourceLocation, Preset> BY_NAME = Maps.newHashMap();
        public static final Preset NETHER = new Preset(new ResourceLocation("nether"), (preset, registry) -> new MultiNoiseBiomeSource(new Climate.ParameterList<Biome>(ImmutableList.of(Pair.of(Climate.parameters(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), () -> registry.getOrThrow(Biomes.NETHER_WASTES)), Pair.of(Climate.parameters(0.0f, -0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), () -> registry.getOrThrow(Biomes.SOUL_SAND_VALLEY)), Pair.of(Climate.parameters(0.4f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), () -> registry.getOrThrow(Biomes.CRIMSON_FOREST)), Pair.of(Climate.parameters(0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.375f), () -> registry.getOrThrow(Biomes.WARPED_FOREST)), Pair.of(Climate.parameters(-0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.175f), () -> registry.getOrThrow(Biomes.BASALT_DELTAS)))), Optional.of(Pair.of(registry, preset))));
        public static final Preset OVERWORLD = new Preset(new ResourceLocation("overworld"), (preset, registry) -> {
            ImmutableList.Builder builder = ImmutableList.builder();
            new OverworldBiomeBuilder().addBiomes(pair -> builder.add(pair.mapSecond(resourceKey -> () -> (Biome)registry.getOrThrow(resourceKey))));
            return new MultiNoiseBiomeSource(new Climate.ParameterList<Biome>((List<Pair<Climate.ParameterPoint, Supplier<Biome>>>)((Object)builder.build())), Optional.of(Pair.of(registry, preset)));
        });
        final ResourceLocation name;
        private final BiFunction<Preset, Registry<Biome>, MultiNoiseBiomeSource> biomeSource;

        public Preset(ResourceLocation resourceLocation, BiFunction<Preset, Registry<Biome>, MultiNoiseBiomeSource> biFunction) {
            this.name = resourceLocation;
            this.biomeSource = biFunction;
            BY_NAME.put(resourceLocation, this);
        }

        public MultiNoiseBiomeSource biomeSource(Registry<Biome> registry) {
            return this.biomeSource.apply(this, registry);
        }
    }
}

