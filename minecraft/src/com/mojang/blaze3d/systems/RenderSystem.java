package com.mojang.blaze3d.systems;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeSource;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
@DontObfuscate
public class RenderSystem {
	static final Logger LOGGER = LogUtils.getLogger();
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
	private static Matrix3f inverseViewRotationMatrix = new Matrix3f();
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
	private static FogShape shaderFogShape = FogShape.SPHERE;
	private static final Vector3f[] shaderLightDirections = new Vector3f[2];
	private static float shaderGameTime;
	private static float shaderLineWidth = 1.0F;
	private static String apiDescription = "Unknown";
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

	public static void assertInInitPhase() {
		if (!isInInitPhase()) {
			throw constructThreadException();
		}
	}

	public static void assertOnGameThreadOrInit() {
		if (!isInInit && !isOnGameThread()) {
			throw constructThreadException();
		}
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

	public static void assertOnGameThread() {
		if (!isOnGameThread()) {
			throw constructThreadException();
		}
	}

	private static IllegalStateException constructThreadException() {
		return new IllegalStateException("Rendersystem called from wrong thread");
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
		assertOnRenderThread();
		GlStateManager._disableDepthTest();
	}

	public static void enableDepthTest() {
		assertOnGameThreadOrInit();
		GlStateManager._enableDepthTest();
	}

	public static void enableScissor(int i, int j, int k, int l) {
		assertOnGameThreadOrInit();
		GlStateManager._enableScissorTest();
		GlStateManager._scissorBox(i, j, k, l);
	}

	public static void disableScissor() {
		assertOnGameThreadOrInit();
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

	public static void enableTexture() {
		assertOnRenderThread();
		GlStateManager._enableTexture();
	}

	public static void disableTexture() {
		assertOnRenderThread();
		GlStateManager._disableTexture();
	}

	public static void texParameter(int i, int j, int k) {
		GlStateManager._texParameter(i, j, k);
	}

	public static void deleteTexture(int i) {
		assertOnGameThreadOrInit();
		GlStateManager._deleteTexture(i);
	}

	public static void bindTextureForSetup(int i) {
		bindTexture(i);
	}

	public static void bindTexture(int i) {
		GlStateManager._bindTexture(i);
	}

	public static void viewport(int i, int j, int k, int l) {
		assertOnGameThreadOrInit();
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
		assertOnGameThreadOrInit();
		GlStateManager._clearDepth(d);
	}

	public static void clearColor(float f, float g, float h, float i) {
		assertOnGameThreadOrInit();
		GlStateManager._clearColor(f, g, h, i);
	}

	public static void clearStencil(int i) {
		assertOnRenderThread();
		GlStateManager._clearStencil(i);
	}

	public static void clear(int i, boolean bl) {
		assertOnGameThreadOrInit();
		GlStateManager._clear(i, bl);
	}

	public static void setShaderFogStart(float f) {
		assertOnRenderThread();
		_setShaderFogStart(f);
	}

	private static void _setShaderFogStart(float f) {
		shaderFogStart = f;
	}

	public static float getShaderFogStart() {
		assertOnRenderThread();
		return shaderFogStart;
	}

	public static void setShaderFogEnd(float f) {
		assertOnRenderThread();
		_setShaderFogEnd(f);
	}

	private static void _setShaderFogEnd(float f) {
		shaderFogEnd = f;
	}

	public static float getShaderFogEnd() {
		assertOnRenderThread();
		return shaderFogEnd;
	}

	public static void setShaderFogColor(float f, float g, float h, float i) {
		assertOnRenderThread();
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
		assertOnRenderThread();
		return shaderFogColor;
	}

	public static void setShaderFogShape(FogShape fogShape) {
		assertOnRenderThread();
		_setShaderFogShape(fogShape);
	}

	private static void _setShaderFogShape(FogShape fogShape) {
		shaderFogShape = fogShape;
	}

	public static FogShape getShaderFogShape() {
		assertOnRenderThread();
		return shaderFogShape;
	}

	public static void setShaderLights(Vector3f vector3f, Vector3f vector3f2) {
		assertOnRenderThread();
		_setShaderLights(vector3f, vector3f2);
	}

	public static void _setShaderLights(Vector3f vector3f, Vector3f vector3f2) {
		shaderLightDirections[0] = vector3f;
		shaderLightDirections[1] = vector3f2;
	}

	public static void setupShaderLights(ShaderInstance shaderInstance) {
		assertOnRenderThread();
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
		assertOnRenderThread();
		return shaderColor;
	}

	public static void drawElements(int i, int j, int k) {
		assertOnRenderThread();
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
		assertOnRenderThread();
		return shaderLineWidth;
	}

	public static void pixelStore(int i, int j) {
		assertOnGameThreadOrInit();
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
		assertInInitPhase();
		return String.format(Locale.ROOT, "LWJGL version %s", GLX._getLWJGLVersion());
	}

	public static String getApiDescription() {
		return apiDescription;
	}

	public static TimeSource.NanoTimeSource initBackendSystem() {
		assertInInitPhase();
		return GLX._initGlfw()::getAsLong;
	}

	public static void initRenderer(int i, boolean bl) {
		assertInInitPhase();
		GLX._init(i, bl);
		apiDescription = GLX.getOpenGLVersionString();
	}

	public static void setErrorCallback(GLFWErrorCallbackI gLFWErrorCallbackI) {
		assertInInitPhase();
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
		assertInInitPhase();
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

	public static void glBindBuffer(int i, IntSupplier intSupplier) {
		GlStateManager._glBindBuffer(i, intSupplier.getAsInt());
	}

	public static void glBindVertexArray(Supplier<Integer> supplier) {
		GlStateManager._glBindVertexArray((Integer)supplier.get());
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

	public static void setupOverlayColor(IntSupplier intSupplier, int i) {
		assertOnRenderThread();
		int j = intSupplier.getAsInt();
		setShaderTexture(1, j);
	}

	public static void teardownOverlayColor() {
		assertOnRenderThread();
		setShaderTexture(1, 0);
	}

	public static void setupLevelDiffuseLighting(Vector3f vector3f, Vector3f vector3f2, Matrix4f matrix4f) {
		assertOnRenderThread();
		GlStateManager.setupLevelDiffuseLighting(vector3f, vector3f2, matrix4f);
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
		assertOnRenderThread();
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
			OptionInstance<GraphicsStatus> optionInstance = Minecraft.getInstance().options.graphicsMode();
			GraphicsStatus graphicsStatus = optionInstance.get();
			optionInstance.set(GraphicsStatus.FANCY);
			runnable.run();
			optionInstance.set(graphicsStatus);
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
		assertOnRenderThread();
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
		assertOnRenderThread();
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

	public static void setInverseViewRotationMatrix(Matrix3f matrix3f) {
		Matrix3f matrix3f2 = matrix3f.copy();
		if (!isOnRenderThread()) {
			recordRenderCall(() -> inverseViewRotationMatrix = matrix3f2);
		} else {
			inverseViewRotationMatrix = matrix3f2;
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
		assertOnRenderThread();
		return projectionMatrix;
	}

	public static Matrix3f getInverseViewRotationMatrix() {
		assertOnRenderThread();
		return inverseViewRotationMatrix;
	}

	public static Matrix4f getModelViewMatrix() {
		assertOnRenderThread();
		return modelViewMatrix;
	}

	public static PoseStack getModelViewStack() {
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
		float g = ((float)(l % 24000L) + f) / 24000.0F;
		if (!isOnRenderThread()) {
			recordRenderCall(() -> shaderGameTime = g);
		} else {
			shaderGameTime = g;
		}
	}

	public static float getShaderGameTime() {
		assertOnRenderThread();
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

		AutoStorageIndexBuffer(int i, int j, RenderSystem.AutoStorageIndexBuffer.IndexGenerator indexGenerator) {
			this.vertexStride = i;
			this.indexStride = j;
			this.generator = indexGenerator;
		}

		public boolean hasStorage(int i) {
			return i <= this.indexCount;
		}

		public void bind(int i) {
			if (this.name == 0) {
				this.name = GlStateManager._glGenBuffers();
			}

			GlStateManager._glBindBuffer(34963, this.name);
			this.ensureStorage(i);
		}

		private void ensureStorage(int i) {
			if (!this.hasStorage(i)) {
				i = Mth.roundToward(i * 2, this.indexStride);
				RenderSystem.LOGGER.debug("Growing IndexBuffer: Old limit {}, new limit {}.", this.indexCount, i);
				VertexFormat.IndexType indexType = VertexFormat.IndexType.least(i);
				int j = Mth.roundToward(i * indexType.bytes, 4);
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
					this.indexCount = i;
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

		public VertexFormat.IndexType type() {
			return this.type;
		}

		@Environment(EnvType.CLIENT)
		interface IndexGenerator {
			void accept(it.unimi.dsi.fastutil.ints.IntConsumer intConsumer, int i);
		}
	}
}
