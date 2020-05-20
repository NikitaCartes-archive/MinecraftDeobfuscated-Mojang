package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;

public class BlockBlobConfiguration implements FeatureConfiguration {
	public static final Codec<BlockBlobConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockState.CODEC.fieldOf("state").forGetter(blockBlobConfiguration -> blockBlobConfiguration.state),
					Codec.INT.fieldOf("start_radius").withDefault(0).forGetter(blockBlobConfiguration -> blockBlobConfiguration.startRadius)
				)
				.apply(instance, BlockBlobConfiguration::new)
	);
	public final BlockState state;
	public final int startRadius;

	public BlockBlobConfiguration(BlockState blockState, int i) {
		this.state = blockState;
		this.startRadius = i;
	}
}
