package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.behavior.WeightedList;
import net.minecraft.world.level.block.state.BlockState;

public class WeightedStateProvider extends BlockStateProvider {
	private final WeightedList<BlockState> weightedList;

	private WeightedStateProvider(WeightedList<BlockState> weightedList) {
		super(BlockStateProviderType.WEIGHTED_STATE_PROVIDER);
		this.weightedList = weightedList;
	}

	public WeightedStateProvider() {
		this(new WeightedList<>());
	}

	public <T> WeightedStateProvider(Dynamic<T> dynamic) {
		this(new WeightedList<>(dynamic.get("entries").orElseEmptyList(), BlockState::deserialize));
	}

	public WeightedStateProvider add(BlockState blockState, int i) {
		this.weightedList.add(blockState, i);
		return this;
	}

	@Override
	public BlockState getState(Random random, BlockPos blockPos) {
		return this.weightedList.getOne(random);
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("type"), dynamicOps.createString(Registry.BLOCKSTATE_PROVIDER_TYPES.getKey(this.type).toString()))
			.put(dynamicOps.createString("entries"), this.weightedList.serialize(dynamicOps, blockState -> BlockState.serialize(dynamicOps, blockState)));
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(builder.build())).getValue();
	}
}
