package com.mojang.blaze3d.systems;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;

@Environment(EnvType.CLIENT)
public class RenderSystem {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ConcurrentLinkedQueue<RenderCall> recordingQueue = Queues.newConcurrentLinkedQueue();
	private static final Tesselator RENDER_THREAD_TESSELATOR = new Tesselator();
	private static final int MINIMUM_ATLAS_TEXTURE_SIZE = 1024;
	private static boolean isReplayingQueue;
	@Nullable
	private static Thread gameThread;
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
	private static PoseStack modelViewStack = new PoseStack();
	private static Matrix4f modelViewMatrix = new Matrix4f();
	private static Matrix4f textureMatrix = new Matrix4f();
	private static final int[] shaderTextures = new int[12];
	private static final float[] shaderColor = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
	private static float shaderFogStart;
	private static float shaderFogEnd = 1.0F;
	private static final float[] shaderFogColor = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
	private static final Vector3f[] shaderLightDirections = new Vector3f[2];
	private static float shaderGameTime;
	private static float shaderLineWidth = 1.0F;
	@Nullable
	private static ShaderInstance shader;

	public static void initRenderThread() {
		if (renderThread == null && gameThread != Thread.currentThread()) {
			renderThread = Thread.currentThread();
		} else {
			throw new IllegalStateException("Could not initialize render thread");
		}
	}

	public static boolean isOnRenderThread() {
		return Thread.currentThread() == renderThread;
	}

	public static boolean isOnRenderThreadOrInit() {
		return isInInit || isOnRenderThread();
	}

	public static void initGameThread(boolean bl) {
		boolean bl2 = renderThread == Thread.currentThread();
		if (gameThread == null && renderThread != null && bl2 != bl) {
			gameThread = Thread.currentThread();
		} else {
			throw new IllegalStateException("Could not initialize tick thread");
		}
	}

	public static boolean isOnGameThread() {
		return true;
	}

	public static boolean isOnGameThreadOrInit() {
		return isInInit || isOnGameThread();
	}

	public static void assertThread(Supplier<Boolean> supplier) {
		if (!(Boolean)supplier.get()) {
			throw new IllegalStateException("Rendersystem called from wrong thread");
		}
	}

	public static boolean isInInitPhase() {
		return true;
	}

	public static void recordRenderCall(RenderCall renderCall) {
		recordingQueue.add(renderCall);
	}

	public static void flipFrame(long l) {
		GLFW.glfwPollEvents();
		replayQueue();
		Tesselator.getInstance().getBuilder().clear();
		GLFW.glfwSwapBuffers(l);
		GLFW.glfwPollEvents();
	}

	public static void replayQueue() {
		isReplayingQueue = true;

		while (!recordingQueue.isEmpty()) {
			RenderCall renderCall = (RenderCall)recordingQueue.poll();
			renderCall.execute();
		}

		isReplayingQueue = false;
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
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._disableDepthTest();
	}

	public static void enableDepthTest() {
		assertThread(RenderSystem::isOnGameThreadOrInit);
		GlStateManager._enableDepthTest();
	}

	public static void enableScissor(int i, int j, int k, int l) {
		assertThread(RenderSystem::isOnGameThreadOrInit);
		GlStateManager._enableScissorTest();
		GlStateManager._scissorBox(i, j, k, l);
	}

	public static void disableScissor() {
		assertThread(RenderSystem::isOnGameThreadOrInit);
		GlStateManager._disableScissorTest();
	}

	public static void depthFunc(int i) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._depthFunc(i);
	}

	public static void depthMask(boolean bl) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._depthMask(bl);
	}

	public static void enableBlend() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._enableBlend();
	}

	public static void disableBlend() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._disableBlend();
	}

	public static void blendFunc(GlStateManager.SourceFactor sourceFactor, GlStateManager.DestFactor destFactor) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._blendFunc(sourceFactor.value, destFactor.value);
	}

	public static void blendFunc(int i, int j) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._blendFunc(i, j);
	}

	public static void blendFuncSeparate(
		GlStateManager.SourceFactor sourceFactor,
		GlStateManager.DestFactor destFactor,
		GlStateManager.SourceFactor sourceFactor2,
		GlStateManager.DestFactor destFactor2
	) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._blendFuncSeparate(sourceFactor.value, destFactor.value, sourceFactor2.value, destFactor2.value);
	}

	public static void blendFuncSeparate(int i, int j, int k, int l) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._blendFuncSeparate(i, j, k, l);
	}

	public static void blendEquation(int i) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._blendEquation(i);
	}

	public static void enableCull() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._enableCull();
	}

	public static void disableCull() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._disableCull();
	}

	public static void polygonMode(int i, int j) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._polygonMode(i, j);
	}

	public static void enablePolygonOffset() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._enablePolygonOffset();
	}

	public static void disablePolygonOffset() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._disablePolygonOffset();
	}

	public static void polygonOffset(float f, float g) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._polygonOffset(f, g);
	}

	public static void enableColorLogicOp() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._enableColorLogicOp();
	}

	public static void disableColorLogicOp() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._disableColorLogicOp();
	}

	public static void logicOp(GlStateManager.LogicOp logicOp) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._logicOp(logicOp.value);
	}

	public static void activeTexture(int i) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._activeTexture(i);
	}

	public static void enableTexture() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._enableTexture();
	}

	public static void disableTexture() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._disableTexture();
	}

	public static void texParameter(int i, int j, int k) {
		GlStateManager._texParameter(i, j, k);
	}

	public static void deleteTexture(int i) {
		assertThread(RenderSystem::isOnGameThreadOrInit);
		GlStateManager._deleteTexture(i);
	}

	public static void bindTextureForSetup(int i) {
		bindTexture(i);
	}

	public static void bindTexture(int i) {
		GlStateManager._bindTexture(i);
	}

	public static void viewport(int i, int j, int k, int l) {
		assertThread(RenderSystem::isOnGameThreadOrInit);
		GlStateManager._viewport(i, j, k, l);
	}

	public static void colorMask(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._colorMask(bl, bl2, bl3, bl4);
	}

	public static void stencilFunc(int i, int j, int k) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._stencilFunc(i, j, k);
	}

	public static void stencilMask(int i) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._stencilMask(i);
	}

	public static void stencilOp(int i, int j, int k) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._stencilOp(i, j, k);
	}

	public static void clearDepth(double d) {
		assertThread(RenderSystem::isOnGameThreadOrInit);
		GlStateManager._clearDepth(d);
	}

	public static void clearColor(float f, float g, float h, float i) {
		assertThread(RenderSystem::isOnGameThreadOrInit);
		GlStateManager._clearColor(f, g, h, i);
	}

	public static void clearStencil(int i) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._clearStencil(i);
	}

	public static void clear(int i, boolean bl) {
		assertThread(RenderSystem::isOnGameThreadOrInit);
		GlStateManager._clear(i, bl);
	}

	public static void setShaderFogStart(float f) {
		assertThread(RenderSystem::isOnGameThread);
		_setShaderFogStart(f);
	}

	private static void _setShaderFogStart(float f) {
		shaderFogStart = f;
	}

	public static float getShaderFogStart() {
		assertThread(RenderSystem::isOnRenderThread);
		return shaderFogStart;
	}

	public static void setShaderFogEnd(float f) {
		assertThread(RenderSystem::isOnGameThread);
		_setShaderFogEnd(f);
	}

	private static void _setShaderFogEnd(float f) {
		shaderFogEnd = f;
	}

	public static float getShaderFogEnd() {
		assertThread(RenderSystem::isOnRenderThread);
		return shaderFogEnd;
	}

	public static void setShaderFogColor(float f, float g, float h, float i) {
		assertThread(RenderSystem::isOnGameThread);
		_setShaderFogColor(f, g, h, i);
	}

	public static void setShaderFogColor(float f, float g, float h) {
		setShaderFogColor(f, g, h, 1.0F);
	}

	private static void _setShaderFogColor(float f, float g, float h, float i) {
		shaderFogColor[0] = f;
		shaderFogColor[1] = g;
		shaderFogColor[2] = h;
		shaderFogColor[3] = i;
	}

	public static float[] getShaderFogColor() {
		assertThread(RenderSystem::isOnRenderThread);
		return shaderFogColor;
	}

	public static void setShaderLights(Vector3f vector3f, Vector3f vector3f2) {
		assertThread(RenderSystem::isOnGameThread);
		_setShaderLights(vector3f, vector3f2);
	}

	public static void _setShaderLights(Vector3f vector3f, Vector3f vector3f2) {
		shaderLightDirections[0] = vector3f;
		shaderLightDirections[1] = vector3f2;
	}

	public static void setupShaderLights(ShaderInstance shaderInstance) {
		assertThread(RenderSystem::isOnRenderThread);
		if (shaderInstance.LIGHT0_DIRECTION != null) {
			shaderInstance.LIGHT0_DIRECTION.set(shaderLightDirections[0]);
		}

		if (shaderInstance.LIGHT1_DIRECTION != null) {
			shaderInstance.LIGHT1_DIRECTION.set(shaderLightDirections[1]);
		}
	}

	public static void setShaderColor(float f, float g, float h, float i) {
		if (!isOnRenderThread()) {
			recordRenderCall(() -> _setShaderColor(f, g, h, i));
		} else {
			_setShaderColor(f, g, h, i);
		}
	}

	private static void _setShaderColor(float f, float g, float h, float i) {
		shaderColor[0] = f;
		shaderColor[1] = g;
		shaderColor[2] = h;
		shaderColor[3] = i;
	}

	public static float[] getShaderColor() {
		assertThread(RenderSystem::isOnRenderThread);
		return shaderColor;
	}

	public static void drawElements(int i, int j, int k) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._drawElements(i, j, k, 0L);
	}

	public static void lineWidth(float f) {
		if (!isOnRenderThread()) {
			recordRenderCall(() -> shaderLineWidth = f);
		} else {
			shaderLineWidth = f;
		}
	}

	public static float getShaderLineWidth() {
		assertThread(RenderSystem::isOnRenderThread);
		return shaderLineWidth;
	}

	public static void pixelStore(int i, int j) {
		assertThread(RenderSystem::isOnGameThreadOrInit);
		GlStateManager._pixelStore(i, j);
	}

	public static void readPixels(int i, int j, int k, int l, int m, int n, ByteBuffer byteBuffer) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._readPixels(i, j, k, l, m, n, byteBuffer);
	}

	public static void getString(int i, Consumer<String> consumer) {
		assertThread(RenderSystem::isOnGameThread);
		consumer.accept(GlStateManager._getString(i));
	}

	public static String getBackendDescription() {
		assertThread(RenderSystem::isInInitPhase);
		return String.format("LWJGL version %s", GLX._getLWJGLVersion());
	}

	public static String getApiDescription() {
		assertThread(RenderSystem::isInInitPhase);
		return GLX.getOpenGLVersionString();
	}

	public static LongSupplier initBackendSystem() {
		assertThread(RenderSystem::isInInitPhase);
		return GLX._initGlfw();
	}

	public static void initRenderer(int i, boolean bl) {
		assertThread(RenderSystem::isInInitPhase);
		GLX._init(i, bl);
	}

	public static void setErrorCallback(GLFWErrorCallbackI gLFWErrorCallbackI) {
		assertThread(RenderSystem::isInInitPhase);
		GLX._setGlfwErrorCallback(gLFWErrorCallbackI);
	}

	public static void renderCrosshair(int i) {
		assertThread(RenderSystem::isOnGameThread);
		GLX._renderCrosshair(i, true, true, true);
	}

	public static String getCapsString() {
		assertThread(RenderSystem::isOnGameThread);
		return "Using framebuffer using OpenGL 3.2";
	}

	public static void setupDefaultState(int i, int j, int k, int l) {
		assertThread(RenderSystem::isInInitPhase);
		GlStateManager._enableTexture();
		GlStateManager._clearDepth(1.0);
		GlStateManager._enableDepthTest();
		GlStateManager._depthFunc(515);
		projectionMatrix.setIdentity();
		savedProjectionMatrix.setIdentity();
		modelViewMatrix.setIdentity();
		textureMatrix.setIdentity();
		GlStateManager._viewport(i, j, k, l);
	}

	public static int maxSupportedTextureSize() {
		assertThread(RenderSystem::isInInitPhase);
		if (MAX_SUPPORTED_TEXTURE_SIZE == -1) {
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

	public static void glBindBuffer(int i, IntSupplier intSupplier) {
		GlStateManager._glBindBuffer(i, intSupplier.getAsInt());
	}

	public static void glBindVertexArray(Supplier<Integer> supplier) {
		GlStateManager._glBindVertexArray((Integer)supplier.get());
	}

	public static void glBufferData(int i, ByteBuffer byteBuffer, int j) {
		assertThread(RenderSystem::isOnRenderThreadOrInit);
		GlStateManager._glBufferData(i, byteBuffer, j);
	}

	public static void glDeleteBuffers(int i) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._glDeleteBuffers(i);
	}

	public static void glDeleteVertexArrays(int i) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._glDeleteVertexArrays(i);
	}

	public static void glUniform1i(int i, int j) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._glUniform1i(i, j);
	}

	public static void glUniform1(int i, IntBuffer intBuffer) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._glUniform1(i, intBuffer);
	}

	public static void glUniform2(int i, IntBuffer intBuffer) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._glUniform2(i, intBuffer);
	}

	public static void glUniform3(int i, IntBuffer intBuffer) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._glUniform3(i, intBuffer);
	}

	public static void glUniform4(int i, IntBuffer intBuffer) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._glUniform4(i, intBuffer);
	}

	public static void glUniform1(int i, FloatBuffer floatBuffer) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._glUniform1(i, floatBuffer);
	}

	public static void glUniform2(int i, FloatBuffer floatBuffer) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._glUniform2(i, floatBuffer);
	}

	public static void glUniform3(int i, FloatBuffer floatBuffer) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._glUniform3(i, floatBuffer);
	}

	public static void glUniform4(int i, FloatBuffer floatBuffer) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._glUniform4(i, floatBuffer);
	}

	public static void glUniformMatrix2(int i, boolean bl, FloatBuffer floatBuffer) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._glUniformMatrix2(i, bl, floatBuffer);
	}

	public static void glUniformMatrix3(int i, boolean bl, FloatBuffer floatBuffer) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._glUniformMatrix3(i, bl, floatBuffer);
	}

	public static void glUniformMatrix4(int i, boolean bl, FloatBuffer floatBuffer) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._glUniformMatrix4(i, bl, floatBuffer);
	}

	public static void setupOverlayColor(IntSupplier intSupplier, int i) {
		assertThread(RenderSystem::isOnGameThread);
		int j = intSupplier.getAsInt();
		setShaderTexture(1, j);
	}

	public static void teardownOverlayColor() {
		assertThread(RenderSystem::isOnGameThread);
		setShaderTexture(1, 0);
	}

	public static void setupLevelDiffuseLighting(Vector3f vector3f, Vector3f vector3f2, Matrix4f matrix4f) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager.setupLevelDiffuseLighting(vector3f, vector3f2, matrix4f);
	}

	public static void setupGuiFlatDiffuseLighting(Vector3f vector3f, Vector3f vector3f2) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager.setupGuiFlatDiffuseLighting(vector3f, vector3f2);
	}

	public static void setupGui3DDiffuseLighting(Vector3f vector3f, Vector3f vector3f2) {
		assertThread(RenderSystem::isOnGameThread);
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

	public static void glGenBuffers(Consumer<Integer> consumer) {
		if (!isOnRenderThread()) {
			recordRenderCall(() -> consumer.accept(GlStateManager._glGenBuffers()));
		} else {
			consumer.accept(GlStateManager._glGenBuffers());
		}
	}

	public static void glGenVertexArrays(Consumer<Integer> consumer) {
		if (!isOnRenderThread()) {
			recordRenderCall(() -> consumer.accept(GlStateManager._glGenVertexArrays()));
		} else {
			consumer.accept(GlStateManager._glGenVertexArrays());
		}
	}

	public static Tesselator renderThreadTesselator() {
		assertThread(RenderSystem::isOnRenderThread);
		return RENDER_THREAD_TESSELATOR;
	}

	public static void defaultBlendFunc() {
		blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
	}

	@Deprecated
	public static void runAsFancy(Runnable runnable) {
		boolean bl = Minecraft.useShaderTransparency();
		if (!bl) {
			runnable.run();
		} else {
			Options options = Minecraft.getInstance().options;
			GraphicsStatus graphicsStatus = options.graphicsMode;
			options.graphicsMode = GraphicsStatus.FANCY;
			runnable.run();
			options.graphicsMode = graphicsStatus;
		}
	}

	public static void setShader(Supplier<ShaderInstance> supplier) {
		if (!isOnRenderThread()) {
			recordRenderCall(() -> shader = (ShaderInstance)supplier.get());
		} else {
			shader = (ShaderInstance)supplier.get();
		}
	}

	@Nullable
	public static ShaderInstance getShader() {
		assertThread(RenderSystem::isOnRenderThread);
		return shader;
	}

	public static int getTextureId(int i) {
		return GlStateManager._getTextureId(i);
	}

	public static void setShaderTexture(int i, ResourceLocation resourceLocation) {
		if (!isOnRenderThread()) {
			recordRenderCall(() -> _setShaderTexture(i, resourceLocation));
		} else {
			_setShaderTexture(i, resourceLocation);
		}
	}

	public static void _setShaderTexture(int i, ResourceLocation resourceLocation) {
		if (i >= 0 && i < shaderTextures.length) {
			TextureManager textureManager = Minecraft.getInstance().getTextureManager();
			AbstractTexture abstractTexture = textureManager.getTexture(resourceLocation);
			shaderTextures[i] = abstractTexture.getId();
		}
	}

	public static void setShaderTexture(int i, int j) {
		if (!isOnRenderThread()) {
			recordRenderCall(() -> _setShaderTexture(i, j));
		} else {
			_setShaderTexture(i, j);
		}
	}

	public static void _setShaderTexture(int i, int j) {
		if (i >= 0 && i < shaderTextures.length) {
			shaderTextures[i] = j;
		}
	}

	public static int getShaderTexture(int i) {
		assertThread(RenderSystem::isOnRenderThread);
		return i >= 0 && i < shaderTextures.length ? shaderTextures[i] : 0;
	}

	public static void setProjectionMatrix(Matrix4f matrix4f) {
		Matrix4f matrix4f2 = matrix4f.copy();
		if (!isOnRenderThread()) {
			recordRenderCall(() -> projectionMatrix = matrix4f2);
		} else {
			projectionMatrix = matrix4f2;
		}
	}

	public static void setTextureMatrix(Matrix4f matrix4f) {
		Matrix4f matrix4f2 = matrix4f.copy();
		if (!isOnRenderThread()) {
			recordRenderCall(() -> textureMatrix = matrix4f2);
		} else {
			textureMatrix = matrix4f2;
		}
	}

	public static void resetTextureMatrix() {
		if (!isOnRenderThread()) {
			recordRenderCall(() -> textureMatrix.setIdentity());
		} else {
			textureMatrix.setIdentity();
		}
	}

	public static void applyModelViewMatrix() {
		Matrix4f matrix4f = modelViewStack.last().pose().copy();
		if (!isOnRenderThread()) {
			recordRenderCall(() -> modelViewMatrix = matrix4f);
		} else {
			modelViewMatrix = matrix4f;
		}
	}

	public static void backupProjectionMatrix() {
		if (!isOnRenderThread()) {
			recordRenderCall(() -> _backupProjectionMatrix());
		} else {
			_backupProjectionMatrix();
		}
	}

	private static void _backupProjectionMatrix() {
		savedProjectionMatrix = projectionMatrix;
	}

	public static void restoreProjectionMatrix() {
		if (!isOnRenderThread()) {
			recordRenderCall(() -> _restoreProjectionMatrix());
		} else {
			_restoreProjectionMatrix();
		}
	}

	private static void _restoreProjectionMatrix() {
		projectionMatrix = savedProjectionMatrix;
	}

	public static Matrix4f getProjectionMatrix() {
		assertThread(RenderSystem::isOnRenderThread);
		return projectionMatrix;
	}

	public static Matrix4f getModelViewMatrix() {
		assertThread(RenderSystem::isOnRenderThread);
		return modelViewMatrix;
	}

	public static PoseStack getModelViewStack() {
		return modelViewStack;
	}

	public static Matrix4f getTextureMatrix() {
		assertThread(RenderSystem::isOnRenderThread);
		return textureMatrix;
	}

	public static RenderSystem.AutoStorageIndexBuffer getSequentialBuffer(VertexFormat.Mode mode, int i) {
		assertThread(RenderSystem::isOnRenderThread);
		RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer;
		if (mode == VertexFormat.Mode.QUADS) {
			autoStorageIndexBuffer = sharedSequentialQuad;
		} else if (mode == VertexFormat.Mode.LINES) {
			autoStorageIndexBuffer = sharedSequentialLines;
		} else {
			autoStorageIndexBuffer = sharedSequential;
		}

		autoStorageIndexBuffer.ensureStorage(i);
		return autoStorageIndexBuffer;
	}

	public static void setShaderGameTime(long l, float f) {
		float g = ((float)l + f) / 24000.0F;
		if (!isOnRenderThread()) {
			recordRenderCall(() -> shaderGameTime = g);
		} else {
			shaderGameTime = g;
		}
	}

	public static float getShaderGameTime() {
		assertThread(RenderSystem::isOnRenderThread);
		return shaderGameTime;
	}

	static {
		projectionMatrix.setIdentity();
		savedProjectionMatrix.setIdentity();
		modelViewMatrix.setIdentity();
		textureMatrix.setIdentity();
	}

	@Environment(EnvType.CLIENT)
	public static final class AutoStorageIndexBuffer {
		private final int vertexStride;
		private final int indexStride;
		private final RenderSystem.AutoStorageIndexBuffer.IndexGenerator generator;
		private int name;
		private VertexFormat.IndexType type = VertexFormat.IndexType.BYTE;
		private int indexCount;

		private AutoStorageIndexBuffer(int i, int j, RenderSystem.AutoStorageIndexBuffer.IndexGenerator indexGenerator) {
			this.vertexStride = i;
			this.indexStride = j;
			this.generator = indexGenerator;
		}

		private void ensureStorage(int i) {
			if (i > this.indexCount) {
				RenderSystem.LOGGER.debug("Growing IndexBuffer: Old limit {}, new limit {}.", this.indexCount, i);
				if (this.name == 0) {
					this.name = GlStateManager._glGenBuffers();
				}

				VertexFormat.IndexType indexType = VertexFormat.IndexType.least(i);
				int j = Mth.roundToward(i * indexType.bytes, 4);
				GlStateManager._glBindBuffer(34963, this.name);
				GlStateManager._glBufferData(34963, (long)j, 35048);
				ByteBuffer byteBuffer = GlStateManager._glMapBuffer(34963, 35001);
				if (byteBuffer == null) {
					throw new RuntimeException("Failed to map GL buffer");
				} else {
					this.type = indexType;
					it.unimi.dsi.fastutil.ints.IntConsumer intConsumer = this.intConsumer(byteBuffer);

					for (int k = 0; k < i; k += this.indexStride) {
						this.generator.accept(intConsumer, k * this.vertexStride / this.indexStride);
					}

					GlStateManager._glUnmapBuffer(34963);
					GlStateManager._glBindBuffer(34963, 0);
					this.indexCount = i;
					BufferUploader.invalidateElementArrayBufferBinding();
				}
			}
		}

		private it.unimi.dsi.fastutil.ints.IntConsumer intConsumer(ByteBuffer byteBuffer) {
			switch (this.type) {
				case BYTE:
					return i -> byteBuffer.put((byte)i);
				case SHORT:
					return i -> byteBuffer.putShort((short)i);
				case INT:
				default:
					return byteBuffer::putInt;
			}
		}

		public int name() {
			return this.name;
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
