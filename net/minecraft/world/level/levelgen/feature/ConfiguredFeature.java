/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.AbstractFlowerFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfiguredFeature<FC extends FeatureConfiguration, F extends Feature<FC>> {
    public static final Logger LOGGER = LogManager.getLogger();
    public final F feature;
    public final FC config;

    public ConfiguredFeature(F feature, FC featureConfiguration) {
        this.feature = feature;
        this.config = featureConfiguration;
    }

    public ConfiguredFeature(F feature, Dynamic<?> dynamic) {
        this(feature, ((Feature)feature).createSettings(dynamic));
    }

    public ConfiguredFeature<?, ?> decorated(ConfiguredDecorator<?> configuredDecorator) {
        Feature<DecoratedFeatureConfiguration> feature = this.feature instanceof AbstractFlowerFeature ? Feature.DECORATED_FLOWER : Feature.DECORATED;
        return feature.configured(new DecoratedFeatureConfiguration(this, configuredDecorator));
    }

    public WeightedConfiguredFeature<FC> weighted(float f) {
        return new WeightedConfiguredFeature(this, f);
    }

    public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("name"), dynamicOps.createString(Registry.FEATURE.getKey((Feature<?>)this.feature).toString()), dynamicOps.createString("config"), this.config.serialize(dynamicOps).getValue())));
    }

    public boolean place(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos) {
        return ((Feature)this.feature).place(worldGenLevel, structureFeatureManager, chunkGenerator, random, blockPos, this.config);
    }

    public static <T> ConfiguredFeature<?, ?> deserialize(Dynamic<T> dynamic) {
        String string = dynamic.get("name").asString("");
        Feature<?> feature = Registry.FEATURE.get(new ResourceLocation(string));
        try {
            return new ConfiguredFeature(feature, dynamic.get("config").orElseEmptyMap());
        } catch (RuntimeException runtimeException) {
            LOGGER.warn("Error while deserializing {}", (Object)string);
            return new ConfiguredFeature<NoneFeatureConfiguration, Feature<NoneFeatureConfiguration>>(Feature.NO_OP, NoneFeatureConfiguration.NONE);
        }
    }
}

