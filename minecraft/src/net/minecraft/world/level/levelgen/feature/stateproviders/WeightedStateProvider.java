package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.behavior.WeightedList;
import net.minecraft.world.level.block.state.BlockState;

public class WeightedStateProvider extends BlockStateProvider {
	public static final Codec<WeightedStateProvider> CODEC = WeightedList.codec(BlockState.CODEC)
		.<WeightedStateProvider>comapFlatMap(WeightedStateProvider::create, weightedStateProvider -> weightedStateProvider.weightedList)
		.fieldOf("entries")
		.codec();
	private final WeightedList<BlockState> weightedList;

	private static DataResult<WeightedStateProvider> create(WeightedList<BlockState> weightedList) {
		return weightedList.isEmpty() ? DataResult.error("WeightedStateProvider with no states") : DataResult.success(new WeightedStateProvider(weightedList));
	}

	private WeightedStateProvider(WeightedList<BlockState> weightedList) {
		this.weightedList = weightedList;
	}

	@Override
	protected BlockStateProviderType<?> type() {
		return BlockStateProviderType.WEIGHTED_STATE_PROVIDER;
	}

	public WeightedStateProvider() {
		this(new WeightedList<>());
	}

	public WeightedStateProvider add(BlockState blockState, int i) {
		this.weightedList.add(blockState, i);
		return this;
	}

	@Override
	public BlockState getState(Random random, BlockPos blockPos) {
		return this.weightedList.getOne(random);
	}
}
