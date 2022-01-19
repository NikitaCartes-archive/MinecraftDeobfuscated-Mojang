package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

@Environment(EnvType.CLIENT)
public class VertexBuffer implements AutoCloseable {
	private int vertextBufferId;
	private int indexBufferId;
	private VertexFormat.IndexType indexType;
	private int arrayObjectId;
	private int indexCount;
	private VertexFormat.Mode mode;
	private boolean sequentialIndices;
	private VertexFormat format;

	public VertexBuffer() {
		RenderSystem.glGenBuffers(integer -> this.vertextBufferId = integer);
		RenderSystem.glGenVertexArrays(integer -> this.arrayObjectId = integer);
		RenderSystem.glGenBuffers(integer -> this.indexBufferId = integer);
	}

	public void bind() {
		RenderSystem.glBindBuffer(34962, () -> this.vertextBufferId);
		if (this.sequentialIndices) {
			RenderSystem.glBindBuffer(34963, () -> {
				RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(this.mode, this.indexCount);
				this.indexType = autoStorageIndexBuffer.type();
				return autoStorageIndexBuffer.name();
			});
		} else {
			RenderSystem.glBindBuffer(34963, () -> this.indexBufferId);
		}
	}

	public void upload(BufferBuilder bufferBuilder) {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> this.upload_(bufferBuilder));
		} else {
			this.upload_(bufferBuilder);
		}
	}

	public CompletableFuture<Void> uploadLater(BufferBuilder bufferBuilder) {
		if (!RenderSystem.isOnRenderThread()) {
			return CompletableFuture.runAsync(() -> this.upload_(bufferBuilder), runnable -> RenderSystem.recordRenderCall(runnable::run));
		} else {
			this.upload_(bufferBuilder);
			return CompletableFuture.completedFuture(null);
		}
	}

	private void upload_(BufferBuilder bufferBuilder) {
		Pair<BufferBuilder.DrawState, ByteBuffer> pair = bufferBuilder.popNextBuffer();
		if (this.vertextBufferId != 0) {
			BufferUploader.reset();
			BufferBuilder.DrawState drawState = pair.getFirst();
			ByteBuffer byteBuffer = pair.getSecond();
			int i = drawState.vertexBufferSize();
			this.indexCount = drawState.indexCount();
			this.indexType = drawState.indexType();
			this.format = drawState.format();
			this.mode = drawState.mode();
			this.sequentialIndices = drawState.sequentialIndex();
			this.bindVertexArray();
			this.bind();
			if (!drawState.indexOnly()) {
				byteBuffer.limit(i);
				RenderSystem.glBufferData(34962, byteBuffer, 35044);
				byteBuffer.position(i);
			}

			if (!this.sequentialIndices) {
				byteBuffer.limit(drawState.bufferSize());
				RenderSystem.glBufferData(34963, byteBuffer, 35044);
				byteBuffer.position(0);
			} else {
				byteBuffer.limit(drawState.bufferSize());
				byteBuffer.position(0);
			}

			unbind();
			unbindVertexArray();
		}
	}

	private void bindVertexArray() {
		RenderSystem.glBindVertexArray(() -> this.arrayObjectId);
	}

	public static void unbindVertexArray() {
		RenderSystem.glBindVertexArray(() -> 0);
	}

	public void draw() {
		if (this.indexCount != 0) {
			RenderSystem.drawElements(this.mode.asGLMode, this.indexCount, this.indexType.asGLType);
		}
	}

	public void drawWithShader(Matrix4f matrix4f, Matrix4f matrix4f2, ShaderInstance shaderInstance) {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> this._drawWithShader(matrix4f.copy(), matrix4f2.copy(), shaderInstance));
		} else {
			this._drawWithShader(matrix4f, matrix4f2, shaderInstance);
		}
	}

	public void _drawWithShader(Matrix4f matrix4f, Matrix4f matrix4f2, ShaderInstance shaderInstance) {
		if (this.indexCount != 0) {
			RenderSystem.assertOnRenderThread();
			BufferUploader.reset();

			for (int i = 0; i < 12; i++) {
				int j = RenderSystem.getShaderTexture(i);
				shaderInstance.setSampler("Sampler" + i, j);
			}

			if (shaderInstance.MODEL_VIEW_MATRIX != null) {
				shaderInstance.MODEL_VIEW_MATRIX.set(matrix4f);
			}

			if (shaderInstance.PROJECTION_MATRIX != null) {
				shaderInstance.PROJECTION_MATRIX.set(matrix4f2);
			}

			if (shaderInstance.INVERSE_VIEW_ROTATION_MATRIX != null) {
				shaderInstance.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
			}

			if (shaderInstance.COLOR_MODULATOR != null) {
				shaderInstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
			}

			if (shaderInstance.FOG_START != null) {
				shaderInstance.FOG_START.set(RenderSystem.getShaderFogStart());
			}

			if (shaderInstance.FOG_END != null) {
				shaderInstance.FOG_END.set(RenderSystem.getShaderFogEnd());
			}

			if (shaderInstance.FOG_COLOR != null) {
				shaderInstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
			}

			if (shaderInstance.FOG_SHAPE != null) {
				shaderInstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
			}

			if (shaderInstance.TEXTURE_MATRIX != null) {
				shaderInstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
			}

			if (shaderInstance.GAME_TIME != null) {
				shaderInstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
			}

			if (shaderInstance.SCREEN_SIZE != null) {
				Window window = Minecraft.getInstance().getWindow();
				shaderInstance.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
			}

			if (shaderInstance.LINE_WIDTH != null && (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP)) {
				shaderInstance.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
			}

			RenderSystem.setupShaderLights(shaderInstance);
			this.bindVertexArray();
			this.bind();
			this.getFormat().setupBufferState();
			shaderInstance.apply();
			RenderSystem.drawElements(this.mode.asGLMode, this.indexCount, this.indexType.asGLType);
			shaderInstance.clear();
			this.getFormat().clearBufferState();
			unbind();
			unbindVertexArray();
		}
	}

	public void drawChunkLayer() {
		if (this.indexCount != 0) {
			RenderSystem.assertOnRenderThread();
			this.bindVertexArray();
			this.bind();
			this.format.setupBufferState();
			RenderSystem.drawElements(this.mode.asGLMode, this.indexCount, this.indexType.asGLType);
		}
	}

	public static void unbind() {
		RenderSystem.glBindBuffer(34962, () -> 0);
		RenderSystem.glBindBuffer(34963, () -> 0);
	}

	public void close() {
		if (this.indexBufferId >= 0) {
			RenderSystem.glDeleteBuffers(this.indexBufferId);
			this.indexBufferId = -1;
		}

		if (this.vertextBufferId > 0) {
			RenderSystem.glDeleteBuffers(this.vertextBufferId);
			this.vertextBufferId = 0;
		}

		if (this.arrayObjectId > 0) {
			RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
			this.arrayObjectId = 0;
		}
	}

	public VertexFormat getFormat() {
		return this.format;
	}
}
