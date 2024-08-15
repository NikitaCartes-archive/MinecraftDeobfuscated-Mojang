package net.minecraft.core.dispenser;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.block.DispenserBlock;

public class ProjectileDispenseBehavior extends DefaultDispenseItemBehavior {
	private final ProjectileItem projectileItem;
	private final ProjectileItem.DispenseConfig dispenseConfig;

	public ProjectileDispenseBehavior(Item item) {
		if (item instanceof ProjectileItem projectileItem) {
			this.projectileItem = projectileItem;
			this.dispenseConfig = projectileItem.createDispenseConfig();
		} else {
			throw new IllegalArgumentException(item + " not instance of " + ProjectileItem.class.getSimpleName());
		}
	}

	@Override
	public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
		ServerLevel serverLevel = blockSource.level();
		Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
		Position position = this.dispenseConfig.positionFunction().getDispensePosition(blockSource, direction);
		Projectile.spawnProjectileUsingShoot(
			this.projectileItem.asProjectile(serverLevel, position, itemStack, direction),
			serverLevel,
			itemStack,
			(double)direction.getStepX(),
			(double)direction.getStepY(),
			(double)direction.getStepZ(),
			this.dispenseConfig.power(),
			this.dispenseConfig.uncertainty()
		);
		itemStack.shrink(1);
		return itemStack;
	}

	@Override
	protected void playSound(BlockSource blockSource) {
		blockSource.level().levelEvent(this.dispenseConfig.overrideDispenseEvent().orElse(1002), blockSource.pos(), 0);
	}
}
