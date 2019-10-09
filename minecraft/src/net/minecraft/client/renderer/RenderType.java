package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
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
public class RenderType extends RenderStateShard {
	private static final RenderType SOLID = new RenderType.CompositeRenderType(
		"solid",
		DefaultVertexFormat.BLOCK,
		7,
		2097152,
		true,
		false,
		RenderType.CompositeState.builder()
			.setShadeModelState(SMOOTH_SHADE)
			.setLightmapState(LIGHTMAP)
			.setTextureState(BLOCK_SHEET_MIPPED)
			.createCompositeState(false)
	);
	private static final RenderType CUTOUT_MIPPED = new RenderType.CompositeRenderType(
		"cutout_mipped",
		DefaultVertexFormat.BLOCK,
		7,
		131072,
		true,
		false,
		RenderType.CompositeState.builder()
			.setShadeModelState(SMOOTH_SHADE)
			.setLightmapState(LIGHTMAP)
			.setTextureState(BLOCK_SHEET_MIPPED)
			.setAlphaState(MIDWAY_ALPHA)
			.createCompositeState(false)
	);
	private static final RenderType CUTOUT = new RenderType.CompositeRenderType(
		"cutout",
		DefaultVertexFormat.BLOCK,
		7,
		131072,
		true,
		false,
		RenderType.CompositeState.builder()
			.setShadeModelState(SMOOTH_SHADE)
			.setLightmapState(LIGHTMAP)
			.setTextureState(BLOCK_SHEET)
			.setAlphaState(MIDWAY_ALPHA)
			.createCompositeState(false)
	);
	private static final RenderType TRANSLUCENT = new RenderType.CompositeRenderType(
		"translucent",
		DefaultVertexFormat.BLOCK,
		7,
		262144,
		true,
		true,
		RenderType.CompositeState.builder()
			.setShadeModelState(SMOOTH_SHADE)
			.setLightmapState(LIGHTMAP)
			.setTextureState(BLOCK_SHEET)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.createCompositeState(false)
	);
	private static final RenderType TRANSLUCENT_NO_CRUMBLING = new RenderType(
		"translucent_no_crumbling", DefaultVertexFormat.BLOCK, 7, 256, false, true, TRANSLUCENT::setupRenderState, TRANSLUCENT::clearRenderState
	);
	private static final RenderType LEASH = new RenderType.CompositeRenderType(
		"leash",
		DefaultVertexFormat.POSITION_COLOR,
		7,
		256,
		RenderType.CompositeState.builder().setTextureState(NO_TEXTURE).setCullState(NO_CULL).createCompositeState(false)
	);
	private static final RenderType WATER_MASK = new RenderType.CompositeRenderType(
		"water_mask",
		DefaultVertexFormat.POSITION,
		7,
		256,
		RenderType.CompositeState.builder().setTextureState(NO_TEXTURE).setWriteMaskState(DEPTH_WRITE).createCompositeState(false)
	);
	private static final RenderType GLINT = new RenderType.CompositeRenderType(
		"glint",
		DefaultVertexFormat.POSITION_TEX,
		7,
		256,
		RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, false, false))
			.setWriteMaskState(COLOR_WRITE)
			.setDepthTestState(EQUAL_DEPTH_TEST)
			.setTransparencyState(GLINT_TRANSPARENCY)
			.setTexturingState(GLINT_TEXTURING)
			.createCompositeState(false)
	);
	private static final RenderType ENTITY_GLINT = new RenderType.CompositeRenderType(
		"entity_glint",
		DefaultVertexFormat.POSITION_TEX,
		7,
		256,
		RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, false, false))
			.setWriteMaskState(COLOR_WRITE)
			.setDepthTestState(EQUAL_DEPTH_TEST)
			.setTransparencyState(GLINT_TRANSPARENCY)
			.setTexturingState(ENTITY_GLINT_TEXTURING)
			.createCompositeState(false)
	);
	private static final RenderType BEACON_BEAM = new RenderType.CompositeRenderType(
		"beacon_beam",
		DefaultVertexFormat.BLOCK,
		7,
		256,
		false,
		true,
		RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(BeaconRenderer.BEAM_LOCATION, false, false))
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setWriteMaskState(COLOR_WRITE)
			.setFogState(NO_FOG)
			.createCompositeState(false)
	);
	private static final RenderType LIGHTNING = new RenderType.CompositeRenderType(
		"lightning",
		DefaultVertexFormat.POSITION_COLOR,
		7,
		256,
		false,
		true,
		RenderType.CompositeState.builder()
			.setWriteMaskState(COLOR_WRITE)
			.setTransparencyState(LIGHTNING_TRANSPARENCY)
			.setShadeModelState(SMOOTH_SHADE)
			.createCompositeState(false)
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
		hashMap.put(Blocks.HONEY_BLOCK, TRANSLUCENT);
		hashMap.put(Blocks.FROSTED_ICE, TRANSLUCENT);
		hashMap.put(Blocks.BUBBLE_COLUMN, TRANSLUCENT);
	});
	private static final Map<Fluid, RenderType> TYPE_BY_FLUID = Util.make(Maps.<Fluid, RenderType>newHashMap(), hashMap -> {
		hashMap.put(Fluids.FLOWING_WATER, TRANSLUCENT);
		hashMap.put(Fluids.WATER, TRANSLUCENT);
	});
	private final VertexFormat format;
	private final int mode;
	private final int bufferSize;
	private final boolean affectsCrumbling;
	private final boolean sortOnUpload;

	public static RenderType solid() {
		return SOLID;
	}

	public static RenderType cutoutMipped() {
		return CUTOUT_MIPPED;
	}

	public static RenderType cutout() {
		return CUTOUT;
	}

	public static RenderType translucent() {
		return TRANSLUCENT;
	}

	public static RenderType translucentNoCrumbling() {
		return TRANSLUCENT_NO_CRUMBLING;
	}

	public static RenderType entitySolid(ResourceLocation resourceLocation) {
		RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
			.setTransparencyState(NO_TRANSPARENCY)
			.setDiffuseLightingState(DIFFUSE_LIGHTING)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true);
		return new RenderType.CompositeRenderType("entity_solid", DefaultVertexFormat.NEW_ENTITY, 7, 256, compositeState);
	}

	public static RenderType entityCutout(ResourceLocation resourceLocation) {
		RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
			.setTransparencyState(NO_TRANSPARENCY)
			.setDiffuseLightingState(DIFFUSE_LIGHTING)
			.setAlphaState(DEFAULT_ALPHA)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true);
		return new RenderType.CompositeRenderType("entity_cutout", DefaultVertexFormat.NEW_ENTITY, 7, 256, compositeState);
	}

	public static RenderType entityCutoutNoCull(ResourceLocation resourceLocation) {
		RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
			.setTransparencyState(NO_TRANSPARENCY)
			.setDiffuseLightingState(DIFFUSE_LIGHTING)
			.setAlphaState(DEFAULT_ALPHA)
			.setCullState(NO_CULL)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true);
		return new RenderType.CompositeRenderType("entity_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY, 7, 256, compositeState);
	}

	public static RenderType entityTranslucent(ResourceLocation resourceLocation) {
		RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setDiffuseLightingState(DIFFUSE_LIGHTING)
			.setAlphaState(DEFAULT_ALPHA)
			.setCullState(NO_CULL)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true);
		return new RenderType.CompositeRenderType("entity_translucent", DefaultVertexFormat.NEW_ENTITY, 7, 256, compositeState);
	}

	public static RenderType entityForceTranslucent(ResourceLocation resourceLocation) {
		RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
			.setTransparencyState(FORCED_TRANSPARENCY)
			.setDiffuseLightingState(DIFFUSE_LIGHTING)
			.setCullState(NO_CULL)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true);
		return new RenderType.CompositeRenderType("entity_force_translucent", DefaultVertexFormat.NEW_ENTITY, 7, 256, false, true, compositeState);
	}

	public static RenderType entitySmoothCutout(ResourceLocation resourceLocation) {
		RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
			.setAlphaState(MIDWAY_ALPHA)
			.setDiffuseLightingState(DIFFUSE_LIGHTING)
			.setShadeModelState(SMOOTH_SHADE)
			.setCullState(NO_CULL)
			.setLightmapState(LIGHTMAP)
			.createCompositeState(true);
		return new RenderType.CompositeRenderType("entity_smooth_cutout", DefaultVertexFormat.NEW_ENTITY, 7, 256, compositeState);
	}

	public static RenderType entityDecal(ResourceLocation resourceLocation) {
		RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
			.setDiffuseLightingState(DIFFUSE_LIGHTING)
			.setAlphaState(DEFAULT_ALPHA)
			.setDepthTestState(EQUAL_DEPTH_TEST)
			.setCullState(NO_CULL)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(false);
		return new RenderType.CompositeRenderType("entity_decal", DefaultVertexFormat.NEW_ENTITY, 7, 256, compositeState);
	}

	public static RenderType entityNoOutline(ResourceLocation resourceLocation) {
		RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setDiffuseLightingState(DIFFUSE_LIGHTING)
			.setAlphaState(DEFAULT_ALPHA)
			.setCullState(NO_CULL)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(false);
		return new RenderType.CompositeRenderType("entity_no_outline", DefaultVertexFormat.NEW_ENTITY, 7, 256, false, true, compositeState);
	}

	public static RenderType entityAlpha(ResourceLocation resourceLocation, float f) {
		RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
			.setAlphaState(new RenderStateShard.AlphaStateShard(f))
			.setCullState(NO_CULL)
			.createCompositeState(true);
		return new RenderType.CompositeRenderType("entity_alpha", DefaultVertexFormat.NEW_ENTITY, 7, 256, compositeState);
	}

	public static RenderType eyes(ResourceLocation resourceLocation) {
		RenderStateShard.TextureStateShard textureStateShard = new RenderStateShard.TextureStateShard(resourceLocation, false, false);
		return new RenderType.CompositeRenderType(
			"eyes",
			DefaultVertexFormat.NEW_ENTITY,
			7,
			256,
			false,
			true,
			RenderType.CompositeState.builder()
				.setTextureState(textureStateShard)
				.setTransparencyState(ADDITIVE_TRANSPARENCY)
				.setWriteMaskState(COLOR_WRITE)
				.setFogState(BLACK_FOG)
				.createCompositeState(false)
		);
	}

	public static RenderType powerSwirl(ResourceLocation resourceLocation, float f, float g) {
		return new RenderType.CompositeRenderType(
			"power_swirl",
			DefaultVertexFormat.NEW_ENTITY,
			7,
			256,
			false,
			true,
			RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setTexturingState(new RenderStateShard.SwirlTexturingStateShard(f, g))
				.setFogState(BLACK_FOG)
				.setTransparencyState(ADDITIVE_TRANSPARENCY)
				.setDiffuseLightingState(DIFFUSE_LIGHTING)
				.setAlphaState(DEFAULT_ALPHA)
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(false)
		);
	}

	public static RenderType leash() {
		return LEASH;
	}

	public static RenderType waterMask() {
		return WATER_MASK;
	}

	public static RenderType outline(ResourceLocation resourceLocation) {
		return new RenderType.CompositeRenderType(
			"outline",
			DefaultVertexFormat.POSITION_COLOR_TEX,
			7,
			256,
			RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setCullState(NO_CULL)
				.setDepthTestState(NO_DEPTH_TEST)
				.setAlphaState(DEFAULT_ALPHA)
				.setTexturingState(OUTLINE_TEXTURING)
				.setFogState(NO_FOG)
				.setOutputState(OUTLINE_TARGET)
				.createCompositeState(false)
		);
	}

	public static RenderType glint() {
		return GLINT;
	}

	public static RenderType entityGlint() {
		return ENTITY_GLINT;
	}

	public static RenderType crumbling(int i) {
		RenderStateShard.TextureStateShard textureStateShard = new RenderStateShard.TextureStateShard(
			(ResourceLocation)ModelBakery.BREAKING_LOCATIONS.get(i), false, false
		);
		return new RenderType.CompositeRenderType(
			"crumbling",
			DefaultVertexFormat.BLOCK,
			7,
			256,
			false,
			true,
			RenderType.CompositeState.builder()
				.setTextureState(textureStateShard)
				.setAlphaState(DEFAULT_ALPHA)
				.setTransparencyState(CRUMBLING_TRANSPARENCY)
				.setWriteMaskState(COLOR_WRITE)
				.setLayeringState(POLYGON_OFFSET_LAYERING)
				.createCompositeState(false)
		);
	}

	public static RenderType text(ResourceLocation resourceLocation) {
		return new RenderType.CompositeRenderType(
			"text",
			DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
			7,
			256,
			false,
			true,
			RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setAlphaState(DEFAULT_ALPHA)
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(LIGHTMAP)
				.createCompositeState(false)
		);
	}

	public static RenderType textSeeThrough(ResourceLocation resourceLocation) {
		return new RenderType.CompositeRenderType(
			"text_see_through",
			DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
			7,
			256,
			false,
			true,
			RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setAlphaState(DEFAULT_ALPHA)
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(LIGHTMAP)
				.setDepthTestState(NO_DEPTH_TEST)
				.setWriteMaskState(COLOR_WRITE)
				.createCompositeState(false)
		);
	}

	public static RenderType beaconBeam() {
		return BEACON_BEAM;
	}

	public static RenderType lightning() {
		return LIGHTNING;
	}

	public static RenderType endPortal(int i) {
		RenderStateShard.TransparencyStateShard transparencyStateShard;
		RenderStateShard.TextureStateShard textureStateShard;
		if (i <= 1) {
			transparencyStateShard = TRANSLUCENT_TRANSPARENCY;
			textureStateShard = new RenderStateShard.TextureStateShard(TheEndPortalRenderer.END_SKY_LOCATION, false, false);
		} else {
			transparencyStateShard = ADDITIVE_TRANSPARENCY;
			textureStateShard = new RenderStateShard.TextureStateShard(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false);
		}

		return new RenderType.CompositeRenderType(
			"end_portal",
			DefaultVertexFormat.POSITION_COLOR,
			7,
			256,
			false,
			true,
			RenderType.CompositeState.builder()
				.setTransparencyState(transparencyStateShard)
				.setTextureState(textureStateShard)
				.setTexturingState(new RenderStateShard.PortalTexturingStateShard(i))
				.setFogState(BLACK_FOG)
				.createCompositeState(false)
		);
	}

	public static RenderType lines() {
		return new RenderType.CompositeRenderType(
			"lines",
			DefaultVertexFormat.POSITION_COLOR,
			1,
			256,
			RenderType.CompositeState.builder()
				.setLineState(new RenderStateShard.LineStateShard(Math.max(2.5F, (float)Minecraft.getInstance().getWindow().getWidth() / 1920.0F * 2.5F)))
				.setLayeringState(PROJECTION_LAYERING)
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.createCompositeState(false)
		);
	}

	public RenderType(String string, VertexFormat vertexFormat, int i, int j, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
		super(string, runnable, runnable2);
		this.format = vertexFormat;
		this.mode = i;
		this.bufferSize = j;
		this.affectsCrumbling = bl;
		this.sortOnUpload = bl2;
	}

	public static void setFancy(boolean bl) {
		renderCutout = bl;
	}

	public void end(BufferBuilder bufferBuilder) {
		if (bufferBuilder.building()) {
			bufferBuilder.end();
			this.setupRenderState();
			BufferUploader.end(bufferBuilder);
			this.clearRenderState();
		}
	}

	public String toString() {
		return this.name;
	}

	public static RenderType getChunkRenderType(BlockState blockState) {
		Block block = blockState.getBlock();
		if (block instanceof LeavesBlock) {
			return renderCutout ? cutoutMipped() : solid();
		} else {
			RenderType renderType = (RenderType)TYPE_BY_BLOCK.get(block);
			return renderType != null ? renderType : solid();
		}
	}

	public static RenderType getRenderType(BlockState blockState) {
		RenderType renderType = getChunkRenderType(blockState);
		if (renderType == translucent()) {
			return entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
		} else {
			return renderType != cutout() && renderType != cutoutMipped() ? entitySolid(TextureAtlas.LOCATION_BLOCKS) : entityCutout(TextureAtlas.LOCATION_BLOCKS);
		}
	}

	public static RenderType getRenderType(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item instanceof BlockItem) {
			Block block = ((BlockItem)item).getBlock();
			return getRenderType(block.defaultBlockState());
		} else {
			return entityTranslucent(TextureAtlas.LOCATION_BLOCKS);
		}
	}

	public static RenderType getRenderLayer(FluidState fluidState) {
		RenderType renderType = (RenderType)TYPE_BY_FLUID.get(fluidState.getType());
		return renderType != null ? renderType : solid();
	}

	public static List<RenderType> chunkBufferLayers() {
		return ImmutableList.of(solid(), cutoutMipped(), cutout(), translucent());
	}

	public int bufferSize() {
		return this.bufferSize;
	}

	public VertexFormat format() {
		return this.format;
	}

	public int mode() {
		return this.mode;
	}

	public Optional<ResourceLocation> outlineTexture() {
		return Optional.empty();
	}

	public boolean affectsCrumbling() {
		return this.affectsCrumbling;
	}

	@Environment(EnvType.CLIENT)
	static class CompositeRenderType extends RenderType {
		private final RenderType.CompositeState state;
		private int hashCode;
		private boolean hashed = false;

		public CompositeRenderType(String string, VertexFormat vertexFormat, int i, int j, RenderType.CompositeState compositeState) {
			this(string, vertexFormat, i, j, false, false, compositeState);
		}

		public CompositeRenderType(String string, VertexFormat vertexFormat, int i, int j, boolean bl, boolean bl2, RenderType.CompositeState compositeState) {
			super(
				string,
				vertexFormat,
				i,
				j,
				bl,
				bl2,
				() -> compositeState.states.forEach(RenderStateShard::setupRenderState),
				() -> compositeState.states.forEach(RenderStateShard::clearRenderState)
			);
			this.state = compositeState;
		}

		@Override
		public Optional<ResourceLocation> outlineTexture() {
			return this.state().affectsOutline ? this.state().textureState.texture() : Optional.empty();
		}

		protected final RenderType.CompositeState state() {
			return this.state;
		}

		@Override
		public boolean equals(@Nullable Object object) {
			if (!super.equals(object)) {
				return false;
			} else if (this.getClass() != object.getClass()) {
				return false;
			} else {
				RenderType.CompositeRenderType compositeRenderType = (RenderType.CompositeRenderType)object;
				return this.state.equals(compositeRenderType.state);
			}
		}

		@Override
		public int hashCode() {
			if (!this.hashed) {
				this.hashed = true;
				this.hashCode = Objects.hash(new Object[]{super.hashCode(), this.state});
			}

			return this.hashCode;
		}
	}

	@Environment(EnvType.CLIENT)
	public static final class CompositeState {
		private final RenderStateShard.TextureStateShard textureState;
		private final RenderStateShard.TransparencyStateShard transparencyState;
		private final RenderStateShard.DiffuseLightingStateShard diffuseLightingState;
		private final RenderStateShard.ShadeModelStateShard shadeModelState;
		private final RenderStateShard.AlphaStateShard alphaState;
		private final RenderStateShard.DepthTestStateShard depthTestState;
		private final RenderStateShard.CullStateShard cullState;
		private final RenderStateShard.LightmapStateShard lightmapState;
		private final RenderStateShard.OverlayStateShard overlayState;
		private final RenderStateShard.FogStateShard fogState;
		private final RenderStateShard.LayeringStateShard layeringState;
		private final RenderStateShard.OutputStateShard outputState;
		private final RenderStateShard.TexturingStateShard texturingState;
		private final RenderStateShard.WriteMaskStateShard writeMaskState;
		private final RenderStateShard.LineStateShard lineState;
		private final boolean affectsOutline;
		private final ImmutableList<RenderStateShard> states;

		private CompositeState(
			RenderStateShard.TextureStateShard textureStateShard,
			RenderStateShard.TransparencyStateShard transparencyStateShard,
			RenderStateShard.DiffuseLightingStateShard diffuseLightingStateShard,
			RenderStateShard.ShadeModelStateShard shadeModelStateShard,
			RenderStateShard.AlphaStateShard alphaStateShard,
			RenderStateShard.DepthTestStateShard depthTestStateShard,
			RenderStateShard.CullStateShard cullStateShard,
			RenderStateShard.LightmapStateShard lightmapStateShard,
			RenderStateShard.OverlayStateShard overlayStateShard,
			RenderStateShard.FogStateShard fogStateShard,
			RenderStateShard.LayeringStateShard layeringStateShard,
			RenderStateShard.OutputStateShard outputStateShard,
			RenderStateShard.TexturingStateShard texturingStateShard,
			RenderStateShard.WriteMaskStateShard writeMaskStateShard,
			RenderStateShard.LineStateShard lineStateShard,
			boolean bl
		) {
			this.textureState = textureStateShard;
			this.transparencyState = transparencyStateShard;
			this.diffuseLightingState = diffuseLightingStateShard;
			this.shadeModelState = shadeModelStateShard;
			this.alphaState = alphaStateShard;
			this.depthTestState = depthTestStateShard;
			this.cullState = cullStateShard;
			this.lightmapState = lightmapStateShard;
			this.overlayState = overlayStateShard;
			this.fogState = fogStateShard;
			this.layeringState = layeringStateShard;
			this.outputState = outputStateShard;
			this.texturingState = texturingStateShard;
			this.writeMaskState = writeMaskStateShard;
			this.lineState = lineStateShard;
			this.affectsOutline = bl;
			this.states = ImmutableList.of(
				this.textureState,
				this.transparencyState,
				this.diffuseLightingState,
				this.shadeModelState,
				this.alphaState,
				this.depthTestState,
				this.cullState,
				this.lightmapState,
				this.overlayState,
				this.fogState,
				this.layeringState,
				this.outputState,
				this.texturingState,
				this.writeMaskState,
				this.lineState
			);
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else if (object != null && this.getClass() == object.getClass()) {
				RenderType.CompositeState compositeState = (RenderType.CompositeState)object;
				return this.affectsOutline == compositeState.affectsOutline && this.states.equals(compositeState.states);
			} else {
				return false;
			}
		}

		public int hashCode() {
			return Objects.hash(new Object[]{this.states, this.affectsOutline});
		}

		public static RenderType.CompositeState.CompositeStateBuilder builder() {
			return new RenderType.CompositeState.CompositeStateBuilder();
		}

		@Environment(EnvType.CLIENT)
		public static class CompositeStateBuilder {
			private RenderStateShard.TextureStateShard textureState = RenderStateShard.NO_TEXTURE;
			private RenderStateShard.TransparencyStateShard transparencyState = RenderStateShard.NO_TRANSPARENCY;
			private RenderStateShard.DiffuseLightingStateShard diffuseLightingState = RenderStateShard.NO_DIFFUSE_LIGHTING;
			private RenderStateShard.ShadeModelStateShard shadeModelState = RenderStateShard.FLAT_SHADE;
			private RenderStateShard.AlphaStateShard alphaState = RenderStateShard.NO_ALPHA;
			private RenderStateShard.DepthTestStateShard depthTestState = RenderStateShard.LEQUAL_DEPTH_TEST;
			private RenderStateShard.CullStateShard cullState = RenderStateShard.CULL;
			private RenderStateShard.LightmapStateShard lightmapState = RenderStateShard.NO_LIGHTMAP;
			private RenderStateShard.OverlayStateShard overlayState = RenderStateShard.NO_OVERLAY;
			private RenderStateShard.FogStateShard fogState = RenderStateShard.FOG;
			private RenderStateShard.LayeringStateShard layeringState = RenderStateShard.NO_LAYERING;
			private RenderStateShard.OutputStateShard outputState = RenderStateShard.MAIN_TARGET;
			private RenderStateShard.TexturingStateShard texturingState = RenderStateShard.DEFAULT_TEXTURING;
			private RenderStateShard.WriteMaskStateShard writeMaskState = RenderStateShard.COLOR_DEPTH_WRITE;
			private RenderStateShard.LineStateShard lineState = RenderStateShard.DEFAULT_LINE;

			private CompositeStateBuilder() {
			}

			public RenderType.CompositeState.CompositeStateBuilder setTextureState(RenderStateShard.TextureStateShard textureStateShard) {
				this.textureState = textureStateShard;
				return this;
			}

			public RenderType.CompositeState.CompositeStateBuilder setTransparencyState(RenderStateShard.TransparencyStateShard transparencyStateShard) {
				this.transparencyState = transparencyStateShard;
				return this;
			}

			public RenderType.CompositeState.CompositeStateBuilder setDiffuseLightingState(RenderStateShard.DiffuseLightingStateShard diffuseLightingStateShard) {
				this.diffuseLightingState = diffuseLightingStateShard;
				return this;
			}

			public RenderType.CompositeState.CompositeStateBuilder setShadeModelState(RenderStateShard.ShadeModelStateShard shadeModelStateShard) {
				this.shadeModelState = shadeModelStateShard;
				return this;
			}

			public RenderType.CompositeState.CompositeStateBuilder setAlphaState(RenderStateShard.AlphaStateShard alphaStateShard) {
				this.alphaState = alphaStateShard;
				return this;
			}

			public RenderType.CompositeState.CompositeStateBuilder setDepthTestState(RenderStateShard.DepthTestStateShard depthTestStateShard) {
				this.depthTestState = depthTestStateShard;
				return this;
			}

			public RenderType.CompositeState.CompositeStateBuilder setCullState(RenderStateShard.CullStateShard cullStateShard) {
				this.cullState = cullStateShard;
				return this;
			}

			public RenderType.CompositeState.CompositeStateBuilder setLightmapState(RenderStateShard.LightmapStateShard lightmapStateShard) {
				this.lightmapState = lightmapStateShard;
				return this;
			}

			public RenderType.CompositeState.CompositeStateBuilder setOverlayState(RenderStateShard.OverlayStateShard overlayStateShard) {
				this.overlayState = overlayStateShard;
				return this;
			}

			public RenderType.CompositeState.CompositeStateBuilder setFogState(RenderStateShard.FogStateShard fogStateShard) {
				this.fogState = fogStateShard;
				return this;
			}

			public RenderType.CompositeState.CompositeStateBuilder setLayeringState(RenderStateShard.LayeringStateShard layeringStateShard) {
				this.layeringState = layeringStateShard;
				return this;
			}

			public RenderType.CompositeState.CompositeStateBuilder setOutputState(RenderStateShard.OutputStateShard outputStateShard) {
				this.outputState = outputStateShard;
				return this;
			}

			public RenderType.CompositeState.CompositeStateBuilder setTexturingState(RenderStateShard.TexturingStateShard texturingStateShard) {
				this.texturingState = texturingStateShard;
				return this;
			}

			public RenderType.CompositeState.CompositeStateBuilder setWriteMaskState(RenderStateShard.WriteMaskStateShard writeMaskStateShard) {
				this.writeMaskState = writeMaskStateShard;
				return this;
			}

			public RenderType.CompositeState.CompositeStateBuilder setLineState(RenderStateShard.LineStateShard lineStateShard) {
				this.lineState = lineStateShard;
				return this;
			}

			public RenderType.CompositeState createCompositeState(boolean bl) {
				return new RenderType.CompositeState(
					this.textureState,
					this.transparencyState,
					this.diffuseLightingState,
					this.shadeModelState,
					this.alphaState,
					this.depthTestState,
					this.cullState,
					this.lightmapState,
					this.overlayState,
					this.fogState,
					this.layeringState,
					this.outputState,
					this.texturingState,
					this.writeMaskState,
					this.lineState,
					bl
				);
			}
		}
	}
}
