package net.minecraft.world.level.levelgen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FeatureCountTracker {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final LoadingCache<ServerLevel, FeatureCountTracker.LevelData> data = CacheBuilder.newBuilder()
		.weakKeys()
		.expireAfterAccess(5L, TimeUnit.MINUTES)
		.build(new CacheLoader<ServerLevel, FeatureCountTracker.LevelData>() {
			public FeatureCountTracker.LevelData load(ServerLevel serverLevel) {
				return new FeatureCountTracker.LevelData(Object2IntMaps.synchronize(new Object2IntOpenHashMap<>()), new MutableInt(0));
			}
		});

	public static void chunkDecorated(ServerLevel serverLevel) {
		try {
			data.get(serverLevel).chunksWithFeatures().increment();
		} catch (Exception var2) {
			LOGGER.error(var2);
		}
	}

	public static void featurePlaced(ServerLevel serverLevel, ConfiguredFeature<?, ?> configuredFeature, Optional<PlacedFeature> optional) {
		try {
			data.get(serverLevel)
				.featureData()
				.computeInt(new FeatureCountTracker.FeatureData(configuredFeature, optional), (featureData, integer) -> integer == null ? 1 : integer + 1);
		} catch (Exception var4) {
			LOGGER.error(var4);
		}
	}

	public static void clearCounts() {
		data.invalidateAll();
		LOGGER.debug("Cleared feature counts");
	}

	public static void logCounts() {
		LOGGER.debug("Logging feature counts:");
		data.asMap()
			.forEach(
				(serverLevel, levelData) -> {
					String string = serverLevel.dimension().location().toString();
					boolean bl = serverLevel.getServer().isRunning();
					Registry<PlacedFeature> registry = serverLevel.registryAccess().registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
					String string2 = (bl ? "running" : "dead") + " " + string;
					Integer integer = levelData.chunksWithFeatures().getValue();
					LOGGER.debug(string2 + " total_chunks: " + integer);
					levelData.featureData()
						.forEach(
							(featureData, integer2) -> LOGGER.debug(
									string2
										+ " "
										+ String.format("%10d ", integer2)
										+ String.format("%10f ", (double)integer2.intValue() / (double)integer.intValue())
										+ featureData.topFeature().flatMap(registry::getResourceKey).map(ResourceKey::location)
										+ " "
										+ featureData.feature().feature()
										+ " "
										+ featureData.feature()
								)
						);
				}
			);
	}

	static record FeatureData() {
		private final ConfiguredFeature<?, ?> feature;
		private final Optional<PlacedFeature> topFeature;

		FeatureData(ConfiguredFeature<?, ?> configuredFeature, Optional<PlacedFeature> optional) {
			this.feature = configuredFeature;
			this.topFeature = optional;
		}
	}

	static record LevelData() {
		private final Object2IntMap<FeatureCountTracker.FeatureData> featureData;
		private final MutableInt chunksWithFeatures;

		LevelData(Object2IntMap<FeatureCountTracker.FeatureData> object2IntMap, MutableInt mutableInt) {
			this.featureData = object2IntMap;
			this.chunksWithFeatures = mutableInt;
		}
	}
}
