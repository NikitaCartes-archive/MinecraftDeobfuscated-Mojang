package com.mojang.blaze3d.systems;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.TracyFrameCapture;
import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.ShaderProgram;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeSource;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
@DontObfuscate
public class RenderSystem {
	static final Logger LOGGER = LogUtils.getLogger();
	private static final ConcurrentLinkedQueue<RenderCall> recordingQueue = Queues.newConcurrentLinkedQueue();
	private static final Tesselator RENDER_THREAD_TESSELATOR = new Tesselator(1536);
	private static final int MINIMUM_ATLAS_TEXTURE_SIZE = 1024;
	@Nullable
	private static Thread renderThread;
	private static int MAX_SUPPORTED_TEXTURE_SIZE = -1;
	private static boolean isInInit;
	private static double lastDrawTime = Double.MIN_VALUE;
	private static final RenderSystem.AutoStorageIndexBuffer sharedSequential = new RenderSystem.AutoStorageIndexBuffer(1, 1, IntConsumer::accept);
	private static final RenderSystem.AutoStorageIndexBuffer sharedSequentialQuad = new RenderSystem.AutoStorageIndexBuffer(4, 6, (intConsumer, i) -> {
		intConsumer.accept(i + 0);
		intConsumer.accept(i + 1);
		intConsumer.accept(i + 2);
		intConsumer.accept(i + 2);
		intConsumer.accept(i + 3);
		intConsumer.accept(i + 0);
	});
	private static final RenderSystem.AutoStorageIndexBuffer sharedSequentialLines = new RenderSystem.AutoStorageIndexBuffer(4, 6, (intConsumer, i) -> {
		intConsumer.accept(i + 0);
		intConsumer.accept(i + 1);
		intConsumer.accept(i + 2);
		intConsumer.accept(i + 3);
		intConsumer.accept(i + 2);
		intConsumer.accept(i + 1);
	});
	private static Matrix4f projectionMatrix = new Matrix4f();
	private static Matrix4f savedProjectionMatrix = new Matrix4f();
	private static ProjectionType projectionType = ProjectionType.PERSPECTIVE;
	private static ProjectionType savedProjectionType = ProjectionType.PERSPECTIVE;
	private static final Matrix4fStack modelViewStack = new Matrix4fStack(16);
	private static Matrix4f textureMatrix = new Matrix4f();
	private static final int[] shaderTextures = new int[12];
	private static final float[] shaderColor = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
	private static float shaderGlintAlpha = 1.0F;
	private static FogParameters shaderFog = FogParameters.NO_FOG;
	private static final Vector3f[] shaderLightDirections = new Vector3f[2];
	private static float shaderGameTime;
	private static float shaderLineWidth = 1.0F;
	private static String apiDescription = "Unknown";
	@Nullable
	private static CompiledShaderProgram shader;
	private static final AtomicLong pollEventsWaitStart = new AtomicLong();
	private static final AtomicBoolean pollingEvents = new AtomicBoolean(false);

	public static void initRenderThread() {
		if (renderThread != null) {
			throw new IllegalStateException("Could not initialize render thread");
		} else {
			renderThread = Thread.currentThread();
		}
	}

	public static boolean isOnRenderThread() {
		return Thread.currentThread() == renderThread;
	}

	public static boolean isOnRenderThreadOrInit() {
		return isInInit || isOnRenderThread();
	}

	public static void assertOnRenderThreadOrInit() {
		if (!isInInit && !isOnRenderThread()) {
			throw constructThreadException();
		}
	}

	public static void assertOnRenderThread() {
		if (!isOnRenderThread()) {
			throw constructThreadException();
		}
	}

	private static IllegalStateException constructThreadException() {
		return new IllegalStateException("Rendersystem called from wrong thread");
	}

	public static void recordRenderCall(RenderCall renderCall) {
		recordingQueue.add(renderCall);
	}

	private static void pollEvents() {
		pollEventsWaitStart.set(Util.getMillis());
		pollingEvents.set(true);
		GLFW.glfwPollEvents();
		pollingEvents.set(false);
	}

	public static boolean isFrozenAtPollEvents() {
		return pollingEvents.get() && Util.getMillis() - pollEventsWaitStart.get() > 200L;
	}

	public static void flipFrame(long l, @Nullable TracyFrameCapture tracyFrameCapture) {
		pollEvents();
		replayQueue();
		Tesselator.getInstance().clear();
		GLFW.glfwSwapBuffers(l);
		if (tracyFrameCapture != null) {
			tracyFrameCapture.endFrame();
		}

		pollEvents();
	}

	public static void replayQueue() {
		while (!recordingQueue.isEmpty()) {
			RenderCall renderCall = (RenderCall)recordingQueue.poll();
			renderCall.execute();
		}
	}

	public static void limitDisplayFPS(int i) {
		double d = lastDrawTime + 1.0 / (double)i;

		double e;
		for (e = GLFW.glfwGetTime(); e < d; e = GLFW.glfwGetTime()) {
			GLFW.glfwWaitEventsTimeout(d - e);
		}

		lastDrawTime = e;
	}

	public static void disableDepthTest() {
		assertOnRenderThread();
		GlStateManager._disableDepthTest();
	}

	public static void enableDepthTest() {
		GlStateManager._enableDepthTest();
	}

	public static void enableScissor(int i, int j, int k, int l) {
		GlStateManager._enableScissorTest();
		GlStateManager._scissorBox(i, j, k, l);
	}

	public static void disableScissor() {
		GlStateManager._disableScissorTest();
	}

	public static void depthFunc(int i) {
		assertOnRenderThread();
		GlStateManager._depthFunc(i);
	}

	public static void depthMask(boolean bl) {
		assertOnRenderThread();
		GlStateManager._depthMask(bl);
	}

	public static void enableBlend() {
		assertOnRenderThread();
		GlStateManager._enableBlend();
	}

	public static void disableBlend() {
		assertOnRenderThread();
		GlStateManager._disableBlend();
	}

	public static void blendFunc(GlStateManager.SourceFactor sourceFactor, GlStateManager.DestFactor destFactor) {
		assertOnRenderThread();
		GlStateManager._blendFunc(sourceFactor.value, destFactor.value);
	}

	public static void blendFunc(int i, int j) {
		assertOnRenderThread();
		GlStateManager._blendFunc(i, j);
	}

	public static void blendFuncSeparate(
		GlStateManager.SourceFactor sourceFactor,
		GlStateManager.DestFactor destFactor,
		GlStateManager.SourceFactor sourceFactor2,
		GlStateManager.DestFactor destFactor2
	) {
		assertOnRenderThread();
		GlStateManager._blendFuncSeparate(sourceFactor.value, destFactor.value, sourceFactor2.value, destFactor2.value);
	}

	public static void blendFuncSeparate(int i, int j, int k, int l) {
		assertOnRenderThread();
		GlStateManager._blendFuncSeparate(i, j, k, l);
	}

	public static void blendEquation(int i) {
		assertOnRenderThread();
		GlStateManager._blendEquation(i);
	}

	public static void enableCull() {
		assertOnRenderThread();
		GlStateManager._enableCull();
	}

	public static void disableCull() {
		assertOnRenderThread();
		GlStateManager._disableCull();
	}

	public static void polygonMode(int i, int j) {
		assertOnRenderThread();
		GlStateManager._polygonMode(i, j);
	}

	public static void enablePolygonOffset() {
		assertOnRenderThread();
		GlStateManager._enablePolygonOffset();
	}

	public static void disablePolygonOffset() {
		assertOnRenderThread();
		GlStateManager._disablePolygonOffset();
	}

	public static void polygonOffset(float f, float g) {
		assertOnRenderThread();
		GlStateManager._polygonOffset(f, g);
	}

	public static void enableColorLogicOp() {
		assertOnRenderThread();
		GlStateManager._enableColorLogicOp();
	}

	public static void disableColorLogicOp() {
		assertOnRenderThread();
		GlStateManager._disableColorLogicOp();
	}

	public static void logicOp(GlStateManager.LogicOp logicOp) {
		assertOnRenderThread();
		GlStateManager._logicOp(logicOp.value);
	}

	public static void activeTexture(int i) {
		assertOnRenderThread();
		GlStateManager._activeTexture(i);
	}

	public static void texParameter(int i, int j, int k) {
		GlStateManager._texParameter(i, j, k);
	}

	public static void deleteTexture(int i) {
		GlStateManager._deleteTexture(i);
	}

	public static void bindTextureForSetup(int i) {
		bindTexture(i);
	}

	public static void bindTexture(int i) {
		GlStateManager._bindTexture(i);
	}

	public static void viewport(int i, int j, int k, int l) {
		GlStateManager._viewport(i, j, k, l);
	}

	public static void colorMask(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
		assertOnRenderThread();
		GlStateManager._colorMask(bl, bl2, bl3, bl4);
	}

	public static void stencilFunc(int i, int j, int k) {
		assertOnRenderThread();
		GlStateManager._stencilFunc(i, j, k);
	}

	public static void stencilMask(int i) {
		assertOnRenderThread();
		GlStateManager._stencilMask(i);
	}

	public static void stencilOp(int i, int j, int k) {
		assertOnRenderThread();
		GlStateManager._stencilOp(i, j, k);
	}

	public static void clearDepth(double d) {
		GlStateManager._clearDepth(d);
	}

	public static void clearColor(float f, float g, float h, float i) {
		GlStateManager._clearColor(f, g, h, i);
	}

	public static void clearStencil(int i) {
		assertOnRenderThread();
		GlStateManager._clearStencil(i);
	}

	public static void clear(int i) {
		GlStateManager._clear(i);
	}

	public static void setShaderFog(FogParameters fogParameters) {
		assertOnRenderThread();
		shaderFog = fogParameters;
	}

	public static FogParameters getShaderFog() {
		assertOnRenderThread();
		return shaderFog;
	}

	public static void setShaderGlintAlpha(double d) {
		setShaderGlintAlpha((float)d);
	}

	public static void setShaderGlintAlpha(float f) {
		assertOnRenderThread();
		shaderGlintAlpha = f;
	}

	public static float getShaderGlintAlpha() {
		assertOnRenderThread();
		return shaderGlintAlpha;
	}

	public static void setShaderLights(Vector3f vector3f, Vector3f vector3f2) {
		assertOnRenderThread();
		shaderLightDirections[0] = vector3f;
		shaderLightDirections[1] = vector3f2;
	}

	public static void setupShaderLights(CompiledShaderProgram compiledShaderProgram) {
		assertOnRenderThread();
		if (compiledShaderProgram.LIGHT0_DIRECTION != null) {
			compiledShaderProgram.LIGHT0_DIRECTION.set(shaderLightDirections[0]);
		}

		if (compiledShaderProgram.LIGHT1_DIRECTION != null) {
			compiledShaderProgram.LIGHT1_DIRECTION.set(shaderLightDirections[1]);
		}
	}

	public static void setShaderColor(float f, float g, float h, float i) {
		assertOnRenderThread();
		shaderColor[0] = f;
		shaderColor[1] = g;
		shaderColor[2] = h;
		shaderColor[3] = i;
	}

	public static float[] getShaderColor() {
		assertOnRenderThread();
		return shaderColor;
	}

	public static void drawElements(int i, int j, int k) {
		assertOnRenderThread();
		GlStateManager._drawElements(i, j, k, 0L);
	}

	public static void lineWidth(float f) {
		assertOnRenderThread();
		shaderLineWidth = f;
	}

	public static float getShaderLineWidth() {
		assertOnRenderThread();
		return shaderLineWidth;
	}

	public static void pixelStore(int i, int j) {
		GlStateManager._pixelStore(i, j);
	}

	public static void readPixels(int i, int j, int k, int l, int m, int n, ByteBuffer byteBuffer) {
		assertOnRenderThread();
		GlStateManager._readPixels(i, j, k, l, m, n, byteBuffer);
	}

	public static void getString(int i, Consumer<String> consumer) {
		assertOnRenderThread();
		consumer.accept(GlStateManager._getString(i));
	}

	public static String getBackendDescription() {
		return String.format(Locale.ROOT, "LWJGL version %s", GLX._getLWJGLVersion());
	}

	public static String getApiDescription() {
		return apiDescription;
	}

	public static TimeSource.NanoTimeSource initBackendSystem() {
		return GLX._initGlfw()::getAsLong;
	}

	public static void initRenderer(int i, boolean bl) {
		GLX._init(i, bl);
		apiDescription = GLX.getOpenGLVersionString();
	}

	public static void setErrorCallback(GLFWErrorCallbackI gLFWErrorCallbackI) {
		GLX._setGlfwErrorCallback(gLFWErrorCallbackI);
	}

	public static void renderCrosshair(int i) {
		assertOnRenderThread();
		GLX._renderCrosshair(i, true, true, true);
	}

	public static String getCapsString() {
		assertOnRenderThread();
		return "Using framebuffer using OpenGL 3.2";
	}

	public static void setupDefaultState(int i, int j, int k, int l) {
		GlStateManager._clearDepth(1.0);
		GlStateManager._enableDepthTest();
		GlStateManager._depthFunc(515);
		projectionMatrix.identity();
		savedProjectionMatrix.identity();
		modelViewStack.clear();
		textureMatrix.identity();
		GlStateManager._viewport(i, j, k, l);
	}

	public static int maxSupportedTextureSize() {
		if (MAX_SUPPORTED_TEXTURE_SIZE == -1) {
			assertOnRenderThreadOrInit();
			int i = GlStateManager._getInteger(3379);

			for (int j = Math.max(32768, i); j >= 1024; j >>= 1) {
				GlStateManager._texImage2D(32868, 0, 6408, j, j, 0, 6408, 5121, null);
				int k = GlStateManager._getTexLevelParameter(32868, 0, 4096);
				if (k != 0) {
					MAX_SUPPORTED_TEXTURE_SIZE = j;
					return j;
				}
			}

			MAX_SUPPORTED_TEXTURE_SIZE = Math.max(i, 1024);
			LOGGER.info("Failed to determine maximum texture size by probing, trying GL_MAX_TEXTURE_SIZE = {}", MAX_SUPPORTED_TEXTURE_SIZE);
		}

		return MAX_SUPPORTED_TEXTURE_SIZE;
	}

	public static void glBindBuffer(int i, int j) {
		GlStateManager._glBindBuffer(i, j);
	}

	public static void glBindVertexArray(int i) {
		GlStateManager._glBindVertexArray(i);
	}

	public static void glBufferData(int i, ByteBuffer byteBuffer, int j) {
		assertOnRenderThreadOrInit();
		GlStateManager._glBufferData(i, byteBuffer, j);
	}

	public static void glDeleteBuffers(int i) {
		assertOnRenderThread();
		GlStateManager._glDeleteBuffers(i);
	}

	public static void glDeleteVertexArrays(int i) {
		assertOnRenderThread();
		GlStateManager._glDeleteVertexArrays(i);
	}

	public static void glUniform1i(int i, int j) {
		assertOnRenderThread();
		GlStateManager._glUniform1i(i, j);
	}

	public static void glUniform1(int i, IntBuffer intBuffer) {
		assertOnRenderThread();
		GlStateManager._glUniform1(i, intBuffer);
	}

	public static void glUniform2(int i, IntBuffer intBuffer) {
		assertOnRenderThread();
		GlStateManager._glUniform2(i, intBuffer);
	}

	public static void glUniform3(int i, IntBuffer intBuffer) {
		assertOnRenderThread();
		GlStateManager._glUniform3(i, intBuffer);
	}

	public static void glUniform4(int i, IntBuffer intBuffer) {
		assertOnRenderThread();
		GlStateManager._glUniform4(i, intBuffer);
	}

	public static void glUniform1(int i, FloatBuffer floatBuffer) {
		assertOnRenderThread();
		GlStateManager._glUniform1(i, floatBuffer);
	}

	public static void glUniform2(int i, FloatBuffer floatBuffer) {
		assertOnRenderThread();
		GlStateManager._glUniform2(i, floatBuffer);
	}

	public static void glUniform3(int i, FloatBuffer floatBuffer) {
		assertOnRenderThread();
		GlStateManager._glUniform3(i, floatBuffer);
	}

	public static void glUniform4(int i, FloatBuffer floatBuffer) {
		assertOnRenderThread();
		GlStateManager._glUniform4(i, floatBuffer);
	}

	public static void glUniformMatrix2(int i, boolean bl, FloatBuffer floatBuffer) {
		assertOnRenderThread();
		GlStateManager._glUniformMatrix2(i, bl, floatBuffer);
	}

	public static void glUniformMatrix3(int i, boolean bl, FloatBuffer floatBuffer) {
		assertOnRenderThread();
		GlStateManager._glUniformMatrix3(i, bl, floatBuffer);
	}

	public static void glUniformMatrix4(int i, boolean bl, FloatBuffer floatBuffer) {
		assertOnRenderThread();
		GlStateManager._glUniformMatrix4(i, bl, floatBuffer);
	}

	public static void setupOverlayColor(int i, int j) {
		assertOnRenderThread();
		setShaderTexture(1, i);
	}

	public static void teardownOverlayColor() {
		assertOnRenderThread();
		setShaderTexture(1, 0);
	}

	public static void setupLevelDiffuseLighting(Vector3f vector3f, Vector3f vector3f2) {
		assertOnRenderThread();
		setShaderLights(vector3f, vector3f2);
	}

	public static void setupGuiFlatDiffuseLighting(Vector3f vector3f, Vector3f vector3f2) {
		assertOnRenderThread();
		GlStateManager.setupGuiFlatDiffuseLighting(vector3f, vector3f2);
	}

	public static void setupGui3DDiffuseLighting(Vector3f vector3f, Vector3f vector3f2) {
		assertOnRenderThread();
		GlStateManager.setupGui3DDiffuseLighting(vector3f, vector3f2);
	}

	public static void beginInitialization() {
		isInInit = true;
	}

	public static void finishInitialization() {
		isInInit = false;
		if (!recordingQueue.isEmpty()) {
			replayQueue();
		}

		if (!recordingQueue.isEmpty()) {
			throw new IllegalStateException("Recorded to render queue during initialization");
		}
	}

	public static Tesselator renderThreadTesselator() {
		assertOnRenderThread();
		return RENDER_THREAD_TESSELATOR;
	}

	public static void defaultBlendFunc() {
		blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
	}

	public static void overlayBlendFunc() {
		blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
	}

	@Nullable
	public static CompiledShaderProgram setShader(ShaderProgram shaderProgram) {
		assertOnRenderThread();
		CompiledShaderProgram compiledShaderProgram = Minecraft.getInstance().getShaderManager().getProgram(shaderProgram);
		shader = compiledShaderProgram;
		return compiledShaderProgram;
	}

	public static void setShader(CompiledShaderProgram compiledShaderProgram) {
		assertOnRenderThread();
		shader = compiledShaderProgram;
	}

	public static void clearShader() {
		assertOnRenderThread();
		shader = null;
	}

	@Nullable
	public static CompiledShaderProgram getShader() {
		assertOnRenderThread();
		return shader;
	}

	public static void setShaderTexture(int i, ResourceLocation resourceLocation) {
		assertOnRenderThread();
		if (i >= 0 && i < shaderTextures.length) {
			TextureManager textureManager = Minecraft.getInstance().getTextureManager();
			AbstractTexture abstractTexture = textureManager.getTexture(resourceLocation);
			shaderTextures[i] = abstractTexture.getId();
		}
	}

	public static void setShaderTexture(int i, int j) {
		assertOnRenderThread();
		if (i >= 0 && i < shaderTextures.length) {
			shaderTextures[i] = j;
		}
	}

	public static int getShaderTexture(int i) {
		assertOnRenderThread();
		return i >= 0 && i < shaderTextures.length ? shaderTextures[i] : 0;
	}

	public static void setProjectionMatrix(Matrix4f matrix4f, ProjectionType projectionType) {
		assertOnRenderThread();
		projectionMatrix = new Matrix4f(matrix4f);
		RenderSystem.projectionType = projectionType;
	}

	public static void setTextureMatrix(Matrix4f matrix4f) {
		assertOnRenderThread();
		textureMatrix = new Matrix4f(matrix4f);
	}

	public static void resetTextureMatrix() {
		assertOnRenderThread();
		textureMatrix.identity();
	}

	public static void backupProjectionMatrix() {
		assertOnRenderThread();
		savedProjectionMatrix = projectionMatrix;
		savedProjectionType = projectionType;
	}

	public static void restoreProjectionMatrix() {
		assertOnRenderThread();
		projectionMatrix = savedProjectionMatrix;
		projectionType = savedProjectionType;
	}

	public static Matrix4f getProjectionMatrix() {
		assertOnRenderThread();
		return projectionMatrix;
	}

	public static Matrix4f getModelViewMatrix() {
		assertOnRenderThread();
		return modelViewStack;
	}

	public static Matrix4fStack getModelViewStack() {
		assertOnRenderThread();
		return modelViewStack;
	}

	public static Matrix4f getTextureMatrix() {
		assertOnRenderThread();
		return textureMatrix;
	}

	public static RenderSystem.AutoStorageIndexBuffer getSequentialBuffer(VertexFormat.Mode mode) {
		assertOnRenderThread();

		return switch (mode) {
			case QUADS -> sharedSequentialQuad;
			case LINES -> sharedSequentialLines;
			default -> sharedSequential;
		};
	}

	public static void setShaderGameTime(long l, float f) {
		assertOnRenderThread();
		shaderGameTime = ((float)(l % 24000L) + f) / 24000.0F;
	}

	public static float getShaderGameTime() {
		assertOnRenderThread();
		return shaderGameTime;
	}

	public static ProjectionType getProjectionType() {
		assertOnRenderThread();
		return projectionType;
	}

	@Environment(EnvType.CLIENT)
	public static final class AutoStorageIndexBuffer {
		private final int vertexStride;
		private final int indexStride;
		private final RenderSystem.AutoStorageIndexBuffer.IndexGenerator generator;
		@Nullable
		private GpuBuffer buffer;
		private VertexFormat.IndexType type = VertexFormat.IndexType.SHORT;
		private int indexCount;

		AutoStorageIndexBuffer(int i, int j, RenderSystem.AutoStorageIndexBuffer.IndexGenerator indexGenerator) {
			this.vertexStride = i;
			this.indexStride = j;
			this.generator = indexGenerator;
		}

		public boolean hasStorage(int i) {
			return i <= this.indexCount;
		}

		public void bind(int i) {
			if (this.buffer == null) {
				this.buffer = new GpuBuffer(BufferType.INDICES, BufferUsage.DYNAMIC_WRITE, 0);
			}

			this.buffer.bind();
			this.ensureStorage(i);
		}

		private void ensureStorage(int i) {
			if (!this.hasStorage(i)) {
				i = Mth.roundToward(i * 2, this.indexStride);
				RenderSystem.LOGGER.debug("Growing IndexBuffer: Old limit {}, new limit {}.", this.indexCount, i);
				int j = i / this.indexStride;
				int k = j * this.vertexStride;
				VertexFormat.IndexType indexType = VertexFormat.IndexType.least(k);
				int l = Mth.roundToward(i * indexType.bytes, 4);
				ByteBuffer byteBuffer = MemoryUtil.memAlloc(l);

				try {
					this.type = indexType;
					it.unimi.dsi.fastutil.ints.IntConsumer intConsumer = this.intConsumer(byteBuffer);

					for (int m = 0; m < i; m += this.indexStride) {
						this.generator.accept(intConsumer, m * this.vertexStride / this.indexStride);
					}

					byteBuffer.flip();
					this.buffer.resize(l);
					this.buffer.write(byteBuffer, 0);
				} finally {
					MemoryUtil.memFree(byteBuffer);
				}

				this.indexCount = i;
			}
		}

		private it.unimi.dsi.fastutil.ints.IntConsumer intConsumer(ByteBuffer byteBuffer) {
			switch (this.type) {
				case SHORT:
					return i -> byteBuffer.putShort((short)i);
				case INT:
				default:
					return byteBuffer::putInt;
			}
		}

		public VertexFormat.IndexType type() {
			return this.type;
		}

		@Environment(EnvType.CLIENT)
		interface IndexGenerator {
			void accept(it.unimi.dsi.fastutil.ints.IntConsumer intConsumer, int i);
		}
	}
}
