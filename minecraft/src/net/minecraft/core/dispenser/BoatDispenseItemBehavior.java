package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
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
		double d = blockSource.x() + (double)((float)direction.getStepX() * 1.125F);
		double e = blockSource.y() + (double)((float)direction.getStepY() * 1.125F);
		double f = blockSource.z() + (double)((float)direction.getStepZ() * 1.125F);
		BlockPos blockPos = blockSource.getPos().relative(direction);
		double g;
		if (level.getFluidState(blockPos).is(FluidTags.WATER)) {
			g = 1.0;
		} else {
			if (!level.getBlockState(blockPos).isAir() || !level.getFluidState(blockPos.below()).is(FluidTags.WATER)) {
				return this.defaultDispenseItemBehavior.dispense(blockSource, itemStack);
			}

			g = 0.0;
		}

		Boat boat = (Boat)(this.isChestBoat ? new ChestBoat(level, d, e + g, f) : new Boat(level, d, e + g, f));
		boat.setType(this.type);
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
