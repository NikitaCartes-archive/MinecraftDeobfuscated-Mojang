package net.minecraft.data.loot.packs;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public interface LootData {
	Map<DyeColor, ItemLike> WOOL_ITEM_BY_DYE = Util.make(Maps.newEnumMap(DyeColor.class), enumMap -> {
		enumMap.put(DyeColor.WHITE, Blocks.WHITE_WOOL);
		enumMap.put(DyeColor.ORANGE, Blocks.ORANGE_WOOL);
		enumMap.put(DyeColor.MAGENTA, Blocks.MAGENTA_WOOL);
		enumMap.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL);
		enumMap.put(DyeColor.YELLOW, Blocks.YELLOW_WOOL);
		enumMap.put(DyeColor.LIME, Blocks.LIME_WOOL);
		enumMap.put(DyeColor.PINK, Blocks.PINK_WOOL);
		enumMap.put(DyeColor.GRAY, Blocks.GRAY_WOOL);
		enumMap.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL);
		enumMap.put(DyeColor.CYAN, Blocks.CYAN_WOOL);
		enumMap.put(DyeColor.PURPLE, Blocks.PURPLE_WOOL);
		enumMap.put(DyeColor.BLUE, Blocks.BLUE_WOOL);
		enumMap.put(DyeColor.BROWN, Blocks.BROWN_WOOL);
		enumMap.put(DyeColor.GREEN, Blocks.GREEN_WOOL);
		enumMap.put(DyeColor.RED, Blocks.RED_WOOL);
		enumMap.put(DyeColor.BLACK, Blocks.BLACK_WOOL);
	});
}
