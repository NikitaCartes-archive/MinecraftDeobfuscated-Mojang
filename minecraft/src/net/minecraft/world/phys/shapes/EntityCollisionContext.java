package net.minecraft.world.phys.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class EntityCollisionContext implements CollisionContext {
	protected static final CollisionContext EMPTY = new EntityCollisionContext(false, -Double.MAX_VALUE, Items.AIR) {
		@Override
		public boolean isAbove(VoxelShape voxelShape, BlockPos blockPos, boolean bl) {
			return bl;
		}
	};
	private final boolean descending;
	private final double entityBottom;
	private final Item heldItem;

	protected EntityCollisionContext(boolean bl, double d, Item item) {
		this.descending = bl;
		this.entityBottom = d;
		this.heldItem = item;
	}

	@Deprecated
	protected EntityCollisionContext(Entity entity) {
		this(entity.isDescending(), entity.getY(), entity instanceof LivingEntity ? ((LivingEntity)entity).getMainHandItem().getItem() : Items.AIR);
	}

	@Override
	public boolean isHoldingItem(Item item) {
		return this.heldItem == item;
	}

	@Override
	public boolean isDescending() {
		return this.descending;
	}

	@Override
	public boolean isAbove(VoxelShape voxelShape, BlockPos blockPos, boolean bl) {
		return this.entityBottom > (double)blockPos.getY() + voxelShape.max(Direction.Axis.Y) - 1.0E-5F;
	}
}
