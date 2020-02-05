package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class HugeFungiConfiguration implements FeatureConfiguration {
	public final BlockState stemState;
	public final BlockState hatState;
	public final BlockState decorState;
	public final boolean planted;

	public HugeFungiConfiguration(BlockState blockState, BlockState blockState2, BlockState blockState3, boolean bl) {
		this.stemState = blockState;
		this.hatState = blockState2;
		this.decorState = blockState3;
		this.planted = bl;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("stem_state"),
					BlockState.serialize(dynamicOps, this.stemState).getValue(),
					dynamicOps.createString("hat_state"),
					BlockState.serialize(dynamicOps, this.hatState).getValue(),
					dynamicOps.createString("decor_state"),
					BlockState.serialize(dynamicOps, this.decorState).getValue(),
					dynamicOps.createString("planted"),
					dynamicOps.createBoolean(this.planted)
				)
			)
		);
	}

	public static <T> HugeFungiConfiguration deserialize(Dynamic<T> dynamic) {
		BlockState blockState = (BlockState)dynamic.get("stem_state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		BlockState blockState2 = (BlockState)dynamic.get("hat_state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		BlockState blockState3 = (BlockState)dynamic.get("decor_state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		boolean bl = dynamic.get("planted").asBoolean(false);
		return new HugeFungiConfiguration(blockState, blockState2, blockState3, bl);
	}
}
