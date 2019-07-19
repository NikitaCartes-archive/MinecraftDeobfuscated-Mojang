package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DiskConfiguration implements FeatureConfiguration {
	public final BlockState state;
	public final int radius;
	public final int ySize;
	public final List<BlockState> targets;

	public DiskConfiguration(BlockState blockState, int i, int j, List<BlockState> list) {
		this.state = blockState;
		this.radius = i;
		this.ySize = j;
		this.targets = list;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("state"),
					BlockState.serialize(dynamicOps, this.state).getValue(),
					dynamicOps.createString("radius"),
					dynamicOps.createInt(this.radius),
					dynamicOps.createString("y_size"),
					dynamicOps.createInt(this.ySize),
					dynamicOps.createString("targets"),
					dynamicOps.createList(this.targets.stream().map(blockState -> BlockState.serialize(dynamicOps, blockState).getValue()))
				)
			)
		);
	}

	public static <T> DiskConfiguration deserialize(Dynamic<T> dynamic) {
		BlockState blockState = (BlockState)dynamic.get("state").map(BlockState::deserialize).orElse(Blocks.AIR.defaultBlockState());
		int i = dynamic.get("radius").asInt(0);
		int j = dynamic.get("y_size").asInt(0);
		List<BlockState> list = dynamic.get("targets").asList(BlockState::deserialize);
		return new DiskConfiguration(blockState, i, j, list);
	}
}
