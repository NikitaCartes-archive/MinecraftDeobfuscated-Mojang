package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

@Environment(EnvType.CLIENT)
public class RenderType {
	public static final RenderType SOLID = new RenderType("solid", DefaultVertexFormat.BLOCK, 7, 2097152, false, true, () -> {
		RenderSystem.enableTexture();
		Minecraft.getInstance().getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
		RenderSystem.enableCull();
		RenderSystem.shadeModel(7425);
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.disableAlphaTest();
		RenderSystem.depthFunc(515);
		Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
	}, () -> {
		Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
		RenderSystem.shadeModel(7424);
	});
	public static final RenderType CUTOUT_MIPPED = new RenderType("cutout_mipped", DefaultVertexFormat.BLOCK, 7, 131072, false, true, () -> {
		Minecraft.getInstance().getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
		RenderSystem.enableAlphaTest();
		RenderSystem.alphaFunc(516, 0.5F);
		RenderSystem.shadeModel(7425);
		Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
	}, () -> {
		RenderSystem.disableAlphaTest();
		RenderSystem.defaultAlphaFunc();
		Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
		RenderSystem.shadeModel(7424);
	});
	public static final RenderType CUTOUT = new RenderType("cutout", DefaultVertexFormat.BLOCK, 7, 131072, false, true, () -> {
		AbstractTexture abstractTexture = Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
		abstractTexture.bind();
		abstractTexture.pushFilter(false, false);
		CUTOUT_MIPPED.setupRenderState();
	}, () -> {
		AbstractTexture abstractTexture = Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
		abstractTexture.bind();
		abstractTexture.popFilter();
		CUTOUT_MIPPED.clearRenderState();
	});
	public static final RenderType TRANSLUCENT = new RenderType("translucent", DefaultVertexFormat.BLOCK, 7, 262144, false, true, () -> {
		Minecraft.getInstance().getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.shadeModel(7425);
		Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
	}, () -> {
		RenderSystem.disableBlend();
		Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
		RenderSystem.shadeModel(7424);
	});
	public static final RenderType TRANSLUCENT_NO_CRUMBLING = new RenderType(
		"translucent_no_crumbling", DefaultVertexFormat.BLOCK, 7, 256, false, false, TRANSLUCENT::setupRenderState, TRANSLUCENT::clearRenderState
	);
	public static final RenderType LEASH = new RenderType("leash", DefaultVertexFormat.POSITION_COLOR, 7, 256, false, false, () -> {
		RenderSystem.disableTexture();
		RenderSystem.disableCull();
	}, () -> {
		RenderSystem.enableTexture();
		RenderSystem.enableCull();
	});
	public static final RenderType WATER_MASK = new RenderType("water_mask", DefaultVertexFormat.BLOCK, 7, 256, false, false, () -> {
		RenderSystem.disableTexture();
		RenderSystem.colorMask(false, false, false, false);
	}, () -> {
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableTexture();
	});
	public static final RenderType OUTLINE = new RenderType("outline", DefaultVertexFormat.POSITION_COLOR, 7, 256, false, false, () -> {
		RenderSystem.depthFunc(519);
		RenderSystem.disableTexture();
		RenderSystem.disableFog();
		Minecraft.getInstance().levelRenderer.entityTarget().bindWrite(false);
	}, () -> {
		RenderSystem.depthFunc(515);
		RenderSystem.enableTexture();
		RenderSystem.enableFog();
	});
	public static final RenderType GLINT = new RenderType(
		"glint", DefaultVertexFormat.POSITION_TEX, 7, 256, false, false, () -> setupGlint(8.0F), () -> clearGlint()
	);
	public static final RenderType ENTITY_GLINT = new RenderType(
		"entity_glint", DefaultVertexFormat.POSITION_TEX, 7, 256, false, false, () -> setupGlint(0.16F), () -> clearGlint()
	);
	public static final RenderType BEACON_BEAM = new RenderType("beacon_beam", DefaultVertexFormat.BLOCK, 7, 256, false, false, () -> {
		RenderSystem.defaultAlphaFunc();
		Minecraft.getInstance().getTextureManager().bind(BeaconRenderer.BEAM_LOCATION);
		RenderSystem.texParameter(3553, 10242, 10497);
		RenderSystem.texParameter(3553, 10243, 10497);
		RenderSystem.disableFog();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(false);
	}, () -> {
		RenderSystem.enableFog();
		RenderSystem.depthMask(true);
	});
	public static final RenderType LIGHTNING = new RenderType("lightning", DefaultVertexFormat.POSITION_COLOR, 7, 256, false, false, () -> {
		RenderSystem.disableTexture();
		RenderSystem.depthMask(false);
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		RenderSystem.shadeModel(7425);
		RenderSystem.disableAlphaTest();
	}, () -> {
		RenderSystem.enableTexture();
		RenderSystem.depthMask(true);
		RenderSystem.shadeModel(7424);
		RenderSystem.enableAlphaTest();
	});
	public static final RenderType LINES = new RenderType("lines", DefaultVertexFormat.POSITION_COLOR, 1, 256, false, false, () -> {
		RenderSystem.disableAlphaTest();
		RenderSystem.lineWidth(Math.max(2.5F, (float)Minecraft.getInstance().getWindow().getWidth() / 1920.0F * 2.5F));
		RenderSystem.disableTexture();
		RenderSystem.depthMask(false);
		RenderSystem.matrixMode(5889);
		RenderSystem.pushMatrix();
		RenderSystem.scalef(1.0F, 1.0F, 0.999F);
		RenderSystem.matrixMode(5888);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
	}, () -> {
		RenderSystem.matrixMode(5889);
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(5888);
		RenderSystem.depthMask(true);
		RenderSystem.enableTexture();
		RenderSystem.enableAlphaTest();
		RenderSystem.disableBlend();
	});
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
	private final VertexFormat format;
	private final int mode;
	private final int bufferSize;
	private final Runnable setupState;
	private final Runnable clearState;
	private final boolean affectsEntityOutline;
	private final boolean affectsCrumbling;

	public static RenderType NEW_ENTITY(ResourceLocation resourceLocation) {
		return NEW_ENTITY(resourceLocation, false, true, false);
	}

	public static RenderType NEW_ENTITY(ResourceLocation resourceLocation, boolean bl, boolean bl2, boolean bl3) {
		return NEW_ENTITY(resourceLocation, bl, bl2, bl3, 0.1F, false);
	}

	public static RenderType NEW_ENTITY(ResourceLocation resourceLocation, boolean bl, boolean bl2, boolean bl3, float f, boolean bl4) {
		return new RenderType.StatefullRenderType<>(
			"new_entity", DefaultVertexFormat.NEW_ENTITY, 256, new RenderType.EntityState(resourceLocation, bl, bl2, bl3, f, bl4), true, false, entityState -> {
				RenderSystem.disableCull();
				RenderSystem.enableRescaleNormal();
				RenderSystem.shadeModel(entityState.smoothShading ? 7425 : 7424);
				Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
				Minecraft.getInstance().gameRenderer.overlayTexture().setupOverlayColor();
				Minecraft.getInstance().getTextureManager().bind(entityState.texture);
				RenderSystem.texParameter(3553, 10241, 9728);
				RenderSystem.texParameter(3553, 10240, 9728);
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.enableDepthTest();
				if (entityState.forceTranslucent) {
					RenderSystem.depthMask(false);
					RenderSystem.enableBlend();
					RenderSystem.blendColor(1.0F, 1.0F, 1.0F, 0.15F);
					RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
				}

				if (entityState.alphaCutoff <= 0.0F) {
					RenderSystem.disableAlphaTest();
				} else {
					RenderSystem.enableAlphaTest();
					RenderSystem.alphaFunc(516, entityState.alphaCutoff);
				}

				if (entityState.lighting) {
					Lighting.turnBackOn();
				}

				if (entityState.equalDepth) {
					RenderSystem.depthFunc(514);
				}
			}, entityState -> {
				RenderSystem.shadeModel(7424);
				Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
				Minecraft.getInstance().gameRenderer.overlayTexture().teardownOverlayColor();
				RenderSystem.enableCull();
				RenderSystem.cullFace(GlStateManager.CullFace.BACK);
				if (entityState.forceTranslucent) {
					RenderSystem.defaultBlendFunc();
					RenderSystem.blendColor(1.0F, 1.0F, 1.0F, 1.0F);
					RenderSystem.depthMask(true);
				}

				if (entityState.lighting) {
					Lighting.turnOff();
				}

				if (entityState.equalDepth) {
					RenderSystem.depthFunc(515);
				}

				RenderSystem.disableAlphaTest();
				RenderSystem.defaultAlphaFunc();
			}
		);
	}

	public static RenderType EYES(ResourceLocation resourceLocation) {
		return new RenderType.StatefullRenderType<>("eyes", DefaultVertexFormat.NEW_ENTITY, 256, resourceLocation, false, false, resourceLocationx -> {
			Minecraft.getInstance().getTextureManager().bind(resourceLocationx);
			RenderSystem.enableBlend();
			RenderSystem.disableAlphaTest();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
			RenderSystem.depthMask(false);
			FogRenderer.resetFogColor(true);
			RenderSystem.enableDepthTest();
		}, resourceLocationx -> {
			RenderSystem.depthMask(true);
			RenderSystem.disableBlend();
			RenderSystem.enableAlphaTest();
			FogRenderer.resetFogColor(false);
			RenderSystem.defaultBlendFunc();
		});
	}

	public static RenderType POWER_SWIRL(ResourceLocation resourceLocation, float f, float g) {
		RenderType renderType = NEW_ENTITY(resourceLocation);
		return new RenderType.StatefullRenderType<>(
			"power_swirl", DefaultVertexFormat.NEW_ENTITY, 256, new RenderType.SwirlState(resourceLocation, f, g), false, false, swirlState -> {
				renderType.setupRenderState();
				RenderSystem.matrixMode(5890);
				RenderSystem.pushMatrix();
				RenderSystem.loadIdentity();
				RenderSystem.translatef(swirlState.uOffset, swirlState.vOffset, 0.0F);
				RenderSystem.matrixMode(5888);
				RenderSystem.enableBlend();
				RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
				FogRenderer.resetFogColor(true);
			}, swirlState -> {
				renderType.clearRenderState();
				FogRenderer.resetFogColor(false);
				RenderSystem.matrixMode(5890);
				RenderSystem.popMatrix();
				RenderSystem.matrixMode(5888);
				RenderSystem.disableBlend();
				RenderSystem.depthMask(true);
			}
		);
	}

	public static RenderType CRUMBLING(int i) {
		return new RenderType.StatefullRenderType<>(
			"crumbling",
			DefaultVertexFormat.BLOCK,
			256,
			i,
			false,
			false,
			integer -> {
				Minecraft.getInstance().getTextureManager().bind((ResourceLocation)ModelBakery.BREAKING_LOCATIONS.get(integer));
				RenderSystem.polygonOffset(-1.0F, -10.0F);
				RenderSystem.enablePolygonOffset();
				RenderSystem.defaultAlphaFunc();
				RenderSystem.enableAlphaTest();
				RenderSystem.enableBlend();
				RenderSystem.depthMask(false);
				RenderSystem.blendFuncSeparate(
					GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
				);
			},
			integer -> {
				RenderSystem.disableAlphaTest();
				RenderSystem.polygonOffset(0.0F, 0.0F);
				RenderSystem.disablePolygonOffset();
				RenderSystem.disableBlend();
				RenderSystem.depthMask(true);
			}
		);
	}

	public static RenderType TEXT(ResourceLocation resourceLocation) {
		return new RenderType.StatefullRenderType<>(
			"text", DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR, 256, resourceLocation, false, false, resourceLocationx -> {
				Minecraft.getInstance().getTextureManager().bind(resourceLocationx);
				RenderSystem.enableAlphaTest();
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
			}, resourceLocationx -> {
			}
		);
	}

	public static RenderType TEXT_SEE_THROUGH(ResourceLocation resourceLocation) {
		RenderType renderType = TEXT(resourceLocation);
		return new RenderType.StatefullRenderType<>(
			"text_see_through", DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR, 256, resourceLocation, false, false, resourceLocationx -> {
				renderType.setupRenderState();
				RenderSystem.disableDepthTest();
				RenderSystem.depthMask(false);
			}, resourceLocationx -> {
				renderType.clearRenderState();
				RenderSystem.enableDepthTest();
				RenderSystem.depthMask(true);
			}
		);
	}

	public static RenderType PORTAL(int i) {
		return new RenderType.StatefullRenderType<>(
			"portal",
			DefaultVertexFormat.POSITION_COLOR,
			256,
			i,
			false,
			false,
			integer -> {
				RenderSystem.enableBlend();
				if (integer >= 2) {
					RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE.value, GlStateManager.DestFactor.ONE.value);
					Minecraft.getInstance().getTextureManager().bind(TheEndPortalRenderer.END_PORTAL_LOCATION);
					FogRenderer.resetFogColor(true);
				} else {
					RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA.value, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value);
					Minecraft.getInstance().getTextureManager().bind(TheEndPortalRenderer.END_SKY_LOCATION);
				}

				RenderSystem.matrixMode(5890);
				RenderSystem.pushMatrix();
				RenderSystem.loadIdentity();
				RenderSystem.translatef(0.5F, 0.5F, 0.0F);
				RenderSystem.scalef(0.5F, 0.5F, 1.0F);
				RenderSystem.translatef(
					17.0F / (float)integer.intValue(), (2.0F + (float)integer.intValue() / 1.5F) * ((float)(Util.getMillis() % 800000L) / 800000.0F), 0.0F
				);
				RenderSystem.rotatef(((float)(integer * integer) * 4321.0F + (float)integer.intValue() * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
				RenderSystem.scalef(4.5F - (float)integer.intValue() / 4.0F, 4.5F - (float)integer.intValue() / 4.0F, 1.0F);
				RenderSystem.mulTextureByProjModelView();
				RenderSystem.matrixMode(5888);
				RenderSystem.setupEndPortalTexGen();
			},
			integer -> {
				RenderSystem.defaultBlendFunc();
				RenderSystem.matrixMode(5890);
				RenderSystem.popMatrix();
				RenderSystem.matrixMode(5888);
				RenderSystem.clearTexGen();
				FogRenderer.resetFogColor(false);
			}
		);
	}

	public RenderType(String string, VertexFormat vertexFormat, int i, int j, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
		this.name = string;
		this.format = vertexFormat;
		this.mode = i;
		this.bufferSize = j;
		this.setupState = runnable;
		this.clearState = runnable2;
		this.affectsEntityOutline = bl;
		this.affectsCrumbling = bl2;
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

	public static List<RenderType> chunkBufferLayers() {
		return ImmutableList.of(SOLID, CUTOUT_MIPPED, CUTOUT, TRANSLUCENT);
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

	public VertexFormat format() {
		return this.format;
	}

	public int mode() {
		return this.mode;
	}

	public boolean affectsEntityOutline() {
		return this.affectsEntityOutline;
	}

	public boolean affectsCrumbling() {
		return this.affectsCrumbling;
	}

	public boolean equals(@Nullable Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			RenderType renderType = (RenderType)object;
			return this.name.equals(renderType.name);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return this.name.hashCode();
	}

	private static void setupGlint(float f) {
		RenderSystem.enableTexture();
		TextureManager textureManager = Minecraft.getInstance().getTextureManager();
		textureManager.bind(ItemRenderer.ENCHANT_GLINT_LOCATION);
		RenderSystem.texParameter(3553, 10241, 9728);
		RenderSystem.texParameter(3553, 10240, 9728);
		RenderSystem.texParameter(3553, 10242, 10497);
		RenderSystem.texParameter(3553, 10243, 10497);
		RenderSystem.depthMask(false);
		RenderSystem.depthFunc(514);
		RenderSystem.enableBlend();
		RenderSystem.disableAlphaTest();
		Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
		RenderSystem.matrixMode(5890);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		long l = Util.getMillis() * 8L;
		float g = (float)(l % 110000L) / 110000.0F;
		float h = (float)(l % 30000L) / 30000.0F;
		RenderSystem.translatef(-g, h, 0.0F);
		RenderSystem.rotatef(10.0F, 0.0F, 0.0F, 1.0F);
		RenderSystem.scalef(f, f, f);
	}

	private static void clearGlint() {
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(5888);
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.depthFunc(515);
		RenderSystem.depthMask(true);
	}

	@Environment(EnvType.CLIENT)
	public static final class EntityState {
		private final ResourceLocation texture;
		private final boolean forceTranslucent;
		private final boolean lighting;
		private final boolean smoothShading;
		private final float alphaCutoff;
		private final boolean equalDepth;

		public EntityState(ResourceLocation resourceLocation, boolean bl, boolean bl2, boolean bl3, float f, boolean bl4) {
			this.texture = resourceLocation;
			this.forceTranslucent = bl;
			this.lighting = bl2;
			this.smoothShading = bl3;
			this.alphaCutoff = f;
			this.equalDepth = bl4;
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else if (object != null && this.getClass() == object.getClass()) {
				RenderType.EntityState entityState = (RenderType.EntityState)object;
				return this.forceTranslucent == entityState.forceTranslucent && this.lighting == entityState.lighting && this.texture.equals(entityState.texture);
			} else {
				return false;
			}
		}

		public int hashCode() {
			return Objects.hash(new Object[]{this.texture, this.forceTranslucent, this.lighting});
		}
	}

	@Environment(EnvType.CLIENT)
	public static class StatefullRenderType<S> extends RenderType {
		private final S state;

		public StatefullRenderType(String string, VertexFormat vertexFormat, int i, S object, boolean bl, boolean bl2, Consumer<S> consumer, Consumer<S> consumer2) {
			super(string, vertexFormat, 7, i, bl, bl2, () -> consumer.accept(object), () -> consumer2.accept(object));
			this.state = object;
		}

		@Override
		public boolean equals(@Nullable Object object) {
			if (!super.equals(object)) {
				return false;
			} else if (this.getClass() != object.getClass()) {
				return false;
			} else {
				RenderType.StatefullRenderType<?> statefullRenderType = (RenderType.StatefullRenderType<?>)object;
				return this.state.equals(statefullRenderType.state);
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(new Object[]{super.hashCode(), this.state});
		}
	}

	@Environment(EnvType.CLIENT)
	public static final class SwirlState {
		private final ResourceLocation texture;
		private final float uOffset;
		private final float vOffset;

		public SwirlState(ResourceLocation resourceLocation, float f, float g) {
			this.texture = resourceLocation;
			this.uOffset = f;
			this.vOffset = g;
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else if (object != null && this.getClass() == object.getClass()) {
				RenderType.SwirlState swirlState = (RenderType.SwirlState)object;
				return Float.compare(swirlState.uOffset, this.uOffset) == 0
					&& Float.compare(swirlState.vOffset, this.vOffset) == 0
					&& this.texture.equals(swirlState.texture);
			} else {
				return false;
			}
		}

		public int hashCode() {
			return Objects.hash(new Object[]{this.texture, this.uOffset, this.vOffset});
		}
	}
}
