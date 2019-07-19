package net.minecraft.world.item;

import net.minecraft.world.level.block.Block;

public class ItemNameBlockItem extends BlockItem {
	public ItemNameBlockItem(Block block, Item.Properties properties) {
		super(block, properties);
	}

	@Override
	public String getDescriptionId() {
		return this.getOrCreateDescriptionId();
	}
}
