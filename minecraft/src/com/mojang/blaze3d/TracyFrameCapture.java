package com.mojang.blaze3d;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.jtracy.TracyClient;
import java.nio.ByteBuffer;
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
	private final int pixelbuffer = GlStateManager._glGenBuffers();
	private long fence;
	private boolean inProgress;
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
			GlStateManager._glBindBuffer(35051, this.pixelbuffer);
			GlStateManager._glBufferData(35051, (long)i * (long)j * 4L, 35041);
			GlStateManager._glBindBuffer(35051, 0);
			this.inProgress = false;
		}
	}

	public void capture(RenderTarget renderTarget) {
		if (!this.inProgress && !this.capturedThisFrame) {
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
			GlStateManager._glBindBuffer(35051, this.pixelbuffer);
			GlStateManager._glBindFramebuffer(36008, this.frameBuffer.frameBufferId);
			GlStateManager._readPixels(0, 0, this.width, this.height, 6408, 5121, 0L);
			GlStateManager._glBindFramebuffer(36008, 0);
			GlStateManager._glBindBuffer(35051, 0);
			this.fence = GlStateManager._glFenceSync(37143, 0);
			this.inProgress = true;
			this.lastCaptureDelay = 0;
		}
	}

	public void upload() {
		if (this.inProgress) {
			if (GlStateManager._glClientWaitSync(this.fence, 0, 0) != 37147) {
				GlStateManager._glDeleteSync(this.fence);
				GlStateManager._glBindBuffer(35051, this.pixelbuffer);
				ByteBuffer byteBuffer = GlStateManager._glMapBuffer(35051, 35000);
				if (byteBuffer != null) {
					TracyClient.frameImage(byteBuffer, this.width, this.height, this.lastCaptureDelay, true);
				}

				GlStateManager._glUnmapBuffer(35051);
				GlStateManager._glBindBuffer(35051, 0);
				this.inProgress = false;
			}
		}
	}

	public void endFrame() {
		this.lastCaptureDelay++;
		this.capturedThisFrame = false;
		TracyClient.markFrame();
	}

	public void close() {
		if (this.inProgress) {
			GlStateManager._glDeleteSync(this.fence);
			this.inProgress = false;
		}

		GlStateManager._glDeleteBuffers(this.pixelbuffer);
		this.frameBuffer.destroyBuffers();
	}
}
