package net.minecraft.world.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

public class ArrowItem extends Item implements ProjectileItem {
	public ArrowItem(Item.Properties properties) {
		super(properties);
	}

	public AbstractArrow createArrow(Level level, ItemStack itemStack, LivingEntity livingEntity) {
		return new Arrow(level, livingEntity, itemStack.copyWithCount(1));
	}

	@Override
	public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
		Arrow arrow = new Arrow(level, position.x(), position.y(), position.z(), itemStack.copyWithCount(1));
		arrow.pickup = AbstractArrow.Pickup.ALLOWED;
		return arrow;
	}
}
