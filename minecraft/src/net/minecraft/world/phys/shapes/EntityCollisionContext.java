package net.minecraft.world.phys.shapes;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

public class EntityCollisionContext implements CollisionContext {
	protected static final CollisionContext EMPTY = new EntityCollisionContext(false, -Double.MAX_VALUE, ItemStack.EMPTY, fluid -> false) {
		@Override
		public boolean isAbove(VoxelShape voxelShape, BlockPos blockPos, boolean bl) {
			return bl;
		}
	};
	private final boolean descending;
	private final double entityBottom;
	private final ItemStack heldItem;
	private final Predicate<Fluid> canStandOnFluid;

	protected EntityCollisionContext(boolean bl, double d, ItemStack itemStack, Predicate<Fluid> predicate) {
		this.descending = bl;
		this.entityBottom = d;
		this.heldItem = itemStack;
		this.canStandOnFluid = predicate;
	}

	@Deprecated
	protected EntityCollisionContext(Entity entity) {
		this(
			entity.isDescending(),
			entity.getY(),
			entity instanceof LivingEntity ? ((LivingEntity)entity).getMainHandItem() : ItemStack.EMPTY,
			entity instanceof LivingEntity ? ((LivingEntity)entity)::canStandOnFluid : fluid -> false
		);
	}

	@Override
	public boolean isHoldingItem(Item item) {
		return this.heldItem.is(item);
	}

	@Override
	public boolean canStandOnFluid(FluidState fluidState, FlowingFluid flowingFluid) {
		return this.canStandOnFluid.test(flowingFluid) && !fluidState.getType().isSame(flowingFluid);
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
