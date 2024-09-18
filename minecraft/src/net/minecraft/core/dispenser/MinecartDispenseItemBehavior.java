package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public class MinecartDispenseItemBehavior extends DefaultDispenseItemBehavior {
	private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
	private final EntityType<? extends AbstractMinecart> entityType;

	public MinecartDispenseItemBehavior(EntityType<? extends AbstractMinecart> entityType) {
		this.entityType = entityType;
	}

	@Override
	public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
		Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
		ServerLevel serverLevel = blockSource.level();
		Vec3 vec3 = blockSource.center();
		double d = vec3.x() + (double)direction.getStepX() * 1.125;
		double e = Math.floor(vec3.y()) + (double)direction.getStepY();
		double f = vec3.z() + (double)direction.getStepZ() * 1.125;
		BlockPos blockPos = blockSource.pos().relative(direction);
		BlockState blockState = serverLevel.getBlockState(blockPos);
		double g;
		if (blockState.is(BlockTags.RAILS)) {
			if (getRailShape(blockState).isSlope()) {
				g = 0.6;
			} else {
				g = 0.1;
			}
		} else {
			if (!blockState.isAir()) {
				return this.defaultDispenseItemBehavior.dispense(blockSource, itemStack);
			}

			BlockState blockState2 = serverLevel.getBlockState(blockPos.below());
			if (!blockState2.is(BlockTags.RAILS)) {
				return this.defaultDispenseItemBehavior.dispense(blockSource, itemStack);
			}

			if (direction != Direction.DOWN && getRailShape(blockState2).isSlope()) {
				g = -0.4;
			} else {
				g = -0.9;
			}
		}

		Vec3 vec32 = new Vec3(d, e + g, f);
		AbstractMinecart abstractMinecart = AbstractMinecart.createMinecart(
			serverLevel, vec32.x, vec32.y, vec32.z, this.entityType, EntitySpawnReason.DISPENSER, itemStack, null
		);
		if (abstractMinecart != null) {
			serverLevel.addFreshEntity(abstractMinecart);
			itemStack.shrink(1);
		}

		return itemStack;
	}

	private static RailShape getRailShape(BlockState blockState) {
		return blockState.getBlock() instanceof BaseRailBlock baseRailBlock ? blockState.getValue(baseRailBlock.getShapeProperty()) : RailShape.NORTH_SOUTH;
	}

	@Override
	protected void playSound(BlockSource blockSource) {
		blockSource.level().levelEvent(1000, blockSource.pos(), 0);
	}
}
