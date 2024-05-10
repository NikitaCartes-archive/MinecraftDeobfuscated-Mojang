package net.minecraft.core.dispenser;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class DefaultDispenseItemBehavior implements DispenseItemBehavior {
	private static final int DEFAULT_ACCURACY = 6;

	@Override
	public final ItemStack dispense(BlockSource blockSource, ItemStack itemStack) {
		ItemStack itemStack2 = this.execute(blockSource, itemStack);
		this.playSound(blockSource);
		this.playAnimation(blockSource, blockSource.state().getValue(DispenserBlock.FACING));
		return itemStack2;
	}

	protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
		Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
		Position position = DispenserBlock.getDispensePosition(blockSource);
		ItemStack itemStack2 = itemStack.split(1);
		spawnItem(blockSource.level(), itemStack2, 6, direction, position);
		return itemStack;
	}

	public static void spawnItem(Level level, ItemStack itemStack, int i, Direction direction, Position position) {
		double d = position.x();
		double e = position.y();
		double f = position.z();
		if (direction.getAxis() == Direction.Axis.Y) {
			e -= 0.125;
		} else {
			e -= 0.15625;
		}

		ItemEntity itemEntity = new ItemEntity(level, d, e, f, itemStack);
		double g = level.random.nextDouble() * 0.1 + 0.2;
		itemEntity.setDeltaMovement(
			level.random.triangle((double)direction.getStepX() * g, 0.0172275 * (double)i),
			level.random.triangle(0.2, 0.0172275 * (double)i),
			level.random.triangle((double)direction.getStepZ() * g, 0.0172275 * (double)i)
		);
		level.addFreshEntity(itemEntity);
	}

	protected void playSound(BlockSource blockSource) {
		playDefaultSound(blockSource);
	}

	protected void playAnimation(BlockSource blockSource, Direction direction) {
		playDefaultAnimation(blockSource, direction);
	}

	private static void playDefaultSound(BlockSource blockSource) {
		blockSource.level().levelEvent(1000, blockSource.pos(), 0);
	}

	private static void playDefaultAnimation(BlockSource blockSource, Direction direction) {
		blockSource.level().levelEvent(2000, blockSource.pos(), direction.get3DDataValue());
	}

	protected ItemStack consumeWithRemainder(BlockSource blockSource, ItemStack itemStack, ItemStack itemStack2) {
		itemStack.shrink(1);
		if (itemStack.isEmpty()) {
			return itemStack2;
		} else {
			this.addToInventoryOrDispense(blockSource, itemStack2);
			return itemStack;
		}
	}

	private void addToInventoryOrDispense(BlockSource blockSource, ItemStack itemStack) {
		ItemStack itemStack2 = blockSource.blockEntity().insertItem(itemStack);
		if (!itemStack2.isEmpty()) {
			Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
			spawnItem(blockSource.level(), itemStack2, 6, direction, DispenserBlock.getDispensePosition(blockSource));
			playDefaultSound(blockSource);
			playDefaultAnimation(blockSource, direction);
		}
	}
}
