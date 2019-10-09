package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleStateProvider extends BlockStateProvider {
	private final BlockState state;

	public SimpleStateProvider(BlockState blockState) {
		super(BlockStateProviderType.SIMPLE_STATE_PROVIDER);
		this.state = blockState;
	}

	public <T> SimpleStateProvider(Dynamic<T> dynamic) {
		this(BlockState.deserialize(dynamic.get("state").orElseEmptyMap()));
	}

	@Override
	public BlockState getState(Random random, BlockPos blockPos) {
		return this.state;
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("type"), dynamicOps.createString(Registry.BLOCKSTATE_PROVIDER_TYPES.getKey(this.type).toString()))
			.put(dynamicOps.createString("state"), BlockState.serialize(dynamicOps, this.state).getValue());
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(builder.build())).getValue();
	}
}
