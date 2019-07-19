package net.minecraft.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class BowlFoodItem extends Item {
	public BowlFoodItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		super.finishUsingItem(itemStack, level, livingEntity);
		return new ItemStack(Items.BOWL);
	}
}
