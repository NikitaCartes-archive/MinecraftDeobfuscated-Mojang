package net.minecraft.core.cauldron;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public interface CauldronInteraction {
	Map<String, CauldronInteraction.InteractionMap> INTERACTIONS = new Object2ObjectArrayMap<>();
	Codec<CauldronInteraction.InteractionMap> CODEC = ExtraCodecs.stringResolverCodec(CauldronInteraction.InteractionMap::name, INTERACTIONS::get);
	CauldronInteraction.InteractionMap EMPTY = newInteractionMap("empty");
	CauldronInteraction.InteractionMap WATER = newInteractionMap("water");
	CauldronInteraction.InteractionMap LAVA = newInteractionMap("lava");
	CauldronInteraction.InteractionMap POWDER_SNOW = newInteractionMap("powder_snow");
	CauldronInteraction FILL_WATER = (blockState, level, blockPos, player, interactionHand, itemStack) -> emptyBucket(
			level,
			blockPos,
			player,
			interactionHand,
			itemStack,
			Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, Integer.valueOf(3)),
			SoundEvents.BUCKET_EMPTY
		);
	CauldronInteraction FILL_LAVA = (blockState, level, blockPos, player, interactionHand, itemStack) -> emptyBucket(
			level, blockPos, player, interactionHand, itemStack, Blocks.LAVA_CAULDRON.defaultBlockState(), SoundEvents.BUCKET_EMPTY_LAVA
		);
	CauldronInteraction FILL_POWDER_SNOW = (blockState, level, blockPos, player, interactionHand, itemStack) -> emptyBucket(
			level,
			blockPos,
			player,
			interactionHand,
			itemStack,
			Blocks.POWDER_SNOW_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, Integer.valueOf(3)),
			SoundEvents.BUCKET_EMPTY_POWDER_SNOW
		);
	CauldronInteraction SHULKER_BOX = (blockState, level, blockPos, player, interactionHand, itemStack) -> {
		Block block = Block.byItem(itemStack.getItem());
		if (!(block instanceof ShulkerBoxBlock)) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		} else {
			if (!level.isClientSide) {
				player.setItemInHand(interactionHand, itemStack.transmuteCopy(Blocks.SHULKER_BOX, 1));
				player.awardStat(Stats.CLEAN_SHULKER_BOX);
				LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
			}

			return ItemInteractionResult.sidedSuccess(level.isClientSide);
		}
	};
	CauldronInteraction BANNER = (blockState, level, blockPos, player, interactionHand, itemStack) -> {
		BannerPatternLayers bannerPatternLayers = itemStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
		if (bannerPatternLayers.layers().isEmpty()) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		} else {
			if (!level.isClientSide) {
				ItemStack itemStack2 = itemStack.copyWithCount(1);
				itemStack2.set(DataComponents.BANNER_PATTERNS, bannerPatternLayers.removeLast());
				itemStack.consume(1, player);
				if (itemStack.isEmpty()) {
					player.setItemInHand(interactionHand, itemStack2);
				} else if (player.getInventory().add(itemStack2)) {
					player.inventoryMenu.sendAllDataToRemote();
				} else {
					player.drop(itemStack2, false);
				}

				player.awardStat(Stats.CLEAN_BANNER);
				LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
			}

			return ItemInteractionResult.sidedSuccess(level.isClientSide);
		}
	};
	CauldronInteraction DYED_ITEM = (blockState, level, blockPos, player, interactionHand, itemStack) -> {
		if (!itemStack.is(ItemTags.DYEABLE)) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		} else if (!itemStack.has(DataComponents.DYED_COLOR)) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		} else {
			if (!level.isClientSide) {
				itemStack.remove(DataComponents.DYED_COLOR);
				player.awardStat(Stats.CLEAN_ARMOR);
				LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
			}

			return ItemInteractionResult.sidedSuccess(level.isClientSide);
		}
	};

	static CauldronInteraction.InteractionMap newInteractionMap(String string) {
		Object2ObjectOpenHashMap<Item, CauldronInteraction> object2ObjectOpenHashMap = new Object2ObjectOpenHashMap<>();
		object2ObjectOpenHashMap.defaultReturnValue(
			(blockState, level, blockPos, player, interactionHand, itemStack) -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
		);
		CauldronInteraction.InteractionMap interactionMap = new CauldronInteraction.InteractionMap(string, object2ObjectOpenHashMap);
		INTERACTIONS.put(string, interactionMap);
		return interactionMap;
	}

	ItemInteractionResult interact(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, ItemStack itemStack);

	static void bootStrap() {
		Map<Item, CauldronInteraction> map = EMPTY.map();
		addDefaultInteractions(map);
		map.put(Items.POTION, (CauldronInteraction)(blockState, level, blockPos, player, interactionHand, itemStack) -> {
			PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
			if (potionContents != null && potionContents.is(Potions.WATER)) {
				if (!level.isClientSide) {
					Item item = itemStack.getItem();
					player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.GLASS_BOTTLE)));
					player.awardStat(Stats.USE_CAULDRON);
					player.awardStat(Stats.ITEM_USED.get(item));
					level.setBlockAndUpdate(blockPos, Blocks.WATER_CAULDRON.defaultBlockState());
					level.playSound(null, blockPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
					level.gameEvent(null, GameEvent.FLUID_PLACE, blockPos);
				}

				return ItemInteractionResult.sidedSuccess(level.isClientSide);
			} else {
				return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			}
		});
		Map<Item, CauldronInteraction> map2 = WATER.map();
		addDefaultInteractions(map2);
		map2.put(
			Items.BUCKET,
			(CauldronInteraction)(blockState, level, blockPos, player, interactionHand, itemStack) -> fillBucket(
					blockState,
					level,
					blockPos,
					player,
					interactionHand,
					itemStack,
					new ItemStack(Items.WATER_BUCKET),
					blockStatex -> (Integer)blockStatex.getValue(LayeredCauldronBlock.LEVEL) == 3,
					SoundEvents.BUCKET_FILL
				)
		);
		map2.put(Items.GLASS_BOTTLE, (CauldronInteraction)(blockState, level, blockPos, player, interactionHand, itemStack) -> {
			if (!level.isClientSide) {
				Item item = itemStack.getItem();
				player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, PotionContents.createItemStack(Items.POTION, Potions.WATER)));
				player.awardStat(Stats.USE_CAULDRON);
				player.awardStat(Stats.ITEM_USED.get(item));
				LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
				level.playSound(null, blockPos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
				level.gameEvent(null, GameEvent.FLUID_PICKUP, blockPos);
			}

			return ItemInteractionResult.sidedSuccess(level.isClientSide);
		});
		map2.put(Items.POTION, (CauldronInteraction)(blockState, level, blockPos, player, interactionHand, itemStack) -> {
			if ((Integer)blockState.getValue(LayeredCauldronBlock.LEVEL) == 3) {
				return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			} else {
				PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
				if (potionContents != null && potionContents.is(Potions.WATER)) {
					if (!level.isClientSide) {
						player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.GLASS_BOTTLE)));
						player.awardStat(Stats.USE_CAULDRON);
						player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
						level.setBlockAndUpdate(blockPos, blockState.cycle(LayeredCauldronBlock.LEVEL));
						level.playSound(null, blockPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
						level.gameEvent(null, GameEvent.FLUID_PLACE, blockPos);
					}

					return ItemInteractionResult.sidedSuccess(level.isClientSide);
				} else {
					return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
				}
			}
		});
		map2.put(Items.LEATHER_BOOTS, DYED_ITEM);
		map2.put(Items.LEATHER_LEGGINGS, DYED_ITEM);
		map2.put(Items.LEATHER_CHESTPLATE, DYED_ITEM);
		map2.put(Items.LEATHER_HELMET, DYED_ITEM);
		map2.put(Items.LEATHER_HORSE_ARMOR, DYED_ITEM);
		map2.put(Items.WOLF_ARMOR, DYED_ITEM);
		map2.put(Items.WHITE_BANNER, BANNER);
		map2.put(Items.GRAY_BANNER, BANNER);
		map2.put(Items.BLACK_BANNER, BANNER);
		map2.put(Items.BLUE_BANNER, BANNER);
		map2.put(Items.BROWN_BANNER, BANNER);
		map2.put(Items.CYAN_BANNER, BANNER);
		map2.put(Items.GREEN_BANNER, BANNER);
		map2.put(Items.LIGHT_BLUE_BANNER, BANNER);
		map2.put(Items.LIGHT_GRAY_BANNER, BANNER);
		map2.put(Items.LIME_BANNER, BANNER);
		map2.put(Items.MAGENTA_BANNER, BANNER);
		map2.put(Items.ORANGE_BANNER, BANNER);
		map2.put(Items.PINK_BANNER, BANNER);
		map2.put(Items.PURPLE_BANNER, BANNER);
		map2.put(Items.RED_BANNER, BANNER);
		map2.put(Items.YELLOW_BANNER, BANNER);
		map2.put(Items.WHITE_SHULKER_BOX, SHULKER_BOX);
		map2.put(Items.GRAY_SHULKER_BOX, SHULKER_BOX);
		map2.put(Items.BLACK_SHULKER_BOX, SHULKER_BOX);
		map2.put(Items.BLUE_SHULKER_BOX, SHULKER_BOX);
		map2.put(Items.BROWN_SHULKER_BOX, SHULKER_BOX);
		map2.put(Items.CYAN_SHULKER_BOX, SHULKER_BOX);
		map2.put(Items.GREEN_SHULKER_BOX, SHULKER_BOX);
		map2.put(Items.LIGHT_BLUE_SHULKER_BOX, SHULKER_BOX);
		map2.put(Items.LIGHT_GRAY_SHULKER_BOX, SHULKER_BOX);
		map2.put(Items.LIME_SHULKER_BOX, SHULKER_BOX);
		map2.put(Items.MAGENTA_SHULKER_BOX, SHULKER_BOX);
		map2.put(Items.ORANGE_SHULKER_BOX, SHULKER_BOX);
		map2.put(Items.PINK_SHULKER_BOX, SHULKER_BOX);
		map2.put(Items.PURPLE_SHULKER_BOX, SHULKER_BOX);
		map2.put(Items.RED_SHULKER_BOX, SHULKER_BOX);
		map2.put(Items.YELLOW_SHULKER_BOX, SHULKER_BOX);
		Map<Item, CauldronInteraction> map3 = LAVA.map();
		map3.put(
			Items.BUCKET,
			(CauldronInteraction)(blockState, level, blockPos, player, interactionHand, itemStack) -> fillBucket(
					blockState, level, blockPos, player, interactionHand, itemStack, new ItemStack(Items.LAVA_BUCKET), blockStatex -> true, SoundEvents.BUCKET_FILL_LAVA
				)
		);
		addDefaultInteractions(map3);
		Map<Item, CauldronInteraction> map4 = POWDER_SNOW.map();
		map4.put(
			Items.BUCKET,
			(CauldronInteraction)(blockState, level, blockPos, player, interactionHand, itemStack) -> fillBucket(
					blockState,
					level,
					blockPos,
					player,
					interactionHand,
					itemStack,
					new ItemStack(Items.POWDER_SNOW_BUCKET),
					blockStatex -> (Integer)blockStatex.getValue(LayeredCauldronBlock.LEVEL) == 3,
					SoundEvents.BUCKET_FILL_POWDER_SNOW
				)
		);
		addDefaultInteractions(map4);
	}

	static void addDefaultInteractions(Map<Item, CauldronInteraction> map) {
		map.put(Items.LAVA_BUCKET, FILL_LAVA);
		map.put(Items.WATER_BUCKET, FILL_WATER);
		map.put(Items.POWDER_SNOW_BUCKET, FILL_POWDER_SNOW);
	}

	static ItemInteractionResult fillBucket(
		BlockState blockState,
		Level level,
		BlockPos blockPos,
		Player player,
		InteractionHand interactionHand,
		ItemStack itemStack,
		ItemStack itemStack2,
		Predicate<BlockState> predicate,
		SoundEvent soundEvent
	) {
		if (!predicate.test(blockState)) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		} else {
			if (!level.isClientSide) {
				Item item = itemStack.getItem();
				player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, itemStack2));
				player.awardStat(Stats.USE_CAULDRON);
				player.awardStat(Stats.ITEM_USED.get(item));
				level.setBlockAndUpdate(blockPos, Blocks.CAULDRON.defaultBlockState());
				level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
				level.gameEvent(null, GameEvent.FLUID_PICKUP, blockPos);
			}

			return ItemInteractionResult.sidedSuccess(level.isClientSide);
		}
	}

	static ItemInteractionResult emptyBucket(
		Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, ItemStack itemStack, BlockState blockState, SoundEvent soundEvent
	) {
		if (!level.isClientSide) {
			Item item = itemStack.getItem();
			player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.BUCKET)));
			player.awardStat(Stats.FILL_CAULDRON);
			player.awardStat(Stats.ITEM_USED.get(item));
			level.setBlockAndUpdate(blockPos, blockState);
			level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
			level.gameEvent(null, GameEvent.FLUID_PLACE, blockPos);
		}

		return ItemInteractionResult.sidedSuccess(level.isClientSide);
	}

	public static record InteractionMap(String name, Map<Item, CauldronInteraction> map) {
	}
}
