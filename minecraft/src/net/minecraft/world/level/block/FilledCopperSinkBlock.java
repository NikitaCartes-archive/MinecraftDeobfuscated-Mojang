package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class FilledCopperSinkBlock extends AbstractCauldronBlock {
	public FilledCopperSinkBlock(BlockBehaviour.Properties properties) {
		super(properties, CauldronInteraction.WATER_SINK);
	}

	@Override
	public boolean isFull(BlockState blockState) {
		return true;
	}

	@Override
	protected double getContentHeight(BlockState blockState) {
		return 0.9375;
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		if (!level.isClientSide && entity.isOnFire() && this.isEntityInsideContent(blockState, blockPos, entity)) {
			entity.clearFire();
		}
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
		return 3;
	}
}
