/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

public class FeatureCountTracker {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LoadingCache<ServerLevel, LevelData> data = CacheBuilder.newBuilder().weakKeys().expireAfterAccess(5L, TimeUnit.MINUTES).build(new CacheLoader<ServerLevel, LevelData>(){

        @Override
        public LevelData load(ServerLevel serverLevel) {
            return new LevelData(Object2IntMaps.synchronize(new Object2IntOpenHashMap()), new MutableInt(0));
        }

        @Override
        public /* synthetic */ Object load(Object object) throws Exception {
            return this.load((ServerLevel)object);
        }
    });

    public static void chunkDecorated(ServerLevel serverLevel) {
        try {
            data.get(serverLevel).chunksWithFeatures().increment();
        } catch (Exception exception) {
            LOGGER.error("Failed to increment chunk count", exception);
        }
    }

    public static void featurePlaced(ServerLevel serverLevel, ConfiguredFeature<?, ?> configuredFeature, Optional<PlacedFeature> optional) {
        try {
            data.get(serverLevel).featureData().computeInt(new FeatureData(configuredFeature, optional), (featureData, integer) -> integer == null ? 1 : integer + 1);
        } catch (Exception exception) {
            LOGGER.error("Failed to increment feature count", exception);
        }
    }

    public static void clearCounts() {
        data.invalidateAll();
        LOGGER.debug("Cleared feature counts");
    }

    public static void logCounts() {
        LOGGER.debug("Logging feature counts:");
        data.asMap().forEach((serverLevel, levelData) -> {
            String string = serverLevel.dimension().location().toString();
            boolean bl = serverLevel.getServer().isRunning();
            Registry<PlacedFeature> registry = serverLevel.registryAccess().registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
            String string2 = (bl ? "running" : "dead") + " " + string;
            Integer integer = levelData.chunksWithFeatures().getValue();
            LOGGER.debug(string2 + " total_chunks: " + integer);
            levelData.featureData().forEach((featureData, integer2) -> LOGGER.debug(string2 + " " + String.format(Locale.ROOT, "%10d ", integer2) + String.format(Locale.ROOT, "%10f ", (double)integer2.intValue() / (double)integer.intValue()) + featureData.topFeature().flatMap(registry::getResourceKey).map(ResourceKey::location) + " " + featureData.feature().feature() + " " + featureData.feature()));
        });
    }

    record LevelData(Object2IntMap<FeatureData> featureData, MutableInt chunksWithFeatures) {
    }

    record FeatureData(ConfiguredFeature<?, ?> feature, Optional<PlacedFeature> topFeature) {
    }
}

