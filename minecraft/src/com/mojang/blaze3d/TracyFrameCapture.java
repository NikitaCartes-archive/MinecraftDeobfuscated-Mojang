package com.mojang.blaze3d;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.jtracy.TracyClient;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class TracyFrameCapture implements AutoCloseable {
	private static final int MAX_WIDTH = 320;
	private static final int MAX_HEIGHT = 180;
	private static final int BYTES_PER_PIXEL = 4;
	private int targetWidth;
	private int targetHeight;
	private int width;
	private int height;
	private final RenderTarget frameBuffer = new TextureTarget(320, 180, false);
	private final GpuBuffer pixelbuffer = new GpuBuffer(BufferType.PIXEL_PACK, BufferUsage.STREAM_READ, 0);
	@Nullable
	private GpuFence fence;
	private int lastCaptureDelay;
	private boolean capturedThisFrame;

	private void resize(int i, int j) {
		float f = (float)i / (float)j;
		if (i > 320) {
			i = 320;
			j = (int)(320.0F / f);
		}

		if (j > 180) {
			i = (int)(180.0F * f);
			j = 180;
		}

		i = i / 4 * 4;
		j = j / 4 * 4;
		if (this.width != i || this.height != j) {
			this.width = i;
			this.height = j;
			this.frameBuffer.resize(i, j);
			this.pixelbuffer.resize(i * j * 4);
			if (this.fence != null) {
				this.fence.close();
				this.fence = null;
			}
		}
	}

	public void capture(RenderTarget renderTarget) {
		if (this.fence == null && !this.capturedThisFrame) {
			this.capturedThisFrame = true;
			if (renderTarget.width != this.targetWidth || renderTarget.height != this.targetHeight) {
				this.targetWidth = renderTarget.width;
				this.targetHeight = renderTarget.height;
				this.resize(this.targetWidth, this.targetHeight);
			}

			GlStateManager._glBindFramebuffer(36009, this.frameBuffer.frameBufferId);
			GlStateManager._glBindFramebuffer(36008, renderTarget.frameBufferId);
			GlStateManager._glBlitFrameBuffer(0, 0, renderTarget.width, renderTarget.height, 0, 0, this.width, this.height, 16384, 9729);
			GlStateManager._glBindFramebuffer(36008, 0);
			GlStateManager._glBindFramebuffer(36009, 0);
			this.pixelbuffer.bind();
			GlStateManager._glBindFramebuffer(36008, this.frameBuffer.frameBufferId);
			GlStateManager._readPixels(0, 0, this.width, this.height, 6408, 5121, 0L);
			GlStateManager._glBindFramebuffer(36008, 0);
			this.fence = new GpuFence();
			this.lastCaptureDelay = 0;
		}
	}

	public void upload() {
		if (this.fence != null) {
			if (this.fence.awaitCompletion(0L)) {
				this.fence = null;

				try (GpuBuffer.ReadView readView = this.pixelbuffer.read()) {
					if (readView != null) {
						TracyClient.frameImage(readView.data(), this.width, this.height, this.lastCaptureDelay, true);
					}
				}
			}
		}
	}

	public void endFrame() {
		this.lastCaptureDelay++;
		this.capturedThisFrame = false;
		TracyClient.markFrame();
	}

	public void close() {
		if (this.fence != null) {
			this.fence.close();
			this.fence = null;
		}

		this.pixelbuffer.close();
		this.frameBuffer.destroyBuffers();
	}
}
