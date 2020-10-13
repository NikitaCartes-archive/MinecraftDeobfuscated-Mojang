package com.mojang.blaze3d.systems;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;

@Environment(EnvType.CLIENT)
public class RenderSystem {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ConcurrentLinkedQueue<RenderCall> recordingQueue = Queues.newConcurrentLinkedQueue();
	private static final Tesselator RENDER_THREAD_TESSELATOR = new Tesselator();
	public static final float DEFAULTALPHACUTOFF = 0.1F;
	private static final int MINIMUM_ATLAS_TEXTURE_SIZE = 1024;
	private static boolean isReplayingQueue;
	private static Thread gameThread;
	private static Thread renderThread;
	private static int MAX_SUPPORTED_TEXTURE_SIZE = -1;
	private static boolean isInInit;
	private static double lastDrawTime = Double.MIN_VALUE;

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

	@Deprecated
	public static void pushLightingAttributes() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._pushLightingAttributes();
	}

	@Deprecated
	public static void pushTextureAttributes() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._pushTextureAttributes();
	}

	@Deprecated
	public static void popAttributes() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._popAttributes();
	}

	@Deprecated
	public static void disableAlphaTest() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._disableAlphaTest();
	}

	@Deprecated
	public static void enableAlphaTest() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._enableAlphaTest();
	}

	@Deprecated
	public static void alphaFunc(int i, float f) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._alphaFunc(i, f);
	}

	@Deprecated
	public static void enableLighting() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._enableLighting();
	}

	@Deprecated
	public static void disableLighting() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._disableLighting();
	}

	@Deprecated
	public static void enableColorMaterial() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._enableColorMaterial();
	}

	@Deprecated
	public static void disableColorMaterial() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._disableColorMaterial();
	}

	@Deprecated
	public static void colorMaterial(int i, int j) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._colorMaterial(i, j);
	}

	@Deprecated
	public static void normal3f(float f, float g, float h) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._normal3f(f, g, h);
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

	public static void blendColor(float f, float g, float h, float i) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._blendColor(f, g, h, i);
	}

	@Deprecated
	public static void enableFog() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._enableFog();
	}

	@Deprecated
	public static void disableFog() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._disableFog();
	}

	@Deprecated
	public static void fogMode(GlStateManager.FogMode fogMode) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._fogMode(fogMode.value);
	}

	@Deprecated
	public static void fogMode(int i) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._fogMode(i);
	}

	@Deprecated
	public static void fogDensity(float f) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._fogDensity(f);
	}

	@Deprecated
	public static void fogStart(float f) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._fogStart(f);
	}

	@Deprecated
	public static void fogEnd(float f) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._fogEnd(f);
	}

	@Deprecated
	public static void fog(int i, float f, float g, float h, float j) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._fog(i, new float[]{f, g, h, j});
	}

	@Deprecated
	public static void fogi(int i, int j) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._fogi(i, j);
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

	public static void enableLineOffset() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._enableLineOffset();
	}

	public static void disableLineOffset() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._disableLineOffset();
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

	public static void bindTexture(int i) {
		GlStateManager._bindTexture(i);
	}

	@Deprecated
	public static void shadeModel(int i) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._shadeModel(i);
	}

	@Deprecated
	public static void enableRescaleNormal() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._enableRescaleNormal();
	}

	@Deprecated
	public static void disableRescaleNormal() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._disableRescaleNormal();
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

	@Deprecated
	public static void matrixMode(int i) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._matrixMode(i);
	}

	@Deprecated
	public static void loadIdentity() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._loadIdentity();
	}

	@Deprecated
	public static void pushMatrix() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._pushMatrix();
	}

	@Deprecated
	public static void popMatrix() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._popMatrix();
	}

	@Deprecated
	public static void ortho(double d, double e, double f, double g, double h, double i) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._ortho(d, e, f, g, h, i);
	}

	@Deprecated
	public static void rotatef(float f, float g, float h, float i) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._rotatef(f, g, h, i);
	}

	@Deprecated
	public static void scalef(float f, float g, float h) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._scalef(f, g, h);
	}

	@Deprecated
	public static void scaled(double d, double e, double f) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._scaled(d, e, f);
	}

	@Deprecated
	public static void translatef(float f, float g, float h) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._translatef(f, g, h);
	}

	@Deprecated
	public static void translated(double d, double e, double f) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._translated(d, e, f);
	}

	@Deprecated
	public static void multMatrix(Matrix4f matrix4f) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._multMatrix(matrix4f);
	}

	@Deprecated
	public static void color4f(float f, float g, float h, float i) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._color4f(f, g, h, i);
	}

	@Deprecated
	public static void color3f(float f, float g, float h) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._color4f(f, g, h, 1.0F);
	}

	@Deprecated
	public static void clearCurrentColor() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._clearCurrentColor();
	}

	public static void drawArrays(int i, int j, int k) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._drawArrays(i, j, k);
	}

	public static void lineWidth(float f) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._lineWidth(f);
	}

	public static void pixelStore(int i, int j) {
		assertThread(RenderSystem::isOnGameThreadOrInit);
		GlStateManager._pixelStore(i, j);
	}

	public static void pixelTransfer(int i, float f) {
		GlStateManager._pixelTransfer(i, f);
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

	public static void setupNvFogDistance() {
		assertThread(RenderSystem::isOnGameThread);
		GLX._setupNvFogDistance();
	}

	@Deprecated
	public static void glMultiTexCoord2f(int i, float f, float g) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._glMultiTexCoord2f(i, f, g);
	}

	public static String getCapsString() {
		assertThread(RenderSystem::isOnGameThread);
		return GLX._getCapsString();
	}

	public static void setupDefaultState(int i, int j, int k, int l) {
		assertThread(RenderSystem::isInInitPhase);
		GlStateManager._enableTexture();
		GlStateManager._shadeModel(7425);
		GlStateManager._clearDepth(1.0);
		GlStateManager._enableDepthTest();
		GlStateManager._depthFunc(515);
		GlStateManager._enableAlphaTest();
		GlStateManager._alphaFunc(516, 0.1F);
		GlStateManager._matrixMode(5889);
		GlStateManager._loadIdentity();
		GlStateManager._matrixMode(5888);
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

	public static void glBindBuffer(int i, Supplier<Integer> supplier) {
		GlStateManager._glBindBuffer(i, (Integer)supplier.get());
	}

	public static void glBufferData(int i, ByteBuffer byteBuffer, int j) {
		assertThread(RenderSystem::isOnRenderThreadOrInit);
		GlStateManager._glBufferData(i, byteBuffer, j);
	}

	public static void glDeleteBuffers(int i) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager._glDeleteBuffers(i);
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

	public static void setupOutline() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager.setupOutline();
	}

	public static void teardownOutline() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager.teardownOutline();
	}

	public static void setupOverlayColor(IntSupplier intSupplier, int i) {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager.setupOverlayColor(intSupplier.getAsInt(), i);
	}

	public static void teardownOverlayColor() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager.teardownOverlayColor();
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

	public static void mulTextureByProjModelView() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager.mulTextureByProjModelView();
	}

	public static void setupEndPortalTexGen() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager.setupEndPortalTexGen();
	}

	public static void clearTexGen() {
		assertThread(RenderSystem::isOnGameThread);
		GlStateManager.clearTexGen();
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

	public static Tesselator renderThreadTesselator() {
		assertThread(RenderSystem::isOnRenderThread);
		return RENDER_THREAD_TESSELATOR;
	}

	public static void defaultBlendFunc() {
		blendFuncSeparate(
			GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
	}

	public static void defaultAlphaFunc() {
		alphaFunc(516, 0.1F);
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
}
