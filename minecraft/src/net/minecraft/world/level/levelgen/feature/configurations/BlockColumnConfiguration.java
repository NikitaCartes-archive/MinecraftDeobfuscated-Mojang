package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record BlockColumnConfiguration(List<BlockColumnConfiguration.Layer> layers, Direction direction, BlockPredicate allowedPlacement, boolean prioritizeTip)
	implements FeatureConfiguration {
	public static final Codec<BlockColumnConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockColumnConfiguration.Layer.CODEC.listOf().fieldOf("layers").forGetter(BlockColumnConfiguration::layers),
					Direction.CODEC.fieldOf("direction").forGetter(BlockColumnConfiguration::direction),
					BlockPredicate.CODEC.fieldOf("allowed_placement").forGetter(BlockColumnConfiguration::allowedPlacement),
					Codec.BOOL.fieldOf("prioritize_tip").forGetter(BlockColumnConfiguration::prioritizeTip)
				)
				.apply(instance, BlockColumnConfiguration::new)
	);

	public static BlockColumnConfiguration.Layer layer(IntProvider intProvider, BlockStateProvider blockStateProvider) {
		return new BlockColumnConfiguration.Layer(intProvider, blockStateProvider);
	}

	public static BlockColumnConfiguration simple(IntProvider intProvider, BlockStateProvider blockStateProvider) {
		return new BlockColumnConfiguration(List.of(layer(intProvider, blockStateProvider)), Direction.UP, BlockPredicate.ONLY_IN_AIR_PREDICATE, false);
	}

	public static record Layer(IntProvider height, BlockStateProvider state) {
		public static final Codec<BlockColumnConfiguration.Layer> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						IntProvider.NON_NEGATIVE_CODEC.fieldOf("height").forGetter(BlockColumnConfiguration.Layer::height),
						BlockStateProvider.CODEC.fieldOf("provider").forGetter(BlockColumnConfiguration.Layer::state)
					)
					.apply(instance, BlockColumnConfiguration.Layer::new)
		);
	}
}
