package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CompiledShaderProgram;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class VertexBuffer implements AutoCloseable {
	private final BufferUsage usage;
	private final GpuBuffer vertexBuffer;
	@Nullable
	private GpuBuffer indexBuffer = null;
	private int arrayObjectId;
	@Nullable
	private VertexFormat format;
	@Nullable
	private RenderSystem.AutoStorageIndexBuffer sequentialIndices;
	private VertexFormat.IndexType indexType;
	private int indexCount;
	private VertexFormat.Mode mode;

	public VertexBuffer(BufferUsage bufferUsage) {
		this.usage = bufferUsage;
		RenderSystem.assertOnRenderThread();
		this.vertexBuffer = new GpuBuffer(BufferType.VERTICES, bufferUsage, 0);
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

		label46: {
			try {
				if (this.isInvalid()) {
					break label46;
				}

				RenderSystem.assertOnRenderThread();
				if (this.indexBuffer != null) {
					this.indexBuffer.close();
				}

				this.indexBuffer = new GpuBuffer(BufferType.INDICES, this.usage, result.byteBuffer());
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

			this.vertexBuffer.bind();
			drawState.format().setupBufferState();
			bl = true;
		}

		if (byteBuffer != null) {
			if (!bl) {
				this.vertexBuffer.bind();
			}

			this.vertexBuffer.resize(byteBuffer.remaining());
			this.vertexBuffer.write(byteBuffer, 0);
		}

		return drawState.format();
	}

	@Nullable
	private RenderSystem.AutoStorageIndexBuffer uploadIndexBuffer(MeshData.DrawState drawState, @Nullable ByteBuffer byteBuffer) {
		if (byteBuffer != null) {
			if (this.indexBuffer != null) {
				this.indexBuffer.close();
			}

			this.indexBuffer = new GpuBuffer(BufferType.INDICES, this.usage, byteBuffer);
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

	public void drawWithShader(Matrix4f matrix4f, Matrix4f matrix4f2, @Nullable CompiledShaderProgram compiledShaderProgram) {
		if (compiledShaderProgram != null) {
			RenderSystem.assertOnRenderThread();
			compiledShaderProgram.setDefaultUniforms(this.mode, matrix4f, matrix4f2, Minecraft.getInstance().getWindow());
			compiledShaderProgram.apply();
			this.draw();
			compiledShaderProgram.clear();
		}
	}

	public void close() {
		this.vertexBuffer.close();
		if (this.indexBuffer != null) {
			this.indexBuffer.close();
			this.indexBuffer = null;
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
