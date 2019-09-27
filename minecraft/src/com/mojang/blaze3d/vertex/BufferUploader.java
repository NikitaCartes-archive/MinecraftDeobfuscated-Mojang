package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class BufferUploader {
	public static void end(BufferBuilder bufferBuilder) {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> {
				Pair<BufferBuilder.DrawState, ByteBuffer> pairx = bufferBuilder.popNextBuffer();
				BufferBuilder.DrawState drawStatex = pairx.getFirst();
				_end(pairx.getSecond(), drawStatex.mode(), drawStatex.format(), drawStatex.vertexCount());
			});
		} else {
			Pair<BufferBuilder.DrawState, ByteBuffer> pair = bufferBuilder.popNextBuffer();
			BufferBuilder.DrawState drawState = pair.getFirst();
			_end(pair.getSecond(), drawState.mode(), drawState.format(), drawState.vertexCount());
		}
	}

	private static void _end(ByteBuffer byteBuffer, int i, VertexFormat vertexFormat, int j) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		byteBuffer.clear();
		if (j > 0) {
			vertexFormat.setupBufferState(MemoryUtil.memAddress(byteBuffer));
			GlStateManager._drawArrays(i, 0, j);
			vertexFormat.clearBufferState();
		}
	}
}
