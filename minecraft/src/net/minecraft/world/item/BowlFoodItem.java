package net.minecraft.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class BowlFoodItem extends Item {
	public BowlFoodItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
		ItemStack itemStack2 = super.finishUsingItem(itemStack, level, livingEntity);
		if (livingEntity instanceof Player player && player.hasInfiniteMaterials()) {
			return itemStack2;
		}

		return new ItemStack(Items.BOWL);
	}
}
