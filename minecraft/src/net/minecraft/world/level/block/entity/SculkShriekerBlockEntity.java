package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SculkShriekerBlockEntity extends BlockEntity {
	public SculkShriekerBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.SCULK_SHRIEKER, blockPos, blockState);
	}
}
