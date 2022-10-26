package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public abstract class RenderTarget {
	private static final int RED_CHANNEL = 0;
	private static final int GREEN_CHANNEL = 1;
	private static final int BLUE_CHANNEL = 2;
	private static final int ALPHA_CHANNEL = 3;
	public int width;
	public int height;
	public int viewWidth;
	public int viewHeight;
	public final boolean useDepth;
	public int frameBufferId;
	protected int colorTextureId;
	protected int depthBufferId;
	private final float[] clearChannels = Util.make(() -> new float[]{1.0F, 1.0F, 1.0F, 0.0F});
	public int filterMode;

	public RenderTarget(boolean bl) {
		this.useDepth = bl;
		this.frameBufferId = -1;
		this.colorTextureId = -1;
		this.depthBufferId = -1;
	}

	public void resize(int i, int j, boolean bl) {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> this._resize(i, j, bl));
		} else {
			this._resize(i, j, bl);
		}
	}

	private void _resize(int i, int j, boolean bl) {
		RenderSystem.assertOnRenderThreadOrInit();
		GlStateManager._enableDepthTest();
		if (this.frameBufferId >= 0) {
			this.destroyBuffers();
		}

		this.createBuffers(i, j, bl);
		GlStateManager._glBindFramebuffer(36160, 0);
	}

	public void destroyBuffers() {
		RenderSystem.assertOnRenderThreadOrInit();
		this.unbindRead();
		this.unbindWrite();
		if (this.depthBufferId > -1) {
			TextureUtil.releaseTextureId(this.depthBufferId);
			this.depthBufferId = -1;
		}

		if (this.colorTextureId > -1) {
			TextureUtil.releaseTextureId(this.colorTextureId);
			this.colorTextureId = -1;
		}

		if (this.frameBufferId > -1) {
			GlStateManager._glBindFramebuffer(36160, 0);
			GlStateManager._glDeleteFramebuffers(this.frameBufferId);
			this.frameBufferId = -1;
		}
	}

	public void copyDepthFrom(RenderTarget renderTarget) {
		RenderSystem.assertOnRenderThreadOrInit();
		GlStateManager._glBindFramebuffer(36008, renderTarget.frameBufferId);
		GlStateManager._glBindFramebuffer(36009, this.frameBufferId);
		GlStateManager._glBlitFrameBuffer(0, 0, renderTarget.width, renderTarget.height, 0, 0, this.width, this.height, 256, 9728);
		GlStateManager._glBindFramebuffer(36160, 0);
	}

	public void createBuffers(int i, int j, boolean bl) {
		RenderSystem.assertOnRenderThreadOrInit();
		int k = RenderSystem.maxSupportedTextureSize();
		if (i > 0 && i <= k && j > 0 && j <= k) {
			this.viewWidth = i;
			this.viewHeight = j;
			this.width = i;
			this.height = j;
			this.frameBufferId = GlStateManager.glGenFramebuffers();
			this.colorTextureId = TextureUtil.generateTextureId();
			if (this.useDepth) {
				this.depthBufferId = TextureUtil.generateTextureId();
				GlStateManager._bindTexture(this.depthBufferId);
				GlStateManager._texParameter(3553, 10241, 9728);
				GlStateManager._texParameter(3553, 10240, 9728);
				GlStateManager._texParameter(3553, 34892, 0);
				GlStateManager._texParameter(3553, 10242, 33071);
				GlStateManager._texParameter(3553, 10243, 33071);
				GlStateManager._texImage2D(3553, 0, 6402, this.width, this.height, 0, 6402, 5126, null);
			}

			this.setFilterMode(9728);
			GlStateManager._bindTexture(this.colorTextureId);
			GlStateManager._texParameter(3553, 10242, 33071);
			GlStateManager._texParameter(3553, 10243, 33071);
			GlStateManager._texImage2D(3553, 0, 32856, this.width, this.height, 0, 6408, 5121, null);
			GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
			GlStateManager._glFramebufferTexture2D(36160, 36064, 3553, this.colorTextureId, 0);
			if (this.useDepth) {
				GlStateManager._glFramebufferTexture2D(36160, 36096, 3553, this.depthBufferId, 0);
			}

			this.checkStatus();
			this.clear(bl);
			this.unbindRead();
		} else {
			throw new IllegalArgumentException("Window " + i + "x" + j + " size out of bounds (max. size: " + k + ")");
		}
	}

	public void setFilterMode(int i) {
		RenderSystem.assertOnRenderThreadOrInit();
		this.filterMode = i;
		GlStateManager._bindTexture(this.colorTextureId);
		GlStateManager._texParameter(3553, 10241, i);
		GlStateManager._texParameter(3553, 10240, i);
		GlStateManager._bindTexture(0);
	}

	public void checkStatus() {
		RenderSystem.assertOnRenderThreadOrInit();
		int i = GlStateManager.glCheckFramebufferStatus(36160);
		if (i != 36053) {
			if (i == 36054) {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
			} else if (i == 36055) {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
			} else if (i == 36059) {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
			} else if (i == 36060) {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
			} else if (i == 36061) {
				throw new RuntimeException("GL_FRAMEBUFFER_UNSUPPORTED");
			} else if (i == 1285) {
				throw new RuntimeException("GL_OUT_OF_MEMORY");
			} else {
				throw new RuntimeException("glCheckFramebufferStatus returned unknown status:" + i);
			}
		}
	}

	public void bindRead() {
		RenderSystem.assertOnRenderThread();
		GlStateManager._bindTexture(this.colorTextureId);
	}

	public void unbindRead() {
		RenderSystem.assertOnRenderThreadOrInit();
		GlStateManager._bindTexture(0);
	}

	public void bindWrite(boolean bl) {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> this._bindWrite(bl));
		} else {
			this._bindWrite(bl);
		}
	}

	private void _bindWrite(boolean bl) {
		RenderSystem.assertOnRenderThreadOrInit();
		GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
		if (bl) {
			GlStateManager._viewport(0, 0, this.viewWidth, this.viewHeight);
		}
	}

	public void unbindWrite() {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> GlStateManager._glBindFramebuffer(36160, 0));
		} else {
			GlStateManager._glBindFramebuffer(36160, 0);
		}
	}

	public void setClearColor(float f, float g, float h, float i) {
		this.clearChannels[0] = f;
		this.clearChannels[1] = g;
		this.clearChannels[2] = h;
		this.clearChannels[3] = i;
	}

	public void blitToScreen(int i, int j) {
		this.blitToScreen(i, j, true);
	}

	public void blitToScreen(int i, int j, boolean bl) {
		RenderSystem.assertOnGameThreadOrInit();
		if (!RenderSystem.isInInitPhase()) {
			RenderSystem.recordRenderCall(() -> this._blitToScreen(i, j, bl));
		} else {
			this._blitToScreen(i, j, bl);
		}
	}

	private void _blitToScreen(int i, int j, boolean bl) {
		RenderSystem.assertOnRenderThread();
		GlStateManager._colorMask(true, true, true, false);
		GlStateManager._disableDepthTest();
		GlStateManager._depthMask(false);
		GlStateManager._viewport(0, 0, i, j);
		if (bl) {
			GlStateManager._disableBlend();
		}

		Minecraft minecraft = Minecraft.getInstance();
		ShaderInstance shaderInstance = minecraft.gameRenderer.blitShader;
		shaderInstance.setSampler("DiffuseSampler", this.colorTextureId);
		Matrix4f matrix4f = new Matrix4f().setOrtho(0.0F, (float)i, (float)j, 0.0F, 1000.0F, 3000.0F);
		RenderSystem.setProjectionMatrix(matrix4f);
		if (shaderInstance.MODEL_VIEW_MATRIX != null) {
			shaderInstance.MODEL_VIEW_MATRIX.set(new Matrix4f().translation(0.0F, 0.0F, -2000.0F));
		}

		if (shaderInstance.PROJECTION_MATRIX != null) {
			shaderInstance.PROJECTION_MATRIX.set(matrix4f);
		}

		shaderInstance.apply();
		float f = (float)i;
		float g = (float)j;
		float h = (float)this.viewWidth / (float)this.width;
		float k = (float)this.viewHeight / (float)this.height;
		Tesselator tesselator = RenderSystem.renderThreadTesselator();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.vertex(0.0, (double)g, 0.0).uv(0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex((double)f, (double)g, 0.0).uv(h, 0.0F).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex((double)f, 0.0, 0.0).uv(h, k).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(0.0, 0.0, 0.0).uv(0.0F, k).color(255, 255, 255, 255).endVertex();
		BufferUploader.draw(bufferBuilder.end());
		shaderInstance.clear();
		GlStateManager._depthMask(true);
		GlStateManager._colorMask(true, true, true, true);
	}

	public void clear(boolean bl) {
		RenderSystem.assertOnRenderThreadOrInit();
		this.bindWrite(true);
		GlStateManager._clearColor(this.clearChannels[0], this.clearChannels[1], this.clearChannels[2], this.clearChannels[3]);
		int i = 16384;
		if (this.useDepth) {
			GlStateManager._clearDepth(1.0);
			i |= 256;
		}

		GlStateManager._clear(i, bl);
		this.unbindWrite();
	}

	public int getColorTextureId() {
		return this.colorTextureId;
	}

	public int getDepthTextureId() {
		return this.depthBufferId;
	}
}
