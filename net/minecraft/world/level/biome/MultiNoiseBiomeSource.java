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
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.levelgen.NoiseRouterData;

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
        double d = NoiseRouterData.peaksAndValleys(m);
        OverworldBiomeBuilder overworldBiomeBuilder = new OverworldBiomeBuilder();
        list.add("Biome builder PV: " + OverworldBiomeBuilder.getDebugStringForPeaksAndValleys(d) + " C: " + overworldBiomeBuilder.getDebugStringForContinentalness(f) + " E: " + overworldBiomeBuilder.getDebugStringForErosion(g) + " T: " + overworldBiomeBuilder.getDebugStringForTemperature(h) + " H: " + overworldBiomeBuilder.getDebugStringForHumidity(l));
    }

    record PresetInstance(Preset preset, HolderGetter<Biome> biomes) {
        public static final MapCodec<PresetInstance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)ResourceLocation.CODEC.flatXmap(resourceLocation -> Optional.ofNullable(Preset.BY_NAME.get(resourceLocation)).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown preset: " + resourceLocation)), preset -> DataResult.success(preset.name)).fieldOf("preset")).stable().forGetter(PresetInstance::preset), RegistryOps.retrieveGetter(Registries.BIOME)).apply((Applicative<PresetInstance, ?>)instance, instance.stable(PresetInstance::new)));

        public MultiNoiseBiomeSource biomeSource() {
            return this.preset.biomeSource(this, true);
        }
    }

    public static class Preset {
        static final Map<ResourceLocation, Preset> BY_NAME = Maps.newHashMap();
        public static final Preset NETHER = new Preset(new ResourceLocation("nether"), new SourceProvider(){

            @Override
            public <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> function) {
                return new Climate.ParameterList<T>(List.of(Pair.of(Climate.parameters(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), function.apply(Biomes.NETHER_WASTES)), Pair.of(Climate.parameters(0.0f, -0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), function.apply(Biomes.SOUL_SAND_VALLEY)), Pair.of(Climate.parameters(0.4f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), function.apply(Biomes.CRIMSON_FOREST)), Pair.of(Climate.parameters(0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.375f), function.apply(Biomes.WARPED_FOREST)), Pair.of(Climate.parameters(-0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.175f), function.apply(Biomes.BASALT_DELTAS))));
            }
        });
        public static final Preset OVERWORLD = new Preset(new ResourceLocation("overworld"), new SourceProvider(){

            @Override
            public <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> function) {
                return Preset.generateOverworldBiomes(function, OverworldBiomeBuilder.Modifier.NONE);
            }
        });
        public static final Preset OVERWORLD_UPDATE_1_20 = new Preset(new ResourceLocation("overworld_update_1_20"), new SourceProvider(){

            @Override
            public <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> function) {
                return Preset.generateOverworldBiomes(function, OverworldBiomeBuilder.Modifier.UPDATE_1_20);
            }
        });
        final ResourceLocation name;
        private final SourceProvider parameterSource;

        public Preset(ResourceLocation resourceLocation, SourceProvider sourceProvider) {
            this.name = resourceLocation;
            this.parameterSource = sourceProvider;
            BY_NAME.put(resourceLocation, this);
        }

        @VisibleForDebug
        public static Stream<Pair<ResourceLocation, Preset>> getPresets() {
            return BY_NAME.entrySet().stream().map(entry -> Pair.of((ResourceLocation)entry.getKey(), (Preset)entry.getValue()));
        }

        static <T> Climate.ParameterList<T> generateOverworldBiomes(Function<ResourceKey<Biome>, T> function, OverworldBiomeBuilder.Modifier modifier) {
            ImmutableList.Builder builder = ImmutableList.builder();
            new OverworldBiomeBuilder(modifier).addBiomes(pair -> builder.add(pair.mapSecond(function)));
            return new Climate.ParameterList(builder.build());
        }

        MultiNoiseBiomeSource biomeSource(PresetInstance presetInstance, boolean bl) {
            Climate.ParameterList<Holder<Biome>> parameterList = this.parameterSource.apply(resourceKey -> presetInstance.biomes().getOrThrow((ResourceKey<Biome>)resourceKey));
            return new MultiNoiseBiomeSource(parameterList, bl ? Optional.of(presetInstance) : Optional.empty());
        }

        public MultiNoiseBiomeSource biomeSource(HolderGetter<Biome> holderGetter, boolean bl) {
            return this.biomeSource(new PresetInstance(this, holderGetter), bl);
        }

        public MultiNoiseBiomeSource biomeSource(HolderGetter<Biome> holderGetter) {
            return this.biomeSource(holderGetter, true);
        }

        public Stream<ResourceKey<Biome>> possibleBiomes() {
            return this.parameterSource.apply(resourceKey -> resourceKey).values().stream().map(Pair::getSecond).distinct();
        }

        @FunctionalInterface
        public static interface SourceProvider {
            public <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> var1);
        }
    }
}

