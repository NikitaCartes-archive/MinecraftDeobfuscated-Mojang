package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

@Environment(EnvType.CLIENT)
public class GlStateManager {
	private static final FloatBuffer MATRIX_BUFFER = GLX.make(
		MemoryUtil.memAllocFloat(16), floatBuffer -> DebugMemoryUntracker.untrack(MemoryUtil.memAddress(floatBuffer))
	);
	private static final FloatBuffer COLOR_BUFFER = GLX.make(
		MemoryUtil.memAllocFloat(4), floatBuffer -> DebugMemoryUntracker.untrack(MemoryUtil.memAddress(floatBuffer))
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
	private static final GlStateManager.ClearState CLEAR = new GlStateManager.ClearState();
	private static final GlStateManager.StencilState STENCIL = new GlStateManager.StencilState();
	private static final GlStateManager.BooleanState NORMALIZE = new GlStateManager.BooleanState(2977);
	private static int activeTexture;
	private static final GlStateManager.TextureState[] TEXTURES = (GlStateManager.TextureState[])IntStream.range(0, 8)
		.mapToObj(i -> new GlStateManager.TextureState())
		.toArray(GlStateManager.TextureState[]::new);
	private static int shadeModel = 7425;
	private static final GlStateManager.BooleanState RESCALE_NORMAL = new GlStateManager.BooleanState(32826);
	private static final GlStateManager.ColorMask COLOR_MASK = new GlStateManager.ColorMask();
	private static final GlStateManager.Color COLOR = new GlStateManager.Color();
	private static GlStateManager.FboMode fboMode;

	public static void _pushLightingAttributes() {
		GL11.glPushAttrib(8256);
	}

	public static void _pushTextureAttributes() {
		GL11.glPushAttrib(270336);
	}

	public static void _popAttributes() {
		GL11.glPopAttrib();
	}

	public static void _disableAlphaTest() {
		ALPHA_TEST.mode.disable();
	}

	public static void _enableAlphaTest() {
		ALPHA_TEST.mode.enable();
	}

	public static void _alphaFunc(int i, float f) {
		if (i != ALPHA_TEST.func || f != ALPHA_TEST.reference) {
			ALPHA_TEST.func = i;
			ALPHA_TEST.reference = f;
			GL11.glAlphaFunc(i, f);
		}
	}

	public static void _enableLighting() {
		LIGHTING.enable();
	}

	public static void _disableLighting() {
		LIGHTING.disable();
	}

	public static void _enableLight(int i) {
		LIGHT_ENABLE[i].enable();
	}

	public static void _disableLight(int i) {
		LIGHT_ENABLE[i].disable();
	}

	public static void _enableColorMaterial() {
		COLOR_MATERIAL.enable.enable();
	}

	public static void _disableColorMaterial() {
		COLOR_MATERIAL.enable.disable();
	}

	public static void _colorMaterial(int i, int j) {
		if (i != COLOR_MATERIAL.face || j != COLOR_MATERIAL.mode) {
			COLOR_MATERIAL.face = i;
			COLOR_MATERIAL.mode = j;
			GL11.glColorMaterial(i, j);
		}
	}

	public static void _light(int i, int j, FloatBuffer floatBuffer) {
		GL11.glLightfv(i, j, floatBuffer);
	}

	public static void _lightModel(int i, FloatBuffer floatBuffer) {
		GL11.glLightModelfv(i, floatBuffer);
	}

	public static void _normal3f(float f, float g, float h) {
		GL11.glNormal3f(f, g, h);
	}

	public static void _disableDepthTest() {
		DEPTH.mode.disable();
	}

	public static void _enableDepthTest() {
		DEPTH.mode.enable();
	}

	public static void _depthFunc(int i) {
		if (i != DEPTH.func) {
			DEPTH.func = i;
			GL11.glDepthFunc(i);
		}
	}

	public static void _depthMask(boolean bl) {
		if (bl != DEPTH.mask) {
			DEPTH.mask = bl;
			GL11.glDepthMask(bl);
		}
	}

	public static void _disableBlend() {
		BLEND.mode.disable();
	}

	public static void _enableBlend() {
		BLEND.mode.enable();
	}

	public static void _blendFunc(int i, int j) {
		if (i != BLEND.srcRgb || j != BLEND.dstRgb) {
			BLEND.srcRgb = i;
			BLEND.dstRgb = j;
			GL11.glBlendFunc(i, j);
		}
	}

	public static void _blendFuncSeparate(int i, int j, int k, int l) {
		if (i != BLEND.srcRgb || j != BLEND.dstRgb || k != BLEND.srcAlpha || l != BLEND.dstAlpha) {
			BLEND.srcRgb = i;
			BLEND.dstRgb = j;
			BLEND.srcAlpha = k;
			BLEND.dstAlpha = l;
			glBlendFuncSeparate(i, j, k, l);
		}
	}

	public static void _blendEquation(int i) {
		GL14.glBlendEquation(i);
	}

	public static void _setupSolidRenderingTextureCombine(int i) {
		COLOR_BUFFER.put(0, (float)(i >> 16 & 0xFF) / 255.0F);
		COLOR_BUFFER.put(1, (float)(i >> 8 & 0xFF) / 255.0F);
		COLOR_BUFFER.put(2, (float)(i >> 0 & 0xFF) / 255.0F);
		COLOR_BUFFER.put(3, (float)(i >> 24 & 0xFF) / 255.0F);
		RenderSystem.texEnv(8960, 8705, COLOR_BUFFER);
		RenderSystem.texEnv(8960, 8704, 34160);
		RenderSystem.texEnv(8960, 34161, 7681);
		RenderSystem.texEnv(8960, 34176, 34166);
		RenderSystem.texEnv(8960, 34192, 768);
		RenderSystem.texEnv(8960, 34162, 7681);
		RenderSystem.texEnv(8960, 34184, 5890);
		RenderSystem.texEnv(8960, 34200, 770);
	}

	public static void _tearDownSolidRenderingTextureCombine() {
		RenderSystem.texEnv(8960, 8704, 8448);
		RenderSystem.texEnv(8960, 34161, 8448);
		RenderSystem.texEnv(8960, 34162, 8448);
		RenderSystem.texEnv(8960, 34176, 5890);
		RenderSystem.texEnv(8960, 34184, 5890);
		RenderSystem.texEnv(8960, 34192, 768);
		RenderSystem.texEnv(8960, 34200, 770);
	}

	public static String _init_fbo(GLCapabilities gLCapabilities) {
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
		return GL20.glGetProgrami(i, j);
	}

	public static void glAttachShader(int i, int j) {
		GL20.glAttachShader(i, j);
	}

	public static void glDeleteShader(int i) {
		GL20.glDeleteShader(i);
	}

	public static int glCreateShader(int i) {
		return GL20.glCreateShader(i);
	}

	public static void glShaderSource(int i, CharSequence charSequence) {
		GL20.glShaderSource(i, charSequence);
	}

	public static void glCompileShader(int i) {
		GL20.glCompileShader(i);
	}

	public static int glGetShaderi(int i, int j) {
		return GL20.glGetShaderi(i, j);
	}

	public static void _glUseProgram(int i) {
		GL20.glUseProgram(i);
	}

	public static int glCreateProgram() {
		return GL20.glCreateProgram();
	}

	public static void glDeleteProgram(int i) {
		GL20.glDeleteProgram(i);
	}

	public static void glLinkProgram(int i) {
		GL20.glLinkProgram(i);
	}

	public static int _glGetUniformLocation(int i, CharSequence charSequence) {
		return GL20.glGetUniformLocation(i, charSequence);
	}

	public static void glUniform1(int i, IntBuffer intBuffer) {
		GL20.glUniform1iv(i, intBuffer);
	}

	public static void _glUniform1i(int i, int j) {
		GL20.glUniform1i(i, j);
	}

	public static void glUniform1(int i, FloatBuffer floatBuffer) {
		GL20.glUniform1fv(i, floatBuffer);
	}

	public static void glUniform2(int i, IntBuffer intBuffer) {
		GL20.glUniform2iv(i, intBuffer);
	}

	public static void glUniform2(int i, FloatBuffer floatBuffer) {
		GL20.glUniform2fv(i, floatBuffer);
	}

	public static void glUniform3(int i, IntBuffer intBuffer) {
		GL20.glUniform3iv(i, intBuffer);
	}

	public static void glUniform3(int i, FloatBuffer floatBuffer) {
		GL20.glUniform3fv(i, floatBuffer);
	}

	public static void glUniform4(int i, IntBuffer intBuffer) {
		GL20.glUniform4iv(i, intBuffer);
	}

	public static void glUniform4(int i, FloatBuffer floatBuffer) {
		GL20.glUniform4fv(i, floatBuffer);
	}

	public static void glUniformMatrix2(int i, boolean bl, FloatBuffer floatBuffer) {
		GL20.glUniformMatrix2fv(i, bl, floatBuffer);
	}

	public static void glUniformMatrix3(int i, boolean bl, FloatBuffer floatBuffer) {
		GL20.glUniformMatrix3fv(i, bl, floatBuffer);
	}

	public static void glUniformMatrix4(int i, boolean bl, FloatBuffer floatBuffer) {
		GL20.glUniformMatrix4fv(i, bl, floatBuffer);
	}

	public static int _glGetAttribLocation(int i, CharSequence charSequence) {
		return GL20.glGetAttribLocation(i, charSequence);
	}

	public static int glGenBuffers() {
		return GL15.glGenBuffers();
	}

	public static void glBindBuffer(int i, int j) {
		GL15.glBindBuffer(i, j);
	}

	public static void glBufferData(int i, ByteBuffer byteBuffer, int j) {
		GL15.glBufferData(i, byteBuffer, j);
	}

	public static void glDeleteBuffers(int i) {
		GL15.glDeleteBuffers(i);
	}

	public static void glBindFramebuffer(int i, int j) {
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

	public static void glBindRenderbuffer(int i, int j) {
		switch (fboMode) {
			case BASE:
				GL30.glBindRenderbuffer(i, j);
				break;
			case ARB:
				ARBFramebufferObject.glBindRenderbuffer(i, j);
				break;
			case EXT:
				EXTFramebufferObject.glBindRenderbufferEXT(i, j);
		}
	}

	public static void glDeleteRenderbuffers(int i) {
		switch (fboMode) {
			case BASE:
				GL30.glDeleteRenderbuffers(i);
				break;
			case ARB:
				ARBFramebufferObject.glDeleteRenderbuffers(i);
				break;
			case EXT:
				EXTFramebufferObject.glDeleteRenderbuffersEXT(i);
		}
	}

	public static void glDeleteFramebuffers(int i) {
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

	public static int glGenRenderbuffers() {
		switch (fboMode) {
			case BASE:
				return GL30.glGenRenderbuffers();
			case ARB:
				return ARBFramebufferObject.glGenRenderbuffers();
			case EXT:
				return EXTFramebufferObject.glGenRenderbuffersEXT();
			default:
				return -1;
		}
	}

	public static void glRenderbufferStorage(int i, int j, int k, int l) {
		switch (fboMode) {
			case BASE:
				GL30.glRenderbufferStorage(i, j, k, l);
				break;
			case ARB:
				ARBFramebufferObject.glRenderbufferStorage(i, j, k, l);
				break;
			case EXT:
				EXTFramebufferObject.glRenderbufferStorageEXT(i, j, k, l);
		}
	}

	public static void glFramebufferRenderbuffer(int i, int j, int k, int l) {
		switch (fboMode) {
			case BASE:
				GL30.glFramebufferRenderbuffer(i, j, k, l);
				break;
			case ARB:
				ARBFramebufferObject.glFramebufferRenderbuffer(i, j, k, l);
				break;
			case EXT:
				EXTFramebufferObject.glFramebufferRenderbufferEXT(i, j, k, l);
		}
	}

	public static int glCheckFramebufferStatus(int i) {
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

	public static void glFramebufferTexture2D(int i, int j, int k, int l, int m) {
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

	public static void glActiveTexture(int i) {
		GL13.glActiveTexture(i);
	}

	public static void _glClientActiveTexture(int i) {
		GL13.glClientActiveTexture(i);
	}

	public static void _glMultiTexCoord2f(int i, float f, float g) {
		GL13.glMultiTexCoord2f(i, f, g);
	}

	public static void glBlendFuncSeparate(int i, int j, int k, int l) {
		GL14.glBlendFuncSeparate(i, j, k, l);
	}

	public static String glGetShaderInfoLog(int i, int j) {
		return GL20.glGetShaderInfoLog(i, j);
	}

	public static String glGetProgramInfoLog(int i, int j) {
		return GL20.glGetProgramInfoLog(i, j);
	}

	public static void _enableFog() {
		FOG.enable.enable();
	}

	public static void _disableFog() {
		FOG.enable.disable();
	}

	public static void _fogMode(int i) {
		if (i != FOG.mode) {
			FOG.mode = i;
			_fogi(2917, i);
		}
	}

	public static void _fogDensity(float f) {
		if (f != FOG.density) {
			FOG.density = f;
			GL11.glFogf(2914, f);
		}
	}

	public static void _fogStart(float f) {
		if (f != FOG.start) {
			FOG.start = f;
			GL11.glFogf(2915, f);
		}
	}

	public static void _fogEnd(float f) {
		if (f != FOG.end) {
			FOG.end = f;
			GL11.glFogf(2916, f);
		}
	}

	public static void _fog(int i, FloatBuffer floatBuffer) {
		GL11.glFogfv(i, floatBuffer);
	}

	public static void _fogi(int i, int j) {
		GL11.glFogi(i, j);
	}

	public static void _enableCull() {
		CULL.enable.enable();
	}

	public static void _disableCull() {
		CULL.enable.disable();
	}

	public static void _cullFace(int i) {
		if (i != CULL.mode) {
			CULL.mode = i;
			GL11.glCullFace(i);
		}
	}

	public static void _polygonMode(int i, int j) {
		GL11.glPolygonMode(i, j);
	}

	public static void _enablePolygonOffset() {
		POLY_OFFSET.fill.enable();
	}

	public static void _disablePolygonOffset() {
		POLY_OFFSET.fill.disable();
	}

	public static void _enableLineOffset() {
		POLY_OFFSET.line.enable();
	}

	public static void _disableLineOffset() {
		POLY_OFFSET.line.disable();
	}

	public static void _polygonOffset(float f, float g) {
		if (f != POLY_OFFSET.factor || g != POLY_OFFSET.units) {
			POLY_OFFSET.factor = f;
			POLY_OFFSET.units = g;
			GL11.glPolygonOffset(f, g);
		}
	}

	public static void _enableColorLogicOp() {
		COLOR_LOGIC.enable.enable();
	}

	public static void _disableColorLogicOp() {
		COLOR_LOGIC.enable.disable();
	}

	public static void _logicOp(int i) {
		if (i != COLOR_LOGIC.op) {
			COLOR_LOGIC.op = i;
			GL11.glLogicOp(i);
		}
	}

	public static void _enableTexGen(GlStateManager.TexGen texGen) {
		getTexGen(texGen).enable.enable();
	}

	public static void _disableTexGen(GlStateManager.TexGen texGen) {
		getTexGen(texGen).enable.disable();
	}

	public static void _texGenMode(GlStateManager.TexGen texGen, int i) {
		GlStateManager.TexGenCoord texGenCoord = getTexGen(texGen);
		if (i != texGenCoord.mode) {
			texGenCoord.mode = i;
			GL11.glTexGeni(texGenCoord.coord, 9472, i);
		}
	}

	public static void _texGenParam(GlStateManager.TexGen texGen, int i, FloatBuffer floatBuffer) {
		GL11.glTexGenfv(getTexGen(texGen).coord, i, floatBuffer);
	}

	private static GlStateManager.TexGenCoord getTexGen(GlStateManager.TexGen texGen) {
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
		if (activeTexture != i - 33984) {
			activeTexture = i - 33984;
			glActiveTexture(i);
		}
	}

	public static void _enableTexture() {
		TEXTURES[activeTexture].enable.enable();
	}

	public static void _disableTexture() {
		TEXTURES[activeTexture].enable.disable();
	}

	public static void _texEnv(int i, int j, FloatBuffer floatBuffer) {
		GL11.glTexEnvfv(i, j, floatBuffer);
	}

	public static void _texEnv(int i, int j, int k) {
		GL11.glTexEnvi(i, j, k);
	}

	public static void _texEnv(int i, int j, float f) {
		GL11.glTexEnvf(i, j, f);
	}

	public static void _texParameter(int i, int j, float f) {
		GL11.glTexParameterf(i, j, f);
	}

	public static void _texParameter(int i, int j, int k) {
		GL11.glTexParameteri(i, j, k);
	}

	public static int _getTexLevelParameter(int i, int j, int k) {
		return GL11.glGetTexLevelParameteri(i, j, k);
	}

	public static int _genTexture() {
		return GL11.glGenTextures();
	}

	public static void _deleteTexture(int i) {
		GL11.glDeleteTextures(i);

		for (GlStateManager.TextureState textureState : TEXTURES) {
			if (textureState.binding == i) {
				textureState.binding = -1;
			}
		}
	}

	public static void _bindTexture(int i) {
		if (i != TEXTURES[activeTexture].binding) {
			TEXTURES[activeTexture].binding = i;
			GL11.glBindTexture(3553, i);
		}
	}

	public static void _texImage2D(int i, int j, int k, int l, int m, int n, int o, int p, @Nullable IntBuffer intBuffer) {
		GL11.glTexImage2D(i, j, k, l, m, n, o, p, intBuffer);
	}

	public static void _texSubImage2D(int i, int j, int k, int l, int m, int n, int o, int p, long q) {
		GL11.glTexSubImage2D(i, j, k, l, m, n, o, p, q);
	}

	public static void _copyTexSubImage2D(int i, int j, int k, int l, int m, int n, int o, int p) {
		GL11.glCopyTexSubImage2D(i, j, k, l, m, n, o, p);
	}

	public static void _getTexImage(int i, int j, int k, int l, long m) {
		GL11.glGetTexImage(i, j, k, l, m);
	}

	public static void _enableNormalize() {
		NORMALIZE.enable();
	}

	public static void _disableNormalize() {
		NORMALIZE.disable();
	}

	public static void _shadeModel(int i) {
		if (i != shadeModel) {
			shadeModel = i;
			GL11.glShadeModel(i);
		}
	}

	public static void _enableRescaleNormal() {
		RESCALE_NORMAL.enable();
	}

	public static void _disableRescaleNormal() {
		RESCALE_NORMAL.disable();
	}

	public static void _viewport(int i, int j, int k, int l) {
		GlStateManager.Viewport.INSTANCE.x = i;
		GlStateManager.Viewport.INSTANCE.y = j;
		GlStateManager.Viewport.INSTANCE.width = k;
		GlStateManager.Viewport.INSTANCE.height = l;
		GL11.glViewport(i, j, k, l);
	}

	public static void _colorMask(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
		if (bl != COLOR_MASK.red || bl2 != COLOR_MASK.green || bl3 != COLOR_MASK.blue || bl4 != COLOR_MASK.alpha) {
			COLOR_MASK.red = bl;
			COLOR_MASK.green = bl2;
			COLOR_MASK.blue = bl3;
			COLOR_MASK.alpha = bl4;
			GL11.glColorMask(bl, bl2, bl3, bl4);
		}
	}

	public static void _stencilFunc(int i, int j, int k) {
		if (i != STENCIL.func.func || i != STENCIL.func.ref || i != STENCIL.func.mask) {
			STENCIL.func.func = i;
			STENCIL.func.ref = j;
			STENCIL.func.mask = k;
			GL11.glStencilFunc(i, j, k);
		}
	}

	public static void _stencilMask(int i) {
		if (i != STENCIL.mask) {
			STENCIL.mask = i;
			GL11.glStencilMask(i);
		}
	}

	public static void _stencilOp(int i, int j, int k) {
		if (i != STENCIL.fail || j != STENCIL.zfail || k != STENCIL.zpass) {
			STENCIL.fail = i;
			STENCIL.zfail = j;
			STENCIL.zpass = k;
			GL11.glStencilOp(i, j, k);
		}
	}

	public static void _clearDepth(double d) {
		if (d != CLEAR.depth) {
			CLEAR.depth = d;
			GL11.glClearDepth(d);
		}
	}

	public static void _clearColor(float f, float g, float h, float i) {
		if (f != CLEAR.color.r || g != CLEAR.color.g || h != CLEAR.color.b || i != CLEAR.color.a) {
			CLEAR.color.r = f;
			CLEAR.color.g = g;
			CLEAR.color.b = h;
			CLEAR.color.a = i;
			GL11.glClearColor(f, g, h, i);
		}
	}

	public static void _clearStencil(int i) {
		if (i != CLEAR.stencil) {
			CLEAR.stencil = i;
			GL11.glClearStencil(i);
		}
	}

	public static void _clear(int i, boolean bl) {
		GL11.glClear(i);
		if (bl) {
			RenderSystem.getError();
		}
	}

	public static void _matrixMode(int i) {
		GL11.glMatrixMode(i);
	}

	public static void _loadIdentity() {
		GL11.glLoadIdentity();
	}

	public static void _pushMatrix() {
		GL11.glPushMatrix();
	}

	public static void _popMatrix() {
		GL11.glPopMatrix();
	}

	public static void _getMatrix(int i, FloatBuffer floatBuffer) {
		GL11.glGetFloatv(i, floatBuffer);
	}

	public static Matrix4f _getMatrix4f(int i) {
		_getMatrix(i, MATRIX_BUFFER);
		MATRIX_BUFFER.rewind();
		Matrix4f matrix4f = new Matrix4f();
		matrix4f.load(MATRIX_BUFFER);
		MATRIX_BUFFER.rewind();
		return matrix4f;
	}

	public static void _ortho(double d, double e, double f, double g, double h, double i) {
		GL11.glOrtho(d, e, f, g, h, i);
	}

	public static void _rotatef(float f, float g, float h, float i) {
		GL11.glRotatef(f, g, h, i);
	}

	public static void _rotated(double d, double e, double f, double g) {
		GL11.glRotated(d, e, f, g);
	}

	public static void _scalef(float f, float g, float h) {
		GL11.glScalef(f, g, h);
	}

	public static void _scaled(double d, double e, double f) {
		GL11.glScaled(d, e, f);
	}

	public static void _translatef(float f, float g, float h) {
		GL11.glTranslatef(f, g, h);
	}

	public static void _translated(double d, double e, double f) {
		GL11.glTranslated(d, e, f);
	}

	public static void _multMatrix(FloatBuffer floatBuffer) {
		GL11.glMultMatrixf(floatBuffer);
	}

	public static void _multMatrix(Matrix4f matrix4f) {
		matrix4f.store(MATRIX_BUFFER);
		MATRIX_BUFFER.rewind();
		_multMatrix(MATRIX_BUFFER);
	}

	public static void _color4f(float f, float g, float h, float i) {
		if (f != COLOR.r || g != COLOR.g || h != COLOR.b || i != COLOR.a) {
			COLOR.r = f;
			COLOR.g = g;
			COLOR.b = h;
			COLOR.a = i;
			GL11.glColor4f(f, g, h, i);
		}
	}

	public static void _texCoord2f(float f, float g) {
		GL11.glTexCoord2f(f, g);
	}

	public static void _vertex3f(float f, float g, float h) {
		GL11.glVertex3f(f, g, h);
	}

	public static void _clearCurrentColor() {
		COLOR.r = -1.0F;
		COLOR.g = -1.0F;
		COLOR.b = -1.0F;
		COLOR.a = -1.0F;
	}

	public static void _normalPointer(int i, int j, int k) {
		GL11.glNormalPointer(i, j, (long)k);
	}

	public static void _normalPointer(int i, int j, ByteBuffer byteBuffer) {
		GL11.glNormalPointer(i, j, byteBuffer);
	}

	public static void _texCoordPointer(int i, int j, int k, int l) {
		GL11.glTexCoordPointer(i, j, k, (long)l);
	}

	public static void _texCoordPointer(int i, int j, int k, ByteBuffer byteBuffer) {
		GL11.glTexCoordPointer(i, j, k, byteBuffer);
	}

	public static void _vertexPointer(int i, int j, int k, int l) {
		GL11.glVertexPointer(i, j, k, (long)l);
	}

	public static void _vertexPointer(int i, int j, int k, ByteBuffer byteBuffer) {
		GL11.glVertexPointer(i, j, k, byteBuffer);
	}

	public static void _colorPointer(int i, int j, int k, int l) {
		GL11.glColorPointer(i, j, k, (long)l);
	}

	public static void _colorPointer(int i, int j, int k, ByteBuffer byteBuffer) {
		GL11.glColorPointer(i, j, k, byteBuffer);
	}

	public static void _disableClientState(int i) {
		GL11.glDisableClientState(i);
	}

	public static void _enableClientState(int i) {
		GL11.glEnableClientState(i);
	}

	public static void _begin(int i) {
		GL11.glBegin(i);
	}

	public static void _end() {
		GL11.glEnd();
	}

	public static void _drawArrays(int i, int j, int k) {
		GL11.glDrawArrays(i, j, k);
	}

	public static void _lineWidth(float f) {
		GL11.glLineWidth(f);
	}

	public static void _callList(int i) {
		GL11.glCallList(i);
	}

	public static void _deleteLists(int i, int j) {
		GL11.glDeleteLists(i, j);
	}

	public static void _newList(int i, int j) {
		GL11.glNewList(i, j);
	}

	public static void _endList() {
		GL11.glEndList();
	}

	public static int _genLists(int i) {
		return GL11.glGenLists(i);
	}

	public static void _pixelStore(int i, int j) {
		GL11.glPixelStorei(i, j);
	}

	public static void _pixelTransfer(int i, float f) {
		GL11.glPixelTransferf(i, f);
	}

	public static void _readPixels(int i, int j, int k, int l, int m, int n, ByteBuffer byteBuffer) {
		GL11.glReadPixels(i, j, k, l, m, n, byteBuffer);
	}

	public static void _readPixels(int i, int j, int k, int l, int m, int n, long o) {
		GL11.glReadPixels(i, j, k, l, m, n, o);
	}

	public static int _getError() {
		return GL11.glGetError();
	}

	public static String _getString(int i) {
		return GL11.glGetString(i);
	}

	public static void _getInteger(int i, IntBuffer intBuffer) {
		GL11.glGetIntegerv(i, intBuffer);
	}

	public static int _getInteger(int i) {
		return GL11.glGetInteger(i);
	}

	public static void setProfile(GlStateManager.Profile profile) {
		profile.apply();
	}

	public static void unsetProfile(GlStateManager.Profile profile) {
		profile.clean();
	}

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

	@Environment(EnvType.CLIENT)
	static class ClearState {
		public double depth = 1.0;
		public final GlStateManager.Color color = new GlStateManager.Color(0.0F, 0.0F, 0.0F, 0.0F);
		public int stencil;

		private ClearState() {
		}
	}

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

	@Environment(EnvType.CLIENT)
	static class ColorMaterialState {
		public final GlStateManager.BooleanState enable = new GlStateManager.BooleanState(2903);
		public int face = 1032;
		public int mode = 5634;

		private ColorMaterialState() {
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum CullFace {
		FRONT(1028),
		BACK(1029),
		FRONT_AND_BACK(1032);

		public final int value;

		private CullFace(int j) {
			this.value = j;
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
	public static enum FboMode {
		BASE,
		ARB,
		EXT;
	}

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
	public static enum Profile {
		DEFAULT {
			@Override
			public void apply() {
				RenderSystem.disableAlphaTest();
				RenderSystem.alphaFunc(519, 0.0F);
				RenderSystem.disableLighting();
				RenderSystem.lightModel(2899, Lighting.getBuffer(0.2F, 0.2F, 0.2F, 1.0F));

				for (int i = 0; i < 8; i++) {
					RenderSystem.disableLight(i);
					RenderSystem.light(16384 + i, 4608, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
					RenderSystem.light(16384 + i, 4611, Lighting.getBuffer(0.0F, 0.0F, 1.0F, 0.0F));
					if (i == 0) {
						RenderSystem.light(16384 + i, 4609, Lighting.getBuffer(1.0F, 1.0F, 1.0F, 1.0F));
						RenderSystem.light(16384 + i, 4610, Lighting.getBuffer(1.0F, 1.0F, 1.0F, 1.0F));
					} else {
						RenderSystem.light(16384 + i, 4609, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
						RenderSystem.light(16384 + i, 4610, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
					}
				}

				RenderSystem.disableColorMaterial();
				RenderSystem.colorMaterial(1032, 5634);
				RenderSystem.disableDepthTest();
				RenderSystem.depthFunc(513);
				RenderSystem.depthMask(true);
				RenderSystem.disableBlend();
				RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				RenderSystem.blendFuncSeparate(
					GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
				);
				RenderSystem.blendEquation(32774);
				RenderSystem.disableFog();
				RenderSystem.fogi(2917, 2048);
				RenderSystem.fogDensity(1.0F);
				RenderSystem.fogStart(0.0F);
				RenderSystem.fogEnd(1.0F);
				RenderSystem.fog(2918, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 0.0F));
				if (GL.getCapabilities().GL_NV_fog_distance) {
					RenderSystem.fogi(2917, 34140);
				}

				RenderSystem.polygonOffset(0.0F, 0.0F);
				RenderSystem.disableColorLogicOp();
				RenderSystem.logicOp(5379);
				RenderSystem.disableTexGen(GlStateManager.TexGen.S);
				RenderSystem.texGenMode(GlStateManager.TexGen.S, 9216);
				RenderSystem.texGenParam(GlStateManager.TexGen.S, 9474, Lighting.getBuffer(1.0F, 0.0F, 0.0F, 0.0F));
				RenderSystem.texGenParam(GlStateManager.TexGen.S, 9217, Lighting.getBuffer(1.0F, 0.0F, 0.0F, 0.0F));
				RenderSystem.disableTexGen(GlStateManager.TexGen.T);
				RenderSystem.texGenMode(GlStateManager.TexGen.T, 9216);
				RenderSystem.texGenParam(GlStateManager.TexGen.T, 9474, Lighting.getBuffer(0.0F, 1.0F, 0.0F, 0.0F));
				RenderSystem.texGenParam(GlStateManager.TexGen.T, 9217, Lighting.getBuffer(0.0F, 1.0F, 0.0F, 0.0F));
				RenderSystem.disableTexGen(GlStateManager.TexGen.R);
				RenderSystem.texGenMode(GlStateManager.TexGen.R, 9216);
				RenderSystem.texGenParam(GlStateManager.TexGen.R, 9474, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 0.0F));
				RenderSystem.texGenParam(GlStateManager.TexGen.R, 9217, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 0.0F));
				RenderSystem.disableTexGen(GlStateManager.TexGen.Q);
				RenderSystem.texGenMode(GlStateManager.TexGen.Q, 9216);
				RenderSystem.texGenParam(GlStateManager.TexGen.Q, 9474, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 0.0F));
				RenderSystem.texGenParam(GlStateManager.TexGen.Q, 9217, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 0.0F));
				RenderSystem.activeTexture(0);
				RenderSystem.texParameter(3553, 10240, 9729);
				RenderSystem.texParameter(3553, 10241, 9986);
				RenderSystem.texParameter(3553, 10242, 10497);
				RenderSystem.texParameter(3553, 10243, 10497);
				RenderSystem.texParameter(3553, 33085, 1000);
				RenderSystem.texParameter(3553, 33083, 1000);
				RenderSystem.texParameter(3553, 33082, -1000);
				RenderSystem.texParameter(3553, 34049, 0.0F);
				RenderSystem.texEnv(8960, 8704, 8448);
				RenderSystem.texEnv(8960, 8705, Lighting.getBuffer(0.0F, 0.0F, 0.0F, 0.0F));
				RenderSystem.texEnv(8960, 34161, 8448);
				RenderSystem.texEnv(8960, 34162, 8448);
				RenderSystem.texEnv(8960, 34176, 5890);
				RenderSystem.texEnv(8960, 34177, 34168);
				RenderSystem.texEnv(8960, 34178, 34166);
				RenderSystem.texEnv(8960, 34184, 5890);
				RenderSystem.texEnv(8960, 34185, 34168);
				RenderSystem.texEnv(8960, 34186, 34166);
				RenderSystem.texEnv(8960, 34192, 768);
				RenderSystem.texEnv(8960, 34193, 768);
				RenderSystem.texEnv(8960, 34194, 770);
				RenderSystem.texEnv(8960, 34200, 770);
				RenderSystem.texEnv(8960, 34201, 770);
				RenderSystem.texEnv(8960, 34202, 770);
				RenderSystem.texEnv(8960, 34163, 1.0F);
				RenderSystem.texEnv(8960, 3356, 1.0F);
				RenderSystem.disableNormalize();
				RenderSystem.shadeModel(7425);
				RenderSystem.disableRescaleNormal();
				RenderSystem.colorMask(true, true, true, true);
				RenderSystem.clearDepth(1.0);
				RenderSystem.lineWidth(1.0F);
				RenderSystem.normal3f(0.0F, 0.0F, 1.0F);
				RenderSystem.polygonMode(1028, 6914);
				RenderSystem.polygonMode(1029, 6914);
			}

			@Override
			public void clean() {
			}
		},
		PLAYER_SKIN {
			@Override
			public void apply() {
				RenderSystem.enableBlend();
				RenderSystem.blendFuncSeparate(770, 771, 1, 0);
			}

			@Override
			public void clean() {
				RenderSystem.disableBlend();
			}
		},
		TRANSPARENT_MODEL {
			@Override
			public void apply() {
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 0.15F);
				RenderSystem.depthMask(false);
				RenderSystem.enableBlend();
				RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				RenderSystem.alphaFunc(516, 0.003921569F);
			}

			@Override
			public void clean() {
				RenderSystem.disableBlend();
				RenderSystem.alphaFunc(516, 0.1F);
				RenderSystem.depthMask(true);
			}
		};

		private Profile() {
		}

		public abstract void apply();

		public abstract void clean();
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

	@Environment(EnvType.CLIENT)
	public static enum TexGen {
		S,
		T,
		R,
		Q;
	}

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
