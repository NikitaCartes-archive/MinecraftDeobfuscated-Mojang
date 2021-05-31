package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public abstract class RenderType extends RenderStateShard {
	private static final int BYTES_IN_INT = 4;
	private static final int MEGABYTE = 1048576;
	public static final int BIG_BUFFER_SIZE = 2097152;
	public static final int MEDIUM_BUFFER_SIZE = 262144;
	public static final int SMALL_BUFFER_SIZE = 131072;
	public static final int TRANSIENT_BUFFER_SIZE = 256;
	private static final RenderType SOLID = create(
		"solid",
		DefaultVertexFormat.BLOCK,
		VertexFormat.Mode.QUADS,
		2097152,
		true,
		false,
		RenderType.CompositeState.builder()
			.setLightmapState(LIGHTMAP)
			.setShaderState(RENDERTYPE_SOLID_SHADER)
			.setTextureState(BLOCK_SHEET_MIPPED)
			.createCompositeState(true)
	);
	private static final RenderType CUTOUT_MIPPED = create(
		"cutout_mipped",
		DefaultVertexFormat.BLOCK,
		VertexFormat.Mode.QUADS,
		131072,
		true,
		false,
		RenderType.CompositeState.builder()
			.setLightmapState(LIGHTMAP)
			.setShaderState(RENDERTYPE_CUTOUT_MIPPED_SHADER)
			.setTextureState(BLOCK_SHEET_MIPPED)
			.createCompositeState(true)
	);
	private static final RenderType CUTOUT = create(
		"cutout",
		DefaultVertexFormat.BLOCK,
		VertexFormat.Mode.QUADS,
		131072,
		true,
		false,
		RenderType.CompositeState.builder()
			.setLightmapState(LIGHTMAP)
			.setShaderState(RENDERTYPE_CUTOUT_SHADER)
			.setTextureState(BLOCK_SHEET)
			.createCompositeState(true)
	);
	private static final RenderType TRANSLUCENT = create(
		"translucent", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, true, translucentState(RENDERTYPE_TRANSLUCENT_SHADER)
	);
	private static final RenderType TRANSLUCENT_MOVING_BLOCK = create(
		"translucent_moving_block", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 262144, false, true, translucentMovingBlockState()
	);
	private static final RenderType TRANSLUCENT_NO_CRUMBLING = create(
		"translucent_no_crumbling",
		DefaultVertexFormat.BLOCK,
		VertexFormat.Mode.QUADS,
		262144,
		false,
		true,
		translucentState(RENDERTYPE_TRANSLUCENT_NO_CRUMBLING_SHADER)
	);
	private static final Function<ResourceLocation, RenderType> ARMOR_CUTOUT_NO_CULL = Util.memoize(
		(Function<ResourceLocation, RenderType>)(resourceLocation -> {
			RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ARMOR_CUTOUT_NO_CULL_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setTransparencyState(NO_TRANSPARENCY)
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.createCompositeState(true);
			return create("armor_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, compositeState);
		})
	);
	private static final Function<ResourceLocation, RenderType> ENTITY_SOLID = Util.memoize(
		(Function<ResourceLocation, RenderType>)(resourceLocation -> {
			RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setTransparencyState(NO_TRANSPARENCY)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true);
			return create("entity_solid", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, compositeState);
		})
	);
	private static final Function<ResourceLocation, RenderType> ENTITY_CUTOUT = Util.memoize(
		(Function<ResourceLocation, RenderType>)(resourceLocation -> {
			RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_CUTOUT_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setTransparencyState(NO_TRANSPARENCY)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true);
			return create("entity_cutout", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, compositeState);
		})
	);
	private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_CUTOUT_NO_CULL = Util.memoize(
		(BiFunction<ResourceLocation, Boolean, RenderType>)((resourceLocation, boolean_) -> {
			RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setTransparencyState(NO_TRANSPARENCY)
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(boolean_);
			return create("entity_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, compositeState);
		})
	);
	private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_CUTOUT_NO_CULL_Z_OFFSET = Util.memoize(
		(BiFunction<ResourceLocation, Boolean, RenderType>)((resourceLocation, boolean_) -> {
			RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_CUTOUT_NO_CULL_Z_OFFSET_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setTransparencyState(NO_TRANSPARENCY)
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.createCompositeState(boolean_);
			return create("entity_cutout_no_cull_z_offset", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, compositeState);
		})
	);
	private static final Function<ResourceLocation, RenderType> ITEM_ENTITY_TRANSLUCENT_CULL = Util.memoize(
		(Function<ResourceLocation, RenderType>)(resourceLocation -> {
			RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setOutputState(ITEM_ENTITY_TARGET)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
				.createCompositeState(true);
			return create("item_entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, compositeState);
		})
	);
	private static final Function<ResourceLocation, RenderType> ENTITY_TRANSLUCENT_CULL = Util.memoize(
		(Function<ResourceLocation, RenderType>)(resourceLocation -> {
			RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(true);
			return create("entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, compositeState);
		})
	);
	private static final BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_TRANSLUCENT = Util.memoize(
		(BiFunction<ResourceLocation, Boolean, RenderType>)((resourceLocation, boolean_) -> {
			RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(boolean_);
			return create("entity_translucent", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, compositeState);
		})
	);
	private static final Function<ResourceLocation, RenderType> ENTITY_SMOOTH_CUTOUT = Util.memoize(
		(Function<ResourceLocation, RenderType>)(resourceLocation -> {
			RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_SMOOTH_CUTOUT_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP)
				.createCompositeState(true);
			return create("entity_smooth_cutout", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, compositeState);
		})
	);
	private static final BiFunction<ResourceLocation, Boolean, RenderType> BEACON_BEAM = Util.memoize(
		(BiFunction<ResourceLocation, Boolean, RenderType>)((resourceLocation, boolean_) -> {
			RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_BEACON_BEAM_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setTransparencyState(boolean_ ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY)
				.setWriteMaskState(boolean_ ? COLOR_WRITE : COLOR_DEPTH_WRITE)
				.createCompositeState(false);
			return create("beacon_beam", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, true, compositeState);
		})
	);
	private static final Function<ResourceLocation, RenderType> ENTITY_DECAL = Util.memoize(
		(Function<ResourceLocation, RenderType>)(resourceLocation -> {
			RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_DECAL_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setDepthTestState(EQUAL_DEPTH_TEST)
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.createCompositeState(false);
			return create("entity_decal", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, compositeState);
		})
	);
	private static final Function<ResourceLocation, RenderType> ENTITY_NO_OUTLINE = Util.memoize(
		(Function<ResourceLocation, RenderType>)(resourceLocation -> {
			RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_NO_OUTLINE_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setCullState(NO_CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.setWriteMaskState(COLOR_WRITE)
				.createCompositeState(false);
			return create("entity_no_outline", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, compositeState);
		})
	);
	private static final Function<ResourceLocation, RenderType> ENTITY_SHADOW = Util.memoize(
		(Function<ResourceLocation, RenderType>)(resourceLocation -> {
			RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_SHADOW_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setCullState(CULL)
				.setLightmapState(LIGHTMAP)
				.setOverlayState(OVERLAY)
				.setWriteMaskState(COLOR_WRITE)
				.setDepthTestState(LEQUAL_DEPTH_TEST)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.createCompositeState(false);
			return create("entity_shadow", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, false, compositeState);
		})
	);
	private static final Function<ResourceLocation, RenderType> DRAGON_EXPLOSION_ALPHA = Util.memoize(
		(Function<ResourceLocation, RenderType>)(resourceLocation -> {
			RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENTITY_ALPHA_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setCullState(NO_CULL)
				.createCompositeState(true);
			return create("entity_alpha", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, compositeState);
		})
	);
	private static final Function<ResourceLocation, RenderType> EYES = Util.memoize(
		(Function<ResourceLocation, RenderType>)(resourceLocation -> {
			RenderStateShard.TextureStateShard textureStateShard = new RenderStateShard.TextureStateShard(resourceLocation, false, false);
			return create(
				"eyes",
				DefaultVertexFormat.NEW_ENTITY,
				VertexFormat.Mode.QUADS,
				256,
				false,
				true,
				RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_EYES_SHADER)
					.setTextureState(textureStateShard)
					.setTransparencyState(ADDITIVE_TRANSPARENCY)
					.setWriteMaskState(COLOR_WRITE)
					.createCompositeState(false)
			);
		})
	);
	private static final RenderType LEASH = create(
		"leash",
		DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
		VertexFormat.Mode.TRIANGLE_STRIP,
		256,
		RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_LEASH_SHADER)
			.setTextureState(NO_TEXTURE)
			.setCullState(NO_CULL)
			.setLightmapState(LIGHTMAP)
			.createCompositeState(false)
	);
	private static final RenderType WATER_MASK = create(
		"water_mask",
		DefaultVertexFormat.POSITION,
		VertexFormat.Mode.QUADS,
		256,
		RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_WATER_MASK_SHADER)
			.setTextureState(NO_TEXTURE)
			.setWriteMaskState(DEPTH_WRITE)
			.createCompositeState(false)
	);
	private static final RenderType ARMOR_GLINT = create(
		"armor_glint",
		DefaultVertexFormat.POSITION_TEX,
		VertexFormat.Mode.QUADS,
		256,
		RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_ARMOR_GLINT_SHADER)
			.setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, true, false))
			.setWriteMaskState(COLOR_WRITE)
			.setCullState(NO_CULL)
			.setDepthTestState(EQUAL_DEPTH_TEST)
			.setTransparencyState(GLINT_TRANSPARENCY)
			.setTexturingState(GLINT_TEXTURING)
			.setLayeringState(VIEW_OFFSET_Z_LAYERING)
			.createCompositeState(false)
	);
	private static final RenderType ARMOR_ENTITY_GLINT = create(
		"armor_entity_glint",
		DefaultVertexFormat.POSITION_TEX,
		VertexFormat.Mode.QUADS,
		256,
		RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER)
			.setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, true, false))
			.setWriteMaskState(COLOR_WRITE)
			.setCullState(NO_CULL)
			.setDepthTestState(EQUAL_DEPTH_TEST)
			.setTransparencyState(GLINT_TRANSPARENCY)
			.setTexturingState(ENTITY_GLINT_TEXTURING)
			.setLayeringState(VIEW_OFFSET_Z_LAYERING)
			.createCompositeState(false)
	);
	private static final RenderType GLINT_TRANSLUCENT = create(
		"glint_translucent",
		DefaultVertexFormat.POSITION_TEX,
		VertexFormat.Mode.QUADS,
		256,
		RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_GLINT_TRANSLUCENT_SHADER)
			.setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, true, false))
			.setWriteMaskState(COLOR_WRITE)
			.setCullState(NO_CULL)
			.setDepthTestState(EQUAL_DEPTH_TEST)
			.setTransparencyState(GLINT_TRANSPARENCY)
			.setTexturingState(GLINT_TEXTURING)
			.setOutputState(ITEM_ENTITY_TARGET)
			.createCompositeState(false)
	);
	private static final RenderType GLINT = create(
		"glint",
		DefaultVertexFormat.POSITION_TEX,
		VertexFormat.Mode.QUADS,
		256,
		RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_GLINT_SHADER)
			.setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, true, false))
			.setWriteMaskState(COLOR_WRITE)
			.setCullState(NO_CULL)
			.setDepthTestState(EQUAL_DEPTH_TEST)
			.setTransparencyState(GLINT_TRANSPARENCY)
			.setTexturingState(GLINT_TEXTURING)
			.createCompositeState(false)
	);
	private static final RenderType GLINT_DIRECT = create(
		"glint_direct",
		DefaultVertexFormat.POSITION_TEX,
		VertexFormat.Mode.QUADS,
		256,
		RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_GLINT_DIRECT_SHADER)
			.setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, true, false))
			.setWriteMaskState(COLOR_WRITE)
			.setCullState(NO_CULL)
			.setDepthTestState(EQUAL_DEPTH_TEST)
			.setTransparencyState(GLINT_TRANSPARENCY)
			.setTexturingState(GLINT_TEXTURING)
			.createCompositeState(false)
	);
	private static final RenderType ENTITY_GLINT = create(
		"entity_glint",
		DefaultVertexFormat.POSITION_TEX,
		VertexFormat.Mode.QUADS,
		256,
		RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_ENTITY_GLINT_SHADER)
			.setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, true, false))
			.setWriteMaskState(COLOR_WRITE)
			.setCullState(NO_CULL)
			.setDepthTestState(EQUAL_DEPTH_TEST)
			.setTransparencyState(GLINT_TRANSPARENCY)
			.setOutputState(ITEM_ENTITY_TARGET)
			.setTexturingState(ENTITY_GLINT_TEXTURING)
			.createCompositeState(false)
	);
	private static final RenderType ENTITY_GLINT_DIRECT = create(
		"entity_glint_direct",
		DefaultVertexFormat.POSITION_TEX,
		VertexFormat.Mode.QUADS,
		256,
		RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_ENTITY_GLINT_DIRECT_SHADER)
			.setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, true, false))
			.setWriteMaskState(COLOR_WRITE)
			.setCullState(NO_CULL)
			.setDepthTestState(EQUAL_DEPTH_TEST)
			.setTransparencyState(GLINT_TRANSPARENCY)
			.setTexturingState(ENTITY_GLINT_TEXTURING)
			.createCompositeState(false)
	);
	private static final Function<ResourceLocation, RenderType> CRUMBLING = Util.memoize(
		(Function<ResourceLocation, RenderType>)(resourceLocation -> {
			RenderStateShard.TextureStateShard textureStateShard = new RenderStateShard.TextureStateShard(resourceLocation, false, false);
			return create(
				"crumbling",
				DefaultVertexFormat.BLOCK,
				VertexFormat.Mode.QUADS,
				256,
				false,
				true,
				RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_CRUMBLING_SHADER)
					.setTextureState(textureStateShard)
					.setTransparencyState(CRUMBLING_TRANSPARENCY)
					.setWriteMaskState(COLOR_WRITE)
					.setLayeringState(POLYGON_OFFSET_LAYERING)
					.createCompositeState(false)
			);
		})
	);
	private static final Function<ResourceLocation, RenderType> TEXT = Util.memoize(
		(Function<ResourceLocation, RenderType>)(resourceLocation -> create(
				"text",
				DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
				VertexFormat.Mode.QUADS,
				256,
				false,
				true,
				RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_TEXT_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setLightmapState(LIGHTMAP)
					.createCompositeState(false)
			))
	);
	private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY = Util.memoize(
		(Function<ResourceLocation, RenderType>)(resourceLocation -> create(
				"text_intensity",
				DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
				VertexFormat.Mode.QUADS,
				256,
				false,
				true,
				RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_TEXT_INTENSITY_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setLightmapState(LIGHTMAP)
					.createCompositeState(false)
			))
	);
	private static final Function<ResourceLocation, RenderType> TEXT_POLYGON_OFFSET = Util.memoize(
		(Function<ResourceLocation, RenderType>)(resourceLocation -> create(
				"text_polygon_offset",
				DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
				VertexFormat.Mode.QUADS,
				256,
				false,
				true,
				RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_TEXT_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setLightmapState(LIGHTMAP)
					.setLayeringState(POLYGON_OFFSET_LAYERING)
					.createCompositeState(false)
			))
	);
	private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY_POLYGON_OFFSET = Util.memoize(
		(Function<ResourceLocation, RenderType>)(resourceLocation -> create(
				"text_intensity_polygon_offset",
				DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
				VertexFormat.Mode.QUADS,
				256,
				false,
				true,
				RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_TEXT_INTENSITY_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setLightmapState(LIGHTMAP)
					.setLayeringState(POLYGON_OFFSET_LAYERING)
					.createCompositeState(false)
			))
	);
	private static final Function<ResourceLocation, RenderType> TEXT_SEE_THROUGH = Util.memoize(
		(Function<ResourceLocation, RenderType>)(resourceLocation -> create(
				"text_see_through",
				DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
				VertexFormat.Mode.QUADS,
				256,
				false,
				true,
				RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_TEXT_SEE_THROUGH_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setLightmapState(LIGHTMAP)
					.setDepthTestState(NO_DEPTH_TEST)
					.setWriteMaskState(COLOR_WRITE)
					.createCompositeState(false)
			))
	);
	private static final Function<ResourceLocation, RenderType> TEXT_INTENSITY_SEE_THROUGH = Util.memoize(
		(Function<ResourceLocation, RenderType>)(resourceLocation -> create(
				"text_intensity_see_through",
				DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
				VertexFormat.Mode.QUADS,
				256,
				false,
				true,
				RenderType.CompositeState.builder()
					.setShaderState(RENDERTYPE_TEXT_INTENSITY_SEE_THROUGH_SHADER)
					.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
					.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
					.setLightmapState(LIGHTMAP)
					.setDepthTestState(NO_DEPTH_TEST)
					.setWriteMaskState(COLOR_WRITE)
					.createCompositeState(false)
			))
	);
	private static final RenderType LIGHTNING = create(
		"lightning",
		DefaultVertexFormat.POSITION_COLOR,
		VertexFormat.Mode.QUADS,
		256,
		false,
		true,
		RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_LIGHTNING_SHADER)
			.setWriteMaskState(COLOR_DEPTH_WRITE)
			.setTransparencyState(LIGHTNING_TRANSPARENCY)
			.setOutputState(WEATHER_TARGET)
			.createCompositeState(false)
	);
	private static final RenderType TRIPWIRE = create("tripwire", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 262144, true, true, tripwireState());
	private static final RenderType END_PORTAL = create(
		"end_portal",
		DefaultVertexFormat.POSITION,
		VertexFormat.Mode.QUADS,
		256,
		false,
		false,
		RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_END_PORTAL_SHADER)
			.setTextureState(
				RenderStateShard.MultiTextureStateShard.builder()
					.add(TheEndPortalRenderer.END_SKY_LOCATION, false, false)
					.add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false)
					.build()
			)
			.createCompositeState(false)
	);
	private static final RenderType END_GATEWAY = create(
		"end_gateway",
		DefaultVertexFormat.POSITION,
		VertexFormat.Mode.QUADS,
		256,
		false,
		false,
		RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_END_GATEWAY_SHADER)
			.setTextureState(
				RenderStateShard.MultiTextureStateShard.builder()
					.add(TheEndPortalRenderer.END_SKY_LOCATION, false, false)
					.add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false)
					.build()
			)
			.createCompositeState(false)
	);
	public static final RenderType.CompositeRenderType LINES = create(
		"lines",
		DefaultVertexFormat.POSITION_COLOR_NORMAL,
		VertexFormat.Mode.LINES,
		256,
		RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_LINES_SHADER)
			.setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
			.setLayeringState(VIEW_OFFSET_Z_LAYERING)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setOutputState(ITEM_ENTITY_TARGET)
			.setWriteMaskState(COLOR_DEPTH_WRITE)
			.setCullState(NO_CULL)
			.createCompositeState(false)
	);
	public static final RenderType.CompositeRenderType LINE_STRIP = create(
		"line_strip",
		DefaultVertexFormat.POSITION_COLOR_NORMAL,
		VertexFormat.Mode.LINE_STRIP,
		256,
		RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_LINES_SHADER)
			.setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
			.setLayeringState(VIEW_OFFSET_Z_LAYERING)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setOutputState(ITEM_ENTITY_TARGET)
			.setWriteMaskState(COLOR_DEPTH_WRITE)
			.setCullState(NO_CULL)
			.createCompositeState(false)
	);
	private final VertexFormat format;
	private final VertexFormat.Mode mode;
	private final int bufferSize;
	private final boolean affectsCrumbling;
	private final boolean sortOnUpload;
	private final Optional<RenderType> asOptional;

	public static RenderType solid() {
		return SOLID;
	}

	public static RenderType cutoutMipped() {
		return CUTOUT_MIPPED;
	}

	public static RenderType cutout() {
		return CUTOUT;
	}

	private static RenderType.CompositeState translucentState(RenderStateShard.ShaderStateShard shaderStateShard) {
		return RenderType.CompositeState.builder()
			.setLightmapState(LIGHTMAP)
			.setShaderState(shaderStateShard)
			.setTextureState(BLOCK_SHEET_MIPPED)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setOutputState(TRANSLUCENT_TARGET)
			.createCompositeState(true);
	}

	public static RenderType translucent() {
		return TRANSLUCENT;
	}

	private static RenderType.CompositeState translucentMovingBlockState() {
		return RenderType.CompositeState.builder()
			.setLightmapState(LIGHTMAP)
			.setShaderState(RENDERTYPE_TRANSLUCENT_MOVING_BLOCK_SHADER)
			.setTextureState(BLOCK_SHEET_MIPPED)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setOutputState(ITEM_ENTITY_TARGET)
			.createCompositeState(true);
	}

	public static RenderType translucentMovingBlock() {
		return TRANSLUCENT_MOVING_BLOCK;
	}

	public static RenderType translucentNoCrumbling() {
		return TRANSLUCENT_NO_CRUMBLING;
	}

	public static RenderType armorCutoutNoCull(ResourceLocation resourceLocation) {
		return (RenderType)ARMOR_CUTOUT_NO_CULL.apply(resourceLocation);
	}

	public static RenderType entitySolid(ResourceLocation resourceLocation) {
		return (RenderType)ENTITY_SOLID.apply(resourceLocation);
	}

	public static RenderType entityCutout(ResourceLocation resourceLocation) {
		return (RenderType)ENTITY_CUTOUT.apply(resourceLocation);
	}

	public static RenderType entityCutoutNoCull(ResourceLocation resourceLocation, boolean bl) {
		return (RenderType)ENTITY_CUTOUT_NO_CULL.apply(resourceLocation, bl);
	}

	public static RenderType entityCutoutNoCull(ResourceLocation resourceLocation) {
		return entityCutoutNoCull(resourceLocation, true);
	}

	public static RenderType entityCutoutNoCullZOffset(ResourceLocation resourceLocation, boolean bl) {
		return (RenderType)ENTITY_CUTOUT_NO_CULL_Z_OFFSET.apply(resourceLocation, bl);
	}

	public static RenderType entityCutoutNoCullZOffset(ResourceLocation resourceLocation) {
		return entityCutoutNoCullZOffset(resourceLocation, true);
	}

	public static RenderType itemEntityTranslucentCull(ResourceLocation resourceLocation) {
		return (RenderType)ITEM_ENTITY_TRANSLUCENT_CULL.apply(resourceLocation);
	}

	public static RenderType entityTranslucentCull(ResourceLocation resourceLocation) {
		return (RenderType)ENTITY_TRANSLUCENT_CULL.apply(resourceLocation);
	}

	public static RenderType entityTranslucent(ResourceLocation resourceLocation, boolean bl) {
		return (RenderType)ENTITY_TRANSLUCENT.apply(resourceLocation, bl);
	}

	public static RenderType entityTranslucent(ResourceLocation resourceLocation) {
		return entityTranslucent(resourceLocation, true);
	}

	public static RenderType entitySmoothCutout(ResourceLocation resourceLocation) {
		return (RenderType)ENTITY_SMOOTH_CUTOUT.apply(resourceLocation);
	}

	public static RenderType beaconBeam(ResourceLocation resourceLocation, boolean bl) {
		return (RenderType)BEACON_BEAM.apply(resourceLocation, bl);
	}

	public static RenderType entityDecal(ResourceLocation resourceLocation) {
		return (RenderType)ENTITY_DECAL.apply(resourceLocation);
	}

	public static RenderType entityNoOutline(ResourceLocation resourceLocation) {
		return (RenderType)ENTITY_NO_OUTLINE.apply(resourceLocation);
	}

	public static RenderType entityShadow(ResourceLocation resourceLocation) {
		return (RenderType)ENTITY_SHADOW.apply(resourceLocation);
	}

	public static RenderType dragonExplosionAlpha(ResourceLocation resourceLocation) {
		return (RenderType)DRAGON_EXPLOSION_ALPHA.apply(resourceLocation);
	}

	public static RenderType eyes(ResourceLocation resourceLocation) {
		return (RenderType)EYES.apply(resourceLocation);
	}

	public static RenderType energySwirl(ResourceLocation resourceLocation, float f, float g) {
		return create(
			"energy_swirl",
			DefaultVertexFormat.NEW_ENTITY,
			VertexFormat.Mode.QUADS,
			256,
			false,
			true,
			RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setTexturingState(new RenderStateShard.OffsetTexturingStateShard(f, g))
				.setTransparencyState(ADDITIVE_TRANSPARENCY)
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
		return (RenderType)RenderType.CompositeRenderType.OUTLINE.apply(resourceLocation, NO_CULL);
	}

	public static RenderType armorGlint() {
		return ARMOR_GLINT;
	}

	public static RenderType armorEntityGlint() {
		return ARMOR_ENTITY_GLINT;
	}

	public static RenderType glintTranslucent() {
		return GLINT_TRANSLUCENT;
	}

	public static RenderType glint() {
		return GLINT;
	}

	public static RenderType glintDirect() {
		return GLINT_DIRECT;
	}

	public static RenderType entityGlint() {
		return ENTITY_GLINT;
	}

	public static RenderType entityGlintDirect() {
		return ENTITY_GLINT_DIRECT;
	}

	public static RenderType crumbling(ResourceLocation resourceLocation) {
		return (RenderType)CRUMBLING.apply(resourceLocation);
	}

	public static RenderType text(ResourceLocation resourceLocation) {
		return (RenderType)TEXT.apply(resourceLocation);
	}

	public static RenderType textIntensity(ResourceLocation resourceLocation) {
		return (RenderType)TEXT_INTENSITY.apply(resourceLocation);
	}

	public static RenderType textPolygonOffset(ResourceLocation resourceLocation) {
		return (RenderType)TEXT_POLYGON_OFFSET.apply(resourceLocation);
	}

	public static RenderType textIntensityPolygonOffset(ResourceLocation resourceLocation) {
		return (RenderType)TEXT_INTENSITY_POLYGON_OFFSET.apply(resourceLocation);
	}

	public static RenderType textSeeThrough(ResourceLocation resourceLocation) {
		return (RenderType)TEXT_SEE_THROUGH.apply(resourceLocation);
	}

	public static RenderType textIntensitySeeThrough(ResourceLocation resourceLocation) {
		return (RenderType)TEXT_INTENSITY_SEE_THROUGH.apply(resourceLocation);
	}

	public static RenderType lightning() {
		return LIGHTNING;
	}

	private static RenderType.CompositeState tripwireState() {
		return RenderType.CompositeState.builder()
			.setLightmapState(LIGHTMAP)
			.setShaderState(RENDERTYPE_TRIPWIRE_SHADER)
			.setTextureState(BLOCK_SHEET_MIPPED)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setOutputState(WEATHER_TARGET)
			.createCompositeState(true);
	}

	public static RenderType tripwire() {
		return TRIPWIRE;
	}

	public static RenderType endPortal() {
		return END_PORTAL;
	}

	public static RenderType endGateway() {
		return END_GATEWAY;
	}

	public static RenderType lines() {
		return LINES;
	}

	public static RenderType lineStrip() {
		return LINE_STRIP;
	}

	public RenderType(String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
		super(string, runnable, runnable2);
		this.format = vertexFormat;
		this.mode = mode;
		this.bufferSize = i;
		this.affectsCrumbling = bl;
		this.sortOnUpload = bl2;
		this.asOptional = Optional.of(this);
	}

	static RenderType.CompositeRenderType create(String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, RenderType.CompositeState compositeState) {
		return create(string, vertexFormat, mode, i, false, false, compositeState);
	}

	private static RenderType.CompositeRenderType create(
		String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean bl, boolean bl2, RenderType.CompositeState compositeState
	) {
		return new RenderType.CompositeRenderType(string, vertexFormat, mode, i, bl, bl2, compositeState);
	}

	public void end(BufferBuilder bufferBuilder, int i, int j, int k) {
		if (bufferBuilder.building()) {
			if (this.sortOnUpload) {
				bufferBuilder.setQuadSortOrigin((float)i, (float)j, (float)k);
			}

			bufferBuilder.end();
			this.setupRenderState();
			BufferUploader.end(bufferBuilder);
			this.clearRenderState();
		}
	}

	@Override
	public String toString() {
		return this.name;
	}

	public static List<RenderType> chunkBufferLayers() {
		return ImmutableList.of(solid(), cutoutMipped(), cutout(), translucent(), tripwire());
	}

	public int bufferSize() {
		return this.bufferSize;
	}

	public VertexFormat format() {
		return this.format;
	}

	public VertexFormat.Mode mode() {
		return this.mode;
	}

	public Optional<RenderType> outline() {
		return Optional.empty();
	}

	public boolean isOutline() {
		return false;
	}

	public boolean affectsCrumbling() {
		return this.affectsCrumbling;
	}

	public Optional<RenderType> asOptional() {
		return this.asOptional;
	}

	@Environment(EnvType.CLIENT)
	static final class CompositeRenderType extends RenderType {
		static final BiFunction<ResourceLocation, RenderStateShard.CullStateShard, RenderType> OUTLINE = Util.memoize(
			(BiFunction<ResourceLocation, RenderStateShard.CullStateShard, RenderType>)((resourceLocation, cullStateShard) -> RenderType.create(
					"outline",
					DefaultVertexFormat.POSITION_COLOR_TEX,
					VertexFormat.Mode.QUADS,
					256,
					RenderType.CompositeState.builder()
						.setShaderState(RENDERTYPE_OUTLINE_SHADER)
						.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
						.setCullState(cullStateShard)
						.setDepthTestState(NO_DEPTH_TEST)
						.setOutputState(OUTLINE_TARGET)
						.createCompositeState(RenderType.OutlineProperty.IS_OUTLINE)
				))
		);
		private final RenderType.CompositeState state;
		private final Optional<RenderType> outline;
		private final boolean isOutline;

		CompositeRenderType(
			String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean bl, boolean bl2, RenderType.CompositeState compositeState
		) {
			super(
				string,
				vertexFormat,
				mode,
				i,
				bl,
				bl2,
				() -> compositeState.states.forEach(RenderStateShard::setupRenderState),
				() -> compositeState.states.forEach(RenderStateShard::clearRenderState)
			);
			this.state = compositeState;
			this.outline = compositeState.outlineProperty == RenderType.OutlineProperty.AFFECTS_OUTLINE
				? compositeState.textureState.cutoutTexture().map(resourceLocation -> (RenderType)OUTLINE.apply(resourceLocation, compositeState.cullState))
				: Optional.empty();
			this.isOutline = compositeState.outlineProperty == RenderType.OutlineProperty.IS_OUTLINE;
		}

		@Override
		public Optional<RenderType> outline() {
			return this.outline;
		}

		@Override
		public boolean isOutline() {
			return this.isOutline;
		}

		protected final RenderType.CompositeState state() {
			return this.state;
		}

		@Override
		public String toString() {
			return "RenderType[" + this.name + ":" + this.state + "]";
		}
	}

	@Environment(EnvType.CLIENT)
	protected static final class CompositeState {
		final RenderStateShard.EmptyTextureStateShard textureState;
		private final RenderStateShard.ShaderStateShard shaderState;
		private final RenderStateShard.TransparencyStateShard transparencyState;
		private final RenderStateShard.DepthTestStateShard depthTestState;
		final RenderStateShard.CullStateShard cullState;
		private final RenderStateShard.LightmapStateShard lightmapState;
		private final RenderStateShard.OverlayStateShard overlayState;
		private final RenderStateShard.LayeringStateShard layeringState;
		private final RenderStateShard.OutputStateShard outputState;
		private final RenderStateShard.TexturingStateShard texturingState;
		private final RenderStateShard.WriteMaskStateShard writeMaskState;
		private final RenderStateShard.LineStateShard lineState;
		final RenderType.OutlineProperty outlineProperty;
		final ImmutableList<RenderStateShard> states;

		CompositeState(
			RenderStateShard.EmptyTextureStateShard emptyTextureStateShard,
			RenderStateShard.ShaderStateShard shaderStateShard,
			RenderStateShard.TransparencyStateShard transparencyStateShard,
			RenderStateShard.DepthTestStateShard depthTestStateShard,
			RenderStateShard.CullStateShard cullStateShard,
			RenderStateShard.LightmapStateShard lightmapStateShard,
			RenderStateShard.OverlayStateShard overlayStateShard,
			RenderStateShard.LayeringStateShard layeringStateShard,
			RenderStateShard.OutputStateShard outputStateShard,
			RenderStateShard.TexturingStateShard texturingStateShard,
			RenderStateShard.WriteMaskStateShard writeMaskStateShard,
			RenderStateShard.LineStateShard lineStateShard,
			RenderType.OutlineProperty outlineProperty
		) {
			this.textureState = emptyTextureStateShard;
			this.shaderState = shaderStateShard;
			this.transparencyState = transparencyStateShard;
			this.depthTestState = depthTestStateShard;
			this.cullState = cullStateShard;
			this.lightmapState = lightmapStateShard;
			this.overlayState = overlayStateShard;
			this.layeringState = layeringStateShard;
			this.outputState = outputStateShard;
			this.texturingState = texturingStateShard;
			this.writeMaskState = writeMaskStateShard;
			this.lineState = lineStateShard;
			this.outlineProperty = outlineProperty;
			this.states = ImmutableList.of(
				this.textureState,
				this.shaderState,
				this.transparencyState,
				this.depthTestState,
				this.cullState,
				this.lightmapState,
				this.overlayState,
				this.layeringState,
				this.outputState,
				this.texturingState,
				this.writeMaskState,
				this.lineState
			);
		}

		public String toString() {
			return "CompositeState[" + this.states + ", outlineProperty=" + this.outlineProperty + "]";
		}

		public static RenderType.CompositeState.CompositeStateBuilder builder() {
			return new RenderType.CompositeState.CompositeStateBuilder();
		}

		@Environment(EnvType.CLIENT)
		public static class CompositeStateBuilder {
			private RenderStateShard.EmptyTextureStateShard textureState = RenderStateShard.NO_TEXTURE;
			private RenderStateShard.ShaderStateShard shaderState = RenderStateShard.NO_SHADER;
			private RenderStateShard.TransparencyStateShard transparencyState = RenderStateShard.NO_TRANSPARENCY;
			private RenderStateShard.DepthTestStateShard depthTestState = RenderStateShard.LEQUAL_DEPTH_TEST;
			private RenderStateShard.CullStateShard cullState = RenderStateShard.CULL;
			private RenderStateShard.LightmapStateShard lightmapState = RenderStateShard.NO_LIGHTMAP;
			private RenderStateShard.OverlayStateShard overlayState = RenderStateShard.NO_OVERLAY;
			private RenderStateShard.LayeringStateShard layeringState = RenderStateShard.NO_LAYERING;
			private RenderStateShard.OutputStateShard outputState = RenderStateShard.MAIN_TARGET;
			private RenderStateShard.TexturingStateShard texturingState = RenderStateShard.DEFAULT_TEXTURING;
			private RenderStateShard.WriteMaskStateShard writeMaskState = RenderStateShard.COLOR_DEPTH_WRITE;
			private RenderStateShard.LineStateShard lineState = RenderStateShard.DEFAULT_LINE;

			CompositeStateBuilder() {
			}

			public RenderType.CompositeState.CompositeStateBuilder setTextureState(RenderStateShard.EmptyTextureStateShard emptyTextureStateShard) {
				this.textureState = emptyTextureStateShard;
				return this;
			}

			public RenderType.CompositeState.CompositeStateBuilder setShaderState(RenderStateShard.ShaderStateShard shaderStateShard) {
				this.shaderState = shaderStateShard;
				return this;
			}

			public RenderType.CompositeState.CompositeStateBuilder setTransparencyState(RenderStateShard.TransparencyStateShard transparencyStateShard) {
				this.transparencyState = transparencyStateShard;
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
				return this.createCompositeState(bl ? RenderType.OutlineProperty.AFFECTS_OUTLINE : RenderType.OutlineProperty.NONE);
			}

			public RenderType.CompositeState createCompositeState(RenderType.OutlineProperty outlineProperty) {
				return new RenderType.CompositeState(
					this.textureState,
					this.shaderState,
					this.transparencyState,
					this.depthTestState,
					this.cullState,
					this.lightmapState,
					this.overlayState,
					this.layeringState,
					this.outputState,
					this.texturingState,
					this.writeMaskState,
					this.lineState,
					outlineProperty
				);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static enum OutlineProperty {
		NONE("none"),
		IS_OUTLINE("is_outline"),
		AFFECTS_OUTLINE("affects_outline");

		private final String name;

		private OutlineProperty(String string2) {
			this.name = string2;
		}

		public String toString() {
			return this.name;
		}
	}
}
