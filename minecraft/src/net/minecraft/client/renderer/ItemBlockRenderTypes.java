package net.minecraft.client.renderer;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
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
		RenderType renderType = RenderType.cutoutMipped();
		hashMap.put(Blocks.GRASS_BLOCK, renderType);
		hashMap.put(Blocks.IRON_BARS, renderType);
		hashMap.put(Blocks.GLASS_PANE, renderType);
		hashMap.put(Blocks.TRIPWIRE_HOOK, renderType);
		hashMap.put(Blocks.HOPPER, renderType);
		hashMap.put(Blocks.JUNGLE_LEAVES, renderType);
		hashMap.put(Blocks.OAK_LEAVES, renderType);
		hashMap.put(Blocks.SPRUCE_LEAVES, renderType);
		hashMap.put(Blocks.ACACIA_LEAVES, renderType);
		hashMap.put(Blocks.BIRCH_LEAVES, renderType);
		hashMap.put(Blocks.DARK_OAK_LEAVES, renderType);
		RenderType renderType2 = RenderType.cutout();
		hashMap.put(Blocks.OAK_SAPLING, renderType2);
		hashMap.put(Blocks.SPRUCE_SAPLING, renderType2);
		hashMap.put(Blocks.BIRCH_SAPLING, renderType2);
		hashMap.put(Blocks.JUNGLE_SAPLING, renderType2);
		hashMap.put(Blocks.ACACIA_SAPLING, renderType2);
		hashMap.put(Blocks.DARK_OAK_SAPLING, renderType2);
		hashMap.put(Blocks.GLASS, renderType2);
		hashMap.put(Blocks.WHITE_BED, renderType2);
		hashMap.put(Blocks.ORANGE_BED, renderType2);
		hashMap.put(Blocks.MAGENTA_BED, renderType2);
		hashMap.put(Blocks.LIGHT_BLUE_BED, renderType2);
		hashMap.put(Blocks.YELLOW_BED, renderType2);
		hashMap.put(Blocks.LIME_BED, renderType2);
		hashMap.put(Blocks.PINK_BED, renderType2);
		hashMap.put(Blocks.GRAY_BED, renderType2);
		hashMap.put(Blocks.LIGHT_GRAY_BED, renderType2);
		hashMap.put(Blocks.CYAN_BED, renderType2);
		hashMap.put(Blocks.PURPLE_BED, renderType2);
		hashMap.put(Blocks.BLUE_BED, renderType2);
		hashMap.put(Blocks.BROWN_BED, renderType2);
		hashMap.put(Blocks.GREEN_BED, renderType2);
		hashMap.put(Blocks.RED_BED, renderType2);
		hashMap.put(Blocks.BLACK_BED, renderType2);
		hashMap.put(Blocks.POWERED_RAIL, renderType2);
		hashMap.put(Blocks.DETECTOR_RAIL, renderType2);
		hashMap.put(Blocks.COBWEB, renderType2);
		hashMap.put(Blocks.GRASS, renderType2);
		hashMap.put(Blocks.FERN, renderType2);
		hashMap.put(Blocks.DEAD_BUSH, renderType2);
		hashMap.put(Blocks.SEAGRASS, renderType2);
		hashMap.put(Blocks.TALL_SEAGRASS, renderType2);
		hashMap.put(Blocks.DANDELION, renderType2);
		hashMap.put(Blocks.POPPY, renderType2);
		hashMap.put(Blocks.BLUE_ORCHID, renderType2);
		hashMap.put(Blocks.ALLIUM, renderType2);
		hashMap.put(Blocks.AZURE_BLUET, renderType2);
		hashMap.put(Blocks.RED_TULIP, renderType2);
		hashMap.put(Blocks.ORANGE_TULIP, renderType2);
		hashMap.put(Blocks.WHITE_TULIP, renderType2);
		hashMap.put(Blocks.PINK_TULIP, renderType2);
		hashMap.put(Blocks.OXEYE_DAISY, renderType2);
		hashMap.put(Blocks.CORNFLOWER, renderType2);
		hashMap.put(Blocks.WITHER_ROSE, renderType2);
		hashMap.put(Blocks.LILY_OF_THE_VALLEY, renderType2);
		hashMap.put(Blocks.BROWN_MUSHROOM, renderType2);
		hashMap.put(Blocks.RED_MUSHROOM, renderType2);
		hashMap.put(Blocks.TORCH, renderType2);
		hashMap.put(Blocks.WALL_TORCH, renderType2);
		hashMap.put(Blocks.FIRE, renderType2);
		hashMap.put(Blocks.SPAWNER, renderType2);
		hashMap.put(Blocks.REDSTONE_WIRE, renderType2);
		hashMap.put(Blocks.WHEAT, renderType2);
		hashMap.put(Blocks.OAK_DOOR, renderType2);
		hashMap.put(Blocks.LADDER, renderType2);
		hashMap.put(Blocks.RAIL, renderType2);
		hashMap.put(Blocks.IRON_DOOR, renderType2);
		hashMap.put(Blocks.REDSTONE_TORCH, renderType2);
		hashMap.put(Blocks.REDSTONE_WALL_TORCH, renderType2);
		hashMap.put(Blocks.CACTUS, renderType2);
		hashMap.put(Blocks.SUGAR_CANE, renderType2);
		hashMap.put(Blocks.REPEATER, renderType2);
		hashMap.put(Blocks.OAK_TRAPDOOR, renderType2);
		hashMap.put(Blocks.SPRUCE_TRAPDOOR, renderType2);
		hashMap.put(Blocks.BIRCH_TRAPDOOR, renderType2);
		hashMap.put(Blocks.JUNGLE_TRAPDOOR, renderType2);
		hashMap.put(Blocks.ACACIA_TRAPDOOR, renderType2);
		hashMap.put(Blocks.DARK_OAK_TRAPDOOR, renderType2);
		hashMap.put(Blocks.ATTACHED_PUMPKIN_STEM, renderType2);
		hashMap.put(Blocks.ATTACHED_MELON_STEM, renderType2);
		hashMap.put(Blocks.PUMPKIN_STEM, renderType2);
		hashMap.put(Blocks.MELON_STEM, renderType2);
		hashMap.put(Blocks.VINE, renderType2);
		hashMap.put(Blocks.LILY_PAD, renderType2);
		hashMap.put(Blocks.NETHER_WART, renderType2);
		hashMap.put(Blocks.BREWING_STAND, renderType2);
		hashMap.put(Blocks.COCOA, renderType2);
		hashMap.put(Blocks.BEACON, renderType2);
		hashMap.put(Blocks.FLOWER_POT, renderType2);
		hashMap.put(Blocks.POTTED_OAK_SAPLING, renderType2);
		hashMap.put(Blocks.POTTED_SPRUCE_SAPLING, renderType2);
		hashMap.put(Blocks.POTTED_BIRCH_SAPLING, renderType2);
		hashMap.put(Blocks.POTTED_JUNGLE_SAPLING, renderType2);
		hashMap.put(Blocks.POTTED_ACACIA_SAPLING, renderType2);
		hashMap.put(Blocks.POTTED_DARK_OAK_SAPLING, renderType2);
		hashMap.put(Blocks.POTTED_FERN, renderType2);
		hashMap.put(Blocks.POTTED_DANDELION, renderType2);
		hashMap.put(Blocks.POTTED_POPPY, renderType2);
		hashMap.put(Blocks.POTTED_BLUE_ORCHID, renderType2);
		hashMap.put(Blocks.POTTED_ALLIUM, renderType2);
		hashMap.put(Blocks.POTTED_AZURE_BLUET, renderType2);
		hashMap.put(Blocks.POTTED_RED_TULIP, renderType2);
		hashMap.put(Blocks.POTTED_ORANGE_TULIP, renderType2);
		hashMap.put(Blocks.POTTED_WHITE_TULIP, renderType2);
		hashMap.put(Blocks.POTTED_PINK_TULIP, renderType2);
		hashMap.put(Blocks.POTTED_OXEYE_DAISY, renderType2);
		hashMap.put(Blocks.POTTED_CORNFLOWER, renderType2);
		hashMap.put(Blocks.POTTED_LILY_OF_THE_VALLEY, renderType2);
		hashMap.put(Blocks.POTTED_WITHER_ROSE, renderType2);
		hashMap.put(Blocks.POTTED_RED_MUSHROOM, renderType2);
		hashMap.put(Blocks.POTTED_BROWN_MUSHROOM, renderType2);
		hashMap.put(Blocks.POTTED_DEAD_BUSH, renderType2);
		hashMap.put(Blocks.POTTED_CACTUS, renderType2);
		hashMap.put(Blocks.CARROTS, renderType2);
		hashMap.put(Blocks.POTATOES, renderType2);
		hashMap.put(Blocks.COMPARATOR, renderType2);
		hashMap.put(Blocks.ACTIVATOR_RAIL, renderType2);
		hashMap.put(Blocks.IRON_TRAPDOOR, renderType2);
		hashMap.put(Blocks.SUNFLOWER, renderType2);
		hashMap.put(Blocks.LILAC, renderType2);
		hashMap.put(Blocks.ROSE_BUSH, renderType2);
		hashMap.put(Blocks.PEONY, renderType2);
		hashMap.put(Blocks.TALL_GRASS, renderType2);
		hashMap.put(Blocks.LARGE_FERN, renderType2);
		hashMap.put(Blocks.SPRUCE_DOOR, renderType2);
		hashMap.put(Blocks.BIRCH_DOOR, renderType2);
		hashMap.put(Blocks.JUNGLE_DOOR, renderType2);
		hashMap.put(Blocks.ACACIA_DOOR, renderType2);
		hashMap.put(Blocks.DARK_OAK_DOOR, renderType2);
		hashMap.put(Blocks.END_ROD, renderType2);
		hashMap.put(Blocks.CHORUS_PLANT, renderType2);
		hashMap.put(Blocks.CHORUS_FLOWER, renderType2);
		hashMap.put(Blocks.BEETROOTS, renderType2);
		hashMap.put(Blocks.KELP, renderType2);
		hashMap.put(Blocks.KELP_PLANT, renderType2);
		hashMap.put(Blocks.TURTLE_EGG, renderType2);
		hashMap.put(Blocks.DEAD_TUBE_CORAL, renderType2);
		hashMap.put(Blocks.DEAD_BRAIN_CORAL, renderType2);
		hashMap.put(Blocks.DEAD_BUBBLE_CORAL, renderType2);
		hashMap.put(Blocks.DEAD_FIRE_CORAL, renderType2);
		hashMap.put(Blocks.DEAD_HORN_CORAL, renderType2);
		hashMap.put(Blocks.TUBE_CORAL, renderType2);
		hashMap.put(Blocks.BRAIN_CORAL, renderType2);
		hashMap.put(Blocks.BUBBLE_CORAL, renderType2);
		hashMap.put(Blocks.FIRE_CORAL, renderType2);
		hashMap.put(Blocks.HORN_CORAL, renderType2);
		hashMap.put(Blocks.DEAD_TUBE_CORAL_FAN, renderType2);
		hashMap.put(Blocks.DEAD_BRAIN_CORAL_FAN, renderType2);
		hashMap.put(Blocks.DEAD_BUBBLE_CORAL_FAN, renderType2);
		hashMap.put(Blocks.DEAD_FIRE_CORAL_FAN, renderType2);
		hashMap.put(Blocks.DEAD_HORN_CORAL_FAN, renderType2);
		hashMap.put(Blocks.TUBE_CORAL_FAN, renderType2);
		hashMap.put(Blocks.BRAIN_CORAL_FAN, renderType2);
		hashMap.put(Blocks.BUBBLE_CORAL_FAN, renderType2);
		hashMap.put(Blocks.FIRE_CORAL_FAN, renderType2);
		hashMap.put(Blocks.HORN_CORAL_FAN, renderType2);
		hashMap.put(Blocks.DEAD_TUBE_CORAL_WALL_FAN, renderType2);
		hashMap.put(Blocks.DEAD_BRAIN_CORAL_WALL_FAN, renderType2);
		hashMap.put(Blocks.DEAD_BUBBLE_CORAL_WALL_FAN, renderType2);
		hashMap.put(Blocks.DEAD_FIRE_CORAL_WALL_FAN, renderType2);
		hashMap.put(Blocks.DEAD_HORN_CORAL_WALL_FAN, renderType2);
		hashMap.put(Blocks.TUBE_CORAL_WALL_FAN, renderType2);
		hashMap.put(Blocks.BRAIN_CORAL_WALL_FAN, renderType2);
		hashMap.put(Blocks.BUBBLE_CORAL_WALL_FAN, renderType2);
		hashMap.put(Blocks.FIRE_CORAL_WALL_FAN, renderType2);
		hashMap.put(Blocks.HORN_CORAL_WALL_FAN, renderType2);
		hashMap.put(Blocks.SEA_PICKLE, renderType2);
		hashMap.put(Blocks.CONDUIT, renderType2);
		hashMap.put(Blocks.BAMBOO_SAPLING, renderType2);
		hashMap.put(Blocks.BAMBOO, renderType2);
		hashMap.put(Blocks.POTTED_BAMBOO, renderType2);
		hashMap.put(Blocks.SCAFFOLDING, renderType2);
		hashMap.put(Blocks.STONECUTTER, renderType2);
		hashMap.put(Blocks.LANTERN, renderType2);
		hashMap.put(Blocks.CAMPFIRE, renderType2);
		hashMap.put(Blocks.SWEET_BERRY_BUSH, renderType2);
		RenderType renderType3 = RenderType.translucent();
		hashMap.put(Blocks.ICE, renderType3);
		hashMap.put(Blocks.NETHER_PORTAL, renderType3);
		hashMap.put(Blocks.WHITE_STAINED_GLASS, renderType3);
		hashMap.put(Blocks.ORANGE_STAINED_GLASS, renderType3);
		hashMap.put(Blocks.MAGENTA_STAINED_GLASS, renderType3);
		hashMap.put(Blocks.LIGHT_BLUE_STAINED_GLASS, renderType3);
		hashMap.put(Blocks.YELLOW_STAINED_GLASS, renderType3);
		hashMap.put(Blocks.LIME_STAINED_GLASS, renderType3);
		hashMap.put(Blocks.PINK_STAINED_GLASS, renderType3);
		hashMap.put(Blocks.GRAY_STAINED_GLASS, renderType3);
		hashMap.put(Blocks.LIGHT_GRAY_STAINED_GLASS, renderType3);
		hashMap.put(Blocks.CYAN_STAINED_GLASS, renderType3);
		hashMap.put(Blocks.PURPLE_STAINED_GLASS, renderType3);
		hashMap.put(Blocks.BLUE_STAINED_GLASS, renderType3);
		hashMap.put(Blocks.BROWN_STAINED_GLASS, renderType3);
		hashMap.put(Blocks.GREEN_STAINED_GLASS, renderType3);
		hashMap.put(Blocks.RED_STAINED_GLASS, renderType3);
		hashMap.put(Blocks.BLACK_STAINED_GLASS, renderType3);
		hashMap.put(Blocks.TRIPWIRE, renderType3);
		hashMap.put(Blocks.WHITE_STAINED_GLASS_PANE, renderType3);
		hashMap.put(Blocks.ORANGE_STAINED_GLASS_PANE, renderType3);
		hashMap.put(Blocks.MAGENTA_STAINED_GLASS_PANE, renderType3);
		hashMap.put(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, renderType3);
		hashMap.put(Blocks.YELLOW_STAINED_GLASS_PANE, renderType3);
		hashMap.put(Blocks.LIME_STAINED_GLASS_PANE, renderType3);
		hashMap.put(Blocks.PINK_STAINED_GLASS_PANE, renderType3);
		hashMap.put(Blocks.GRAY_STAINED_GLASS_PANE, renderType3);
		hashMap.put(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, renderType3);
		hashMap.put(Blocks.CYAN_STAINED_GLASS_PANE, renderType3);
		hashMap.put(Blocks.PURPLE_STAINED_GLASS_PANE, renderType3);
		hashMap.put(Blocks.BLUE_STAINED_GLASS_PANE, renderType3);
		hashMap.put(Blocks.BROWN_STAINED_GLASS_PANE, renderType3);
		hashMap.put(Blocks.GREEN_STAINED_GLASS_PANE, renderType3);
		hashMap.put(Blocks.RED_STAINED_GLASS_PANE, renderType3);
		hashMap.put(Blocks.BLACK_STAINED_GLASS_PANE, renderType3);
		hashMap.put(Blocks.SLIME_BLOCK, renderType3);
		hashMap.put(Blocks.HONEY_BLOCK, renderType3);
		hashMap.put(Blocks.FROSTED_ICE, renderType3);
		hashMap.put(Blocks.BUBBLE_COLUMN, renderType3);
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

	public static RenderType getRenderType(BlockState blockState) {
		RenderType renderType = getChunkRenderType(blockState);
		return renderType == RenderType.translucent() ? Sheets.translucentBlockSheet() : Sheets.cutoutBlockSheet();
	}

	public static RenderType getRenderType(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item instanceof BlockItem) {
			Block block = ((BlockItem)item).getBlock();
			return getRenderType(block.defaultBlockState());
		} else {
			return Sheets.translucentBlockSheet();
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