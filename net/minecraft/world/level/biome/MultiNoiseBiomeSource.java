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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.biome.TerrainShaper;

public class MultiNoiseBiomeSource
extends BiomeSource {
    public static final MapCodec<MultiNoiseBiomeSource> DIRECT_CODEC = RecordCodecBuilder.mapCodec(instance2 -> instance2.group(((MapCodec)ExtraCodecs.nonEmptyList(RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Climate.ParameterPoint.CODEC.fieldOf("parameters")).forGetter(Pair::getFirst), ((MapCodec)Biome.CODEC.fieldOf("biome")).forGetter(Pair::getSecond)).apply((Applicative<Pair, ?>)instance, Pair::of)).listOf()).xmap(Climate.ParameterList::new, Climate.ParameterList::values).fieldOf("biomes")).forGetter(multiNoiseBiomeSource -> multiNoiseBiomeSource.parameters)).apply((Applicative<MultiNoiseBiomeSource, ?>)instance2, MultiNoiseBiomeSource::new));
    public static final Codec<MultiNoiseBiomeSource> CODEC = Codec.mapEither(PresetInstance.CODEC, DIRECT_CODEC).xmap(either -> either.map(PresetInstance::biomeSource, Function.identity()), multiNoiseBiomeSource -> multiNoiseBiomeSource.preset().map(Either::left).orElseGet(() -> Either.right(multiNoiseBiomeSource))).codec();
    private final Climate.ParameterList<Holder<Biome>> parameters;
    private final Optional<PresetInstance> preset;

    private MultiNoiseBiomeSource(Climate.ParameterList<Holder<Biome>> parameterList) {
        this(parameterList, Optional.empty());
    }

    MultiNoiseBiomeSource(Climate.ParameterList<Holder<Biome>> parameterList, Optional<PresetInstance> optional) {
        super(parameterList.values().stream().map(Pair::getSecond));
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
        return this.preset;
    }

    public boolean stable(Preset preset) {
        return this.preset.isPresent() && Objects.equals(this.preset.get().preset(), preset);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int i, int j, int k, Climate.Sampler sampler) {
        return this.getNoiseBiome(sampler.sample(i, j, k));
    }

    @VisibleForDebug
    public Holder<Biome> getNoiseBiome(Climate.TargetPoint targetPoint) {
        return this.parameters.findValue(targetPoint);
    }

    @Override
    public void addDebugInfo(List<String> list, BlockPos blockPos, Climate.Sampler sampler) {
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
        OverworldBiomeBuilder overworldBiomeBuilder = new OverworldBiomeBuilder();
        list.add("Biome builder PV: " + OverworldBiomeBuilder.getDebugStringForPeaksAndValleys(d) + " C: " + overworldBiomeBuilder.getDebugStringForContinentalness(f) + " E: " + overworldBiomeBuilder.getDebugStringForErosion(g) + " T: " + overworldBiomeBuilder.getDebugStringForTemperature(h) + " H: " + overworldBiomeBuilder.getDebugStringForHumidity(l));
    }

    record PresetInstance(Preset preset, Registry<Biome> biomes) {
        public static final MapCodec<PresetInstance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)ResourceLocation.CODEC.flatXmap(resourceLocation -> Optional.ofNullable(Preset.BY_NAME.get(resourceLocation)).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown preset: " + resourceLocation)), preset -> DataResult.success(preset.name)).fieldOf("preset")).stable().forGetter(PresetInstance::preset), RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(PresetInstance::biomes)).apply((Applicative<PresetInstance, ?>)instance, instance.stable(PresetInstance::new)));

        public MultiNoiseBiomeSource biomeSource() {
            return this.preset.biomeSource(this, true);
        }
    }

    public static class Preset {
        static final Map<ResourceLocation, Preset> BY_NAME = Maps.newHashMap();
        public static final Preset NETHER = new Preset(new ResourceLocation("nether"), registry -> new Climate.ParameterList(ImmutableList.of(Pair.of(Climate.parameters(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), registry.getOrCreateHolder(Biomes.NETHER_WASTES)), Pair.of(Climate.parameters(0.0f, -0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), registry.getOrCreateHolder(Biomes.SOUL_SAND_VALLEY)), Pair.of(Climate.parameters(0.4f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), registry.getOrCreateHolder(Biomes.CRIMSON_FOREST)), Pair.of(Climate.parameters(0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.375f), registry.getOrCreateHolder(Biomes.WARPED_FOREST)), Pair.of(Climate.parameters(-0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.175f), registry.getOrCreateHolder(Biomes.BASALT_DELTAS)))));
        public static final Preset OVERWORLD = new Preset(new ResourceLocation("overworld"), registry -> {
            ImmutableList.Builder builder = ImmutableList.builder();
            new OverworldBiomeBuilder().addBiomes(pair -> builder.add(pair.mapSecond(registry::getOrCreateHolder)));
            return new Climate.ParameterList(builder.build());
        });
        final ResourceLocation name;
        private final Function<Registry<Biome>, Climate.ParameterList<Holder<Biome>>> parameterSource;

        public Preset(ResourceLocation resourceLocation, Function<Registry<Biome>, Climate.ParameterList<Holder<Biome>>> function) {
            this.name = resourceLocation;
            this.parameterSource = function;
            BY_NAME.put(resourceLocation, this);
        }

        MultiNoiseBiomeSource biomeSource(PresetInstance presetInstance, boolean bl) {
            Climate.ParameterList<Holder<Biome>> parameterList = this.parameterSource.apply(presetInstance.biomes());
            return new MultiNoiseBiomeSource(parameterList, bl ? Optional.of(presetInstance) : Optional.empty());
        }

        public MultiNoiseBiomeSource biomeSource(Registry<Biome> registry, boolean bl) {
            return this.biomeSource(new PresetInstance(this, registry), bl);
        }

        public MultiNoiseBiomeSource biomeSource(Registry<Biome> registry) {
            return this.biomeSource(registry, true);
        }
    }
}

