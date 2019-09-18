package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class WebBlock extends Block {
	public WebBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		entity.makeStuckInBlock(blockState, new Vec3(0.25, 0.05F, 0.25));
	}
}
