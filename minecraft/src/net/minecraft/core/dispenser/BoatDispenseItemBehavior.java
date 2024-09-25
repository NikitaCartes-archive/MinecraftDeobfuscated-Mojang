package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

public class BoatDispenseItemBehavior extends DefaultDispenseItemBehavior {
	private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
	private final EntityType<? extends AbstractBoat> type;

	public BoatDispenseItemBehavior(EntityType<? extends AbstractBoat> entityType) {
		this.type = entityType;
	}

	@Override
	public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
		Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
		ServerLevel serverLevel = blockSource.level();
		Vec3 vec3 = blockSource.center();
		double d = 0.5625 + (double)this.type.getWidth() / 2.0;
		double e = vec3.x() + (double)direction.getStepX() * d;
		double f = vec3.y() + (double)((float)direction.getStepY() * 1.125F);
		double g = vec3.z() + (double)direction.getStepZ() * d;
		BlockPos blockPos = blockSource.pos().relative(direction);
		double h;
		if (serverLevel.getFluidState(blockPos).is(FluidTags.WATER)) {
			h = 1.0;
		} else {
			if (!serverLevel.getBlockState(blockPos).isAir() || !serverLevel.getFluidState(blockPos.below()).is(FluidTags.WATER)) {
				return this.defaultDispenseItemBehavior.dispense(blockSource, itemStack);
			}

			h = 0.0;
		}

		AbstractBoat abstractBoat = this.type.create(serverLevel, EntitySpawnReason.DISPENSER);
		if (abstractBoat != null) {
			abstractBoat.setInitialPos(e, f + h, g);
			EntityType.createDefaultStackConfig(serverLevel, itemStack, null).accept(abstractBoat);
			abstractBoat.setYRot(direction.toYRot());
			serverLevel.addFreshEntity(abstractBoat);
			itemStack.shrink(1);
		}

		return itemStack;
	}

	@Override
	protected void playSound(BlockSource blockSource) {
		blockSource.level().levelEvent(1000, blockSource.pos(), 0);
	}
}
