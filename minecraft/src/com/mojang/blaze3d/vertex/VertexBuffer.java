package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class VertexBuffer implements AutoCloseable {
	private int id;
	private int indexBufferId;
	private VertexFormat.IndexType indexType;
	private int indexCount;
	private VertexFormat.Mode mode;
	private boolean sequentialIndices;

	public VertexBuffer() {
		RenderSystem.glGenBuffers(integer -> this.id = integer);
		RenderSystem.glGenBuffers(integer -> this.indexBufferId = integer);
	}

	public void bind() {
		RenderSystem.glBindBuffer(34962, () -> this.id);
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
		if (this.id != -1) {
			BufferBuilder.DrawState drawState = pair.getFirst();
			ByteBuffer byteBuffer = pair.getSecond();
			int i = drawState.vertexBufferSize();
			this.indexCount = drawState.indexCount();
			this.indexType = drawState.indexType();
			this.mode = drawState.mode();
			this.sequentialIndices = drawState.sequentialIndex();
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
		}
	}

	public void draw(Matrix4f matrix4f) {
		if (this.indexCount != 0) {
			RenderSystem.pushMatrix();
			RenderSystem.loadIdentity();
			RenderSystem.multMatrix(matrix4f);
			RenderSystem.drawElements(this.mode.asGLMode, this.indexCount, this.indexType.asGLType);
			RenderSystem.popMatrix();
		}
	}

	public static void unbind() {
		RenderSystem.glBindBuffer(34962, () -> 0);
		RenderSystem.glBindBuffer(34963, () -> 0);
	}

	public void close() {
		if (this.id >= 0) {
			RenderSystem.glDeleteBuffers(this.id);
			this.id = -1;
		}

		if (this.indexBufferId >= 0) {
			RenderSystem.glDeleteBuffers(this.indexBufferId);
			this.indexBufferId = -1;
		}
	}
}
