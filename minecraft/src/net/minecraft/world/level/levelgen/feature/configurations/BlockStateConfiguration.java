package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStateConfiguration implements FeatureConfiguration {
	public static final Codec<BlockStateConfiguration> CODEC = BlockState.CODEC
		.fieldOf("state")
		.<BlockStateConfiguration>xmap(BlockStateConfiguration::new, blockStateConfiguration -> blockStateConfiguration.state)
		.codec();
	public final BlockState state;

	public BlockStateConfiguration(BlockState blockState) {
		this.state = blockState;
	}
}
