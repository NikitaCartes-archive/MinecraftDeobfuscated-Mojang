package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.state.BlockState;

public class WeightedStateProvider extends BlockStateProvider {
	public static final Codec<WeightedStateProvider> CODEC = SimpleWeightedRandomList.wrappedCodec(BlockState.CODEC)
		.<WeightedStateProvider>comapFlatMap(WeightedStateProvider::create, weightedStateProvider -> weightedStateProvider.weightedList)
		.fieldOf("entries")
		.codec();
	private final SimpleWeightedRandomList<BlockState> weightedList;

	private static DataResult<WeightedStateProvider> create(SimpleWeightedRandomList<BlockState> simpleWeightedRandomList) {
		return simpleWeightedRandomList.isEmpty()
			? DataResult.error("WeightedStateProvider with no states")
			: DataResult.success(new WeightedStateProvider(simpleWeightedRandomList));
	}

	public WeightedStateProvider(SimpleWeightedRandomList<BlockState> simpleWeightedRandomList) {
		this.weightedList = simpleWeightedRandomList;
	}

	public WeightedStateProvider(SimpleWeightedRandomList.Builder<BlockState> builder) {
		this(builder.build());
	}

	@Override
	protected BlockStateProviderType<?> type() {
		return BlockStateProviderType.WEIGHTED_STATE_PROVIDER;
	}

	@Override
	public BlockState getState(RandomSource randomSource, BlockPos blockPos) {
		return (BlockState)this.weightedList.getRandomValue(randomSource).orElseThrow(IllegalStateException::new);
	}
}
