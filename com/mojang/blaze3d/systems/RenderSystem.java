/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.systems;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;

@Environment(value=EnvType.CLIENT)
public class RenderSystem {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ConcurrentLinkedQueue<RenderCall> recordingQueue = Queues.newConcurrentLinkedQueue();
    private static final Tesselator RENDER_THREAD_TESSELATOR = new Tesselator();
    public static final float DEFAULTALPHACUTOFF = 0.1f;
    private static final int MINIMUM_ATLAS_TEXTURE_SIZE = 1024;
    private static boolean isReplayingQueue;
    private static Thread gameThread;
    private static Thread renderThread;
    private static int MAX_SUPPORTED_TEXTURE_SIZE;
    private static boolean isInInit;
    private static double lastDrawTime;
    private static final AutoStorageIndexBuffer sharedSequential;
    private static final AutoStorageIndexBuffer sharedSequentialQuad;

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

    public static void recordRenderCall(RenderCall renderCall) {
        recordingQueue.add(renderCall);
    }

    public static void flipFrame(long l) {
        GLFW.glfwPollEvents();
        RenderSystem.replayQueue();
        Tesselator.getInstance().getBuilder().clear();
        GLFW.glfwSwapBuffers(l);
        GLFW.glfwPollEvents();
    }

    public static void replayQueue() {
        isReplayingQueue = true;
        while (!recordingQueue.isEmpty()) {
            RenderCall renderCall = recordingQueue.poll();
            renderCall.execute();
        }
        isReplayingQueue = false;
    }

    public static void limitDisplayFPS(int i) {
        double d = lastDrawTime + 1.0 / (double)i;
        double e = GLFW.glfwGetTime();
        while (e < d) {
            GLFW.glfwWaitEventsTimeout(d - e);
            e = GLFW.glfwGetTime();
        }
        lastDrawTime = e;
    }

    @Deprecated
    public static void pushLightingAttributes() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._pushLightingAttributes();
    }

    @Deprecated
    public static void pushTextureAttributes() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._pushTextureAttributes();
    }

    @Deprecated
    public static void popAttributes() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._popAttributes();
    }

    @Deprecated
    public static void disableAlphaTest() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableAlphaTest();
    }

    @Deprecated
    public static void enableAlphaTest() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableAlphaTest();
    }

    @Deprecated
    public static void alphaFunc(int i, float f) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._alphaFunc(i, f);
    }

    @Deprecated
    public static void enableLighting() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableLighting();
    }

    @Deprecated
    public static void disableLighting() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableLighting();
    }

    @Deprecated
    public static void enableColorMaterial() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableColorMaterial();
    }

    @Deprecated
    public static void disableColorMaterial() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableColorMaterial();
    }

    @Deprecated
    public static void colorMaterial(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._colorMaterial(i, j);
    }

    @Deprecated
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

    public static void enableScissor(int i, int j, int k, int l) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._enableScissorTest();
        GlStateManager._scissorBox(i, j, k, l);
    }

    public static void disableScissor() {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._disableScissorTest();
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

    public static void blendColor(float f, float g, float h, float i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._blendColor(f, g, h, i);
    }

    @Deprecated
    public static void enableFog() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableFog();
    }

    @Deprecated
    public static void disableFog() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._disableFog();
    }

    @Deprecated
    public static void fogMode(GlStateManager.FogMode fogMode) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fogMode(fogMode.value);
    }

    @Deprecated
    public static void fogMode(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fogMode(i);
    }

    @Deprecated
    public static void fogDensity(float f) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fogDensity(f);
    }

    @Deprecated
    public static void fogStart(float f) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fogStart(f);
    }

    @Deprecated
    public static void fogEnd(float f) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fogEnd(f);
    }

    @Deprecated
    public static void fog(int i, float f, float g, float h, float j) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._fog(i, new float[]{f, g, h, j});
    }

    @Deprecated
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

    @Deprecated
    public static void shadeModel(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._shadeModel(i);
    }

    @Deprecated
    public static void enableRescaleNormal() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._enableRescaleNormal();
    }

    @Deprecated
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

    @Deprecated
    public static void matrixMode(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._matrixMode(i);
    }

    @Deprecated
    public static void loadIdentity() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._loadIdentity();
    }

    @Deprecated
    public static void pushMatrix() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._pushMatrix();
    }

    @Deprecated
    public static void popMatrix() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._popMatrix();
    }

    @Deprecated
    public static void ortho(double d, double e, double f, double g, double h, double i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._ortho(d, e, f, g, h, i);
    }

    @Deprecated
    public static void rotatef(float f, float g, float h, float i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._rotatef(f, g, h, i);
    }

    @Deprecated
    public static void scalef(float f, float g, float h) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._scalef(f, g, h);
    }

    @Deprecated
    public static void scaled(double d, double e, double f) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._scaled(d, e, f);
    }

    @Deprecated
    public static void translatef(float f, float g, float h) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._translatef(f, g, h);
    }

    @Deprecated
    public static void translated(double d, double e, double f) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._translated(d, e, f);
    }

    @Deprecated
    public static void multMatrix(Matrix4f matrix4f) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._multMatrix(matrix4f);
    }

    @Deprecated
    public static void color4f(float f, float g, float h, float i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._color4f(f, g, h, i);
    }

    @Deprecated
    public static void color3f(float f, float g, float h) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._color4f(f, g, h, 1.0f);
    }

    @Deprecated
    public static void clearCurrentColor() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._clearCurrentColor();
    }

    public static void drawElements(int i, int j, int k) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._drawElements(i, j, k, 0L);
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

    public static void renderCrosshair(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GLX._renderCrosshair(i, true, true, true);
    }

    public static void setupNvFogDistance() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GLX._setupNvFogDistance();
    }

    @Deprecated
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
        GlStateManager._matrixMode(5889);
        GlStateManager._loadIdentity();
        GlStateManager._matrixMode(5888);
        GlStateManager._viewport(i, j, k, l);
    }

    public static int maxSupportedTextureSize() {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        if (MAX_SUPPORTED_TEXTURE_SIZE == -1) {
            int i = GlStateManager._getInteger(3379);
            for (int j = Math.max(32768, i); j >= 1024; j >>= 1) {
                GlStateManager._texImage2D(32868, 0, 6408, j, j, 0, 6408, 5121, null);
                int k = GlStateManager._getTexLevelParameter(32868, 0, 4096);
                if (k == 0) continue;
                MAX_SUPPORTED_TEXTURE_SIZE = j;
                return j;
            }
            MAX_SUPPORTED_TEXTURE_SIZE = Math.max(i, 1024);
            LOGGER.info("Failed to determine maximum texture size by probing, trying GL_MAX_TEXTURE_SIZE = {}", (Object)MAX_SUPPORTED_TEXTURE_SIZE);
        }
        return MAX_SUPPORTED_TEXTURE_SIZE;
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

    public static void setupOutline() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager.setupOutline();
    }

    public static void teardownOutline() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager.teardownOutline();
    }

    public static void setupOverlayColor(IntSupplier intSupplier, int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager.setupOverlayColor(intSupplier.getAsInt(), i);
    }

    public static void teardownOverlayColor() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager.teardownOverlayColor();
    }

    public static void setupLevelDiffuseLighting(Vector3f vector3f, Vector3f vector3f2, Matrix4f matrix4f) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager.setupLevelDiffuseLighting(vector3f, vector3f2, matrix4f);
    }

    public static void setupGuiFlatDiffuseLighting(Vector3f vector3f, Vector3f vector3f2) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager.setupGuiFlatDiffuseLighting(vector3f, vector3f2);
    }

    public static void setupGui3DDiffuseLighting(Vector3f vector3f, Vector3f vector3f2) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager.setupGui3DDiffuseLighting(vector3f, vector3f2);
    }

    public static void mulTextureByProjModelView() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager.mulTextureByProjModelView();
    }

    public static void setupEndPortalTexGen() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager.setupEndPortalTexGen();
    }

    public static void clearTexGen() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager.clearTexGen();
    }

    public static void beginInitialization() {
        isInInit = true;
    }

    public static void finishInitialization() {
        isInInit = false;
        if (!recordingQueue.isEmpty()) {
            RenderSystem.replayQueue();
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

    @Deprecated
    public static void runAsFancy(Runnable runnable) {
        boolean bl = Minecraft.useShaderTransparency();
        if (!bl) {
            runnable.run();
            return;
        }
        Options options = Minecraft.getInstance().options;
        GraphicsStatus graphicsStatus = options.graphicsMode;
        options.graphicsMode = GraphicsStatus.FANCY;
        runnable.run();
        options.graphicsMode = graphicsStatus;
    }

    public static AutoStorageIndexBuffer getSequentialBuffer(VertexFormat.Mode mode, int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        AutoStorageIndexBuffer autoStorageIndexBuffer = mode == VertexFormat.Mode.QUADS ? sharedSequentialQuad : sharedSequential;
        autoStorageIndexBuffer.ensureStorage(i);
        return autoStorageIndexBuffer;
    }

    private static /* synthetic */ void lambda$setupGui3DDiffuseLighting$72(Vector3f vector3f, Vector3f vector3f2) {
        GlStateManager.setupGui3DDiffuseLighting(vector3f, vector3f2);
    }

    private static /* synthetic */ void lambda$setupGuiFlatDiffuseLighting$71(Vector3f vector3f, Vector3f vector3f2) {
        GlStateManager.setupGuiFlatDiffuseLighting(vector3f, vector3f2);
    }

    private static /* synthetic */ void lambda$setupLevelDiffuseLighting$70(Vector3f vector3f, Vector3f vector3f2, Matrix4f matrix4f) {
        GlStateManager.setupLevelDiffuseLighting(vector3f, vector3f2, matrix4f);
    }

    private static /* synthetic */ void lambda$setupOverlayColor$69(IntSupplier intSupplier, int i) {
        GlStateManager.setupOverlayColor(intSupplier.getAsInt(), i);
    }

    private static /* synthetic */ void lambda$glUniformMatrix4$68(int i, boolean bl, FloatBuffer floatBuffer) {
        GlStateManager._glUniformMatrix4(i, bl, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniformMatrix3$67(int i, boolean bl, FloatBuffer floatBuffer) {
        GlStateManager._glUniformMatrix3(i, bl, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniformMatrix2$66(int i, boolean bl, FloatBuffer floatBuffer) {
        GlStateManager._glUniformMatrix2(i, bl, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform4$65(int i, FloatBuffer floatBuffer) {
        GlStateManager._glUniform4(i, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform3$64(int i, FloatBuffer floatBuffer) {
        GlStateManager._glUniform3(i, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform2$63(int i, FloatBuffer floatBuffer) {
        GlStateManager._glUniform2(i, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform1$62(int i, FloatBuffer floatBuffer) {
        GlStateManager._glUniform1(i, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform4$61(int i, IntBuffer intBuffer) {
        GlStateManager._glUniform4(i, intBuffer);
    }

    private static /* synthetic */ void lambda$glUniform3$60(int i, IntBuffer intBuffer) {
        GlStateManager._glUniform3(i, intBuffer);
    }

    private static /* synthetic */ void lambda$glUniform2$59(int i, IntBuffer intBuffer) {
        GlStateManager._glUniform2(i, intBuffer);
    }

    private static /* synthetic */ void lambda$glUniform1$58(int i, IntBuffer intBuffer) {
        GlStateManager._glUniform1(i, intBuffer);
    }

    private static /* synthetic */ void lambda$glUniform1i$57(int i, int j) {
        GlStateManager._glUniform1i(i, j);
    }

    private static /* synthetic */ void lambda$glDeleteBuffers$56(int i) {
        GlStateManager._glDeleteBuffers(i);
    }

    private static /* synthetic */ void lambda$glBindBuffer$55(int i, Supplier supplier) {
        GlStateManager._glBindBuffer(i, (Integer)supplier.get());
    }

    private static /* synthetic */ void lambda$glMultiTexCoord2f$54(int i, float f, float g) {
        GlStateManager._glMultiTexCoord2f(i, f, g);
    }

    private static /* synthetic */ void lambda$renderCrosshair$53(int i) {
        GLX._renderCrosshair(i, true, true, true);
    }

    private static /* synthetic */ void lambda$getString$52(int i, Consumer consumer) {
        String string = GlStateManager._getString(i);
        consumer.accept(string);
    }

    private static /* synthetic */ void lambda$readPixels$51(int i, int j, int k, int l, int m, int n, ByteBuffer byteBuffer) {
        GlStateManager._readPixels(i, j, k, l, m, n, byteBuffer);
    }

    private static /* synthetic */ void lambda$pixelTransfer$50(int i, float f) {
        GlStateManager._pixelTransfer(i, f);
    }

    private static /* synthetic */ void lambda$pixelStore$49(int i, int j) {
        GlStateManager._pixelStore(i, j);
    }

    private static /* synthetic */ void lambda$lineWidth$48(float f) {
        GlStateManager._lineWidth(f);
    }

    private static /* synthetic */ void lambda$drawElements$47(int i, int j, int k) {
        GlStateManager._drawElements(i, j, k, 0L);
    }

    private static /* synthetic */ void lambda$color3f$46(float f, float g, float h) {
        GlStateManager._color4f(f, g, h, 1.0f);
    }

    private static /* synthetic */ void lambda$color4f$45(float f, float g, float h, float i) {
        GlStateManager._color4f(f, g, h, i);
    }

    private static /* synthetic */ void lambda$multMatrix$44(Matrix4f matrix4f) {
        GlStateManager._multMatrix(matrix4f);
    }

    private static /* synthetic */ void lambda$translated$43(double d, double e, double f) {
        GlStateManager._translated(d, e, f);
    }

    private static /* synthetic */ void lambda$translatef$42(float f, float g, float h) {
        GlStateManager._translatef(f, g, h);
    }

    private static /* synthetic */ void lambda$scaled$41(double d, double e, double f) {
        GlStateManager._scaled(d, e, f);
    }

    private static /* synthetic */ void lambda$scalef$40(float f, float g, float h) {
        GlStateManager._scalef(f, g, h);
    }

    private static /* synthetic */ void lambda$rotatef$39(float f, float g, float h, float i) {
        GlStateManager._rotatef(f, g, h, i);
    }

    private static /* synthetic */ void lambda$ortho$38(double d, double e, double f, double g, double h, double i) {
        GlStateManager._ortho(d, e, f, g, h, i);
    }

    private static /* synthetic */ void lambda$matrixMode$37(int i) {
        GlStateManager._matrixMode(i);
    }

    private static /* synthetic */ void lambda$clear$36(int i, boolean bl) {
        GlStateManager._clear(i, bl);
    }

    private static /* synthetic */ void lambda$clearStencil$35(int i) {
        GlStateManager._clearStencil(i);
    }

    private static /* synthetic */ void lambda$clearColor$34(float f, float g, float h, float i) {
        GlStateManager._clearColor(f, g, h, i);
    }

    private static /* synthetic */ void lambda$clearDepth$33(double d) {
        GlStateManager._clearDepth(d);
    }

    private static /* synthetic */ void lambda$stencilOp$32(int i, int j, int k) {
        GlStateManager._stencilOp(i, j, k);
    }

    private static /* synthetic */ void lambda$stencilMask$31(int i) {
        GlStateManager._stencilMask(i);
    }

    private static /* synthetic */ void lambda$stencilFunc$30(int i, int j, int k) {
        GlStateManager._stencilFunc(i, j, k);
    }

    private static /* synthetic */ void lambda$colorMask$29(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        GlStateManager._colorMask(bl, bl2, bl3, bl4);
    }

    private static /* synthetic */ void lambda$viewport$28(int i, int j, int k, int l) {
        GlStateManager._viewport(i, j, k, l);
    }

    private static /* synthetic */ void lambda$shadeModel$27(int i) {
        GlStateManager._shadeModel(i);
    }

    private static /* synthetic */ void lambda$bindTexture$26(int i) {
        GlStateManager._bindTexture(i);
    }

    private static /* synthetic */ void lambda$deleteTexture$25(int i) {
        GlStateManager._deleteTexture(i);
    }

    private static /* synthetic */ void lambda$texParameter$24(int i, int j, int k) {
        GlStateManager._texParameter(i, j, k);
    }

    private static /* synthetic */ void lambda$activeTexture$23(int i) {
        GlStateManager._activeTexture(i);
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

    private static /* synthetic */ void lambda$fogi$19(int i, int j) {
        GlStateManager._fogi(i, j);
    }

    private static /* synthetic */ void lambda$fog$18(int i, float f, float g, float h, float j) {
        GlStateManager._fog(i, new float[]{f, g, h, j});
    }

    private static /* synthetic */ void lambda$fogEnd$17(float f) {
        GlStateManager._fogEnd(f);
    }

    private static /* synthetic */ void lambda$fogStart$16(float f) {
        GlStateManager._fogStart(f);
    }

    private static /* synthetic */ void lambda$fogDensity$15(float f) {
        GlStateManager._fogDensity(f);
    }

    private static /* synthetic */ void lambda$fogMode$14(int i) {
        GlStateManager._fogMode(i);
    }

    private static /* synthetic */ void lambda$fogMode$13(GlStateManager.FogMode fogMode) {
        GlStateManager._fogMode(fogMode.value);
    }

    private static /* synthetic */ void lambda$blendColor$12(float f, float g, float h, float i) {
        GlStateManager._blendColor(f, g, h, i);
    }

    private static /* synthetic */ void lambda$blendEquation$11(int i) {
        GlStateManager._blendEquation(i);
    }

    private static /* synthetic */ void lambda$blendFuncSeparate$10(int i, int j, int k, int l) {
        GlStateManager._blendFuncSeparate(i, j, k, l);
    }

    private static /* synthetic */ void lambda$blendFuncSeparate$9(GlStateManager.SourceFactor sourceFactor, GlStateManager.DestFactor destFactor, GlStateManager.SourceFactor sourceFactor2, GlStateManager.DestFactor destFactor2) {
        GlStateManager._blendFuncSeparate(sourceFactor.value, destFactor.value, sourceFactor2.value, destFactor2.value);
    }

    private static /* synthetic */ void lambda$blendFunc$8(int i, int j) {
        GlStateManager._blendFunc(i, j);
    }

    private static /* synthetic */ void lambda$blendFunc$7(GlStateManager.SourceFactor sourceFactor, GlStateManager.DestFactor destFactor) {
        GlStateManager._blendFunc(sourceFactor.value, destFactor.value);
    }

    private static /* synthetic */ void lambda$depthMask$6(boolean bl) {
        GlStateManager._depthMask(bl);
    }

    private static /* synthetic */ void lambda$depthFunc$5(int i) {
        GlStateManager._depthFunc(i);
    }

    private static /* synthetic */ void lambda$enableScissor$4(int i, int j, int k, int l) {
        GlStateManager._enableScissorTest();
        GlStateManager._scissorBox(i, j, k, l);
    }

    private static /* synthetic */ void lambda$normal3f$3(float f, float g, float h) {
        GlStateManager._normal3f(f, g, h);
    }

    private static /* synthetic */ void lambda$colorMaterial$2(int i, int j) {
        GlStateManager._colorMaterial(i, j);
    }

    private static /* synthetic */ void lambda$alphaFunc$1(int i, float f) {
        GlStateManager._alphaFunc(i, f);
    }

    static {
        MAX_SUPPORTED_TEXTURE_SIZE = -1;
        lastDrawTime = Double.MIN_VALUE;
        sharedSequential = new AutoStorageIndexBuffer(1, 1, IntConsumer::accept);
        sharedSequentialQuad = new AutoStorageIndexBuffer(4, 6, (intConsumer, i) -> {
            intConsumer.accept(i + 0);
            intConsumer.accept(i + 1);
            intConsumer.accept(i + 2);
            intConsumer.accept(i + 2);
            intConsumer.accept(i + 3);
            intConsumer.accept(i + 0);
        });
    }

    @Environment(value=EnvType.CLIENT)
    public static final class AutoStorageIndexBuffer {
        private final int vertexStride;
        private final int indexStride;
        private final IndexGenerator generator;
        private int name;
        private VertexFormat.IndexType type = VertexFormat.IndexType.BYTE;
        private int indexCount;

        private AutoStorageIndexBuffer(int i, int j, IndexGenerator indexGenerator) {
            this.vertexStride = i;
            this.indexStride = j;
            this.generator = indexGenerator;
        }

        private void ensureStorage(int i) {
            if (i <= this.indexCount) {
                return;
            }
            LOGGER.debug("Growing IndexBuffer: Old limit {}, new limit {}.", (Object)this.indexCount, (Object)i);
            if (this.name == 0) {
                this.name = GlStateManager._glGenBuffers();
            }
            VertexFormat.IndexType indexType = VertexFormat.IndexType.least(i);
            int j = Mth.roundToward(i * indexType.bytes, 4);
            GlStateManager._glBindBuffer(34963, this.name);
            GlStateManager._glBufferData(34963, j, 35044);
            ByteBuffer byteBuffer = GlStateManager._glMapBuffer(34963, 35001);
            if (byteBuffer == null) {
                throw new RuntimeException("Failed to map GL buffer");
            }
            this.type = indexType;
            it.unimi.dsi.fastutil.ints.IntConsumer intConsumer = this.intConsumer(byteBuffer);
            for (int k = 0; k < i; k += this.indexStride) {
                this.generator.accept(intConsumer, k * this.vertexStride / this.indexStride);
            }
            GlStateManager._glUnmapBuffer(34963);
            GlStateManager._glBindBuffer(34963, 0);
            this.indexCount = i;
        }

        private it.unimi.dsi.fastutil.ints.IntConsumer intConsumer(ByteBuffer byteBuffer) {
            switch (this.type) {
                case BYTE: {
                    return i -> byteBuffer.put((byte)i);
                }
                case SHORT: {
                    return i -> byteBuffer.putShort((short)i);
                }
            }
            return byteBuffer::putInt;
        }

        public int name() {
            return this.name;
        }

        public VertexFormat.IndexType type() {
            return this.type;
        }

        @Environment(value=EnvType.CLIENT)
        static interface IndexGenerator {
            public void accept(it.unimi.dsi.fastutil.ints.IntConsumer var1, int var2);
        }
    }
}

