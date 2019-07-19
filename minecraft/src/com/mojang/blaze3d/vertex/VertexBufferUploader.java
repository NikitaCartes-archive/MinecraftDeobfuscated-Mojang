package com.mojang.blaze3d.vertex;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class VertexBufferUploader extends BufferUploader {
	private VertexBuffer buffer;

	@Override
	public void end(BufferBuilder bufferBuilder) {
		bufferBuilder.clear();
		this.buffer.upload(bufferBuilder.getBuffer());
	}

	public void setBuffer(VertexBuffer vertexBuffer) {
		this.buffer = vertexBuffer;
	}
}
