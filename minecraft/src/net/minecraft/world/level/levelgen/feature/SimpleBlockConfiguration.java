package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleBlockConfiguration implements FeatureConfiguration {
	protected final BlockState toPlace;
	protected final List<BlockState> placeOn;
	protected final List<BlockState> placeIn;
	protected final List<BlockState> placeUnder;

	public SimpleBlockConfiguration(BlockState blockState, List<BlockState> list, List<BlockState> list2, List<BlockState> list3) {
		this.toPlace = blockState;
		this.placeOn = list;
		this.placeIn = list2;
		this.placeUnder = list3;
	}

	public SimpleBlockConfiguration(BlockState blockState, BlockState[] blockStates, BlockState[] blockStates2, BlockState[] blockStates3) {
		this(blockState, Lists.<BlockState>newArrayList(blockStates), Lists.<BlockState>newArrayList(blockStates2), Lists.<BlockState>newArrayList(blockStates3));
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		T object = BlockState.serialize(dynamicOps, this.toPlace).getValue();
		T object2 = dynamicOps.createList(this.placeOn.stream().map(blockState -> BlockState.serialize(dynamicOps, blockState).getValue()));
		T object3 = dynamicOps.createList(this.placeIn.stream().map(blockState -> BlockState.serialize(dynamicOps, blockState).getValue()));
		T object4 = dynamicOps.createList(this.placeUnder.stream().map(blockState -> BlockState.serialize(dynamicOps, blockState).getValue()));
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("to_place"),
					object,
					dynamicOps.createString("place_on"),
					object2,
					dynamicOps.createString("place_in"),
					object3,
					dynamicOps.createString("place_under"),
					object4
				)
			)
		);
	}

	public static <T> SimpleBlockConfiguration deserialize(Dynamic<T> dynamic) {
		BlockState blockState = (BlockState)dynamic.get("to_place").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		List<BlockState> list = dynamic.get("place_on").asList(BlockState::deserialize);
		List<BlockState> list2 = dynamic.get("place_in").asList(BlockState::deserialize);
		List<BlockState> list3 = dynamic.get("place_under").asList(BlockState::deserialize);
		return new SimpleBlockConfiguration(blockState, list, list2, list3);
	}
}
