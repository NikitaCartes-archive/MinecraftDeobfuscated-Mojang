package net.minecraft.world.level.block;

import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class ShearableDoublePlantBlock extends DoublePlantBlock {
	public static final EnumProperty<DoubleBlockHalf> HALF = DoublePlantBlock.HALF;

	public ShearableDoublePlantBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
		boolean bl = super.canBeReplaced(blockState, blockPlaceContext);
		return bl && blockPlaceContext.getItemInHand().getItem() == this.asItem() ? false : bl;
	}
}
