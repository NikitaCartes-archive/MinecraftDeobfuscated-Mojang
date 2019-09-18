package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RenderTarget {
	public int width;
	public int height;
	public int viewWidth;
	public int viewHeight;
	public final boolean useDepth;
	public int frameBufferId;
	public int colorTextureId;
	public int depthBufferId;
	public final float[] clearChannels;
	public int filterMode;

	public RenderTarget(int i, int j, boolean bl, boolean bl2) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		this.useDepth = bl;
		this.frameBufferId = -1;
		this.colorTextureId = -1;
		this.depthBufferId = -1;
		this.clearChannels = new float[4];
		this.clearChannels[0] = 1.0F;
		this.clearChannels[1] = 1.0F;
		this.clearChannels[2] = 1.0F;
		this.clearChannels[3] = 0.0F;
		this.resize(i, j, bl2);
	}

	public void resize(int i, int j, boolean bl) {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> this._resize(i, j, bl));
		} else {
			this._resize(i, j, bl);
		}
	}

	private void _resize(int i, int j, boolean bl) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GlStateManager._enableDepthTest();
		if (this.frameBufferId >= 0) {
			this.destroyBuffers();
		}

		this.createBuffers(i, j, bl);
		GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
	}

	public void destroyBuffers() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		this.unbindRead();
		this.unbindWrite();
		if (this.depthBufferId > -1) {
			GlStateManager._glDeleteRenderbuffers(this.depthBufferId);
			this.depthBufferId = -1;
		}

		if (this.colorTextureId > -1) {
			TextureUtil.releaseTextureId(this.colorTextureId);
			this.colorTextureId = -1;
		}

		if (this.frameBufferId > -1) {
			GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
			GlStateManager._glDeleteFramebuffers(this.frameBufferId);
			this.frameBufferId = -1;
		}
	}

	public void createBuffers(int i, int j, boolean bl) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		this.viewWidth = i;
		this.viewHeight = j;
		this.width = i;
		this.height = j;
		this.frameBufferId = GlStateManager.glGenFramebuffers();
		this.colorTextureId = TextureUtil.generateTextureId();
		if (this.useDepth) {
			this.depthBufferId = GlStateManager.glGenRenderbuffers();
		}

		this.setFilterMode(9728);
		GlStateManager._bindTexture(this.colorTextureId);
		GlStateManager._texImage2D(3553, 0, 32856, this.width, this.height, 0, 6408, 5121, null);
		GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, this.frameBufferId);
		GlStateManager._glFramebufferTexture2D(GlConst.GL_FRAMEBUFFER, GlConst.GL_COLOR_ATTACHMENT0, 3553, this.colorTextureId, 0);
		if (this.useDepth) {
			GlStateManager._glBindRenderbuffer(GlConst.GL_RENDERBUFFER, this.depthBufferId);
			GlStateManager._glRenderbufferStorage(GlConst.GL_RENDERBUFFER, 33190, this.width, this.height);
			GlStateManager._glFramebufferRenderbuffer(GlConst.GL_FRAMEBUFFER, GlConst.GL_DEPTH_ATTACHMENT, GlConst.GL_RENDERBUFFER, this.depthBufferId);
		}

		this.checkStatus();
		this.clear(bl);
		this.unbindRead();
	}

	public void setFilterMode(int i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		this.filterMode = i;
		GlStateManager._bindTexture(this.colorTextureId);
		GlStateManager._texParameter(3553, 10241, i);
		GlStateManager._texParameter(3553, 10240, i);
		GlStateManager._texParameter(3553, 10242, 10496);
		GlStateManager._texParameter(3553, 10243, 10496);
		GlStateManager._bindTexture(0);
	}

	public void checkStatus() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		int i = GlStateManager.glCheckFramebufferStatus(GlConst.GL_FRAMEBUFFER);
		if (i != GlConst.GL_FRAMEBUFFER_COMPLETE) {
			if (i == GlConst.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT) {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
			} else if (i == GlConst.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT) {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
			} else if (i == GlConst.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER) {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
			} else if (i == GlConst.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER) {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
			} else {
				throw new RuntimeException("glCheckFramebufferStatus returned unknown status:" + i);
			}
		}
	}

	public void bindRead() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GlStateManager._bindTexture(this.colorTextureId);
	}

	public void unbindRead() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
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
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, this.frameBufferId);
		if (bl) {
			GlStateManager._viewport(0, 0, this.viewWidth, this.viewHeight);
		}
	}

	public void unbindWrite() {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0));
		} else {
			GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);
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
		RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
		if (!RenderSystem.isInInitPhase()) {
			RenderSystem.recordRenderCall(() -> this._blitToScreen(i, j, bl));
		} else {
			this._blitToScreen(i, j, bl);
		}
	}

	private void _blitToScreen(int i, int j, boolean bl) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GlStateManager._colorMask(true, true, true, false);
		GlStateManager._disableDepthTest();
		GlStateManager._depthMask(false);
		GlStateManager._matrixMode(5889);
		GlStateManager._loadIdentity();
		GlStateManager._ortho(0.0, (double)i, (double)j, 0.0, 1000.0, 3000.0);
		GlStateManager._matrixMode(5888);
		GlStateManager._loadIdentity();
		GlStateManager._translatef(0.0F, 0.0F, -2000.0F);
		GlStateManager._viewport(0, 0, i, j);
		GlStateManager._enableTexture();
		GlStateManager._disableLighting();
		GlStateManager._disableAlphaTest();
		if (bl) {
			GlStateManager._disableBlend();
			GlStateManager._enableColorMaterial();
		}

		GlStateManager._color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.bindRead();
		float f = (float)i;
		float g = (float)j;
		float h = (float)this.viewWidth / (float)this.width;
		float k = (float)this.viewHeight / (float)this.height;
		Tesselator tesselator = RenderSystem.renderThreadTesselator();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferBuilder.vertex(0.0, (double)g, 0.0).uv(0.0, 0.0).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex((double)f, (double)g, 0.0).uv((double)h, 0.0).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex((double)f, 0.0, 0.0).uv((double)h, (double)k).color(255, 255, 255, 255).endVertex();
		bufferBuilder.vertex(0.0, 0.0, 0.0).uv(0.0, (double)k).color(255, 255, 255, 255).endVertex();
		tesselator.end();
		this.unbindRead();
		GlStateManager._depthMask(true);
		GlStateManager._colorMask(true, true, true, true);
	}

	public void clear(boolean bl) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
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
}
