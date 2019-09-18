package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.TextureObject;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

@Environment(EnvType.CLIENT)
public class RenderType {
	private static final Set<RenderType> LAYERS = Sets.<RenderType>newHashSet();
	public static final RenderType SOLID = register(new RenderType("solid", 2097152, () -> {
	}, () -> {
	}));
	public static final RenderType CUTOUT_MIPPED = register(new RenderType("cutout_mipped", 131072, () -> {
		RenderSystem.enableAlphaTest();
		RenderSystem.alphaFunc(516, 0.5F);
	}, () -> {
		RenderSystem.disableAlphaTest();
		RenderSystem.defaultAlphaFunc();
	}));
	public static final RenderType CUTOUT = register(new RenderType("cutout", 131072, () -> {
		TextureObject textureObject = Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
		textureObject.bind();
		textureObject.pushFilter(false, false);
		CUTOUT_MIPPED.setupRenderState();
	}, () -> {
		TextureObject textureObject = Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
		textureObject.bind();
		textureObject.popFilter();
		CUTOUT_MIPPED.clearRenderState();
	}));
	public static final RenderType TRANSLUCENT = register(new RenderType("translucent", 262144, () -> {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(false);
	}, () -> {
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
	}));
	public static final RenderType ENTITY = register(new RenderType("entity", 262144, () -> {
		TextureObject textureObject = Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
		textureObject.bind();
		Lighting.turnOff();
		RenderSystem.blendFunc(770, 771);
		RenderSystem.enableBlend();
		if (Minecraft.useAmbientOcclusion()) {
			RenderSystem.shadeModel(7425);
		} else {
			RenderSystem.shadeModel(7424);
		}
	}, () -> Lighting.turnOn()));
	public static final RenderType CRUMBLING = register(
		new RenderType(
			"crumbling",
			262144,
			() -> {
				RenderSystem.polygonOffset(-1.0F, -10.0F);
				RenderSystem.enablePolygonOffset();
				RenderSystem.defaultAlphaFunc();
				RenderSystem.enableAlphaTest();
				RenderSystem.enableBlend();
				RenderSystem.blendFuncSeparate(
					GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
				);
			},
			() -> {
				RenderSystem.disableAlphaTest();
				RenderSystem.polygonOffset(0.0F, 0.0F);
				RenderSystem.disablePolygonOffset();
				RenderSystem.enableAlphaTest();
				RenderSystem.disableBlend();
			}
		)
	);
	private static boolean renderCutout;
	private static final Map<Block, RenderType> TYPE_BY_BLOCK = Util.make(Maps.<Block, RenderType>newHashMap(), hashMap -> {
		hashMap.put(Blocks.GRASS_BLOCK, CUTOUT_MIPPED);
		hashMap.put(Blocks.IRON_BARS, CUTOUT_MIPPED);
		hashMap.put(Blocks.GLASS_PANE, CUTOUT_MIPPED);
		hashMap.put(Blocks.TRIPWIRE_HOOK, CUTOUT_MIPPED);
		hashMap.put(Blocks.HOPPER, CUTOUT_MIPPED);
		hashMap.put(Blocks.JUNGLE_LEAVES, CUTOUT_MIPPED);
		hashMap.put(Blocks.OAK_LEAVES, CUTOUT_MIPPED);
		hashMap.put(Blocks.SPRUCE_LEAVES, CUTOUT_MIPPED);
		hashMap.put(Blocks.ACACIA_LEAVES, CUTOUT_MIPPED);
		hashMap.put(Blocks.BIRCH_LEAVES, CUTOUT_MIPPED);
		hashMap.put(Blocks.DARK_OAK_LEAVES, CUTOUT_MIPPED);
		hashMap.put(Blocks.OAK_SAPLING, CUTOUT);
		hashMap.put(Blocks.SPRUCE_SAPLING, CUTOUT);
		hashMap.put(Blocks.BIRCH_SAPLING, CUTOUT);
		hashMap.put(Blocks.JUNGLE_SAPLING, CUTOUT);
		hashMap.put(Blocks.ACACIA_SAPLING, CUTOUT);
		hashMap.put(Blocks.DARK_OAK_SAPLING, CUTOUT);
		hashMap.put(Blocks.GLASS, CUTOUT);
		hashMap.put(Blocks.WHITE_BED, CUTOUT);
		hashMap.put(Blocks.ORANGE_BED, CUTOUT);
		hashMap.put(Blocks.MAGENTA_BED, CUTOUT);
		hashMap.put(Blocks.LIGHT_BLUE_BED, CUTOUT);
		hashMap.put(Blocks.YELLOW_BED, CUTOUT);
		hashMap.put(Blocks.LIME_BED, CUTOUT);
		hashMap.put(Blocks.PINK_BED, CUTOUT);
		hashMap.put(Blocks.GRAY_BED, CUTOUT);
		hashMap.put(Blocks.LIGHT_GRAY_BED, CUTOUT);
		hashMap.put(Blocks.CYAN_BED, CUTOUT);
		hashMap.put(Blocks.PURPLE_BED, CUTOUT);
		hashMap.put(Blocks.BLUE_BED, CUTOUT);
		hashMap.put(Blocks.BROWN_BED, CUTOUT);
		hashMap.put(Blocks.GREEN_BED, CUTOUT);
		hashMap.put(Blocks.RED_BED, CUTOUT);
		hashMap.put(Blocks.BLACK_BED, CUTOUT);
		hashMap.put(Blocks.POWERED_RAIL, CUTOUT);
		hashMap.put(Blocks.DETECTOR_RAIL, CUTOUT);
		hashMap.put(Blocks.COBWEB, CUTOUT);
		hashMap.put(Blocks.GRASS, CUTOUT);
		hashMap.put(Blocks.FERN, CUTOUT);
		hashMap.put(Blocks.DEAD_BUSH, CUTOUT);
		hashMap.put(Blocks.SEAGRASS, CUTOUT);
		hashMap.put(Blocks.TALL_SEAGRASS, CUTOUT);
		hashMap.put(Blocks.DANDELION, CUTOUT);
		hashMap.put(Blocks.POPPY, CUTOUT);
		hashMap.put(Blocks.BLUE_ORCHID, CUTOUT);
		hashMap.put(Blocks.ALLIUM, CUTOUT);
		hashMap.put(Blocks.AZURE_BLUET, CUTOUT);
		hashMap.put(Blocks.RED_TULIP, CUTOUT);
		hashMap.put(Blocks.ORANGE_TULIP, CUTOUT);
		hashMap.put(Blocks.WHITE_TULIP, CUTOUT);
		hashMap.put(Blocks.PINK_TULIP, CUTOUT);
		hashMap.put(Blocks.OXEYE_DAISY, CUTOUT);
		hashMap.put(Blocks.CORNFLOWER, CUTOUT);
		hashMap.put(Blocks.WITHER_ROSE, CUTOUT);
		hashMap.put(Blocks.LILY_OF_THE_VALLEY, CUTOUT);
		hashMap.put(Blocks.BROWN_MUSHROOM, CUTOUT);
		hashMap.put(Blocks.RED_MUSHROOM, CUTOUT);
		hashMap.put(Blocks.TORCH, CUTOUT);
		hashMap.put(Blocks.WALL_TORCH, CUTOUT);
		hashMap.put(Blocks.FIRE, CUTOUT);
		hashMap.put(Blocks.SPAWNER, CUTOUT);
		hashMap.put(Blocks.REDSTONE_WIRE, CUTOUT);
		hashMap.put(Blocks.WHEAT, CUTOUT);
		hashMap.put(Blocks.OAK_DOOR, CUTOUT);
		hashMap.put(Blocks.LADDER, CUTOUT);
		hashMap.put(Blocks.RAIL, CUTOUT);
		hashMap.put(Blocks.IRON_DOOR, CUTOUT);
		hashMap.put(Blocks.REDSTONE_TORCH, CUTOUT);
		hashMap.put(Blocks.REDSTONE_WALL_TORCH, CUTOUT);
		hashMap.put(Blocks.CACTUS, CUTOUT);
		hashMap.put(Blocks.SUGAR_CANE, CUTOUT);
		hashMap.put(Blocks.REPEATER, CUTOUT);
		hashMap.put(Blocks.OAK_TRAPDOOR, CUTOUT);
		hashMap.put(Blocks.SPRUCE_TRAPDOOR, CUTOUT);
		hashMap.put(Blocks.BIRCH_TRAPDOOR, CUTOUT);
		hashMap.put(Blocks.JUNGLE_TRAPDOOR, CUTOUT);
		hashMap.put(Blocks.ACACIA_TRAPDOOR, CUTOUT);
		hashMap.put(Blocks.DARK_OAK_TRAPDOOR, CUTOUT);
		hashMap.put(Blocks.ATTACHED_PUMPKIN_STEM, CUTOUT);
		hashMap.put(Blocks.ATTACHED_MELON_STEM, CUTOUT);
		hashMap.put(Blocks.PUMPKIN_STEM, CUTOUT);
		hashMap.put(Blocks.MELON_STEM, CUTOUT);
		hashMap.put(Blocks.VINE, CUTOUT);
		hashMap.put(Blocks.LILY_PAD, CUTOUT);
		hashMap.put(Blocks.NETHER_WART, CUTOUT);
		hashMap.put(Blocks.BREWING_STAND, CUTOUT);
		hashMap.put(Blocks.COCOA, CUTOUT);
		hashMap.put(Blocks.BEACON, CUTOUT);
		hashMap.put(Blocks.FLOWER_POT, CUTOUT);
		hashMap.put(Blocks.POTTED_OAK_SAPLING, CUTOUT);
		hashMap.put(Blocks.POTTED_SPRUCE_SAPLING, CUTOUT);
		hashMap.put(Blocks.POTTED_BIRCH_SAPLING, CUTOUT);
		hashMap.put(Blocks.POTTED_JUNGLE_SAPLING, CUTOUT);
		hashMap.put(Blocks.POTTED_ACACIA_SAPLING, CUTOUT);
		hashMap.put(Blocks.POTTED_DARK_OAK_SAPLING, CUTOUT);
		hashMap.put(Blocks.POTTED_FERN, CUTOUT);
		hashMap.put(Blocks.POTTED_DANDELION, CUTOUT);
		hashMap.put(Blocks.POTTED_POPPY, CUTOUT);
		hashMap.put(Blocks.POTTED_BLUE_ORCHID, CUTOUT);
		hashMap.put(Blocks.POTTED_ALLIUM, CUTOUT);
		hashMap.put(Blocks.POTTED_AZURE_BLUET, CUTOUT);
		hashMap.put(Blocks.POTTED_RED_TULIP, CUTOUT);
		hashMap.put(Blocks.POTTED_ORANGE_TULIP, CUTOUT);
		hashMap.put(Blocks.POTTED_WHITE_TULIP, CUTOUT);
		hashMap.put(Blocks.POTTED_PINK_TULIP, CUTOUT);
		hashMap.put(Blocks.POTTED_OXEYE_DAISY, CUTOUT);
		hashMap.put(Blocks.POTTED_CORNFLOWER, CUTOUT);
		hashMap.put(Blocks.POTTED_LILY_OF_THE_VALLEY, CUTOUT);
		hashMap.put(Blocks.POTTED_WITHER_ROSE, CUTOUT);
		hashMap.put(Blocks.POTTED_RED_MUSHROOM, CUTOUT);
		hashMap.put(Blocks.POTTED_BROWN_MUSHROOM, CUTOUT);
		hashMap.put(Blocks.POTTED_DEAD_BUSH, CUTOUT);
		hashMap.put(Blocks.POTTED_CACTUS, CUTOUT);
		hashMap.put(Blocks.CARROTS, CUTOUT);
		hashMap.put(Blocks.POTATOES, CUTOUT);
		hashMap.put(Blocks.COMPARATOR, CUTOUT);
		hashMap.put(Blocks.ACTIVATOR_RAIL, CUTOUT);
		hashMap.put(Blocks.IRON_TRAPDOOR, CUTOUT);
		hashMap.put(Blocks.SUNFLOWER, CUTOUT);
		hashMap.put(Blocks.LILAC, CUTOUT);
		hashMap.put(Blocks.ROSE_BUSH, CUTOUT);
		hashMap.put(Blocks.PEONY, CUTOUT);
		hashMap.put(Blocks.TALL_GRASS, CUTOUT);
		hashMap.put(Blocks.LARGE_FERN, CUTOUT);
		hashMap.put(Blocks.SPRUCE_DOOR, CUTOUT);
		hashMap.put(Blocks.BIRCH_DOOR, CUTOUT);
		hashMap.put(Blocks.JUNGLE_DOOR, CUTOUT);
		hashMap.put(Blocks.ACACIA_DOOR, CUTOUT);
		hashMap.put(Blocks.DARK_OAK_DOOR, CUTOUT);
		hashMap.put(Blocks.END_ROD, CUTOUT);
		hashMap.put(Blocks.CHORUS_PLANT, CUTOUT);
		hashMap.put(Blocks.CHORUS_FLOWER, CUTOUT);
		hashMap.put(Blocks.BEETROOTS, CUTOUT);
		hashMap.put(Blocks.KELP, CUTOUT);
		hashMap.put(Blocks.KELP_PLANT, CUTOUT);
		hashMap.put(Blocks.TURTLE_EGG, CUTOUT);
		hashMap.put(Blocks.DEAD_TUBE_CORAL, CUTOUT);
		hashMap.put(Blocks.DEAD_BRAIN_CORAL, CUTOUT);
		hashMap.put(Blocks.DEAD_BUBBLE_CORAL, CUTOUT);
		hashMap.put(Blocks.DEAD_FIRE_CORAL, CUTOUT);
		hashMap.put(Blocks.DEAD_HORN_CORAL, CUTOUT);
		hashMap.put(Blocks.TUBE_CORAL, CUTOUT);
		hashMap.put(Blocks.BRAIN_CORAL, CUTOUT);
		hashMap.put(Blocks.BUBBLE_CORAL, CUTOUT);
		hashMap.put(Blocks.FIRE_CORAL, CUTOUT);
		hashMap.put(Blocks.HORN_CORAL, CUTOUT);
		hashMap.put(Blocks.DEAD_TUBE_CORAL_FAN, CUTOUT);
		hashMap.put(Blocks.DEAD_BRAIN_CORAL_FAN, CUTOUT);
		hashMap.put(Blocks.DEAD_BUBBLE_CORAL_FAN, CUTOUT);
		hashMap.put(Blocks.DEAD_FIRE_CORAL_FAN, CUTOUT);
		hashMap.put(Blocks.DEAD_HORN_CORAL_FAN, CUTOUT);
		hashMap.put(Blocks.TUBE_CORAL_FAN, CUTOUT);
		hashMap.put(Blocks.BRAIN_CORAL_FAN, CUTOUT);
		hashMap.put(Blocks.BUBBLE_CORAL_FAN, CUTOUT);
		hashMap.put(Blocks.FIRE_CORAL_FAN, CUTOUT);
		hashMap.put(Blocks.HORN_CORAL_FAN, CUTOUT);
		hashMap.put(Blocks.DEAD_TUBE_CORAL_WALL_FAN, CUTOUT);
		hashMap.put(Blocks.DEAD_BRAIN_CORAL_WALL_FAN, CUTOUT);
		hashMap.put(Blocks.DEAD_BUBBLE_CORAL_WALL_FAN, CUTOUT);
		hashMap.put(Blocks.DEAD_FIRE_CORAL_WALL_FAN, CUTOUT);
		hashMap.put(Blocks.DEAD_HORN_CORAL_WALL_FAN, CUTOUT);
		hashMap.put(Blocks.TUBE_CORAL_WALL_FAN, CUTOUT);
		hashMap.put(Blocks.BRAIN_CORAL_WALL_FAN, CUTOUT);
		hashMap.put(Blocks.BUBBLE_CORAL_WALL_FAN, CUTOUT);
		hashMap.put(Blocks.FIRE_CORAL_WALL_FAN, CUTOUT);
		hashMap.put(Blocks.HORN_CORAL_WALL_FAN, CUTOUT);
		hashMap.put(Blocks.SEA_PICKLE, CUTOUT);
		hashMap.put(Blocks.CONDUIT, CUTOUT);
		hashMap.put(Blocks.BAMBOO_SAPLING, CUTOUT);
		hashMap.put(Blocks.BAMBOO, CUTOUT);
		hashMap.put(Blocks.POTTED_BAMBOO, CUTOUT);
		hashMap.put(Blocks.SCAFFOLDING, CUTOUT);
		hashMap.put(Blocks.STONECUTTER, CUTOUT);
		hashMap.put(Blocks.LANTERN, CUTOUT);
		hashMap.put(Blocks.CAMPFIRE, CUTOUT);
		hashMap.put(Blocks.SWEET_BERRY_BUSH, CUTOUT);
		hashMap.put(Blocks.ICE, TRANSLUCENT);
		hashMap.put(Blocks.NETHER_PORTAL, TRANSLUCENT);
		hashMap.put(Blocks.WHITE_STAINED_GLASS, TRANSLUCENT);
		hashMap.put(Blocks.ORANGE_STAINED_GLASS, TRANSLUCENT);
		hashMap.put(Blocks.MAGENTA_STAINED_GLASS, TRANSLUCENT);
		hashMap.put(Blocks.LIGHT_BLUE_STAINED_GLASS, TRANSLUCENT);
		hashMap.put(Blocks.YELLOW_STAINED_GLASS, TRANSLUCENT);
		hashMap.put(Blocks.LIME_STAINED_GLASS, TRANSLUCENT);
		hashMap.put(Blocks.PINK_STAINED_GLASS, TRANSLUCENT);
		hashMap.put(Blocks.GRAY_STAINED_GLASS, TRANSLUCENT);
		hashMap.put(Blocks.LIGHT_GRAY_STAINED_GLASS, TRANSLUCENT);
		hashMap.put(Blocks.CYAN_STAINED_GLASS, TRANSLUCENT);
		hashMap.put(Blocks.PURPLE_STAINED_GLASS, TRANSLUCENT);
		hashMap.put(Blocks.BLUE_STAINED_GLASS, TRANSLUCENT);
		hashMap.put(Blocks.BROWN_STAINED_GLASS, TRANSLUCENT);
		hashMap.put(Blocks.GREEN_STAINED_GLASS, TRANSLUCENT);
		hashMap.put(Blocks.RED_STAINED_GLASS, TRANSLUCENT);
		hashMap.put(Blocks.BLACK_STAINED_GLASS, TRANSLUCENT);
		hashMap.put(Blocks.TRIPWIRE, TRANSLUCENT);
		hashMap.put(Blocks.WHITE_STAINED_GLASS_PANE, TRANSLUCENT);
		hashMap.put(Blocks.ORANGE_STAINED_GLASS_PANE, TRANSLUCENT);
		hashMap.put(Blocks.MAGENTA_STAINED_GLASS_PANE, TRANSLUCENT);
		hashMap.put(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, TRANSLUCENT);
		hashMap.put(Blocks.YELLOW_STAINED_GLASS_PANE, TRANSLUCENT);
		hashMap.put(Blocks.LIME_STAINED_GLASS_PANE, TRANSLUCENT);
		hashMap.put(Blocks.PINK_STAINED_GLASS_PANE, TRANSLUCENT);
		hashMap.put(Blocks.GRAY_STAINED_GLASS_PANE, TRANSLUCENT);
		hashMap.put(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, TRANSLUCENT);
		hashMap.put(Blocks.CYAN_STAINED_GLASS_PANE, TRANSLUCENT);
		hashMap.put(Blocks.PURPLE_STAINED_GLASS_PANE, TRANSLUCENT);
		hashMap.put(Blocks.BLUE_STAINED_GLASS_PANE, TRANSLUCENT);
		hashMap.put(Blocks.BROWN_STAINED_GLASS_PANE, TRANSLUCENT);
		hashMap.put(Blocks.GREEN_STAINED_GLASS_PANE, TRANSLUCENT);
		hashMap.put(Blocks.RED_STAINED_GLASS_PANE, TRANSLUCENT);
		hashMap.put(Blocks.BLACK_STAINED_GLASS_PANE, TRANSLUCENT);
		hashMap.put(Blocks.SLIME_BLOCK, TRANSLUCENT);
		hashMap.put(Blocks.FROSTED_ICE, TRANSLUCENT);
		hashMap.put(Blocks.BUBBLE_COLUMN, TRANSLUCENT);
	});
	private static final Map<Fluid, RenderType> TYPE_BY_FLUID = Util.make(Maps.<Fluid, RenderType>newHashMap(), hashMap -> {
		hashMap.put(Fluids.FLOWING_WATER, TRANSLUCENT);
		hashMap.put(Fluids.WATER, TRANSLUCENT);
	});
	private final String name;
	private final int bufferSize;
	private final Runnable setupState;
	private final Runnable clearState;

	private static RenderType register(RenderType renderType) {
		LAYERS.add(renderType);
		return renderType;
	}

	RenderType(String string, int i, Runnable runnable, Runnable runnable2) {
		this.name = string;
		this.bufferSize = i;
		this.setupState = runnable;
		this.clearState = runnable2;
	}

	public static void setFancy(boolean bl) {
		renderCutout = bl;
	}

	public String toString() {
		return this.name;
	}

	public static RenderType getRenderLayer(BlockState blockState) {
		Block block = blockState.getBlock();
		if (block instanceof LeavesBlock) {
			return renderCutout ? CUTOUT_MIPPED : SOLID;
		} else {
			RenderType renderType = (RenderType)TYPE_BY_BLOCK.get(block);
			return renderType != null ? renderType : SOLID;
		}
	}

	public static RenderType getRenderLayer(FluidState fluidState) {
		RenderType renderType = (RenderType)TYPE_BY_FLUID.get(fluidState.getType());
		return renderType != null ? renderType : SOLID;
	}

	public static Set<RenderType> chunkBufferLayers() {
		return ImmutableSet.of(SOLID, CUTOUT_MIPPED, CUTOUT, TRANSLUCENT);
	}

	public int bufferSize() {
		return this.bufferSize;
	}

	public void setupRenderState() {
		this.setupState.run();
	}

	public void clearRenderState() {
		this.clearState.run();
	}
}
