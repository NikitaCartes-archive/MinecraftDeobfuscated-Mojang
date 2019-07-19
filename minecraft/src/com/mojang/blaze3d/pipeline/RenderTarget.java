package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
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
		if (!GLX.isUsingFBOs()) {
			this.viewWidth = i;
			this.viewHeight = j;
		} else {
			GlStateManager.enableDepthTest();
			if (this.frameBufferId >= 0) {
				this.destroyBuffers();
			}

			this.createBuffers(i, j, bl);
			GLX.glBindFramebuffer(GLX.GL_FRAMEBUFFER, 0);
		}
	}

	public void destroyBuffers() {
		if (GLX.isUsingFBOs()) {
			this.unbindRead();
			this.unbindWrite();
			if (this.depthBufferId > -1) {
				GLX.glDeleteRenderbuffers(this.depthBufferId);
				this.depthBufferId = -1;
			}

			if (this.colorTextureId > -1) {
				TextureUtil.releaseTextureId(this.colorTextureId);
				this.colorTextureId = -1;
			}

			if (this.frameBufferId > -1) {
				GLX.glBindFramebuffer(GLX.GL_FRAMEBUFFER, 0);
				GLX.glDeleteFramebuffers(this.frameBufferId);
				this.frameBufferId = -1;
			}
		}
	}

	public void createBuffers(int i, int j, boolean bl) {
		this.viewWidth = i;
		this.viewHeight = j;
		this.width = i;
		this.height = j;
		if (!GLX.isUsingFBOs()) {
			this.clear(bl);
		} else {
			this.frameBufferId = GLX.glGenFramebuffers();
			this.colorTextureId = TextureUtil.generateTextureId();
			if (this.useDepth) {
				this.depthBufferId = GLX.glGenRenderbuffers();
			}

			this.setFilterMode(9728);
			GlStateManager.bindTexture(this.colorTextureId);
			GlStateManager.texImage2D(3553, 0, 32856, this.width, this.height, 0, 6408, 5121, null);
			GLX.glBindFramebuffer(GLX.GL_FRAMEBUFFER, this.frameBufferId);
			GLX.glFramebufferTexture2D(GLX.GL_FRAMEBUFFER, GLX.GL_COLOR_ATTACHMENT0, 3553, this.colorTextureId, 0);
			if (this.useDepth) {
				GLX.glBindRenderbuffer(GLX.GL_RENDERBUFFER, this.depthBufferId);
				GLX.glRenderbufferStorage(GLX.GL_RENDERBUFFER, 33190, this.width, this.height);
				GLX.glFramebufferRenderbuffer(GLX.GL_FRAMEBUFFER, GLX.GL_DEPTH_ATTACHMENT, GLX.GL_RENDERBUFFER, this.depthBufferId);
			}

			this.checkStatus();
			this.clear(bl);
			this.unbindRead();
		}
	}

	public void setFilterMode(int i) {
		if (GLX.isUsingFBOs()) {
			this.filterMode = i;
			GlStateManager.bindTexture(this.colorTextureId);
			GlStateManager.texParameter(3553, 10241, i);
			GlStateManager.texParameter(3553, 10240, i);
			GlStateManager.texParameter(3553, 10242, 10496);
			GlStateManager.texParameter(3553, 10243, 10496);
			GlStateManager.bindTexture(0);
		}
	}

	public void checkStatus() {
		int i = GLX.glCheckFramebufferStatus(GLX.GL_FRAMEBUFFER);
		if (i != GLX.GL_FRAMEBUFFER_COMPLETE) {
			if (i == GLX.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT) {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
			} else if (i == GLX.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT) {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
			} else if (i == GLX.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER) {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
			} else if (i == GLX.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER) {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
			} else {
				throw new RuntimeException("glCheckFramebufferStatus returned unknown status:" + i);
			}
		}
	}

	public void bindRead() {
		if (GLX.isUsingFBOs()) {
			GlStateManager.bindTexture(this.colorTextureId);
		}
	}

	public void unbindRead() {
		if (GLX.isUsingFBOs()) {
			GlStateManager.bindTexture(0);
		}
	}

	public void bindWrite(boolean bl) {
		if (GLX.isUsingFBOs()) {
			GLX.glBindFramebuffer(GLX.GL_FRAMEBUFFER, this.frameBufferId);
			if (bl) {
				GlStateManager.viewport(0, 0, this.viewWidth, this.viewHeight);
			}
		}
	}

	public void unbindWrite() {
		if (GLX.isUsingFBOs()) {
			GLX.glBindFramebuffer(GLX.GL_FRAMEBUFFER, 0);
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
		if (GLX.isUsingFBOs()) {
			GlStateManager.colorMask(true, true, true, false);
			GlStateManager.disableDepthTest();
			GlStateManager.depthMask(false);
			GlStateManager.matrixMode(5889);
			GlStateManager.loadIdentity();
			GlStateManager.ortho(0.0, (double)i, (double)j, 0.0, 1000.0, 3000.0);
			GlStateManager.matrixMode(5888);
			GlStateManager.loadIdentity();
			GlStateManager.translatef(0.0F, 0.0F, -2000.0F);
			GlStateManager.viewport(0, 0, i, j);
			GlStateManager.enableTexture();
			GlStateManager.disableLighting();
			GlStateManager.disableAlphaTest();
			if (bl) {
				GlStateManager.disableBlend();
				GlStateManager.enableColorMaterial();
			}

			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.bindRead();
			float f = (float)i;
			float g = (float)j;
			float h = (float)this.viewWidth / (float)this.width;
			float k = (float)this.viewHeight / (float)this.height;
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferBuilder.vertex(0.0, (double)g, 0.0).uv(0.0, 0.0).color(255, 255, 255, 255).endVertex();
			bufferBuilder.vertex((double)f, (double)g, 0.0).uv((double)h, 0.0).color(255, 255, 255, 255).endVertex();
			bufferBuilder.vertex((double)f, 0.0, 0.0).uv((double)h, (double)k).color(255, 255, 255, 255).endVertex();
			bufferBuilder.vertex(0.0, 0.0, 0.0).uv(0.0, (double)k).color(255, 255, 255, 255).endVertex();
			tesselator.end();
			this.unbindRead();
			GlStateManager.depthMask(true);
			GlStateManager.colorMask(true, true, true, true);
		}
	}

	public void clear(boolean bl) {
		this.bindWrite(true);
		GlStateManager.clearColor(this.clearChannels[0], this.clearChannels[1], this.clearChannels[2], this.clearChannels[3]);
		int i = 16384;
		if (this.useDepth) {
			GlStateManager.clearDepth(1.0);
			i |= 256;
		}

		GlStateManager.clear(i, bl);
		this.unbindWrite();
	}
}
