package net.minecraft.world.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.level.Level;

public class SpectralArrowItem extends ArrowItem {
	public SpectralArrowItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public AbstractArrow createArrow(Level level, ItemStack itemStack, LivingEntity livingEntity) {
		return new SpectralArrow(level, livingEntity, itemStack.copyWithCount(1));
	}

	@Override
	public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
		SpectralArrow spectralArrow = new SpectralArrow(level, position.x(), position.y(), position.z(), itemStack.copyWithCount(1));
		spectralArrow.pickup = AbstractArrow.Pickup.ALLOWED;
		return spectralArrow;
	}
}
