package net.minecraft.client.color.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class ItemColors {
	private final IdMapper<ItemColor> itemColors = new IdMapper<>(32);

	public static ItemColors createDefault(BlockColors blockColors) {
		ItemColors itemColors = new ItemColors();
		itemColors.register(
			(itemStack, i) -> i > 0 ? -1 : ((DyeableLeatherItem)itemStack.getItem()).getColor(itemStack),
			Items.LEATHER_HELMET,
			Items.LEATHER_CHESTPLATE,
			Items.LEATHER_LEGGINGS,
			Items.LEATHER_BOOTS,
			Items.LEATHER_HORSE_ARMOR
		);
		itemColors.register((itemStack, i) -> GrassColor.get(0.5, 1.0), Blocks.TALL_GRASS, Blocks.LARGE_FERN);
		itemColors.register((itemStack, i) -> {
			if (i != 1) {
				return -1;
			} else {
				CompoundTag compoundTag = itemStack.getTagElement("Explosion");
				int[] is = compoundTag != null && compoundTag.contains("Colors", 11) ? compoundTag.getIntArray("Colors") : null;
				if (is != null && is.length != 0) {
					if (is.length == 1) {
						return is[0];
					} else {
						int j = 0;
						int k = 0;
						int l = 0;

						for (int m : is) {
							j += (m & 0xFF0000) >> 16;
							k += (m & 0xFF00) >> 8;
							l += (m & 0xFF) >> 0;
						}

						j /= is.length;
						k /= is.length;
						l /= is.length;
						return j << 16 | k << 8 | l;
					}
				} else {
					return 9079434;
				}
			}
		}, Items.FIREWORK_STAR);
		itemColors.register((itemStack, i) -> i > 0 ? -1 : PotionUtils.getColor(itemStack), Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);

		for (SpawnEggItem spawnEggItem : SpawnEggItem.eggs()) {
			itemColors.register((itemStack, i) -> spawnEggItem.getColor(i), spawnEggItem);
		}

		itemColors.register(
			(itemStack, i) -> {
				BlockState blockState = ((BlockItem)itemStack.getItem()).getBlock().defaultBlockState();
				return blockColors.getColor(blockState, null, null, i);
			},
			Blocks.GRASS_BLOCK,
			Blocks.GRASS,
			Blocks.FERN,
			Blocks.VINE,
			Blocks.OAK_LEAVES,
			Blocks.SPRUCE_LEAVES,
			Blocks.BIRCH_LEAVES,
			Blocks.JUNGLE_LEAVES,
			Blocks.ACACIA_LEAVES,
			Blocks.DARK_OAK_LEAVES,
			Blocks.LILY_PAD
		);
		itemColors.register((itemStack, i) -> i == 0 ? PotionUtils.getColor(itemStack) : -1, Items.TIPPED_ARROW);
		itemColors.register((itemStack, i) -> i == 0 ? -1 : MapItem.getColor(itemStack), Items.FILLED_MAP);
		return itemColors;
	}

	public int getColor(ItemStack itemStack, int i) {
		ItemColor itemColor = this.itemColors.byId(Registry.ITEM.getId(itemStack.getItem()));
		return itemColor == null ? -1 : itemColor.getColor(itemStack, i);
	}

	public void register(ItemColor itemColor, ItemLike... itemLikes) {
		for (ItemLike itemLike : itemLikes) {
			this.itemColors.addMapping(itemColor, Item.getId(itemLike.asItem()));
		}
	}
}
