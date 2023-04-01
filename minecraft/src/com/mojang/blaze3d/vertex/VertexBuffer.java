package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class VertexBuffer implements AutoCloseable {
	private int vertexBufferId;
	private int indexBufferId;
	private int arrayObjectId;
	@Nullable
	private VertexFormat format;
	@Nullable
	private RenderSystem.AutoStorageIndexBuffer sequentialIndices;
	private VertexFormat.IndexType indexType;
	private int indexCount;
	private VertexFormat.Mode mode;

	public VertexBuffer() {
		RenderSystem.assertOnRenderThread();
		this.vertexBufferId = GlStateManager._glGenBuffers();
		this.indexBufferId = GlStateManager._glGenBuffers();
		this.arrayObjectId = GlStateManager._glGenVertexArrays();
	}

	public void upload(BufferBuilder.RenderedBuffer renderedBuffer) {
		if (!this.isInvalid()) {
			RenderSystem.assertOnRenderThread();

			try {
				BufferBuilder.DrawState drawState = renderedBuffer.drawState();
				this.format = this.uploadVertexBuffer(drawState, renderedBuffer.vertexBuffer());
				this.sequentialIndices = this.uploadIndexBuffer(drawState, renderedBuffer.indexBuffer());
				this.indexCount = drawState.indexCount();
				this.indexType = drawState.indexType();
				this.mode = drawState.mode();
			} finally {
				renderedBuffer.release();
			}
		}
	}

	private VertexFormat uploadVertexBuffer(BufferBuilder.DrawState drawState, ByteBuffer byteBuffer) {
		boolean bl = false;
		if (!drawState.format().equals(this.format)) {
			if (this.format != null) {
				this.format.clearBufferState();
			}

			GlStateManager._glBindBuffer(34962, this.vertexBufferId);
			drawState.format().setupBufferState();
			bl = true;
		}

		if (!drawState.indexOnly()) {
			if (!bl) {
				GlStateManager._glBindBuffer(34962, this.vertexBufferId);
			}

			RenderSystem.glBufferData(34962, byteBuffer, 35044);
		}

		return drawState.format();
	}

	@Nullable
	private RenderSystem.AutoStorageIndexBuffer uploadIndexBuffer(BufferBuilder.DrawState drawState, ByteBuffer byteBuffer) {
		if (!drawState.sequentialIndex()) {
			GlStateManager._glBindBuffer(34963, this.indexBufferId);
			RenderSystem.glBufferData(34963, byteBuffer, 35044);
			return null;
		} else {
			RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(drawState.mode());
			if (autoStorageIndexBuffer != this.sequentialIndices || !autoStorageIndexBuffer.hasStorage(drawState.indexCount())) {
				autoStorageIndexBuffer.bind(drawState.indexCount());
			}

			return autoStorageIndexBuffer;
		}
	}

	public void bind() {
		BufferUploader.invalidate();
		GlStateManager._glBindVertexArray(this.arrayObjectId);
	}

	public static void unbind() {
		BufferUploader.invalidate();
		GlStateManager._glBindVertexArray(0);
	}

	public void draw() {
		RenderSystem.drawElements(this.mode.asGLMode, this.indexCount, this.getIndexType().asGLType);
	}

	private VertexFormat.IndexType getIndexType() {
		RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = this.sequentialIndices;
		return autoStorageIndexBuffer != null ? autoStorageIndexBuffer.type() : this.indexType;
	}

	public void drawWithShader(Matrix4f matrix4f, Matrix4f matrix4f2, ShaderInstance shaderInstance) {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> this._drawWithShader(new Matrix4f(matrix4f), new Matrix4f(matrix4f2), shaderInstance));
		} else {
			this._drawWithShader(matrix4f, matrix4f2, shaderInstance);
		}
	}

	private void _drawWithShader(Matrix4f matrix4f, Matrix4f matrix4f2, ShaderInstance shaderInstance) {
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

		if (shaderInstance.GLINT_ALPHA != null) {
			shaderInstance.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());
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

		if (shaderInstance.WALL_TIME != null) {
			shaderInstance.WALL_TIME.set((float)Util.getMillis());
		}

		if (shaderInstance.SCREEN_SIZE != null) {
			Window window = Minecraft.getInstance().getWindow();
			shaderInstance.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
		}

		if (shaderInstance.LINE_WIDTH != null && (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP)) {
			shaderInstance.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
		}

		RenderSystem.setupShaderLights(shaderInstance);
		shaderInstance.apply();
		this.draw();
		shaderInstance.clear();
	}

	public void close() {
		if (this.vertexBufferId >= 0) {
			RenderSystem.glDeleteBuffers(this.vertexBufferId);
			this.vertexBufferId = -1;
		}

		if (this.indexBufferId >= 0) {
			RenderSystem.glDeleteBuffers(this.indexBufferId);
			this.indexBufferId = -1;
		}

		if (this.arrayObjectId >= 0) {
			RenderSystem.glDeleteVertexArrays(this.arrayObjectId);
			this.arrayObjectId = -1;
		}
	}

	public VertexFormat getFormat() {
		return this.format;
	}

	public boolean isInvalid() {
		return this.arrayObjectId == -1;
	}
}
