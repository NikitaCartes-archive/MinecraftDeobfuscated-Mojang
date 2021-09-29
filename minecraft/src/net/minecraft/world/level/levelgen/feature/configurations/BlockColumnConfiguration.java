package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record BlockColumnConfiguration() implements FeatureConfiguration {
	private final List<BlockColumnConfiguration.Layer> layers;
	private final Direction direction;
	private final boolean allowWater;
	private final boolean prioritizeTip;
	public static final Codec<BlockColumnConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockColumnConfiguration.Layer.CODEC.listOf().fieldOf("layers").forGetter(BlockColumnConfiguration::layers),
					Direction.CODEC.fieldOf("direction").forGetter(BlockColumnConfiguration::direction),
					Codec.BOOL.fieldOf("allow_water").forGetter(BlockColumnConfiguration::allowWater),
					Codec.BOOL.fieldOf("prioritize_tip").forGetter(BlockColumnConfiguration::prioritizeTip)
				)
				.apply(instance, BlockColumnConfiguration::new)
	);

	public BlockColumnConfiguration(List<BlockColumnConfiguration.Layer> list, Direction direction, boolean bl, boolean bl2) {
		this.layers = list;
		this.direction = direction;
		this.allowWater = bl;
		this.prioritizeTip = bl2;
	}

	public static BlockColumnConfiguration.Layer layer(IntProvider intProvider, BlockStateProvider blockStateProvider) {
		return new BlockColumnConfiguration.Layer(intProvider, blockStateProvider);
	}

	public static BlockColumnConfiguration simple(IntProvider intProvider, BlockStateProvider blockStateProvider) {
		return new BlockColumnConfiguration(List.of(layer(intProvider, blockStateProvider)), Direction.UP, false, false);
	}

	public static record Layer() {
		private final IntProvider height;
		private final BlockStateProvider state;
		public static final Codec<BlockColumnConfiguration.Layer> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						IntProvider.NON_NEGATIVE_CODEC.fieldOf("height").forGetter(BlockColumnConfiguration.Layer::height),
						BlockStateProvider.CODEC.fieldOf("provider").forGetter(BlockColumnConfiguration.Layer::state)
					)
					.apply(instance, BlockColumnConfiguration.Layer::new)
		);

		public Layer(IntProvider intProvider, BlockStateProvider blockStateProvider) {
			this.height = intProvider;
			this.state = blockStateProvider;
		}
	}
}
