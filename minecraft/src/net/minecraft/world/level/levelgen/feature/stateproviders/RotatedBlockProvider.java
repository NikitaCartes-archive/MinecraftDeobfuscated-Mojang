package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class RotatedBlockProvider extends BlockStateProvider {
	public static final Codec<RotatedBlockProvider> CODEC = BlockState.CODEC
		.fieldOf("state")
		.xmap(BlockBehaviour.BlockStateBase::getBlock, Block::defaultBlockState)
		.<RotatedBlockProvider>xmap(RotatedBlockProvider::new, rotatedBlockProvider -> rotatedBlockProvider.block)
		.codec();
	private final Block block;

	public RotatedBlockProvider(Block block) {
		this.block = block;
	}

	@Override
	protected BlockStateProviderType<?> type() {
		return BlockStateProviderType.ROTATED_BLOCK_PROVIDER;
	}

	@Override
	public BlockState getState(RandomSource randomSource, BlockPos blockPos) {
		Direction.Axis axis = Direction.Axis.getRandom(randomSource);
		return this.block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
	}
}
