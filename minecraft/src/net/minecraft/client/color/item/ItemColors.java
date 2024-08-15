package net.minecraft.client.color.item;

import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.IdMapper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class ItemColors {
	private static final int DEFAULT = -1;
	private final IdMapper<ItemColor> itemColors = new IdMapper<>(32);

	public static ItemColors createDefault(BlockColors blockColors) {
		ItemColors itemColors = new ItemColors();
		itemColors.register(
			(itemStack, i) -> i > 0 ? -1 : DyedItemColor.getOrDefault(itemStack, -6265536),
			Items.LEATHER_HELMET,
			Items.LEATHER_CHESTPLATE,
			Items.LEATHER_LEGGINGS,
			Items.LEATHER_BOOTS,
			Items.LEATHER_HORSE_ARMOR
		);
		itemColors.register((itemStack, i) -> i != 1 ? -1 : DyedItemColor.getOrDefault(itemStack, 0), Items.WOLF_ARMOR);
		itemColors.register((itemStack, i) -> GrassColor.get(0.5, 1.0), Blocks.TALL_GRASS, Blocks.LARGE_FERN);
		itemColors.register((itemStack, i) -> {
			if (i != 1) {
				return -1;
			} else {
				FireworkExplosion fireworkExplosion = itemStack.get(DataComponents.FIREWORK_EXPLOSION);
				IntList intList = fireworkExplosion != null ? fireworkExplosion.colors() : IntList.of();
				int j = intList.size();
				if (j == 0) {
					return -7697782;
				} else if (j == 1) {
					return ARGB.opaque(intList.getInt(0));
				} else {
					int k = 0;
					int l = 0;
					int m = 0;

					for (int n = 0; n < j; n++) {
						int o = intList.getInt(n);
						k += ARGB.red(o);
						l += ARGB.green(o);
						m += ARGB.blue(o);
					}

					return ARGB.color(k / j, l / j, m / j);
				}
			}
		}, Items.FIREWORK_STAR);
		itemColors.register(
			(itemStack, i) -> i > 0 ? -1 : ARGB.opaque(itemStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor()),
			Items.POTION,
			Items.SPLASH_POTION,
			Items.LINGERING_POTION,
			Items.TIPPED_ARROW
		);

		for (SpawnEggItem spawnEggItem : SpawnEggItem.eggs()) {
			itemColors.register((itemStack, i) -> ARGB.opaque(spawnEggItem.getColor(i)), spawnEggItem);
		}

		itemColors.register(
			(itemStack, i) -> {
				BlockState blockState = ((BlockItem)itemStack.getItem()).getBlock().defaultBlockState();
				return blockColors.getColor(blockState, null, null, i);
			},
			Blocks.GRASS_BLOCK,
			Blocks.SHORT_GRASS,
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
		itemColors.register((itemStack, i) -> FoliageColor.getMangroveColor(), Blocks.MANGROVE_LEAVES);
		itemColors.register(
			(itemStack, i) -> i == 0 ? -1 : ARGB.opaque(itemStack.getOrDefault(DataComponents.MAP_COLOR, MapItemColor.DEFAULT).rgb()), Items.FILLED_MAP
		);
		return itemColors;
	}

	public int getColor(ItemStack itemStack, int i) {
		ItemColor itemColor = this.itemColors.byId(BuiltInRegistries.ITEM.getId(itemStack.getItem()));
		return itemColor == null ? -1 : itemColor.getColor(itemStack, i);
	}

	public void register(ItemColor itemColor, ItemLike... itemLikes) {
		for (ItemLike itemLike : itemLikes) {
			this.itemColors.addMapping(itemColor, Item.getId(itemLike.asItem()));
		}
	}
}
