package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WeepingVinesBlock extends GrowingPlantHeadBlock {
	public static final MapCodec<WeepingVinesBlock> CODEC = simpleCodec(WeepingVinesBlock::new);
	protected static final VoxelShape SHAPE = Block.box(4.0, 9.0, 4.0, 12.0, 16.0, 12.0);

	@Override
	public MapCodec<WeepingVinesBlock> codec() {
		return CODEC;
	}

	public WeepingVinesBlock(BlockBehaviour.Properties properties) {
		super(properties, Direction.DOWN, SHAPE, false, 0.1);
	}

	@Override
	protected int getBlocksToGrowWhenBonemealed(RandomSource randomSource) {
		return NetherVines.getBlocksToGrowWhenBonemealed(randomSource);
	}

	@Override
	protected Block getBodyBlock() {
		return Blocks.WEEPING_VINES_PLANT;
	}

	@Override
	protected boolean canGrowInto(BlockState blockState) {
		return NetherVines.isValidGrowthState(blockState);
	}
}
