/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

@Environment(value=EnvType.CLIENT)
@DontObfuscate
public class GlStateManager {
    public static final int TEXTURE_COUNT = 12;
    private static final BlendState BLEND = new BlendState();
    private static final DepthState DEPTH = new DepthState();
    private static final CullState CULL = new CullState();
    private static final PolygonOffsetState POLY_OFFSET = new PolygonOffsetState();
    private static final ColorLogicState COLOR_LOGIC = new ColorLogicState();
    private static final StencilState STENCIL = new StencilState();
    private static final ScissorState SCISSOR = new ScissorState();
    private static int activeTexture;
    private static final TextureState[] TEXTURES;
    private static final ColorMask COLOR_MASK;

    public static void _disableScissorTest() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager.SCISSOR.mode.disable();
    }

    public static void _enableScissorTest() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager.SCISSOR.mode.enable();
    }

    public static void _scissorBox(int i, int j, int k, int l) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL20.glScissor(i, j, k, l);
    }

    public static void _disableDepthTest() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager.DEPTH.mode.disable();
    }

    public static void _enableDepthTest() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager.DEPTH.mode.enable();
    }

    public static void _depthFunc(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (i != GlStateManager.DEPTH.func) {
            GlStateManager.DEPTH.func = i;
            GL11.glDepthFunc(i);
        }
    }

    public static void _depthMask(boolean bl) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (bl != GlStateManager.DEPTH.mask) {
            GlStateManager.DEPTH.mask = bl;
            GL11.glDepthMask(bl);
        }
    }

    public static void _disableBlend() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.BLEND.mode.disable();
    }

    public static void _enableBlend() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.BLEND.mode.enable();
    }

    public static void _blendFunc(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (i != GlStateManager.BLEND.srcRgb || j != GlStateManager.BLEND.dstRgb) {
            GlStateManager.BLEND.srcRgb = i;
            GlStateManager.BLEND.dstRgb = j;
            GL11.glBlendFunc(i, j);
        }
    }

    public static void _blendFuncSeparate(int i, int j, int k, int l) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (i != GlStateManager.BLEND.srcRgb || j != GlStateManager.BLEND.dstRgb || k != GlStateManager.BLEND.srcAlpha || l != GlStateManager.BLEND.dstAlpha) {
            GlStateManager.BLEND.srcRgb = i;
            GlStateManager.BLEND.dstRgb = j;
            GlStateManager.BLEND.srcAlpha = k;
            GlStateManager.BLEND.dstAlpha = l;
            GlStateManager.glBlendFuncSeparate(i, j, k, l);
        }
    }

    public static void _blendEquation(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL14.glBlendEquation(i);
    }

    public static int glGetProgrami(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL20.glGetProgrami(i, j);
    }

    public static void glAttachShader(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glAttachShader(i, j);
    }

    public static void glDeleteShader(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glDeleteShader(i);
    }

    public static int glCreateShader(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL20.glCreateShader(i);
    }

    public static void glShaderSource(int i, List<String> list) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glShaderSource(i, list.toArray(new CharSequence[0]));
    }

    public static void glCompileShader(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glCompileShader(i);
    }

    public static int glGetShaderi(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL20.glGetShaderi(i, j);
    }

    public static void _glUseProgram(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUseProgram(i);
    }

    public static int glCreateProgram() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL20.glCreateProgram();
    }

    public static void glDeleteProgram(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glDeleteProgram(i);
    }

    public static void glLinkProgram(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glLinkProgram(i);
    }

    public static int _glGetUniformLocation(int i, CharSequence charSequence) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL20.glGetUniformLocation(i, charSequence);
    }

    public static void _glUniform1(int i, IntBuffer intBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniform1iv(i, intBuffer);
    }

    public static void _glUniform1i(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniform1i(i, j);
    }

    public static void _glUniform1(int i, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniform1fv(i, floatBuffer);
    }

    public static void _glUniform2(int i, IntBuffer intBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniform2iv(i, intBuffer);
    }

    public static void _glUniform2(int i, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniform2fv(i, floatBuffer);
    }

    public static void _glUniform3(int i, IntBuffer intBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniform3iv(i, intBuffer);
    }

    public static void _glUniform3(int i, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniform3fv(i, floatBuffer);
    }

    public static void _glUniform4(int i, IntBuffer intBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniform4iv(i, intBuffer);
    }

    public static void _glUniform4(int i, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniform4fv(i, floatBuffer);
    }

    public static void _glUniformMatrix2(int i, boolean bl, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniformMatrix2fv(i, bl, floatBuffer);
    }

    public static void _glUniformMatrix3(int i, boolean bl, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniformMatrix3fv(i, bl, floatBuffer);
    }

    public static void _glUniformMatrix4(int i, boolean bl, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glUniformMatrix4fv(i, bl, floatBuffer);
    }

    public static int _glGetAttribLocation(int i, CharSequence charSequence) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL20.glGetAttribLocation(i, charSequence);
    }

    public static void _glBindAttribLocation(int i, int j, CharSequence charSequence) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glBindAttribLocation(i, j, charSequence);
    }

    public static int _glGenBuffers() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        return GL15.glGenBuffers();
    }

    public static int _glGenVertexArrays() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        return GL30.glGenVertexArrays();
    }

    public static void _glBindBuffer(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL15.glBindBuffer(i, j);
    }

    public static void _glBindVertexArray(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30.glBindVertexArray(i);
    }

    public static void _glBufferData(int i, ByteBuffer byteBuffer, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL15.glBufferData(i, byteBuffer, j);
    }

    public static void _glBufferData(int i, long l, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL15.glBufferData(i, l, j);
    }

    @Nullable
    public static ByteBuffer _glMapBuffer(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        return GL15.glMapBuffer(i, j);
    }

    public static void _glUnmapBuffer(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL15.glUnmapBuffer(i);
    }

    public static void _glDeleteBuffers(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL15.glDeleteBuffers(i);
    }

    public static void _glCopyTexSubImage2D(int i, int j, int k, int l, int m, int n, int o, int p) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL20.glCopyTexSubImage2D(i, j, k, l, m, n, o, p);
    }

    public static void _glDeleteVertexArrays(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL30.glDeleteVertexArrays(i);
    }

    public static void _glBindFramebuffer(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30.glBindFramebuffer(i, j);
    }

    public static void _glBlitFrameBuffer(int i, int j, int k, int l, int m, int n, int o, int p, int q, int r) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30.glBlitFramebuffer(i, j, k, l, m, n, o, p, q, r);
    }

    public static void _glBindRenderbuffer(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30.glBindRenderbuffer(i, j);
    }

    public static void _glDeleteRenderbuffers(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30.glDeleteRenderbuffers(i);
    }

    public static void _glDeleteFramebuffers(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30.glDeleteFramebuffers(i);
    }

    public static int glGenFramebuffers() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        return GL30.glGenFramebuffers();
    }

    public static int glGenRenderbuffers() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        return GL30.glGenRenderbuffers();
    }

    public static void _glRenderbufferStorage(int i, int j, int k, int l) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30.glRenderbufferStorage(i, j, k, l);
    }

    public static void _glFramebufferRenderbuffer(int i, int j, int k, int l) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30.glFramebufferRenderbuffer(i, j, k, l);
    }

    public static int glCheckFramebufferStatus(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        return GL30.glCheckFramebufferStatus(i);
    }

    public static void _glFramebufferTexture2D(int i, int j, int k, int l, int m) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL30.glFramebufferTexture2D(i, j, k, l, m);
    }

    public static int getBoundFramebuffer() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GlStateManager._getInteger(36006);
    }

    public static void glActiveTexture(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL13.glActiveTexture(i);
    }

    public static void glBlendFuncSeparate(int i, int j, int k, int l) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL14.glBlendFuncSeparate(i, j, k, l);
    }

    public static String glGetShaderInfoLog(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL20.glGetShaderInfoLog(i, j);
    }

    public static String glGetProgramInfoLog(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL20.glGetProgramInfoLog(i, j);
    }

    public static void setupLevelDiffuseLighting(Vector3f vector3f, Vector3f vector3f2, Matrix4f matrix4f) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        Vector4f vector4f = new Vector4f(vector3f);
        vector4f.transform(matrix4f);
        Vector4f vector4f2 = new Vector4f(vector3f2);
        vector4f2.transform(matrix4f);
        RenderSystem.setShaderLights(new Vector3f(vector4f), new Vector3f(vector4f2));
    }

    public static void setupGuiFlatDiffuseLighting(Vector3f vector3f, Vector3f vector3f2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        matrix4f.multiply(Matrix4f.createScaleMatrix(1.0f, -1.0f, 1.0f));
        matrix4f.multiply(Vector3f.YP.rotationDegrees(-22.5f));
        matrix4f.multiply(Vector3f.XP.rotationDegrees(135.0f));
        GlStateManager.setupLevelDiffuseLighting(vector3f, vector3f2, matrix4f);
    }

    public static void setupGui3DDiffuseLighting(Vector3f vector3f, Vector3f vector3f2) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        matrix4f.multiply(Vector3f.YP.rotationDegrees(62.0f));
        matrix4f.multiply(Vector3f.XP.rotationDegrees(185.5f));
        matrix4f.multiply(Vector3f.YP.rotationDegrees(-22.5f));
        matrix4f.multiply(Vector3f.XP.rotationDegrees(135.0f));
        GlStateManager.setupLevelDiffuseLighting(vector3f, vector3f2, matrix4f);
    }

    public static void _enableCull() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.CULL.enable.enable();
    }

    public static void _disableCull() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.CULL.enable.disable();
    }

    public static void _polygonMode(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glPolygonMode(i, j);
    }

    public static void _enablePolygonOffset() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.POLY_OFFSET.fill.enable();
    }

    public static void _disablePolygonOffset() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.POLY_OFFSET.fill.disable();
    }

    public static void _polygonOffset(float f, float g) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (f != GlStateManager.POLY_OFFSET.factor || g != GlStateManager.POLY_OFFSET.units) {
            GlStateManager.POLY_OFFSET.factor = f;
            GlStateManager.POLY_OFFSET.units = g;
            GL11.glPolygonOffset(f, g);
        }
    }

    public static void _enableColorLogicOp() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.COLOR_LOGIC.enable.enable();
    }

    public static void _disableColorLogicOp() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.COLOR_LOGIC.enable.disable();
    }

    public static void _logicOp(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (i != GlStateManager.COLOR_LOGIC.op) {
            GlStateManager.COLOR_LOGIC.op = i;
            GL11.glLogicOp(i);
        }
    }

    public static void _activeTexture(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (activeTexture != i - 33984) {
            activeTexture = i - 33984;
            GlStateManager.glActiveTexture(i);
        }
    }

    public static void _enableTexture() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager.TEXTURES[GlStateManager.activeTexture].enable = true;
    }

    public static void _disableTexture() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.TEXTURES[GlStateManager.activeTexture].enable = false;
    }

    public static void _texParameter(int i, int j, float f) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glTexParameterf(i, j, f);
    }

    public static void _texParameter(int i, int j, int k) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glTexParameteri(i, j, k);
    }

    public static int _getTexLevelParameter(int i, int j, int k) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        return GL11.glGetTexLevelParameteri(i, j, k);
    }

    public static int _genTexture() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        return GL11.glGenTextures();
    }

    public static void _genTextures(int[] is) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glGenTextures(is);
    }

    public static void _deleteTexture(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glDeleteTextures(i);
        for (TextureState textureState : TEXTURES) {
            if (textureState.binding != i) continue;
            textureState.binding = -1;
        }
    }

    public static void _deleteTextures(int[] is) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        for (TextureState textureState : TEXTURES) {
            for (int i : is) {
                if (textureState.binding != i) continue;
                textureState.binding = -1;
            }
        }
        GL11.glDeleteTextures(is);
    }

    public static void _bindTexture(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (i != GlStateManager.TEXTURES[GlStateManager.activeTexture].binding) {
            GlStateManager.TEXTURES[GlStateManager.activeTexture].binding = i;
            GL11.glBindTexture(3553, i);
        }
    }

    public static int _getTextureId(int i) {
        if (i >= 0 && i < 12 && GlStateManager.TEXTURES[i].enable) {
            return GlStateManager.TEXTURES[i].binding;
        }
        return 0;
    }

    public static int _getActiveTexture() {
        return activeTexture + 33984;
    }

    public static void _texImage2D(int i, int j, int k, int l, int m, int n, int o, int p, @Nullable IntBuffer intBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glTexImage2D(i, j, k, l, m, n, o, p, intBuffer);
    }

    public static void _texSubImage2D(int i, int j, int k, int l, int m, int n, int o, int p, long q) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glTexSubImage2D(i, j, k, l, m, n, o, p, q);
    }

    public static void _getTexImage(int i, int j, int k, int l, long m) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glGetTexImage(i, j, k, l, m);
    }

    public static void _viewport(int i, int j, int k, int l) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        Viewport.INSTANCE.x = i;
        Viewport.INSTANCE.y = j;
        Viewport.INSTANCE.width = k;
        Viewport.INSTANCE.height = l;
        GL11.glViewport(i, j, k, l);
    }

    public static void _colorMask(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (bl != GlStateManager.COLOR_MASK.red || bl2 != GlStateManager.COLOR_MASK.green || bl3 != GlStateManager.COLOR_MASK.blue || bl4 != GlStateManager.COLOR_MASK.alpha) {
            GlStateManager.COLOR_MASK.red = bl;
            GlStateManager.COLOR_MASK.green = bl2;
            GlStateManager.COLOR_MASK.blue = bl3;
            GlStateManager.COLOR_MASK.alpha = bl4;
            GL11.glColorMask(bl, bl2, bl3, bl4);
        }
    }

    public static void _stencilFunc(int i, int j, int k) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (i != GlStateManager.STENCIL.func.func || i != GlStateManager.STENCIL.func.ref || i != GlStateManager.STENCIL.func.mask) {
            GlStateManager.STENCIL.func.func = i;
            GlStateManager.STENCIL.func.ref = j;
            GlStateManager.STENCIL.func.mask = k;
            GL11.glStencilFunc(i, j, k);
        }
    }

    public static void _stencilMask(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (i != GlStateManager.STENCIL.mask) {
            GlStateManager.STENCIL.mask = i;
            GL11.glStencilMask(i);
        }
    }

    public static void _stencilOp(int i, int j, int k) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (i != GlStateManager.STENCIL.fail || j != GlStateManager.STENCIL.zfail || k != GlStateManager.STENCIL.zpass) {
            GlStateManager.STENCIL.fail = i;
            GlStateManager.STENCIL.zfail = j;
            GlStateManager.STENCIL.zpass = k;
            GL11.glStencilOp(i, j, k);
        }
    }

    public static void _clearDepth(double d) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glClearDepth(d);
    }

    public static void _clearColor(float f, float g, float h, float i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glClearColor(f, g, h, i);
    }

    public static void _clearStencil(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glClearStencil(i);
    }

    public static void _clear(int i, boolean bl) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glClear(i);
        if (bl) {
            GlStateManager._getError();
        }
    }

    public static void _glDrawPixels(int i, int j, int k, int l, long m) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glDrawPixels(i, j, k, l, m);
    }

    public static void _vertexAttribPointer(int i, int j, int k, boolean bl, int l, long m) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glVertexAttribPointer(i, j, k, bl, l, m);
    }

    public static void _vertexAttribIPointer(int i, int j, int k, int l, long m) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL30.glVertexAttribIPointer(i, j, k, l, m);
    }

    public static void _enableVertexAttribArray(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glEnableVertexAttribArray(i);
    }

    public static void _disableVertexAttribArray(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glDisableVertexAttribArray(i);
    }

    public static void _drawElements(int i, int j, int k, long l) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glDrawElements(i, j, k, l);
    }

    public static void _pixelStore(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glPixelStorei(i, j);
    }

    public static void _readPixels(int i, int j, int k, int l, int m, int n, ByteBuffer byteBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glReadPixels(i, j, k, l, m, n, byteBuffer);
    }

    public static void _readPixels(int i, int j, int k, int l, int m, int n, long o) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glReadPixels(i, j, k, l, m, n, o);
    }

    public static int _getError() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL11.glGetError();
    }

    public static String _getString(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL11.glGetString(i);
    }

    public static int _getInteger(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        return GL11.glGetInteger(i);
    }

    static {
        TEXTURES = (TextureState[])IntStream.range(0, 12).mapToObj(i -> new TextureState()).toArray(TextureState[]::new);
        COLOR_MASK = new ColorMask();
    }

    @Environment(value=EnvType.CLIENT)
    @DontObfuscate
    public static enum DestFactor {
        CONSTANT_ALPHA(32771),
        CONSTANT_COLOR(32769),
        DST_ALPHA(772),
        DST_COLOR(774),
        ONE(1),
        ONE_MINUS_CONSTANT_ALPHA(32772),
        ONE_MINUS_CONSTANT_COLOR(32770),
        ONE_MINUS_DST_ALPHA(773),
        ONE_MINUS_DST_COLOR(775),
        ONE_MINUS_SRC_ALPHA(771),
        ONE_MINUS_SRC_COLOR(769),
        SRC_ALPHA(770),
        SRC_COLOR(768),
        ZERO(0);

        public final int value;

        private DestFactor(int j) {
            this.value = j;
        }
    }

    @Environment(value=EnvType.CLIENT)
    @DontObfuscate
    public static enum SourceFactor {
        CONSTANT_ALPHA(32771),
        CONSTANT_COLOR(32769),
        DST_ALPHA(772),
        DST_COLOR(774),
        ONE(1),
        ONE_MINUS_CONSTANT_ALPHA(32772),
        ONE_MINUS_CONSTANT_COLOR(32770),
        ONE_MINUS_DST_ALPHA(773),
        ONE_MINUS_DST_COLOR(775),
        ONE_MINUS_SRC_ALPHA(771),
        ONE_MINUS_SRC_COLOR(769),
        SRC_ALPHA(770),
        SRC_ALPHA_SATURATE(776),
        SRC_COLOR(768),
        ZERO(0);

        public final int value;

        private SourceFactor(int j) {
            this.value = j;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class BooleanState {
        private final int state;
        private boolean enabled;

        public BooleanState(int i) {
            this.state = i;
        }

        public void disable() {
            this.setEnabled(false);
        }

        public void enable() {
            this.setEnabled(true);
        }

        public void setEnabled(boolean bl) {
            RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
            if (bl != this.enabled) {
                this.enabled = bl;
                if (bl) {
                    GL11.glEnable(this.state);
                } else {
                    GL11.glDisable(this.state);
                }
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ColorMask {
        public boolean red = true;
        public boolean green = true;
        public boolean blue = true;
        public boolean alpha = true;

        private ColorMask() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ScissorState {
        public final BooleanState mode = new BooleanState(3089);

        private ScissorState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class StencilState {
        public final StencilFunc func = new StencilFunc();
        public int mask = -1;
        public int fail = 7680;
        public int zfail = 7680;
        public int zpass = 7680;

        private StencilState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class StencilFunc {
        public int func = 519;
        public int ref;
        public int mask = -1;

        private StencilFunc() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ColorLogicState {
        public final BooleanState enable = new BooleanState(3058);
        public int op = 5379;

        private ColorLogicState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class PolygonOffsetState {
        public final BooleanState fill = new BooleanState(32823);
        public final BooleanState line = new BooleanState(10754);
        public float factor;
        public float units;

        private PolygonOffsetState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class CullState {
        public final BooleanState enable = new BooleanState(2884);
        public int mode = 1029;

        private CullState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class DepthState {
        public final BooleanState mode = new BooleanState(2929);
        public boolean mask = true;
        public int func = 513;

        private DepthState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class BlendState {
        public final BooleanState mode = new BooleanState(3042);
        public int srcRgb = 1;
        public int dstRgb = 0;
        public int srcAlpha = 1;
        public int dstAlpha = 0;

        private BlendState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class TextureState {
        public boolean enable;
        public int binding;

        private TextureState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Viewport {
        INSTANCE;

        protected int x;
        protected int y;
        protected int width;
        protected int height;

        public static int x() {
            return Viewport.INSTANCE.x;
        }

        public static int y() {
            return Viewport.INSTANCE.y;
        }

        public static int width() {
            return Viewport.INSTANCE.width;
        }

        public static int height() {
            return Viewport.INSTANCE.height;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum LogicOp {
        AND(5377),
        AND_INVERTED(5380),
        AND_REVERSE(5378),
        CLEAR(5376),
        COPY(5379),
        COPY_INVERTED(5388),
        EQUIV(5385),
        INVERT(5386),
        NAND(5390),
        NOOP(5381),
        NOR(5384),
        OR(5383),
        OR_INVERTED(5389),
        OR_REVERSE(5387),
        SET(5391),
        XOR(5382);

        public final int value;

        private LogicOp(int j) {
            this.value = j;
        }
    }
}

