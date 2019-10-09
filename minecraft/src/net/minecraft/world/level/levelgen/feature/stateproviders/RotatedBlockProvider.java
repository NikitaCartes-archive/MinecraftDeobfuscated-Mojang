package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RotatedBlockProvider extends BlockStateProvider {
	private final Block block;

	public RotatedBlockProvider(Block block) {
		super(BlockStateProviderType.SIMPLE_STATE_PROVIDER);
		this.block = block;
	}

	public <T> RotatedBlockProvider(Dynamic<T> dynamic) {
		this(BlockState.deserialize(dynamic.get("state").orElseEmptyMap()).getBlock());
	}

	@Override
	public BlockState getState(Random random, BlockPos blockPos) {
		Direction.Axis axis = Direction.Axis.getRandomAxis(random);
		return this.block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("type"), dynamicOps.createString(Registry.BLOCKSTATE_PROVIDER_TYPES.getKey(this.type).toString()))
			.put(dynamicOps.createString("state"), BlockState.serialize(dynamicOps, this.block.defaultBlockState()).getValue());
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(builder.build())).getValue();
	}
}
