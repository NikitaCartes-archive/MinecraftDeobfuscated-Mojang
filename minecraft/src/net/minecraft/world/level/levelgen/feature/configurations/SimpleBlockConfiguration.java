package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record SimpleBlockConfiguration() implements FeatureConfiguration {
	private final BlockStateProvider toPlace;
	public static final Codec<SimpleBlockConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(BlockStateProvider.CODEC.fieldOf("to_place").forGetter(simpleBlockConfiguration -> simpleBlockConfiguration.toPlace))
				.apply(instance, SimpleBlockConfiguration::new)
	);

	public SimpleBlockConfiguration(BlockStateProvider blockStateProvider) {
		this.toPlace = blockStateProvider;
	}
}
