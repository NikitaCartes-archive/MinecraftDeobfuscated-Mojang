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

	public static void drawWithShader(MeshData meshData) {
		RenderSystem.assertOnRenderThread();
		VertexBuffer vertexBuffer = upload(meshData);
		vertexBuffer.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
	}

	public static void draw(MeshData meshData) {
		RenderSystem.assertOnRenderThread();
		VertexBuffer vertexBuffer = upload(meshData);
		vertexBuffer.draw();
	}

	private static VertexBuffer upload(MeshData meshData) {
		VertexBuffer vertexBuffer = bindImmediateBuffer(meshData.drawState().format());
		vertexBuffer.upload(meshData);
		return vertexBuffer;
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
