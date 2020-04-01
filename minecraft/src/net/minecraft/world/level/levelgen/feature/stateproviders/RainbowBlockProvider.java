package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class RainbowBlockProvider extends BlockStateProvider {
	private final List<BlockState> blocks;

	public RainbowBlockProvider(Dynamic<?> dynamic) {
		this((List<BlockState>)dynamic.get("states").asStream().map(BlockState::deserialize).collect(ImmutableList.toImmutableList()));
	}

	public RainbowBlockProvider(List<BlockState> list) {
		super(BlockStateProviderType.RAINBOW_BLOCK_PROVIDER);
		this.blocks = list;
	}

	@Override
	public BlockState getState(Random random, BlockPos blockPos) {
		int i = Math.abs(blockPos.getX() + blockPos.getY() + blockPos.getZ());
		return (BlockState)this.blocks.get(i % this.blocks.size());
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		return dynamicOps.createMap(
			ImmutableMap.of(
				dynamicOps.createString("states"), dynamicOps.createList(this.blocks.stream().map(blockState -> BlockState.serialize(dynamicOps, blockState).getValue()))
			)
		);
	}
}
