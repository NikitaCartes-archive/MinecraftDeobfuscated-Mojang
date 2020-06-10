package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.EXTFramebufferBlit;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class GlStateManager {
	private static final FloatBuffer MATRIX_BUFFER = GLX.make(
		MemoryUtil.memAllocFloat(16), floatBuffer -> DebugMemoryUntracker.untrack(MemoryUtil.memAddress(floatBuffer))
	);
	private static final GlStateManager.AlphaState ALPHA_TEST = new GlStateManager.AlphaState();
	private static final GlStateManager.BooleanState LIGHTING = new GlStateManager.BooleanState(2896);
	private static final GlStateManager.BooleanState[] LIGHT_ENABLE = (GlStateManager.BooleanState[])IntStream.range(0, 8)
		.mapToObj(i -> new GlStateManager.BooleanState(16384 + i))
		.toArray(GlStateManager.BooleanState[]::new);
	private static final GlStateManager.ColorMaterialState COLOR_MATERIAL = new GlStateManager.ColorMaterialState();
	private static final GlStateManager.BlendState BLEND = new GlStateManager.BlendState();
	private static final GlStateManager.DepthState DEPTH = new GlStateManager.DepthState();
	private static final GlStateManager.FogState FOG = new GlStateManager.FogState();
	private static final GlStateManager.CullState CULL = new GlStateManager.CullState();
	private static final GlStateManager.PolygonOffsetState POLY_OFFSET = new GlStateManager.PolygonOffsetState();
	private static final GlStateManager.ColorLogicState COLOR_LOGIC = new GlStateManager.ColorLogicState();
	private static final GlStateManager.TexGenState TEX_GEN = new GlStateManager.TexGenState();
	private static final GlStateManager.StencilState STENCIL = new GlStateManager.StencilState();
	private static final FloatBuffer FLOAT_ARG_BUFFER = MemoryTracker.createFloatBuffer(4);
	private static int activeTexture;
	private static final GlStateManager.TextureState[] TEXTURES = (GlStateManager.TextureState[])IntStream.range(0, 12)
		.mapToObj(i -> new GlStateManager.TextureState())
		.toArray(GlStateManager.TextureState[]::new);
	private static int shadeModel = 7425;
	private static final GlStateManager.BooleanState RESCALE_NORMAL = new GlStateManager.BooleanState(32826);
	private static final GlStateManager.ColorMask COLOR_MASK = new GlStateManager.ColorMask();
	private static final GlStateManager.Color COLOR = new GlStateManager.Color();
	private static GlStateManager.FboMode fboMode;
	private static GlStateManager.FboBlitMode fboBlitMode;

	@Deprecated
	public static void _pushLightingAttributes() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glPushAttrib(8256);
	}

	@Deprecated
	public static void _pushTextureAttributes() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glPushAttrib(270336);
	}

	@Deprecated
	public static void _popAttributes() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glPopAttrib();
	}

	@Deprecated
	public static void _disableAlphaTest() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		ALPHA_TEST.mode.disable();
	}

	@Deprecated
	public static void _enableAlphaTest() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		ALPHA_TEST.mode.enable();
	}

	@Deprecated
	public static void _alphaFunc(int i, float f) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		if (i != ALPHA_TEST.func || f != ALPHA_TEST.reference) {
			ALPHA_TEST.func = i;
			ALPHA_TEST.reference = f;
			GL11.glAlphaFunc(i, f);
		}
	}

	@Deprecated
	public static void _enableLighting() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		LIGHTING.enable();
	}

	@Deprecated
	public static void _disableLighting() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		LIGHTING.disable();
	}

	@Deprecated
	public static void _enableLight(int i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		LIGHT_ENABLE[i].enable();
	}

	@Deprecated
	public static void _enableColorMaterial() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		COLOR_MATERIAL.enable.enable();
	}

	@Deprecated
	public static void _disableColorMaterial() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		COLOR_MATERIAL.enable.disable();
	}

	@Deprecated
	public static void _colorMaterial(int i, int j) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		if (i != COLOR_MATERIAL.face || j != COLOR_MATERIAL.mode) {
			COLOR_MATERIAL.face = i;
			COLOR_MATERIAL.mode = j;
			GL11.glColorMaterial(i, j);
		}
	}

	@Deprecated
	public static void _light(int i, int j, FloatBuffer floatBuffer) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glLightfv(i, j, floatBuffer);
	}

	@Deprecated
	public static void _lightModel(int i, FloatBuffer floatBuffer) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glLightModelfv(i, floatBuffer);
	}

	@Deprecated
	public static void _normal3f(float f, float g, float h) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glNormal3f(f, g, h);
	}

	public static void _disableDepthTest() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		DEPTH.mode.disable();
	}

	public static void _enableDepthTest() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		DEPTH.mode.enable();
	}

	public static void _depthFunc(int i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		if (i != DEPTH.func) {
			DEPTH.func = i;
			GL11.glDepthFunc(i);
		}
	}

	public static void _depthMask(boolean bl) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		if (bl != DEPTH.mask) {
			DEPTH.mask = bl;
			GL11.glDepthMask(bl);
		}
	}

	public static void _disableBlend() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		BLEND.mode.disable();
	}

	public static void _enableBlend() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		BLEND.mode.enable();
	}

	public static void _blendFunc(int i, int j) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		if (i != BLEND.srcRgb || j != BLEND.dstRgb) {
			BLEND.srcRgb = i;
			BLEND.dstRgb = j;
			GL11.glBlendFunc(i, j);
		}
	}

	public static void _blendFuncSeparate(int i, int j, int k, int l) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		if (i != BLEND.srcRgb || j != BLEND.dstRgb || k != BLEND.srcAlpha || l != BLEND.dstAlpha) {
			BLEND.srcRgb = i;
			BLEND.dstRgb = j;
			BLEND.srcAlpha = k;
			BLEND.dstAlpha = l;
			glBlendFuncSeparate(i, j, k, l);
		}
	}

	public static void _blendColor(float f, float g, float h, float i) {
		GL14.glBlendColor(f, g, h, i);
	}

	public static void _blendEquation(int i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL14.glBlendEquation(i);
	}

	public static String _init_fbo(GLCapabilities gLCapabilities) {
		RenderSystem.assertThread(RenderSystem::isInInitPhase);
		if (gLCapabilities.OpenGL30) {
			fboBlitMode = GlStateManager.FboBlitMode.BASE;
		} else if (gLCapabilities.GL_EXT_framebuffer_blit) {
			fboBlitMode = GlStateManager.FboBlitMode.EXT;
		} else {
			fboBlitMode = GlStateManager.FboBlitMode.NONE;
		}

		if (gLCapabilities.OpenGL30) {
			fboMode = GlStateManager.FboMode.BASE;
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
		} else if (gLCapabilities.GL_ARB_framebuffer_object) {
			fboMode = GlStateManager.FboMode.ARB;
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
		} else if (gLCapabilities.GL_EXT_framebuffer_object) {
			fboMode = GlStateManager.FboMode.EXT;
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
		} else {
			throw new IllegalStateException("Could not initialize framebuffer support.");
		}
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

	public static void _glCopyTexSubImage2D(int i, int j, int k, int l, int m, int n, int o, int p) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL20.glCopyTexSubImage2D(i, j, k, l, m, n, o, p);
	}

	public static void _glBindFramebuffer(int i, int j) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		switch (fboMode) {
			case BASE:
				GL30.glBindFramebuffer(i, j);
				break;
			case ARB:
				ARBFramebufferObject.glBindFramebuffer(i, j);
				break;
			case EXT:
				EXTFramebufferObject.glBindFramebufferEXT(i, j);
		}
	}

	public static int getFramebufferDepthTexture() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		switch (fboMode) {
			case BASE:
				if (GL30.glGetFramebufferAttachmentParameteri(36160, 36096, 36048) == 5890) {
					return GL30.glGetFramebufferAttachmentParameteri(36160, 36096, 36049);
				}
				break;
			case ARB:
				if (ARBFramebufferObject.glGetFramebufferAttachmentParameteri(36160, 36096, 36048) == 5890) {
					return ARBFramebufferObject.glGetFramebufferAttachmentParameteri(36160, 36096, 36049);
				}
				break;
			case EXT:
				if (EXTFramebufferObject.glGetFramebufferAttachmentParameteriEXT(36160, 36096, 36048) == 5890) {
					return EXTFramebufferObject.glGetFramebufferAttachmentParameteriEXT(36160, 36096, 36049);
				}
		}

		return 0;
	}

	public static void _glBlitFrameBuffer(int i, int j, int k, int l, int m, int n, int o, int p, int q, int r) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		switch (fboBlitMode) {
			case BASE:
				GL30.glBlitFramebuffer(i, j, k, l, m, n, o, p, q, r);
				break;
			case EXT:
				EXTFramebufferBlit.glBlitFramebufferEXT(i, j, k, l, m, n, o, p, q, r);
			case NONE:
		}
	}

	public static void _glDeleteFramebuffers(int i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		switch (fboMode) {
			case BASE:
				GL30.glDeleteFramebuffers(i);
				break;
			case ARB:
				ARBFramebufferObject.glDeleteFramebuffers(i);
				break;
			case EXT:
				EXTFramebufferObject.glDeleteFramebuffersEXT(i);
		}
	}

	public static int glGenFramebuffers() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		switch (fboMode) {
			case BASE:
				return GL30.glGenFramebuffers();
			case ARB:
				return ARBFramebufferObject.glGenFramebuffers();
			case EXT:
				return EXTFramebufferObject.glGenFramebuffersEXT();
			default:
				return -1;
		}
	}

	public static int glCheckFramebufferStatus(int i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		switch (fboMode) {
			case BASE:
				return GL30.glCheckFramebufferStatus(i);
			case ARB:
				return ARBFramebufferObject.glCheckFramebufferStatus(i);
			case EXT:
				return EXTFramebufferObject.glCheckFramebufferStatusEXT(i);
			default:
				return -1;
		}
	}

	public static void _glFramebufferTexture2D(int i, int j, int k, int l, int m) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		switch (fboMode) {
			case BASE:
				GL30.glFramebufferTexture2D(i, j, k, l, m);
				break;
			case ARB:
				ARBFramebufferObject.glFramebufferTexture2D(i, j, k, l, m);
				break;
			case EXT:
				EXTFramebufferObject.glFramebufferTexture2DEXT(i, j, k, l, m);
		}
	}

	@Deprecated
	public static int getActiveTextureName() {
		return TEXTURES[activeTexture].binding;
	}

	public static void glActiveTexture(int i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL13.glActiveTexture(i);
	}

	@Deprecated
	public static void _glClientActiveTexture(int i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL13.glClientActiveTexture(i);
	}

	@Deprecated
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

	public static void setupOutline() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		_texEnv(8960, 8704, 34160);
		color1arg(7681, 34168);
	}

	public static void teardownOutline() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		_texEnv(8960, 8704, 8448);
		color3arg(8448, 5890, 34168, 34166);
	}

	public static void setupOverlayColor(int i, int j) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		_activeTexture(33985);
		_enableTexture();
		_matrixMode(5890);
		_loadIdentity();
		float f = 1.0F / (float)(j - 1);
		_scalef(f, f, f);
		_matrixMode(5888);
		_bindTexture(i);
		_texParameter(3553, 10241, 9728);
		_texParameter(3553, 10240, 9728);
		_texParameter(3553, 10242, 10496);
		_texParameter(3553, 10243, 10496);
		_texEnv(8960, 8704, 34160);
		color3arg(34165, 34168, 5890, 5890);
		alpha1arg(7681, 34168);
		_activeTexture(33984);
	}

	public static void teardownOverlayColor() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		_activeTexture(33985);
		_disableTexture();
		_activeTexture(33984);
	}

	private static void color1arg(int i, int j) {
		_texEnv(8960, 34161, i);
		_texEnv(8960, 34176, j);
		_texEnv(8960, 34192, 768);
	}

	private static void color3arg(int i, int j, int k, int l) {
		_texEnv(8960, 34161, i);
		_texEnv(8960, 34176, j);
		_texEnv(8960, 34192, 768);
		_texEnv(8960, 34177, k);
		_texEnv(8960, 34193, 768);
		_texEnv(8960, 34178, l);
		_texEnv(8960, 34194, 770);
	}

	private static void alpha1arg(int i, int j) {
		_texEnv(8960, 34162, i);
		_texEnv(8960, 34184, j);
		_texEnv(8960, 34200, 770);
	}

	public static void setupLevelDiffuseLighting(Vector3f vector3f, Vector3f vector3f2, Matrix4f matrix4f) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		_pushMatrix();
		_loadIdentity();
		_enableLight(0);
		_enableLight(1);
		Vector4f vector4f = new Vector4f(vector3f);
		vector4f.transform(matrix4f);
		_light(16384, 4611, getBuffer(vector4f.x(), vector4f.y(), vector4f.z(), 0.0F));
		float f = 0.6F;
		_light(16384, 4609, getBuffer(0.6F, 0.6F, 0.6F, 1.0F));
		_light(16384, 4608, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
		_light(16384, 4610, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
		Vector4f vector4f2 = new Vector4f(vector3f2);
		vector4f2.transform(matrix4f);
		_light(16385, 4611, getBuffer(vector4f2.x(), vector4f2.y(), vector4f2.z(), 0.0F));
		_light(16385, 4609, getBuffer(0.6F, 0.6F, 0.6F, 1.0F));
		_light(16385, 4608, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
		_light(16385, 4610, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
		_shadeModel(7424);
		float g = 0.4F;
		_lightModel(2899, getBuffer(0.4F, 0.4F, 0.4F, 1.0F));
		_popMatrix();
	}

	public static void setupGuiFlatDiffuseLighting(Vector3f vector3f, Vector3f vector3f2) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.setIdentity();
		matrix4f.multiply(Matrix4f.createScaleMatrix(1.0F, -1.0F, 1.0F));
		matrix4f.multiply(Vector3f.YP.rotationDegrees(-22.5F));
		matrix4f.multiply(Vector3f.XP.rotationDegrees(135.0F));
		setupLevelDiffuseLighting(vector3f, vector3f2, matrix4f);
	}

	public static void setupGui3DDiffuseLighting(Vector3f vector3f, Vector3f vector3f2) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.setIdentity();
		matrix4f.multiply(Vector3f.YP.rotationDegrees(62.0F));
		matrix4f.multiply(Vector3f.XP.rotationDegrees(185.5F));
		matrix4f.multiply(Matrix4f.createScaleMatrix(1.0F, -1.0F, 1.0F));
		matrix4f.multiply(Vector3f.YP.rotationDegrees(-22.5F));
		matrix4f.multiply(Vector3f.XP.rotationDegrees(135.0F));
		setupLevelDiffuseLighting(vector3f, vector3f2, matrix4f);
	}

	private static FloatBuffer getBuffer(float f, float g, float h, float i) {
		FLOAT_ARG_BUFFER.clear();
		FLOAT_ARG_BUFFER.put(f).put(g).put(h).put(i);
		FLOAT_ARG_BUFFER.flip();
		return FLOAT_ARG_BUFFER;
	}

	public static void setupEndPortalTexGen() {
		_texGenMode(GlStateManager.TexGen.S, 9216);
		_texGenMode(GlStateManager.TexGen.T, 9216);
		_texGenMode(GlStateManager.TexGen.R, 9216);
		_texGenParam(GlStateManager.TexGen.S, 9474, getBuffer(1.0F, 0.0F, 0.0F, 0.0F));
		_texGenParam(GlStateManager.TexGen.T, 9474, getBuffer(0.0F, 1.0F, 0.0F, 0.0F));
		_texGenParam(GlStateManager.TexGen.R, 9474, getBuffer(0.0F, 0.0F, 1.0F, 0.0F));
		_enableTexGen(GlStateManager.TexGen.S);
		_enableTexGen(GlStateManager.TexGen.T);
		_enableTexGen(GlStateManager.TexGen.R);
	}

	public static void clearTexGen() {
		_disableTexGen(GlStateManager.TexGen.S);
		_disableTexGen(GlStateManager.TexGen.T);
		_disableTexGen(GlStateManager.TexGen.R);
	}

	public static void mulTextureByProjModelView() {
		_getMatrix(2983, MATRIX_BUFFER);
		_multMatrix(MATRIX_BUFFER);
		_getMatrix(2982, MATRIX_BUFFER);
		_multMatrix(MATRIX_BUFFER);
	}

	@Deprecated
	public static void _enableFog() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		FOG.enable.enable();
	}

	@Deprecated
	public static void _disableFog() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		FOG.enable.disable();
	}

	@Deprecated
	public static void _fogMode(int i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		if (i != FOG.mode) {
			FOG.mode = i;
			_fogi(2917, i);
		}
	}

	@Deprecated
	public static void _fogDensity(float f) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		if (f != FOG.density) {
			FOG.density = f;
			GL11.glFogf(2914, f);
		}
	}

	@Deprecated
	public static void _fogStart(float f) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		if (f != FOG.start) {
			FOG.start = f;
			GL11.glFogf(2915, f);
		}
	}

	@Deprecated
	public static void _fogEnd(float f) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		if (f != FOG.end) {
			FOG.end = f;
			GL11.glFogf(2916, f);
		}
	}

	@Deprecated
	public static void _fog(int i, float[] fs) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glFogfv(i, fs);
	}

	@Deprecated
	public static void _fogi(int i, int j) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glFogi(i, j);
	}

	public static void _enableCull() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		CULL.enable.enable();
	}

	public static void _disableCull() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		CULL.enable.disable();
	}

	public static void _polygonMode(int i, int j) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glPolygonMode(i, j);
	}

	public static void _enablePolygonOffset() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		POLY_OFFSET.fill.enable();
	}

	public static void _disablePolygonOffset() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		POLY_OFFSET.fill.disable();
	}

	public static void _enableLineOffset() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		POLY_OFFSET.line.enable();
	}

	public static void _disableLineOffset() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		POLY_OFFSET.line.disable();
	}

	public static void _polygonOffset(float f, float g) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		if (f != POLY_OFFSET.factor || g != POLY_OFFSET.units) {
			POLY_OFFSET.factor = f;
			POLY_OFFSET.units = g;
			GL11.glPolygonOffset(f, g);
		}
	}

	public static void _enableColorLogicOp() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		COLOR_LOGIC.enable.enable();
	}

	public static void _disableColorLogicOp() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		COLOR_LOGIC.enable.disable();
	}

	public static void _logicOp(int i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		if (i != COLOR_LOGIC.op) {
			COLOR_LOGIC.op = i;
			GL11.glLogicOp(i);
		}
	}

	@Deprecated
	public static void _enableTexGen(GlStateManager.TexGen texGen) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		getTexGen(texGen).enable.enable();
	}

	@Deprecated
	public static void _disableTexGen(GlStateManager.TexGen texGen) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		getTexGen(texGen).enable.disable();
	}

	@Deprecated
	public static void _texGenMode(GlStateManager.TexGen texGen, int i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GlStateManager.TexGenCoord texGenCoord = getTexGen(texGen);
		if (i != texGenCoord.mode) {
			texGenCoord.mode = i;
			GL11.glTexGeni(texGenCoord.coord, 9472, i);
		}
	}

	@Deprecated
	public static void _texGenParam(GlStateManager.TexGen texGen, int i, FloatBuffer floatBuffer) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glTexGenfv(getTexGen(texGen).coord, i, floatBuffer);
	}

	@Deprecated
	private static GlStateManager.TexGenCoord getTexGen(GlStateManager.TexGen texGen) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		switch (texGen) {
			case S:
				return TEX_GEN.s;
			case T:
				return TEX_GEN.t;
			case R:
				return TEX_GEN.r;
			case Q:
				return TEX_GEN.q;
			default:
				return TEX_GEN.s;
		}
	}

	public static void _activeTexture(int i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		if (activeTexture != i - 33984) {
			activeTexture = i - 33984;
			glActiveTexture(i);
		}
	}

	public static void _enableTexture() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		TEXTURES[activeTexture].enable.enable();
	}

	public static void _disableTexture() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		TEXTURES[activeTexture].enable.disable();
	}

	@Deprecated
	public static void _texEnv(int i, int j, int k) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glTexEnvi(i, j, k);
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

		for (GlStateManager.TextureState textureState : TEXTURES) {
			if (textureState.binding == i) {
				textureState.binding = -1;
			}
		}
	}

	public static void _bindTexture(int i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		if (i != TEXTURES[activeTexture].binding) {
			TEXTURES[activeTexture].binding = i;
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

	public static void _getTexImage(int i, int j, int k, int l, long m) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glGetTexImage(i, j, k, l, m);
	}

	@Deprecated
	public static void _shadeModel(int i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		if (i != shadeModel) {
			shadeModel = i;
			GL11.glShadeModel(i);
		}
	}

	@Deprecated
	public static void _enableRescaleNormal() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		RESCALE_NORMAL.enable();
	}

	@Deprecated
	public static void _disableRescaleNormal() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		RESCALE_NORMAL.disable();
	}

	public static void _viewport(int i, int j, int k, int l) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GlStateManager.Viewport.INSTANCE.x = i;
		GlStateManager.Viewport.INSTANCE.y = j;
		GlStateManager.Viewport.INSTANCE.width = k;
		GlStateManager.Viewport.INSTANCE.height = l;
		GL11.glViewport(i, j, k, l);
	}

	public static void _colorMask(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		if (bl != COLOR_MASK.red || bl2 != COLOR_MASK.green || bl3 != COLOR_MASK.blue || bl4 != COLOR_MASK.alpha) {
			COLOR_MASK.red = bl;
			COLOR_MASK.green = bl2;
			COLOR_MASK.blue = bl3;
			COLOR_MASK.alpha = bl4;
			GL11.glColorMask(bl, bl2, bl3, bl4);
		}
	}

	public static void _stencilFunc(int i, int j, int k) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		if (i != STENCIL.func.func || i != STENCIL.func.ref || i != STENCIL.func.mask) {
			STENCIL.func.func = i;
			STENCIL.func.ref = j;
			STENCIL.func.mask = k;
			GL11.glStencilFunc(i, j, k);
		}
	}

	public static void _stencilMask(int i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		if (i != STENCIL.mask) {
			STENCIL.mask = i;
			GL11.glStencilMask(i);
		}
	}

	public static void _stencilOp(int i, int j, int k) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		if (i != STENCIL.fail || j != STENCIL.zfail || k != STENCIL.zpass) {
			STENCIL.fail = i;
			STENCIL.zfail = j;
			STENCIL.zpass = k;
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
			_getError();
		}
	}

	@Deprecated
	public static void _matrixMode(int i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL11.glMatrixMode(i);
	}

	@Deprecated
	public static void _loadIdentity() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GL11.glLoadIdentity();
	}

	@Deprecated
	public static void _pushMatrix() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glPushMatrix();
	}

	@Deprecated
	public static void _popMatrix() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glPopMatrix();
	}

	@Deprecated
	public static void _getMatrix(int i, FloatBuffer floatBuffer) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glGetFloatv(i, floatBuffer);
	}

	@Deprecated
	public static void _ortho(double d, double e, double f, double g, double h, double i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glOrtho(d, e, f, g, h, i);
	}

	@Deprecated
	public static void _rotatef(float f, float g, float h, float i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glRotatef(f, g, h, i);
	}

	@Deprecated
	public static void _scalef(float f, float g, float h) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glScalef(f, g, h);
	}

	@Deprecated
	public static void _scaled(double d, double e, double f) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glScaled(d, e, f);
	}

	@Deprecated
	public static void _translatef(float f, float g, float h) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glTranslatef(f, g, h);
	}

	@Deprecated
	public static void _translated(double d, double e, double f) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glTranslated(d, e, f);
	}

	@Deprecated
	public static void _multMatrix(FloatBuffer floatBuffer) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glMultMatrixf(floatBuffer);
	}

	@Deprecated
	public static void _multMatrix(Matrix4f matrix4f) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		matrix4f.store(MATRIX_BUFFER);
		MATRIX_BUFFER.rewind();
		_multMatrix(MATRIX_BUFFER);
	}

	@Deprecated
	public static void _color4f(float f, float g, float h, float i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		if (f != COLOR.r || g != COLOR.g || h != COLOR.b || i != COLOR.a) {
			COLOR.r = f;
			COLOR.g = g;
			COLOR.b = h;
			COLOR.a = i;
			GL11.glColor4f(f, g, h, i);
		}
	}

	@Deprecated
	public static void _clearCurrentColor() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		COLOR.r = -1.0F;
		COLOR.g = -1.0F;
		COLOR.b = -1.0F;
		COLOR.a = -1.0F;
	}

	@Deprecated
	public static void _normalPointer(int i, int j, long l) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glNormalPointer(i, j, l);
	}

	@Deprecated
	public static void _texCoordPointer(int i, int j, int k, long l) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glTexCoordPointer(i, j, k, l);
	}

	@Deprecated
	public static void _vertexPointer(int i, int j, int k, long l) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glVertexPointer(i, j, k, l);
	}

	@Deprecated
	public static void _colorPointer(int i, int j, int k, long l) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glColorPointer(i, j, k, l);
	}

	public static void _vertexAttribPointer(int i, int j, int k, boolean bl, int l, long m) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL20.glVertexAttribPointer(i, j, k, bl, l, m);
	}

	@Deprecated
	public static void _enableClientState(int i) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glEnableClientState(i);
	}

	@Deprecated
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

	public static boolean supportsFramebufferBlit() {
		return fboBlitMode != GlStateManager.FboBlitMode.NONE;
	}

	@Deprecated
	@Environment(EnvType.CLIENT)
	static class AlphaState {
		public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(3008);
		public int func = 519;
		public float reference = -1.0F;

		private AlphaState() {
		}
	}

	@Environment(EnvType.CLIENT)
	static class BlendState {
		public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(3042);
		public int srcRgb = 1;
		public int dstRgb = 0;
		public int srcAlpha = 1;
		public int dstAlpha = 0;

		private BlendState() {
		}
	}

	@Environment(EnvType.CLIENT)
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

	@Deprecated
	@Environment(EnvType.CLIENT)
	static class Color {
		public float r = 1.0F;
		public float g = 1.0F;
		public float b = 1.0F;
		public float a = 1.0F;

		public Color() {
			this(1.0F, 1.0F, 1.0F, 1.0F);
		}

		public Color(float f, float g, float h, float i) {
			this.r = f;
			this.g = g;
			this.b = h;
			this.a = i;
		}
	}

	@Environment(EnvType.CLIENT)
	static class ColorLogicState {
		public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(3058);
		public int op = 5379;

		private ColorLogicState() {
		}
	}

	@Environment(EnvType.CLIENT)
	static class ColorMask {
		public boolean red = true;
		public boolean green = true;
		public boolean blue = true;
		public boolean alpha = true;

		private ColorMask() {
		}
	}

	@Deprecated
	@Environment(EnvType.CLIENT)
	static class ColorMaterialState {
		public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(2903);
		public int face = 1032;
		public int mode = 5634;

		private ColorMaterialState() {
		}
	}

	@Environment(EnvType.CLIENT)
	static class CullState {
		public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(2884);
		public int mode = 1029;

		private CullState() {
		}
	}

	@Environment(EnvType.CLIENT)
	static class DepthState {
		public final GlStateManager.BooleanState mode = new GlStateManager.BooleanState(2929);
		public boolean mask = true;
		public int func = 513;

		private DepthState() {
		}
	}

	@Environment(EnvType.CLIENT)
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

	@Environment(EnvType.CLIENT)
	public static enum FboBlitMode {
		BASE,
		EXT,
		NONE;
	}

	@Environment(EnvType.CLIENT)
	public static enum FboMode {
		BASE,
		ARB,
		EXT;
	}

	@Deprecated
	@Environment(EnvType.CLIENT)
	public static enum FogMode {
		LINEAR(9729),
		EXP(2048),
		EXP2(2049);

		public final int value;

		private FogMode(int j) {
			this.value = j;
		}
	}

	@Deprecated
	@Environment(EnvType.CLIENT)
	static class FogState {
		public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(2912);
		public int mode = 2048;
		public float density = 1.0F;
		public float start;
		public float end = 1.0F;

		private FogState() {
		}
	}

	@Environment(EnvType.CLIENT)
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

	@Environment(EnvType.CLIENT)
	static class PolygonOffsetState {
		public final GlStateManager.BooleanState fill = new GlStateManager.BooleanState(32823);
		public final GlStateManager.BooleanState line = new GlStateManager.BooleanState(10754);
		public float factor;
		public float units;

		private PolygonOffsetState() {
		}
	}

	@Environment(EnvType.CLIENT)
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

	@Environment(EnvType.CLIENT)
	static class StencilFunc {
		public int func = 519;
		public int ref;
		public int mask = -1;

		private StencilFunc() {
		}
	}

	@Environment(EnvType.CLIENT)
	static class StencilState {
		public final GlStateManager.StencilFunc func = new GlStateManager.StencilFunc();
		public int mask = -1;
		public int fail = 7680;
		public int zfail = 7680;
		public int zpass = 7680;

		private StencilState() {
		}
	}

	@Deprecated
	@Environment(EnvType.CLIENT)
	public static enum TexGen {
		S,
		T,
		R,
		Q;
	}

	@Deprecated
	@Environment(EnvType.CLIENT)
	static class TexGenCoord {
		public final GlStateManager.BooleanState enable;
		public final int coord;
		public int mode = -1;

		public TexGenCoord(int i, int j) {
			this.coord = i;
			this.enable = new GlStateManager.BooleanState(j);
		}
	}

	@Deprecated
	@Environment(EnvType.CLIENT)
	static class TexGenState {
		public final GlStateManager.TexGenCoord s = new GlStateManager.TexGenCoord(8192, 3168);
		public final GlStateManager.TexGenCoord t = new GlStateManager.TexGenCoord(8193, 3169);
		public final GlStateManager.TexGenCoord r = new GlStateManager.TexGenCoord(8194, 3170);
		public final GlStateManager.TexGenCoord q = new GlStateManager.TexGenCoord(8195, 3171);

		private TexGenState() {
		}
	}

	@Environment(EnvType.CLIENT)
	static class TextureState {
		public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(3553);
		public int binding;

		private TextureState() {
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum Viewport {
		INSTANCE;

		protected int x;
		protected int y;
		protected int width;
		protected int height;
	}
}
