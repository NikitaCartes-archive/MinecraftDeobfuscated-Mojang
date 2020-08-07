package net.minecraft.client.renderer;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

@Environment(EnvType.CLIENT)
public class ItemBlockRenderTypes {
	private static final Map<Block, RenderType> TYPE_BY_BLOCK = Util.make(Maps.<Block, RenderType>newHashMap(), hashMap -> {
		RenderType renderType = RenderType.tripwire();
		hashMap.put(Blocks.TRIPWIRE, renderType);
		RenderType renderType2 = RenderType.cutoutMipped();
		hashMap.put(Blocks.GRASS_BLOCK, renderType2);
		hashMap.put(Blocks.IRON_BARS, renderType2);
		hashMap.put(Blocks.GLASS_PANE, renderType2);
		hashMap.put(Blocks.TRIPWIRE_HOOK, renderType2);
		hashMap.put(Blocks.HOPPER, renderType2);
		hashMap.put(Blocks.CHAIN, renderType2);
		hashMap.put(Blocks.JUNGLE_LEAVES, renderType2);
		hashMap.put(Blocks.OAK_LEAVES, renderType2);
		hashMap.put(Blocks.SPRUCE_LEAVES, renderType2);
		hashMap.put(Blocks.ACACIA_LEAVES, renderType2);
		hashMap.put(Blocks.BIRCH_LEAVES, renderType2);
		hashMap.put(Blocks.DARK_OAK_LEAVES, renderType2);
		RenderType renderType3 = RenderType.cutout();
		hashMap.put(Blocks.OAK_SAPLING, renderType3);
		hashMap.put(Blocks.SPRUCE_SAPLING, renderType3);
		hashMap.put(Blocks.BIRCH_SAPLING, renderType3);
		hashMap.put(Blocks.JUNGLE_SAPLING, renderType3);
		hashMap.put(Blocks.ACACIA_SAPLING, renderType3);
		hashMap.put(Blocks.DARK_OAK_SAPLING, renderType3);
		hashMap.put(Blocks.GLASS, renderType3);
		hashMap.put(Blocks.WHITE_BED, renderType3);
		hashMap.put(Blocks.ORANGE_BED, renderType3);
		hashMap.put(Blocks.MAGENTA_BED, renderType3);
		hashMap.put(Blocks.LIGHT_BLUE_BED, renderType3);
		hashMap.put(Blocks.YELLOW_BED, renderType3);
		hashMap.put(Blocks.LIME_BED, renderType3);
		hashMap.put(Blocks.PINK_BED, renderType3);
		hashMap.put(Blocks.GRAY_BED, renderType3);
		hashMap.put(Blocks.LIGHT_GRAY_BED, renderType3);
		hashMap.put(Blocks.CYAN_BED, renderType3);
		hashMap.put(Blocks.PURPLE_BED, renderType3);
		hashMap.put(Blocks.BLUE_BED, renderType3);
		hashMap.put(Blocks.BROWN_BED, renderType3);
		hashMap.put(Blocks.GREEN_BED, renderType3);
		hashMap.put(Blocks.RED_BED, renderType3);
		hashMap.put(Blocks.BLACK_BED, renderType3);
		hashMap.put(Blocks.POWERED_RAIL, renderType3);
		hashMap.put(Blocks.DETECTOR_RAIL, renderType3);
		hashMap.put(Blocks.COBWEB, renderType3);
		hashMap.put(Blocks.GRASS, renderType3);
		hashMap.put(Blocks.FERN, renderType3);
		hashMap.put(Blocks.DEAD_BUSH, renderType3);
		hashMap.put(Blocks.SEAGRASS, renderType3);
		hashMap.put(Blocks.TALL_SEAGRASS, renderType3);
		hashMap.put(Blocks.DANDELION, renderType3);
		hashMap.put(Blocks.POPPY, renderType3);
		hashMap.put(Blocks.BLUE_ORCHID, renderType3);
		hashMap.put(Blocks.ALLIUM, renderType3);
		hashMap.put(Blocks.AZURE_BLUET, renderType3);
		hashMap.put(Blocks.RED_TULIP, renderType3);
		hashMap.put(Blocks.ORANGE_TULIP, renderType3);
		hashMap.put(Blocks.WHITE_TULIP, renderType3);
		hashMap.put(Blocks.PINK_TULIP, renderType3);
		hashMap.put(Blocks.OXEYE_DAISY, renderType3);
		hashMap.put(Blocks.CORNFLOWER, renderType3);
		hashMap.put(Blocks.WITHER_ROSE, renderType3);
		hashMap.put(Blocks.LILY_OF_THE_VALLEY, renderType3);
		hashMap.put(Blocks.BROWN_MUSHROOM, renderType3);
		hashMap.put(Blocks.RED_MUSHROOM, renderType3);
		hashMap.put(Blocks.TORCH, renderType3);
		hashMap.put(Blocks.WALL_TORCH, renderType3);
		hashMap.put(Blocks.SOUL_TORCH, renderType3);
		hashMap.put(Blocks.SOUL_WALL_TORCH, renderType3);
		hashMap.put(Blocks.FIRE, renderType3);
		hashMap.put(Blocks.SOUL_FIRE, renderType3);
		hashMap.put(Blocks.SPAWNER, renderType3);
		hashMap.put(Blocks.REDSTONE_WIRE, renderType3);
		hashMap.put(Blocks.WHEAT, renderType3);
		hashMap.put(Blocks.OAK_DOOR, renderType3);
		hashMap.put(Blocks.LADDER, renderType3);
		hashMap.put(Blocks.RAIL, renderType3);
		hashMap.put(Blocks.IRON_DOOR, renderType3);
		hashMap.put(Blocks.REDSTONE_TORCH, renderType3);
		hashMap.put(Blocks.REDSTONE_WALL_TORCH, renderType3);
		hashMap.put(Blocks.CACTUS, renderType3);
		hashMap.put(Blocks.SUGAR_CANE, renderType3);
		hashMap.put(Blocks.REPEATER, renderType3);
		hashMap.put(Blocks.OAK_TRAPDOOR, renderType3);
		hashMap.put(Blocks.SPRUCE_TRAPDOOR, renderType3);
		hashMap.put(Blocks.BIRCH_TRAPDOOR, renderType3);
		hashMap.put(Blocks.JUNGLE_TRAPDOOR, renderType3);
		hashMap.put(Blocks.ACACIA_TRAPDOOR, renderType3);
		hashMap.put(Blocks.DARK_OAK_TRAPDOOR, renderType3);
		hashMap.put(Blocks.CRIMSON_TRAPDOOR, renderType3);
		hashMap.put(Blocks.WARPED_TRAPDOOR, renderType3);
		hashMap.put(Blocks.ATTACHED_PUMPKIN_STEM, renderType3);
		hashMap.put(Blocks.ATTACHED_MELON_STEM, renderType3);
		hashMap.put(Blocks.PUMPKIN_STEM, renderType3);
		hashMap.put(Blocks.MELON_STEM, renderType3);
		hashMap.put(Blocks.VINE, renderType3);
		hashMap.put(Blocks.LILY_PAD, renderType3);
		hashMap.put(Blocks.NETHER_WART, renderType3);
		hashMap.put(Blocks.BREWING_STAND, renderType3);
		hashMap.put(Blocks.COCOA, renderType3);
		hashMap.put(Blocks.BEACON, renderType3);
		hashMap.put(Blocks.FLOWER_POT, renderType3);
		hashMap.put(Blocks.POTTED_OAK_SAPLING, renderType3);
		hashMap.put(Blocks.POTTED_SPRUCE_SAPLING, renderType3);
		hashMap.put(Blocks.POTTED_BIRCH_SAPLING, renderType3);
		hashMap.put(Blocks.POTTED_JUNGLE_SAPLING, renderType3);
		hashMap.put(Blocks.POTTED_ACACIA_SAPLING, renderType3);
		hashMap.put(Blocks.POTTED_DARK_OAK_SAPLING, renderType3);
		hashMap.put(Blocks.POTTED_FERN, renderType3);
		hashMap.put(Blocks.POTTED_DANDELION, renderType3);
		hashMap.put(Blocks.POTTED_POPPY, renderType3);
		hashMap.put(Blocks.POTTED_BLUE_ORCHID, renderType3);
		hashMap.put(Blocks.POTTED_ALLIUM, renderType3);
		hashMap.put(Blocks.POTTED_AZURE_BLUET, renderType3);
		hashMap.put(Blocks.POTTED_RED_TULIP, renderType3);
		hashMap.put(Blocks.POTTED_ORANGE_TULIP, renderType3);
		hashMap.put(Blocks.POTTED_WHITE_TULIP, renderType3);
		hashMap.put(Blocks.POTTED_PINK_TULIP, renderType3);
		hashMap.put(Blocks.POTTED_OXEYE_DAISY, renderType3);
		hashMap.put(Blocks.POTTED_CORNFLOWER, renderType3);
		hashMap.put(Blocks.POTTED_LILY_OF_THE_VALLEY, renderType3);
		hashMap.put(Blocks.POTTED_WITHER_ROSE, renderType3);
		hashMap.put(Blocks.POTTED_RED_MUSHROOM, renderType3);
		hashMap.put(Blocks.POTTED_BROWN_MUSHROOM, renderType3);
		hashMap.put(Blocks.POTTED_DEAD_BUSH, renderType3);
		hashMap.put(Blocks.POTTED_CACTUS, renderType3);
		hashMap.put(Blocks.CARROTS, renderType3);
		hashMap.put(Blocks.POTATOES, renderType3);
		hashMap.put(Blocks.COMPARATOR, renderType3);
		hashMap.put(Blocks.ACTIVATOR_RAIL, renderType3);
		hashMap.put(Blocks.IRON_TRAPDOOR, renderType3);
		hashMap.put(Blocks.SUNFLOWER, renderType3);
		hashMap.put(Blocks.LILAC, renderType3);
		hashMap.put(Blocks.ROSE_BUSH, renderType3);
		hashMap.put(Blocks.PEONY, renderType3);
		hashMap.put(Blocks.TALL_GRASS, renderType3);
		hashMap.put(Blocks.LARGE_FERN, renderType3);
		hashMap.put(Blocks.SPRUCE_DOOR, renderType3);
		hashMap.put(Blocks.BIRCH_DOOR, renderType3);
		hashMap.put(Blocks.JUNGLE_DOOR, renderType3);
		hashMap.put(Blocks.ACACIA_DOOR, renderType3);
		hashMap.put(Blocks.DARK_OAK_DOOR, renderType3);
		hashMap.put(Blocks.END_ROD, renderType3);
		hashMap.put(Blocks.CHORUS_PLANT, renderType3);
		hashMap.put(Blocks.CHORUS_FLOWER, renderType3);
		hashMap.put(Blocks.BEETROOTS, renderType3);
		hashMap.put(Blocks.KELP, renderType3);
		hashMap.put(Blocks.KELP_PLANT, renderType3);
		hashMap.put(Blocks.TURTLE_EGG, renderType3);
		hashMap.put(Blocks.DEAD_TUBE_CORAL, renderType3);
		hashMap.put(Blocks.DEAD_BRAIN_CORAL, renderType3);
		hashMap.put(Blocks.DEAD_BUBBLE_CORAL, renderType3);
		hashMap.put(Blocks.DEAD_FIRE_CORAL, renderType3);
		hashMap.put(Blocks.DEAD_HORN_CORAL, renderType3);
		hashMap.put(Blocks.TUBE_CORAL, renderType3);
		hashMap.put(Blocks.BRAIN_CORAL, renderType3);
		hashMap.put(Blocks.BUBBLE_CORAL, renderType3);
		hashMap.put(Blocks.FIRE_CORAL, renderType3);
		hashMap.put(Blocks.HORN_CORAL, renderType3);
		hashMap.put(Blocks.DEAD_TUBE_CORAL_FAN, renderType3);
		hashMap.put(Blocks.DEAD_BRAIN_CORAL_FAN, renderType3);
		hashMap.put(Blocks.DEAD_BUBBLE_CORAL_FAN, renderType3);
		hashMap.put(Blocks.DEAD_FIRE_CORAL_FAN, renderType3);
		hashMap.put(Blocks.DEAD_HORN_CORAL_FAN, renderType3);
		hashMap.put(Blocks.TUBE_CORAL_FAN, renderType3);
		hashMap.put(Blocks.BRAIN_CORAL_FAN, renderType3);
		hashMap.put(Blocks.BUBBLE_CORAL_FAN, renderType3);
		hashMap.put(Blocks.FIRE_CORAL_FAN, renderType3);
		hashMap.put(Blocks.HORN_CORAL_FAN, renderType3);
		hashMap.put(Blocks.DEAD_TUBE_CORAL_WALL_FAN, renderType3);
		hashMap.put(Blocks.DEAD_BRAIN_CORAL_WALL_FAN, renderType3);
		hashMap.put(Blocks.DEAD_BUBBLE_CORAL_WALL_FAN, renderType3);
		hashMap.put(Blocks.DEAD_FIRE_CORAL_WALL_FAN, renderType3);
		hashMap.put(Blocks.DEAD_HORN_CORAL_WALL_FAN, renderType3);
		hashMap.put(Blocks.TUBE_CORAL_WALL_FAN, renderType3);
		hashMap.put(Blocks.BRAIN_CORAL_WALL_FAN, renderType3);
		hashMap.put(Blocks.BUBBLE_CORAL_WALL_FAN, renderType3);
		hashMap.put(Blocks.FIRE_CORAL_WALL_FAN, renderType3);
		hashMap.put(Blocks.HORN_CORAL_WALL_FAN, renderType3);
		hashMap.put(Blocks.SEA_PICKLE, renderType3);
		hashMap.put(Blocks.CONDUIT, renderType3);
		hashMap.put(Blocks.BAMBOO_SAPLING, renderType3);
		hashMap.put(Blocks.BAMBOO, renderType3);
		hashMap.put(Blocks.POTTED_BAMBOO, renderType3);
		hashMap.put(Blocks.SCAFFOLDING, renderType3);
		hashMap.put(Blocks.STONECUTTER, renderType3);
		hashMap.put(Blocks.LANTERN, renderType3);
		hashMap.put(Blocks.SOUL_LANTERN, renderType3);
		hashMap.put(Blocks.CAMPFIRE, renderType3);
		hashMap.put(Blocks.SOUL_CAMPFIRE, renderType3);
		hashMap.put(Blocks.SWEET_BERRY_BUSH, renderType3);
		hashMap.put(Blocks.WEEPING_VINES, renderType3);
		hashMap.put(Blocks.WEEPING_VINES_PLANT, renderType3);
		hashMap.put(Blocks.TWISTING_VINES, renderType3);
		hashMap.put(Blocks.TWISTING_VINES_PLANT, renderType3);
		hashMap.put(Blocks.NETHER_SPROUTS, renderType3);
		hashMap.put(Blocks.CRIMSON_FUNGUS, renderType3);
		hashMap.put(Blocks.WARPED_FUNGUS, renderType3);
		hashMap.put(Blocks.CRIMSON_ROOTS, renderType3);
		hashMap.put(Blocks.WARPED_ROOTS, renderType3);
		hashMap.put(Blocks.POTTED_CRIMSON_FUNGUS, renderType3);
		hashMap.put(Blocks.POTTED_WARPED_FUNGUS, renderType3);
		hashMap.put(Blocks.POTTED_CRIMSON_ROOTS, renderType3);
		hashMap.put(Blocks.POTTED_WARPED_ROOTS, renderType3);
		hashMap.put(Blocks.CRIMSON_DOOR, renderType3);
		hashMap.put(Blocks.WARPED_DOOR, renderType3);
		RenderType renderType4 = RenderType.translucent();
		hashMap.put(Blocks.ICE, renderType4);
		hashMap.put(Blocks.NETHER_PORTAL, renderType4);
		hashMap.put(Blocks.WHITE_STAINED_GLASS, renderType4);
		hashMap.put(Blocks.ORANGE_STAINED_GLASS, renderType4);
		hashMap.put(Blocks.MAGENTA_STAINED_GLASS, renderType4);
		hashMap.put(Blocks.LIGHT_BLUE_STAINED_GLASS, renderType4);
		hashMap.put(Blocks.YELLOW_STAINED_GLASS, renderType4);
		hashMap.put(Blocks.LIME_STAINED_GLASS, renderType4);
		hashMap.put(Blocks.PINK_STAINED_GLASS, renderType4);
		hashMap.put(Blocks.GRAY_STAINED_GLASS, renderType4);
		hashMap.put(Blocks.LIGHT_GRAY_STAINED_GLASS, renderType4);
		hashMap.put(Blocks.CYAN_STAINED_GLASS, renderType4);
		hashMap.put(Blocks.PURPLE_STAINED_GLASS, renderType4);
		hashMap.put(Blocks.BLUE_STAINED_GLASS, renderType4);
		hashMap.put(Blocks.BROWN_STAINED_GLASS, renderType4);
		hashMap.put(Blocks.GREEN_STAINED_GLASS, renderType4);
		hashMap.put(Blocks.RED_STAINED_GLASS, renderType4);
		hashMap.put(Blocks.BLACK_STAINED_GLASS, renderType4);
		hashMap.put(Blocks.WHITE_STAINED_GLASS_PANE, renderType4);
		hashMap.put(Blocks.ORANGE_STAINED_GLASS_PANE, renderType4);
		hashMap.put(Blocks.MAGENTA_STAINED_GLASS_PANE, renderType4);
		hashMap.put(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, renderType4);
		hashMap.put(Blocks.YELLOW_STAINED_GLASS_PANE, renderType4);
		hashMap.put(Blocks.LIME_STAINED_GLASS_PANE, renderType4);
		hashMap.put(Blocks.PINK_STAINED_GLASS_PANE, renderType4);
		hashMap.put(Blocks.GRAY_STAINED_GLASS_PANE, renderType4);
		hashMap.put(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, renderType4);
		hashMap.put(Blocks.CYAN_STAINED_GLASS_PANE, renderType4);
		hashMap.put(Blocks.PURPLE_STAINED_GLASS_PANE, renderType4);
		hashMap.put(Blocks.BLUE_STAINED_GLASS_PANE, renderType4);
		hashMap.put(Blocks.BROWN_STAINED_GLASS_PANE, renderType4);
		hashMap.put(Blocks.GREEN_STAINED_GLASS_PANE, renderType4);
		hashMap.put(Blocks.RED_STAINED_GLASS_PANE, renderType4);
		hashMap.put(Blocks.BLACK_STAINED_GLASS_PANE, renderType4);
		hashMap.put(Blocks.SLIME_BLOCK, renderType4);
		hashMap.put(Blocks.HONEY_BLOCK, renderType4);
		hashMap.put(Blocks.FROSTED_ICE, renderType4);
		hashMap.put(Blocks.BUBBLE_COLUMN, renderType4);
	});
	private static final Map<Fluid, RenderType> TYPE_BY_FLUID = Util.make(Maps.<Fluid, RenderType>newHashMap(), hashMap -> {
		RenderType renderType = RenderType.translucent();
		hashMap.put(Fluids.FLOWING_WATER, renderType);
		hashMap.put(Fluids.WATER, renderType);
	});
	private static boolean renderCutout;

	public static RenderType getChunkRenderType(BlockState blockState) {
		Block block = blockState.getBlock();
		if (block instanceof LeavesBlock) {
			return renderCutout ? RenderType.cutoutMipped() : RenderType.solid();
		} else {
			RenderType renderType = (RenderType)TYPE_BY_BLOCK.get(block);
			return renderType != null ? renderType : RenderType.solid();
		}
	}

	public static RenderType getMovingBlockRenderType(BlockState blockState) {
		Block block = blockState.getBlock();
		if (block instanceof LeavesBlock) {
			return renderCutout ? RenderType.cutoutMipped() : RenderType.solid();
		} else {
			RenderType renderType = (RenderType)TYPE_BY_BLOCK.get(block);
			if (renderType != null) {
				return renderType == RenderType.translucent() ? RenderType.translucentMovingBlock() : renderType;
			} else {
				return RenderType.solid();
			}
		}
	}

	public static RenderType getRenderType(BlockState blockState, boolean bl) {
		RenderType renderType = getChunkRenderType(blockState);
		if (renderType == RenderType.translucent()) {
			if (!Minecraft.useShaderTransparency()) {
				return Sheets.translucentCullBlockSheet();
			} else {
				return bl ? Sheets.translucentCullBlockSheet() : Sheets.translucentItemSheet();
			}
		} else {
			return Sheets.cutoutBlockSheet();
		}
	}

	public static RenderType getRenderType(ItemStack itemStack, boolean bl) {
		Item item = itemStack.getItem();
		if (item instanceof BlockItem) {
			Block block = ((BlockItem)item).getBlock();
			return getRenderType(block.defaultBlockState(), bl);
		} else {
			return bl ? Sheets.translucentCullBlockSheet() : Sheets.translucentItemSheet();
		}
	}

	public static RenderType getRenderLayer(FluidState fluidState) {
		RenderType renderType = (RenderType)TYPE_BY_FLUID.get(fluidState.getType());
		return renderType != null ? renderType : RenderType.solid();
	}

	public static void setFancy(boolean bl) {
		renderCutout = bl;
	}
}
