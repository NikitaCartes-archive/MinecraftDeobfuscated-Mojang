package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;

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
			.createCompositeState(true)
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
			.createCompositeState(true)
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
			.createCompositeState(true)
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
			.setTextureState(BLOCK_SHEET_MIPPED)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.createCompositeState(true)
	);
	private static final RenderType TRANSLUCENT_NO_CRUMBLING = new RenderType(
		"translucent_no_crumbling", DefaultVertexFormat.BLOCK, 7, 256, false, true, TRANSLUCENT::setupRenderState, TRANSLUCENT::clearRenderState
	);
	private static final RenderType LEASH = new RenderType.CompositeRenderType(
		"leash",
		DefaultVertexFormat.POSITION_COLOR_LIGHTMAP,
		7,
		256,
		RenderType.CompositeState.builder().setTextureState(NO_TEXTURE).setCullState(NO_CULL).setLightmapState(LIGHTMAP).createCompositeState(false)
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
			.setCullState(NO_CULL)
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
			.setCullState(NO_CULL)
			.setDepthTestState(EQUAL_DEPTH_TEST)
			.setTransparencyState(GLINT_TRANSPARENCY)
			.setTexturingState(ENTITY_GLINT_TEXTURING)
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
		return new RenderType.CompositeRenderType("entity_solid", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, false, compositeState);
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
		return new RenderType.CompositeRenderType("entity_cutout", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, false, compositeState);
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
		return new RenderType.CompositeRenderType("entity_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, false, compositeState);
	}

	public static RenderType entityTranslucentCull(ResourceLocation resourceLocation) {
		RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setDiffuseLightingState(DIFFUSE_LIGHTING)
			.setAlphaState(DEFAULT_ALPHA)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true);
		return new RenderType.CompositeRenderType("entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, true, compositeState);
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
		return new RenderType.CompositeRenderType("entity_translucent", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, true, compositeState);
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

	public static RenderType beaconBeam(ResourceLocation resourceLocation, boolean bl) {
		RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
			.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
			.setTransparencyState(bl ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY)
			.setWriteMaskState(bl ? COLOR_WRITE : COLOR_DEPTH_WRITE)
			.setFogState(NO_FOG)
			.createCompositeState(false);
		return new RenderType.CompositeRenderType("beacon_beam", DefaultVertexFormat.BLOCK, 7, 256, false, true, compositeState);
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
			.setWriteMaskState(COLOR_WRITE)
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

	public static RenderType energySwirl(ResourceLocation resourceLocation, float f, float g) {
		return new RenderType.CompositeRenderType(
			"energy_swirl",
			DefaultVertexFormat.NEW_ENTITY,
			7,
			256,
			false,
			true,
			RenderType.CompositeState.builder()
				.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
				.setTexturingState(new RenderStateShard.OffsetTexturingStateShard(f, g))
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

	public void end(BufferBuilder bufferBuilder, int i, int j, int k) {
		if (bufferBuilder.building()) {
			if (this.sortOnUpload) {
				bufferBuilder.sortQuads((float)i, (float)j, (float)k);
			}

			bufferBuilder.end();
			this.setupRenderState();
			BufferUploader.end(bufferBuilder);
			this.clearRenderState();
		}
	}

	public String toString() {
		return this.name;
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
