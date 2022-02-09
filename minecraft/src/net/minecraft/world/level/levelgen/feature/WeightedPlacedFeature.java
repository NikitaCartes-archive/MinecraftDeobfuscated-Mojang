package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class WeightedPlacedFeature {
	public static final Codec<WeightedPlacedFeature> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					PlacedFeature.CODEC.fieldOf("feature").forGetter(weightedPlacedFeature -> weightedPlacedFeature.feature),
					Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(weightedPlacedFeature -> weightedPlacedFeature.chance)
				)
				.apply(instance, WeightedPlacedFeature::new)
	);
	public final Holder<PlacedFeature> feature;
	public final float chance;

	public WeightedPlacedFeature(Holder<PlacedFeature> holder, float f) {
		this.feature = holder;
		this.chance = f;
	}

	public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos) {
		return this.feature.value().place(worldGenLevel, chunkGenerator, random, blockPos);
	}
}
