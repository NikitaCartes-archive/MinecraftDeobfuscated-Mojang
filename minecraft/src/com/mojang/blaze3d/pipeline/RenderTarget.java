package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

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

	public void resize(int i, int j) {
		RenderSystem.assertOnRenderThreadOrInit();
		GlStateManager._enableDepthTest();
		if (this.frameBufferId >= 0) {
			this.destroyBuffers();
		}

		this.createBuffers(i, j);
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

	public void createBuffers(int i, int j) {
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

			this.setFilterMode(9728, true);
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
			this.clear();
			this.unbindRead();
		} else {
			throw new IllegalArgumentException("Window " + i + "x" + j + " size out of bounds (max. size: " + k + ")");
		}
	}

	public void setFilterMode(int i) {
		this.setFilterMode(i, false);
	}

	private void setFilterMode(int i, boolean bl) {
		RenderSystem.assertOnRenderThreadOrInit();
		if (bl || i != this.filterMode) {
			this.filterMode = i;
			GlStateManager._bindTexture(this.colorTextureId);
			GlStateManager._texParameter(3553, 10241, i);
			GlStateManager._texParameter(3553, 10240, i);
			GlStateManager._bindTexture(0);
		}
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
		RenderSystem.assertOnRenderThreadOrInit();
		GlStateManager._glBindFramebuffer(36160, this.frameBufferId);
		if (bl) {
			GlStateManager._viewport(0, 0, this.viewWidth, this.viewHeight);
		}
	}

	public void unbindWrite() {
		RenderSystem.assertOnRenderThreadOrInit();
		GlStateManager._glBindFramebuffer(36160, 0);
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
		this._blitToScreen(i, j, bl);
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
		ShaderInstance shaderInstance = (ShaderInstance)Objects.requireNonNull(minecraft.gameRenderer.blitShader, "Blit shader not loaded");
		shaderInstance.setSampler("InSampler", this.colorTextureId);
		shaderInstance.apply();
		BufferBuilder bufferBuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
		bufferBuilder.addVertex(0.0F, 0.0F, 0.0F);
		bufferBuilder.addVertex(1.0F, 0.0F, 0.0F);
		bufferBuilder.addVertex(1.0F, 1.0F, 0.0F);
		bufferBuilder.addVertex(0.0F, 1.0F, 0.0F);
		BufferUploader.draw(bufferBuilder.buildOrThrow());
		shaderInstance.clear();
		GlStateManager._depthMask(true);
		GlStateManager._colorMask(true, true, true, true);
	}

	public void clear() {
		RenderSystem.assertOnRenderThreadOrInit();
		this.bindWrite(true);
		GlStateManager._clearColor(this.clearChannels[0], this.clearChannels[1], this.clearChannels[2], this.clearChannels[3]);
		int i = 16384;
		if (this.useDepth) {
			GlStateManager._clearDepth(1.0);
			i |= 256;
		}

		GlStateManager._clear(i);
		this.unbindWrite();
	}

	public int getColorTextureId() {
		return this.colorTextureId;
	}

	public int getDepthTextureId() {
		return this.depthBufferId;
	}
}
