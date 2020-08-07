package net.minecraft.world.entity.npc;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class VillagerTrades {
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
							new VillagerTrades.SuspisciousStewForEmerald(MobEffects.NIGHT_VISION, 100, 15),
							new VillagerTrades.SuspisciousStewForEmerald(MobEffects.JUMP, 160, 15),
							new VillagerTrades.SuspisciousStewForEmerald(MobEffects.WEAKNESS, 140, 15),
							new VillagerTrades.SuspisciousStewForEmerald(MobEffects.BLINDNESS, 120, 15),
							new VillagerTrades.SuspisciousStewForEmerald(MobEffects.POISON, 280, 15),
							new VillagerTrades.SuspisciousStewForEmerald(MobEffects.SATURATION, 7, 15)
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
							new VillagerTrades.ItemsAndEmeraldsToItems(Items.COD, 6, Items.COOKED_COD, 6, 16, 1),
							new VillagerTrades.ItemsForEmeralds(Items.COD_BUCKET, 3, 1, 16, 1)
						},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.COD, 15, 16, 10),
							new VillagerTrades.ItemsAndEmeraldsToItems(Items.SALMON, 6, Items.COOKED_SALMON, 6, 16, 5),
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
							new VillagerTrades.ItemsAndEmeraldsToItems(Blocks.GRAVEL, 10, Items.FLINT, 10, 12, 1)
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
							new VillagerTrades.TreasureMapForEmeralds(13, StructureFeature.OCEAN_MONUMENT, MapDecoration.Type.MONUMENT, 12, 5)
						},
						3,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Items.COMPASS, 1, 12, 20),
							new VillagerTrades.TreasureMapForEmeralds(14, StructureFeature.WOODLAND_MANSION, MapDecoration.Type.MANSION, 12, 10)
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
						new VillagerTrades.ItemListing[]{new VillagerTrades.ItemsForEmeralds(Items.GLOBE_BANNER_PATTER, 8, 1, 30)}
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
							new VillagerTrades.EmeraldForItems(Items.SCUTE, 4, 12, 30),
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
							new VillagerTrades.EmeraldForItems(Items.SCUTE, 4, 12, 30), new VillagerTrades.DyedArmorForEmeralds(Items.LEATHER_HORSE_ARMOR, 6, 12, 15)
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
				new VillagerTrades.ItemsForEmeralds(Items.SAND, 1, 8, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Items.RED_SAND, 1, 4, 6, 1)
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

	private static Int2ObjectMap<VillagerTrades.ItemListing[]> toIntMap(ImmutableMap<Integer, VillagerTrades.ItemListing[]> immutableMap) {
		return new Int2ObjectOpenHashMap<>(immutableMap);
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
		public MerchantOffer getOffer(Entity entity, Random random) {
			ItemStack itemStack = new ItemStack(Items.EMERALD, this.value);
			ItemStack itemStack2 = new ItemStack(this.item);
			if (this.item instanceof DyeableArmorItem) {
				List<DyeItem> list = Lists.<DyeItem>newArrayList();
				list.add(getRandomDye(random));
				if (random.nextFloat() > 0.7F) {
					list.add(getRandomDye(random));
				}

				if (random.nextFloat() > 0.8F) {
					list.add(getRandomDye(random));
				}

				itemStack2 = DyeableLeatherItem.dyeArmor(itemStack2, list);
			}

			return new MerchantOffer(itemStack, itemStack2, this.maxUses, this.villagerXp, 0.2F);
		}

		private static DyeItem getRandomDye(Random random) {
			return DyeItem.byColor(DyeColor.byId(random.nextInt(16)));
		}
	}

	static class EmeraldForItems implements VillagerTrades.ItemListing {
		private final Item item;
		private final int cost;
		private final int maxUses;
		private final int villagerXp;
		private final float priceMultiplier;

		public EmeraldForItems(ItemLike itemLike, int i, int j, int k) {
			this.item = itemLike.asItem();
			this.cost = i;
			this.maxUses = j;
			this.villagerXp = k;
			this.priceMultiplier = 0.05F;
		}

		@Override
		public MerchantOffer getOffer(Entity entity, Random random) {
			ItemStack itemStack = new ItemStack(this.item, this.cost);
			return new MerchantOffer(itemStack, new ItemStack(Items.EMERALD), this.maxUses, this.villagerXp, this.priceMultiplier);
		}
	}

	static class EmeraldsForVillagerTypeItem implements VillagerTrades.ItemListing {
		private final Map<VillagerType, Item> trades;
		private final int cost;
		private final int maxUses;
		private final int villagerXp;

		public EmeraldsForVillagerTypeItem(int i, int j, int k, Map<VillagerType, Item> map) {
			Registry.VILLAGER_TYPE.stream().filter(villagerType -> !map.containsKey(villagerType)).findAny().ifPresent(villagerType -> {
				throw new IllegalStateException("Missing trade for villager type: " + Registry.VILLAGER_TYPE.getKey(villagerType));
			});
			this.trades = map;
			this.cost = i;
			this.maxUses = j;
			this.villagerXp = k;
		}

		@Nullable
		@Override
		public MerchantOffer getOffer(Entity entity, Random random) {
			if (entity instanceof VillagerDataHolder) {
				ItemStack itemStack = new ItemStack((ItemLike)this.trades.get(((VillagerDataHolder)entity).getVillagerData().getType()), this.cost);
				return new MerchantOffer(itemStack, new ItemStack(Items.EMERALD), this.maxUses, this.villagerXp, 0.05F);
			} else {
				return null;
			}
		}
	}

	static class EnchantBookForEmeralds implements VillagerTrades.ItemListing {
		private final int villagerXp;

		public EnchantBookForEmeralds(int i) {
			this.villagerXp = i;
		}

		@Override
		public MerchantOffer getOffer(Entity entity, Random random) {
			List<Enchantment> list = (List<Enchantment>)Registry.ENCHANTMENT.stream().filter(Enchantment::isTradeable).collect(Collectors.toList());
			Enchantment enchantment = (Enchantment)list.get(random.nextInt(list.size()));
			int i = Mth.nextInt(random, enchantment.getMinLevel(), enchantment.getMaxLevel());
			ItemStack itemStack = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, i));
			int j = 2 + random.nextInt(5 + i * 10) + 3 * i;
			if (enchantment.isTreasureOnly()) {
				j *= 2;
			}

			if (j > 64) {
				j = 64;
			}

			return new MerchantOffer(new ItemStack(Items.EMERALD, j), new ItemStack(Items.BOOK), itemStack, 12, this.villagerXp, 0.2F);
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
		public MerchantOffer getOffer(Entity entity, Random random) {
			int i = 5 + random.nextInt(15);
			ItemStack itemStack = EnchantmentHelper.enchantItem(random, new ItemStack(this.itemStack.getItem()), i, false);
			int j = Math.min(this.baseEmeraldCost + i, 64);
			ItemStack itemStack2 = new ItemStack(Items.EMERALD, j);
			return new MerchantOffer(itemStack2, itemStack, this.maxUses, this.villagerXp, this.priceMultiplier);
		}
	}

	public interface ItemListing {
		@Nullable
		MerchantOffer getOffer(Entity entity, Random random);
	}

	static class ItemsAndEmeraldsToItems implements VillagerTrades.ItemListing {
		private final ItemStack fromItem;
		private final int fromCount;
		private final int emeraldCost;
		private final ItemStack toItem;
		private final int toCount;
		private final int maxUses;
		private final int villagerXp;
		private final float priceMultiplier;

		public ItemsAndEmeraldsToItems(ItemLike itemLike, int i, Item item, int j, int k, int l) {
			this(itemLike, i, 1, item, j, k, l);
		}

		public ItemsAndEmeraldsToItems(ItemLike itemLike, int i, int j, Item item, int k, int l, int m) {
			this.fromItem = new ItemStack(itemLike);
			this.fromCount = i;
			this.emeraldCost = j;
			this.toItem = new ItemStack(item);
			this.toCount = k;
			this.maxUses = l;
			this.villagerXp = m;
			this.priceMultiplier = 0.05F;
		}

		@Nullable
		@Override
		public MerchantOffer getOffer(Entity entity, Random random) {
			return new MerchantOffer(
				new ItemStack(Items.EMERALD, this.emeraldCost),
				new ItemStack(this.fromItem.getItem(), this.fromCount),
				new ItemStack(this.toItem.getItem(), this.toCount),
				this.maxUses,
				this.villagerXp,
				this.priceMultiplier
			);
		}
	}

	static class ItemsForEmeralds implements VillagerTrades.ItemListing {
		private final ItemStack itemStack;
		private final int emeraldCost;
		private final int numberOfItems;
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

		public ItemsForEmeralds(ItemStack itemStack, int i, int j, int k, int l, float f) {
			this.itemStack = itemStack;
			this.emeraldCost = i;
			this.numberOfItems = j;
			this.maxUses = k;
			this.villagerXp = l;
			this.priceMultiplier = f;
		}

		@Override
		public MerchantOffer getOffer(Entity entity, Random random) {
			return new MerchantOffer(
				new ItemStack(Items.EMERALD, this.emeraldCost),
				new ItemStack(this.itemStack.getItem(), this.numberOfItems),
				this.maxUses,
				this.villagerXp,
				this.priceMultiplier
			);
		}
	}

	static class SuspisciousStewForEmerald implements VillagerTrades.ItemListing {
		final MobEffect effect;
		final int duration;
		final int xp;
		private final float priceMultiplier;

		public SuspisciousStewForEmerald(MobEffect mobEffect, int i, int j) {
			this.effect = mobEffect;
			this.duration = i;
			this.xp = j;
			this.priceMultiplier = 0.05F;
		}

		@Nullable
		@Override
		public MerchantOffer getOffer(Entity entity, Random random) {
			ItemStack itemStack = new ItemStack(Items.SUSPICIOUS_STEW, 1);
			SuspiciousStewItem.saveMobEffect(itemStack, this.effect, this.duration);
			return new MerchantOffer(new ItemStack(Items.EMERALD, 1), itemStack, 12, this.xp, this.priceMultiplier);
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
		public MerchantOffer getOffer(Entity entity, Random random) {
			ItemStack itemStack = new ItemStack(Items.EMERALD, this.emeraldCost);
			List<Potion> list = (List<Potion>)Registry.POTION
				.stream()
				.filter(potionx -> !potionx.getEffects().isEmpty() && PotionBrewing.isBrewablePotion(potionx))
				.collect(Collectors.toList());
			Potion potion = (Potion)list.get(random.nextInt(list.size()));
			ItemStack itemStack2 = PotionUtils.setPotion(new ItemStack(this.toItem.getItem(), this.toCount), potion);
			return new MerchantOffer(itemStack, new ItemStack(this.fromItem, this.fromCount), itemStack2, this.maxUses, this.villagerXp, this.priceMultiplier);
		}
	}

	static class TreasureMapForEmeralds implements VillagerTrades.ItemListing {
		private final int emeraldCost;
		private final StructureFeature<?> destination;
		private final MapDecoration.Type destinationType;
		private final int maxUses;
		private final int villagerXp;

		public TreasureMapForEmeralds(int i, StructureFeature<?> structureFeature, MapDecoration.Type type, int j, int k) {
			this.emeraldCost = i;
			this.destination = structureFeature;
			this.destinationType = type;
			this.maxUses = j;
			this.villagerXp = k;
		}

		@Nullable
		@Override
		public MerchantOffer getOffer(Entity entity, Random random) {
			if (!(entity.level instanceof ServerLevel)) {
				return null;
			} else {
				ServerLevel serverLevel = (ServerLevel)entity.level;
				BlockPos blockPos = serverLevel.findNearestMapFeature(this.destination, entity.blockPosition(), 100, true);
				if (blockPos != null) {
					ItemStack itemStack = MapItem.create(serverLevel, blockPos.getX(), blockPos.getZ(), (byte)2, true, true);
					MapItem.renderBiomePreviewMap(serverLevel, itemStack);
					MapItemSavedData.addTargetDecoration(itemStack, blockPos, "+", this.destinationType);
					itemStack.setHoverName(new TranslatableComponent("filled_map." + this.destination.getFeatureName().toLowerCase(Locale.ROOT)));
					return new MerchantOffer(new ItemStack(Items.EMERALD, this.emeraldCost), new ItemStack(Items.COMPASS), itemStack, this.maxUses, this.villagerXp, 0.2F);
				} else {
					return null;
				}
			}
		}
	}
}
