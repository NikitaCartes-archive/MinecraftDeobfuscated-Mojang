package net.minecraft.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;

public class ArrowItem extends Item {
	public ArrowItem(Item.Properties properties) {
		super(properties);
	}

	public AbstractArrow createArrow(Level level, ItemStack itemStack, LivingEntity livingEntity) {
		Arrow arrow = new Arrow(level, livingEntity);
		arrow.setEffectsFromItem(itemStack);
		return arrow;
	}
}
