package net.minecraft.voting.rules.actual;

import net.minecraft.core.Holder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class Goldifier {
	public static ItemStack apply(ItemStack itemStack) {
		Item item = apply(itemStack.getItem());
		if (itemStack.getItem() == item) {
			return itemStack;
		} else {
			ItemStack itemStack2 = new ItemStack(item, itemStack.getCount());
			if (itemStack.hasTag()) {
				itemStack2.setTag(itemStack.getTag());
			}

			return itemStack2;
		}
	}

	public static Item apply(Item item) {
		Holder.Reference<Item> reference = item.builtInRegistryHolder();
		if (reference.is(ItemTags.RAILS)) {
			return Items.POWERED_RAIL;
		} else if (reference.is(ItemTags.PICKAXES)) {
			return Items.GOLDEN_PICKAXE;
		} else if (reference.is(ItemTags.SWORDS)) {
			return Items.GOLDEN_SWORD;
		} else if (reference.is(ItemTags.SHOVELS)) {
			return Items.GOLDEN_SHOVEL;
		} else if (reference.is(ItemTags.HOES)) {
			return Items.GOLDEN_HOE;
		} else if (reference.is(ItemTags.AXES)) {
			return Items.GOLDEN_AXE;
		} else if (item instanceof ArmorItem armorItem) {
			return switch (armorItem.getType()) {
				case HELMET -> Items.GOLDEN_HELMET;
				case CHESTPLATE -> Items.GOLDEN_CHESTPLATE;
				case LEGGINGS -> Items.GOLDEN_LEGGINGS;
				case BOOTS -> Items.GOLDEN_BOOTS;
			};
		} else if (item == Items.APPLE) {
			return Items.GOLDEN_APPLE;
		} else if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) {
			return Items.ENCHANTED_GOLDEN_APPLE;
		} else if (item == Items.CARROT || item == Items.GOLDEN_CARROT) {
			return Items.GOLDEN_CARROT;
		} else if (item == Items.ARROW || item == Items.SPECTRAL_ARROW) {
			return Items.SPECTRAL_ARROW;
		} else if (item == Items.IRON_INGOT || item == Items.COPPER_INGOT || item == Items.NETHERITE_INGOT || item == Items.GOLD_INGOT) {
			return Items.GOLD_INGOT;
		} else if (item == Items.IRON_NUGGET || item == Items.GOLD_NUGGET) {
			return Items.GOLD_NUGGET;
		} else if (item == Items.RAW_GOLD || item == Items.RAW_COPPER || item == Items.RAW_IRON) {
			return Items.RAW_GOLD;
		} else if (item == Items.MELON_SLICE || item == Items.GLISTERING_MELON_SLICE) {
			return Items.GLISTERING_MELON_SLICE;
		} else if (item instanceof HorseArmorItem) {
			return Items.GOLDEN_HORSE_ARMOR;
		} else {
			return item instanceof BlockItem blockItem ? apply(blockItem.getBlock()).asItem() : Items.GOLD_INGOT;
		}
	}

	public static Block apply(Block block) {
		if (block == Blocks.RAW_COPPER_BLOCK || block == Blocks.RAW_IRON_BLOCK || block == Blocks.RAW_GOLD_BLOCK) {
			return Blocks.RAW_GOLD_BLOCK;
		} else if (block == Blocks.GOLD_ORE
			|| block == Blocks.COPPER_ORE
			|| block == Blocks.IRON_ORE
			|| block == Blocks.COAL_ORE
			|| block == Blocks.EMERALD_ORE
			|| block == Blocks.REDSTONE_ORE
			|| block == Blocks.DIAMOND_ORE
			|| block == Blocks.LAPIS_ORE) {
			return Blocks.GOLD_ORE;
		} else if (block == Blocks.DEEPSLATE_GOLD_ORE
			|| block == Blocks.DEEPSLATE_COPPER_ORE
			|| block == Blocks.DEEPSLATE_IRON_ORE
			|| block == Blocks.DEEPSLATE_COAL_ORE
			|| block == Blocks.DEEPSLATE_EMERALD_ORE
			|| block == Blocks.DEEPSLATE_REDSTONE_ORE
			|| block == Blocks.DEEPSLATE_DIAMOND_ORE
			|| block == Blocks.DEEPSLATE_LAPIS_ORE) {
			return Blocks.DEEPSLATE_GOLD_ORE;
		} else {
			return block != Blocks.NETHER_QUARTZ_ORE && block != Blocks.NETHER_GOLD_ORE ? Blocks.GOLD_BLOCK : Blocks.NETHER_GOLD_ORE;
		}
	}

	public static BlockState apply(BlockState blockState) {
		Block block = apply(blockState.getBlock());
		return block == blockState.getBlock() ? blockState : copyProperties(blockState, block.defaultBlockState());
	}

	private static BlockState copyProperties(BlockState blockState, BlockState blockState2) {
		for (Property<?> property : blockState.getProperties()) {
			blockState2 = copyProperty(blockState, blockState2, property);
		}

		return blockState2;
	}

	private static <T extends Comparable<T>> BlockState copyProperty(BlockState blockState, BlockState blockState2, Property<T> property) {
		if (blockState2.hasProperty(property)) {
			blockState2 = blockState2.setValue(property, blockState.getValue(property));
		}

		return blockState2;
	}
}
