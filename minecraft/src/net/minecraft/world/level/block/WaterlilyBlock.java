package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WaterlilyBlock extends BushBlock {
	protected static final VoxelShape AABB = Block.box(1.0, 0.0, 1.0, 15.0, 1.5, 15.0);

	protected WaterlilyBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
		super.entityInside(blockState, level, blockPos, entity);
		if (level instanceof ServerLevel && entity instanceof Boat) {
			level.destroyBlock(new BlockPos(blockPos), true, entity);
		}
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return AABB;
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		FluidState fluidState = blockGetter.getFluidState(blockPos);
		return fluidState.getType() == Fluids.WATER || blockState.getMaterial() == Material.ICE;
	}
}
