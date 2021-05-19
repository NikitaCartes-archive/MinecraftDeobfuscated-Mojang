package net.minecraft.core.cauldron;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public interface CauldronInteraction {
	Map<Item, CauldronInteraction> EMPTY = newInteractionMap();
	Map<Item, CauldronInteraction> WATER = newInteractionMap();
	Map<Item, CauldronInteraction> LAVA = newInteractionMap();
	Map<Item, CauldronInteraction> POWDER_SNOW = newInteractionMap();
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
			return InteractionResult.PASS;
		} else {
			if (!level.isClientSide) {
				ItemStack itemStack2 = new ItemStack(Blocks.SHULKER_BOX);
				if (itemStack.hasTag()) {
					itemStack2.setTag(itemStack.getTag().copy());
				}

				player.setItemInHand(interactionHand, itemStack2);
				player.awardStat(Stats.CLEAN_SHULKER_BOX);
				LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
			}

			return InteractionResult.sidedSuccess(level.isClientSide);
		}
	};
	CauldronInteraction BANNER = (blockState, level, blockPos, player, interactionHand, itemStack) -> {
		if (BannerBlockEntity.getPatternCount(itemStack) <= 0) {
			return InteractionResult.PASS;
		} else {
			if (!level.isClientSide) {
				ItemStack itemStack2 = itemStack.copy();
				itemStack2.setCount(1);
				BannerBlockEntity.removeLastPattern(itemStack2);
				if (!player.getAbilities().instabuild) {
					itemStack.shrink(1);
				}

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

			return InteractionResult.sidedSuccess(level.isClientSide);
		}
	};
	CauldronInteraction DYED_ITEM = (blockState, level, blockPos, player, interactionHand, itemStack) -> {
		if (!(itemStack.getItem() instanceof DyeableLeatherItem dyeableLeatherItem)) {
			return InteractionResult.PASS;
		} else if (!dyeableLeatherItem.hasCustomColor(itemStack)) {
			return InteractionResult.PASS;
		} else {
			if (!level.isClientSide) {
				dyeableLeatherItem.clearColor(itemStack);
				player.awardStat(Stats.CLEAN_ARMOR);
				LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
			}

			return InteractionResult.sidedSuccess(level.isClientSide);
		}
	};

	static Object2ObjectOpenHashMap<Item, CauldronInteraction> newInteractionMap() {
		return Util.make(
			new Object2ObjectOpenHashMap<>(),
			object2ObjectOpenHashMap -> object2ObjectOpenHashMap.defaultReturnValue(
					(blockState, level, blockPos, player, interactionHand, itemStack) -> InteractionResult.PASS
				)
		);
	}

	InteractionResult interact(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, ItemStack itemStack);

	static void bootStrap() {
		addDefaultInteractions(EMPTY);
		EMPTY.put(Items.POTION, (CauldronInteraction)(blockState, level, blockPos, player, interactionHand, itemStack) -> {
			if (PotionUtils.getPotion(itemStack) != Potions.WATER) {
				return InteractionResult.PASS;
			} else {
				if (!level.isClientSide) {
					Item item = itemStack.getItem();
					player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.GLASS_BOTTLE)));
					player.awardStat(Stats.USE_CAULDRON);
					player.awardStat(Stats.ITEM_USED.get(item));
					level.setBlockAndUpdate(blockPos, Blocks.WATER_CAULDRON.defaultBlockState());
					level.playSound(null, blockPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
					level.gameEvent(null, GameEvent.FLUID_PLACE, blockPos);
				}

				return InteractionResult.sidedSuccess(level.isClientSide);
			}
		});
		addDefaultInteractions(WATER);
		WATER.put(
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
		WATER.put(Items.GLASS_BOTTLE, (CauldronInteraction)(blockState, level, blockPos, player, interactionHand, itemStack) -> {
			if (!level.isClientSide) {
				Item item = itemStack.getItem();
				player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)));
				player.awardStat(Stats.USE_CAULDRON);
				player.awardStat(Stats.ITEM_USED.get(item));
				LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
				level.playSound(null, blockPos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
				level.gameEvent(null, GameEvent.FLUID_PICKUP, blockPos);
			}

			return InteractionResult.sidedSuccess(level.isClientSide);
		});
		WATER.put(Items.POTION, (CauldronInteraction)(blockState, level, blockPos, player, interactionHand, itemStack) -> {
			if ((Integer)blockState.getValue(LayeredCauldronBlock.LEVEL) != 3 && PotionUtils.getPotion(itemStack) == Potions.WATER) {
				if (!level.isClientSide) {
					player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.GLASS_BOTTLE)));
					player.awardStat(Stats.USE_CAULDRON);
					player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
					level.setBlockAndUpdate(blockPos, blockState.cycle(LayeredCauldronBlock.LEVEL));
					level.playSound(null, blockPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
					level.gameEvent(null, GameEvent.FLUID_PLACE, blockPos);
				}

				return InteractionResult.sidedSuccess(level.isClientSide);
			} else {
				return InteractionResult.PASS;
			}
		});
		WATER.put(Items.LEATHER_BOOTS, DYED_ITEM);
		WATER.put(Items.LEATHER_LEGGINGS, DYED_ITEM);
		WATER.put(Items.LEATHER_CHESTPLATE, DYED_ITEM);
		WATER.put(Items.LEATHER_HELMET, DYED_ITEM);
		WATER.put(Items.LEATHER_HORSE_ARMOR, DYED_ITEM);
		WATER.put(Items.WHITE_BANNER, BANNER);
		WATER.put(Items.GRAY_BANNER, BANNER);
		WATER.put(Items.BLACK_BANNER, BANNER);
		WATER.put(Items.BLUE_BANNER, BANNER);
		WATER.put(Items.BROWN_BANNER, BANNER);
		WATER.put(Items.CYAN_BANNER, BANNER);
		WATER.put(Items.GREEN_BANNER, BANNER);
		WATER.put(Items.LIGHT_BLUE_BANNER, BANNER);
		WATER.put(Items.LIGHT_GRAY_BANNER, BANNER);
		WATER.put(Items.LIME_BANNER, BANNER);
		WATER.put(Items.MAGENTA_BANNER, BANNER);
		WATER.put(Items.ORANGE_BANNER, BANNER);
		WATER.put(Items.PINK_BANNER, BANNER);
		WATER.put(Items.PURPLE_BANNER, BANNER);
		WATER.put(Items.RED_BANNER, BANNER);
		WATER.put(Items.YELLOW_BANNER, BANNER);
		WATER.put(Items.WHITE_SHULKER_BOX, SHULKER_BOX);
		WATER.put(Items.GRAY_SHULKER_BOX, SHULKER_BOX);
		WATER.put(Items.BLACK_SHULKER_BOX, SHULKER_BOX);
		WATER.put(Items.BLUE_SHULKER_BOX, SHULKER_BOX);
		WATER.put(Items.BROWN_SHULKER_BOX, SHULKER_BOX);
		WATER.put(Items.CYAN_SHULKER_BOX, SHULKER_BOX);
		WATER.put(Items.GREEN_SHULKER_BOX, SHULKER_BOX);
		WATER.put(Items.LIGHT_BLUE_SHULKER_BOX, SHULKER_BOX);
		WATER.put(Items.LIGHT_GRAY_SHULKER_BOX, SHULKER_BOX);
		WATER.put(Items.LIME_SHULKER_BOX, SHULKER_BOX);
		WATER.put(Items.MAGENTA_SHULKER_BOX, SHULKER_BOX);
		WATER.put(Items.ORANGE_SHULKER_BOX, SHULKER_BOX);
		WATER.put(Items.PINK_SHULKER_BOX, SHULKER_BOX);
		WATER.put(Items.PURPLE_SHULKER_BOX, SHULKER_BOX);
		WATER.put(Items.RED_SHULKER_BOX, SHULKER_BOX);
		WATER.put(Items.YELLOW_SHULKER_BOX, SHULKER_BOX);
		LAVA.put(
			Items.BUCKET,
			(CauldronInteraction)(blockState, level, blockPos, player, interactionHand, itemStack) -> fillBucket(
					blockState, level, blockPos, player, interactionHand, itemStack, new ItemStack(Items.LAVA_BUCKET), blockStatex -> true, SoundEvents.BUCKET_FILL_LAVA
				)
		);
		addDefaultInteractions(LAVA);
		POWDER_SNOW.put(
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
		addDefaultInteractions(POWDER_SNOW);
	}

	static void addDefaultInteractions(Map<Item, CauldronInteraction> map) {
		map.put(Items.LAVA_BUCKET, FILL_LAVA);
		map.put(Items.WATER_BUCKET, FILL_WATER);
		map.put(Items.POWDER_SNOW_BUCKET, FILL_POWDER_SNOW);
	}

	static InteractionResult fillBucket(
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
			return InteractionResult.PASS;
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

			return InteractionResult.sidedSuccess(level.isClientSide);
		}
	}

	static InteractionResult emptyBucket(
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

		return InteractionResult.sidedSuccess(level.isClientSide);
	}
}
