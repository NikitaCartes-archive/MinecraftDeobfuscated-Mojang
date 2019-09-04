package net.minecraft.world.phys.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;

public interface CollisionContext {
	static CollisionContext empty() {
		return EntityCollisionContext.EMPTY;
	}

	static CollisionContext of(Entity entity) {
		return new EntityCollisionContext(entity);
	}

	boolean isDescending();

	boolean isAbove(VoxelShape voxelShape, BlockPos blockPos, boolean bl);

	boolean isHoldingItem(Item item);
}
