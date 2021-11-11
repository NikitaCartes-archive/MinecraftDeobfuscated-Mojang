package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class WeightedPlacedFeature {
	public static final Codec<WeightedPlacedFeature> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					PlacedFeature.CODEC
						.fieldOf("feature")
						.flatXmap(ExtraCodecs.nonNullSupplierCheck(), ExtraCodecs.nonNullSupplierCheck())
						.forGetter(weightedPlacedFeature -> weightedPlacedFeature.feature),
					Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(weightedPlacedFeature -> weightedPlacedFeature.chance)
				)
				.apply(instance, WeightedPlacedFeature::new)
	);
	public final Supplier<PlacedFeature> feature;
	public final float chance;

	public WeightedPlacedFeature(PlacedFeature placedFeature, float f) {
		this(() -> placedFeature, f);
	}

	private WeightedPlacedFeature(Supplier<PlacedFeature> supplier, float f) {
		this.feature = supplier;
		this.chance = f;
	}

	public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos) {
		return ((PlacedFeature)this.feature.get()).place(worldGenLevel, chunkGenerator, random, blockPos);
	}
}
