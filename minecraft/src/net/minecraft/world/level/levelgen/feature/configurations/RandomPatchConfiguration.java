package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public record RandomPatchConfiguration() implements FeatureConfiguration {
	private final int tries;
	private final int xzSpread;
	private final int ySpread;
	private final Supplier<PlacedFeature> feature;
	public static final Codec<RandomPatchConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(128).forGetter(RandomPatchConfiguration::tries),
					ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xz_spread").orElse(7).forGetter(RandomPatchConfiguration::xzSpread),
					ExtraCodecs.NON_NEGATIVE_INT.fieldOf("y_spread").orElse(3).forGetter(RandomPatchConfiguration::ySpread),
					PlacedFeature.CODEC.fieldOf("feature").forGetter(RandomPatchConfiguration::feature)
				)
				.apply(instance, RandomPatchConfiguration::new)
	);

	public RandomPatchConfiguration(int i, int j, int k, Supplier<PlacedFeature> supplier) {
		this.tries = i;
		this.xzSpread = j;
		this.ySpread = k;
		this.feature = supplier;
	}
}
