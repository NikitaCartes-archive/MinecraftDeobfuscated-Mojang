/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RenderType
extends RenderStateShard {
    private static final RenderType SOLID = new CompositeRenderType("solid", DefaultVertexFormat.BLOCK, 7, 0x200000, true, false, CompositeState.builder().setShadeModelState(SMOOTH_SHADE).setLightmapState(LIGHTMAP).setTextureState(BLOCK_SHEET_MIPPED).createCompositeState(true));
    private static final RenderType CUTOUT_MIPPED = new CompositeRenderType("cutout_mipped", DefaultVertexFormat.BLOCK, 7, 131072, true, false, CompositeState.builder().setShadeModelState(SMOOTH_SHADE).setLightmapState(LIGHTMAP).setTextureState(BLOCK_SHEET_MIPPED).setAlphaState(MIDWAY_ALPHA).createCompositeState(true));
    private static final RenderType CUTOUT = new CompositeRenderType("cutout", DefaultVertexFormat.BLOCK, 7, 131072, true, false, CompositeState.builder().setShadeModelState(SMOOTH_SHADE).setLightmapState(LIGHTMAP).setTextureState(BLOCK_SHEET).setAlphaState(MIDWAY_ALPHA).createCompositeState(true));
    private static final RenderType TRANSLUCENT = new CompositeRenderType("translucent", DefaultVertexFormat.BLOCK, 7, 262144, true, true, CompositeState.builder().setShadeModelState(SMOOTH_SHADE).setLightmapState(LIGHTMAP).setTextureState(BLOCK_SHEET_MIPPED).setTransparencyState(TRANSLUCENT_TRANSPARENCY).createCompositeState(true));
    private static final RenderType TRANSLUCENT_NO_CRUMBLING = new RenderType("translucent_no_crumbling", DefaultVertexFormat.BLOCK, 7, 256, false, true, TRANSLUCENT::setupRenderState, TRANSLUCENT::clearRenderState);
    private static final RenderType LEASH = new CompositeRenderType("leash", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, 7, 256, CompositeState.builder().setTextureState(NO_TEXTURE).setCullState(NO_CULL).setLightmapState(LIGHTMAP).createCompositeState(false));
    private static final RenderType WATER_MASK = new CompositeRenderType("water_mask", DefaultVertexFormat.POSITION, 7, 256, CompositeState.builder().setTextureState(NO_TEXTURE).setWriteMaskState(DEPTH_WRITE).createCompositeState(false));
    private static final RenderType GLINT = new CompositeRenderType("glint", DefaultVertexFormat.POSITION_TEX, 7, 256, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, false, false)).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST).setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(GLINT_TEXTURING).createCompositeState(false));
    private static final RenderType ENTITY_GLINT = new CompositeRenderType("entity_glint", DefaultVertexFormat.POSITION_TEX, 7, 256, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(ItemRenderer.ENCHANT_GLINT_LOCATION, false, false)).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST).setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(ENTITY_GLINT_TEXTURING).createCompositeState(false));
    private static final RenderType LIGHTNING = new CompositeRenderType("lightning", DefaultVertexFormat.POSITION_COLOR, 7, 256, false, true, CompositeState.builder().setWriteMaskState(COLOR_WRITE).setTransparencyState(LIGHTNING_TRANSPARENCY).setShadeModelState(SMOOTH_SHADE).createCompositeState(false));
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
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false)).setTransparencyState(NO_TRANSPARENCY).setDiffuseLightingState(DIFFUSE_LIGHTING).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
        return new CompositeRenderType("entity_solid", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, false, compositeState);
    }

    public static RenderType entityCutout(ResourceLocation resourceLocation) {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false)).setTransparencyState(NO_TRANSPARENCY).setDiffuseLightingState(DIFFUSE_LIGHTING).setAlphaState(DEFAULT_ALPHA).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
        return new CompositeRenderType("entity_cutout", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, false, compositeState);
    }

    public static RenderType entityCutoutNoCull(ResourceLocation resourceLocation) {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false)).setTransparencyState(NO_TRANSPARENCY).setDiffuseLightingState(DIFFUSE_LIGHTING).setAlphaState(DEFAULT_ALPHA).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
        return new CompositeRenderType("entity_cutout_no_cull", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, false, compositeState);
    }

    public static RenderType entityTranslucentCull(ResourceLocation resourceLocation) {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDiffuseLightingState(DIFFUSE_LIGHTING).setAlphaState(DEFAULT_ALPHA).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
        return new CompositeRenderType("entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, true, compositeState);
    }

    public static RenderType entityTranslucent(ResourceLocation resourceLocation) {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDiffuseLightingState(DIFFUSE_LIGHTING).setAlphaState(DEFAULT_ALPHA).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
        return new CompositeRenderType("entity_translucent", DefaultVertexFormat.NEW_ENTITY, 7, 256, true, true, compositeState);
    }

    public static RenderType entityForceTranslucent(ResourceLocation resourceLocation) {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false)).setTransparencyState(FORCED_TRANSPARENCY).setDiffuseLightingState(DIFFUSE_LIGHTING).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
        return new CompositeRenderType("entity_force_translucent", DefaultVertexFormat.NEW_ENTITY, 7, 256, false, true, compositeState);
    }

    public static RenderType entitySmoothCutout(ResourceLocation resourceLocation) {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false)).setAlphaState(MIDWAY_ALPHA).setDiffuseLightingState(DIFFUSE_LIGHTING).setShadeModelState(SMOOTH_SHADE).setCullState(NO_CULL).setLightmapState(LIGHTMAP).createCompositeState(true);
        return new CompositeRenderType("entity_smooth_cutout", DefaultVertexFormat.NEW_ENTITY, 7, 256, compositeState);
    }

    public static RenderType beaconBeam(ResourceLocation resourceLocation, boolean bl) {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false)).setTransparencyState(bl ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY).setWriteMaskState(bl ? COLOR_WRITE : COLOR_DEPTH_WRITE).setFogState(NO_FOG).createCompositeState(false);
        return new CompositeRenderType("beacon_beam", DefaultVertexFormat.BLOCK, 7, 256, false, true, compositeState);
    }

    public static RenderType entityDecal(ResourceLocation resourceLocation) {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false)).setDiffuseLightingState(DIFFUSE_LIGHTING).setAlphaState(DEFAULT_ALPHA).setDepthTestState(EQUAL_DEPTH_TEST).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(false);
        return new CompositeRenderType("entity_decal", DefaultVertexFormat.NEW_ENTITY, 7, 256, compositeState);
    }

    public static RenderType entityNoOutline(ResourceLocation resourceLocation) {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setDiffuseLightingState(DIFFUSE_LIGHTING).setAlphaState(DEFAULT_ALPHA).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).setWriteMaskState(COLOR_WRITE).createCompositeState(false);
        return new CompositeRenderType("entity_no_outline", DefaultVertexFormat.NEW_ENTITY, 7, 256, false, true, compositeState);
    }

    public static RenderType entityAlpha(ResourceLocation resourceLocation, float f) {
        CompositeState compositeState = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false)).setAlphaState(new RenderStateShard.AlphaStateShard(f)).setCullState(NO_CULL).createCompositeState(true);
        return new CompositeRenderType("entity_alpha", DefaultVertexFormat.NEW_ENTITY, 7, 256, compositeState);
    }

    public static RenderType eyes(ResourceLocation resourceLocation) {
        RenderStateShard.TextureStateShard textureStateShard = new RenderStateShard.TextureStateShard(resourceLocation, false, false);
        return new CompositeRenderType("eyes", DefaultVertexFormat.NEW_ENTITY, 7, 256, false, true, CompositeState.builder().setTextureState(textureStateShard).setTransparencyState(ADDITIVE_TRANSPARENCY).setWriteMaskState(COLOR_WRITE).setFogState(BLACK_FOG).createCompositeState(false));
    }

    public static RenderType energySwirl(ResourceLocation resourceLocation, float f, float g) {
        return new CompositeRenderType("energy_swirl", DefaultVertexFormat.NEW_ENTITY, 7, 256, false, true, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false)).setTexturingState(new RenderStateShard.OffsetTexturingStateShard(f, g)).setFogState(BLACK_FOG).setTransparencyState(ADDITIVE_TRANSPARENCY).setDiffuseLightingState(DIFFUSE_LIGHTING).setAlphaState(DEFAULT_ALPHA).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(false));
    }

    public static RenderType leash() {
        return LEASH;
    }

    public static RenderType waterMask() {
        return WATER_MASK;
    }

    public static RenderType outline(ResourceLocation resourceLocation) {
        return new CompositeRenderType("outline", DefaultVertexFormat.POSITION_COLOR_TEX, 7, 256, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false)).setCullState(NO_CULL).setDepthTestState(NO_DEPTH_TEST).setAlphaState(DEFAULT_ALPHA).setTexturingState(OUTLINE_TEXTURING).setFogState(NO_FOG).setOutputState(OUTLINE_TARGET).createCompositeState(false));
    }

    public static RenderType glint() {
        return GLINT;
    }

    public static RenderType entityGlint() {
        return ENTITY_GLINT;
    }

    public static RenderType crumbling(int i) {
        RenderStateShard.TextureStateShard textureStateShard = new RenderStateShard.TextureStateShard(ModelBakery.BREAKING_LOCATIONS.get(i), false, false);
        return new CompositeRenderType("crumbling", DefaultVertexFormat.BLOCK, 7, 256, false, true, CompositeState.builder().setTextureState(textureStateShard).setAlphaState(DEFAULT_ALPHA).setTransparencyState(CRUMBLING_TRANSPARENCY).setWriteMaskState(COLOR_WRITE).setLayeringState(POLYGON_OFFSET_LAYERING).createCompositeState(false));
    }

    public static RenderType text(ResourceLocation resourceLocation) {
        return new CompositeRenderType("text", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, 7, 256, false, true, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false)).setAlphaState(DEFAULT_ALPHA).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).createCompositeState(false));
    }

    public static RenderType textSeeThrough(ResourceLocation resourceLocation) {
        return new CompositeRenderType("text_see_through", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, 7, 256, false, true, CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false)).setAlphaState(DEFAULT_ALPHA).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).setDepthTestState(NO_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).createCompositeState(false));
    }

    public static RenderType lightning() {
        return LIGHTNING;
    }

    public static RenderType endPortal(int i) {
        RenderStateShard.TextureStateShard textureStateShard;
        RenderStateShard.TransparencyStateShard transparencyStateShard;
        if (i <= 1) {
            transparencyStateShard = TRANSLUCENT_TRANSPARENCY;
            textureStateShard = new RenderStateShard.TextureStateShard(TheEndPortalRenderer.END_SKY_LOCATION, false, false);
        } else {
            transparencyStateShard = ADDITIVE_TRANSPARENCY;
            textureStateShard = new RenderStateShard.TextureStateShard(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false);
        }
        return new CompositeRenderType("end_portal", DefaultVertexFormat.POSITION_COLOR, 7, 256, false, true, CompositeState.builder().setTransparencyState(transparencyStateShard).setTextureState(textureStateShard).setTexturingState(new RenderStateShard.PortalTexturingStateShard(i)).setFogState(BLACK_FOG).createCompositeState(false));
    }

    public static RenderType lines() {
        return new CompositeRenderType("lines", DefaultVertexFormat.POSITION_COLOR, 1, 256, CompositeState.builder().setLineState(new RenderStateShard.LineStateShard(Math.max(2.5f, (float)Minecraft.getInstance().getWindow().getWidth() / 1920.0f * 2.5f))).setLayeringState(PROJECTION_LAYERING).setTransparencyState(TRANSLUCENT_TRANSPARENCY).createCompositeState(false));
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
        if (!bufferBuilder.building()) {
            return;
        }
        if (this.sortOnUpload) {
            bufferBuilder.sortQuads(i, j, k);
        }
        bufferBuilder.end();
        this.setupRenderState();
        BufferUploader.end(bufferBuilder);
        this.clearRenderState();
    }

    public String toString() {
        return this.name;
    }

    public static List<RenderType> chunkBufferLayers() {
        return ImmutableList.of(RenderType.solid(), RenderType.cutoutMipped(), RenderType.cutout(), RenderType.translucent());
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

    @Environment(value=EnvType.CLIENT)
    static class CompositeRenderType
    extends RenderType {
        private final CompositeState state;
        private int hashCode;
        private boolean hashed = false;

        public CompositeRenderType(String string, VertexFormat vertexFormat, int i, int j, CompositeState compositeState) {
            this(string, vertexFormat, i, j, false, false, compositeState);
        }

        public CompositeRenderType(String string, VertexFormat vertexFormat, int i, int j, boolean bl, boolean bl2, CompositeState compositeState) {
            super(string, vertexFormat, i, j, bl, bl2, () -> compositeState.states.forEach(RenderStateShard::setupRenderState), () -> compositeState.states.forEach(RenderStateShard::clearRenderState));
            this.state = compositeState;
        }

        @Override
        public Optional<ResourceLocation> outlineTexture() {
            return this.state().affectsOutline ? this.state().textureState.texture() : Optional.empty();
        }

        protected final CompositeState state() {
            return this.state;
        }

        @Override
        public boolean equals(@Nullable Object object) {
            if (!super.equals(object)) {
                return false;
            }
            if (this.getClass() != object.getClass()) {
                return false;
            }
            CompositeRenderType compositeRenderType = (CompositeRenderType)object;
            return this.state.equals(compositeRenderType.state);
        }

        @Override
        public int hashCode() {
            if (!this.hashed) {
                this.hashed = true;
                this.hashCode = Objects.hash(super.hashCode(), this.state);
            }
            return this.hashCode;
        }
    }

    @Environment(value=EnvType.CLIENT)
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

        private CompositeState(RenderStateShard.TextureStateShard textureStateShard, RenderStateShard.TransparencyStateShard transparencyStateShard, RenderStateShard.DiffuseLightingStateShard diffuseLightingStateShard, RenderStateShard.ShadeModelStateShard shadeModelStateShard, RenderStateShard.AlphaStateShard alphaStateShard, RenderStateShard.DepthTestStateShard depthTestStateShard, RenderStateShard.CullStateShard cullStateShard, RenderStateShard.LightmapStateShard lightmapStateShard, RenderStateShard.OverlayStateShard overlayStateShard, RenderStateShard.FogStateShard fogStateShard, RenderStateShard.LayeringStateShard layeringStateShard, RenderStateShard.OutputStateShard outputStateShard, RenderStateShard.TexturingStateShard texturingStateShard, RenderStateShard.WriteMaskStateShard writeMaskStateShard, RenderStateShard.LineStateShard lineStateShard, boolean bl) {
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
            this.states = ImmutableList.of(this.textureState, this.transparencyState, this.diffuseLightingState, this.shadeModelState, this.alphaState, this.depthTestState, this.cullState, this.lightmapState, this.overlayState, this.fogState, this.layeringState, this.outputState, new RenderStateShard[]{this.texturingState, this.writeMaskState, this.lineState});
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            CompositeState compositeState = (CompositeState)object;
            return this.affectsOutline == compositeState.affectsOutline && this.states.equals(compositeState.states);
        }

        public int hashCode() {
            return Objects.hash(this.states, this.affectsOutline);
        }

        public static CompositeStateBuilder builder() {
            return new CompositeStateBuilder();
        }

        @Environment(value=EnvType.CLIENT)
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

            public CompositeStateBuilder setTextureState(RenderStateShard.TextureStateShard textureStateShard) {
                this.textureState = textureStateShard;
                return this;
            }

            public CompositeStateBuilder setTransparencyState(RenderStateShard.TransparencyStateShard transparencyStateShard) {
                this.transparencyState = transparencyStateShard;
                return this;
            }

            public CompositeStateBuilder setDiffuseLightingState(RenderStateShard.DiffuseLightingStateShard diffuseLightingStateShard) {
                this.diffuseLightingState = diffuseLightingStateShard;
                return this;
            }

            public CompositeStateBuilder setShadeModelState(RenderStateShard.ShadeModelStateShard shadeModelStateShard) {
                this.shadeModelState = shadeModelStateShard;
                return this;
            }

            public CompositeStateBuilder setAlphaState(RenderStateShard.AlphaStateShard alphaStateShard) {
                this.alphaState = alphaStateShard;
                return this;
            }

            public CompositeStateBuilder setDepthTestState(RenderStateShard.DepthTestStateShard depthTestStateShard) {
                this.depthTestState = depthTestStateShard;
                return this;
            }

            public CompositeStateBuilder setCullState(RenderStateShard.CullStateShard cullStateShard) {
                this.cullState = cullStateShard;
                return this;
            }

            public CompositeStateBuilder setLightmapState(RenderStateShard.LightmapStateShard lightmapStateShard) {
                this.lightmapState = lightmapStateShard;
                return this;
            }

            public CompositeStateBuilder setOverlayState(RenderStateShard.OverlayStateShard overlayStateShard) {
                this.overlayState = overlayStateShard;
                return this;
            }

            public CompositeStateBuilder setFogState(RenderStateShard.FogStateShard fogStateShard) {
                this.fogState = fogStateShard;
                return this;
            }

            public CompositeStateBuilder setLayeringState(RenderStateShard.LayeringStateShard layeringStateShard) {
                this.layeringState = layeringStateShard;
                return this;
            }

            public CompositeStateBuilder setOutputState(RenderStateShard.OutputStateShard outputStateShard) {
                this.outputState = outputStateShard;
                return this;
            }

            public CompositeStateBuilder setTexturingState(RenderStateShard.TexturingStateShard texturingStateShard) {
                this.texturingState = texturingStateShard;
                return this;
            }

            public CompositeStateBuilder setWriteMaskState(RenderStateShard.WriteMaskStateShard writeMaskStateShard) {
                this.writeMaskState = writeMaskStateShard;
                return this;
            }

            public CompositeStateBuilder setLineState(RenderStateShard.LineStateShard lineStateShard) {
                this.lineState = lineStateShard;
                return this;
            }

            public CompositeState createCompositeState(boolean bl) {
                return new CompositeState(this.textureState, this.transparencyState, this.diffuseLightingState, this.shadeModelState, this.alphaState, this.depthTestState, this.cullState, this.lightmapState, this.overlayState, this.fogState, this.layeringState, this.outputState, this.texturingState, this.writeMaskState, this.lineState, bl);
            }
        }
    }
}

