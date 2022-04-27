package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BufferUploader {
	@Nullable
	private static VertexBuffer lastImmediateBuffer;

	public static void reset() {
		if (lastImmediateBuffer != null) {
			invalidate();
			VertexBuffer.unbind();
		}
	}

	public static void invalidate() {
		lastImmediateBuffer = null;
	}

	public static void drawWithShader(BufferBuilder.RenderedBuffer renderedBuffer) {
		if (!RenderSystem.isOnRenderThreadOrInit()) {
			RenderSystem.recordRenderCall(() -> _drawWithShader(renderedBuffer));
		} else {
			_drawWithShader(renderedBuffer);
		}
	}

	private static void _drawWithShader(BufferBuilder.RenderedBuffer renderedBuffer) {
		VertexBuffer vertexBuffer = upload(renderedBuffer);
		if (vertexBuffer != null) {
			vertexBuffer.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
		}
	}

	public static void draw(BufferBuilder.RenderedBuffer renderedBuffer) {
		VertexBuffer vertexBuffer = upload(renderedBuffer);
		if (vertexBuffer != null) {
			vertexBuffer.draw();
		}
	}

	@Nullable
	private static VertexBuffer upload(BufferBuilder.RenderedBuffer renderedBuffer) {
		RenderSystem.assertOnRenderThread();
		if (renderedBuffer.isEmpty()) {
			renderedBuffer.release();
			return null;
		} else {
			VertexBuffer vertexBuffer = bindImmediateBuffer(renderedBuffer.drawState().format());
			vertexBuffer.upload(renderedBuffer);
			return vertexBuffer;
		}
	}

	private static VertexBuffer bindImmediateBuffer(VertexFormat vertexFormat) {
		VertexBuffer vertexBuffer = vertexFormat.getImmediateDrawVertexBuffer();
		bindImmediateBuffer(vertexBuffer);
		return vertexBuffer;
	}

	private static void bindImmediateBuffer(VertexBuffer vertexBuffer) {
		if (vertexBuffer != lastImmediateBuffer) {
			vertexBuffer.bind();
			lastImmediateBuffer = vertexBuffer;
		}
	}
}
