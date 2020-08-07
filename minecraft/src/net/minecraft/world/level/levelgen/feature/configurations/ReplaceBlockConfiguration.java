package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;

public class ReplaceBlockConfiguration implements FeatureConfiguration {
	public static final Codec<ReplaceBlockConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					BlockState.CODEC.fieldOf("target").forGetter(replaceBlockConfiguration -> replaceBlockConfiguration.target),
					BlockState.CODEC.fieldOf("state").forGetter(replaceBlockConfiguration -> replaceBlockConfiguration.state)
				)
				.apply(instance, ReplaceBlockConfiguration::new)
	);
	public final BlockState target;
	public final BlockState state;

	public ReplaceBlockConfiguration(BlockState blockState, BlockState blockState2) {
		this.target = blockState;
		this.state = blockState2;
	}
}
