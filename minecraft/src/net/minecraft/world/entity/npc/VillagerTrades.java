package net.minecraft.world.entity.npc;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.CarryableTrade;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.CarriedBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class VillagerTrades {
	public static final CarryableTrade.Block CURRENCY = CarryableTrade.block(Blocks.EMERALD_ORE);
	private static final int DEFAULT_SUPPLY = Integer.MAX_VALUE;
	private static final int COMMON_ITEMS_SUPPLY = Integer.MAX_VALUE;
	private static final int XP_LEVEL_1_SELL = 4;
	private static final int XP_LEVEL_1_BUY = 8;
	private static final int XP_LEVEL_2_SELL = 20;
	private static final int XP_LEVEL_2_BUY = 40;
	private static final int XP_LEVEL_3_SELL = 40;
	private static final int XP_LEVEL_3_BUY = 80;
	private static final int XP_LEVEL_4_SELL = 60;
	private static final int XP_LEVEL_4_BUY = 120;
	private static final int XP_LEVEL_5_TRADE = 120;
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
							new VillagerTrades.EmeraldForItems(Blocks.HAY_BLOCK, Integer.MAX_VALUE, 8), new VillagerTrades.ItemsForEmeralds(Blocks.HAY_BLOCK, 1, 4)
						},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Blocks.PUMPKIN, Integer.MAX_VALUE, 40), new VillagerTrades.ItemsForEmeralds(Blocks.PUMPKIN, 1, 20)
						},
						3,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Blocks.MELON, Integer.MAX_VALUE, 80),
							new VillagerTrades.ItemsForEmeralds(Blocks.MELON, 1, 40),
							new VillagerTrades.EmeraldForItems(CarryableTrade.entity(EntityType.SKELETON), 1, 80),
							new VillagerTrades.EmeraldForItems(CarryableTrade.entity(EntityType.SKELETON_HORSE), 1, 80)
						},
						4,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.ItemsForEmeralds(Blocks.CAKE, 1, 60), new VillagerTrades.EmeraldForItems(CarryableTrade.entity(EntityType.WITHER_SKELETON), 1, 120)
						},
						5,
						new VillagerTrades.ItemListing[]{new VillagerTrades.ItemsForEmeralds(Blocks.CACTUS, 1, 120)}
					)
				)
			);
			hashMap.put(
				VillagerProfession.FISHERMAN,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Blocks.KELP_PLANT, Integer.MAX_VALUE, 8),
							new VillagerTrades.EmeraldForItems(CarryableTrade.entity(EntityType.COD), Integer.MAX_VALUE, 8),
							new VillagerTrades.EmeraldForItems(CarryableTrade.entity(EntityType.SALMON), Integer.MAX_VALUE, 8),
							new VillagerTrades.ItemsForEmeralds(CarryableTrade.entity(EntityType.COD), Integer.MAX_VALUE, 4)
						},
						2,
						new VillagerTrades.ItemListing[]{new VillagerTrades.ItemsForEmeralds(Blocks.CAMPFIRE, 1, 20)},
						3,
						new VillagerTrades.ItemListing[]{new VillagerTrades.ItemsForEmeralds(Blocks.PRISMARINE, 1, 40)},
						4,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.ItemsForEmeralds(Blocks.DARK_PRISMARINE, 1, 60), new VillagerTrades.ItemsForEmeralds(CarryableTrade.entity(EntityType.SQUID), 1, 8)
						},
						5,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.ItemsForEmeralds(Blocks.BRAIN_CORAL_BLOCK, 1, 120),
							new VillagerTrades.ItemsForEmeralds(Blocks.BUBBLE_CORAL_BLOCK, 1, 120),
							new VillagerTrades.ItemsForEmeralds(Blocks.TUBE_CORAL_BLOCK, 1, 120),
							new VillagerTrades.EmeraldForItems(Blocks.FIRE_CORAL_BLOCK, Integer.MAX_VALUE, 120),
							new VillagerTrades.EmeraldForItems(Blocks.HORN_CORAL_BLOCK, Integer.MAX_VALUE, 120),
							new VillagerTrades.ItemsForEmeralds(CarryableTrade.entity(EntityType.TROPICAL_FISH), Integer.MAX_VALUE, 120),
							new VillagerTrades.ItemsForEmeralds(CarryableTrade.entity(EntityType.GLOW_SQUID), 1, 8)
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
							new VillagerTrades.EmeraldForItems(Blocks.WHITE_WOOL, Integer.MAX_VALUE, 8),
							new VillagerTrades.EmeraldForItems(Blocks.YELLOW_WOOL, Integer.MAX_VALUE, 8),
							new VillagerTrades.EmeraldForItems(Blocks.RED_WOOL, Integer.MAX_VALUE, 8),
							new VillagerTrades.EmeraldForItems(Blocks.BLACK_WOOL, Integer.MAX_VALUE, 8),
							new VillagerTrades.EmeraldForItems(Blocks.BLUE_WOOL, Integer.MAX_VALUE, 8),
							new VillagerTrades.EmeraldForItems(Blocks.GREEN_WOOL, Integer.MAX_VALUE, 8),
							new VillagerTrades.EmeraldForItems(Blocks.LIME_WOOL, Integer.MAX_VALUE, 8),
							new VillagerTrades.EmeraldForItems(Blocks.PURPLE_WOOL, Integer.MAX_VALUE, 8),
							new VillagerTrades.ItemsForEmeralds(Blocks.WHITE_WOOL, 1, 4),
							new VillagerTrades.ItemsForEmeralds(Blocks.YELLOW_WOOL, 1, 4),
							new VillagerTrades.ItemsForEmeralds(Blocks.RED_WOOL, 1, 4),
							new VillagerTrades.ItemsForEmeralds(Blocks.BLACK_WOOL, 1, 4),
							new VillagerTrades.ItemsForEmeralds(Blocks.BLUE_WOOL, 1, 4),
							new VillagerTrades.ItemsForEmeralds(Blocks.GREEN_WOOL, 1, 4),
							new VillagerTrades.ItemsForEmeralds(Blocks.LIME_WOOL, 1, 4),
							new VillagerTrades.ItemsForEmeralds(Blocks.PURPLE_WOOL, 1, 4)
						},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.ItemsForEmeralds(Blocks.WHITE_BED, 1, 20),
							new VillagerTrades.ItemsForEmeralds(Blocks.YELLOW_BED, 1, 20),
							new VillagerTrades.ItemsForEmeralds(Blocks.RED_BED, 1, 20),
							new VillagerTrades.ItemsForEmeralds(Blocks.BLACK_BED, 1, 20),
							new VillagerTrades.ItemsForEmeralds(Blocks.BLUE_BED, 1, 20),
							new VillagerTrades.ItemsForEmeralds(Blocks.GREEN_BED, 1, 20),
							new VillagerTrades.ItemsForEmeralds(Blocks.LIME_BED, 1, 20),
							new VillagerTrades.ItemsForEmeralds(Blocks.PURPLE_BED, 1, 20)
						}
					)
				)
			);
			hashMap.put(
				VillagerProfession.FLETCHER,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Blocks.OAK_LOG, 4, 8),
							new VillagerTrades.EmeraldForItems(Blocks.SPRUCE_LOG, 4, 8),
							new VillagerTrades.EmeraldForItems(Blocks.ACACIA_LOG, 4, 8),
							new VillagerTrades.EmeraldForItems(Blocks.DARK_OAK_LOG, 4, 8),
							new VillagerTrades.EmeraldForItems(Blocks.BIRCH_LOG, 4, 8),
							new VillagerTrades.EmeraldForItems(Blocks.JUNGLE_LOG, 4, 8),
							new VillagerTrades.ItemsForEmeralds(Items.STONE_AXE, 4, 4)
						},
						2,
						new VillagerTrades.ItemListing[]{new VillagerTrades.EmeraldForItems(Blocks.GRAVEL, Integer.MAX_VALUE, 40)},
						3,
						new VillagerTrades.ItemListing[]{new VillagerTrades.ItemsForEmeralds(Blocks.GRAVEL, 1, 40)}
					)
				)
			);
			hashMap.put(
				VillagerProfession.LIBRARIAN,
				toIntMap(
					ImmutableMap.<Integer, VillagerTrades.ItemListing[]>builder()
						.put(1, new VillagerTrades.ItemListing[]{new VillagerTrades.ItemsForEmeralds(Blocks.BOOKSHELF, 1, 4)})
						.put(
							2,
							new VillagerTrades.ItemListing[]{
								new VillagerTrades.EmeraldForItems(Blocks.BOOKSHELF, Integer.MAX_VALUE, 40), new VillagerTrades.ItemsForEmeralds(Blocks.LANTERN, 1, 20)
							}
						)
						.put(3, new VillagerTrades.ItemListing[]{new VillagerTrades.ItemsForEmeralds(Blocks.GLASS, 1, 40)})
						.build()
				)
			);
			hashMap.put(
				VillagerProfession.CARTOGRAPHER,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Blocks.GLASS_PANE, Integer.MAX_VALUE, 8),
							new VillagerTrades.ItemsForEmeralds(CarryableTrade.entity(EntityType.PARROT), 1, 4)
						},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.ItemsForEmeralds(Blocks.WHITE_BANNER, 1, 20),
							new VillagerTrades.ItemsForEmeralds(Blocks.BLUE_BANNER, 1, 20),
							new VillagerTrades.ItemsForEmeralds(Blocks.RED_BANNER, 1, 20),
							new VillagerTrades.ItemsForEmeralds(Blocks.GREEN_BANNER, 1, 20),
							new VillagerTrades.ItemsForEmeralds(Blocks.LIME_BANNER, 1, 20),
							new VillagerTrades.ItemsForEmeralds(Blocks.BLACK_BANNER, 1, 20),
							new VillagerTrades.ItemsForEmeralds(Blocks.PURPLE_BANNER, 1, 20),
							new VillagerTrades.ItemsForEmeralds(Blocks.MAGENTA_BANNER, 1, 20),
							new VillagerTrades.ItemsForEmeralds(Blocks.YELLOW_BANNER, 1, 20),
							new VillagerTrades.ItemsForEmeralds(Blocks.ORANGE_BANNER, 1, 20)
						},
						3,
						new VillagerTrades.ItemListing[]{new VillagerTrades.ItemsForEmeralds(Items.SPYGLASS, Integer.MAX_VALUE, 40)}
					)
				)
			);
			hashMap.put(
				VillagerProfession.CLERIC,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.ItemsForEmeralds(Blocks.GLOWSTONE, 1, 8),
							new VillagerTrades.ItemsForEmeralds(Blocks.REDSTONE_BLOCK, 1, 8),
							new VillagerTrades.ItemsForEmeralds(CarryableTrade.entity(EntityType.SILVERFISH), 1, 8)
						},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.ItemsForEmeralds(Blocks.GOLD_BLOCK, 1, 40),
							new VillagerTrades.ItemsForEmeralds(Blocks.LAPIS_BLOCK, 1, 40),
							new VillagerTrades.ItemsForEmeralds(Blocks.EMERALD_BLOCK, 1, 40)
						},
						3,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(CarryableTrade.entity(EntityType.ZOMBIE), 1, 80),
							new VillagerTrades.EmeraldForItems(CarryableTrade.entity(EntityType.ZOMBIE_VILLAGER), 1, 80)
						},
						4,
						new VillagerTrades.ItemListing[]{new VillagerTrades.ItemsForEmeralds(CarryableTrade.entity(EntityType.SNOW_GOLEM), 4, 60)},
						5,
						new VillagerTrades.ItemListing[]{new VillagerTrades.ItemsForEmeralds(CarryableTrade.entity(EntityType.IRON_GOLEM), 1, 120)}
					)
				)
			);
			hashMap.put(
				VillagerProfession.ARMORER,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Blocks.COAL_ORE, Integer.MAX_VALUE, 8),
							new VillagerTrades.ItemsForEmeralds(Blocks.CARVED_PUMPKIN, Integer.MAX_VALUE, 4)
						},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Blocks.IRON_ORE, Integer.MAX_VALUE, 40),
							new VillagerTrades.EmeraldForItems(Blocks.GOLD_ORE, Integer.MAX_VALUE, 40),
							new VillagerTrades.EmeraldForItems(Blocks.DIAMOND_ORE, Integer.MAX_VALUE, 40)
						},
						3,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.ItemsForEmeralds(Blocks.IRON_BLOCK, 1, 40),
							new VillagerTrades.ItemsForEmeralds(Blocks.GOLD_BLOCK, 1, 40),
							new VillagerTrades.ItemsForEmeralds(CarryableTrade.block(Blocks.BELL), 1, 40, 0.2F)
						},
						4,
						new VillagerTrades.ItemListing[]{new VillagerTrades.ItemsForEmeralds(Blocks.DIAMOND_BLOCK, 1, 60)}
					)
				)
			);
			hashMap.put(
				VillagerProfession.WEAPONSMITH,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Blocks.COAL_ORE, Integer.MAX_VALUE, 8),
							new VillagerTrades.EmeraldForItems(Blocks.GRAVEL, Integer.MAX_VALUE, 8),
							new VillagerTrades.ItemsForEmeralds(Blocks.CACTUS, Integer.MAX_VALUE, 4)
						},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Blocks.IRON_ORE, Integer.MAX_VALUE, 40),
							new VillagerTrades.EmeraldForItems(Blocks.GOLD_ORE, Integer.MAX_VALUE, 40),
							new VillagerTrades.EmeraldForItems(Blocks.DIAMOND_ORE, Integer.MAX_VALUE, 40)
						},
						3,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.ItemsForEmeralds(Blocks.IRON_BLOCK, 1, 40), new VillagerTrades.ItemsForEmeralds(Blocks.GOLD_BLOCK, 1, 40)
						},
						4,
						new VillagerTrades.ItemListing[]{new VillagerTrades.ItemsForEmeralds(Blocks.DIAMOND_BLOCK, 1, 60)}
					)
				)
			);
			hashMap.put(
				VillagerProfession.TOOLSMITH,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Blocks.COAL_ORE, Integer.MAX_VALUE, 8),
							new VillagerTrades.ItemsForEmeralds(Items.STONE_PICKAXE, Integer.MAX_VALUE, 8)
						},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Blocks.IRON_ORE, Integer.MAX_VALUE, 40),
							new VillagerTrades.EmeraldForItems(Blocks.GOLD_ORE, Integer.MAX_VALUE, 40),
							new VillagerTrades.EmeraldForItems(Blocks.DIAMOND_ORE, Integer.MAX_VALUE, 40)
						},
						3,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.ItemsForEmeralds(Items.IRON_PICKAXE, Integer.MAX_VALUE, 80),
							new VillagerTrades.ItemsForEmeralds(Blocks.IRON_BLOCK, 1, 40),
							new VillagerTrades.ItemsForEmeralds(Blocks.GOLD_BLOCK, 1, 40)
						},
						4,
						new VillagerTrades.ItemListing[]{new VillagerTrades.ItemsForEmeralds(Blocks.DIAMOND_BLOCK, 1, 60)},
						5,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.ItemsForEmeralds(Blocks.DIAMOND_BLOCK, 1, 120), new VillagerTrades.EmeraldForItems(CarryableTrade.entity(EntityType.CREEPER), 1, 120)
						}
					)
				)
			);
			hashMap.put(
				VillagerProfession.BUTCHER,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.ItemsForEmeralds(Blocks.CAMPFIRE, 1, 4), new VillagerTrades.EmeraldForItems(CarryableTrade.entity(EntityType.CHICKEN), 3, 8)
						},
						2,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Blocks.DRIED_KELP_BLOCK, Integer.MAX_VALUE, 40),
							new VillagerTrades.EmeraldForItems(CarryableTrade.entity(EntityType.PIG), 3, 8),
							new VillagerTrades.EmeraldForItems(CarryableTrade.entity(EntityType.COW), 3, 8),
							new VillagerTrades.EmeraldForItems(CarryableTrade.entity(EntityType.GOAT), 3, 8)
						}
					)
				)
			);
			hashMap.put(
				VillagerProfession.LEATHERWORKER,
				toIntMap(
					ImmutableMap.of(
						1,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(CarryableTrade.entity(EntityType.COW), 3, 8),
							new VillagerTrades.ItemsForEmeralds(CarryableTrade.entity(EntityType.CAT), 3, 4)
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
							new VillagerTrades.EmeraldForItems(Blocks.CLAY, Integer.MAX_VALUE, 8), new VillagerTrades.ItemsForEmeralds(Blocks.BRICKS, Integer.MAX_VALUE, 4)
						},
						2,
						new VillagerTrades.ItemListing[]{new VillagerTrades.EmeraldForItems(Blocks.STONE, Integer.MAX_VALUE, 40)},
						3,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Blocks.GRANITE, Integer.MAX_VALUE, 80),
							new VillagerTrades.EmeraldForItems(Blocks.ANDESITE, Integer.MAX_VALUE, 80),
							new VillagerTrades.EmeraldForItems(Blocks.DIORITE, Integer.MAX_VALUE, 80),
							new VillagerTrades.ItemsForEmeralds(Blocks.DRIPSTONE_BLOCK, 1, 40)
						},
						4,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.EmeraldForItems(Blocks.QUARTZ_BLOCK, Integer.MAX_VALUE, 120),
							new VillagerTrades.ItemsForEmeralds(Blocks.ORANGE_TERRACOTTA, Integer.MAX_VALUE, 60),
							new VillagerTrades.ItemsForEmeralds(Blocks.WHITE_TERRACOTTA, Integer.MAX_VALUE, 60),
							new VillagerTrades.ItemsForEmeralds(Blocks.RED_TERRACOTTA, Integer.MAX_VALUE, 60),
							new VillagerTrades.ItemsForEmeralds(Blocks.PINK_TERRACOTTA, Integer.MAX_VALUE, 60),
							new VillagerTrades.ItemsForEmeralds(Blocks.GREEN_TERRACOTTA, Integer.MAX_VALUE, 60),
							new VillagerTrades.ItemsForEmeralds(Blocks.BLACK_GLAZED_TERRACOTTA, Integer.MAX_VALUE, 60),
							new VillagerTrades.ItemsForEmeralds(Blocks.MAGENTA_GLAZED_TERRACOTTA, Integer.MAX_VALUE, 60),
							new VillagerTrades.ItemsForEmeralds(Blocks.LIME_GLAZED_TERRACOTTA, Integer.MAX_VALUE, 60),
							new VillagerTrades.ItemsForEmeralds(Blocks.GREEN_GLAZED_TERRACOTTA, Integer.MAX_VALUE, 60),
							new VillagerTrades.ItemsForEmeralds(Blocks.YELLOW_GLAZED_TERRACOTTA, Integer.MAX_VALUE, 60)
						},
						5,
						new VillagerTrades.ItemListing[]{
							new VillagerTrades.ItemsForEmeralds(Blocks.QUARTZ_PILLAR, 1, 120),
							new VillagerTrades.ItemsForEmeralds(Blocks.QUARTZ_BLOCK, 1, 120),
							new VillagerTrades.ItemsForEmeralds(Blocks.TNT, 3, 120),
							new VillagerTrades.ItemsForEmeralds(Items.DIAMOND_PICKAXE, Integer.MAX_VALUE, 120)
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
				new VillagerTrades.ItemsForEmeralds(Blocks.SEA_PICKLE, 5, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.GLOWSTONE, 5, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.FERN, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.SUGAR_CANE, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.PUMPKIN, 4, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.KELP, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.CACTUS, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.DANDELION, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.POPPY, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.BLUE_ORCHID, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.ALLIUM, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.AZURE_BLUET, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.RED_TULIP, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.ORANGE_TULIP, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.WHITE_TULIP, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.PINK_TULIP, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.OXEYE_DAISY, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.CORNFLOWER, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.LILY_OF_THE_VALLEY, 7, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.ACACIA_SAPLING, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.BIRCH_SAPLING, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.DARK_OAK_SAPLING, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.JUNGLE_SAPLING, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.OAK_SAPLING, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.SPRUCE_SAPLING, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.BRAIN_CORAL_BLOCK, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.BUBBLE_CORAL_BLOCK, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.FIRE_CORAL_BLOCK, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.HORN_CORAL_BLOCK, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.TUBE_CORAL_BLOCK, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.VINE, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.BROWN_MUSHROOM, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.RED_MUSHROOM, 12, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.LILY_PAD, 5, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.SMALL_DRIPLEAF, 5, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.SAND, 8, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.RED_SAND, 6, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.POINTED_DRIPSTONE, 5, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.ROOTED_DIRT, 5, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.MOSS_BLOCK, 5, 1)
			},
			2,
			new VillagerTrades.ItemListing[]{
				new VillagerTrades.ItemsForEmeralds(Blocks.PACKED_ICE, 6, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.BLUE_ICE, 6, 1),
				new VillagerTrades.ItemsForEmeralds(Blocks.PODZOL, 6, 1)
			}
		)
	);

	private static Int2ObjectMap<VillagerTrades.ItemListing[]> toIntMap(ImmutableMap<Integer, VillagerTrades.ItemListing[]> immutableMap) {
		return new Int2ObjectOpenHashMap<>(immutableMap);
	}

	static class EmeraldForItems implements VillagerTrades.ItemListing {
		private final CarryableTrade cost;
		private final int maxUses;
		private final int villagerXp;
		private final float priceMultiplier;

		public EmeraldForItems(Block block, int i, int j) {
			this(CarryableTrade.block(block), i, j);
		}

		public EmeraldForItems(CarryableTrade carryableTrade, int i, int j) {
			this.cost = carryableTrade;
			this.maxUses = i;
			this.villagerXp = j;
			this.priceMultiplier = 0.05F;
		}

		@Override
		public MerchantOffer getOffer(Entity entity, Random random) {
			return new MerchantOffer(this.cost, VillagerTrades.CURRENCY, this.maxUses, this.villagerXp, this.priceMultiplier);
		}
	}

	public interface ItemListing {
		@Nullable
		MerchantOffer getOffer(Entity entity, Random random);
	}

	static class ItemsForEmeralds implements VillagerTrades.ItemListing {
		private final CarryableTrade result;
		private final int maxUses;
		private final int villagerXp;
		private final float priceMultiplier;

		public ItemsForEmeralds(Block block, int i, int j) {
			this(CarryableTrade.block(block), i, j);
		}

		public ItemsForEmeralds(Item item, int i, int j) {
			this(
				CarryableTrade.block(
					(Block)CarriedBlocks.getBlockFromItemStack(item.getDefaultInstance()).map(BlockBehaviour.BlockStateBase::getBlock).orElse(VillagerTrades.CURRENCY.block())
				),
				i,
				j
			);
		}

		public ItemsForEmeralds(CarryableTrade carryableTrade, int i, int j) {
			this(carryableTrade, i, j, 0.05F);
		}

		public ItemsForEmeralds(CarryableTrade carryableTrade, int i, int j, float f) {
			this.result = carryableTrade;
			this.maxUses = i;
			this.villagerXp = j;
			this.priceMultiplier = f;
		}

		@Override
		public MerchantOffer getOffer(Entity entity, Random random) {
			return new MerchantOffer(VillagerTrades.CURRENCY, this.result, this.maxUses, this.villagerXp, this.priceMultiplier);
		}
	}
}
