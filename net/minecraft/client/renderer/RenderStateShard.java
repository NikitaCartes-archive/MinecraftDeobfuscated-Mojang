/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class RenderStateShard {
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
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
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
        RenderSystem.defaultBlendFunc();
    }, () -> RenderSystem.disableBlend());
    protected static final AlphaStateShard NO_ALPHA = new AlphaStateShard(0.0f);
    protected static final AlphaStateShard DEFAULT_ALPHA = new AlphaStateShard(0.003921569f);
    protected static final AlphaStateShard MIDWAY_ALPHA = new AlphaStateShard(0.5f);
    protected static final ShadeModelStateShard FLAT_SHADE = new ShadeModelStateShard(false);
    protected static final ShadeModelStateShard SMOOTH_SHADE = new ShadeModelStateShard(true);
    protected static final TextureStateShard BLOCK_SHEET_MIPPED = new TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, true);
    protected static final TextureStateShard BLOCK_SHEET = new TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false);
    protected static final TextureStateShard NO_TEXTURE = new TextureStateShard();
    protected static final TexturingStateShard DEFAULT_TEXTURING = new TexturingStateShard("default_texturing", () -> {}, () -> {});
    protected static final TexturingStateShard OUTLINE_TEXTURING = new TexturingStateShard("outline_texturing", () -> RenderSystem.setupOutline(), () -> RenderSystem.teardownOutline());
    protected static final TexturingStateShard GLINT_TEXTURING = new TexturingStateShard("glint_texturing", () -> RenderStateShard.setupGlintTexturing(8.0f), () -> {
        RenderSystem.matrixMode(5890);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(5888);
    });
    protected static final TexturingStateShard ENTITY_GLINT_TEXTURING = new TexturingStateShard("entity_glint_texturing", () -> RenderStateShard.setupGlintTexturing(0.16f), () -> {
        RenderSystem.matrixMode(5890);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(5888);
    });
    protected static final LightmapStateShard LIGHTMAP = new LightmapStateShard(true);
    protected static final LightmapStateShard NO_LIGHTMAP = new LightmapStateShard(false);
    protected static final OverlayStateShard OVERLAY = new OverlayStateShard(true);
    protected static final OverlayStateShard NO_OVERLAY = new OverlayStateShard(false);
    protected static final DiffuseLightingStateShard DIFFUSE_LIGHTING = new DiffuseLightingStateShard(true);
    protected static final DiffuseLightingStateShard NO_DIFFUSE_LIGHTING = new DiffuseLightingStateShard(false);
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
    protected static final LayeringStateShard SHADOW_LAYERING = new LayeringStateShard("shadow_layering", () -> {
        RenderSystem.pushMatrix();
        RenderSystem.scalef(0.99975586f, 0.99975586f, 0.99975586f);
    }, RenderSystem::popMatrix);
    protected static final LayeringStateShard PROJECTION_LAYERING = new LayeringStateShard("projection_layering", () -> {
        RenderSystem.matrixMode(5889);
        RenderSystem.pushMatrix();
        RenderSystem.scalef(1.0f, 1.0f, 0.999f);
        RenderSystem.matrixMode(5888);
    }, () -> {
        RenderSystem.matrixMode(5889);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(5888);
    });
    protected static final FogStateShard NO_FOG = new FogStateShard("no_fog", () -> {}, () -> {});
    protected static final FogStateShard FOG = new FogStateShard("fog", () -> {
        FogRenderer.levelFogColor();
        RenderSystem.enableFog();
    }, () -> RenderSystem.disableFog());
    protected static final FogStateShard BLACK_FOG = new FogStateShard("black_fog", () -> {
        RenderSystem.fog(2918, 0.0f, 0.0f, 0.0f, 1.0f);
        RenderSystem.enableFog();
    }, () -> {
        FogRenderer.levelFogColor();
        RenderSystem.disableFog();
    });
    protected static final OutputStateShard MAIN_TARGET = new OutputStateShard("main_target", () -> {}, () -> {});
    protected static final OutputStateShard OUTLINE_TARGET = new OutputStateShard("outline_target", () -> Minecraft.getInstance().levelRenderer.entityTarget().bindWrite(false), () -> Minecraft.getInstance().getMainRenderTarget().bindWrite(false));
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

    public boolean equals(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        RenderStateShard renderStateShard = (RenderStateShard)object;
        return this.name.equals(renderStateShard.name);
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public String toString() {
        return this.name;
    }

    private static void setupGlintTexturing(float f) {
        RenderSystem.matrixMode(5890);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        long l = Util.getMillis() * 8L;
        float g = (float)(l % 110000L) / 110000.0f;
        float h = (float)(l % 30000L) / 30000.0f;
        RenderSystem.translatef(-g, h, 0.0f);
        RenderSystem.rotatef(10.0f, 0.0f, 0.0f, 1.0f);
        RenderSystem.scalef(f, f, f);
        RenderSystem.matrixMode(5888);
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
        public boolean equals(@Nullable Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            if (!super.equals(object)) {
                return false;
            }
            return Objects.equals(this.width, ((LineStateShard)object).width);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.width);
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
    public static class FogStateShard
    extends RenderStateShard {
        public FogStateShard(String string, Runnable runnable, Runnable runnable2) {
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
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            WriteMaskStateShard writeMaskStateShard = (WriteMaskStateShard)object;
            return this.writeColor == writeMaskStateShard.writeColor && this.writeDepth == writeMaskStateShard.writeDepth;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.writeColor, this.writeDepth);
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
        private final int function;

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
            this.function = i;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            DepthTestStateShard depthTestStateShard = (DepthTestStateShard)object;
            return this.function == depthTestStateShard.function;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(this.function);
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
    public static class DiffuseLightingStateShard
    extends BooleanStateShard {
        public DiffuseLightingStateShard(boolean bl) {
            super("diffuse_lighting", () -> {
                if (bl) {
                    Lighting.turnBackOn();
                }
            }, () -> {
                if (bl) {
                    Lighting.turnOff();
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
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            BooleanStateShard booleanStateShard = (BooleanStateShard)object;
            return this.enabled == booleanStateShard.enabled;
        }

        @Override
        public int hashCode() {
            return Boolean.hashCode(this.enabled);
        }

        @Override
        public String toString() {
            return this.name + '[' + this.enabled + ']';
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class PortalTexturingStateShard
    extends TexturingStateShard {
        private final int iteration;

        public PortalTexturingStateShard(int i) {
            super("portal_texturing", () -> {
                RenderSystem.matrixMode(5890);
                RenderSystem.pushMatrix();
                RenderSystem.loadIdentity();
                RenderSystem.translatef(0.5f, 0.5f, 0.0f);
                RenderSystem.scalef(0.5f, 0.5f, 1.0f);
                RenderSystem.translatef(17.0f / (float)i, (2.0f + (float)i / 1.5f) * ((float)(Util.getMillis() % 800000L) / 800000.0f), 0.0f);
                RenderSystem.rotatef(((float)(i * i) * 4321.0f + (float)i * 9.0f) * 2.0f, 0.0f, 0.0f, 1.0f);
                RenderSystem.scalef(4.5f - (float)i / 4.0f, 4.5f - (float)i / 4.0f, 1.0f);
                RenderSystem.mulTextureByProjModelView();
                RenderSystem.matrixMode(5888);
                RenderSystem.setupEndPortalTexGen();
            }, () -> {
                RenderSystem.matrixMode(5890);
                RenderSystem.popMatrix();
                RenderSystem.matrixMode(5888);
                RenderSystem.clearTexGen();
            });
            this.iteration = i;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            PortalTexturingStateShard portalTexturingStateShard = (PortalTexturingStateShard)object;
            return this.iteration == portalTexturingStateShard.iteration;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(this.iteration);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class OffsetTexturingStateShard
    extends TexturingStateShard {
        private final float uOffset;
        private final float vOffset;

        public OffsetTexturingStateShard(float f, float g) {
            super("offset_texturing", () -> {
                RenderSystem.matrixMode(5890);
                RenderSystem.pushMatrix();
                RenderSystem.loadIdentity();
                RenderSystem.translatef(f, g, 0.0f);
                RenderSystem.matrixMode(5888);
            }, () -> {
                RenderSystem.matrixMode(5890);
                RenderSystem.popMatrix();
                RenderSystem.matrixMode(5888);
            });
            this.uOffset = f;
            this.vOffset = g;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            OffsetTexturingStateShard offsetTexturingStateShard = (OffsetTexturingStateShard)object;
            return Float.compare(offsetTexturingStateShard.uOffset, this.uOffset) == 0 && Float.compare(offsetTexturingStateShard.vOffset, this.vOffset) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(Float.valueOf(this.uOffset), Float.valueOf(this.vOffset));
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
    extends RenderStateShard {
        private final Optional<ResourceLocation> texture;
        private final boolean blur;
        private final boolean mipmap;

        public TextureStateShard(ResourceLocation resourceLocation, boolean bl, boolean bl2) {
            super("texture", () -> {
                RenderSystem.enableTexture();
                TextureManager textureManager = Minecraft.getInstance().getTextureManager();
                textureManager.bind(resourceLocation);
                textureManager.getTexture(resourceLocation).setFilter(bl, bl2);
            }, () -> {});
            this.texture = Optional.of(resourceLocation);
            this.blur = bl;
            this.mipmap = bl2;
        }

        public TextureStateShard() {
            super("texture", () -> RenderSystem.disableTexture(), () -> RenderSystem.enableTexture());
            this.texture = Optional.empty();
            this.blur = false;
            this.mipmap = false;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            TextureStateShard textureStateShard = (TextureStateShard)object;
            return this.texture.equals(textureStateShard.texture) && this.blur == textureStateShard.blur && this.mipmap == textureStateShard.mipmap;
        }

        @Override
        public int hashCode() {
            return this.texture.hashCode();
        }

        @Override
        public String toString() {
            return this.name + '[' + this.texture + "(blur=" + this.blur + ", mipmap=" + this.mipmap + ")]";
        }

        protected Optional<ResourceLocation> texture() {
            return this.texture;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class ShadeModelStateShard
    extends RenderStateShard {
        private final boolean smooth;

        public ShadeModelStateShard(boolean bl) {
            super("shade_model", () -> RenderSystem.shadeModel(bl ? 7425 : 7424), () -> RenderSystem.shadeModel(7424));
            this.smooth = bl;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            ShadeModelStateShard shadeModelStateShard = (ShadeModelStateShard)object;
            return this.smooth == shadeModelStateShard.smooth;
        }

        @Override
        public int hashCode() {
            return Boolean.hashCode(this.smooth);
        }

        @Override
        public String toString() {
            return this.name + '[' + (this.smooth ? "smooth" : "flat") + ']';
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class AlphaStateShard
    extends RenderStateShard {
        private final float cutoff;

        public AlphaStateShard(float f) {
            super("alpha", () -> {
                if (f > 0.0f) {
                    RenderSystem.enableAlphaTest();
                    RenderSystem.alphaFunc(516, f);
                } else {
                    RenderSystem.disableAlphaTest();
                }
            }, () -> {
                RenderSystem.disableAlphaTest();
                RenderSystem.defaultAlphaFunc();
            });
            this.cutoff = f;
        }

        @Override
        public boolean equals(@Nullable Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            if (!super.equals(object)) {
                return false;
            }
            return this.cutoff == ((AlphaStateShard)object).cutoff;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), Float.valueOf(this.cutoff));
        }

        @Override
        public String toString() {
            return this.name + '[' + this.cutoff + ']';
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

