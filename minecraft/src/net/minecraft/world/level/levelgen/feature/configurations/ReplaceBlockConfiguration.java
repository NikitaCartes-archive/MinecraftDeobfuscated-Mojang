package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ReplaceBlockConfiguration implements FeatureConfiguration {
	public final BlockState target;
	public final BlockState state;

	public ReplaceBlockConfiguration(BlockState blockState, BlockState blockState2) {
		this.target = blockState;
		this.state = blockState2;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("target"),
					BlockState.serialize(dynamicOps, this.target).getValue(),
					dynamicOps.createString("state"),
					BlockState.serialize(dynamicOps, this.state).getValue()
				)
			)
		);
	}

	public static <T> ReplaceBlockConfiguration deserialize(Dynamic<T> dynamic) {
		BlockState blockState = (BlockState)dynamic.get("target").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		BlockState blockState2 = (BlockState)dynamic.get("state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		return new ReplaceBlockConfiguration(blockState, blockState2);
	}
}
