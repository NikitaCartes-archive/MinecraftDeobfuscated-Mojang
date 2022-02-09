package net.minecraft.world.phys.shapes;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;

public class EntityCollisionContext implements CollisionContext {
	protected static final CollisionContext EMPTY = new EntityCollisionContext(false, -Double.MAX_VALUE, ItemStack.EMPTY, fluidState -> false, null) {
		@Override
		public boolean isAbove(VoxelShape voxelShape, BlockPos blockPos, boolean bl) {
			return bl;
		}
	};
	private final boolean descending;
	private final double entityBottom;
	private final ItemStack heldItem;
	private final Predicate<FluidState> canStandOnFluid;
	@Nullable
	private final Entity entity;

	protected EntityCollisionContext(boolean bl, double d, ItemStack itemStack, Predicate<FluidState> predicate, @Nullable Entity entity) {
		this.descending = bl;
		this.entityBottom = d;
		this.heldItem = itemStack;
		this.canStandOnFluid = predicate;
		this.entity = entity;
	}

	@Deprecated
	protected EntityCollisionContext(Entity entity) {
		this(
			entity.isDescending(),
			entity.getY(),
			entity instanceof LivingEntity ? ((LivingEntity)entity).getMainHandItem() : ItemStack.EMPTY,
			entity instanceof LivingEntity ? ((LivingEntity)entity)::canStandOnFluid : fluidState -> false,
			entity
		);
	}

	@Override
	public boolean isHoldingItem(Item item) {
		return this.heldItem.is(item);
	}

	@Override
	public boolean canStandOnFluid(FluidState fluidState, FluidState fluidState2) {
		return this.canStandOnFluid.test(fluidState2) && !fluidState.getType().isSame(fluidState2.getType());
	}

	@Override
	public boolean isDescending() {
		return this.descending;
	}

	@Override
	public boolean isAbove(VoxelShape voxelShape, BlockPos blockPos, boolean bl) {
		return this.entityBottom > (double)blockPos.getY() + voxelShape.max(Direction.Axis.Y) - 1.0E-5F;
	}

	@Nullable
	public Entity getEntity() {
		return this.entity;
	}
}
