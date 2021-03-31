/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Triple;

@Environment(value=EnvType.CLIENT)
public abstract class RenderStateShard {
    private static final float VIEW_SCALE_Z_EPSILON = 0.99975586f;
    protected final String name;
    private final Runnable setupState;
    private final Runnable clearState;
    protected static final TransparencyStateShard NO_TRANSPARENCY = new TransparencyStateShard("no_transparency", () -> RenderSystem.disableBlend(), () -> {});
    protected static final TransparencyStateShard ADDITIVE_TRANSPARENCY = new TransparencyStateShard("additive_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    protected static final TransparencyStateShard LIGHTNING_TRANSPARENCY = new TransparencyStateShard("lightning_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    protected static final TransparencyStateShard GLINT_TRANSPARENCY = new TransparencyStateShard("glint_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    protected static final TransparencyStateShard CRUMBLING_TRANSPARENCY = new TransparencyStateShard("crumbling_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    protected static final TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new TransparencyStateShard("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    protected static final ShaderStateShard NO_SHADER = new ShaderStateShard();
    protected static final ShaderStateShard BLOCK_SHADER = new ShaderStateShard(GameRenderer::getBlockShader);
    protected static final ShaderStateShard NEW_ENTITY_SHADER = new ShaderStateShard(GameRenderer::getNewEntityShader);
    protected static final ShaderStateShard POSITION_COLOR_LIGHTMAP_SHADER = new ShaderStateShard(GameRenderer::getPositionColorLightmapShader);
    protected static final ShaderStateShard POSITION_SHADER = new ShaderStateShard(GameRenderer::getPositionShader);
    protected static final ShaderStateShard POSITION_COLOR_TEX_SHADER = new ShaderStateShard(GameRenderer::getPositionColorTexShader);
    protected static final ShaderStateShard POSITION_TEX_SHADER = new ShaderStateShard(GameRenderer::getPositionTexShader);
    protected static final ShaderStateShard POSITION_COLOR_TEX_LIGHTMAP_SHADER = new ShaderStateShard(GameRenderer::getPositionColorTexLightmapShader);
    protected static final ShaderStateShard POSITION_COLOR_SHADER = new ShaderStateShard(GameRenderer::getPositionColorShader);
    protected static final ShaderStateShard RENDERTYPE_SOLID_SHADER = new ShaderStateShard(GameRenderer::getRendertypeSolidShader);
    protected static final ShaderStateShard RENDERTYPE_CUTOUT_MIPPED_SHADER = new ShaderStateShard(GameRenderer::getRendertypeCutoutMippedShader);
    protected static final ShaderStateShard RENDERTYPE_CUTOUT_SHADER = new ShaderStateShard(GameRenderer::getRendertypeCutoutShader);
    protected static final ShaderStateShard RENDERTYPE_TRANSLUCENT_SHADER = new ShaderStateShard(GameRenderer::getRendertypeTranslucentShader);
    protected static final ShaderStateShard RENDERTYPE_TRANSLUCENT_MOVING_BLOCK_SHADER = new ShaderStateShard(GameRenderer::getRendertypeTranslucentMovingBlockShader);
    protected static final ShaderStateShard RENDERTYPE_TRANSLUCENT_NO_CRUMBLING_SHADER = new ShaderStateShard(GameRenderer::getRendertypeTranslucentNoCrumblingShader);
    protected static final ShaderStateShard RENDERTYPE_ARMOR_CUTOUT_NO_CULL_SHADER = new ShaderStateShard(GameRenderer::getRendertypeArmorCutoutNoCullShader);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_SOLID_SHADER = new ShaderStateShard(GameRenderer::getRendertypeEntitySolidShader);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_CUTOUT_SHADER = new ShaderStateShard(GameRenderer::getRendertypeEntityCutoutShader);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER = new ShaderStateShard(GameRenderer::getRendertypeEntityCutoutNoCullShader);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_CUTOUT_NO_CULL_Z_OFFSET_SHADER = new ShaderStateShard(GameRenderer::getRendertypeEntityCutoutNoCullZOffsetShader);
    protected static final ShaderStateShard RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER = new ShaderStateShard(GameRenderer::getRendertypeItemEntityTranslucentCullShader);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER = new ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentCullShader);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_TRANSLUCENT_SHADER = new ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentShader);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_SMOOTH_CUTOUT_SHADER = new ShaderStateShard(GameRenderer::getRendertypeEntitySmoothCutoutShader);
    protected static final ShaderStateShard RENDERTYPE_BEACON_BEAM_SHADER = new ShaderStateShard(GameRenderer::getRendertypeBeaconBeamShader);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_DECAL_SHADER = new ShaderStateShard(GameRenderer::getRendertypeEntityDecalShader);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_NO_OUTLINE_SHADER = new ShaderStateShard(GameRenderer::getRendertypeEntityNoOutlineShader);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_SHADOW_SHADER = new ShaderStateShard(GameRenderer::getRendertypeEntityShadowShader);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_ALPHA_SHADER = new ShaderStateShard(GameRenderer::getRendertypeEntityAlphaShader);
    protected static final ShaderStateShard RENDERTYPE_EYES_SHADER = new ShaderStateShard(GameRenderer::getRendertypeEyesShader);
    protected static final ShaderStateShard RENDERTYPE_ENERGY_SWIRL_SHADER = new ShaderStateShard(GameRenderer::getRendertypeEnergySwirlShader);
    protected static final ShaderStateShard RENDERTYPE_LEASH_SHADER = new ShaderStateShard(GameRenderer::getRendertypeLeashShader);
    protected static final ShaderStateShard RENDERTYPE_WATER_MASK_SHADER = new ShaderStateShard(GameRenderer::getRendertypeWaterMaskShader);
    protected static final ShaderStateShard RENDERTYPE_OUTLINE_SHADER = new ShaderStateShard(GameRenderer::getRendertypeOutlineShader);
    protected static final ShaderStateShard RENDERTYPE_ARMOR_GLINT_SHADER = new ShaderStateShard(GameRenderer::getRendertypeArmorGlintShader);
    protected static final ShaderStateShard RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER = new ShaderStateShard(GameRenderer::getRendertypeArmorEntityGlintShader);
    protected static final ShaderStateShard RENDERTYPE_GLINT_TRANSLUCENT_SHADER = new ShaderStateShard(GameRenderer::getRendertypeGlintTranslucentShader);
    protected static final ShaderStateShard RENDERTYPE_GLINT_SHADER = new ShaderStateShard(GameRenderer::getRendertypeGlintShader);
    protected static final ShaderStateShard RENDERTYPE_GLINT_DIRECT_SHADER = new ShaderStateShard(GameRenderer::getRendertypeGlintDirectShader);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_GLINT_SHADER = new ShaderStateShard(GameRenderer::getRendertypeEntityGlintShader);
    protected static final ShaderStateShard RENDERTYPE_ENTITY_GLINT_DIRECT_SHADER = new ShaderStateShard(GameRenderer::getRendertypeEntityGlintDirectShader);
    protected static final ShaderStateShard RENDERTYPE_CRUMBLING_SHADER = new ShaderStateShard(GameRenderer::getRendertypeCrumblingShader);
    protected static final ShaderStateShard RENDERTYPE_TEXT_SHADER = new ShaderStateShard(GameRenderer::getRendertypeTextShader);
    protected static final ShaderStateShard RENDERTYPE_TEXT_SEE_THROUGH_SHADER = new ShaderStateShard(GameRenderer::getRendertypeTextSeeThroughShader);
    protected static final ShaderStateShard RENDERTYPE_LIGHTNING_SHADER = new ShaderStateShard(GameRenderer::getRendertypeLightningShader);
    protected static final ShaderStateShard RENDERTYPE_TRIPWIRE_SHADER = new ShaderStateShard(GameRenderer::getRendertypeTripwireShader);
    protected static final ShaderStateShard RENDERTYPE_END_PORTAL_SHADER = new ShaderStateShard(GameRenderer::getRendertypeEndPortalShader);
    protected static final ShaderStateShard RENDERTYPE_END_GATEWAY_SHADER = new ShaderStateShard(GameRenderer::getRendertypeEndGatewayShader);
    protected static final ShaderStateShard RENDERTYPE_LINES_SHADER = new ShaderStateShard(GameRenderer::getRendertypeLinesShader);
    protected static final TextureStateShard BLOCK_SHEET_MIPPED = new TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, true);
    protected static final TextureStateShard BLOCK_SHEET = new TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false);
    protected static final EmptyTextureStateShard NO_TEXTURE = new EmptyTextureStateShard();
    protected static final TexturingStateShard DEFAULT_TEXTURING = new TexturingStateShard("default_texturing", () -> {}, () -> {});
    protected static final TexturingStateShard GLINT_TEXTURING = new TexturingStateShard("glint_texturing", () -> RenderStateShard.setupGlintTexturing(8.0f), () -> RenderSystem.resetTextureMatrix());
    protected static final TexturingStateShard ENTITY_GLINT_TEXTURING = new TexturingStateShard("entity_glint_texturing", () -> RenderStateShard.setupGlintTexturing(0.16f), () -> RenderSystem.resetTextureMatrix());
    protected static final LightmapStateShard LIGHTMAP = new LightmapStateShard(true);
    protected static final LightmapStateShard NO_LIGHTMAP = new LightmapStateShard(false);
    protected static final OverlayStateShard OVERLAY = new OverlayStateShard(true);
    protected static final OverlayStateShard NO_OVERLAY = new OverlayStateShard(false);
    protected static final CullStateShard CULL = new CullStateShard(true);
    protected static final CullStateShard NO_CULL = new CullStateShard(false);
    protected static final DepthTestStateShard NO_DEPTH_TEST = new DepthTestStateShard("always", 519);
    protected static final DepthTestStateShard EQUAL_DEPTH_TEST = new DepthTestStateShard("==", 514);
    protected static final DepthTestStateShard LEQUAL_DEPTH_TEST = new DepthTestStateShard("<=", 515);
    protected static final WriteMaskStateShard COLOR_DEPTH_WRITE = new WriteMaskStateShard(true, true);
    protected static final WriteMaskStateShard COLOR_WRITE = new WriteMaskStateShard(true, false);
    protected static final WriteMaskStateShard DEPTH_WRITE = new WriteMaskStateShard(false, true);
    protected static final LayeringStateShard NO_LAYERING = new LayeringStateShard("no_layering", () -> {}, () -> {});
    protected static final LayeringStateShard POLYGON_OFFSET_LAYERING = new LayeringStateShard("polygon_offset_layering", () -> {
        RenderSystem.polygonOffset(-1.0f, -10.0f);
        RenderSystem.enablePolygonOffset();
    }, () -> {
        RenderSystem.polygonOffset(0.0f, 0.0f);
        RenderSystem.disablePolygonOffset();
    });
    protected static final LayeringStateShard VIEW_OFFSET_Z_LAYERING = new LayeringStateShard("view_offset_z_layering", () -> {
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.scale(0.99975586f, 0.99975586f, 0.99975586f);
        RenderSystem.applyModelViewMatrix();
    }, () -> {
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    });
    protected static final OutputStateShard MAIN_TARGET = new OutputStateShard("main_target", () -> {}, () -> {});
    protected static final OutputStateShard OUTLINE_TARGET = new OutputStateShard("outline_target", () -> Minecraft.getInstance().levelRenderer.entityTarget().bindWrite(false), () -> Minecraft.getInstance().getMainRenderTarget().bindWrite(false));
    protected static final OutputStateShard TRANSLUCENT_TARGET = new OutputStateShard("translucent_target", () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().levelRenderer.getTranslucentTarget().bindWrite(false);
        }
    }, () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }
    });
    protected static final OutputStateShard PARTICLES_TARGET = new OutputStateShard("particles_target", () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().levelRenderer.getParticlesTarget().bindWrite(false);
        }
    }, () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }
    });
    protected static final OutputStateShard WEATHER_TARGET = new OutputStateShard("weather_target", () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().levelRenderer.getWeatherTarget().bindWrite(false);
        }
    }, () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }
    });
    protected static final OutputStateShard CLOUDS_TARGET = new OutputStateShard("clouds_target", () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().levelRenderer.getCloudsTarget().bindWrite(false);
        }
    }, () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }
    });
    protected static final OutputStateShard ITEM_ENTITY_TARGET = new OutputStateShard("item_entity_target", () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().levelRenderer.getItemEntityTarget().bindWrite(false);
        }
    }, () -> {
        if (Minecraft.useShaderTransparency()) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }
    });
    protected static final LineStateShard DEFAULT_LINE = new LineStateShard(OptionalDouble.of(1.0));

    public RenderStateShard(String string, Runnable runnable, Runnable runnable2) {
        this.name = string;
        this.setupState = runnable;
        this.clearState = runnable2;
    }

    public void setupRenderState() {
        this.setupState.run();
    }

    public void clearRenderState() {
        this.clearState.run();
    }

    public String toString() {
        return this.name;
    }

    private static void setupGlintTexturing(float f) {
        long l = Util.getMillis() * 8L;
        float g = (float)(l % 110000L) / 110000.0f;
        float h = (float)(l % 30000L) / 30000.0f;
        Matrix4f matrix4f = Matrix4f.createTranslateMatrix(-g, h, 0.0f);
        matrix4f.multiply(Vector3f.ZP.rotationDegrees(10.0f));
        matrix4f.multiply(Matrix4f.createScaleMatrix(f, f, f));
        RenderSystem.setTextureMatrix(matrix4f);
    }

    @Environment(value=EnvType.CLIENT)
    public static class LineStateShard
    extends RenderStateShard {
        private final OptionalDouble width;

        public LineStateShard(OptionalDouble optionalDouble) {
            super("line_width", () -> {
                if (!Objects.equals(optionalDouble, OptionalDouble.of(1.0))) {
                    if (optionalDouble.isPresent()) {
                        RenderSystem.lineWidth((float)optionalDouble.getAsDouble());
                    } else {
                        RenderSystem.lineWidth(Math.max(2.5f, (float)Minecraft.getInstance().getWindow().getWidth() / 1920.0f * 2.5f));
                    }
                }
            }, () -> {
                if (!Objects.equals(optionalDouble, OptionalDouble.of(1.0))) {
                    RenderSystem.lineWidth(1.0f);
                }
            });
            this.width = optionalDouble;
        }

        @Override
        public String toString() {
            return this.name + '[' + (this.width.isPresent() ? Double.valueOf(this.width.getAsDouble()) : "window_scale") + ']';
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class OutputStateShard
    extends RenderStateShard {
        public OutputStateShard(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class LayeringStateShard
    extends RenderStateShard {
        public LayeringStateShard(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class WriteMaskStateShard
    extends RenderStateShard {
        private final boolean writeColor;
        private final boolean writeDepth;

        public WriteMaskStateShard(boolean bl, boolean bl2) {
            super("write_mask_state", () -> {
                if (!bl2) {
                    RenderSystem.depthMask(bl2);
                }
                if (!bl) {
                    RenderSystem.colorMask(bl, bl, bl, bl);
                }
            }, () -> {
                if (!bl2) {
                    RenderSystem.depthMask(true);
                }
                if (!bl) {
                    RenderSystem.colorMask(true, true, true, true);
                }
            });
            this.writeColor = bl;
            this.writeDepth = bl2;
        }

        @Override
        public String toString() {
            return this.name + "[writeColor=" + this.writeColor + ", writeDepth=" + this.writeDepth + ']';
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class DepthTestStateShard
    extends RenderStateShard {
        private final String functionName;

        public DepthTestStateShard(String string, int i) {
            super("depth_test", () -> {
                if (i != 519) {
                    RenderSystem.enableDepthTest();
                    RenderSystem.depthFunc(i);
                }
            }, () -> {
                if (i != 519) {
                    RenderSystem.disableDepthTest();
                    RenderSystem.depthFunc(515);
                }
            });
            this.functionName = string;
        }

        @Override
        public String toString() {
            return this.name + '[' + this.functionName + ']';
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class CullStateShard
    extends BooleanStateShard {
        public CullStateShard(boolean bl) {
            super("cull", () -> {
                if (!bl) {
                    RenderSystem.disableCull();
                }
            }, () -> {
                if (!bl) {
                    RenderSystem.enableCull();
                }
            }, bl);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class OverlayStateShard
    extends BooleanStateShard {
        public OverlayStateShard(boolean bl) {
            super("overlay", () -> {
                if (bl) {
                    Minecraft.getInstance().gameRenderer.overlayTexture().setupOverlayColor();
                }
            }, () -> {
                if (bl) {
                    Minecraft.getInstance().gameRenderer.overlayTexture().teardownOverlayColor();
                }
            }, bl);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class LightmapStateShard
    extends BooleanStateShard {
        public LightmapStateShard(boolean bl) {
            super("lightmap", () -> {
                if (bl) {
                    Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
                }
            }, () -> {
                if (bl) {
                    Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
                }
            }, bl);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class BooleanStateShard
    extends RenderStateShard {
        private final boolean enabled;

        public BooleanStateShard(String string, Runnable runnable, Runnable runnable2, boolean bl) {
            super(string, runnable, runnable2);
            this.enabled = bl;
        }

        @Override
        public String toString() {
            return this.name + '[' + this.enabled + ']';
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class OffsetTexturingStateShard
    extends TexturingStateShard {
        public OffsetTexturingStateShard(float f, float g) {
            super("offset_texturing", () -> RenderSystem.setTextureMatrix(Matrix4f.createTranslateMatrix(f, g, 0.0f)), () -> RenderSystem.resetTextureMatrix());
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class TexturingStateShard
    extends RenderStateShard {
        public TexturingStateShard(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class TextureStateShard
    extends EmptyTextureStateShard {
        private final Optional<ResourceLocation> texture;
        private final boolean blur;
        private final boolean mipmap;

        public TextureStateShard(ResourceLocation resourceLocation, boolean bl, boolean bl2) {
            super(() -> {
                RenderSystem.enableTexture();
                TextureManager textureManager = Minecraft.getInstance().getTextureManager();
                textureManager.getTexture(resourceLocation).setFilter(bl, bl2);
                RenderSystem.setShaderTexture(0, resourceLocation);
            }, () -> {});
            this.texture = Optional.of(resourceLocation);
            this.blur = bl;
            this.mipmap = bl2;
        }

        @Override
        public String toString() {
            return this.name + '[' + this.texture + "(blur=" + this.blur + ", mipmap=" + this.mipmap + ")]";
        }

        @Override
        protected Optional<ResourceLocation> cutoutTexture() {
            return this.texture;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class MultiTextureStateShard
    extends EmptyTextureStateShard {
        private final Optional<ResourceLocation> cutoutTexture;

        private MultiTextureStateShard(ImmutableList<Triple<ResourceLocation, Boolean, Boolean>> immutableList) {
            super(() -> {
                int i = 0;
                for (Triple triple : immutableList) {
                    TextureManager textureManager = Minecraft.getInstance().getTextureManager();
                    textureManager.getTexture((ResourceLocation)triple.getLeft()).setFilter((Boolean)triple.getMiddle(), (Boolean)triple.getRight());
                    RenderSystem.setShaderTexture(i++, (ResourceLocation)triple.getLeft());
                }
            }, () -> {});
            this.cutoutTexture = immutableList.stream().findFirst().map(Triple::getLeft);
        }

        @Override
        protected Optional<ResourceLocation> cutoutTexture() {
            return this.cutoutTexture;
        }

        public static Builder builder() {
            return new Builder();
        }

        @Environment(value=EnvType.CLIENT)
        public static final class Builder {
            private final ImmutableList.Builder<Triple<ResourceLocation, Boolean, Boolean>> builder = new ImmutableList.Builder();

            public Builder add(ResourceLocation resourceLocation, boolean bl, boolean bl2) {
                this.builder.add((Object)Triple.of(resourceLocation, bl, bl2));
                return this;
            }

            public MultiTextureStateShard build() {
                return new MultiTextureStateShard((ImmutableList)this.builder.build());
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class EmptyTextureStateShard
    extends RenderStateShard {
        public EmptyTextureStateShard(Runnable runnable, Runnable runnable2) {
            super("texture", runnable, runnable2);
        }

        private EmptyTextureStateShard() {
            super("texture", () -> {}, () -> {});
        }

        protected Optional<ResourceLocation> cutoutTexture() {
            return Optional.empty();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class ShaderStateShard
    extends RenderStateShard {
        private final Optional<Supplier<ShaderInstance>> shader;

        public ShaderStateShard(Supplier<ShaderInstance> supplier) {
            super("shader", () -> RenderSystem.setShader(supplier), () -> {});
            this.shader = Optional.of(supplier);
        }

        public ShaderStateShard() {
            super("shader", () -> RenderSystem.setShader(() -> null), () -> {});
            this.shader = Optional.empty();
        }

        @Override
        public String toString() {
            return this.name + '[' + this.shader + "]";
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class TransparencyStateShard
    extends RenderStateShard {
        public TransparencyStateShard(String string, Runnable runnable, Runnable runnable2) {
            super(string, runnable, runnable2);
        }
    }
}

