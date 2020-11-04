package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DaylightDetectorBlockEntity extends BlockEntity {
	public DaylightDetectorBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.DAYLIGHT_DETECTOR, blockPos, blockState);
	}
}
