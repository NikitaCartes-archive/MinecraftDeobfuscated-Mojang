package net.minecraft.world.entity.npc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.apache.commons.lang3.tuple.Pair;

public class VillagerTrades {
	private static final int DEFAULT_SUPPLY = 12;
	private static final int COMMON_ITEMS_SUPPLY = 16;
	private static final int UNCOMMON_ITEMS_SUPPLY = 3;
	private static final int XP_LEVEL_1_SELL = 1;
	private static final int XP_LEVEL_1_BUY = 2;
	private static final int XP_LEVEL_2_SELL = 5;
	private static final int XP_LEVEL_2_BUY = 10;
	private static final int XP_LEVEL_3_SELL = 10;
	private static final int XP_LEVEL_3_BUY = 20;
	private static final int XP_LEVEL_4_SELL = 15;
	private static final int XP_LEVEL_4_BUY = 30;
	private static final int XP_LEVEL_5_TRADE = 30;
	private static final float LOW_TIER_PRICE_MULTIPLIER = 0.05F;
	private static final float HIGH_TIER_PRICE_MULTIPLIER = 0.2F;
	public static final Map<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>> TRADES = Util.make(
		Maps.<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>>newHashMap(),
		hashMap -> {
			hashMap.put(
				VillagerProfession.FARMER,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.WHEAT, 20, 16, 2),
							new VillagerTrades.EmeraldForItems(Items.POTATO, 26, 16, 2),
							new VillagerTrades.EmeraldForItems(Items.CARROT, 22, 16, 2),
							new VillagerTrades.EmeraldForItems(Items.BEETROOT, 15, 16, 2),
							new VillagerTrades.ItemsForEmeralds(Items.BREAD, 1, 6, 16, 1)
						},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Blocks.PUMPKIN, 6, 12, 10),
							new VillagerTrades.ItemsForEmeralds(Items.PUMPKIN_PIE, 1, 4, 5),
							new VillagerTrades.ItemsForEmeralds(Items.APPLE, 1, 4, 16, 5)
						},
						3,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.ItemsForEmeralds(Items.COOKIE, 3, 18, 10), new VillagerTrades.EmeraldForItems(Blocks.MELON, 4, 12, 20)
						},
						4,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.ItemsForEmeralds(Blocks.CAKE, 1, 1, 12, 15),
							new VillagerTrades.SuspiciousStewForEmerald(MobEffects.NIGHT_VISION, 100, 15),
							new VillagerTrades.SuspiciousStewForEmerald(MobEffects.JUMP, 160, 15),
							new VillagerTrades.SuspiciousStewForEmerald(MobEffects.WEAKNESS, 140, 15),
							new VillagerTrades.SuspiciousStewForEmerald(MobEffects.BLINDNESS, 120, 15),
							new VillagerTrades.SuspiciousStewForEmerald(MobEffects.POISON, 280, 15),
							new VillagerTrades.SuspiciousStewForEmerald(MobEffects.SATURATION, 7, 15)
						},
						5,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.ItemsForEmeralds(Items.GOLDEN_CARROT, 3, 3, 30), new VillagerTrades.ItemsForEmeralds(Items.GLISTERING_MELON_SLICE, 4, 3, 30)
						}
					)
				)
			);
			hashMap.put(
				VillagerProfession.FISHERMAN,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.STRING, 20, 16, 2),
							new VillagerTrades.EmeraldForItems(Items.COAL, 10, 16, 2),
							new VillagerTrades.ItemsAndEmeraldsToItems(Items.COD, 6, 1, Items.COOKED_COD, 6, 16, 1, 0.05F),
							new VillagerTrades.ItemsForEmeralds(Items.COD_BUCKET, 3, 1, 16, 1)
						},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.COD, 15, 16, 10),
							new VillagerTrades.ItemsAndEmeraldsToItems(Items.SALMON, 6, 1, Items.COOKED_SALMON, 6, 16, 5, 0.05F),
							new VillagerTrades.ItemsForEmeralds(Items.CAMPFIRE, 2, 1, 5)
						},
						3,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.SALMON, 13, 16, 20), new VillagerTrades.EnchantedItemForEmeralds(Items.FISHING_ROD, 3, 3, 10, 0.2F)
						},
						4,
						new VillagerTrades.ItemListing[]{new VillagerTrades.EmeraldForItems(Items.TROPICAL_FISH, 6, 12, 30)},
						5,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.PUFFERFISH, 4, 12, 30),
							new VillagerTrades.EmeraldsForVillagerTypeItem(
								1,
								12,
								30,
								ImmutableMap.<VillagerType, Item>builder()
									.put(VillagerType.PLAINS, Items.OAK_BOAT)
									.put(VillagerType.TAIGA, Items.SPRUCE_BOAT)
									.put(VillagerType.SNOW, Items.SPRUCE_BOAT)
									.put(VillagerType.DESERT, Items.JUNGLE_BOAT)
									.put(VillagerType.JUNGLE, Items.JUNGLE_BOAT)
									.put(VillagerType.SAVANNA, Items.ACACIA_BOAT)
									.put(VillagerType.SWAMP, Items.DARK_OAK_BOAT)
									.build()
							)
						}
					)
				)
			);
			hashMap.put(
				VillagerProfession.SHEPHERD,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Blocks.WHITE_WOOL, 18, 16, 2),
							new VillagerTrades.EmeraldForItems(Blocks.BROWN_WOOL, 18, 16, 2),
							new VillagerTrades.EmeraldForItems(Blocks.BLACK_WOOL, 18, 16, 2),
							new VillagerTrades.EmeraldForItems(Blocks.GRAY_WOOL, 18, 16, 2),
							new VillagerTrades.ItemsForEmeralds(Items.SHEARS, 2, 1, 1)
						},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.WHITE_DYE, 12, 16, 10),
							new VillagerTrades.EmeraldForItems(Items.GRAY_DYE, 12, 16, 10),
							new VillagerTrades.EmeraldForItems(Items.BLACK_DYE, 12, 16, 10),
							new VillagerTrades.EmeraldForItems(Items.LIGHT_BLUE_DYE, 12, 16, 10),
							new VillagerTrades.EmeraldForItems(Items.LIME_DYE, 12, 16, 10),
							new VillagerTrades.ItemsForEmeralds(Blocks.WHITE_WOOL, 1, 1, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.ORANGE_WOOL, 1, 1, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.MAGENTA_WOOL, 1, 1, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.LIGHT_BLUE_WOOL, 1, 1, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.YELLOW_WOOL, 1, 1, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.LIME_WOOL, 1, 1, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.PINK_WOOL, 1, 1, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.GRAY_WOOL, 1, 1, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.LIGHT_GRAY_WOOL, 1, 1, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.CYAN_WOOL, 1, 1, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.PURPLE_WOOL, 1, 1, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.BLUE_WOOL, 1, 1, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.BROWN_WOOL, 1, 1, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.GREEN_WOOL, 1, 1, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.RED_WOOL, 1, 1, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.BLACK_WOOL, 1, 1, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.WHITE_CARPET, 1, 4, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.ORANGE_CARPET, 1, 4, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.MAGENTA_CARPET, 1, 4, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.LIGHT_BLUE_CARPET, 1, 4, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.YELLOW_CARPET, 1, 4, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.LIME_CARPET, 1, 4, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.PINK_CARPET, 1, 4, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.GRAY_CARPET, 1, 4, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.LIGHT_GRAY_CARPET, 1, 4, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.CYAN_CARPET, 1, 4, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.PURPLE_CARPET, 1, 4, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.BLUE_CARPET, 1, 4, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.BROWN_CARPET, 1, 4, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.GREEN_CARPET, 1, 4, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.RED_CARPET, 1, 4, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Blocks.BLACK_CARPET, 1, 4, 16, 5)
						},
						3,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.YELLOW_DYE, 12, 16, 20),
							new VillagerTrades.EmeraldForItems(Items.LIGHT_GRAY_DYE, 12, 16, 20),
							new VillagerTrades.EmeraldForItems(Items.ORANGE_DYE, 12, 16, 20),
							new VillagerTrades.EmeraldForItems(Items.RED_DYE, 12, 16, 20),
							new VillagerTrades.EmeraldForItems(Items.PINK_DYE, 12, 16, 20),
							new VillagerTrades.ItemsForEmeralds(Blocks.WHITE_BED, 3, 1, 12, 10),
							new VillagerTrades.ItemsForEmeralds(Blocks.YELLOW_BED, 3, 1, 12, 10),
							new VillagerTrades.ItemsForEmeralds(Blocks.RED_BED, 3, 1, 12, 10),
							new VillagerTrades.ItemsForEmeralds(Blocks.BLACK_BED, 3, 1, 12, 10),
							new VillagerTrades.ItemsForEmeralds(Blocks.BLUE_BED, 3, 1, 12, 10),
							new VillagerTrades.ItemsForEmeralds(Blocks.BROWN_BED, 3, 1, 12, 10),
							new VillagerTrades.ItemsForEmeralds(Blocks.CYAN_BED, 3, 1, 12, 10),
							new VillagerTrades.ItemsForEmeralds(Blocks.GRAY_BED, 3, 1, 12, 10),
							new VillagerTrades.ItemsForEmeralds(Blocks.GREEN_BED, 3, 1, 12, 10),
							new VillagerTrades.ItemsForEmeralds(Blocks.LIGHT_BLUE_BED, 3, 1, 12, 10),
							new VillagerTrades.ItemsForEmeralds(Blocks.LIGHT_GRAY_BED, 3, 1, 12, 10),
							new VillagerTrades.ItemsForEmeralds(Blocks.LIME_BED, 3, 1, 12, 10),
							new VillagerTrades.ItemsForEmeralds(Blocks.MAGENTA_BED, 3, 1, 12, 10),
							new VillagerTrades.ItemsForEmeralds(Blocks.ORANGE_BED, 3, 1, 12, 10),
							new VillagerTrades.ItemsForEmeralds(Blocks.PINK_BED, 3, 1, 12, 10),
							new VillagerTrades.ItemsForEmeralds(Blocks.PURPLE_BED, 3, 1, 12, 10)
						},
						4,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.BROWN_DYE, 12, 16, 30),
							new VillagerTrades.EmeraldForItems(Items.PURPLE_DYE, 12, 16, 30),
							new VillagerTrades.EmeraldForItems(Items.BLUE_DYE, 12, 16, 30),
							new VillagerTrades.EmeraldForItems(Items.GREEN_DYE, 12, 16, 30),
							new VillagerTrades.EmeraldForItems(Items.MAGENTA_DYE, 12, 16, 30),
							new VillagerTrades.EmeraldForItems(Items.CYAN_DYE, 12, 16, 30),
							new VillagerTrades.ItemsForEmeralds(Items.WHITE_BANNER, 3, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Items.BLUE_BANNER, 3, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Items.LIGHT_BLUE_BANNER, 3, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Items.RED_BANNER, 3, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Items.PINK_BANNER, 3, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Items.GREEN_BANNER, 3, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Items.LIME_BANNER, 3, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Items.GRAY_BANNER, 3, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Items.BLACK_BANNER, 3, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Items.PURPLE_BANNER, 3, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Items.MAGENTA_BANNER, 3, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Items.CYAN_BANNER, 3, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Items.BROWN_BANNER, 3, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Items.YELLOW_BANNER, 3, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Items.ORANGE_BANNER, 3, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Items.LIGHT_GRAY_BANNER, 3, 1, 12, 15)
						},
						5,
						new VillagerTrades.ItemListing[]{new VillagerTrades.ItemsForEmeralds(Items.PAINTING, 2, 3, 30)}
					)
				)
			);
			hashMap.put(
				VillagerProfession.FLETCHER,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.STICK, 32, 16, 2),
							new VillagerTrades.ItemsForEmeralds(Items.ARROW, 1, 16, 1),
							new VillagerTrades.ItemsAndEmeraldsToItems(Blocks.GRAVEL, 10, 1, Items.FLINT, 10, 12, 1, 0.05F)
						},
						2,
						new VillagerTrades.ItemListing[]{new VillagerTrades.EmeraldForItems(Items.FLINT, 26, 12, 10), new VillagerTrades.ItemsForEmeralds(Items.BOW, 2, 1, 5)},
						3,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.STRING, 14, 16, 20), new VillagerTrades.ItemsForEmeralds(Items.CROSSBOW, 3, 1, 10)
						},
						4,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.FEATHER, 24, 16, 30), new VillagerTrades.EnchantedItemForEmeralds(Items.BOW, 2, 3, 15)
						},
						5,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.TRIPWIRE_HOOK, 8, 12, 30),
							new VillagerTrades.EnchantedItemForEmeralds(Items.CROSSBOW, 3, 3, 15),
							new VillagerTrades.TippedArrowForItemsAndEmeralds(Items.ARROW, 5, Items.TIPPED_ARROW, 5, 2, 12, 30)
						}
					)
				)
			);
			hashMap.put(
				VillagerProfession.LIBRARIAN,
				toIntMap(
					ImmutableMap.<Integer, VillagerTrades.ItemListing[]>builder()
						.put(
							1,
							new VillagerTrades.ItemListing[]{
								new VillagerTrades.EmeraldForItems(Items.PAPER, 24, 16, 2),
								new VillagerTrades.EnchantBookForEmeralds(1),
								new VillagerTrades.ItemsForEmeralds(Blocks.BOOKSHELF, 9, 1, 12, 1)
							}
						)
						.put(
							2,
							new VillagerTrades.ItemListing[]{
								new VillagerTrades.EmeraldForItems(Items.BOOK, 4, 12, 10),
								new VillagerTrades.EnchantBookForEmeralds(5),
								new VillagerTrades.ItemsForEmeralds(Items.LANTERN, 1, 1, 5)
							}
						)
						.put(
							3,
							new VillagerTrades.ItemListing[]{
								new VillagerTrades.EmeraldForItems(Items.INK_SAC, 5, 12, 20),
								new VillagerTrades.EnchantBookForEmeralds(10),
								new VillagerTrades.ItemsForEmeralds(Items.GLASS, 1, 4, 10)
							}
						)
						.put(
							4,
							new VillagerTrades.ItemListing[]{
								new VillagerTrades.EmeraldForItems(Items.WRITABLE_BOOK, 2, 12, 30),
								new VillagerTrades.EnchantBookForEmeralds(15),
								new VillagerTrades.ItemsForEmeralds(Items.CLOCK, 5, 1, 15),
								new VillagerTrades.ItemsForEmeralds(Items.COMPASS, 4, 1, 15)
							}
						)
						.put(5, new VillagerTrades.ItemListing[]{new VillagerTrades.ItemsForEmeralds(Items.NAME_TAG, 20, 1, 30)})
						.build()
				)
			);
			hashMap.put(
				VillagerProfession.CARTOGRAPHER,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{new VillagerTrades.EmeraldForItems(Items.PAPER, 24, 16, 2), new VillagerTrades.ItemsForEmeralds(Items.MAP, 7, 1, 1)},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.GLASS_PANE, 11, 16, 10),
							new VillagerTrades.TreasureMapForEmeralds(13, StructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapDecorationTypes.OCEAN_MONUMENT, 12, 5)
						},
						3,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.COMPASS, 1, 12, 20),
							new VillagerTrades.TreasureMapForEmeralds(14, StructureTags.ON_WOODLAND_EXPLORER_MAPS, "filled_map.mansion", MapDecorationTypes.WOODLAND_MANSION, 12, 10),
							new VillagerTrades.TreasureMapForEmeralds(
								12, StructureTags.ON_TRIAL_CHAMBERS_MAPS, "filled_map.trial_chambers", MapDecorationTypes.TRIAL_CHAMBERS, 12, 10
							)
						},
						4,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.ItemsForEmeralds(Items.ITEM_FRAME, 7, 1, 15),
							new VillagerTrades.ItemsForEmeralds(Items.WHITE_BANNER, 3, 1, 15),
							new VillagerTrades.ItemsForEmeralds(Items.BLUE_BANNER, 3, 1, 15),
							new VillagerTrades.ItemsForEmeralds(Items.LIGHT_BLUE_BANNER, 3, 1, 15),
							new VillagerTrades.ItemsForEmeralds(Items.RED_BANNER, 3, 1, 15),
							new VillagerTrades.ItemsForEmeralds(Items.PINK_BANNER, 3, 1, 15),
							new VillagerTrades.ItemsForEmeralds(Items.GREEN_BANNER, 3, 1, 15),
							new VillagerTrades.ItemsForEmeralds(Items.LIME_BANNER, 3, 1, 15),
							new VillagerTrades.ItemsForEmeralds(Items.GRAY_BANNER, 3, 1, 15),
							new VillagerTrades.ItemsForEmeralds(Items.BLACK_BANNER, 3, 1, 15),
							new VillagerTrades.ItemsForEmeralds(Items.PURPLE_BANNER, 3, 1, 15),
							new VillagerTrades.ItemsForEmeralds(Items.MAGENTA_BANNER, 3, 1, 15),
							new VillagerTrades.ItemsForEmeralds(Items.CYAN_BANNER, 3, 1, 15),
							new VillagerTrades.ItemsForEmeralds(Items.BROWN_BANNER, 3, 1, 15),
							new VillagerTrades.ItemsForEmeralds(Items.YELLOW_BANNER, 3, 1, 15),
							new VillagerTrades.ItemsForEmeralds(Items.ORANGE_BANNER, 3, 1, 15),
							new VillagerTrades.ItemsForEmeralds(Items.LIGHT_GRAY_BANNER, 3, 1, 15)
						},
						5,
						new VillagerTrades.ItemListing[]{new VillagerTrades.ItemsForEmeralds(Items.GLOBE_BANNER_PATTERN, 8, 1, 30)}
					)
				)
			);
			hashMap.put(
				VillagerProfession.CLERIC,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.ROTTEN_FLESH, 32, 16, 2), new VillagerTrades.ItemsForEmeralds(Items.REDSTONE, 1, 2, 1)
						},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.GOLD_INGOT, 3, 12, 10), new VillagerTrades.ItemsForEmeralds(Items.LAPIS_LAZULI, 1, 1, 5)
						},
						3,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.RABBIT_FOOT, 2, 12, 20), new VillagerTrades.ItemsForEmeralds(Blocks.GLOWSTONE, 4, 1, 12, 10)
						},
						4,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.TURTLE_SCUTE, 4, 12, 30),
							new VillagerTrades.EmeraldForItems(Items.GLASS_BOTTLE, 9, 12, 30),
							new VillagerTrades.ItemsForEmeralds(Items.ENDER_PEARL, 5, 1, 15)
						},
						5,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.NETHER_WART, 22, 12, 30), new VillagerTrades.ItemsForEmeralds(Items.EXPERIENCE_BOTTLE, 3, 1, 30)
						}
					)
				)
			);
			hashMap.put(
				VillagerProfession.ARMORER,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.COAL, 15, 16, 2),
							new VillagerTrades.ItemsForEmeralds(new ItemStack(Items.IRON_LEGGINGS), 7, 1, 12, 1, 0.2F),
							new VillagerTrades.ItemsForEmeralds(new ItemStack(Items.IRON_BOOTS), 4, 1, 12, 1, 0.2F),
							new VillagerTrades.ItemsForEmeralds(new ItemStack(Items.IRON_HELMET), 5, 1, 12, 1, 0.2F),
							new VillagerTrades.ItemsForEmeralds(new ItemStack(Items.IRON_CHESTPLATE), 9, 1, 12, 1, 0.2F)
						},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.IRON_INGOT, 4, 12, 10),
							new VillagerTrades.ItemsForEmeralds(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2F),
							new VillagerTrades.ItemsForEmeralds(new ItemStack(Items.CHAINMAIL_BOOTS), 1, 1, 12, 5, 0.2F),
							new VillagerTrades.ItemsForEmeralds(new ItemStack(Items.CHAINMAIL_LEGGINGS), 3, 1, 12, 5, 0.2F)
						},
						3,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.LAVA_BUCKET, 1, 12, 20),
							new VillagerTrades.EmeraldForItems(Items.DIAMOND, 1, 12, 20),
							new VillagerTrades.ItemsForEmeralds(new ItemStack(Items.CHAINMAIL_HELMET), 1, 1, 12, 10, 0.2F),
							new VillagerTrades.ItemsForEmeralds(new ItemStack(Items.CHAINMAIL_CHESTPLATE), 4, 1, 12, 10, 0.2F),
							new VillagerTrades.ItemsForEmeralds(new ItemStack(Items.SHIELD), 5, 1, 12, 10, 0.2F)
						},
						4,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EnchantedItemForEmeralds(Items.DIAMOND_LEGGINGS, 14, 3, 15, 0.2F),
							new VillagerTrades.EnchantedItemForEmeralds(Items.DIAMOND_BOOTS, 8, 3, 15, 0.2F)
						},
						5,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EnchantedItemForEmeralds(Items.DIAMOND_HELMET, 8, 3, 30, 0.2F),
							new VillagerTrades.EnchantedItemForEmeralds(Items.DIAMOND_CHESTPLATE, 16, 3, 30, 0.2F)
						}
					)
				)
			);
			hashMap.put(
				VillagerProfession.WEAPONSMITH,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.COAL, 15, 16, 2),
							new VillagerTrades.ItemsForEmeralds(new ItemStack(Items.IRON_AXE), 3, 1, 12, 1, 0.2F),
							new VillagerTrades.EnchantedItemForEmeralds(Items.IRON_SWORD, 2, 3, 1)
						},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.IRON_INGOT, 4, 12, 10), new VillagerTrades.ItemsForEmeralds(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2F)
						},
						3,
						new VillagerTrades.ItemListing[]{new VillagerTrades.EmeraldForItems(Items.FLINT, 24, 12, 20)},
						4,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.DIAMOND, 1, 12, 30), new VillagerTrades.EnchantedItemForEmeralds(Items.DIAMOND_AXE, 12, 3, 15, 0.2F)
						},
						5,
						new VillagerTrades.ItemListing[]{new VillagerTrades.EnchantedItemForEmeralds(Items.DIAMOND_SWORD, 8, 3, 30, 0.2F)}
					)
				)
			);
			hashMap.put(
				VillagerProfession.TOOLSMITH,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.COAL, 15, 16, 2),
							new VillagerTrades.ItemsForEmeralds(new ItemStack(Items.STONE_AXE), 1, 1, 12, 1, 0.2F),
							new VillagerTrades.ItemsForEmeralds(new ItemStack(Items.STONE_SHOVEL), 1, 1, 12, 1, 0.2F),
							new VillagerTrades.ItemsForEmeralds(new ItemStack(Items.STONE_PICKAXE), 1, 1, 12, 1, 0.2F),
							new VillagerTrades.ItemsForEmeralds(new ItemStack(Items.STONE_HOE), 1, 1, 12, 1, 0.2F)
						},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.IRON_INGOT, 4, 12, 10), new VillagerTrades.ItemsForEmeralds(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2F)
						},
						3,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.FLINT, 30, 12, 20),
							new VillagerTrades.EnchantedItemForEmeralds(Items.IRON_AXE, 1, 3, 10, 0.2F),
							new VillagerTrades.EnchantedItemForEmeralds(Items.IRON_SHOVEL, 2, 3, 10, 0.2F),
							new VillagerTrades.EnchantedItemForEmeralds(Items.IRON_PICKAXE, 3, 3, 10, 0.2F),
							new VillagerTrades.ItemsForEmeralds(new ItemStack(Items.DIAMOND_HOE), 4, 1, 3, 10, 0.2F)
						},
						4,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.DIAMOND, 1, 12, 30),
							new VillagerTrades.EnchantedItemForEmeralds(Items.DIAMOND_AXE, 12, 3, 15, 0.2F),
							new VillagerTrades.EnchantedItemForEmeralds(Items.DIAMOND_SHOVEL, 5, 3, 15, 0.2F)
						},
						5,
						new VillagerTrades.ItemListing[]{new VillagerTrades.EnchantedItemForEmeralds(Items.DIAMOND_PICKAXE, 13, 3, 30, 0.2F)}
					)
				)
			);
			hashMap.put(
				VillagerProfession.BUTCHER,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.CHICKEN, 14, 16, 2),
							new VillagerTrades.EmeraldForItems(Items.PORKCHOP, 7, 16, 2),
							new VillagerTrades.EmeraldForItems(Items.RABBIT, 4, 16, 2),
							new VillagerTrades.ItemsForEmeralds(Items.RABBIT_STEW, 1, 1, 1)
						},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.COAL, 15, 16, 2),
							new VillagerTrades.ItemsForEmeralds(Items.COOKED_PORKCHOP, 1, 5, 16, 5),
							new VillagerTrades.ItemsForEmeralds(Items.COOKED_CHICKEN, 1, 8, 16, 5)
						},
						3,
						new VillagerTrades.ItemListing[]{new VillagerTrades.EmeraldForItems(Items.MUTTON, 7, 16, 20), new VillagerTrades.EmeraldForItems(Items.BEEF, 10, 16, 20)},
						4,
						new VillagerTrades.ItemListing[]{new VillagerTrades.EmeraldForItems(Items.DRIED_KELP_BLOCK, 10, 12, 30)},
						5,
						new VillagerTrades.ItemListing[]{new VillagerTrades.EmeraldForItems(Items.SWEET_BERRIES, 10, 12, 30)}
					)
				)
			);
			hashMap.put(
				VillagerProfession.LEATHERWORKER,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.LEATHER, 6, 16, 2),
							new VillagerTrades.DyedArmorForEmeralds(Items.LEATHER_LEGGINGS, 3),
							new VillagerTrades.DyedArmorForEmeralds(Items.LEATHER_CHESTPLATE, 7)
						},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.FLINT, 26, 12, 10),
							new VillagerTrades.DyedArmorForEmeralds(Items.LEATHER_HELMET, 5, 12, 5),
							new VillagerTrades.DyedArmorForEmeralds(Items.LEATHER_BOOTS, 4, 12, 5)
						},
						3,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.RABBIT_HIDE, 9, 12, 20), new VillagerTrades.DyedArmorForEmeralds(Items.LEATHER_CHESTPLATE, 7)
						},
						4,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.TURTLE_SCUTE, 4, 12, 30), new VillagerTrades.DyedArmorForEmeralds(Items.LEATHER_HORSE_ARMOR, 6, 12, 15)
						},
						5,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.ItemsForEmeralds(new ItemStack(Items.SADDLE), 6, 1, 12, 30, 0.2F),
							new VillagerTrades.DyedArmorForEmeralds(Items.LEATHER_HELMET, 5, 12, 30)
						}
					)
				)
			);
			hashMap.put(
				VillagerProfession.MASON,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.CLAY_BALL, 10, 16, 2), new VillagerTrades.ItemsForEmeralds(Items.BRICK, 1, 10, 16, 1)
						},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Blocks.STONE, 20, 16, 10), new VillagerTrades.ItemsForEmeralds(Blocks.CHISELED_STONE_BRICKS, 1, 4, 16, 5)
						},
						3,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Blocks.GRANITE, 16, 16, 20),
							new VillagerTrades.EmeraldForItems(Blocks.ANDESITE, 16, 16, 20),
							new VillagerTrades.EmeraldForItems(Blocks.DIORITE, 16, 16, 20),
							new VillagerTrades.ItemsForEmeralds(Blocks.DRIPSTONE_BLOCK, 1, 4, 16, 10),
							new VillagerTrades.ItemsForEmeralds(Blocks.POLISHED_ANDESITE, 1, 4, 16, 10),
							new VillagerTrades.ItemsForEmeralds(Blocks.POLISHED_DIORITE, 1, 4, 16, 10),
							new VillagerTrades.ItemsForEmeralds(Blocks.POLISHED_GRANITE, 1, 4, 16, 10)
						},
						4,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.QUARTZ, 12, 12, 30),
							new VillagerTrades.ItemsForEmeralds(Blocks.ORANGE_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.WHITE_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.BLUE_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.LIGHT_BLUE_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.GRAY_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.LIGHT_GRAY_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.BLACK_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.RED_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.PINK_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.MAGENTA_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.LIME_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.GREEN_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.CYAN_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.PURPLE_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.YELLOW_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.BROWN_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.ORANGE_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.WHITE_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.BLUE_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.GRAY_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.BLACK_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.RED_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.PINK_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.MAGENTA_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.LIME_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.GREEN_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.CYAN_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.PURPLE_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.YELLOW_GLAZED_TERRACOTTA, 1, 1, 12, 15),
							new VillagerTrades.ItemsForEmeralds(Blocks.BROWN_GLAZED_TERRACOTTA, 1, 1, 12, 15)
						},
						5,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.ItemsForEmeralds(Blocks.QUARTZ_PILLAR, 1, 1, 12, 30), new VillagerTrades.ItemsForEmeralds(Blocks.QUARTZ_BLOCK, 1, 1, 12, 30)
						}
					)
				)
			);
		}
	);
	public static final Int2ObjectMap<VillagerTrades.ItemListing[]> WANDERING_TRADER_TRADES = toIntMap(
		ImmutableMap.of(
			1,
			new VillagerTrades.ItemListing[]{
				new VillagerTrades.ItemsForEmeralds(Items.SEA_PICKLE, 2, 1, 5, 1),
				new VillagerTrades.ItemsForEmeralds(Items.SLIME_BALL, 4, 1, 5, 1),
				new VillagerTrades.ItemsForEmeralds(Items.GLOWSTONE, 2, 1, 5, 1),
				new VillagerTrades.ItemsForEmeralds(Items.NAUTILUS_SHELL, 5, 1, 5, 1),
				new VillagerTrades.ItemsForEmeralds(Items.FERN, 1, 1, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.SUGAR_CANE, 1, 1, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Items.PUMPKIN, 1, 1, 4, 1),
				new VillagerTrades.ItemsForEmeralds(Items.KELP, 3, 1, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.CACTUS, 3, 1, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Items.DANDELION, 1, 1, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.POPPY, 1, 1, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.BLUE_ORCHID, 1, 1, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Items.ALLIUM, 1, 1, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.AZURE_BLUET, 1, 1, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.RED_TULIP, 1, 1, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.ORANGE_TULIP, 1, 1, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.WHITE_TULIP, 1, 1, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.PINK_TULIP, 1, 1, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.OXEYE_DAISY, 1, 1, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.CORNFLOWER, 1, 1, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.LILY_OF_THE_VALLEY, 1, 1, 7, 1),
				new VillagerTrades.ItemsForEmeralds(Items.WHEAT_SEEDS, 1, 1, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.BEETROOT_SEEDS, 1, 1, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.PUMPKIN_SEEDS, 1, 1, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.MELON_SEEDS, 1, 1, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.ACACIA_SAPLING, 5, 1, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Items.BIRCH_SAPLING, 5, 1, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Items.DARK_OAK_SAPLING, 5, 1, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Items.JUNGLE_SAPLING, 5, 1, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Items.OAK_SAPLING, 5, 1, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Items.SPRUCE_SAPLING, 5, 1, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Items.CHERRY_SAPLING, 5, 1, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Items.MANGROVE_PROPAGULE, 5, 1, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Items.RED_DYE, 1, 3, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.WHITE_DYE, 1, 3, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.BLUE_DYE, 1, 3, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.PINK_DYE, 1, 3, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.BLACK_DYE, 1, 3, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.GREEN_DYE, 1, 3, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.LIGHT_GRAY_DYE, 1, 3, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.MAGENTA_DYE, 1, 3, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.YELLOW_DYE, 1, 3, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.GRAY_DYE, 1, 3, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.PURPLE_DYE, 1, 3, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.LIGHT_BLUE_DYE, 1, 3, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.LIME_DYE, 1, 3, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.ORANGE_DYE, 1, 3, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.BROWN_DYE, 1, 3, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.CYAN_DYE, 1, 3, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.BRAIN_CORAL_BLOCK, 3, 1, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Items.BUBBLE_CORAL_BLOCK, 3, 1, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Items.FIRE_CORAL_BLOCK, 3, 1, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Items.HORN_CORAL_BLOCK, 3, 1, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Items.TUBE_CORAL_BLOCK, 3, 1, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Items.VINE, 1, 1, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.BROWN_MUSHROOM, 1, 1, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.RED_MUSHROOM, 1, 1, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Items.LILY_PAD, 1, 2, 5, 1),
				new VillagerTrades.ItemsForEmeralds(Items.SMALL_DRIPLEAF, 1, 2, 5, 1),
				new VillagerTrades.ItemsForEmeralds(Items.SAND, 1, 8, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Items.RED_SAND, 1, 4, 6, 1),
				new VillagerTrades.ItemsForEmeralds(Items.POINTED_DRIPSTONE, 1, 2, 5, 1),
				new VillagerTrades.ItemsForEmeralds(Items.ROOTED_DIRT, 1, 2, 5, 1),
				new VillagerTrades.ItemsForEmeralds(Items.MOSS_BLOCK, 1, 2, 5, 1)
			},
			2,
			new VillagerTrades.ItemListing[]{
				new VillagerTrades.ItemsForEmeralds(Items.TROPICAL_FISH_BUCKET, 5, 1, 4, 1),
				new VillagerTrades.ItemsForEmeralds(Items.PUFFERFISH_BUCKET, 5, 1, 4, 1),
				new VillagerTrades.ItemsForEmeralds(Items.PACKED_ICE, 3, 1, 6, 1),
				new VillagerTrades.ItemsForEmeralds(Items.BLUE_ICE, 6, 1, 6, 1),
				new VillagerTrades.ItemsForEmeralds(Items.GUNPOWDER, 1, 1, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Items.PODZOL, 3, 3, 6, 1)
			}
		)
	);
	private static final VillagerTrades.TreasureMapForEmeralds DESERT_MAP = new VillagerTrades.TreasureMapForEmeralds(
		8, StructureTags.ON_DESERT_VILLAGE_MAPS, "filled_map.village_desert", MapDecorationTypes.DESERT_VILLAGE, 12, 5
	);
	private static final VillagerTrades.TreasureMapForEmeralds SAVANNA_MAP = new VillagerTrades.TreasureMapForEmeralds(
		8, StructureTags.ON_SAVANNA_VILLAGE_MAPS, "filled_map.village_savanna", MapDecorationTypes.SAVANNA_VILLAGE, 12, 5
	);
	private static final VillagerTrades.TreasureMapForEmeralds PLAINS_MAP = new VillagerTrades.TreasureMapForEmeralds(
		8, StructureTags.ON_PLAINS_VILLAGE_MAPS, "filled_map.village_plains", MapDecorationTypes.PLAINS_VILLAGE, 12, 5
	);
	private static final VillagerTrades.TreasureMapForEmeralds TAIGA_MAP = new VillagerTrades.TreasureMapForEmeralds(
		8, StructureTags.ON_TAIGA_VILLAGE_MAPS, "filled_map.village_taiga", MapDecorationTypes.TAIGA_VILLAGE, 12, 5
	);
	private static final VillagerTrades.TreasureMapForEmeralds SNOWY_MAP = new VillagerTrades.TreasureMapForEmeralds(
		8, StructureTags.ON_SNOWY_VILLAGE_MAPS, "filled_map.village_snowy", MapDecorationTypes.SNOWY_VILLAGE, 12, 5
	);
	private static final VillagerTrades.TreasureMapForEmeralds JUNGLE_MAP = new VillagerTrades.TreasureMapForEmeralds(
		8, StructureTags.ON_JUNGLE_EXPLORER_MAPS, "filled_map.explorer_jungle", MapDecorationTypes.JUNGLE_TEMPLE, 12, 5
	);
	private static final VillagerTrades.TreasureMapForEmeralds SWAMP_MAP = new VillagerTrades.TreasureMapForEmeralds(
		8, StructureTags.ON_SWAMP_EXPLORER_MAPS, "filled_map.explorer_swamp", MapDecorationTypes.SWAMP_HUT, 12, 5
	);
	public static final Map<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>> EXPERIMENTAL_TRADES = Map.of(
		VillagerProfession.LIBRARIAN,
		toIntMap(
			ImmutableMap.<Integer, VillagerTrades.ItemListing[]>builder()
				.put(
					1,
					new VillagerTrades.ItemListing[]{
						new VillagerTrades.EmeraldForItems(Items.PAPER, 24, 16, 2), commonBooks(1), new VillagerTrades.ItemsForEmeralds(Blocks.BOOKSHELF, 9, 1, 12, 1)
					}
				)
				.put(
					2,
					new VillagerTrades.ItemListing[]{
						new VillagerTrades.EmeraldForItems(Items.BOOK, 4, 12, 10), commonBooks(5), new VillagerTrades.ItemsForEmeralds(Items.LANTERN, 1, 1, 5)
					}
				)
				.put(
					3,
					new VillagerTrades.ItemListing[]{
						new VillagerTrades.EmeraldForItems(Items.INK_SAC, 5, 12, 20), commonBooks(10), new VillagerTrades.ItemsForEmeralds(Items.GLASS, 1, 4, 10)
					}
				)
				.put(
					4,
					new VillagerTrades.ItemListing[]{
						new VillagerTrades.EmeraldForItems(Items.WRITABLE_BOOK, 2, 12, 30),
						new VillagerTrades.ItemsForEmeralds(Items.CLOCK, 5, 1, 15),
						new VillagerTrades.ItemsForEmeralds(Items.COMPASS, 4, 1, 15)
					}
				)
				.put(5, new VillagerTrades.ItemListing[]{specialBooks(), new VillagerTrades.ItemsForEmeralds(Items.NAME_TAG, 20, 1, 30)})
				.build()
		),
		VillagerProfession.ARMORER,
		toIntMap(
			ImmutableMap.<Integer, VillagerTrades.ItemListing[]>builder()
				.put(
					1,
					new VillagerTrades.ItemListing[]{
						new VillagerTrades.EmeraldForItems(Items.COAL, 15, 12, 2), new VillagerTrades.EmeraldForItems(Items.IRON_INGOT, 5, 12, 2)
					}
				)
				.put(
					2,
					new VillagerTrades.ItemListing[]{
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(Items.IRON_BOOTS, 4, 1, 12, 5, 0.05F),
							VillagerType.DESERT,
							VillagerType.PLAINS,
							VillagerType.SAVANNA,
							VillagerType.SNOW,
							VillagerType.TAIGA
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(Items.CHAINMAIL_BOOTS, 4, 1, 12, 5, 0.05F), VillagerType.JUNGLE, VillagerType.SWAMP
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(Items.IRON_HELMET, 5, 1, 12, 5, 0.05F),
							VillagerType.DESERT,
							VillagerType.PLAINS,
							VillagerType.SAVANNA,
							VillagerType.SNOW,
							VillagerType.TAIGA
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(Items.CHAINMAIL_HELMET, 5, 1, 12, 5, 0.05F), VillagerType.JUNGLE, VillagerType.SWAMP
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(Items.IRON_LEGGINGS, 7, 1, 12, 5, 0.05F),
							VillagerType.DESERT,
							VillagerType.PLAINS,
							VillagerType.SAVANNA,
							VillagerType.SNOW,
							VillagerType.TAIGA
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(Items.CHAINMAIL_LEGGINGS, 7, 1, 12, 5, 0.05F), VillagerType.JUNGLE, VillagerType.SWAMP
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(Items.IRON_CHESTPLATE, 9, 1, 12, 5, 0.05F),
							VillagerType.DESERT,
							VillagerType.PLAINS,
							VillagerType.SAVANNA,
							VillagerType.SNOW,
							VillagerType.TAIGA
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(Items.CHAINMAIL_CHESTPLATE, 9, 1, 12, 5, 0.05F), VillagerType.JUNGLE, VillagerType.SWAMP
						)
					}
				)
				.put(
					3,
					new VillagerTrades.ItemListing[]{
						new VillagerTrades.EmeraldForItems(Items.LAVA_BUCKET, 1, 12, 20),
						new VillagerTrades.ItemsForEmeralds(Items.SHIELD, 5, 1, 12, 10, 0.05F),
						new VillagerTrades.ItemsForEmeralds(Items.BELL, 36, 1, 12, 10, 0.2F)
					}
				)
				.put(
					4,
					new VillagerTrades.ItemListing[]{
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.IRON_BOOTS, Enchantments.THORNS, 1), 8, 1, 3, 15, 0.05F), VillagerType.DESERT
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.IRON_HELMET, Enchantments.THORNS, 1), 9, 1, 3, 15, 0.05F), VillagerType.DESERT
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.IRON_LEGGINGS, Enchantments.THORNS, 1), 11, 1, 3, 15, 0.05F), VillagerType.DESERT
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.IRON_CHESTPLATE, Enchantments.THORNS, 1), 13, 1, 3, 15, 0.05F), VillagerType.DESERT
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.IRON_BOOTS, Enchantments.PROTECTION, 1), 8, 1, 3, 15, 0.05F), VillagerType.PLAINS
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.IRON_HELMET, Enchantments.PROTECTION, 1), 9, 1, 3, 15, 0.05F), VillagerType.PLAINS
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.IRON_LEGGINGS, Enchantments.PROTECTION, 1), 11, 1, 3, 15, 0.05F), VillagerType.PLAINS
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.IRON_CHESTPLATE, Enchantments.PROTECTION, 1), 13, 1, 3, 15, 0.05F), VillagerType.PLAINS
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.IRON_BOOTS, Enchantments.BINDING_CURSE, 1), 2, 1, 3, 15, 0.05F), VillagerType.SAVANNA
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.IRON_HELMET, Enchantments.BINDING_CURSE, 1), 3, 1, 3, 15, 0.05F), VillagerType.SAVANNA
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.IRON_LEGGINGS, Enchantments.BINDING_CURSE, 1), 5, 1, 3, 15, 0.05F), VillagerType.SAVANNA
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.IRON_CHESTPLATE, Enchantments.BINDING_CURSE, 1), 7, 1, 3, 15, 0.05F), VillagerType.SAVANNA
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.IRON_BOOTS, Enchantments.FROST_WALKER, 1), 8, 1, 3, 15, 0.05F), VillagerType.SNOW
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.IRON_HELMET, Enchantments.AQUA_AFFINITY, 1), 9, 1, 3, 15, 0.05F), VillagerType.SNOW
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.CHAINMAIL_BOOTS, Enchantments.UNBREAKING, 1), 8, 1, 3, 15, 0.05F), VillagerType.JUNGLE
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.CHAINMAIL_HELMET, Enchantments.UNBREAKING, 1), 9, 1, 3, 15, 0.05F), VillagerType.JUNGLE
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.CHAINMAIL_LEGGINGS, Enchantments.UNBREAKING, 1), 11, 1, 3, 15, 0.05F), VillagerType.JUNGLE
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.CHAINMAIL_CHESTPLATE, Enchantments.UNBREAKING, 1), 13, 1, 3, 15, 0.05F), VillagerType.JUNGLE
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.CHAINMAIL_BOOTS, Enchantments.MENDING, 1), 8, 1, 3, 15, 0.05F), VillagerType.SWAMP
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.CHAINMAIL_HELMET, Enchantments.MENDING, 1), 9, 1, 3, 15, 0.05F), VillagerType.SWAMP
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.CHAINMAIL_LEGGINGS, Enchantments.MENDING, 1), 11, 1, 3, 15, 0.05F), VillagerType.SWAMP
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.CHAINMAIL_CHESTPLATE, Enchantments.MENDING, 1), 13, 1, 3, 15, 0.05F), VillagerType.SWAMP
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsAndEmeraldsToItems(Items.DIAMOND_BOOTS, 1, 4, Items.DIAMOND_LEGGINGS, 1, 3, 15, 0.05F), VillagerType.TAIGA
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsAndEmeraldsToItems(Items.DIAMOND_LEGGINGS, 1, 4, Items.DIAMOND_CHESTPLATE, 1, 3, 15, 0.05F), VillagerType.TAIGA
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsAndEmeraldsToItems(Items.DIAMOND_HELMET, 1, 4, Items.DIAMOND_BOOTS, 1, 3, 15, 0.05F), VillagerType.TAIGA
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsAndEmeraldsToItems(Items.DIAMOND_CHESTPLATE, 1, 2, Items.DIAMOND_HELMET, 1, 3, 15, 0.05F), VillagerType.TAIGA
						)
					}
				)
				.put(
					5,
					new VillagerTrades.ItemListing[]{
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsAndEmeraldsToItems(Items.DIAMOND, 4, 16, enchant(Items.DIAMOND_CHESTPLATE, Enchantments.THORNS, 1), 1, 3, 30, 0.05F),
							VillagerType.DESERT
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsAndEmeraldsToItems(Items.DIAMOND, 3, 16, enchant(Items.DIAMOND_LEGGINGS, Enchantments.THORNS, 1), 1, 3, 30, 0.05F),
							VillagerType.DESERT
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsAndEmeraldsToItems(Items.DIAMOND, 3, 16, enchant(Items.DIAMOND_LEGGINGS, Enchantments.PROTECTION, 1), 1, 3, 30, 0.05F),
							VillagerType.PLAINS
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsAndEmeraldsToItems(Items.DIAMOND, 2, 12, enchant(Items.DIAMOND_BOOTS, Enchantments.PROTECTION, 1), 1, 3, 30, 0.05F),
							VillagerType.PLAINS
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsAndEmeraldsToItems(Items.DIAMOND, 2, 6, enchant(Items.DIAMOND_HELMET, Enchantments.BINDING_CURSE, 1), 1, 3, 30, 0.05F),
							VillagerType.SAVANNA
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsAndEmeraldsToItems(Items.DIAMOND, 3, 8, enchant(Items.DIAMOND_CHESTPLATE, Enchantments.BINDING_CURSE, 1), 1, 3, 30, 0.05F),
							VillagerType.SAVANNA
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsAndEmeraldsToItems(Items.DIAMOND, 2, 12, enchant(Items.DIAMOND_BOOTS, Enchantments.FROST_WALKER, 1), 1, 3, 30, 0.05F),
							VillagerType.SNOW
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsAndEmeraldsToItems(Items.DIAMOND, 3, 12, enchant(Items.DIAMOND_HELMET, Enchantments.AQUA_AFFINITY, 1), 1, 3, 30, 0.05F),
							VillagerType.SNOW
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.CHAINMAIL_HELMET, Enchantments.PROJECTILE_PROTECTION, 1), 9, 1, 3, 30, 0.05F), VillagerType.JUNGLE
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.CHAINMAIL_BOOTS, Enchantments.FEATHER_FALLING, 1), 8, 1, 3, 30, 0.05F), VillagerType.JUNGLE
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.CHAINMAIL_HELMET, Enchantments.RESPIRATION, 1), 9, 1, 3, 30, 0.05F), VillagerType.SWAMP
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsForEmeralds(enchant(Items.CHAINMAIL_BOOTS, Enchantments.DEPTH_STRIDER, 1), 8, 1, 3, 30, 0.05F), VillagerType.SWAMP
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsAndEmeraldsToItems(Items.DIAMOND, 4, 18, enchant(Items.DIAMOND_CHESTPLATE, Enchantments.BLAST_PROTECTION, 1), 1, 3, 30, 0.05F),
							VillagerType.TAIGA
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.ItemsAndEmeraldsToItems(Items.DIAMOND, 3, 18, enchant(Items.DIAMOND_LEGGINGS, Enchantments.BLAST_PROTECTION, 1), 1, 3, 30, 0.05F),
							VillagerType.TAIGA
						),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(new VillagerTrades.EmeraldForItems(Items.DIAMOND_BLOCK, 1, 12, 30, 42), VillagerType.TAIGA),
						VillagerTrades.TypeSpecificTrade.oneTradeInBiomes(
							new VillagerTrades.EmeraldForItems(Items.IRON_BLOCK, 1, 12, 30, 4),
							VillagerType.DESERT,
							VillagerType.JUNGLE,
							VillagerType.PLAINS,
							VillagerType.SAVANNA,
							VillagerType.SNOW,
							VillagerType.SWAMP
						)
					}
				)
				.build()
		),
		VillagerProfession.CARTOGRAPHER,
		toIntMap(
			ImmutableMap.of(
				1,
				new VillagerTrades.ItemListing[]{new VillagerTrades.EmeraldForItems(Items.PAPER, 24, 16, 2), new VillagerTrades.ItemsForEmeralds(Items.MAP, 7, 1, 1)},
				2,
				new VillagerTrades.ItemListing[]{
					new VillagerTrades.EmeraldForItems(Items.GLASS_PANE, 11, 16, 10),
					new VillagerTrades.TypeSpecificTrade(
						ImmutableMap.<VillagerType, VillagerTrades.ItemListing>builder()
							.put(VillagerType.DESERT, SAVANNA_MAP)
							.put(VillagerType.SAVANNA, PLAINS_MAP)
							.put(VillagerType.PLAINS, TAIGA_MAP)
							.put(VillagerType.TAIGA, SNOWY_MAP)
							.put(VillagerType.SNOW, PLAINS_MAP)
							.put(VillagerType.JUNGLE, SAVANNA_MAP)
							.put(VillagerType.SWAMP, SNOWY_MAP)
							.build()
					),
					new VillagerTrades.TypeSpecificTrade(
						ImmutableMap.<VillagerType, VillagerTrades.ItemListing>builder()
							.put(VillagerType.DESERT, PLAINS_MAP)
							.put(VillagerType.SAVANNA, DESERT_MAP)
							.put(VillagerType.PLAINS, SAVANNA_MAP)
							.put(VillagerType.TAIGA, PLAINS_MAP)
							.put(VillagerType.SNOW, TAIGA_MAP)
							.put(VillagerType.JUNGLE, DESERT_MAP)
							.put(VillagerType.SWAMP, TAIGA_MAP)
							.build()
					),
					new VillagerTrades.TypeSpecificTrade(
						ImmutableMap.<VillagerType, VillagerTrades.ItemListing>builder()
							.put(VillagerType.DESERT, JUNGLE_MAP)
							.put(VillagerType.SAVANNA, JUNGLE_MAP)
							.put(VillagerType.PLAINS, new VillagerTrades.FailureItemListing())
							.put(VillagerType.TAIGA, SWAMP_MAP)
							.put(VillagerType.SNOW, SWAMP_MAP)
							.put(VillagerType.JUNGLE, SWAMP_MAP)
							.put(VillagerType.SWAMP, JUNGLE_MAP)
							.build()
					)
				},
				3,
				new VillagerTrades.ItemListing[]{
					new VillagerTrades.EmeraldForItems(Items.COMPASS, 1, 12, 20),
					new VillagerTrades.TreasureMapForEmeralds(13, StructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapDecorationTypes.OCEAN_MONUMENT, 12, 10),
					new VillagerTrades.TreasureMapForEmeralds(12, StructureTags.ON_TRIAL_CHAMBERS_MAPS, "filled_map.trial_chambers", MapDecorationTypes.TRIAL_CHAMBERS, 12, 10)
				},
				4,
				new VillagerTrades.ItemListing[]{
					new VillagerTrades.ItemsForEmeralds(Items.ITEM_FRAME, 7, 1, 15),
					new VillagerTrades.ItemsForEmeralds(Items.WHITE_BANNER, 3, 1, 15),
					new VillagerTrades.ItemsForEmeralds(Items.BLUE_BANNER, 3, 1, 15),
					new VillagerTrades.ItemsForEmeralds(Items.LIGHT_BLUE_BANNER, 3, 1, 15),
					new VillagerTrades.ItemsForEmeralds(Items.RED_BANNER, 3, 1, 15),
					new VillagerTrades.ItemsForEmeralds(Items.PINK_BANNER, 3, 1, 15),
					new VillagerTrades.ItemsForEmeralds(Items.GREEN_BANNER, 3, 1, 15),
					new VillagerTrades.ItemsForEmeralds(Items.LIME_BANNER, 3, 1, 15),
					new VillagerTrades.ItemsForEmeralds(Items.GRAY_BANNER, 3, 1, 15),
					new VillagerTrades.ItemsForEmeralds(Items.BLACK_BANNER, 3, 1, 15),
					new VillagerTrades.ItemsForEmeralds(Items.PURPLE_BANNER, 3, 1, 15),
					new VillagerTrades.ItemsForEmeralds(Items.MAGENTA_BANNER, 3, 1, 15),
					new VillagerTrades.ItemsForEmeralds(Items.CYAN_BANNER, 3, 1, 15),
					new VillagerTrades.ItemsForEmeralds(Items.BROWN_BANNER, 3, 1, 15),
					new VillagerTrades.ItemsForEmeralds(Items.YELLOW_BANNER, 3, 1, 15),
					new VillagerTrades.ItemsForEmeralds(Items.ORANGE_BANNER, 3, 1, 15),
					new VillagerTrades.ItemsForEmeralds(Items.LIGHT_GRAY_BANNER, 3, 1, 15)
				},
				5,
				new VillagerTrades.ItemListing[]{
					new VillagerTrades.ItemsForEmeralds(Items.GLOBE_BANNER_PATTERN, 8, 1, 30),
					new VillagerTrades.TreasureMapForEmeralds(14, StructureTags.ON_WOODLAND_EXPLORER_MAPS, "filled_map.mansion", MapDecorationTypes.WOODLAND_MANSION, 1, 30)
				}
			)
		)
	);
	public static final List<Pair<VillagerTrades.ItemListing[], Integer>> EXPERIMENTAL_WANDERING_TRADER_TRADES = ImmutableList.<Pair<VillagerTrades.ItemListing[], Integer>>builder()
		.add(
			Pair.of(
				new VillagerTrades.ItemListing[]{
					new VillagerTrades.EmeraldForItems(potionCost(Potions.WATER), 1, 1, 1),
					new VillagerTrades.EmeraldForItems(Items.WATER_BUCKET, 1, 1, 1, 2),
					new VillagerTrades.EmeraldForItems(Items.MILK_BUCKET, 1, 1, 1, 2),
					new VillagerTrades.EmeraldForItems(Items.FERMENTED_SPIDER_EYE, 1, 1, 1, 3),
					new VillagerTrades.EmeraldForItems(Items.BAKED_POTATO, 4, 1, 1),
					new VillagerTrades.EmeraldForItems(Items.HAY_BLOCK, 1, 1, 1)
				},
				2
			)
		)
		.add(
			Pair.of(
				new VillagerTrades.ItemListing[]{
					new VillagerTrades.ItemsForEmeralds(Items.PACKED_ICE, 1, 1, 6, 1),
					new VillagerTrades.ItemsForEmeralds(Items.BLUE_ICE, 6, 1, 6, 1),
					new VillagerTrades.ItemsForEmeralds(Items.GUNPOWDER, 1, 4, 2, 1),
					new VillagerTrades.ItemsForEmeralds(Items.PODZOL, 3, 3, 6, 1),
					new VillagerTrades.ItemsForEmeralds(Blocks.ACACIA_LOG, 1, 8, 4, 1),
					new VillagerTrades.ItemsForEmeralds(Blocks.BIRCH_LOG, 1, 8, 4, 1),
					new VillagerTrades.ItemsForEmeralds(Blocks.DARK_OAK_LOG, 1, 8, 4, 1),
					new VillagerTrades.ItemsForEmeralds(Blocks.JUNGLE_LOG, 1, 8, 4, 1),
					new VillagerTrades.ItemsForEmeralds(Blocks.OAK_LOG, 1, 8, 4, 1),
					new VillagerTrades.ItemsForEmeralds(Blocks.SPRUCE_LOG, 1, 8, 4, 1),
					new VillagerTrades.ItemsForEmeralds(Blocks.CHERRY_LOG, 1, 8, 4, 1),
					new VillagerTrades.EnchantedItemForEmeralds(Items.IRON_PICKAXE, 1, 1, 1, 0.2F),
					new VillagerTrades.ItemsForEmeralds(potion(Potions.LONG_INVISIBILITY), 5, 1, 1, 1)
				},
				2
			)
		)
		.add(
			Pair.of(
				new VillagerTrades.ItemListing[]{
					new VillagerTrades.ItemsForEmeralds(Items.TROPICAL_FISH_BUCKET, 3, 1, 4, 1),
					new VillagerTrades.ItemsForEmeralds(Items.PUFFERFISH_BUCKET, 3, 1, 4, 1),
					new VillagerTrades.ItemsForEmeralds(Items.SEA_PICKLE, 2, 1, 5, 1),
					new VillagerTrades.ItemsForEmeralds(Items.SLIME_BALL, 4, 1, 5, 1),
					new VillagerTrades.ItemsForEmeralds(Items.GLOWSTONE, 2, 1, 5, 1),
					new VillagerTrades.ItemsForEmeralds(Items.NAUTILUS_SHELL, 5, 1, 5, 1),
					new VillagerTrades.ItemsForEmeralds(Items.FERN, 1, 1, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.SUGAR_CANE, 1, 1, 8, 1),
					new VillagerTrades.ItemsForEmeralds(Items.PUMPKIN, 1, 1, 4, 1),
					new VillagerTrades.ItemsForEmeralds(Items.KELP, 3, 1, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.CACTUS, 3, 1, 8, 1),
					new VillagerTrades.ItemsForEmeralds(Items.DANDELION, 1, 1, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.POPPY, 1, 1, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.BLUE_ORCHID, 1, 1, 8, 1),
					new VillagerTrades.ItemsForEmeralds(Items.ALLIUM, 1, 1, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.AZURE_BLUET, 1, 1, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.RED_TULIP, 1, 1, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.ORANGE_TULIP, 1, 1, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.WHITE_TULIP, 1, 1, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.PINK_TULIP, 1, 1, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.OXEYE_DAISY, 1, 1, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.CORNFLOWER, 1, 1, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.LILY_OF_THE_VALLEY, 1, 1, 7, 1),
					new VillagerTrades.ItemsForEmeralds(Items.WHEAT_SEEDS, 1, 1, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.BEETROOT_SEEDS, 1, 1, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.PUMPKIN_SEEDS, 1, 1, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.MELON_SEEDS, 1, 1, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.ACACIA_SAPLING, 5, 1, 8, 1),
					new VillagerTrades.ItemsForEmeralds(Items.BIRCH_SAPLING, 5, 1, 8, 1),
					new VillagerTrades.ItemsForEmeralds(Items.DARK_OAK_SAPLING, 5, 1, 8, 1),
					new VillagerTrades.ItemsForEmeralds(Items.JUNGLE_SAPLING, 5, 1, 8, 1),
					new VillagerTrades.ItemsForEmeralds(Items.OAK_SAPLING, 5, 1, 8, 1),
					new VillagerTrades.ItemsForEmeralds(Items.SPRUCE_SAPLING, 5, 1, 8, 1),
					new VillagerTrades.ItemsForEmeralds(Items.CHERRY_SAPLING, 5, 1, 8, 1),
					new VillagerTrades.ItemsForEmeralds(Items.MANGROVE_PROPAGULE, 5, 1, 8, 1),
					new VillagerTrades.ItemsForEmeralds(Items.RED_DYE, 1, 3, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.WHITE_DYE, 1, 3, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.BLUE_DYE, 1, 3, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.PINK_DYE, 1, 3, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.BLACK_DYE, 1, 3, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.GREEN_DYE, 1, 3, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.LIGHT_GRAY_DYE, 1, 3, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.MAGENTA_DYE, 1, 3, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.YELLOW_DYE, 1, 3, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.GRAY_DYE, 1, 3, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.PURPLE_DYE, 1, 3, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.LIGHT_BLUE_DYE, 1, 3, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.LIME_DYE, 1, 3, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.ORANGE_DYE, 1, 3, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.BROWN_DYE, 1, 3, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.CYAN_DYE, 1, 3, 12, 1),
					new VillagerTrades.ItemsForEmeralds(Items.BRAIN_CORAL_BLOCK, 3, 1, 8, 1),
					new VillagerTrades.ItemsForEmeralds(Items.BUBBLE_CORAL_BLOCK, 3, 1, 8, 1),
					new VillagerTrades.ItemsForEmeralds(Items.FIRE_CORAL_BLOCK, 3, 1, 8, 1),
					new VillagerTrades.ItemsForEmeralds(Items.HORN_CORAL_BLOCK, 3, 1, 8, 1),
					new VillagerTrades.ItemsForEmeralds(Items.TUBE_CORAL_BLOCK, 3, 1, 8, 1),
					new VillagerTrades.ItemsForEmeralds(Items.VINE, 1, 3, 4, 1),
					new VillagerTrades.ItemsForEmeralds(Items.BROWN_MUSHROOM, 1, 3, 4, 1),
					new VillagerTrades.ItemsForEmeralds(Items.RED_MUSHROOM, 1, 3, 4, 1),
					new VillagerTrades.ItemsForEmeralds(Items.LILY_PAD, 1, 5, 2, 1),
					new VillagerTrades.ItemsForEmeralds(Items.SMALL_DRIPLEAF, 1, 2, 5, 1),
					new VillagerTrades.ItemsForEmeralds(Items.SAND, 1, 8, 8, 1),
					new VillagerTrades.ItemsForEmeralds(Items.RED_SAND, 1, 4, 6, 1),
					new VillagerTrades.ItemsForEmeralds(Items.POINTED_DRIPSTONE, 1, 2, 5, 1),
					new VillagerTrades.ItemsForEmeralds(Items.ROOTED_DIRT, 1, 2, 5, 1),
					new VillagerTrades.ItemsForEmeralds(Items.MOSS_BLOCK, 1, 2, 5, 1)
				},
				5
			)
		)
		.build();

	private static VillagerTrades.ItemListing commonBooks(int i) {
		return new VillagerTrades.TypeSpecificTrade(
			ImmutableMap.<VillagerType, VillagerTrades.ItemListing>builder()
				.put(VillagerType.DESERT, new VillagerTrades.EnchantBookForEmeralds(i, Enchantments.FIRE_PROTECTION, Enchantments.THORNS, Enchantments.INFINITY))
				.put(
					VillagerType.JUNGLE, new VillagerTrades.EnchantBookForEmeralds(i, Enchantments.FEATHER_FALLING, Enchantments.PROJECTILE_PROTECTION, Enchantments.POWER)
				)
				.put(VillagerType.PLAINS, new VillagerTrades.EnchantBookForEmeralds(i, Enchantments.PUNCH, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS))
				.put(VillagerType.SAVANNA, new VillagerTrades.EnchantBookForEmeralds(i, Enchantments.KNOCKBACK, Enchantments.BINDING_CURSE, Enchantments.SWEEPING_EDGE))
				.put(VillagerType.SNOW, new VillagerTrades.EnchantBookForEmeralds(i, Enchantments.AQUA_AFFINITY, Enchantments.LOOTING, Enchantments.FROST_WALKER))
				.put(VillagerType.SWAMP, new VillagerTrades.EnchantBookForEmeralds(i, Enchantments.DEPTH_STRIDER, Enchantments.RESPIRATION, Enchantments.VANISHING_CURSE))
				.put(VillagerType.TAIGA, new VillagerTrades.EnchantBookForEmeralds(i, Enchantments.BLAST_PROTECTION, Enchantments.FIRE_ASPECT, Enchantments.FLAME))
				.build()
		);
	}

	private static VillagerTrades.ItemListing specialBooks() {
		return new VillagerTrades.TypeSpecificTrade(
			ImmutableMap.<VillagerType, VillagerTrades.ItemListing>builder()
				.put(VillagerType.DESERT, new VillagerTrades.EnchantBookForEmeralds(30, 3, 3, Enchantments.EFFICIENCY))
				.put(VillagerType.JUNGLE, new VillagerTrades.EnchantBookForEmeralds(30, 2, 2, Enchantments.UNBREAKING))
				.put(VillagerType.PLAINS, new VillagerTrades.EnchantBookForEmeralds(30, 3, 3, Enchantments.PROTECTION))
				.put(VillagerType.SAVANNA, new VillagerTrades.EnchantBookForEmeralds(30, 3, 3, Enchantments.SHARPNESS))
				.put(VillagerType.SNOW, new VillagerTrades.EnchantBookForEmeralds(30, Enchantments.SILK_TOUCH))
				.put(VillagerType.SWAMP, new VillagerTrades.EnchantBookForEmeralds(30, Enchantments.MENDING))
				.put(VillagerType.TAIGA, new VillagerTrades.EnchantBookForEmeralds(30, 2, 2, Enchantments.FORTUNE))
				.build()
		);
	}

	private static Int2ObjectMap<VillagerTrades.ItemListing[]> toIntMap(ImmutableMap<Integer, VillagerTrades.ItemListing[]> immutableMap) {
		return new Int2ObjectOpenHashMap<>(immutableMap);
	}

	private static ItemCost potionCost(Holder<Potion> holder) {
		return new ItemCost(Items.POTION).withComponents(builder -> builder.expect(DataComponents.POTION_CONTENTS, new PotionContents(holder)));
	}

	private static ItemStack potion(Holder<Potion> holder) {
		return PotionContents.createItemStack(Items.POTION, holder);
	}

	private static ItemStack enchant(Item item, Enchantment enchantment, int i) {
		ItemStack itemStack = new ItemStack(item);
		itemStack.enchant(enchantment, i);
		return itemStack;
	}

	static class DyedArmorForEmeralds implements VillagerTrades.ItemListing {
		private final Item item;
		private final int value;
		private final int maxUses;
		private final int villagerXp;

		public DyedArmorForEmeralds(Item item, int i) {
			this(item, i, 12, 1);
		}

		public DyedArmorForEmeralds(Item item, int i, int j, int k) {
			this.item = item;
			this.value = i;
			this.maxUses = j;
			this.villagerXp = k;
		}

		@Override
		public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
			ItemCost itemCost = new ItemCost(Items.EMERALD, this.value);
			ItemStack itemStack = new ItemStack(this.item);
			if (itemStack.is(ItemTags.DYEABLE)) {
				List<DyeItem> list = Lists.<DyeItem>newArrayList();
				list.add(getRandomDye(randomSource));
				if (randomSource.nextFloat() > 0.7F) {
					list.add(getRandomDye(randomSource));
				}

				if (randomSource.nextFloat() > 0.8F) {
					list.add(getRandomDye(randomSource));
				}

				itemStack = DyedItemColor.applyDyes(itemStack, list);
			}

			return new MerchantOffer(itemCost, itemStack, this.maxUses, this.villagerXp, 0.2F);
		}

		private static DyeItem getRandomDye(RandomSource randomSource) {
			return DyeItem.byColor(DyeColor.byId(randomSource.nextInt(16)));
		}
	}

	static class EmeraldForItems implements VillagerTrades.ItemListing {
		private final ItemCost itemStack;
		private final int maxUses;
		private final int villagerXp;
		private final int emeraldAmount;
		private final float priceMultiplier;

		public EmeraldForItems(ItemLike itemLike, int i, int j, int k) {
			this(itemLike, i, j, k, 1);
		}

		public EmeraldForItems(ItemLike itemLike, int i, int j, int k, int l) {
			this(new ItemCost(itemLike.asItem(), i), j, k, l);
		}

		public EmeraldForItems(ItemCost itemCost, int i, int j, int k) {
			this.itemStack = itemCost;
			this.maxUses = i;
			this.villagerXp = j;
			this.emeraldAmount = k;
			this.priceMultiplier = 0.05F;
		}

		@Override
		public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
			return new MerchantOffer(this.itemStack, new ItemStack(Items.EMERALD, this.emeraldAmount), this.maxUses, this.villagerXp, this.priceMultiplier);
		}
	}

	static class EmeraldsForVillagerTypeItem implements VillagerTrades.ItemListing {
		private final Map<VillagerType, Item> trades;
		private final int cost;
		private final int maxUses;
		private final int villagerXp;

		public EmeraldsForVillagerTypeItem(int i, int j, int k, Map<VillagerType, Item> map) {
			BuiltInRegistries.VILLAGER_TYPE.stream().filter(villagerType -> !map.containsKey(villagerType)).findAny().ifPresent(villagerType -> {
				throw new IllegalStateException("Missing trade for villager type: " + BuiltInRegistries.VILLAGER_TYPE.getKey(villagerType));
			});
			this.trades = map;
			this.cost = i;
			this.maxUses = j;
			this.villagerXp = k;
		}

		@Nullable
		@Override
		public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
			if (entity instanceof VillagerDataHolder villagerDataHolder) {
				ItemCost itemCost = new ItemCost((ItemLike)this.trades.get(villagerDataHolder.getVillagerData().getType()), this.cost);
				return new MerchantOffer(itemCost, new ItemStack(Items.EMERALD), this.maxUses, this.villagerXp, 0.05F);
			} else {
				return null;
			}
		}
	}

	static class EnchantBookForEmeralds implements VillagerTrades.ItemListing {
		private final int villagerXp;
		private final List<Enchantment> tradeableEnchantments;
		private final int minLevel;
		private final int maxLevel;

		public EnchantBookForEmeralds(int i) {
			this(i, (Enchantment[])BuiltInRegistries.ENCHANTMENT.stream().filter(Enchantment::isTradeable).toArray(Enchantment[]::new));
		}

		public EnchantBookForEmeralds(int i, Enchantment... enchantments) {
			this(i, 0, Integer.MAX_VALUE, enchantments);
		}

		public EnchantBookForEmeralds(int i, int j, int k, Enchantment... enchantments) {
			this.minLevel = j;
			this.maxLevel = k;
			this.villagerXp = i;
			this.tradeableEnchantments = Arrays.asList(enchantments);
		}

		@Override
		public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
			Enchantment enchantment = (Enchantment)this.tradeableEnchantments.get(randomSource.nextInt(this.tradeableEnchantments.size()));
			int i = Math.max(enchantment.getMinLevel(), this.minLevel);
			int j = Math.min(enchantment.getMaxLevel(), this.maxLevel);
			int k = Mth.nextInt(randomSource, i, j);
			ItemStack itemStack = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, k));
			int l = 2 + randomSource.nextInt(5 + k * 10) + 3 * k;
			if (enchantment.isTreasureOnly()) {
				l *= 2;
			}

			if (l > 64) {
				l = 64;
			}

			return new MerchantOffer(new ItemCost(Items.EMERALD, l), Optional.of(new ItemCost(Items.BOOK)), itemStack, 12, this.villagerXp, 0.2F);
		}
	}

	static class EnchantedItemForEmeralds implements VillagerTrades.ItemListing {
		private final ItemStack itemStack;
		private final int baseEmeraldCost;
		private final int maxUses;
		private final int villagerXp;
		private final float priceMultiplier;

		public EnchantedItemForEmeralds(Item item, int i, int j, int k) {
			this(item, i, j, k, 0.05F);
		}

		public EnchantedItemForEmeralds(Item item, int i, int j, int k, float f) {
			this.itemStack = new ItemStack(item);
			this.baseEmeraldCost = i;
			this.maxUses = j;
			this.villagerXp = k;
			this.priceMultiplier = f;
		}

		@Override
		public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
			int i = 5 + randomSource.nextInt(15);
			ItemStack itemStack = EnchantmentHelper.enchantItem(entity.level().enabledFeatures(), randomSource, new ItemStack(this.itemStack.getItem()), i, false);
			int j = Math.min(this.baseEmeraldCost + i, 64);
			ItemCost itemCost = new ItemCost(Items.EMERALD, j);
			return new MerchantOffer(itemCost, itemStack, this.maxUses, this.villagerXp, this.priceMultiplier);
		}
	}

	static class FailureItemListing implements VillagerTrades.ItemListing {
		@Override
		public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
			return null;
		}
	}

	public interface ItemListing {
		@Nullable
		MerchantOffer getOffer(Entity entity, RandomSource randomSource);
	}

	static class ItemsAndEmeraldsToItems implements VillagerTrades.ItemListing {
		private final ItemCost fromItem;
		private final int emeraldCost;
		private final ItemStack toItem;
		private final int maxUses;
		private final int villagerXp;
		private final float priceMultiplier;

		public ItemsAndEmeraldsToItems(ItemLike itemLike, int i, int j, Item item, int k, int l, int m, float f) {
			this(itemLike, i, j, new ItemStack(item), k, l, m, f);
		}

		ItemsAndEmeraldsToItems(ItemLike itemLike, int i, int j, ItemStack itemStack, int k, int l, int m, float f) {
			this(new ItemCost(itemLike, i), j, itemStack.copyWithCount(k), l, m, f);
		}

		public ItemsAndEmeraldsToItems(ItemCost itemCost, int i, ItemStack itemStack, int j, int k, float f) {
			this.fromItem = itemCost;
			this.emeraldCost = i;
			this.toItem = itemStack;
			this.maxUses = j;
			this.villagerXp = k;
			this.priceMultiplier = f;
		}

		@Nullable
		@Override
		public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
			return new MerchantOffer(
				new ItemCost(Items.EMERALD, this.emeraldCost), Optional.of(this.fromItem), this.toItem.copy(), 0, this.maxUses, this.villagerXp, this.priceMultiplier, 0
			);
		}
	}

	static class ItemsForEmeralds implements VillagerTrades.ItemListing {
		private final ItemStack itemStack;
		private final int emeraldCost;
		private final int maxUses;
		private final int villagerXp;
		private final float priceMultiplier;

		public ItemsForEmeralds(Block block, int i, int j, int k, int l) {
			this(new ItemStack(block), i, j, k, l);
		}

		public ItemsForEmeralds(Item item, int i, int j, int k) {
			this(new ItemStack(item), i, j, 12, k);
		}

		public ItemsForEmeralds(Item item, int i, int j, int k, int l) {
			this(new ItemStack(item), i, j, k, l);
		}

		public ItemsForEmeralds(ItemStack itemStack, int i, int j, int k, int l) {
			this(itemStack, i, j, k, l, 0.05F);
		}

		public ItemsForEmeralds(Item item, int i, int j, int k, int l, float f) {
			this(new ItemStack(item), i, j, k, l, f);
		}

		public ItemsForEmeralds(ItemStack itemStack, int i, int j, int k, int l, float f) {
			this.itemStack = itemStack;
			this.emeraldCost = i;
			this.itemStack.setCount(j);
			this.maxUses = k;
			this.villagerXp = l;
			this.priceMultiplier = f;
		}

		@Override
		public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
			return new MerchantOffer(new ItemCost(Items.EMERALD, this.emeraldCost), this.itemStack.copy(), this.maxUses, this.villagerXp, this.priceMultiplier);
		}
	}

	static class SuspiciousStewForEmerald implements VillagerTrades.ItemListing {
		private final SuspiciousStewEffects effects;
		private final int xp;
		private final float priceMultiplier;

		public SuspiciousStewForEmerald(Holder<MobEffect> holder, int i, int j) {
			this(new SuspiciousStewEffects(List.of(new SuspiciousStewEffects.Entry(holder, i))), j, 0.05F);
		}

		public SuspiciousStewForEmerald(SuspiciousStewEffects suspiciousStewEffects, int i, float f) {
			this.effects = suspiciousStewEffects;
			this.xp = i;
			this.priceMultiplier = f;
		}

		@Nullable
		@Override
		public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
			ItemStack itemStack = new ItemStack(Items.SUSPICIOUS_STEW, 1);
			itemStack.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, this.effects);
			return new MerchantOffer(new ItemCost(Items.EMERALD), itemStack, 12, this.xp, this.priceMultiplier);
		}
	}

	static class TippedArrowForItemsAndEmeralds implements VillagerTrades.ItemListing {
		private final ItemStack toItem;
		private final int toCount;
		private final int emeraldCost;
		private final int maxUses;
		private final int villagerXp;
		private final Item fromItem;
		private final int fromCount;
		private final float priceMultiplier;

		public TippedArrowForItemsAndEmeralds(Item item, int i, Item item2, int j, int k, int l, int m) {
			this.toItem = new ItemStack(item2);
			this.emeraldCost = k;
			this.maxUses = l;
			this.villagerXp = m;
			this.fromItem = item;
			this.fromCount = i;
			this.toCount = j;
			this.priceMultiplier = 0.05F;
		}

		@Override
		public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
			ItemCost itemCost = new ItemCost(Items.EMERALD, this.emeraldCost);
			List<Holder<Potion>> list = (List<Holder<Potion>>)BuiltInRegistries.POTION
				.holders()
				.filter(reference -> !((Potion)reference.value()).getEffects().isEmpty() && PotionBrewing.isBrewablePotion(reference))
				.collect(Collectors.toList());
			Holder<Potion> holder = Util.getRandom(list, randomSource);
			ItemStack itemStack = new ItemStack(this.toItem.getItem(), this.toCount);
			itemStack.set(DataComponents.POTION_CONTENTS, new PotionContents(holder));
			return new MerchantOffer(itemCost, Optional.of(new ItemCost(this.fromItem, this.fromCount)), itemStack, this.maxUses, this.villagerXp, this.priceMultiplier);
		}
	}

	static class TreasureMapForEmeralds implements VillagerTrades.ItemListing {
		private final int emeraldCost;
		private final TagKey<Structure> destination;
		private final String displayName;
		private final Holder<MapDecorationType> destinationType;
		private final int maxUses;
		private final int villagerXp;

		public TreasureMapForEmeralds(int i, TagKey<Structure> tagKey, String string, Holder<MapDecorationType> holder, int j, int k) {
			this.emeraldCost = i;
			this.destination = tagKey;
			this.displayName = string;
			this.destinationType = holder;
			this.maxUses = j;
			this.villagerXp = k;
		}

		@Nullable
		@Override
		public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
			if (!(entity.level() instanceof ServerLevel)) {
				return null;
			} else {
				ServerLevel serverLevel = (ServerLevel)entity.level();
				BlockPos blockPos = serverLevel.findNearestMapStructure(this.destination, entity.blockPosition(), 100, true);
				if (blockPos != null) {
					ItemStack itemStack = MapItem.create(serverLevel, blockPos.getX(), blockPos.getZ(), (byte)2, true, true);
					MapItem.renderBiomePreviewMap(serverLevel, itemStack);
					MapItemSavedData.addTargetDecoration(itemStack, blockPos, "+", this.destinationType);
					itemStack.set(DataComponents.ITEM_NAME, Component.translatable(this.displayName));
					return new MerchantOffer(
						new ItemCost(Items.EMERALD, this.emeraldCost), Optional.of(new ItemCost(Items.COMPASS)), itemStack, this.maxUses, this.villagerXp, 0.2F
					);
				} else {
					return null;
				}
			}
		}
	}

	static record TypeSpecificTrade(Map<VillagerType, VillagerTrades.ItemListing> trades) implements VillagerTrades.ItemListing {
		public static VillagerTrades.TypeSpecificTrade oneTradeInBiomes(VillagerTrades.ItemListing itemListing, VillagerType... villagerTypes) {
			return new VillagerTrades.TypeSpecificTrade(
				(Map<VillagerType, VillagerTrades.ItemListing>)Arrays.stream(villagerTypes)
					.collect(Collectors.toMap(villagerType -> villagerType, villagerType -> itemListing))
			);
		}

		@Nullable
		@Override
		public MerchantOffer getOffer(Entity entity, RandomSource randomSource) {
			if (entity instanceof VillagerDataHolder villagerDataHolder) {
				VillagerType villagerType = villagerDataHolder.getVillagerData().getType();
				VillagerTrades.ItemListing itemListing = (VillagerTrades.ItemListing)this.trades.get(villagerType);
				return itemListing == null ? null : itemListing.getOffer(entity, randomSource);
			} else {
				return null;
			}
		}
	}
}
