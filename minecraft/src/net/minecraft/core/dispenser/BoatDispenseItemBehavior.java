package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class BoatDispenseItemBehavior extends DefaultDispenseItemBehavior {
	private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
	private final Boat.Type type;
	private final boolean isChestBoat;

	public BoatDispenseItemBehavior(Boat.Type type) {
		this(type, false);
	}

	public BoatDispenseItemBehavior(Boat.Type type, boolean bl) {
		this.type = type;
		this.isChestBoat = bl;
	}

	@Override
	public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
		Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
		Level level = blockSource.getLevel();
		double d = 0.5625 + (double)EntityType.BOAT.getWidth() / 2.0;
		double e = blockSource.x() + (double)direction.getStepX() * d;
		double f = blockSource.y() + (double)direction.getStepY() - (double)EntityType.BOAT.getHeight();
		double g = blockSource.z() + (double)direction.getStepZ() * d;
		BlockPos blockPos = blockSource.getPos().relative(direction);
		double h;
		if (level.getFluidState(blockPos).is(FluidTags.WATER)) {
			h = 1.0;
		} else {
			if (!level.getBlockState(blockPos).isAir() || !level.getFluidState(blockPos.below()).is(FluidTags.WATER)) {
				return this.defaultDispenseItemBehavior.dispense(blockSource, itemStack);
			}

			h = 0.0;
		}

		Boat boat = (Boat)(this.isChestBoat ? new ChestBoat(level, e, f + h, g) : new Boat(level, e, f + h, g));
		boat.setVariant(this.type);
		boat.setYRot(direction.toYRot());
		level.addFreshEntity(boat);
		itemStack.shrink(1);
		return itemStack;
	}

	@Override
	protected void playSound(BlockSource blockSource) {
		blockSource.getLevel().levelEvent(1000, blockSource.getPos(), 0);
	}
}
