package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockStateProvider {
	public static final Codec<BlockStateProvider> CODEC = Registry.BLOCKSTATE_PROVIDER_TYPES
		.byNameCodec()
		.dispatch(BlockStateProvider::type, BlockStateProviderType::codec);

	public static SimpleStateProvider simple(BlockState blockState) {
		return new SimpleStateProvider(blockState);
	}

	public static SimpleStateProvider simple(Block block) {
		return new SimpleStateProvider(block.defaultBlockState());
	}

	protected abstract BlockStateProviderType<?> type();

	public abstract BlockState getState(Random random, BlockPos blockPos);
}
