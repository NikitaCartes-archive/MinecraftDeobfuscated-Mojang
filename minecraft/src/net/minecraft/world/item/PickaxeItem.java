package net.minecraft.world.item;

import net.minecraft.tags.BlockTags;

public class PickaxeItem extends DiggerItem {
	public PickaxeItem(ToolMaterial toolMaterial, float f, float g, Item.Properties properties) {
		super(toolMaterial, BlockTags.MINEABLE_WITH_PICKAXE, f, g, properties);
	}
}
