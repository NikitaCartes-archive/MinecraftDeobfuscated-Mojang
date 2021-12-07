package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

@Environment(EnvType.CLIENT)
public class BufferUploader {
	private static int lastVertexArrayObject;
	private static int lastVertexBufferObject;
	private static int lastIndexBufferObject;
	@Nullable
	private static VertexFormat lastFormat;

	public static void reset() {
		if (lastFormat != null) {
			lastFormat.clearBufferState();
			lastFormat = null;
		}

		GlStateManager._glBindBuffer(34963, 0);
		lastIndexBufferObject = 0;
		GlStateManager._glBindBuffer(34962, 0);
		lastVertexBufferObject = 0;
		GlStateManager._glBindVertexArray(0);
		lastVertexArrayObject = 0;
	}

	public static void invalidateElementArrayBufferBinding() {
		GlStateManager._glBindBuffer(34963, 0);
		lastIndexBufferObject = 0;
	}

	public static void end(BufferBuilder bufferBuilder) {
		if (!RenderSystem.isOnRenderThreadOrInit()) {
			RenderSystem.recordRenderCall(
				() -> {
					Pair<BufferBuilder.DrawState, ByteBuffer> pairx = bufferBuilder.popNextBuffer();
					BufferBuilder.DrawState drawStatex = pairx.getFirst();
					_end(
						pairx.getSecond(),
						drawStatex.mode(),
						drawStatex.format(),
						drawStatex.vertexCount(),
						drawStatex.indexType(),
						drawStatex.indexCount(),
						drawStatex.sequentialIndex()
					);
				}
			);
		} else {
			Pair<BufferBuilder.DrawState, ByteBuffer> pair = bufferBuilder.popNextBuffer();
			BufferBuilder.DrawState drawState = pair.getFirst();
			_end(
				pair.getSecond(), drawState.mode(), drawState.format(), drawState.vertexCount(), drawState.indexType(), drawState.indexCount(), drawState.sequentialIndex()
			);
		}
	}

	private static void _end(ByteBuffer byteBuffer, VertexFormat.Mode mode, VertexFormat vertexFormat, int i, VertexFormat.IndexType indexType, int j, boolean bl) {
		RenderSystem.assertOnRenderThread();
		byteBuffer.clear();
		if (i > 0) {
			int k = i * vertexFormat.getVertexSize();
			updateVertexSetup(vertexFormat);
			byteBuffer.position(0);
			byteBuffer.limit(k);
			GlStateManager._glBufferData(34962, byteBuffer, 35048);
			int m;
			if (bl) {
				RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(mode, j);
				int l = autoStorageIndexBuffer.name();
				if (l != lastIndexBufferObject) {
					GlStateManager._glBindBuffer(34963, l);
					lastIndexBufferObject = l;
				}

				m = autoStorageIndexBuffer.type().asGLType;
			} else {
				int n = vertexFormat.getOrCreateIndexBufferObject();
				if (n != lastIndexBufferObject) {
					GlStateManager._glBindBuffer(34963, n);
					lastIndexBufferObject = n;
				}

				byteBuffer.position(k);
				byteBuffer.limit(k + j * indexType.bytes);
				GlStateManager._glBufferData(34963, byteBuffer, 35048);
				m = indexType.asGLType;
			}

			ShaderInstance shaderInstance = RenderSystem.getShader();

			for (int l = 0; l < 8; l++) {
				int o = RenderSystem.getShaderTexture(l);
				shaderInstance.setSampler("Sampler" + l, o);
			}

			if (shaderInstance.MODEL_VIEW_MATRIX != null) {
				shaderInstance.MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
			}

			if (shaderInstance.PROJECTION_MATRIX != null) {
				shaderInstance.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
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

			if (shaderInstance.LINE_WIDTH != null && (mode == VertexFormat.Mode.LINES || mode == VertexFormat.Mode.LINE_STRIP)) {
				shaderInstance.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
			}

			RenderSystem.setupShaderLights(shaderInstance);
			shaderInstance.apply();
			GlStateManager._drawElements(mode.asGLMode, j, m, 0L);
			shaderInstance.clear();
			byteBuffer.position(0);
		}
	}

	public static void _endInternal(BufferBuilder bufferBuilder) {
		RenderSystem.assertOnRenderThread();
		Pair<BufferBuilder.DrawState, ByteBuffer> pair = bufferBuilder.popNextBuffer();
		BufferBuilder.DrawState drawState = pair.getFirst();
		ByteBuffer byteBuffer = pair.getSecond();
		VertexFormat vertexFormat = drawState.format();
		int i = drawState.vertexCount();
		byteBuffer.clear();
		if (i > 0) {
			int j = i * vertexFormat.getVertexSize();
			updateVertexSetup(vertexFormat);
			byteBuffer.position(0);
			byteBuffer.limit(j);
			GlStateManager._glBufferData(34962, byteBuffer, 35048);
			RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(drawState.mode(), drawState.indexCount());
			int k = autoStorageIndexBuffer.name();
			if (k != lastIndexBufferObject) {
				GlStateManager._glBindBuffer(34963, k);
				lastIndexBufferObject = k;
			}

			int l = autoStorageIndexBuffer.type().asGLType;
			GlStateManager._drawElements(drawState.mode().asGLMode, drawState.indexCount(), l, 0L);
			byteBuffer.position(0);
		}
	}

	private static void updateVertexSetup(VertexFormat vertexFormat) {
		int i = vertexFormat.getOrCreateVertexArrayObject();
		int j = vertexFormat.getOrCreateVertexBufferObject();
		boolean bl = vertexFormat != lastFormat;
		if (bl) {
			reset();
		}

		if (i != lastVertexArrayObject) {
			GlStateManager._glBindVertexArray(i);
			lastVertexArrayObject = i;
		}

		if (j != lastVertexBufferObject) {
			GlStateManager._glBindBuffer(34962, j);
			lastVertexBufferObject = j;
		}

		if (bl) {
			vertexFormat.setupBufferState();
			lastFormat = vertexFormat;
		}
	}
}
