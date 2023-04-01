package net.minecraft.world.item;

import net.minecraft.voting.rules.Rules;
import net.minecraft.voting.rules.actual.FoodType;
import net.minecraft.world.level.block.Block;

public class ItemNameBlockItem extends BlockItem {
	public ItemNameBlockItem(Block block, Item.Properties properties) {
		super(block, properties);
	}

	@Override
	public String getDescriptionId() {
		FoodType foodType = Rules.FOOD_RESTRICTION.get();
		if (foodType != FoodType.ANY && this.foodProperties != null) {
			if (foodType.item() == this) {
				return foodType.foodKey();
			}

			if (FoodType.INEDIBLES.contains(this)) {
				return "rule.food_restriction.inedible." + this;
			}
		}

		return this.getOrCreateDescriptionId();
	}
}
