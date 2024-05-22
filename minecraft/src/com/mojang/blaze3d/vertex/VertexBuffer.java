package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class VertexBuffer implements AutoCloseable {
	private final VertexBuffer.Usage usage;
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

	public VertexBuffer(VertexBuffer.Usage usage) {
		this.usage = usage;
		RenderSystem.assertOnRenderThread();
		this.vertexBufferId = GlStateManager._glGenBuffers();
		this.indexBufferId = GlStateManager._glGenBuffers();
		this.arrayObjectId = GlStateManager._glGenVertexArrays();
	}

	public void upload(MeshData meshData) {
		MeshData var2 = meshData;

		label40: {
			try {
				if (this.isInvalid()) {
					break label40;
				}

				RenderSystem.assertOnRenderThread();
				MeshData.DrawState drawState = meshData.drawState();
				this.format = this.uploadVertexBuffer(drawState, meshData.vertexBuffer());
				this.sequentialIndices = this.uploadIndexBuffer(drawState, meshData.indexBuffer());
				this.indexCount = drawState.indexCount();
				this.indexType = drawState.indexType();
				this.mode = drawState.mode();
			} catch (Throwable var6) {
				if (meshData != null) {
					try {
						var2.close();
					} catch (Throwable var5) {
						var6.addSuppressed(var5);
					}
				}

				throw var6;
			}

			if (meshData != null) {
				meshData.close();
			}

			return;
		}

		if (meshData != null) {
			meshData.close();
		}
	}

	public void uploadIndexBuffer(ByteBufferBuilder.Result result) {
		ByteBufferBuilder.Result var2 = result;

		label40: {
			try {
				if (this.isInvalid()) {
					break label40;
				}

				RenderSystem.assertOnRenderThread();
				GlStateManager._glBindBuffer(34963, this.indexBufferId);
				RenderSystem.glBufferData(34963, result.byteBuffer(), this.usage.id);
				this.sequentialIndices = null;
			} catch (Throwable var6) {
				if (result != null) {
					try {
						var2.close();
					} catch (Throwable var5) {
						var6.addSuppressed(var5);
					}
				}

				throw var6;
			}

			if (result != null) {
				result.close();
			}

			return;
		}

		if (result != null) {
			result.close();
		}
	}

	private VertexFormat uploadVertexBuffer(MeshData.DrawState drawState, @Nullable ByteBuffer byteBuffer) {
		boolean bl = false;
		if (!drawState.format().equals(this.format)) {
			if (this.format != null) {
				this.format.clearBufferState();
			}

			GlStateManager._glBindBuffer(34962, this.vertexBufferId);
			drawState.format().setupBufferState();
			bl = true;
		}

		if (byteBuffer != null) {
			if (!bl) {
				GlStateManager._glBindBuffer(34962, this.vertexBufferId);
			}

			RenderSystem.glBufferData(34962, byteBuffer, this.usage.id);
		}

		return drawState.format();
	}

	@Nullable
	private RenderSystem.AutoStorageIndexBuffer uploadIndexBuffer(MeshData.DrawState drawState, @Nullable ByteBuffer byteBuffer) {
		if (byteBuffer != null) {
			GlStateManager._glBindBuffer(34963, this.indexBufferId);
			RenderSystem.glBufferData(34963, byteBuffer, this.usage.id);
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
		shaderInstance.setDefaultUniforms(this.mode, matrix4f, matrix4f2, Minecraft.getInstance().getWindow());
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

	@Environment(EnvType.CLIENT)
	public static enum Usage {
		STATIC(35044),
		DYNAMIC(35048);

		final int id;

		private Usage(final int j) {
			this.id = j;
		}
	}
}
