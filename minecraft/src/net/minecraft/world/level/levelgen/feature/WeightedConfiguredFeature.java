package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class WeightedConfiguredFeature {
	public static final Codec<WeightedConfiguredFeature> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ConfiguredFeature.CODEC.fieldOf("feature").forGetter(weightedConfiguredFeature -> weightedConfiguredFeature.feature),
					Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(weightedConfiguredFeature -> weightedConfiguredFeature.chance)
				)
				.apply(instance, WeightedConfiguredFeature::new)
	);
	public final Supplier<ConfiguredFeature<?, ?>> feature;
	public final float chance;

	public WeightedConfiguredFeature(ConfiguredFeature<?, ?> configuredFeature, float f) {
		this(() -> configuredFeature, f);
	}

	private WeightedConfiguredFeature(Supplier<ConfiguredFeature<?, ?>> supplier, float f) {
		this.feature = supplier;
		this.chance = f;
	}

	public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos) {
		return ((ConfiguredFeature)this.feature.get()).place(worldGenLevel, chunkGenerator, random, blockPos);
	}
}
