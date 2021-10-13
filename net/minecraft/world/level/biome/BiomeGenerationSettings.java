/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiomeGenerationSettings {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final BiomeGenerationSettings EMPTY = new BiomeGenerationSettings(ImmutableMap.of(), ImmutableList.of());
    public static final MapCodec<BiomeGenerationSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.simpleMap(GenerationStep.Carving.CODEC, ConfiguredWorldCarver.LIST_CODEC.promotePartial((Consumer)Util.prefix("Carver: ", LOGGER::error)).flatXmap(ExtraCodecs.nonNullSupplierListCheck(), ExtraCodecs.nonNullSupplierListCheck()), StringRepresentable.keys(GenerationStep.Carving.values())).fieldOf("carvers").forGetter(biomeGenerationSettings -> biomeGenerationSettings.carvers), ((MapCodec)ConfiguredFeature.LIST_CODEC.promotePartial((Consumer)Util.prefix("Feature: ", LOGGER::error)).flatXmap(ExtraCodecs.nonNullSupplierListCheck(), ExtraCodecs.nonNullSupplierListCheck()).listOf().fieldOf("features")).forGetter(biomeGenerationSettings -> biomeGenerationSettings.features)).apply((Applicative<BiomeGenerationSettings, ?>)instance, BiomeGenerationSettings::new));
    private final Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> carvers;
    private final List<List<Supplier<ConfiguredFeature<?, ?>>>> features;
    private final List<ConfiguredFeature<?, ?>> flowerFeatures;
    private final Set<ConfiguredFeature<?, ?>> featureSet;

    BiomeGenerationSettings(Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> map, List<List<Supplier<ConfiguredFeature<?, ?>>>> list) {
        this.carvers = map;
        this.features = list;
        this.flowerFeatures = list.stream().flatMap(Collection::stream).map(Supplier::get).flatMap(ConfiguredFeature::getFeatures).filter(configuredFeature -> configuredFeature.feature == Feature.FLOWER).collect(ImmutableList.toImmutableList());
        this.featureSet = list.stream().flatMap(Collection::stream).map(Supplier::get).collect(Collectors.toSet());
    }

    public List<Supplier<ConfiguredWorldCarver<?>>> getCarvers(GenerationStep.Carving carving) {
        return this.carvers.getOrDefault(carving, ImmutableList.of());
    }

    public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
        return this.flowerFeatures;
    }

    public List<List<Supplier<ConfiguredFeature<?, ?>>>> features() {
        return this.features;
    }

    public boolean hasFeature(ConfiguredFeature<?, ?> configuredFeature) {
        return this.featureSet.contains(configuredFeature);
    }

    public static class Builder {
        private final Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> carvers = Maps.newLinkedHashMap();
        private final List<List<Supplier<ConfiguredFeature<?, ?>>>> features = Lists.newArrayList();

        public Builder addFeature(GenerationStep.Decoration decoration, ConfiguredFeature<?, ?> configuredFeature) {
            return this.addFeature(decoration.ordinal(), () -> configuredFeature);
        }

        public Builder addFeature(int i, Supplier<ConfiguredFeature<?, ?>> supplier) {
            this.addFeatureStepsUpTo(i);
            this.features.get(i).add(supplier);
            return this;
        }

        public <C extends CarverConfiguration> Builder addCarver(GenerationStep.Carving carving2, ConfiguredWorldCarver<C> configuredWorldCarver) {
            this.carvers.computeIfAbsent(carving2, carving -> Lists.newArrayList()).add(() -> configuredWorldCarver);
            return this;
        }

        private void addFeatureStepsUpTo(int i) {
            while (this.features.size() <= i) {
                this.features.add(Lists.newArrayList());
            }
        }

        public BiomeGenerationSettings build() {
            return new BiomeGenerationSettings((Map)this.carvers.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> ImmutableList.copyOf((Collection)entry.getValue()))), this.features.stream().map(ImmutableList::copyOf).collect(ImmutableList.toImmutableList()));
        }
    }
}

