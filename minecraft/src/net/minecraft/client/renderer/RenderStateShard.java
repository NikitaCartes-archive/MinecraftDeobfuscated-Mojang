package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class RenderStateShard {
	protected final String name;
	private final Runnable setupState;
	private final Runnable clearState;
	protected static final RenderStateShard.TransparencyStateShard NO_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
		"no_transparency", () -> RenderSystem.disableBlend(), () -> {
		}
	);
	protected static final RenderStateShard.TransparencyStateShard FORCED_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
		"forced_transparency", () -> {
			RenderSystem.enableBlend();
			RenderSystem.blendColor(1.0F, 1.0F, 1.0F, 0.15F);
			RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
			RenderSystem.depthMask(false);
		}, () -> {
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.blendColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.depthMask(true);
		}
	);
	protected static final RenderStateShard.TransparencyStateShard ADDITIVE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
		"additive_transparency", () -> {
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
		}, () -> {
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
		}
	);
	protected static final RenderStateShard.TransparencyStateShard LIGHTNING_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
		"lightning_transparency", () -> {
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		}, () -> {
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
		}
	);
	protected static final RenderStateShard.TransparencyStateShard GLINT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("glint_transparency", () -> {
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
	}, () -> {
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
	});
	protected static final RenderStateShard.TransparencyStateShard CRUMBLING_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
		"crumbling_transparency",
		() -> {
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
			);
		},
		() -> {
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
		}
	);
	protected static final RenderStateShard.TransparencyStateShard TRANSLUCENT_TRANSPARENCY = new RenderStateShard.TransparencyStateShard(
		"translucent_transparency", () -> {
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
		}, () -> RenderSystem.disableBlend()
	);
	protected static final RenderStateShard.AlphaStateShard NO_ALPHA = new RenderStateShard.AlphaStateShard(0.0F);
	protected static final RenderStateShard.AlphaStateShard DEFAULT_ALPHA = new RenderStateShard.AlphaStateShard(0.1F);
	protected static final RenderStateShard.AlphaStateShard MIDWAY_ALPHA = new RenderStateShard.AlphaStateShard(0.5F);
	protected static final RenderStateShard.ShadeModelStateShard FLAT_SHADE = new RenderStateShard.ShadeModelStateShard(false);
	protected static final RenderStateShard.ShadeModelStateShard SMOOTH_SHADE = new RenderStateShard.ShadeModelStateShard(true);
	protected static final RenderStateShard.TextureStateShard BLOCK_SHEET_MIPPED = new RenderStateShard.TextureStateShard(
		TextureAtlas.LOCATION_BLOCKS, false, true
	);
	protected static final RenderStateShard.TextureStateShard BLOCK_SHEET = new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false);
	protected static final RenderStateShard.TextureStateShard NO_TEXTURE = new RenderStateShard.TextureStateShard();
	protected static final RenderStateShard.TexturingStateShard DEFAULT_TEXTURING = new RenderStateShard.TexturingStateShard("default_texturing", () -> {
	}, () -> {
	});
	protected static final RenderStateShard.TexturingStateShard OUTLINE_TEXTURING = new RenderStateShard.TexturingStateShard(
		"outline_texturing", () -> RenderSystem.setupOutline(), () -> RenderSystem.teardownOutline()
	);
	protected static final RenderStateShard.TexturingStateShard GLINT_TEXTURING = new RenderStateShard.TexturingStateShard(
		"glint_texturing", () -> setupGlintTexturing(8.0F), () -> {
			RenderSystem.matrixMode(5890);
			RenderSystem.popMatrix();
			RenderSystem.matrixMode(5888);
		}
	);
	protected static final RenderStateShard.TexturingStateShard ENTITY_GLINT_TEXTURING = new RenderStateShard.TexturingStateShard(
		"entity_glint_texturing", () -> setupGlintTexturing(0.16F), () -> {
			RenderSystem.matrixMode(5890);
			RenderSystem.popMatrix();
			RenderSystem.matrixMode(5888);
		}
	);
	protected static final RenderStateShard.LightmapStateShard LIGHTMAP = new RenderStateShard.LightmapStateShard(true);
	protected static final RenderStateShard.LightmapStateShard NO_LIGHTMAP = new RenderStateShard.LightmapStateShard(false);
	protected static final RenderStateShard.OverlayStateShard OVERLAY = new RenderStateShard.OverlayStateShard(true);
	protected static final RenderStateShard.OverlayStateShard NO_OVERLAY = new RenderStateShard.OverlayStateShard(false);
	protected static final RenderStateShard.DiffuseLightingStateShard DIFFUSE_LIGHTING = new RenderStateShard.DiffuseLightingStateShard(true);
	protected static final RenderStateShard.DiffuseLightingStateShard NO_DIFFUSE_LIGHTING = new RenderStateShard.DiffuseLightingStateShard(false);
	protected static final RenderStateShard.CullStateShard CULL = new RenderStateShard.CullStateShard(true);
	protected static final RenderStateShard.CullStateShard NO_CULL = new RenderStateShard.CullStateShard(false);
	protected static final RenderStateShard.DepthTestStateShard NO_DEPTH_TEST = new RenderStateShard.DepthTestStateShard(519);
	protected static final RenderStateShard.DepthTestStateShard EQUAL_DEPTH_TEST = new RenderStateShard.DepthTestStateShard(514);
	protected static final RenderStateShard.DepthTestStateShard LEQUAL_DEPTH_TEST = new RenderStateShard.DepthTestStateShard(515);
	protected static final RenderStateShard.WriteMaskStateShard COLOR_DEPTH_WRITE = new RenderStateShard.WriteMaskStateShard(true, true);
	protected static final RenderStateShard.WriteMaskStateShard COLOR_WRITE = new RenderStateShard.WriteMaskStateShard(true, false);
	protected static final RenderStateShard.WriteMaskStateShard DEPTH_WRITE = new RenderStateShard.WriteMaskStateShard(false, true);
	protected static final RenderStateShard.LayeringStateShard NO_LAYERING = new RenderStateShard.LayeringStateShard("no_layering", () -> {
	}, () -> {
	});
	protected static final RenderStateShard.LayeringStateShard POLYGON_OFFSET_LAYERING = new RenderStateShard.LayeringStateShard(
		"polygon_offset_layering", () -> {
			RenderSystem.polygonOffset(-1.0F, -10.0F);
			RenderSystem.enablePolygonOffset();
		}, () -> {
			RenderSystem.polygonOffset(0.0F, 0.0F);
			RenderSystem.disablePolygonOffset();
		}
	);
	protected static final RenderStateShard.LayeringStateShard PROJECTION_LAYERING = new RenderStateShard.LayeringStateShard("projection_layering", () -> {
		RenderSystem.matrixMode(5889);
		RenderSystem.pushMatrix();
		RenderSystem.scalef(1.0F, 1.0F, 0.999F);
		RenderSystem.matrixMode(5888);
	}, () -> {
		RenderSystem.matrixMode(5889);
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(5888);
	});
	protected static final RenderStateShard.FogStateShard NO_FOG = new RenderStateShard.FogStateShard(
		"no_fog", () -> RenderSystem.disableFog(), () -> RenderSystem.enableFog()
	);
	protected static final RenderStateShard.FogStateShard FOG = new RenderStateShard.FogStateShard("fog", () -> {
	}, () -> {
	});
	protected static final RenderStateShard.FogStateShard BLACK_FOG = new RenderStateShard.FogStateShard(
		"black_fog", () -> FogRenderer.resetFogColor(true), () -> FogRenderer.resetFogColor(false)
	);
	protected static final RenderStateShard.OutputStateShard MAIN_TARGET = new RenderStateShard.OutputStateShard("main_target", () -> {
	}, () -> {
	});
	protected static final RenderStateShard.OutputStateShard OUTLINE_TARGET = new RenderStateShard.OutputStateShard(
		"outline_target",
		() -> Minecraft.getInstance().levelRenderer.entityTarget().bindWrite(false),
		() -> Minecraft.getInstance().getMainRenderTarget().bindWrite(false)
	);
	protected static final RenderStateShard.LineStateShard DEFAULT_LINE = new RenderStateShard.LineStateShard(1.0F);

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
		} else if (object != null && this.getClass() == object.getClass()) {
			RenderStateShard renderStateShard = (RenderStateShard)object;
			return this.name.equals(renderStateShard.name);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return this.name.hashCode();
	}

	private static void setupGlintTexturing(float f) {
		RenderSystem.matrixMode(5890);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		long l = Util.getMillis() * 8L;
		float g = (float)(l % 110000L) / 110000.0F;
		float h = (float)(l % 30000L) / 30000.0F;
		RenderSystem.translatef(-g, h, 0.0F);
		RenderSystem.rotatef(10.0F, 0.0F, 0.0F, 1.0F);
		RenderSystem.scalef(f, f, f);
		RenderSystem.matrixMode(5888);
	}

	@Environment(EnvType.CLIENT)
	public static class AlphaStateShard extends RenderStateShard {
		private final float cutoff;

		public AlphaStateShard(float f) {
			super("alpha", () -> {
				if (f > 0.0F) {
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
			} else if (object == null || this.getClass() != object.getClass()) {
				return false;
			} else {
				return !super.equals(object) ? false : this.cutoff == ((RenderStateShard.AlphaStateShard)object).cutoff;
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(new Object[]{super.hashCode(), this.cutoff});
		}
	}

	@Environment(EnvType.CLIENT)
	static class BooleanStateShard extends RenderStateShard {
		private final boolean enabled;

		public BooleanStateShard(String string, Runnable runnable, Runnable runnable2, boolean bl) {
			super(string, runnable, runnable2);
			this.enabled = bl;
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else if (object != null && this.getClass() == object.getClass()) {
				RenderStateShard.BooleanStateShard booleanStateShard = (RenderStateShard.BooleanStateShard)object;
				return this.enabled == booleanStateShard.enabled;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return Boolean.hashCode(this.enabled);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class CullStateShard extends RenderStateShard.BooleanStateShard {
		public CullStateShard(boolean bl) {
			super("cull", () -> {
				if (bl) {
					RenderSystem.enableCull();
				}
			}, () -> {
				if (bl) {
					RenderSystem.disableCull();
				}
			}, bl);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class DepthTestStateShard extends RenderStateShard {
		private final int function;

		public DepthTestStateShard(int i) {
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
			this.function = i;
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else if (object != null && this.getClass() == object.getClass()) {
				RenderStateShard.DepthTestStateShard depthTestStateShard = (RenderStateShard.DepthTestStateShard)object;
				return this.function == depthTestStateShard.function;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return Integer.hashCode(this.function);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class DiffuseLightingStateShard extends RenderStateShard.BooleanStateShard {
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

	@Environment(EnvType.CLIENT)
	public static class FogStateShard extends RenderStateShard {
		public FogStateShard(String string, Runnable runnable, Runnable runnable2) {
			super(string, runnable, runnable2);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class LayeringStateShard extends RenderStateShard {
		public LayeringStateShard(String string, Runnable runnable, Runnable runnable2) {
			super(string, runnable, runnable2);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class LightmapStateShard extends RenderStateShard.BooleanStateShard {
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

	@Environment(EnvType.CLIENT)
	public static class LineStateShard extends RenderStateShard {
		private final float width;

		public LineStateShard(float f) {
			super("alpha", () -> {
				if (f != 1.0F) {
					RenderSystem.lineWidth(f);
				}
			}, () -> {
				if (f != 1.0F) {
					RenderSystem.lineWidth(1.0F);
				}
			});
			this.width = f;
		}

		@Override
		public boolean equals(@Nullable Object object) {
			if (this == object) {
				return true;
			} else if (object == null || this.getClass() != object.getClass()) {
				return false;
			} else {
				return !super.equals(object) ? false : this.width == ((RenderStateShard.LineStateShard)object).width;
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(new Object[]{super.hashCode(), this.width});
		}
	}

	@Environment(EnvType.CLIENT)
	public static final class OffsetTexturingStateShard extends RenderStateShard.TexturingStateShard {
		private final float uOffset;
		private final float vOffset;

		public OffsetTexturingStateShard(float f, float g) {
			super("offset_texturing", () -> {
				RenderSystem.matrixMode(5890);
				RenderSystem.pushMatrix();
				RenderSystem.loadIdentity();
				RenderSystem.translatef(f, g, 0.0F);
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
			} else if (object != null && this.getClass() == object.getClass()) {
				RenderStateShard.OffsetTexturingStateShard offsetTexturingStateShard = (RenderStateShard.OffsetTexturingStateShard)object;
				return Float.compare(offsetTexturingStateShard.uOffset, this.uOffset) == 0 && Float.compare(offsetTexturingStateShard.vOffset, this.vOffset) == 0;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(new Object[]{this.uOffset, this.vOffset});
		}
	}

	@Environment(EnvType.CLIENT)
	public static class OutputStateShard extends RenderStateShard {
		public OutputStateShard(String string, Runnable runnable, Runnable runnable2) {
			super(string, runnable, runnable2);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class OverlayStateShard extends RenderStateShard.BooleanStateShard {
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

	@Environment(EnvType.CLIENT)
	public static final class PortalTexturingStateShard extends RenderStateShard.TexturingStateShard {
		private final int iteration;

		public PortalTexturingStateShard(int i) {
			super("portal_texturing", () -> {
				RenderSystem.matrixMode(5890);
				RenderSystem.pushMatrix();
				RenderSystem.loadIdentity();
				RenderSystem.translatef(0.5F, 0.5F, 0.0F);
				RenderSystem.scalef(0.5F, 0.5F, 1.0F);
				RenderSystem.translatef(17.0F / (float)i, (2.0F + (float)i / 1.5F) * ((float)(Util.getMillis() % 800000L) / 800000.0F), 0.0F);
				RenderSystem.rotatef(((float)(i * i) * 4321.0F + (float)i * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
				RenderSystem.scalef(4.5F - (float)i / 4.0F, 4.5F - (float)i / 4.0F, 1.0F);
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
			} else if (object != null && this.getClass() == object.getClass()) {
				RenderStateShard.PortalTexturingStateShard portalTexturingStateShard = (RenderStateShard.PortalTexturingStateShard)object;
				return this.iteration == portalTexturingStateShard.iteration;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return Integer.hashCode(this.iteration);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class ShadeModelStateShard extends RenderStateShard {
		private final boolean smooth;

		public ShadeModelStateShard(boolean bl) {
			super("shade_model", () -> RenderSystem.shadeModel(bl ? 7425 : 7424), () -> RenderSystem.shadeModel(7424));
			this.smooth = bl;
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else if (object != null && this.getClass() == object.getClass()) {
				RenderStateShard.ShadeModelStateShard shadeModelStateShard = (RenderStateShard.ShadeModelStateShard)object;
				return this.smooth == shadeModelStateShard.smooth;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return Boolean.hashCode(this.smooth);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class TextureStateShard extends RenderStateShard {
		private final Optional<ResourceLocation> texture;
		private final boolean blur;
		private final boolean mipmap;

		public TextureStateShard(ResourceLocation resourceLocation, boolean bl, boolean bl2) {
			super("texture", () -> {
				RenderSystem.enableTexture();
				TextureManager textureManager = Minecraft.getInstance().getTextureManager();
				textureManager.bind(resourceLocation);
				textureManager.getTexture(resourceLocation).setFilter(bl, bl2);
			}, () -> {
			});
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
			} else if (object != null && this.getClass() == object.getClass()) {
				RenderStateShard.TextureStateShard textureStateShard = (RenderStateShard.TextureStateShard)object;
				return this.texture.equals(textureStateShard.texture) && this.blur == textureStateShard.blur && this.mipmap == textureStateShard.mipmap;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return this.texture.hashCode();
		}

		protected Optional<ResourceLocation> texture() {
			return this.texture;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class TexturingStateShard extends RenderStateShard {
		public TexturingStateShard(String string, Runnable runnable, Runnable runnable2) {
			super(string, runnable, runnable2);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class TransparencyStateShard extends RenderStateShard {
		public TransparencyStateShard(String string, Runnable runnable, Runnable runnable2) {
			super(string, runnable, runnable2);
		}
	}

	@Environment(EnvType.CLIENT)
	public static class WriteMaskStateShard extends RenderStateShard {
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
			} else if (object != null && this.getClass() == object.getClass()) {
				RenderStateShard.WriteMaskStateShard writeMaskStateShard = (RenderStateShard.WriteMaskStateShard)object;
				return this.writeColor == writeMaskStateShard.writeColor && this.writeDepth == writeMaskStateShard.writeDepth;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(new Object[]{this.writeColor, this.writeDepth});
		}
	}
}
