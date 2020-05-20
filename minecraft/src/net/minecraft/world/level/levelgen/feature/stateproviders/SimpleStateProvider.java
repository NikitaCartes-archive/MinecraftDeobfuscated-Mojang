package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleStateProvider extends BlockStateProvider {
	public static final Codec<SimpleStateProvider> CODEC = BlockState.CODEC
		.fieldOf("state")
		.<SimpleStateProvider>xmap(SimpleStateProvider::new, simpleStateProvider -> simpleStateProvider.state)
		.codec();
	private final BlockState state;

	public SimpleStateProvider(BlockState blockState) {
		this.state = blockState;
	}

	@Override
	protected BlockStateProviderType<?> type() {
		return BlockStateProviderType.SIMPLE_STATE_PROVIDER;
	}

	@Override
	public BlockState getState(Random random, BlockPos blockPos) {
		return this.state;
	}
}
