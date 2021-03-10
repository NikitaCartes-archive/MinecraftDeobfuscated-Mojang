/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import it.unimi.dsi.fastutil.ints.IntConsumer;
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
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
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
    private static final int MINIMUM_ATLAS_TEXTURE_SIZE = 1024;
    private static boolean isReplayingQueue;
    @Nullable
    private static Thread gameThread;
    @Nullable
    private static Thread renderThread;
    private static int MAX_SUPPORTED_TEXTURE_SIZE;
    private static boolean isInInit;
    private static double lastDrawTime;
    private static final AutoStorageIndexBuffer sharedSequential;
    private static final AutoStorageIndexBuffer sharedSequentialQuad;
    private static final AutoStorageIndexBuffer sharedSequentialLines;
    private static Matrix4f projectionMatrix;
    private static Matrix4f savedProjectionMatrix;
    private static PoseStack modelViewStack;
    private static Matrix4f modelViewMatrix;
    private static Matrix4f textureMatrix;
    private static final int[] shaderTextures;
    private static final float[] shaderColor;
    private static float shaderFogStart;
    private static float shaderFogEnd;
    private static final float[] shaderFogColor;
    private static final Vector3f[] shaderLightDirections;
    private static float shaderGameTime;
    private static float shaderLineWidth;
    @Nullable
    private static ShaderInstance shader;

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

    public static void bindTextureForSetup(int i) {
        RenderSystem.bindTexture(i);
    }

    public static void bindTexture(int i) {
        GlStateManager._bindTexture(i);
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

    public static void setShaderFogStart(float f) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        RenderSystem._setShaderFogStart(f);
    }

    private static void _setShaderFogStart(float f) {
        shaderFogStart = f;
    }

    public static float getShaderFogStart() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return shaderFogStart;
    }

    public static void setShaderFogEnd(float f) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        RenderSystem._setShaderFogEnd(f);
    }

    private static void _setShaderFogEnd(float f) {
        shaderFogEnd = f;
    }

    public static float getShaderFogEnd() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return shaderFogEnd;
    }

    public static void setShaderFogColor(float f, float g, float h, float i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        RenderSystem._setShaderFogColor(f, g, h, i);
    }

    public static void setShaderFogColor(float f, float g, float h) {
        RenderSystem.setShaderFogColor(f, g, h, 1.0f);
    }

    private static void _setShaderFogColor(float f, float g, float h, float i) {
        RenderSystem.shaderFogColor[0] = f;
        RenderSystem.shaderFogColor[1] = g;
        RenderSystem.shaderFogColor[2] = h;
        RenderSystem.shaderFogColor[3] = i;
    }

    public static float[] getShaderFogColor() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return shaderFogColor;
    }

    public static void setShaderLights(Vector3f vector3f, Vector3f vector3f2) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        RenderSystem._setShaderLights(vector3f, vector3f2);
    }

    public static void _setShaderLights(Vector3f vector3f, Vector3f vector3f2) {
        RenderSystem.shaderLightDirections[0] = vector3f;
        RenderSystem.shaderLightDirections[1] = vector3f2;
    }

    public static void setupShaderLights(ShaderInstance shaderInstance) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (shaderInstance.LIGHT0_DIRECTION != null) {
            shaderInstance.LIGHT0_DIRECTION.set(shaderLightDirections[0]);
        }
        if (shaderInstance.LIGHT1_DIRECTION != null) {
            shaderInstance.LIGHT1_DIRECTION.set(shaderLightDirections[1]);
        }
    }

    public static void setShaderColor(float f, float g, float h, float i) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> RenderSystem._setShaderColor(f, g, h, i));
        } else {
            RenderSystem._setShaderColor(f, g, h, i);
        }
    }

    public static void setShaderColor(float f, float g, float h) {
        RenderSystem.setShaderColor(f, g, h, 1.0f);
    }

    private static void _setShaderColor(float f, float g, float h, float i) {
        RenderSystem.shaderColor[0] = f;
        RenderSystem.shaderColor[1] = g;
        RenderSystem.shaderColor[2] = h;
        RenderSystem.shaderColor[3] = i;
    }

    public static float[] getShaderColor() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return shaderColor;
    }

    public static void drawElements(int i, int j, int k) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._drawElements(i, j, k, 0L);
    }

    public static void lineWidth(float f) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                shaderLineWidth = f;
            });
        } else {
            shaderLineWidth = f;
        }
    }

    public static float getShaderLineWidth() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return shaderLineWidth;
    }

    public static void pixelStore(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnGameThreadOrInit);
        GlStateManager._pixelStore(i, j);
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

    public static String getCapsString() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        return "Using framebuffer using OpenGL 3.2";
    }

    public static void setupDefaultState(int i, int j, int k, int l) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
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

    public static void glBindBuffer(int i, IntSupplier intSupplier) {
        GlStateManager._glBindBuffer(i, intSupplier.getAsInt());
    }

    public static void glBindVertexArray(Supplier<Integer> supplier) {
        GlStateManager._glBindVertexArray(supplier.get());
    }

    public static void glBufferData(int i, ByteBuffer byteBuffer, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager._glBufferData(i, byteBuffer, j);
    }

    public static void glDeleteBuffers(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glDeleteBuffers(i);
    }

    public static void glDeleteVertexArrays(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._glDeleteVertexArrays(i);
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

    public static void setupOverlayColor(IntSupplier intSupplier, int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        int j = intSupplier.getAsInt();
        RenderSystem.setShaderTexture(1, j);
    }

    public static void teardownOverlayColor() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        RenderSystem.setShaderTexture(1, 0);
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

    public static void glGenVertexArrays(Consumer<Integer> consumer) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> consumer.accept(GlStateManager._glGenVertexArrays()));
        } else {
            consumer.accept(GlStateManager._glGenVertexArrays());
        }
    }

    public static Tesselator renderThreadTesselator() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return RENDER_THREAD_TESSELATOR;
    }

    public static void defaultBlendFunc() {
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
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

    public static void setShader(Supplier<ShaderInstance> supplier) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                shader = (ShaderInstance)supplier.get();
            });
        } else {
            shader = supplier.get();
        }
    }

    @Nullable
    public static ShaderInstance getShader() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return shader;
    }

    public static int getTextureId(int i) {
        return GlStateManager._getTextureId(i);
    }

    public static void setShaderTexture(int i, ResourceLocation resourceLocation) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> RenderSystem._setShaderTexture(i, resourceLocation));
        } else {
            RenderSystem._setShaderTexture(i, resourceLocation);
        }
    }

    public static void _setShaderTexture(int i, ResourceLocation resourceLocation) {
        if (i >= 0 && i < shaderTextures.length) {
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            AbstractTexture abstractTexture = textureManager.getTexture(resourceLocation);
            RenderSystem.shaderTextures[i] = abstractTexture.getId();
        }
    }

    public static void setShaderTexture(int i, int j) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> RenderSystem._setShaderTexture(i, j));
        } else {
            RenderSystem._setShaderTexture(i, j);
        }
    }

    public static void _setShaderTexture(int i, int j) {
        if (i >= 0 && i < shaderTextures.length) {
            RenderSystem.shaderTextures[i] = j;
        }
    }

    public static int getShaderTexture(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (i >= 0 && i < shaderTextures.length) {
            return shaderTextures[i];
        }
        return 0;
    }

    public static void setProjectionMatrix(Matrix4f matrix4f) {
        Matrix4f matrix4f2 = matrix4f.copy();
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                projectionMatrix = matrix4f2;
            });
        } else {
            projectionMatrix = matrix4f2;
        }
    }

    public static void setTextureMatrix(Matrix4f matrix4f) {
        Matrix4f matrix4f2 = matrix4f.copy();
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                textureMatrix = matrix4f2;
            });
        } else {
            textureMatrix = matrix4f2;
        }
    }

    public static void resetTextureMatrix() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> textureMatrix.setIdentity());
        } else {
            textureMatrix.setIdentity();
        }
    }

    public static void applyModelViewMatrix() {
        Matrix4f matrix4f = modelViewStack.last().pose().copy();
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                modelViewMatrix = matrix4f;
            });
        } else {
            modelViewMatrix = matrix4f;
        }
    }

    public static void backupProjectionMatrix() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> RenderSystem._backupProjectionMatrix());
        } else {
            RenderSystem._backupProjectionMatrix();
        }
    }

    private static void _backupProjectionMatrix() {
        savedProjectionMatrix = projectionMatrix;
    }

    public static void restoreProjectionMatrix() {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> RenderSystem._restoreProjectionMatrix());
        } else {
            RenderSystem._restoreProjectionMatrix();
        }
    }

    private static void _restoreProjectionMatrix() {
        projectionMatrix = savedProjectionMatrix;
    }

    public static Matrix4f getProjectionMatrix() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return projectionMatrix;
    }

    public static Matrix4f getModelViewMatrix() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return modelViewMatrix;
    }

    public static PoseStack getModelViewStack() {
        return modelViewStack;
    }

    public static Matrix4f getTextureMatrix() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return textureMatrix;
    }

    public static AutoStorageIndexBuffer getSequentialBuffer(VertexFormat.Mode mode, int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        AutoStorageIndexBuffer autoStorageIndexBuffer = mode == VertexFormat.Mode.QUADS ? sharedSequentialQuad : (mode == VertexFormat.Mode.LINES ? sharedSequentialLines : sharedSequential);
        autoStorageIndexBuffer.ensureStorage(i);
        return autoStorageIndexBuffer;
    }

    public static void setShaderGameTime(long l, float f) {
        float g = ((float)l + f) / 24000.0f;
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                shaderGameTime = g;
            });
        } else {
            shaderGameTime = g;
        }
    }

    public static float getShaderGameTime() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return shaderGameTime;
    }

    private static /* synthetic */ void lambda$setupGui3DDiffuseLighting$57(Vector3f vector3f, Vector3f vector3f2) {
        GlStateManager.setupGui3DDiffuseLighting(vector3f, vector3f2);
    }

    private static /* synthetic */ void lambda$setupGuiFlatDiffuseLighting$56(Vector3f vector3f, Vector3f vector3f2) {
        GlStateManager.setupGuiFlatDiffuseLighting(vector3f, vector3f2);
    }

    private static /* synthetic */ void lambda$setupLevelDiffuseLighting$55(Vector3f vector3f, Vector3f vector3f2, Matrix4f matrix4f) {
        GlStateManager.setupLevelDiffuseLighting(vector3f, vector3f2, matrix4f);
    }

    private static /* synthetic */ void lambda$teardownOverlayColor$54() {
        RenderSystem.setShaderTexture(1, 0);
    }

    private static /* synthetic */ void lambda$setupOverlayColor$53(IntSupplier intSupplier) {
        int i = intSupplier.getAsInt();
        RenderSystem.setShaderTexture(1, i);
    }

    private static /* synthetic */ void lambda$glUniformMatrix4$52(int i, boolean bl, FloatBuffer floatBuffer) {
        GlStateManager._glUniformMatrix4(i, bl, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniformMatrix3$51(int i, boolean bl, FloatBuffer floatBuffer) {
        GlStateManager._glUniformMatrix3(i, bl, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniformMatrix2$50(int i, boolean bl, FloatBuffer floatBuffer) {
        GlStateManager._glUniformMatrix2(i, bl, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform4$49(int i, FloatBuffer floatBuffer) {
        GlStateManager._glUniform4(i, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform3$48(int i, FloatBuffer floatBuffer) {
        GlStateManager._glUniform3(i, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform2$47(int i, FloatBuffer floatBuffer) {
        GlStateManager._glUniform2(i, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform1$46(int i, FloatBuffer floatBuffer) {
        GlStateManager._glUniform1(i, floatBuffer);
    }

    private static /* synthetic */ void lambda$glUniform4$45(int i, IntBuffer intBuffer) {
        GlStateManager._glUniform4(i, intBuffer);
    }

    private static /* synthetic */ void lambda$glUniform3$44(int i, IntBuffer intBuffer) {
        GlStateManager._glUniform3(i, intBuffer);
    }

    private static /* synthetic */ void lambda$glUniform2$43(int i, IntBuffer intBuffer) {
        GlStateManager._glUniform2(i, intBuffer);
    }

    private static /* synthetic */ void lambda$glUniform1$42(int i, IntBuffer intBuffer) {
        GlStateManager._glUniform1(i, intBuffer);
    }

    private static /* synthetic */ void lambda$glUniform1i$41(int i, int j) {
        GlStateManager._glUniform1i(i, j);
    }

    private static /* synthetic */ void lambda$glDeleteVertexArrays$40(int i) {
        GlStateManager._glDeleteVertexArrays(i);
    }

    private static /* synthetic */ void lambda$glDeleteBuffers$39(int i) {
        GlStateManager._glDeleteBuffers(i);
    }

    private static /* synthetic */ void lambda$glBindVertexArray$38(Supplier supplier) {
        GlStateManager._glBindVertexArray((Integer)supplier.get());
    }

    private static /* synthetic */ void lambda$glBindBuffer$37(int i, IntSupplier intSupplier) {
        GlStateManager._glBindBuffer(i, intSupplier.getAsInt());
    }

    private static /* synthetic */ void lambda$renderCrosshair$36(int i) {
        GLX._renderCrosshair(i, true, true, true);
    }

    private static /* synthetic */ void lambda$getString$35(int i, Consumer consumer) {
        String string = GlStateManager._getString(i);
        consumer.accept(string);
    }

    private static /* synthetic */ void lambda$readPixels$34(int i, int j, int k, int l, int m, int n, ByteBuffer byteBuffer) {
        GlStateManager._readPixels(i, j, k, l, m, n, byteBuffer);
    }

    private static /* synthetic */ void lambda$pixelStore$33(int i, int j) {
        GlStateManager._pixelStore(i, j);
    }

    private static /* synthetic */ void lambda$drawElements$31(int i, int j, int k) {
        GlStateManager._drawElements(i, j, k, 0L);
    }

    private static /* synthetic */ void lambda$setShaderLights$29(Vector3f vector3f, Vector3f vector3f2) {
        RenderSystem._setShaderLights(vector3f, vector3f2);
    }

    private static /* synthetic */ void lambda$setShaderFogColor$28(float f, float g, float h, float i) {
        RenderSystem._setShaderFogColor(f, g, h, i);
    }

    private static /* synthetic */ void lambda$setShaderFogEnd$27(float f) {
        RenderSystem._setShaderFogEnd(f);
    }

    private static /* synthetic */ void lambda$setShaderFogStart$26(float f) {
        RenderSystem._setShaderFogStart(f);
    }

    private static /* synthetic */ void lambda$clear$25(int i, boolean bl) {
        GlStateManager._clear(i, bl);
    }

    private static /* synthetic */ void lambda$clearStencil$24(int i) {
        GlStateManager._clearStencil(i);
    }

    private static /* synthetic */ void lambda$clearColor$23(float f, float g, float h, float i) {
        GlStateManager._clearColor(f, g, h, i);
    }

    private static /* synthetic */ void lambda$clearDepth$22(double d) {
        GlStateManager._clearDepth(d);
    }

    private static /* synthetic */ void lambda$stencilOp$21(int i, int j, int k) {
        GlStateManager._stencilOp(i, j, k);
    }

    private static /* synthetic */ void lambda$stencilMask$20(int i) {
        GlStateManager._stencilMask(i);
    }

    private static /* synthetic */ void lambda$stencilFunc$19(int i, int j, int k) {
        GlStateManager._stencilFunc(i, j, k);
    }

    private static /* synthetic */ void lambda$colorMask$18(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        GlStateManager._colorMask(bl, bl2, bl3, bl4);
    }

    private static /* synthetic */ void lambda$viewport$17(int i, int j, int k, int l) {
        GlStateManager._viewport(i, j, k, l);
    }

    private static /* synthetic */ void lambda$bindTexture$16(int i) {
        GlStateManager._bindTexture(i);
    }

    private static /* synthetic */ void lambda$deleteTexture$15(int i) {
        GlStateManager._deleteTexture(i);
    }

    private static /* synthetic */ void lambda$texParameter$14(int i, int j, int k) {
        GlStateManager._texParameter(i, j, k);
    }

    private static /* synthetic */ void lambda$activeTexture$13(int i) {
        GlStateManager._activeTexture(i);
    }

    private static /* synthetic */ void lambda$logicOp$12(GlStateManager.LogicOp logicOp) {
        GlStateManager._logicOp(logicOp.value);
    }

    private static /* synthetic */ void lambda$polygonOffset$11(float f, float g) {
        GlStateManager._polygonOffset(f, g);
    }

    private static /* synthetic */ void lambda$polygonMode$10(int i, int j) {
        GlStateManager._polygonMode(i, j);
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

    private static /* synthetic */ void lambda$enableScissor$2(int i, int j, int k, int l) {
        GlStateManager._enableScissorTest();
        GlStateManager._scissorBox(i, j, k, l);
    }

    static {
        MAX_SUPPORTED_TEXTURE_SIZE = -1;
        lastDrawTime = Double.MIN_VALUE;
        sharedSequential = new AutoStorageIndexBuffer(1, 1, java.util.function.IntConsumer::accept);
        sharedSequentialQuad = new AutoStorageIndexBuffer(4, 6, (intConsumer, i) -> {
            intConsumer.accept(i + 0);
            intConsumer.accept(i + 1);
            intConsumer.accept(i + 2);
            intConsumer.accept(i + 2);
            intConsumer.accept(i + 3);
            intConsumer.accept(i + 0);
        });
        sharedSequentialLines = new AutoStorageIndexBuffer(4, 6, (intConsumer, i) -> {
            intConsumer.accept(i + 0);
            intConsumer.accept(i + 1);
            intConsumer.accept(i + 2);
            intConsumer.accept(i + 3);
            intConsumer.accept(i + 2);
            intConsumer.accept(i + 1);
        });
        projectionMatrix = new Matrix4f();
        savedProjectionMatrix = new Matrix4f();
        modelViewStack = new PoseStack();
        modelViewMatrix = new Matrix4f();
        textureMatrix = new Matrix4f();
        shaderTextures = new int[12];
        shaderColor = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        shaderFogEnd = 1.0f;
        shaderFogColor = new float[]{0.0f, 0.0f, 0.0f, 0.0f};
        shaderLightDirections = new Vector3f[2];
        shaderLineWidth = 1.0f;
        projectionMatrix.setIdentity();
        savedProjectionMatrix.setIdentity();
        modelViewMatrix.setIdentity();
        textureMatrix.setIdentity();
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
            GlStateManager._glBufferData(34963, j, 35048);
            ByteBuffer byteBuffer = GlStateManager._glMapBuffer(34963, 35001);
            if (byteBuffer == null) {
                throw new RuntimeException("Failed to map GL buffer");
            }
            this.type = indexType;
            IntConsumer intConsumer = this.intConsumer(byteBuffer);
            for (int k = 0; k < i; k += this.indexStride) {
                this.generator.accept(intConsumer, k * this.vertexStride / this.indexStride);
            }
            GlStateManager._glUnmapBuffer(34963);
            GlStateManager._glBindBuffer(34963, 0);
            this.indexCount = i;
            BufferUploader.invalidateElementArrayBufferBinding();
        }

        private IntConsumer intConsumer(ByteBuffer byteBuffer) {
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
            public void accept(IntConsumer var1, int var2);
        }
    }
}

