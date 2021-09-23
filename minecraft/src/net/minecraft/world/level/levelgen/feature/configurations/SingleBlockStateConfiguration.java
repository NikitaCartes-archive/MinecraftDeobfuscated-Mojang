package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.state.BlockState;

public record SingleBlockStateConfiguration() implements DecoratorConfiguration {
	private final BlockState state;
	public static final Codec<SingleBlockStateConfiguration> CODEC = BlockState.CODEC
		.fieldOf("state")
		.<SingleBlockStateConfiguration>xmap(SingleBlockStateConfiguration::new, SingleBlockStateConfiguration::state)
		.codec();

	public SingleBlockStateConfiguration(BlockState blockState) {
		this.state = blockState;
	}
}
