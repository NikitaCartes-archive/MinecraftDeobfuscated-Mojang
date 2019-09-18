/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.systems;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;

@Environment(value=EnvType.CLIENT)
public class RenderSystem {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ConcurrentLinkedQueue<RenderCall> recordingQueue = Queues.newConcurrentLinkedQueue();
    private static final Tesselator RENDER_THREAD_TESSELATOR = new Tesselator();
    private static final float DEFAULTALPHACUTOFF = 0.1f;
    private static boolean isReplayingQueue;
    private static Thread gameThread;
    private static Thread renderThread;
    private static int MAX_SUPPORTED_TEXTURE_SIZE;
    private static boolean isInInit;

    public static void initRenderThread() {
        if (renderThread != null || gameThread == Thread.currentThread()) {
            throw new IllegalStateException("Could not initialize render thread");
        }
        renderThread = Thread.currentThread();
    }

    public static boolean isOnRenderThread() {
        return Thread.currentThread() == renderThread;
    }

    public static boolean isOnRenderThreadOrInit() {
        return isInInit || RenderSystem.isOnRenderThread();
    }

    public static void initGameThread(boolean bl) {
        boolean bl2;
        boolean bl3 = bl2 = renderThread == Thread.currentThread();
        if (gameThread != null || renderThread == null || bl2 == bl) {
            throw new IllegalStateException("Could not initialize tick thread");
        }
        gameThread = Thread.currentThread();
    }

    public static boolean isOnGameThread() {
        return true;
    }

    public static boolean isOnGameThreadOrInit() {
        return isInInit || RenderSystem.isOnGameThread();
    }

    public static void assertThread(Supplier<Boolean> supplier) {
        if (!supplier.get().booleanValue()) {
            throw new IllegalStateException("Rendersystem called from wrong thread");
        }
    }

    public static boolean isInInitPhase() {
        return true;
    }

    public static void pushLightingAttributes() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._pushLightingAttributes();
    }

    public static void pushTextureAttributes() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._pushTextureAttributes();
    }

    public static void popAttributes() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._popAttributes();
    }

    public static void disableAlphaTest() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableAlphaTest();
    }

    public static void enableAlphaTest() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableAlphaTest();
    }

    public static void alphaFunc(int i, float f) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._alphaFunc(i, f);
    }

    public static void enableLighting() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableLighting();
    }

    public static void disableLighting() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableLighting();
    }

    public static void enableColorMaterial() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableColorMaterial();
    }

    public static void disableColorMaterial() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableColorMaterial();
    }

    public static void colorMaterial(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._colorMaterial(i, j);
    }

    public static void normal3f(float f, float g, float h) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._normal3f(f, g, h);
    }

    public static void disableDepthTest() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableDepthTest();
    }

    public static void enableDepthTest() {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._enableDepthTest();
    }

    public static void depthFunc(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._depthFunc(i);
    }

    public static void depthMask(boolean bl) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._depthMask(bl);
    }

    public static void enableBlend() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableBlend();
    }

    public static void disableBlend() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableBlend();
    }

    public static void blendFunc(GlStateManager.SourceFactor sourceFactor, GlStateManager.DestFactor destFactor) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._blendFunc(sourceFactor.value, destFactor.value);
    }

    public static void blendFunc(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._blendFunc(i, j);
    }

    public static void blendFuncSeparate(GlStateManager.SourceFactor sourceFactor, GlStateManager.DestFactor destFactor, GlStateManager.SourceFactor sourceFactor2, GlStateManager.DestFactor destFactor2) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._blendFuncSeparate(sourceFactor.value, destFactor.value, sourceFactor2.value, destFactor2.value);
    }

    public static void blendFuncSeparate(int i, int j, int k, int l) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._blendFuncSeparate(i, j, k, l);
    }

    public static void blendEquation(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._blendEquation(i);
    }

    public static void setupSolidRenderingTextureCombine(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._setupSolidRenderingTextureCombine(i);
    }

    public static void tearDownSolidRenderingTextureCombine() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._tearDownSolidRenderingTextureCombine();
    }

    public static void enableFog() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableFog();
    }

    public static void disableFog() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableFog();
    }

    public static void fogMode(GlStateManager.FogMode fogMode) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fogMode(fogMode.value);
    }

    public static void fogMode(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fogMode(i);
    }

    public static void fogDensity(float f) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fogDensity(f);
    }

    public static void fogStart(float f) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fogStart(f);
    }

    public static void fogEnd(float f) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fogEnd(f);
    }

    public static void fog(int i, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fog(i, floatBuffer);
    }

    public static void fogi(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fogi(i, j);
    }

    public static void enableCull() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableCull();
    }

    public static void disableCull() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableCull();
    }

    public static void cullFace(GlStateManager.CullFace cullFace) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._cullFace(cullFace.value);
    }

    public static void cullFace(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._cullFace(i);
    }

    public static void polygonMode(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._polygonMode(i, j);
    }

    public static void enablePolygonOffset() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enablePolygonOffset();
    }

    public static void disablePolygonOffset() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disablePolygonOffset();
    }

    public static void enableLineOffset() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableLineOffset();
    }

    public static void disableLineOffset() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableLineOffset();
    }

    public static void polygonOffset(float f, float g) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._polygonOffset(f, g);
    }

    public static void enableColorLogicOp() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableColorLogicOp();
    }

    public static void disableColorLogicOp() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableColorLogicOp();
    }

    public static void logicOp(GlStateManager.LogicOp logicOp) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._logicOp(logicOp.value);
    }

    public static void logicOp(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._logicOp(i);
    }

    public static void enableTexGen(GlStateManager.TexGen texGen) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableTexGen(texGen);
    }

    public static void disableTexGen(GlStateManager.TexGen texGen) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableTexGen(texGen);
    }

    public static void texGenMode(GlStateManager.TexGen texGen, int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._texGenMode(texGen, i);
    }

    public static void texGenParam(GlStateManager.TexGen texGen, int i, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._texGenParam(texGen, i, floatBuffer);
    }

    public static void activeTexture(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._activeTexture(i);
    }

    public static void enableTexture() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableTexture();
    }

    public static void disableTexture() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableTexture();
    }

    public static void texParameter(int i, int j, int k) {
        GlStateManager._texParameter(i, j, k);
    }

    public static void deleteTexture(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._deleteTexture(i);
    }

    public static void bindTexture(int i) {
        GlStateManager._bindTexture(i);
    }

    public static void texImage2D(int i, int j, int k, int l, int m, int n, int o, int p, @Nullable IntBuffer intBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._texImage2D(i, j, k, l, m, n, o, p, intBuffer);
    }

    public static void texSubImage2D(int i, int j, int k, int l, int m, int n, int o, int p, long q) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._texSubImage2D(i, j, k, l, m, n, o, p, q);
    }

    public static void copyTexSubImage2D(int i, int j, int k, int l, int m, int n, int o, int p) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._copyTexSubImage2D(i, j, k, l, m, n, o, p);
    }

    public static void getTexImage(int i, int j, int k, int l, long m) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._getTexImage(i, j, k, l, m);
    }

    public static void enableNormalize() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableNormalize();
    }

    public static void disableNormalize() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableNormalize();
    }

    public static void shadeModel(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._shadeModel(i);
    }

    public static void enableRescaleNormal() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableRescaleNormal();
    }

    public static void disableRescaleNormal() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableRescaleNormal();
    }

    public static void viewport(int i, int j, int k, int l) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._viewport(i, j, k, l);
    }

    public static void colorMask(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._colorMask(bl, bl2, bl3, bl4);
    }

    public static void stencilFunc(int i, int j, int k) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._stencilFunc(i, j, k);
    }

    public static void stencilMask(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._stencilMask(i);
    }

    public static void stencilOp(int i, int j, int k) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._stencilOp(i, j, k);
    }

    public static void clearDepth(double d) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._clearDepth(d);
    }

    public static void clearColor(float f, float g, float h, float i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._clearColor(f, g, h, i);
    }

    public static void clearStencil(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._clearStencil(i);
    }

    public static void clear(int i, boolean bl) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._clear(i, bl);
    }

    public static void matrixMode(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._matrixMode(i);
    }

    public static void loadIdentity() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._loadIdentity();
    }

    public static void pushMatrix() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._pushMatrix();
    }

    public static void popMatrix() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._popMatrix();
    }

    public static void getMatrix(int i, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._getMatrix(i, floatBuffer);
    }

    public static void getMatrix4f(int i, Consumer<Matrix4f> consumer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        consumer.accept(GlStateManager._getMatrix4f(i));
    }

    public static void ortho(double d, double e, double f, double g, double h, double i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._ortho(d, e, f, g, h, i);
    }

    public static void rotatef(float f, float g, float h, float i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._rotatef(f, g, h, i);
    }

    public static void rotated(double d, double e, double f, double g) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._rotated(d, e, f, g);
    }

    public static void scalef(float f, float g, float h) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._scalef(f, g, h);
    }

    public static void scaled(double d, double e, double f) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._scaled(d, e, f);
    }

    public static void translatef(float f, float g, float h) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._translatef(f, g, h);
    }

    public static void translated(double d, double e, double f) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._translated(d, e, f);
    }

    public static void multMatrix(FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._multMatrix(floatBuffer);
    }

    public static void multMatrix(Matrix4f matrix4f) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._multMatrix(matrix4f);
    }

    public static void color4f(float f, float g, float h, float i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._color4f(f, g, h, i);
    }

    public static void color3f(float f, float g, float h) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._color4f(f, g, h, 1.0f);
    }

    public static void clearCurrentColor() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._clearCurrentColor();
    }

    public static void drawArrays(int i, int j, int k) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._drawArrays(i, j, k);
    }

    public static void lineWidth(float f) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._lineWidth(f);
    }

    public static void pixelStore(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._pixelStore(i, j);
    }

    public static void pixelTransfer(int i, float f) {
        GlStateManager._pixelTransfer(i, f);
    }

    public static void readPixels(int i, int j, int k, int l, int m, int n, ByteBuffer byteBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._readPixels(i, j, k, l, m, n, byteBuffer);
    }

    public static void getError(Consumer<Integer> consumer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        consumer.accept(GlStateManager._getError());
    }

    public static void getString(int i, Consumer<String> consumer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        consumer.accept(GlStateManager._getString(i));
    }

    public static String getBackendDescription() {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        return String.format("LWJGL version %s", GLX._getLWJGLVersion());
    }

    public static String getApiDescription() {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        return GLX.getOpenGLVersionString();
    }

    public static LongSupplier initBackendSystem() {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        return GLX._initGlfw();
    }

    public static void initRenderer(int i, boolean bl) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        GLX._init(i, bl);
    }

    public static void setErrorCallback(GLFWErrorCallbackI gLFWErrorCallbackI) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        GLX._setGlfwErrorCallback(gLFWErrorCallbackI);
    }

    public static void pollEvents() {
    }

    public static void recordRenderCall(RenderCall renderCall) {
        recordingQueue.add(renderCall);
    }

    public static void glClientActiveTexture(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glClientActiveTexture(i);
    }

    public static void renderCrosshair(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GLX._renderCrosshair(i, true, true, true);
    }

    public static void setupNvFogDistance() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GLX._setupNvFogDistance();
    }

    public static void glMultiTexCoord2f(int i, float f, float g) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glMultiTexCoord2f(i, f, g);
    }

    public static String getCapsString() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        return GLX._getCapsString();
    }

    public static void setupDefaultState(int i, int j, int k, int l) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        GlStateManager._enableTexture();
        GlStateManager._shadeModel(7425);
        GlStateManager._clearDepth(1.0);
        GlStateManager._enableDepthTest();
        GlStateManager._depthFunc(515);
        GlStateManager._enableAlphaTest();
        GlStateManager._alphaFunc(516, 0.1f);
        GlStateManager._cullFace(GlStateManager.CullFace.BACK.value);
        GlStateManager._matrixMode(5889);
        GlStateManager._loadIdentity();
        GlStateManager._matrixMode(5888);
        GlStateManager._viewport(i, j, k, l);
    }

    public static int maxSupportedTextureSize() {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        if (MAX_SUPPORTED_TEXTURE_SIZE == -1) {
            for (int i = 16384; i > 0; i >>= 1) {
                GlStateManager._texImage2D(32868, 0, 6408, i, i, 0, 6408, 5121, null);
                int j = GlStateManager._getTexLevelParameter(32868, 0, 4096);
                if (j == 0) continue;
                MAX_SUPPORTED_TEXTURE_SIZE = i;
                return i;
            }
            MAX_SUPPORTED_TEXTURE_SIZE = Mth.clamp(GlStateManager._getInteger(3379), 1024, 16384);
            LOGGER.info("Failed to determine maximum texture size by probing, trying GL_MAX_TEXTURE_SIZE = {}", (Object)MAX_SUPPORTED_TEXTURE_SIZE);
        }
        return MAX_SUPPORTED_TEXTURE_SIZE;
    }

    public static void flipFrame() {
        GLFW.glfwPollEvents();
        isReplayingQueue = true;
        while (!recordingQueue.isEmpty()) {
            RenderCall renderCall = recordingQueue.poll();
            renderCall.execute();
        }
        isReplayingQueue = false;
        Tesselator.getInstance().getBuilder().clear();
    }

    public static void glBindFramebuffer(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._glBindFramebuffer(i, j);
    }

    public static void glDeleteRenderbuffers(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._glDeleteRenderbuffers(i);
    }

    public static void glDeleteFramebuffers(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._glDeleteFramebuffers(i);
    }

    public static void glFramebufferTexture2D(int i, int j, int k, int l, int m) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._glFramebufferTexture2D(i, j, k, l, m);
    }

    public static void glBindRenderbuffer(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._glBindRenderbuffer(i, j);
    }

    public static void glRenderbufferStorage(int i, int j, int k, int l) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._glRenderbufferStorage(i, j, k, l);
    }

    public static void glFramebufferRenderbuffer(int i, int j, int k, int l) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._glFramebufferRenderbuffer(i, j, k, l);
    }

    public static void glBindBuffer(int i, Supplier<Integer> supplier) {
        GlStateManager._glBindBuffer(i, supplier.get());
    }

    public static void glBufferData(int i, ByteBuffer byteBuffer, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager._glBufferData(i, byteBuffer, j);
    }

    public static void glDeleteBuffers(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glDeleteBuffers(i);
    }

    public static void glUniform1i(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniform1i(i, j);
    }

    public static void glUniform1(int i, IntBuffer intBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniform1(i, intBuffer);
    }

    public static void glUniform2(int i, IntBuffer intBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniform2(i, intBuffer);
    }

    public static void glUniform3(int i, IntBuffer intBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniform3(i, intBuffer);
    }

    public static void glUniform4(int i, IntBuffer intBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniform4(i, intBuffer);
    }

    public static void glUniform1(int i, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniform1(i, floatBuffer);
    }

    public static void glUniform2(int i, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniform2(i, floatBuffer);
    }

    public static void glUniform3(int i, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniform3(i, floatBuffer);
    }

    public static void glUniform4(int i, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniform4(i, floatBuffer);
    }

    public static void glUniformMatrix2(int i, boolean bl, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniformMatrix2(i, bl, floatBuffer);
    }

    public static void glUniformMatrix3(int i, boolean bl, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniformMatrix3(i, bl, floatBuffer);
    }

    public static void glUniformMatrix4(int i, boolean bl, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUniformMatrix4(i, bl, floatBuffer);
    }

    public static void glUseProgram(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glUseProgram(i);
    }

    public static void setupOverlayColor(int i, boolean bl) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager.setupOverlayColor(i, bl);
    }

    public static void teardownOverlayColor() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager.teardownOverlayColor();
    }

    public static void enableUsualDiffuseLighting() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager.enableUsualDiffuseLighting();
    }

    public static void enableGuiDiffuseLighting() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager.enableGuiDiffuseLighting();
    }

    public static void disableDiffuseLighting() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager.disableDiffuseLighting();
    }

    public static void setProfile(Profile profile) {
        profile.apply();
    }

    public static void unsetProfile(Profile profile) {
        profile.clean();
    }

    public static void beginInitialization() {
        isInInit = true;
    }

    public static void finishInitialization() {
        isInInit = false;
        if (!recordingQueue.isEmpty()) {
            RenderSystem.flipFrame();
        }
        if (!recordingQueue.isEmpty()) {
            throw new IllegalStateException("Recorded to render queue during initialization");
        }
    }

    public static void glGenBuffers(Consumer<Integer> consumer) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> consumer.accept(GlStateManager._glGenBuffers()));
        } else {
            consumer.accept(GlStateManager._glGenBuffers());
        }
    }

    public static Tesselator renderThreadTesselator() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return RENDER_THREAD_TESSELATOR;
    }

    public static void defaultBlendFunc() {
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }

    public static void defaultAlphaFunc() {
        RenderSystem.alphaFunc(516, 0.1f);
    }

    private static void setupDefaultGlState() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._setupDefaultGlState();
    }

    private static /* synthetic */ void lambda$setupOverlayColor$92(int i, boolean bl) {
        GlStateManager.setupOverlayColor(i, bl);
    }

    private static /* synthetic */ void lambda$glUseProgram$91(int i) {
        GlStateManager._glUseProgram(i);
    }

    private static /* synthetic */ void lambda$glUniformMatrix4$90(int i, boolean bl, FloatBuffer floatBuffer) {
        GlStateManager._glUniformMatrix4(i, bl, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniformMatrix3$89(int i, boolean bl, FloatBuffer floatBuffer) {
        GlStateManager._glUniformMatrix3(i, bl, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniformMatrix2$88(int i, boolean bl, FloatBuffer floatBuffer) {
        GlStateManager._glUniformMatrix2(i, bl, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform4$87(int i, FloatBuffer floatBuffer) {
        GlStateManager._glUniform4(i, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform3$86(int i, FloatBuffer floatBuffer) {
        GlStateManager._glUniform3(i, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform2$85(int i, FloatBuffer floatBuffer) {
        GlStateManager._glUniform2(i, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform1$84(int i, FloatBuffer floatBuffer) {
        GlStateManager._glUniform1(i, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform4$83(int i, IntBuffer intBuffer) {
        GlStateManager._glUniform4(i, intBuffer);
    }

    private static /* synthetic */ void lambda$glUniform3$82(int i, IntBuffer intBuffer) {
        GlStateManager._glUniform3(i, intBuffer);
    }

    private static /* synthetic */ void lambda$glUniform2$81(int i, IntBuffer intBuffer) {
        GlStateManager._glUniform2(i, intBuffer);
    }

    private static /* synthetic */ void lambda$glUniform1$80(int i, IntBuffer intBuffer) {
        GlStateManager._glUniform1(i, intBuffer);
    }

    private static /* synthetic */ void lambda$glUniform1i$79(int i, int j) {
        GlStateManager._glUniform1i(i, j);
    }

    private static /* synthetic */ void lambda$glDeleteBuffers$78(int i) {
        GlStateManager._glDeleteBuffers(i);
    }

    private static /* synthetic */ void lambda$glBindBuffer$77(int i, Supplier supplier) {
        GlStateManager._glBindBuffer(i, (Integer)supplier.get());
    }

    private static /* synthetic */ void lambda$glFramebufferRenderbuffer$76(int i, int j, int k, int l) {
        GlStateManager._glFramebufferRenderbuffer(i, j, k, l);
    }

    private static /* synthetic */ void lambda$glRenderbufferStorage$75(int i, int j, int k, int l) {
        GlStateManager._glRenderbufferStorage(i, j, k, l);
    }

    private static /* synthetic */ void lambda$glBindRenderbuffer$74(int i, int j) {
        GlStateManager._glBindRenderbuffer(i, j);
    }

    private static /* synthetic */ void lambda$glFramebufferTexture2D$73(int i, int j, int k, int l, int m) {
        GlStateManager._glFramebufferTexture2D(i, j, k, l, m);
    }

    private static /* synthetic */ void lambda$glDeleteFramebuffers$72(int i) {
        GlStateManager._glDeleteFramebuffers(i);
    }

    private static /* synthetic */ void lambda$glDeleteRenderbuffers$71(int i) {
        GlStateManager._glDeleteRenderbuffers(i);
    }

    private static /* synthetic */ void lambda$glBindFramebuffer$70(int i, int j) {
        GlStateManager._glBindFramebuffer(i, j);
    }

    private static /* synthetic */ void lambda$glMultiTexCoord2f$69(int i, float f, float g) {
        GlStateManager._glMultiTexCoord2f(i, f, g);
    }

    private static /* synthetic */ void lambda$renderCrosshair$68(int i) {
        GLX._renderCrosshair(i, true, true, true);
    }

    private static /* synthetic */ void lambda$glClientActiveTexture$67(int i) {
        GlStateManager._glClientActiveTexture(i);
    }

    private static /* synthetic */ void lambda$getString$66(int i, Consumer consumer) {
        String string = GlStateManager._getString(i);
        consumer.accept(string);
    }

    private static /* synthetic */ void lambda$getError$65(Consumer consumer) {
        int i = GlStateManager._getError();
        consumer.accept(i);
    }

    private static /* synthetic */ void lambda$readPixels$64(int i, int j, int k, int l, int m, int n, ByteBuffer byteBuffer) {
        GlStateManager._readPixels(i, j, k, l, m, n, byteBuffer);
    }

    private static /* synthetic */ void lambda$pixelTransfer$63(int i, float f) {
        GlStateManager._pixelTransfer(i, f);
    }

    private static /* synthetic */ void lambda$pixelStore$62(int i, int j) {
        GlStateManager._pixelStore(i, j);
    }

    private static /* synthetic */ void lambda$lineWidth$61(float f) {
        GlStateManager._lineWidth(f);
    }

    private static /* synthetic */ void lambda$drawArrays$60(int i, int j, int k) {
        GlStateManager._drawArrays(i, j, k);
    }

    private static /* synthetic */ void lambda$color3f$59(float f, float g, float h) {
        GlStateManager._color4f(f, g, h, 1.0f);
    }

    private static /* synthetic */ void lambda$color4f$58(float f, float g, float h, float i) {
        GlStateManager._color4f(f, g, h, i);
    }

    private static /* synthetic */ void lambda$multMatrix$57(Matrix4f matrix4f) {
        GlStateManager._multMatrix(matrix4f);
    }

    private static /* synthetic */ void lambda$multMatrix$56(FloatBuffer floatBuffer) {
        GlStateManager._multMatrix(floatBuffer);
    }

    private static /* synthetic */ void lambda$translated$55(double d, double e, double f) {
        GlStateManager._translated(d, e, f);
    }

    private static /* synthetic */ void lambda$translatef$54(float f, float g, float h) {
        GlStateManager._translatef(f, g, h);
    }

    private static /* synthetic */ void lambda$scaled$53(double d, double e, double f) {
        GlStateManager._scaled(d, e, f);
    }

    private static /* synthetic */ void lambda$scalef$52(float f, float g, float h) {
        GlStateManager._scalef(f, g, h);
    }

    private static /* synthetic */ void lambda$rotated$51(double d, double e, double f, double g) {
        GlStateManager._rotated(d, e, f, g);
    }

    private static /* synthetic */ void lambda$rotatef$50(float f, float g, float h, float i) {
        GlStateManager._rotatef(f, g, h, i);
    }

    private static /* synthetic */ void lambda$ortho$49(double d, double e, double f, double g, double h, double i) {
        GlStateManager._ortho(d, e, f, g, h, i);
    }

    private static /* synthetic */ void lambda$getMatrix4f$48(int i, Consumer consumer) {
        Matrix4f matrix4f = GlStateManager._getMatrix4f(i);
        consumer.accept(matrix4f);
    }

    private static /* synthetic */ void lambda$getMatrix$47(int i, FloatBuffer floatBuffer) {
        GlStateManager._getMatrix(i, floatBuffer);
    }

    private static /* synthetic */ void lambda$matrixMode$46(int i) {
        GlStateManager._matrixMode(i);
    }

    private static /* synthetic */ void lambda$clear$45(int i, boolean bl) {
        GlStateManager._clear(i, bl);
    }

    private static /* synthetic */ void lambda$clearStencil$44(int i) {
        GlStateManager._clearStencil(i);
    }

    private static /* synthetic */ void lambda$clearColor$43(float f, float g, float h, float i) {
        GlStateManager._clearColor(f, g, h, i);
    }

    private static /* synthetic */ void lambda$clearDepth$42(double d) {
        GlStateManager._clearDepth(d);
    }

    private static /* synthetic */ void lambda$stencilOp$41(int i, int j, int k) {
        GlStateManager._stencilOp(i, j, k);
    }

    private static /* synthetic */ void lambda$stencilMask$40(int i) {
        GlStateManager._stencilMask(i);
    }

    private static /* synthetic */ void lambda$stencilFunc$39(int i, int j, int k) {
        GlStateManager._stencilFunc(i, j, k);
    }

    private static /* synthetic */ void lambda$colorMask$38(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        GlStateManager._colorMask(bl, bl2, bl3, bl4);
    }

    private static /* synthetic */ void lambda$viewport$37(int i, int j, int k, int l) {
        GlStateManager._viewport(i, j, k, l);
    }

    private static /* synthetic */ void lambda$shadeModel$36(int i) {
        GlStateManager._shadeModel(i);
    }

    private static /* synthetic */ void lambda$getTexImage$35(int i, int j, int k, int l, long m) {
        GlStateManager._getTexImage(i, j, k, l, m);
    }

    private static /* synthetic */ void lambda$copyTexSubImage2D$34(int i, int j, int k, int l, int m, int n, int o, int p) {
        GlStateManager._copyTexSubImage2D(i, j, k, l, m, n, o, p);
    }

    private static /* synthetic */ void lambda$texSubImage2D$33(int i, int j, int k, int l, int m, int n, int o, int p, long q) {
        GlStateManager._texSubImage2D(i, j, k, l, m, n, o, p, q);
    }

    private static /* synthetic */ void lambda$texImage2D$32(int i, int j, int k, int l, int m, int n, int o, int p, IntBuffer intBuffer) {
        GlStateManager._texImage2D(i, j, k, l, m, n, o, p, intBuffer);
    }

    private static /* synthetic */ void lambda$bindTexture$31(int i) {
        GlStateManager._bindTexture(i);
    }

    private static /* synthetic */ void lambda$deleteTexture$30(int i) {
        GlStateManager._deleteTexture(i);
    }

    private static /* synthetic */ void lambda$texParameter$29(int i, int j, int k) {
        GlStateManager._texParameter(i, j, k);
    }

    private static /* synthetic */ void lambda$activeTexture$28(int i) {
        GlStateManager._activeTexture(i);
    }

    private static /* synthetic */ void lambda$texGenParam$27(GlStateManager.TexGen texGen, int i, FloatBuffer floatBuffer) {
        GlStateManager._texGenParam(texGen, i, floatBuffer);
    }

    private static /* synthetic */ void lambda$texGenMode$26(GlStateManager.TexGen texGen, int i) {
        GlStateManager._texGenMode(texGen, i);
    }

    private static /* synthetic */ void lambda$disableTexGen$25(GlStateManager.TexGen texGen) {
        GlStateManager._disableTexGen(texGen);
    }

    private static /* synthetic */ void lambda$enableTexGen$24(GlStateManager.TexGen texGen) {
        GlStateManager._enableTexGen(texGen);
    }

    private static /* synthetic */ void lambda$logicOp$23(int i) {
        GlStateManager._logicOp(i);
    }

    private static /* synthetic */ void lambda$logicOp$22(GlStateManager.LogicOp logicOp) {
        GlStateManager._logicOp(logicOp.value);
    }

    private static /* synthetic */ void lambda$polygonOffset$21(float f, float g) {
        GlStateManager._polygonOffset(f, g);
    }

    private static /* synthetic */ void lambda$polygonMode$20(int i, int j) {
        GlStateManager._polygonMode(i, j);
    }

    private static /* synthetic */ void lambda$cullFace$19(int i) {
        GlStateManager._cullFace(i);
    }

    private static /* synthetic */ void lambda$cullFace$18(GlStateManager.CullFace cullFace) {
        GlStateManager._cullFace(cullFace.value);
    }

    private static /* synthetic */ void lambda$fogi$17(int i, int j) {
        GlStateManager._fogi(i, j);
    }

    private static /* synthetic */ void lambda$fog$16(int i, FloatBuffer floatBuffer) {
        GlStateManager._fog(i, floatBuffer);
    }

    private static /* synthetic */ void lambda$fogEnd$15(float f) {
        GlStateManager._fogEnd(f);
    }

    private static /* synthetic */ void lambda$fogStart$14(float f) {
        GlStateManager._fogStart(f);
    }

    private static /* synthetic */ void lambda$fogDensity$13(float f) {
        GlStateManager._fogDensity(f);
    }

    private static /* synthetic */ void lambda$fogMode$12(int i) {
        GlStateManager._fogMode(i);
    }

    private static /* synthetic */ void lambda$fogMode$11(GlStateManager.FogMode fogMode) {
        GlStateManager._fogMode(fogMode.value);
    }

    private static /* synthetic */ void lambda$setupSolidRenderingTextureCombine$10(int i) {
        GlStateManager._setupSolidRenderingTextureCombine(i);
    }

    private static /* synthetic */ void lambda$blendEquation$9(int i) {
        GlStateManager._blendEquation(i);
    }

    private static /* synthetic */ void lambda$blendFuncSeparate$8(int i, int j, int k, int l) {
        GlStateManager._blendFuncSeparate(i, j, k, l);
    }

    private static /* synthetic */ void lambda$blendFuncSeparate$7(GlStateManager.SourceFactor sourceFactor, GlStateManager.DestFactor destFactor, GlStateManager.SourceFactor sourceFactor2, GlStateManager.DestFactor destFactor2) {
        GlStateManager._blendFuncSeparate(sourceFactor.value, destFactor.value, sourceFactor2.value, destFactor2.value);
    }

    private static /* synthetic */ void lambda$blendFunc$6(int i, int j) {
        GlStateManager._blendFunc(i, j);
    }

    private static /* synthetic */ void lambda$blendFunc$5(GlStateManager.SourceFactor sourceFactor, GlStateManager.DestFactor destFactor) {
        GlStateManager._blendFunc(sourceFactor.value, destFactor.value);
    }

    private static /* synthetic */ void lambda$depthMask$4(boolean bl) {
        GlStateManager._depthMask(bl);
    }

    private static /* synthetic */ void lambda$depthFunc$3(int i) {
        GlStateManager._depthFunc(i);
    }

    private static /* synthetic */ void lambda$normal3f$2(float f, float g, float h) {
        GlStateManager._normal3f(f, g, h);
    }

    private static /* synthetic */ void lambda$colorMaterial$1(int i, int j) {
        GlStateManager._colorMaterial(i, j);
    }

    private static /* synthetic */ void lambda$alphaFunc$0(int i, float f) {
        GlStateManager._alphaFunc(i, f);
    }

    static {
        MAX_SUPPORTED_TEXTURE_SIZE = -1;
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Profile {
        public static final Profile DEFAULT = new Profile(){

            @Override
            public void apply() {
                RenderSystem.setupDefaultGlState();
            }

            @Override
            public void clean() {
            }
        };
        public static final Profile PLAYER_SKIN = new Profile(){

            @Override
            public void apply() {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(770, 771, 1, 0);
            }

            @Override
            public void clean() {
                RenderSystem.disableBlend();
            }
        };
        public static final Profile TRANSPARENT_MODEL = new Profile(){

            @Override
            public void apply() {
                RenderSystem.color4f(1.0f, 1.0f, 1.0f, 0.15f);
                RenderSystem.depthMask(false);
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                RenderSystem.alphaFunc(516, 0.003921569f);
            }

            @Override
            public void clean() {
                RenderSystem.disableBlend();
                RenderSystem.defaultAlphaFunc();
                RenderSystem.depthMask(true);
            }
        };

        public void apply();

        public void clean();
    }
}

