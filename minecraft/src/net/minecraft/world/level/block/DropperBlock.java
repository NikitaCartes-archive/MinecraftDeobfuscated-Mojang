package net.minecraft.world.level.block;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DropperBlock extends DispenserBlock {
	public DropperBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected Vec3 getSpitMotion(BlockState blockState) {
		return Vec3.ZERO;
	}
}
