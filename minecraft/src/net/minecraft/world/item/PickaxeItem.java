package net.minecraft.world.item;

import net.minecraft.tags.BlockTags;

public class PickaxeItem extends DiggerItem {
	public PickaxeItem(Tier tier, Item.Properties properties) {
		super(tier, BlockTags.MINEABLE_WITH_PICKAXE, properties);
	}
}
