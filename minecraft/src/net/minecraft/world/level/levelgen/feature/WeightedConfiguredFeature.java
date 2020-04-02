package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class WeightedConfiguredFeature<FC extends FeatureConfiguration> {
	public final ConfiguredFeature<FC, ?> feature;
	public final float chance;

	public WeightedConfiguredFeature(ConfiguredFeature<FC, ?> configuredFeature, float f) {
		this.feature = configuredFeature;
		this.chance = f;
	}

	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("name"),
					dynamicOps.createString(Registry.FEATURE.getKey(this.feature.feature).toString()),
					dynamicOps.createString("config"),
					this.feature.config.serialize(dynamicOps).getValue(),
					dynamicOps.createString("chance"),
					dynamicOps.createFloat(this.chance)
				)
			)
		);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos
	) {
		return this.feature.place(levelAccessor, structureFeatureManager, chunkGenerator, random, blockPos);
	}

	public static <T> WeightedConfiguredFeature<?> deserialize(Dynamic<T> dynamic) {
		return ConfiguredFeature.deserialize(dynamic).weighted(dynamic.get("chance").asFloat(0.0F));
	}
}
