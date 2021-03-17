package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;

public class LayerConfiguration implements FeatureConfiguration {
	public static final Codec<LayerConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.intRange(0, DimensionType.Y_SIZE).fieldOf("height").forGetter(layerConfiguration -> layerConfiguration.height),
					BlockState.CODEC.fieldOf("state").forGetter(layerConfiguration -> layerConfiguration.state)
				)
				.apply(instance, LayerConfiguration::new)
	);
	public final int height;
	public final BlockState state;

	public LayerConfiguration(int i, BlockState blockState) {
		this.height = i;
		this.state = blockState;
	}
}
