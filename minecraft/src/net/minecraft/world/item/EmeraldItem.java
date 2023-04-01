package net.minecraft.world.item;

import net.minecraft.voting.rules.Rules;

public class EmeraldItem extends Item {
	public EmeraldItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public String getDescriptionId(ItemStack itemStack) {
		return Rules.EMERALD_TO_RUBY.get() ? "item.minecraft.ruby" : super.getDescriptionId(itemStack);
	}
}
