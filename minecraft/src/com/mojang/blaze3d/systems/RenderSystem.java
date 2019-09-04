package com.mojang.blaze3d.systems;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.math.Matrix4f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.LongSupplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.glfw.GLFWErrorCallbackI;

@Environment(EnvType.CLIENT)
public class RenderSystem {
	private static final ConcurrentLinkedQueue<Runnable> recordingQueue = Queues.newConcurrentLinkedQueue();
	private static Thread clientThread;
	private static Thread renderThread;

	public static void initClientThread() {
		if (clientThread == null && renderThread != Thread.currentThread()) {
			clientThread = Thread.currentThread();
		} else {
			throw new IllegalStateException("Could not initialize tick thread");
		}
	}

	public static boolean isOnClientThread() {
		return clientThread == Thread.currentThread();
	}

	public static void initRenderThread() {
		if (renderThread == null && clientThread != Thread.currentThread()) {
			renderThread = Thread.currentThread();
		} else {
			throw new IllegalStateException("Could not initialize render thread");
		}
	}

	public static boolean isOnRenderThread() {
		return renderThread == Thread.currentThread();
	}

	public static void pushLightingAttributes() {
		GlStateManager._pushLightingAttributes();
	}

	public static void pushTextureAttributes() {
		GlStateManager._pushTextureAttributes();
	}

	public static void popAttributes() {
		GlStateManager._popAttributes();
	}

	public static void disableAlphaTest() {
		GlStateManager._disableAlphaTest();
	}

	public static void enableAlphaTest() {
		GlStateManager._enableAlphaTest();
	}

	public static void alphaFunc(int i, float f) {
		GlStateManager._alphaFunc(i, f);
	}

	public static void enableLighting() {
		GlStateManager._enableLighting();
	}

	public static void disableLighting() {
		GlStateManager._disableLighting();
	}

	public static void enableLight(int i) {
		GlStateManager._enableLight(i);
	}

	public static void disableLight(int i) {
		GlStateManager._disableLight(i);
	}

	public static void enableColorMaterial() {
		GlStateManager._enableColorMaterial();
	}

	public static void disableColorMaterial() {
		GlStateManager._disableColorMaterial();
	}

	public static void colorMaterial(int i, int j) {
		GlStateManager._colorMaterial(i, j);
	}

	public static void light(int i, int j, FloatBuffer floatBuffer) {
		GlStateManager._light(i, j, floatBuffer);
	}

	public static void lightModel(int i, FloatBuffer floatBuffer) {
		GlStateManager._lightModel(i, floatBuffer);
	}

	public static void normal3f(float f, float g, float h) {
		GlStateManager._normal3f(f, g, h);
	}

	public static void disableDepthTest() {
		GlStateManager._disableDepthTest();
	}

	public static void enableDepthTest() {
		GlStateManager._enableDepthTest();
	}

	public static void depthFunc(int i) {
		GlStateManager._depthFunc(i);
	}

	public static void depthMask(boolean bl) {
		GlStateManager._depthMask(bl);
	}

	public static void enableBlend() {
		GlStateManager._enableBlend();
	}

	public static void disableBlend() {
		GlStateManager._disableBlend();
	}

	public static void blendFunc(GlStateManager.SourceFactor sourceFactor, GlStateManager.DestFactor destFactor) {
		GlStateManager._blendFunc(sourceFactor.value, destFactor.value);
	}

	public static void blendFunc(int i, int j) {
		GlStateManager._blendFunc(i, j);
	}

	public static void blendFuncSeparate(
		GlStateManager.SourceFactor sourceFactor,
		GlStateManager.DestFactor destFactor,
		GlStateManager.SourceFactor sourceFactor2,
		GlStateManager.DestFactor destFactor2
	) {
		GlStateManager._blendFuncSeparate(sourceFactor.value, destFactor.value, sourceFactor2.value, destFactor2.value);
	}

	public static void blendFuncSeparate(int i, int j, int k, int l) {
		GlStateManager._blendFuncSeparate(i, j, k, l);
	}

	public static void blendEquation(int i) {
		GlStateManager._blendEquation(i);
	}

	public static void setupSolidRenderingTextureCombine(int i) {
		GlStateManager._setupSolidRenderingTextureCombine(i);
	}

	public static void tearDownSolidRenderingTextureCombine() {
		GlStateManager._tearDownSolidRenderingTextureCombine();
	}

	public static void enableFog() {
		GlStateManager._enableFog();
	}

	public static void disableFog() {
		GlStateManager._disableFog();
	}

	public static void fogMode(GlStateManager.FogMode fogMode) {
		GlStateManager._fogMode(fogMode.value);
	}

	public static void fogMode(int i) {
		GlStateManager._fogMode(i);
	}

	public static void fogDensity(float f) {
		GlStateManager._fogDensity(f);
	}

	public static void fogStart(float f) {
		GlStateManager._fogStart(f);
	}

	public static void fogEnd(float f) {
		GlStateManager._fogEnd(f);
	}

	public static void fog(int i, FloatBuffer floatBuffer) {
		GlStateManager._fog(i, floatBuffer);
	}

	public static void fogi(int i, int j) {
		GlStateManager._fogi(i, j);
	}

	public static void enableCull() {
		GlStateManager._enableCull();
	}

	public static void disableCull() {
		GlStateManager._disableCull();
	}

	public static void cullFace(GlStateManager.CullFace cullFace) {
		GlStateManager._cullFace(cullFace.value);
	}

	public static void cullFace(int i) {
		GlStateManager._cullFace(i);
	}

	public static void polygonMode(int i, int j) {
		GlStateManager._polygonMode(i, j);
	}

	public static void enablePolygonOffset() {
		GlStateManager._enablePolygonOffset();
	}

	public static void disablePolygonOffset() {
		GlStateManager._disablePolygonOffset();
	}

	public static void enableLineOffset() {
		GlStateManager._enableLineOffset();
	}

	public static void disableLineOffset() {
		GlStateManager._disableLineOffset();
	}

	public static void polygonOffset(float f, float g) {
		GlStateManager._polygonOffset(f, g);
	}

	public static void enableColorLogicOp() {
		GlStateManager._enableColorLogicOp();
	}

	public static void disableColorLogicOp() {
		GlStateManager._disableColorLogicOp();
	}

	public static void logicOp(GlStateManager.LogicOp logicOp) {
		GlStateManager._logicOp(logicOp.value);
	}

	public static void logicOp(int i) {
		GlStateManager._logicOp(i);
	}

	public static void enableTexGen(GlStateManager.TexGen texGen) {
		GlStateManager._enableTexGen(texGen);
	}

	public static void disableTexGen(GlStateManager.TexGen texGen) {
		GlStateManager._disableTexGen(texGen);
	}

	public static void texGenMode(GlStateManager.TexGen texGen, int i) {
		GlStateManager._texGenMode(texGen, i);
	}

	public static void texGenParam(GlStateManager.TexGen texGen, int i, FloatBuffer floatBuffer) {
		GlStateManager._texGenParam(texGen, i, floatBuffer);
	}

	public static void activeTexture(int i) {
		GlStateManager._activeTexture(i);
	}

	public static void enableTexture() {
		GlStateManager._enableTexture();
	}

	public static void disableTexture() {
		GlStateManager._disableTexture();
	}

	public static void texEnv(int i, int j, FloatBuffer floatBuffer) {
		GlStateManager._texEnv(i, j, floatBuffer);
	}

	public static void texEnv(int i, int j, int k) {
		GlStateManager._texEnv(i, j, k);
	}

	public static void texEnv(int i, int j, float f) {
		GlStateManager._texEnv(i, j, f);
	}

	public static void texParameter(int i, int j, float f) {
		GlStateManager._texParameter(i, j, f);
	}

	public static void texParameter(int i, int j, int k) {
		GlStateManager._texParameter(i, j, k);
	}

	public static int getTexLevelParameter(int i, int j, int k) {
		return GlStateManager._getTexLevelParameter(i, j, k);
	}

	public static int genTexture() {
		return GlStateManager._genTexture();
	}

	public static void deleteTexture(int i) {
		GlStateManager._deleteTexture(i);
	}

	public static void bindTexture(int i) {
		GlStateManager._bindTexture(i);
	}

	public static void texImage2D(int i, int j, int k, int l, int m, int n, int o, int p, @Nullable IntBuffer intBuffer) {
		GlStateManager._texImage2D(i, j, k, l, m, n, o, p, intBuffer);
	}

	public static void texSubImage2D(int i, int j, int k, int l, int m, int n, int o, int p, long q) {
		GlStateManager._texSubImage2D(i, j, k, l, m, n, o, p, q);
	}

	public static void copyTexSubImage2D(int i, int j, int k, int l, int m, int n, int o, int p) {
		GlStateManager._copyTexSubImage2D(i, j, k, l, m, n, o, p);
	}

	public static void getTexImage(int i, int j, int k, int l, long m) {
		GlStateManager._getTexImage(i, j, k, l, m);
	}

	public static void enableNormalize() {
		GlStateManager._enableNormalize();
	}

	public static void disableNormalize() {
		GlStateManager._disableNormalize();
	}

	public static void shadeModel(int i) {
		GlStateManager._shadeModel(i);
	}

	public static void enableRescaleNormal() {
		GlStateManager._enableRescaleNormal();
	}

	public static void disableRescaleNormal() {
		GlStateManager._disableRescaleNormal();
	}

	public static void viewport(int i, int j, int k, int l) {
		GlStateManager._viewport(i, j, k, l);
	}

	public static void colorMask(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
		GlStateManager._colorMask(bl, bl2, bl3, bl4);
	}

	public static void stencilFunc(int i, int j, int k) {
		GlStateManager._stencilFunc(i, j, k);
	}

	public static void stencilMask(int i) {
		GlStateManager._stencilMask(i);
	}

	public static void stencilOp(int i, int j, int k) {
		GlStateManager._stencilOp(i, j, k);
	}

	public static void clearDepth(double d) {
		GlStateManager._clearDepth(d);
	}

	public static void clearColor(float f, float g, float h, float i) {
		GlStateManager._clearColor(f, g, h, i);
	}

	public static void clearStencil(int i) {
		GlStateManager._clearStencil(i);
	}

	public static void clear(int i, boolean bl) {
		GlStateManager._clear(i, bl);
	}

	public static void matrixMode(int i) {
		GlStateManager._matrixMode(i);
	}

	public static void loadIdentity() {
		GlStateManager._loadIdentity();
	}

	public static void pushMatrix() {
		GlStateManager._pushMatrix();
	}

	public static void popMatrix() {
		GlStateManager._popMatrix();
	}

	public static void getMatrix(int i, FloatBuffer floatBuffer) {
		GlStateManager._getMatrix(i, floatBuffer);
	}

	public static Matrix4f getMatrix4f(int i) {
		return GlStateManager._getMatrix4f(i);
	}

	public static void ortho(double d, double e, double f, double g, double h, double i) {
		GlStateManager._ortho(d, e, f, g, h, i);
	}

	public static void rotatef(float f, float g, float h, float i) {
		GlStateManager._rotatef(f, g, h, i);
	}

	public static void rotated(double d, double e, double f, double g) {
		GlStateManager._rotated(d, e, f, g);
	}

	public static void scalef(float f, float g, float h) {
		GlStateManager._scalef(f, g, h);
	}

	public static void scaled(double d, double e, double f) {
		GlStateManager._scaled(d, e, f);
	}

	public static void translatef(float f, float g, float h) {
		GlStateManager._translatef(f, g, h);
	}

	public static void translated(double d, double e, double f) {
		GlStateManager._translated(d, e, f);
	}

	public static void multMatrix(FloatBuffer floatBuffer) {
		GlStateManager._multMatrix(floatBuffer);
	}

	public static void multMatrix(Matrix4f matrix4f) {
		GlStateManager._multMatrix(matrix4f);
	}

	public static void color4f(float f, float g, float h, float i) {
		GlStateManager._color4f(f, g, h, i);
	}

	public static void color3f(float f, float g, float h) {
		GlStateManager._color4f(f, g, h, 1.0F);
	}

	public static void texCoord2f(float f, float g) {
		GlStateManager._texCoord2f(f, g);
	}

	public static void vertex3f(float f, float g, float h) {
		GlStateManager._vertex3f(f, g, h);
	}

	public static void clearCurrentColor() {
		GlStateManager._clearCurrentColor();
	}

	public static void normalPointer(int i, int j, int k) {
		GlStateManager._normalPointer(i, j, k);
	}

	public static void normalPointer(int i, int j, ByteBuffer byteBuffer) {
		GlStateManager._normalPointer(i, j, byteBuffer);
	}

	public static void texCoordPointer(int i, int j, int k, int l) {
		GlStateManager._texCoordPointer(i, j, k, l);
	}

	public static void texCoordPointer(int i, int j, int k, ByteBuffer byteBuffer) {
		GlStateManager._texCoordPointer(i, j, k, byteBuffer);
	}

	public static void vertexPointer(int i, int j, int k, int l) {
		GlStateManager._vertexPointer(i, j, k, l);
	}

	public static void vertexPointer(int i, int j, int k, ByteBuffer byteBuffer) {
		GlStateManager._vertexPointer(i, j, k, byteBuffer);
	}

	public static void colorPointer(int i, int j, int k, int l) {
		GlStateManager._colorPointer(i, j, k, l);
	}

	public static void colorPointer(int i, int j, int k, ByteBuffer byteBuffer) {
		GlStateManager._colorPointer(i, j, k, byteBuffer);
	}

	public static void disableClientState(int i) {
		GlStateManager._disableClientState(i);
	}

	public static void enableClientState(int i) {
		GlStateManager._enableClientState(i);
	}

	public static void begin(int i) {
		GlStateManager._begin(i);
	}

	public static void end() {
		GlStateManager._end();
	}

	public static void drawArrays(int i, int j, int k) {
		GlStateManager._drawArrays(i, j, k);
	}

	public static void lineWidth(float f) {
		GlStateManager._lineWidth(f);
	}

	public static void callList(int i) {
		GlStateManager._callList(i);
	}

	public static void deleteLists(int i, int j) {
		GlStateManager._deleteLists(i, j);
	}

	public static void newList(int i, int j) {
		GlStateManager._newList(i, j);
	}

	public static void endList() {
		GlStateManager._endList();
	}

	public static int genLists(int i) {
		return GlStateManager._genLists(i);
	}

	public static void pixelStore(int i, int j) {
		GlStateManager._pixelStore(i, j);
	}

	public static void pixelTransfer(int i, float f) {
		GlStateManager._pixelTransfer(i, f);
	}

	public static void readPixels(int i, int j, int k, int l, int m, int n, ByteBuffer byteBuffer) {
		GlStateManager._readPixels(i, j, k, l, m, n, byteBuffer);
	}

	public static void readPixels(int i, int j, int k, int l, int m, int n, long o) {
		GlStateManager._readPixels(i, j, k, l, m, n, o);
	}

	public static int getError() {
		return GlStateManager._getError();
	}

	public static String getString(int i) {
		return GlStateManager._getString(i);
	}

	public static void getInteger(int i, IntBuffer intBuffer) {
		GlStateManager._getInteger(i, intBuffer);
	}

	public static int getInteger(int i) {
		return GlStateManager._getInteger(i);
	}

	public static String getBackendDescription() {
		return String.format("LWJGL version %s", GLX._getLWJGLVersion());
	}

	public static String getApiDescription() {
		return GLX.getOpenGLVersionString();
	}

	public static LongSupplier initBackendSystem() {
		return GLX._initGlfw();
	}

	public static void initRenderer(int i, boolean bl) {
		GLX._init(i, bl);
	}

	public static void setErrorCallback(GLFWErrorCallbackI gLFWErrorCallbackI) {
		GLX._setGlfwErrorCallback(gLFWErrorCallbackI);
	}

	public static void pollEvents() {
		GLX._pollEvents();
	}

	public static void glClientActiveTexture(int i) {
		GlStateManager._glClientActiveTexture(i);
	}

	public static void renderCrosshair(int i) {
		GLX._renderCrosshair(i, true, true, true);
	}

	public static void setupNvFogDistance() {
		GLX._setupNvFogDistance();
	}

	public static void glMultiTexCoord2f(int i, float f, float g) {
		GlStateManager._glMultiTexCoord2f(i, f, g);
	}

	public static String getCapsString() {
		return GLX._getCapsString();
	}
}
