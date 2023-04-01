package net.minecraft.voting.rules.actual;

import net.minecraft.voting.rules.Rules;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class RuleFeatureToggles {
	public static boolean isEnabled(Block block) {
		if (block == Blocks.PICKAXE_BLOCK) {
			return Rules.PICKAXE_BLOCK.get();
		} else if (block == Blocks.PLACE_BLOCK) {
			return Rules.PLACE_BLOCK.get();
		} else if (block == Blocks.COPPER_SINK) {
			return Rules.COPPER_SINK.get();
		} else {
			return block == Blocks.PACKED_AIR ? Rules.AIR_BLOCKS.get() : true;
		}
	}

	public static boolean isEnabled(Item item) {
		if (item instanceof BlockItem blockItem) {
			return isEnabled(blockItem.getBlock());
		} else {
			return item == Items.AIR_BLOCK ? Rules.AIR_BLOCKS.get() : true;
		}
	}

	public static boolean isEnabled(ItemStack itemStack) {
		return isEnabled(itemStack.getItem());
	}
}
