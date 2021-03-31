package net.minecraft.world.phys.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;

public interface CollisionContext {
	static CollisionContext empty() {
		return EntityCollisionContext.EMPTY;
	}

	static CollisionContext of(Entity entity) {
		return new EntityCollisionContext(entity);
	}

	boolean isDescending();

	boolean isAbove(VoxelShape voxelShape, BlockPos blockPos, boolean bl);

	boolean hasItemOnFeet(Item item);

	boolean isHoldingItem(Item item);

	boolean canStandOnFluid(FluidState fluidState, FlowingFluid flowingFluid);
}
