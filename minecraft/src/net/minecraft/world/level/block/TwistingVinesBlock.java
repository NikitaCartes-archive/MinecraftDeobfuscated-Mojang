package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TwistingVinesBlock extends GrowingPlantHeadBlock {
	public static final MapCodec<TwistingVinesBlock> CODEC = simpleCodec(TwistingVinesBlock::new);
	public static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 15.0, 12.0);

	@Override
	public MapCodec<TwistingVinesBlock> codec() {
		return CODEC;
	}

	public TwistingVinesBlock(BlockBehaviour.Properties properties) {
		super(properties, Direction.UP, SHAPE, false, 0.1);
	}

	@Override
	protected int getBlocksToGrowWhenBonemealed(RandomSource randomSource) {
		return NetherVines.getBlocksToGrowWhenBonemealed(randomSource);
	}

	@Override
	protected Block getBodyBlock() {
		return Blocks.TWISTING_VINES_PLANT;
	}

	@Override
	protected boolean canGrowInto(BlockState blockState) {
		return NetherVines.isValidGrowthState(blockState);
	}
}
