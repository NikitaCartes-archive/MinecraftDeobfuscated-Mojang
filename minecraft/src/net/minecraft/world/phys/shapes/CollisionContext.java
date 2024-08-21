package net.minecraft.world.phys.shapes;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public interface CollisionContext {
	static CollisionContext empty() {
		return EntityCollisionContext.EMPTY;
	}

	static CollisionContext of(Entity entity) {
		Objects.requireNonNull(entity);

		return (CollisionContext)(switch (entity) {
			case AbstractMinecart abstractMinecart -> AbstractMinecart.useExperimentalMovement(abstractMinecart.level())
			? new MinecartCollisionContext(abstractMinecart, false)
			: new EntityCollisionContext(entity, false);
			default -> new EntityCollisionContext(entity, false);
		});
	}

	static CollisionContext of(Entity entity, boolean bl) {
		return new EntityCollisionContext(entity, bl);
	}

	boolean isDescending();

	boolean isAbove(VoxelShape voxelShape, BlockPos blockPos, boolean bl);

	boolean isHoldingItem(Item item);

	boolean canStandOnFluid(FluidState fluidState, FluidState fluidState2);

	VoxelShape getCollisionShape(BlockState blockState, CollisionGetter collisionGetter, BlockPos blockPos);
}
