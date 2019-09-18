/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.platform.DebugMemoryUntracker;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
public class GlStateManager {
    protected static final FloatBuffer TINT_BUFFER = MemoryTracker.createFloatBuffer(4);
    private static final FloatBuffer MATRIX_BUFFER = GLX.make(MemoryUtil.memAllocFloat(16), floatBuffer -> DebugMemoryUntracker.untrack(MemoryUtil.memAddress(floatBuffer)));
    private static final FloatBuffer COLOR_BUFFER = GLX.make(MemoryUtil.memAllocFloat(4), floatBuffer -> DebugMemoryUntracker.untrack(MemoryUtil.memAddress(floatBuffer)));
    private static final AlphaState ALPHA_TEST = new AlphaState();
    private static final BooleanState LIGHTING = new BooleanState(2896);
    private static final BooleanState[] LIGHT_ENABLE = (BooleanState[])IntStream.range(0, 8).mapToObj(i -> new BooleanState(16384 + i)).toArray(BooleanState[]::new);
    private static final ColorMaterialState COLOR_MATERIAL = new ColorMaterialState();
    private static final BlendState BLEND = new BlendState();
    private static final DepthState DEPTH = new DepthState();
    private static final FogState FOG = new FogState();
    private static final CullState CULL = new CullState();
    private static final PolygonOffsetState POLY_OFFSET = new PolygonOffsetState();
    private static final ColorLogicState COLOR_LOGIC = new ColorLogicState();
    private static final TexGenState TEX_GEN = new TexGenState();
    private static final ClearState CLEAR = new ClearState();
    private static final StencilState STENCIL = new StencilState();
    private static final BooleanState NORMALIZE = new BooleanState(2977);
    private static final FloatBuffer DIFFUSE_LIGHT_BUFFER = MemoryTracker.createFloatBuffer(4);
    private static final Vector3f DIFFUSE_LIGHT_0 = GlStateManager.createVector(0.2f, 1.0f, -0.7f);
    private static final Vector3f DIFFUSE_LIGHT_1 = GlStateManager.createVector(-0.2f, 1.0f, 0.7f);
    private static int activeTexture;
    private static final TextureState[] TEXTURES;
    private static int shadeModel;
    private static final BooleanState RESCALE_NORMAL;
    private static final ColorMask COLOR_MASK;
    private static final Color COLOR;
    private static FboMode fboMode;
    private static final DynamicTexture WHITE_TEXTURE;

    public static int getLightCount() {
        return 8;
    }

    public static void _pushLightingAttributes() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glPushAttrib(8256);
    }

    public static void _pushTextureAttributes() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glPushAttrib(270336);
    }

    public static void _popAttributes() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glPopAttrib();
    }

    public static void _disableAlphaTest() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.ALPHA_TEST.mode.disable();
    }

    public static void _enableAlphaTest() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GlStateManager.ALPHA_TEST.mode.enable();
    }

    public static void _alphaFunc(int i, float f) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (i != GlStateManager.ALPHA_TEST.func || f != GlStateManager.ALPHA_TEST.reference) {
            GlStateManager.ALPHA_TEST.func = i;
            GlStateManager.ALPHA_TEST.reference = f;
            GL11.glAlphaFunc(i, f);
        }
    }

    public static void _enableLighting() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        LIGHTING.enable();
    }

    public static void _disableLighting() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        LIGHTING.disable();
    }

    public static void _enableLight(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        LIGHT_ENABLE[i].enable();
    }

    public static void _disableLight(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        LIGHT_ENABLE[i].disable();
    }

    public static void _enableColorMaterial() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.COLOR_MATERIAL.enable.enable();
    }

    public static void _disableColorMaterial() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.COLOR_MATERIAL.enable.disable();
    }

    public static void _colorMaterial(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (i != GlStateManager.COLOR_MATERIAL.face || j != GlStateManager.COLOR_MATERIAL.mode) {
            GlStateManager.COLOR_MATERIAL.face = i;
            GlStateManager.COLOR_MATERIAL.mode = j;
            GL11.glColorMaterial(i, j);
        }
    }

    public static void _light(int i, int j, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glLightfv(i, j, floatBuffer);
    }

    public static void _lightModel(int i, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glLightModelfv(i, floatBuffer);
    }

    public static void _normal3f(float f, float g, float h) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glNormal3f(f, g, h);
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

    public static void _setupSolidRenderingTextureCombine(int i) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        COLOR_BUFFER.put(0, (float)(i >> 16 & 0xFF) / 255.0f);
        COLOR_BUFFER.put(1, (float)(i >> 8 & 0xFF) / 255.0f);
        COLOR_BUFFER.put(2, (float)(i >> 0 & 0xFF) / 255.0f);
        COLOR_BUFFER.put(3, (float)(i >> 24 & 0xFF) / 255.0f);
        GlStateManager._texEnv(8960, 8705, COLOR_BUFFER);
        GlStateManager._texEnv(8960, 8704, 34160);
        GlStateManager._texEnv(8960, 34161, 7681);
        GlStateManager._texEnv(8960, 34176, 34166);
        GlStateManager._texEnv(8960, 34192, 768);
        GlStateManager._texEnv(8960, 34162, 7681);
        GlStateManager._texEnv(8960, 34184, 5890);
        GlStateManager._texEnv(8960, 34200, 770);
    }

    public static void _tearDownSolidRenderingTextureCombine() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        GlStateManager._texEnv(8960, 8704, 8448);
        GlStateManager._texEnv(8960, 34161, 8448);
        GlStateManager._texEnv(8960, 34162, 8448);
        GlStateManager._texEnv(8960, 34176, 5890);
        GlStateManager._texEnv(8960, 34184, 5890);
        GlStateManager._texEnv(8960, 34192, 768);
        GlStateManager._texEnv(8960, 34200, 770);
    }

    public static String _init_fbo(GLCapabilities gLCapabilities) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        if (gLCapabilities.OpenGL30) {
            fboMode = FboMode.BASE;
            GlConst.GL_FRAMEBUFFER = 36160;
            GlConst.GL_RENDERBUFFER = 36161;
            GlConst.GL_COLOR_ATTACHMENT0 = 36064;
            GlConst.GL_DEPTH_ATTACHMENT = 36096;
            GlConst.GL_FRAMEBUFFER_COMPLETE = 36053;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 36054;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 36055;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = 36059;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = 36060;
            return "OpenGL 3.0";
        }
        if (gLCapabilities.GL_ARB_framebuffer_object) {
            fboMode = FboMode.ARB;
            GlConst.GL_FRAMEBUFFER = 36160;
            GlConst.GL_RENDERBUFFER = 36161;
            GlConst.GL_COLOR_ATTACHMENT0 = 36064;
            GlConst.GL_DEPTH_ATTACHMENT = 36096;
            GlConst.GL_FRAMEBUFFER_COMPLETE = 36053;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 36055;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 36054;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = 36059;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = 36060;
            return "ARB_framebuffer_object extension";
        }
        if (gLCapabilities.GL_EXT_framebuffer_object) {
            fboMode = FboMode.EXT;
            GlConst.GL_FRAMEBUFFER = 36160;
            GlConst.GL_RENDERBUFFER = 36161;
            GlConst.GL_COLOR_ATTACHMENT0 = 36064;
            GlConst.GL_DEPTH_ATTACHMENT = 36096;
            GlConst.GL_FRAMEBUFFER_COMPLETE = 36053;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 36055;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 36054;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = 36059;
            GlConst.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = 36060;
            return "EXT_framebuffer_object extension";
        }
        throw new IllegalStateException("Could not initialize framebuffer support.");
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

    public static void glShaderSource(int i, CharSequence charSequence) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glShaderSource(i, charSequence);
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

    public static int _glGenBuffers() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        return GL15.glGenBuffers();
    }

    public static void _glBindBuffer(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL15.glBindBuffer(i, j);
    }

    public static void _glBufferData(int i, ByteBuffer byteBuffer, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL15.glBufferData(i, byteBuffer, j);
    }

    public static void _glDeleteBuffers(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL15.glDeleteBuffers(i);
    }

    public static void _glBindFramebuffer(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch (fboMode) {
            case BASE: {
                GL30.glBindFramebuffer(i, j);
                break;
            }
            case ARB: {
                ARBFramebufferObject.glBindFramebuffer(i, j);
                break;
            }
            case EXT: {
                EXTFramebufferObject.glBindFramebufferEXT(i, j);
            }
        }
    }

    public static void _glBindRenderbuffer(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch (fboMode) {
            case BASE: {
                GL30.glBindRenderbuffer(i, j);
                break;
            }
            case ARB: {
                ARBFramebufferObject.glBindRenderbuffer(i, j);
                break;
            }
            case EXT: {
                EXTFramebufferObject.glBindRenderbufferEXT(i, j);
            }
        }
    }

    public static void _glDeleteRenderbuffers(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch (fboMode) {
            case BASE: {
                GL30.glDeleteRenderbuffers(i);
                break;
            }
            case ARB: {
                ARBFramebufferObject.glDeleteRenderbuffers(i);
                break;
            }
            case EXT: {
                EXTFramebufferObject.glDeleteRenderbuffersEXT(i);
            }
        }
    }

    public static void _glDeleteFramebuffers(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch (fboMode) {
            case BASE: {
                GL30.glDeleteFramebuffers(i);
                break;
            }
            case ARB: {
                ARBFramebufferObject.glDeleteFramebuffers(i);
                break;
            }
            case EXT: {
                EXTFramebufferObject.glDeleteFramebuffersEXT(i);
            }
        }
    }

    public static int glGenFramebuffers() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch (fboMode) {
            case BASE: {
                return GL30.glGenFramebuffers();
            }
            case ARB: {
                return ARBFramebufferObject.glGenFramebuffers();
            }
            case EXT: {
                return EXTFramebufferObject.glGenFramebuffersEXT();
            }
        }
        return -1;
    }

    public static int glGenRenderbuffers() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch (fboMode) {
            case BASE: {
                return GL30.glGenRenderbuffers();
            }
            case ARB: {
                return ARBFramebufferObject.glGenRenderbuffers();
            }
            case EXT: {
                return EXTFramebufferObject.glGenRenderbuffersEXT();
            }
        }
        return -1;
    }

    public static void _glRenderbufferStorage(int i, int j, int k, int l) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch (fboMode) {
            case BASE: {
                GL30.glRenderbufferStorage(i, j, k, l);
                break;
            }
            case ARB: {
                ARBFramebufferObject.glRenderbufferStorage(i, j, k, l);
                break;
            }
            case EXT: {
                EXTFramebufferObject.glRenderbufferStorageEXT(i, j, k, l);
            }
        }
    }

    public static void _glFramebufferRenderbuffer(int i, int j, int k, int l) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch (fboMode) {
            case BASE: {
                GL30.glFramebufferRenderbuffer(i, j, k, l);
                break;
            }
            case ARB: {
                ARBFramebufferObject.glFramebufferRenderbuffer(i, j, k, l);
                break;
            }
            case EXT: {
                EXTFramebufferObject.glFramebufferRenderbufferEXT(i, j, k, l);
            }
        }
    }

    public static int glCheckFramebufferStatus(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch (fboMode) {
            case BASE: {
                return GL30.glCheckFramebufferStatus(i);
            }
            case ARB: {
                return ARBFramebufferObject.glCheckFramebufferStatus(i);
            }
            case EXT: {
                return EXTFramebufferObject.glCheckFramebufferStatusEXT(i);
            }
        }
        return -1;
    }

    public static void _glFramebufferTexture2D(int i, int j, int k, int l, int m) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        switch (fboMode) {
            case BASE: {
                GL30.glFramebufferTexture2D(i, j, k, l, m);
                break;
            }
            case ARB: {
                ARBFramebufferObject.glFramebufferTexture2D(i, j, k, l, m);
                break;
            }
            case EXT: {
                EXTFramebufferObject.glFramebufferTexture2DEXT(i, j, k, l, m);
            }
        }
    }

    public static void glActiveTexture(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL13.glActiveTexture(i);
    }

    public static void _glClientActiveTexture(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL13.glClientActiveTexture(i);
    }

    public static void _glMultiTexCoord2f(int i, float f, float g) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL13.glMultiTexCoord2f(i, f, g);
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

    public static void setupOverlayColor(int i, boolean bl) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager._activeTexture(33984);
        GlStateManager._enableTexture();
        GlStateManager._texEnv(8960, 8704, 34160);
        GlStateManager._texEnv(8960, 34161, 8448);
        GlStateManager._texEnv(8960, 34176, 33984);
        GlStateManager._texEnv(8960, 34177, 34167);
        GlStateManager._texEnv(8960, 34192, 768);
        GlStateManager._texEnv(8960, 34193, 768);
        GlStateManager._texEnv(8960, 34162, 7681);
        GlStateManager._texEnv(8960, 34184, 33984);
        GlStateManager._texEnv(8960, 34200, 770);
        GlStateManager._activeTexture(33985);
        GlStateManager._enableTexture();
        GlStateManager._texEnv(8960, 8704, 34160);
        GlStateManager._texEnv(8960, 34161, 34165);
        GlStateManager._texEnv(8960, 34176, 34166);
        GlStateManager._texEnv(8960, 34177, 34168);
        GlStateManager._texEnv(8960, 34178, 34166);
        GlStateManager._texEnv(8960, 34192, 768);
        GlStateManager._texEnv(8960, 34193, 768);
        GlStateManager._texEnv(8960, 34194, 770);
        GlStateManager._texEnv(8960, 34162, 7681);
        GlStateManager._texEnv(8960, 34184, 34168);
        GlStateManager._texEnv(8960, 34200, 770);
        TINT_BUFFER.position(0);
        if (bl) {
            TINT_BUFFER.put(1.0f);
            TINT_BUFFER.put(0.0f);
            TINT_BUFFER.put(0.0f);
            TINT_BUFFER.put(0.3f);
        } else {
            float f = (float)(i >> 24 & 0xFF) / 255.0f;
            float g = (float)(i >> 16 & 0xFF) / 255.0f;
            float h = (float)(i >> 8 & 0xFF) / 255.0f;
            float j = (float)(i & 0xFF) / 255.0f;
            TINT_BUFFER.put(g);
            TINT_BUFFER.put(h);
            TINT_BUFFER.put(j);
            TINT_BUFFER.put(1.0f - f);
        }
        TINT_BUFFER.flip();
        GlStateManager._texEnv(8960, 8705, TINT_BUFFER);
        GlStateManager._activeTexture(33986);
        GlStateManager._enableTexture();
        GlStateManager._bindTexture(WHITE_TEXTURE.getId());
        GlStateManager._texEnv(8960, 8704, 34160);
        GlStateManager._texEnv(8960, 34161, 8448);
        GlStateManager._texEnv(8960, 34176, 34168);
        GlStateManager._texEnv(8960, 34177, 33985);
        GlStateManager._texEnv(8960, 34192, 768);
        GlStateManager._texEnv(8960, 34193, 768);
        GlStateManager._texEnv(8960, 34162, 7681);
        GlStateManager._texEnv(8960, 34184, 34168);
        GlStateManager._texEnv(8960, 34200, 770);
        GlStateManager._activeTexture(33984);
    }

    public static void teardownOverlayColor() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager._activeTexture(33984);
        GlStateManager._enableTexture();
        GlStateManager._texEnv(8960, 8704, 34160);
        GlStateManager._texEnv(8960, 34161, 8448);
        GlStateManager._texEnv(8960, 34176, 33984);
        GlStateManager._texEnv(8960, 34177, 34167);
        GlStateManager._texEnv(8960, 34192, 768);
        GlStateManager._texEnv(8960, 34193, 768);
        GlStateManager._texEnv(8960, 34162, 8448);
        GlStateManager._texEnv(8960, 34184, 33984);
        GlStateManager._texEnv(8960, 34185, 34167);
        GlStateManager._texEnv(8960, 34200, 770);
        GlStateManager._texEnv(8960, 34201, 770);
        GlStateManager._activeTexture(33985);
        GlStateManager._texEnv(8960, 8704, 34160);
        GlStateManager._texEnv(8960, 34161, 8448);
        GlStateManager._texEnv(8960, 34192, 768);
        GlStateManager._texEnv(8960, 34193, 768);
        GlStateManager._texEnv(8960, 34176, 5890);
        GlStateManager._texEnv(8960, 34177, 34168);
        GlStateManager._texEnv(8960, 34162, 8448);
        GlStateManager._texEnv(8960, 34200, 770);
        GlStateManager._texEnv(8960, 34184, 5890);
        GlStateManager._color4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager._activeTexture(33986);
        GlStateManager._disableTexture();
        GlStateManager._bindTexture(0);
        GlStateManager._texEnv(8960, 8704, 34160);
        GlStateManager._texEnv(8960, 34161, 8448);
        GlStateManager._texEnv(8960, 34192, 768);
        GlStateManager._texEnv(8960, 34193, 768);
        GlStateManager._texEnv(8960, 34176, 5890);
        GlStateManager._texEnv(8960, 34177, 34168);
        GlStateManager._texEnv(8960, 34162, 8448);
        GlStateManager._texEnv(8960, 34200, 770);
        GlStateManager._texEnv(8960, 34184, 5890);
        GlStateManager._activeTexture(33984);
    }

    public static void _setupDefaultGlState() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager._disableAlphaTest();
        GlStateManager._alphaFunc(519, 0.0f);
        GlStateManager._disableLighting();
        GlStateManager._lightModel(2899, GlStateManager.getBuffer(0.2f, 0.2f, 0.2f, 1.0f));
        for (int i = 0; i < GlStateManager.getLightCount(); ++i) {
            GlStateManager._disableLight(i);
            GlStateManager._light(16384 + i, 4608, GlStateManager.getBuffer(0.0f, 0.0f, 0.0f, 1.0f));
            GlStateManager._light(16384 + i, 4611, GlStateManager.getBuffer(0.0f, 0.0f, 1.0f, 0.0f));
            if (i == 0) {
                GlStateManager._light(16384 + i, 4609, GlStateManager.getBuffer(1.0f, 1.0f, 1.0f, 1.0f));
                GlStateManager._light(16384 + i, 4610, GlStateManager.getBuffer(1.0f, 1.0f, 1.0f, 1.0f));
                continue;
            }
            GlStateManager._light(16384 + i, 4609, GlStateManager.getBuffer(0.0f, 0.0f, 0.0f, 1.0f));
            GlStateManager._light(16384 + i, 4610, GlStateManager.getBuffer(0.0f, 0.0f, 0.0f, 1.0f));
        }
        GlStateManager._disableColorMaterial();
        GlStateManager._colorMaterial(1032, 5634);
        GlStateManager._disableDepthTest();
        GlStateManager._depthFunc(513);
        GlStateManager._depthMask(true);
        GlStateManager._disableBlend();
        GlStateManager._blendFunc(SourceFactor.ONE.value, DestFactor.ZERO.value);
        GlStateManager._blendFuncSeparate(1, 0, 1, 0);
        GlStateManager._blendEquation(32774);
        GlStateManager._disableFog();
        GlStateManager._fogi(2917, 2048);
        GlStateManager._fogDensity(1.0f);
        GlStateManager._fogStart(0.0f);
        GlStateManager._fogEnd(1.0f);
        GlStateManager._fog(2918, GlStateManager.getBuffer(0.0f, 0.0f, 0.0f, 0.0f));
        if (GL.getCapabilities().GL_NV_fog_distance) {
            GlStateManager._fogi(2917, 34140);
        }
        GlStateManager._polygonOffset(0.0f, 0.0f);
        GlStateManager._disableColorLogicOp();
        GlStateManager._logicOp(5379);
        GlStateManager._disableTexGen(TexGen.S);
        GlStateManager._texGenMode(TexGen.S, 9216);
        GlStateManager._texGenParam(TexGen.S, 9474, GlStateManager.getBuffer(1.0f, 0.0f, 0.0f, 0.0f));
        GlStateManager._texGenParam(TexGen.S, 9217, GlStateManager.getBuffer(1.0f, 0.0f, 0.0f, 0.0f));
        GlStateManager._disableTexGen(TexGen.T);
        GlStateManager._texGenMode(TexGen.T, 9216);
        GlStateManager._texGenParam(TexGen.T, 9474, GlStateManager.getBuffer(0.0f, 1.0f, 0.0f, 0.0f));
        GlStateManager._texGenParam(TexGen.T, 9217, GlStateManager.getBuffer(0.0f, 1.0f, 0.0f, 0.0f));
        GlStateManager._disableTexGen(TexGen.R);
        GlStateManager._texGenMode(TexGen.R, 9216);
        GlStateManager._texGenParam(TexGen.R, 9474, GlStateManager.getBuffer(0.0f, 0.0f, 0.0f, 0.0f));
        GlStateManager._texGenParam(TexGen.R, 9217, GlStateManager.getBuffer(0.0f, 0.0f, 0.0f, 0.0f));
        GlStateManager._disableTexGen(TexGen.Q);
        GlStateManager._texGenMode(TexGen.Q, 9216);
        GlStateManager._texGenParam(TexGen.Q, 9474, GlStateManager.getBuffer(0.0f, 0.0f, 0.0f, 0.0f));
        GlStateManager._texGenParam(TexGen.Q, 9217, GlStateManager.getBuffer(0.0f, 0.0f, 0.0f, 0.0f));
        GlStateManager._activeTexture(0);
        GlStateManager._texParameter(3553, 10240, 9729);
        GlStateManager._texParameter(3553, 10241, 9986);
        GlStateManager._texParameter(3553, 10242, 10497);
        GlStateManager._texParameter(3553, 10243, 10497);
        GlStateManager._texParameter(3553, 33085, 1000);
        GlStateManager._texParameter(3553, 33083, 1000);
        GlStateManager._texParameter(3553, 33082, -1000);
        GlStateManager._texParameter(3553, 34049, 0.0f);
        GlStateManager._texEnv(8960, 8704, 8448);
        GlStateManager._texEnv(8960, 8705, GlStateManager.getBuffer(0.0f, 0.0f, 0.0f, 0.0f));
        GlStateManager._texEnv(8960, 34161, 8448);
        GlStateManager._texEnv(8960, 34162, 8448);
        GlStateManager._texEnv(8960, 34176, 5890);
        GlStateManager._texEnv(8960, 34177, 34168);
        GlStateManager._texEnv(8960, 34178, 34166);
        GlStateManager._texEnv(8960, 34184, 5890);
        GlStateManager._texEnv(8960, 34185, 34168);
        GlStateManager._texEnv(8960, 34186, 34166);
        GlStateManager._texEnv(8960, 34192, 768);
        GlStateManager._texEnv(8960, 34193, 768);
        GlStateManager._texEnv(8960, 34194, 770);
        GlStateManager._texEnv(8960, 34200, 770);
        GlStateManager._texEnv(8960, 34201, 770);
        GlStateManager._texEnv(8960, 34202, 770);
        GlStateManager._texEnv(8960, 34163, 1.0f);
        GlStateManager._texEnv(8960, 3356, 1.0f);
        GlStateManager._disableNormalize();
        GlStateManager._shadeModel(7425);
        GlStateManager._disableRescaleNormal();
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._clearDepth(1.0);
        GlStateManager._lineWidth(1.0f);
        GlStateManager._normal3f(0.0f, 0.0f, 1.0f);
        GlStateManager._polygonMode(1028, 6914);
        GlStateManager._polygonMode(1029, 6914);
    }

    public static void enableUsualDiffuseLighting() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager._enableLighting();
        GlStateManager._enableLight(0);
        GlStateManager._enableLight(1);
        GlStateManager._enableColorMaterial();
        GlStateManager._colorMaterial(1032, 5634);
        GlStateManager._light(16384, 4611, GlStateManager.getBuffer(DIFFUSE_LIGHT_0.x(), DIFFUSE_LIGHT_0.y(), DIFFUSE_LIGHT_0.z(), 0.0f));
        float f = 0.6f;
        GlStateManager._light(16384, 4609, GlStateManager.getBuffer(0.6f, 0.6f, 0.6f, 1.0f));
        GlStateManager._light(16384, 4608, GlStateManager.getBuffer(0.0f, 0.0f, 0.0f, 1.0f));
        GlStateManager._light(16384, 4610, GlStateManager.getBuffer(0.0f, 0.0f, 0.0f, 1.0f));
        GlStateManager._light(16385, 4611, GlStateManager.getBuffer(DIFFUSE_LIGHT_1.x(), DIFFUSE_LIGHT_1.y(), DIFFUSE_LIGHT_1.z(), 0.0f));
        GlStateManager._light(16385, 4609, GlStateManager.getBuffer(0.6f, 0.6f, 0.6f, 1.0f));
        GlStateManager._light(16385, 4608, GlStateManager.getBuffer(0.0f, 0.0f, 0.0f, 1.0f));
        GlStateManager._light(16385, 4610, GlStateManager.getBuffer(0.0f, 0.0f, 0.0f, 1.0f));
        GlStateManager._shadeModel(7424);
        float g = 0.4f;
        GlStateManager._lightModel(2899, GlStateManager.getBuffer(0.4f, 0.4f, 0.4f, 1.0f));
    }

    public static void enableGuiDiffuseLighting() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager._pushMatrix();
        GlStateManager._rotatef(-30.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager._rotatef(165.0f, 1.0f, 0.0f, 0.0f);
        GlStateManager.enableUsualDiffuseLighting();
        GlStateManager._popMatrix();
    }

    public static void disableDiffuseLighting() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager._disableLighting();
        GlStateManager._disableLight(0);
        GlStateManager._disableLight(1);
        GlStateManager._disableColorMaterial();
    }

    private static FloatBuffer getBuffer(float f, float g, float h, float i) {
        DIFFUSE_LIGHT_BUFFER.clear();
        DIFFUSE_LIGHT_BUFFER.put(f).put(g).put(h).put(i);
        DIFFUSE_LIGHT_BUFFER.flip();
        return DIFFUSE_LIGHT_BUFFER;
    }

    private static Vector3f createVector(float f, float g, float h) {
        Vector3f vector3f = new Vector3f(f, g, h);
        vector3f.normalize();
        return vector3f;
    }

    public static void _enableFog() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.FOG.enable.enable();
    }

    public static void _disableFog() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.FOG.enable.disable();
    }

    public static void _fogMode(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (i != GlStateManager.FOG.mode) {
            GlStateManager.FOG.mode = i;
            GlStateManager._fogi(2917, i);
        }
    }

    public static void _fogDensity(float f) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (f != GlStateManager.FOG.density) {
            GlStateManager.FOG.density = f;
            GL11.glFogf(2914, f);
        }
    }

    public static void _fogStart(float f) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (f != GlStateManager.FOG.start) {
            GlStateManager.FOG.start = f;
            GL11.glFogf(2915, f);
        }
    }

    public static void _fogEnd(float f) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (f != GlStateManager.FOG.end) {
            GlStateManager.FOG.end = f;
            GL11.glFogf(2916, f);
        }
    }

    public static void _fog(int i, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glFogfv(i, floatBuffer);
    }

    public static void _fogi(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glFogi(i, j);
    }

    public static void _enableCull() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.CULL.enable.enable();
    }

    public static void _disableCull() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.CULL.enable.disable();
    }

    public static void _cullFace(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (i != GlStateManager.CULL.mode) {
            GlStateManager.CULL.mode = i;
            GL11.glCullFace(i);
        }
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

    public static void _enableLineOffset() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.POLY_OFFSET.line.enable();
    }

    public static void _disableLineOffset() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.POLY_OFFSET.line.disable();
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

    public static void _enableTexGen(TexGen texGen) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.getTexGen((TexGen)texGen).enable.enable();
    }

    public static void _disableTexGen(TexGen texGen) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.getTexGen((TexGen)texGen).enable.disable();
    }

    public static void _texGenMode(TexGen texGen, int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        TexGenCoord texGenCoord = GlStateManager.getTexGen(texGen);
        if (i != texGenCoord.mode) {
            texGenCoord.mode = i;
            GL11.glTexGeni(texGenCoord.coord, 9472, i);
        }
    }

    public static void _texGenParam(TexGen texGen, int i, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glTexGenfv(GlStateManager.getTexGen((TexGen)texGen).coord, i, floatBuffer);
    }

    private static TexGenCoord getTexGen(TexGen texGen) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        switch (texGen) {
            case S: {
                return GlStateManager.TEX_GEN.s;
            }
            case T: {
                return GlStateManager.TEX_GEN.t;
            }
            case R: {
                return GlStateManager.TEX_GEN.r;
            }
            case Q: {
                return GlStateManager.TEX_GEN.q;
            }
        }
        return GlStateManager.TEX_GEN.s;
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
        GlStateManager.TEXTURES[GlStateManager.activeTexture].enable.enable();
    }

    public static void _disableTexture() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.TEXTURES[GlStateManager.activeTexture].enable.disable();
    }

    public static void _texEnv(int i, int j, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glTexEnvfv(i, j, floatBuffer);
    }

    public static void _texEnv(int i, int j, int k) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glTexEnvi(i, j, k);
    }

    public static void _texEnv(int i, int j, float f) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glTexEnvf(i, j, f);
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

    public static void _deleteTexture(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glDeleteTextures(i);
        for (TextureState textureState : TEXTURES) {
            if (textureState.binding != i) continue;
            textureState.binding = -1;
        }
    }

    public static void _bindTexture(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (i != GlStateManager.TEXTURES[GlStateManager.activeTexture].binding) {
            GlStateManager.TEXTURES[GlStateManager.activeTexture].binding = i;
            GL11.glBindTexture(3553, i);
        }
    }

    public static void _texImage2D(int i, int j, int k, int l, int m, int n, int o, int p, @Nullable IntBuffer intBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glTexImage2D(i, j, k, l, m, n, o, p, intBuffer);
    }

    public static void _texSubImage2D(int i, int j, int k, int l, int m, int n, int o, int p, long q) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glTexSubImage2D(i, j, k, l, m, n, o, p, q);
    }

    public static void _copyTexSubImage2D(int i, int j, int k, int l, int m, int n, int o, int p) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glCopyTexSubImage2D(i, j, k, l, m, n, o, p);
    }

    public static void _getTexImage(int i, int j, int k, int l, long m) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glGetTexImage(i, j, k, l, m);
    }

    public static void _enableNormalize() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        NORMALIZE.enable();
    }

    public static void _disableNormalize() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        NORMALIZE.disable();
    }

    public static void _shadeModel(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (i != shadeModel) {
            shadeModel = i;
            GL11.glShadeModel(i);
        }
    }

    public static void _enableRescaleNormal() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        RESCALE_NORMAL.enable();
    }

    public static void _disableRescaleNormal() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        RESCALE_NORMAL.disable();
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
        if (d != GlStateManager.CLEAR.depth) {
            GlStateManager.CLEAR.depth = d;
            GL11.glClearDepth(d);
        }
    }

    public static void _clearColor(float f, float g, float h, float i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        if (f != GlStateManager.CLEAR.color.r || g != GlStateManager.CLEAR.color.g || h != GlStateManager.CLEAR.color.b || i != GlStateManager.CLEAR.color.a) {
            GlStateManager.CLEAR.color.r = f;
            GlStateManager.CLEAR.color.g = g;
            GlStateManager.CLEAR.color.b = h;
            GlStateManager.CLEAR.color.a = i;
            GL11.glClearColor(f, g, h, i);
        }
    }

    public static void _clearStencil(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (i != GlStateManager.CLEAR.stencil) {
            GlStateManager.CLEAR.stencil = i;
            GL11.glClearStencil(i);
        }
    }

    public static void _clear(int i, boolean bl) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glClear(i);
        if (bl) {
            GlStateManager._getError();
        }
    }

    public static void _matrixMode(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glMatrixMode(i);
    }

    public static void _loadIdentity() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glLoadIdentity();
    }

    public static void _pushMatrix() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glPushMatrix();
    }

    public static void _popMatrix() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glPopMatrix();
    }

    public static void _getMatrix(int i, FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glGetFloatv(i, floatBuffer);
    }

    public static Matrix4f _getMatrix4f(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager._getMatrix(i, MATRIX_BUFFER);
        MATRIX_BUFFER.rewind();
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.load(MATRIX_BUFFER);
        MATRIX_BUFFER.rewind();
        return matrix4f;
    }

    public static void _ortho(double d, double e, double f, double g, double h, double i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glOrtho(d, e, f, g, h, i);
    }

    public static void _rotatef(float f, float g, float h, float i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glRotatef(f, g, h, i);
    }

    public static void _rotated(double d, double e, double f, double g) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glRotated(d, e, f, g);
    }

    public static void _scalef(float f, float g, float h) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glScalef(f, g, h);
    }

    public static void _scaled(double d, double e, double f) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glScaled(d, e, f);
    }

    public static void _translatef(float f, float g, float h) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glTranslatef(f, g, h);
    }

    public static void _translated(double d, double e, double f) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glTranslated(d, e, f);
    }

    public static void _multMatrix(FloatBuffer floatBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glMultMatrixf(floatBuffer);
    }

    public static void _multMatrix(Matrix4f matrix4f) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        matrix4f.store(MATRIX_BUFFER);
        MATRIX_BUFFER.rewind();
        GlStateManager._multMatrix(MATRIX_BUFFER);
    }

    public static void _color4f(float f, float g, float h, float i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (f != GlStateManager.COLOR.r || g != GlStateManager.COLOR.g || h != GlStateManager.COLOR.b || i != GlStateManager.COLOR.a) {
            GlStateManager.COLOR.r = f;
            GlStateManager.COLOR.g = g;
            GlStateManager.COLOR.b = h;
            GlStateManager.COLOR.a = i;
            GL11.glColor4f(f, g, h, i);
        }
    }

    public static void _clearCurrentColor() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager.COLOR.r = -1.0f;
        GlStateManager.COLOR.g = -1.0f;
        GlStateManager.COLOR.b = -1.0f;
        GlStateManager.COLOR.a = -1.0f;
    }

    public static void _normalPointer(int i, int j, long l) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glNormalPointer(i, j, l);
    }

    public static void _texCoordPointer(int i, int j, int k, long l) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glTexCoordPointer(i, j, k, l);
    }

    public static void _vertexPointer(int i, int j, int k, long l) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glVertexPointer(i, j, k, l);
    }

    public static void _colorPointer(int i, int j, int k, long l) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glColorPointer(i, j, k, l);
    }

    public static void _vertexAttribPointer(int i, int j, int k, boolean bl, int l, long m) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glVertexAttribPointer(i, j, k, bl, l, m);
    }

    public static void _enableClientState(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glEnableClientState(i);
    }

    public static void _disableClientState(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glDisableClientState(i);
    }

    public static void _enableVertexAttribArray(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glEnableVertexAttribArray(i);
    }

    public static void _disableVertexAttribArray(int i) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL20.glEnableVertexAttribArray(i);
    }

    public static void _drawArrays(int i, int j, int k) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glDrawArrays(i, j, k);
    }

    public static void _lineWidth(float f) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glLineWidth(f);
    }

    public static void _pixelStore(int i, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
        GL11.glPixelStorei(i, j);
    }

    public static void _pixelTransfer(int i, float f) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glPixelTransferf(i, f);
    }

    public static void _readPixels(int i, int j, int k, int l, int m, int n, ByteBuffer byteBuffer) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GL11.glReadPixels(i, j, k, l, m, n, byteBuffer);
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
        TEXTURES = (TextureState[])IntStream.range(0, 8).mapToObj(i -> new TextureState()).toArray(TextureState[]::new);
        shadeModel = 7425;
        RESCALE_NORMAL = new BooleanState(32826);
        COLOR_MASK = new ColorMask();
        COLOR = new Color();
        WHITE_TEXTURE = Util.make(new DynamicTexture(16, 16, false), dynamicTexture -> {
            dynamicTexture.getPixels().untrack();
            for (int i = 0; i < 16; ++i) {
                for (int j = 0; j < 16; ++j) {
                    dynamicTexture.getPixels().setPixelRGBA(j, i, -1);
                }
            }
            dynamicTexture.upload();
        });
    }

    @Environment(value=EnvType.CLIENT)
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
    static class Color {
        public float r = 1.0f;
        public float g = 1.0f;
        public float b = 1.0f;
        public float a = 1.0f;

        public Color() {
            this(1.0f, 1.0f, 1.0f, 1.0f);
        }

        public Color(float f, float g, float h, float i) {
            this.r = f;
            this.g = g;
            this.b = h;
            this.a = i;
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
    public static enum TexGen {
        S,
        T,
        R,
        Q;

    }

    @Environment(value=EnvType.CLIENT)
    static class TexGenCoord {
        public final BooleanState enable;
        public final int coord;
        public int mode = -1;

        public TexGenCoord(int i, int j) {
            this.coord = i;
            this.enable = new BooleanState(j);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class TexGenState {
        public final TexGenCoord s = new TexGenCoord(8192, 3168);
        public final TexGenCoord t = new TexGenCoord(8193, 3169);
        public final TexGenCoord r = new TexGenCoord(8194, 3170);
        public final TexGenCoord q = new TexGenCoord(8195, 3171);

        private TexGenState() {
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
    static class ClearState {
        public double depth = 1.0;
        public final Color color = new Color(0.0f, 0.0f, 0.0f, 0.0f);
        public int stencil;

        private ClearState() {
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
    static class FogState {
        public final BooleanState enable = new BooleanState(2912);
        public int mode = 2048;
        public float density = 1.0f;
        public float start;
        public float end = 1.0f;

        private FogState() {
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
    static class ColorMaterialState {
        public final BooleanState enable = new BooleanState(2903);
        public int face = 1032;
        public int mode = 5634;

        private ColorMaterialState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class AlphaState {
        public final BooleanState mode = new BooleanState(3008);
        public int func = 519;
        public float reference = -1.0f;

        private AlphaState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class TextureState {
        public final BooleanState enable = new BooleanState(3553);
        public int binding;

        private TextureState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum FboMode {
        BASE,
        ARB,
        EXT;

    }

    @Environment(value=EnvType.CLIENT)
    public static enum Viewport {
        INSTANCE;

        protected int x;
        protected int y;
        protected int width;
        protected int height;
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

    @Environment(value=EnvType.CLIENT)
    public static enum CullFace {
        FRONT(1028),
        BACK(1029),
        FRONT_AND_BACK(1032);

        public final int value;

        private CullFace(int j) {
            this.value = j;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum FogMode {
        LINEAR(9729),
        EXP(2048),
        EXP2(2049);

        public final int value;

        private FogMode(int j) {
            this.value = j;
        }
    }
}

