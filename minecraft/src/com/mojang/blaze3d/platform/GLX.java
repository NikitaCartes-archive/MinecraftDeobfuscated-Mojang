package com.mojang.blaze3d.platform;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.ARBMultitexture;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.EXTBlendFuncSeparate;
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
import oshi.SystemInfo;
import oshi.hardware.Processor;

@Environment(EnvType.CLIENT)
public class GLX {
	private static final Logger LOGGER = LogManager.getLogger();
	public static boolean isNvidia;
	public static boolean isAmd;
	public static int GL_FRAMEBUFFER;
	public static int GL_RENDERBUFFER;
	public static int GL_COLOR_ATTACHMENT0;
	public static int GL_DEPTH_ATTACHMENT;
	public static int GL_FRAMEBUFFER_COMPLETE;
	public static int GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT;
	public static int GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT;
	public static int GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER;
	public static int GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER;
	private static GLX.FboMode fboMode;
	public static final boolean useFbo = true;
	private static boolean hasShaders;
	private static boolean useShaderArb;
	public static int GL_LINK_STATUS;
	public static int GL_COMPILE_STATUS;
	public static int GL_VERTEX_SHADER;
	public static int GL_FRAGMENT_SHADER;
	private static boolean useMultitextureArb;
	public static int GL_TEXTURE0;
	public static int GL_TEXTURE1;
	public static int GL_TEXTURE2;
	private static boolean useTexEnvCombineArb;
	public static int GL_COMBINE;
	public static int GL_INTERPOLATE;
	public static int GL_PRIMARY_COLOR;
	public static int GL_CONSTANT;
	public static int GL_PREVIOUS;
	public static int GL_COMBINE_RGB;
	public static int GL_SOURCE0_RGB;
	public static int GL_SOURCE1_RGB;
	public static int GL_SOURCE2_RGB;
	public static int GL_OPERAND0_RGB;
	public static int GL_OPERAND1_RGB;
	public static int GL_OPERAND2_RGB;
	public static int GL_COMBINE_ALPHA;
	public static int GL_SOURCE0_ALPHA;
	public static int GL_SOURCE1_ALPHA;
	public static int GL_SOURCE2_ALPHA;
	public static int GL_OPERAND0_ALPHA;
	public static int GL_OPERAND1_ALPHA;
	public static int GL_OPERAND2_ALPHA;
	private static boolean separateBlend;
	public static boolean useSeparateBlendExt;
	public static boolean isOpenGl21;
	public static boolean usePostProcess;
	private static String capsString = "";
	private static String cpuInfo;
	public static final boolean useVbo = true;
	public static boolean needVbo;
	private static boolean useVboArb;
	public static int GL_ARRAY_BUFFER;
	public static int GL_STATIC_DRAW;
	private static final Map<Integer, String> LOOKUP_MAP = make(Maps.<Integer, String>newHashMap(), hashMap -> {
		hashMap.put(0, "No error");
		hashMap.put(1280, "Enum parameter is invalid for this function");
		hashMap.put(1281, "Parameter is invalid for this function");
		hashMap.put(1282, "Current state is invalid for this function");
		hashMap.put(1283, "Stack overflow");
		hashMap.put(1284, "Stack underflow");
		hashMap.put(1285, "Out of memory");
		hashMap.put(1286, "Operation on incomplete framebuffer");
		hashMap.put(1286, "Operation on incomplete framebuffer");
	});

	public static void populateSnooperWithOpenGL(SnooperAccess snooperAccess) {
		snooperAccess.setFixedData("opengl_version", GlStateManager.getString(7938));
		snooperAccess.setFixedData("opengl_vendor", GlStateManager.getString(7936));
		GLCapabilities gLCapabilities = GL.getCapabilities();
		snooperAccess.setFixedData("gl_caps[ARB_arrays_of_arrays]", gLCapabilities.GL_ARB_arrays_of_arrays);
		snooperAccess.setFixedData("gl_caps[ARB_base_instance]", gLCapabilities.GL_ARB_base_instance);
		snooperAccess.setFixedData("gl_caps[ARB_blend_func_extended]", gLCapabilities.GL_ARB_blend_func_extended);
		snooperAccess.setFixedData("gl_caps[ARB_clear_buffer_object]", gLCapabilities.GL_ARB_clear_buffer_object);
		snooperAccess.setFixedData("gl_caps[ARB_color_buffer_float]", gLCapabilities.GL_ARB_color_buffer_float);
		snooperAccess.setFixedData("gl_caps[ARB_compatibility]", gLCapabilities.GL_ARB_compatibility);
		snooperAccess.setFixedData("gl_caps[ARB_compressed_texture_pixel_storage]", gLCapabilities.GL_ARB_compressed_texture_pixel_storage);
		snooperAccess.setFixedData("gl_caps[ARB_compute_shader]", gLCapabilities.GL_ARB_compute_shader);
		snooperAccess.setFixedData("gl_caps[ARB_copy_buffer]", gLCapabilities.GL_ARB_copy_buffer);
		snooperAccess.setFixedData("gl_caps[ARB_copy_image]", gLCapabilities.GL_ARB_copy_image);
		snooperAccess.setFixedData("gl_caps[ARB_depth_buffer_float]", gLCapabilities.GL_ARB_depth_buffer_float);
		snooperAccess.setFixedData("gl_caps[ARB_compute_shader]", gLCapabilities.GL_ARB_compute_shader);
		snooperAccess.setFixedData("gl_caps[ARB_copy_buffer]", gLCapabilities.GL_ARB_copy_buffer);
		snooperAccess.setFixedData("gl_caps[ARB_copy_image]", gLCapabilities.GL_ARB_copy_image);
		snooperAccess.setFixedData("gl_caps[ARB_depth_buffer_float]", gLCapabilities.GL_ARB_depth_buffer_float);
		snooperAccess.setFixedData("gl_caps[ARB_depth_clamp]", gLCapabilities.GL_ARB_depth_clamp);
		snooperAccess.setFixedData("gl_caps[ARB_depth_texture]", gLCapabilities.GL_ARB_depth_texture);
		snooperAccess.setFixedData("gl_caps[ARB_draw_buffers]", gLCapabilities.GL_ARB_draw_buffers);
		snooperAccess.setFixedData("gl_caps[ARB_draw_buffers_blend]", gLCapabilities.GL_ARB_draw_buffers_blend);
		snooperAccess.setFixedData("gl_caps[ARB_draw_elements_base_vertex]", gLCapabilities.GL_ARB_draw_elements_base_vertex);
		snooperAccess.setFixedData("gl_caps[ARB_draw_indirect]", gLCapabilities.GL_ARB_draw_indirect);
		snooperAccess.setFixedData("gl_caps[ARB_draw_instanced]", gLCapabilities.GL_ARB_draw_instanced);
		snooperAccess.setFixedData("gl_caps[ARB_explicit_attrib_location]", gLCapabilities.GL_ARB_explicit_attrib_location);
		snooperAccess.setFixedData("gl_caps[ARB_explicit_uniform_location]", gLCapabilities.GL_ARB_explicit_uniform_location);
		snooperAccess.setFixedData("gl_caps[ARB_fragment_layer_viewport]", gLCapabilities.GL_ARB_fragment_layer_viewport);
		snooperAccess.setFixedData("gl_caps[ARB_fragment_program]", gLCapabilities.GL_ARB_fragment_program);
		snooperAccess.setFixedData("gl_caps[ARB_fragment_shader]", gLCapabilities.GL_ARB_fragment_shader);
		snooperAccess.setFixedData("gl_caps[ARB_fragment_program_shadow]", gLCapabilities.GL_ARB_fragment_program_shadow);
		snooperAccess.setFixedData("gl_caps[ARB_framebuffer_object]", gLCapabilities.GL_ARB_framebuffer_object);
		snooperAccess.setFixedData("gl_caps[ARB_framebuffer_sRGB]", gLCapabilities.GL_ARB_framebuffer_sRGB);
		snooperAccess.setFixedData("gl_caps[ARB_geometry_shader4]", gLCapabilities.GL_ARB_geometry_shader4);
		snooperAccess.setFixedData("gl_caps[ARB_gpu_shader5]", gLCapabilities.GL_ARB_gpu_shader5);
		snooperAccess.setFixedData("gl_caps[ARB_half_float_pixel]", gLCapabilities.GL_ARB_half_float_pixel);
		snooperAccess.setFixedData("gl_caps[ARB_half_float_vertex]", gLCapabilities.GL_ARB_half_float_vertex);
		snooperAccess.setFixedData("gl_caps[ARB_instanced_arrays]", gLCapabilities.GL_ARB_instanced_arrays);
		snooperAccess.setFixedData("gl_caps[ARB_map_buffer_alignment]", gLCapabilities.GL_ARB_map_buffer_alignment);
		snooperAccess.setFixedData("gl_caps[ARB_map_buffer_range]", gLCapabilities.GL_ARB_map_buffer_range);
		snooperAccess.setFixedData("gl_caps[ARB_multisample]", gLCapabilities.GL_ARB_multisample);
		snooperAccess.setFixedData("gl_caps[ARB_multitexture]", gLCapabilities.GL_ARB_multitexture);
		snooperAccess.setFixedData("gl_caps[ARB_occlusion_query2]", gLCapabilities.GL_ARB_occlusion_query2);
		snooperAccess.setFixedData("gl_caps[ARB_pixel_buffer_object]", gLCapabilities.GL_ARB_pixel_buffer_object);
		snooperAccess.setFixedData("gl_caps[ARB_seamless_cube_map]", gLCapabilities.GL_ARB_seamless_cube_map);
		snooperAccess.setFixedData("gl_caps[ARB_shader_objects]", gLCapabilities.GL_ARB_shader_objects);
		snooperAccess.setFixedData("gl_caps[ARB_shader_stencil_export]", gLCapabilities.GL_ARB_shader_stencil_export);
		snooperAccess.setFixedData("gl_caps[ARB_shader_texture_lod]", gLCapabilities.GL_ARB_shader_texture_lod);
		snooperAccess.setFixedData("gl_caps[ARB_shadow]", gLCapabilities.GL_ARB_shadow);
		snooperAccess.setFixedData("gl_caps[ARB_shadow_ambient]", gLCapabilities.GL_ARB_shadow_ambient);
		snooperAccess.setFixedData("gl_caps[ARB_stencil_texturing]", gLCapabilities.GL_ARB_stencil_texturing);
		snooperAccess.setFixedData("gl_caps[ARB_sync]", gLCapabilities.GL_ARB_sync);
		snooperAccess.setFixedData("gl_caps[ARB_tessellation_shader]", gLCapabilities.GL_ARB_tessellation_shader);
		snooperAccess.setFixedData("gl_caps[ARB_texture_border_clamp]", gLCapabilities.GL_ARB_texture_border_clamp);
		snooperAccess.setFixedData("gl_caps[ARB_texture_buffer_object]", gLCapabilities.GL_ARB_texture_buffer_object);
		snooperAccess.setFixedData("gl_caps[ARB_texture_cube_map]", gLCapabilities.GL_ARB_texture_cube_map);
		snooperAccess.setFixedData("gl_caps[ARB_texture_cube_map_array]", gLCapabilities.GL_ARB_texture_cube_map_array);
		snooperAccess.setFixedData("gl_caps[ARB_texture_non_power_of_two]", gLCapabilities.GL_ARB_texture_non_power_of_two);
		snooperAccess.setFixedData("gl_caps[ARB_uniform_buffer_object]", gLCapabilities.GL_ARB_uniform_buffer_object);
		snooperAccess.setFixedData("gl_caps[ARB_vertex_blend]", gLCapabilities.GL_ARB_vertex_blend);
		snooperAccess.setFixedData("gl_caps[ARB_vertex_buffer_object]", gLCapabilities.GL_ARB_vertex_buffer_object);
		snooperAccess.setFixedData("gl_caps[ARB_vertex_program]", gLCapabilities.GL_ARB_vertex_program);
		snooperAccess.setFixedData("gl_caps[ARB_vertex_shader]", gLCapabilities.GL_ARB_vertex_shader);
		snooperAccess.setFixedData("gl_caps[EXT_bindable_uniform]", gLCapabilities.GL_EXT_bindable_uniform);
		snooperAccess.setFixedData("gl_caps[EXT_blend_equation_separate]", gLCapabilities.GL_EXT_blend_equation_separate);
		snooperAccess.setFixedData("gl_caps[EXT_blend_func_separate]", gLCapabilities.GL_EXT_blend_func_separate);
		snooperAccess.setFixedData("gl_caps[EXT_blend_minmax]", gLCapabilities.GL_EXT_blend_minmax);
		snooperAccess.setFixedData("gl_caps[EXT_blend_subtract]", gLCapabilities.GL_EXT_blend_subtract);
		snooperAccess.setFixedData("gl_caps[EXT_draw_instanced]", gLCapabilities.GL_EXT_draw_instanced);
		snooperAccess.setFixedData("gl_caps[EXT_framebuffer_multisample]", gLCapabilities.GL_EXT_framebuffer_multisample);
		snooperAccess.setFixedData("gl_caps[EXT_framebuffer_object]", gLCapabilities.GL_EXT_framebuffer_object);
		snooperAccess.setFixedData("gl_caps[EXT_framebuffer_sRGB]", gLCapabilities.GL_EXT_framebuffer_sRGB);
		snooperAccess.setFixedData("gl_caps[EXT_geometry_shader4]", gLCapabilities.GL_EXT_geometry_shader4);
		snooperAccess.setFixedData("gl_caps[EXT_gpu_program_parameters]", gLCapabilities.GL_EXT_gpu_program_parameters);
		snooperAccess.setFixedData("gl_caps[EXT_gpu_shader4]", gLCapabilities.GL_EXT_gpu_shader4);
		snooperAccess.setFixedData("gl_caps[EXT_packed_depth_stencil]", gLCapabilities.GL_EXT_packed_depth_stencil);
		snooperAccess.setFixedData("gl_caps[EXT_separate_shader_objects]", gLCapabilities.GL_EXT_separate_shader_objects);
		snooperAccess.setFixedData("gl_caps[EXT_shader_image_load_store]", gLCapabilities.GL_EXT_shader_image_load_store);
		snooperAccess.setFixedData("gl_caps[EXT_shadow_funcs]", gLCapabilities.GL_EXT_shadow_funcs);
		snooperAccess.setFixedData("gl_caps[EXT_shared_texture_palette]", gLCapabilities.GL_EXT_shared_texture_palette);
		snooperAccess.setFixedData("gl_caps[EXT_stencil_clear_tag]", gLCapabilities.GL_EXT_stencil_clear_tag);
		snooperAccess.setFixedData("gl_caps[EXT_stencil_two_side]", gLCapabilities.GL_EXT_stencil_two_side);
		snooperAccess.setFixedData("gl_caps[EXT_stencil_wrap]", gLCapabilities.GL_EXT_stencil_wrap);
		snooperAccess.setFixedData("gl_caps[EXT_texture_array]", gLCapabilities.GL_EXT_texture_array);
		snooperAccess.setFixedData("gl_caps[EXT_texture_buffer_object]", gLCapabilities.GL_EXT_texture_buffer_object);
		snooperAccess.setFixedData("gl_caps[EXT_texture_integer]", gLCapabilities.GL_EXT_texture_integer);
		snooperAccess.setFixedData("gl_caps[EXT_texture_sRGB]", gLCapabilities.GL_EXT_texture_sRGB);
		snooperAccess.setFixedData("gl_caps[ARB_vertex_shader]", gLCapabilities.GL_ARB_vertex_shader);
		snooperAccess.setFixedData("gl_caps[gl_max_vertex_uniforms]", GlStateManager.getInteger(35658));
		GlStateManager.getError();
		snooperAccess.setFixedData("gl_caps[gl_max_fragment_uniforms]", GlStateManager.getInteger(35657));
		GlStateManager.getError();
		snooperAccess.setFixedData("gl_caps[gl_max_vertex_attribs]", GlStateManager.getInteger(34921));
		GlStateManager.getError();
		snooperAccess.setFixedData("gl_caps[gl_max_vertex_texture_image_units]", GlStateManager.getInteger(35660));
		GlStateManager.getError();
		snooperAccess.setFixedData("gl_caps[gl_max_texture_image_units]", GlStateManager.getInteger(34930));
		GlStateManager.getError();
		snooperAccess.setFixedData("gl_caps[gl_max_array_texture_layers]", GlStateManager.getInteger(35071));
		GlStateManager.getError();
	}

	public static String getOpenGLVersionString() {
		return GLFW.glfwGetCurrentContext() == 0L
			? "NO CONTEXT"
			: GlStateManager.getString(7937) + " GL version " + GlStateManager.getString(7938) + ", " + GlStateManager.getString(7936);
	}

	public static int getRefreshRate(Window window) {
		long l = GLFW.glfwGetWindowMonitor(window.getWindow());
		if (l == 0L) {
			l = GLFW.glfwGetPrimaryMonitor();
		}

		GLFWVidMode gLFWVidMode = l == 0L ? null : GLFW.glfwGetVideoMode(l);
		return gLFWVidMode == null ? 0 : gLFWVidMode.refreshRate();
	}

	public static String getLWJGLVersion() {
		return Version.getVersion();
	}

	public static LongSupplier initGlfw() {
		Window.checkGlfwError((integer, stringx) -> {
			throw new IllegalStateException(String.format("GLFW error before init: [0x%X]%s", integer, stringx));
		});
		List<String> list = Lists.<String>newArrayList();
		GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback((i, l) -> list.add(String.format("GLFW error during init: [0x%X]%s", i, l)));
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Failed to initialize GLFW, errors: " + Joiner.on(",").join(list));
		} else {
			LongSupplier longSupplier = () -> (long)(GLFW.glfwGetTime() * 1.0E9);

			for (String string : list) {
				LOGGER.error("GLFW error collected during initialization: {}", string);
			}

			setGlfwErrorCallback(gLFWErrorCallback);
			return longSupplier;
		}
	}

	public static void setGlfwErrorCallback(GLFWErrorCallbackI gLFWErrorCallbackI) {
		GLFW.glfwSetErrorCallback(gLFWErrorCallbackI).free();
	}

	public static boolean shouldClose(Window window) {
		return GLFW.glfwWindowShouldClose(window.getWindow());
	}

	public static void pollEvents() {
		GLFW.glfwPollEvents();
	}

	public static String getOpenGLVersion() {
		return GlStateManager.getString(7938);
	}

	public static String getRenderer() {
		return GlStateManager.getString(7937);
	}

	public static String getVendor() {
		return GlStateManager.getString(7936);
	}

	public static void setupNvFogDistance() {
		if (GL.getCapabilities().GL_NV_fog_distance) {
			GlStateManager.fogi(34138, 34139);
		}
	}

	public static boolean supportsOpenGL2() {
		return GL.getCapabilities().OpenGL20;
	}

	public static void withTextureRestore(Runnable runnable) {
		GL11.glPushAttrib(270336);

		try {
			runnable.run();
		} finally {
			GL11.glPopAttrib();
		}
	}

	public static ByteBuffer allocateMemory(int i) {
		return MemoryUtil.memAlloc(i);
	}

	public static void freeMemory(Buffer buffer) {
		MemoryUtil.memFree(buffer);
	}

	public static void init() {
		GLCapabilities gLCapabilities = GL.getCapabilities();
		useMultitextureArb = gLCapabilities.GL_ARB_multitexture && !gLCapabilities.OpenGL13;
		useTexEnvCombineArb = gLCapabilities.GL_ARB_texture_env_combine && !gLCapabilities.OpenGL13;
		if (useMultitextureArb) {
			capsString = capsString + "Using ARB_multitexture.\n";
			GL_TEXTURE0 = 33984;
			GL_TEXTURE1 = 33985;
			GL_TEXTURE2 = 33986;
		} else {
			capsString = capsString + "Using GL 1.3 multitexturing.\n";
			GL_TEXTURE0 = 33984;
			GL_TEXTURE1 = 33985;
			GL_TEXTURE2 = 33986;
		}

		if (useTexEnvCombineArb) {
			capsString = capsString + "Using ARB_texture_env_combine.\n";
			GL_COMBINE = 34160;
			GL_INTERPOLATE = 34165;
			GL_PRIMARY_COLOR = 34167;
			GL_CONSTANT = 34166;
			GL_PREVIOUS = 34168;
			GL_COMBINE_RGB = 34161;
			GL_SOURCE0_RGB = 34176;
			GL_SOURCE1_RGB = 34177;
			GL_SOURCE2_RGB = 34178;
			GL_OPERAND0_RGB = 34192;
			GL_OPERAND1_RGB = 34193;
			GL_OPERAND2_RGB = 34194;
			GL_COMBINE_ALPHA = 34162;
			GL_SOURCE0_ALPHA = 34184;
			GL_SOURCE1_ALPHA = 34185;
			GL_SOURCE2_ALPHA = 34186;
			GL_OPERAND0_ALPHA = 34200;
			GL_OPERAND1_ALPHA = 34201;
			GL_OPERAND2_ALPHA = 34202;
		} else {
			capsString = capsString + "Using GL 1.3 texture combiners.\n";
			GL_COMBINE = 34160;
			GL_INTERPOLATE = 34165;
			GL_PRIMARY_COLOR = 34167;
			GL_CONSTANT = 34166;
			GL_PREVIOUS = 34168;
			GL_COMBINE_RGB = 34161;
			GL_SOURCE0_RGB = 34176;
			GL_SOURCE1_RGB = 34177;
			GL_SOURCE2_RGB = 34178;
			GL_OPERAND0_RGB = 34192;
			GL_OPERAND1_RGB = 34193;
			GL_OPERAND2_RGB = 34194;
			GL_COMBINE_ALPHA = 34162;
			GL_SOURCE0_ALPHA = 34184;
			GL_SOURCE1_ALPHA = 34185;
			GL_SOURCE2_ALPHA = 34186;
			GL_OPERAND0_ALPHA = 34200;
			GL_OPERAND1_ALPHA = 34201;
			GL_OPERAND2_ALPHA = 34202;
		}

		useSeparateBlendExt = gLCapabilities.GL_EXT_blend_func_separate && !gLCapabilities.OpenGL14;
		separateBlend = gLCapabilities.OpenGL14 || gLCapabilities.GL_EXT_blend_func_separate;
		capsString = capsString + "Using framebuffer objects because ";
		if (gLCapabilities.OpenGL30) {
			capsString = capsString + "OpenGL 3.0 is supported and separate blending is supported.\n";
			fboMode = GLX.FboMode.BASE;
			GL_FRAMEBUFFER = 36160;
			GL_RENDERBUFFER = 36161;
			GL_COLOR_ATTACHMENT0 = 36064;
			GL_DEPTH_ATTACHMENT = 36096;
			GL_FRAMEBUFFER_COMPLETE = 36053;
			GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 36054;
			GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 36055;
			GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = 36059;
			GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = 36060;
		} else if (gLCapabilities.GL_ARB_framebuffer_object) {
			capsString = capsString + "ARB_framebuffer_object is supported and separate blending is supported.\n";
			fboMode = GLX.FboMode.ARB;
			GL_FRAMEBUFFER = 36160;
			GL_RENDERBUFFER = 36161;
			GL_COLOR_ATTACHMENT0 = 36064;
			GL_DEPTH_ATTACHMENT = 36096;
			GL_FRAMEBUFFER_COMPLETE = 36053;
			GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 36055;
			GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 36054;
			GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = 36059;
			GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = 36060;
		} else {
			if (!gLCapabilities.GL_EXT_framebuffer_object) {
				throw new IllegalStateException("The driver does not appear to support framebuffer objects");
			}

			capsString = capsString + "EXT_framebuffer_object is supported.\n";
			fboMode = GLX.FboMode.EXT;
			GL_FRAMEBUFFER = 36160;
			GL_RENDERBUFFER = 36161;
			GL_COLOR_ATTACHMENT0 = 36064;
			GL_DEPTH_ATTACHMENT = 36096;
			GL_FRAMEBUFFER_COMPLETE = 36053;
			GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 36055;
			GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 36054;
			GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = 36059;
			GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = 36060;
		}

		isOpenGl21 = gLCapabilities.OpenGL21;
		hasShaders = isOpenGl21 || gLCapabilities.GL_ARB_vertex_shader && gLCapabilities.GL_ARB_fragment_shader && gLCapabilities.GL_ARB_shader_objects;
		capsString = capsString + "Shaders are " + (hasShaders ? "" : "not ") + "available because ";
		if (hasShaders) {
			if (gLCapabilities.OpenGL21) {
				capsString = capsString + "OpenGL 2.1 is supported.\n";
				useShaderArb = false;
				GL_LINK_STATUS = 35714;
				GL_COMPILE_STATUS = 35713;
				GL_VERTEX_SHADER = 35633;
				GL_FRAGMENT_SHADER = 35632;
			} else {
				capsString = capsString + "ARB_shader_objects, ARB_vertex_shader, and ARB_fragment_shader are supported.\n";
				useShaderArb = true;
				GL_LINK_STATUS = 35714;
				GL_COMPILE_STATUS = 35713;
				GL_VERTEX_SHADER = 35633;
				GL_FRAGMENT_SHADER = 35632;
			}
		} else {
			capsString = capsString + "OpenGL 2.1 is " + (gLCapabilities.OpenGL21 ? "" : "not ") + "supported, ";
			capsString = capsString + "ARB_shader_objects is " + (gLCapabilities.GL_ARB_shader_objects ? "" : "not ") + "supported, ";
			capsString = capsString + "ARB_vertex_shader is " + (gLCapabilities.GL_ARB_vertex_shader ? "" : "not ") + "supported, and ";
			capsString = capsString + "ARB_fragment_shader is " + (gLCapabilities.GL_ARB_fragment_shader ? "" : "not ") + "supported.\n";
		}

		usePostProcess = hasShaders;
		String string = GL11.glGetString(7936).toLowerCase(Locale.ROOT);
		isNvidia = string.contains("nvidia");
		useVboArb = !gLCapabilities.OpenGL15 && gLCapabilities.GL_ARB_vertex_buffer_object;
		capsString = capsString + "VBOs are available because ";
		if (useVboArb) {
			capsString = capsString + "ARB_vertex_buffer_object is supported.\n";
			GL_STATIC_DRAW = 35044;
			GL_ARRAY_BUFFER = 34962;
		} else {
			capsString = capsString + "OpenGL 1.5 is supported.\n";
			GL_STATIC_DRAW = 35044;
			GL_ARRAY_BUFFER = 34962;
		}

		isAmd = string.contains("ati");
		if (isAmd) {
			needVbo = true;
		}

		try {
			Processor[] processors = new SystemInfo().getHardware().getProcessors();
			cpuInfo = String.format("%dx %s", processors.length, processors[0]).replaceAll("\\s+", " ");
		} catch (Throwable var3) {
		}
	}

	public static boolean isNextGen() {
		return usePostProcess;
	}

	public static String getCapsString() {
		return capsString;
	}

	public static int glGetProgrami(int i, int j) {
		return useShaderArb ? ARBShaderObjects.glGetObjectParameteriARB(i, j) : GL20.glGetProgrami(i, j);
	}

	public static void glAttachShader(int i, int j) {
		if (useShaderArb) {
			ARBShaderObjects.glAttachObjectARB(i, j);
		} else {
			GL20.glAttachShader(i, j);
		}
	}

	public static void glDeleteShader(int i) {
		if (useShaderArb) {
			ARBShaderObjects.glDeleteObjectARB(i);
		} else {
			GL20.glDeleteShader(i);
		}
	}

	public static int glCreateShader(int i) {
		return useShaderArb ? ARBShaderObjects.glCreateShaderObjectARB(i) : GL20.glCreateShader(i);
	}

	public static void glShaderSource(int i, CharSequence charSequence) {
		if (useShaderArb) {
			ARBShaderObjects.glShaderSourceARB(i, charSequence);
		} else {
			GL20.glShaderSource(i, charSequence);
		}
	}

	public static void glCompileShader(int i) {
		if (useShaderArb) {
			ARBShaderObjects.glCompileShaderARB(i);
		} else {
			GL20.glCompileShader(i);
		}
	}

	public static int glGetShaderi(int i, int j) {
		return useShaderArb ? ARBShaderObjects.glGetObjectParameteriARB(i, j) : GL20.glGetShaderi(i, j);
	}

	public static String glGetShaderInfoLog(int i, int j) {
		return useShaderArb ? ARBShaderObjects.glGetInfoLogARB(i, j) : GL20.glGetShaderInfoLog(i, j);
	}

	public static String glGetProgramInfoLog(int i, int j) {
		return useShaderArb ? ARBShaderObjects.glGetInfoLogARB(i, j) : GL20.glGetProgramInfoLog(i, j);
	}

	public static void glUseProgram(int i) {
		if (useShaderArb) {
			ARBShaderObjects.glUseProgramObjectARB(i);
		} else {
			GL20.glUseProgram(i);
		}
	}

	public static int glCreateProgram() {
		return useShaderArb ? ARBShaderObjects.glCreateProgramObjectARB() : GL20.glCreateProgram();
	}

	public static void glDeleteProgram(int i) {
		if (useShaderArb) {
			ARBShaderObjects.glDeleteObjectARB(i);
		} else {
			GL20.glDeleteProgram(i);
		}
	}

	public static void glLinkProgram(int i) {
		if (useShaderArb) {
			ARBShaderObjects.glLinkProgramARB(i);
		} else {
			GL20.glLinkProgram(i);
		}
	}

	public static int glGetUniformLocation(int i, CharSequence charSequence) {
		return useShaderArb ? ARBShaderObjects.glGetUniformLocationARB(i, charSequence) : GL20.glGetUniformLocation(i, charSequence);
	}

	public static void glUniform1(int i, IntBuffer intBuffer) {
		if (useShaderArb) {
			ARBShaderObjects.glUniform1ivARB(i, intBuffer);
		} else {
			GL20.glUniform1iv(i, intBuffer);
		}
	}

	public static void glUniform1i(int i, int j) {
		if (useShaderArb) {
			ARBShaderObjects.glUniform1iARB(i, j);
		} else {
			GL20.glUniform1i(i, j);
		}
	}

	public static void glUniform1(int i, FloatBuffer floatBuffer) {
		if (useShaderArb) {
			ARBShaderObjects.glUniform1fvARB(i, floatBuffer);
		} else {
			GL20.glUniform1fv(i, floatBuffer);
		}
	}

	public static void glUniform2(int i, IntBuffer intBuffer) {
		if (useShaderArb) {
			ARBShaderObjects.glUniform2ivARB(i, intBuffer);
		} else {
			GL20.glUniform2iv(i, intBuffer);
		}
	}

	public static void glUniform2(int i, FloatBuffer floatBuffer) {
		if (useShaderArb) {
			ARBShaderObjects.glUniform2fvARB(i, floatBuffer);
		} else {
			GL20.glUniform2fv(i, floatBuffer);
		}
	}

	public static void glUniform3(int i, IntBuffer intBuffer) {
		if (useShaderArb) {
			ARBShaderObjects.glUniform3ivARB(i, intBuffer);
		} else {
			GL20.glUniform3iv(i, intBuffer);
		}
	}

	public static void glUniform3(int i, FloatBuffer floatBuffer) {
		if (useShaderArb) {
			ARBShaderObjects.glUniform3fvARB(i, floatBuffer);
		} else {
			GL20.glUniform3fv(i, floatBuffer);
		}
	}

	public static void glUniform4(int i, IntBuffer intBuffer) {
		if (useShaderArb) {
			ARBShaderObjects.glUniform4ivARB(i, intBuffer);
		} else {
			GL20.glUniform4iv(i, intBuffer);
		}
	}

	public static void glUniform4(int i, FloatBuffer floatBuffer) {
		if (useShaderArb) {
			ARBShaderObjects.glUniform4fvARB(i, floatBuffer);
		} else {
			GL20.glUniform4fv(i, floatBuffer);
		}
	}

	public static void glUniformMatrix2(int i, boolean bl, FloatBuffer floatBuffer) {
		if (useShaderArb) {
			ARBShaderObjects.glUniformMatrix2fvARB(i, bl, floatBuffer);
		} else {
			GL20.glUniformMatrix2fv(i, bl, floatBuffer);
		}
	}

	public static void glUniformMatrix3(int i, boolean bl, FloatBuffer floatBuffer) {
		if (useShaderArb) {
			ARBShaderObjects.glUniformMatrix3fvARB(i, bl, floatBuffer);
		} else {
			GL20.glUniformMatrix3fv(i, bl, floatBuffer);
		}
	}

	public static void glUniformMatrix4(int i, boolean bl, FloatBuffer floatBuffer) {
		if (useShaderArb) {
			ARBShaderObjects.glUniformMatrix4fvARB(i, bl, floatBuffer);
		} else {
			GL20.glUniformMatrix4fv(i, bl, floatBuffer);
		}
	}

	public static int glGetAttribLocation(int i, CharSequence charSequence) {
		return useShaderArb ? ARBVertexShader.glGetAttribLocationARB(i, charSequence) : GL20.glGetAttribLocation(i, charSequence);
	}

	public static int glGenBuffers() {
		return useVboArb ? ARBVertexBufferObject.glGenBuffersARB() : GL15.glGenBuffers();
	}

	public static void glGenBuffers(IntBuffer intBuffer) {
		if (useVboArb) {
			ARBVertexBufferObject.glGenBuffersARB(intBuffer);
		} else {
			GL15.glGenBuffers(intBuffer);
		}
	}

	public static void glBindBuffer(int i, int j) {
		if (useVboArb) {
			ARBVertexBufferObject.glBindBufferARB(i, j);
		} else {
			GL15.glBindBuffer(i, j);
		}
	}

	public static void glBufferData(int i, ByteBuffer byteBuffer, int j) {
		if (useVboArb) {
			ARBVertexBufferObject.glBufferDataARB(i, byteBuffer, j);
		} else {
			GL15.glBufferData(i, byteBuffer, j);
		}
	}

	public static void glDeleteBuffers(int i) {
		if (useVboArb) {
			ARBVertexBufferObject.glDeleteBuffersARB(i);
		} else {
			GL15.glDeleteBuffers(i);
		}
	}

	public static void glDeleteBuffers(IntBuffer intBuffer) {
		if (useVboArb) {
			ARBVertexBufferObject.glDeleteBuffersARB(intBuffer);
		} else {
			GL15.glDeleteBuffers(intBuffer);
		}
	}

	public static boolean useVbo() {
		return true;
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

	public static int getBoundFramebuffer() {
		switch (fboMode) {
			case BASE:
				return GlStateManager.getInteger(36006);
			case ARB:
				return GlStateManager.getInteger(36006);
			case EXT:
				return GlStateManager.getInteger(36006);
			default:
				return 0;
		}
	}

	public static void glActiveTexture(int i) {
		if (useMultitextureArb) {
			ARBMultitexture.glActiveTextureARB(i);
		} else {
			GL13.glActiveTexture(i);
		}
	}

	public static void glClientActiveTexture(int i) {
		if (useMultitextureArb) {
			ARBMultitexture.glClientActiveTextureARB(i);
		} else {
			GL13.glClientActiveTexture(i);
		}
	}

	public static void glMultiTexCoord2f(int i, float f, float g) {
		if (useMultitextureArb) {
			ARBMultitexture.glMultiTexCoord2fARB(i, f, g);
		} else {
			GL13.glMultiTexCoord2f(i, f, g);
		}
	}

	public static void glBlendFuncSeparate(int i, int j, int k, int l) {
		if (separateBlend) {
			if (useSeparateBlendExt) {
				EXTBlendFuncSeparate.glBlendFuncSeparateEXT(i, j, k, l);
			} else {
				GL14.glBlendFuncSeparate(i, j, k, l);
			}
		} else {
			GL11.glBlendFunc(i, j);
		}
	}

	public static boolean isUsingFBOs() {
		return true;
	}

	public static String getCpuInfo() {
		return cpuInfo == null ? "<unknown>" : cpuInfo;
	}

	public static void renderCrosshair(int i) {
		renderCrosshair(i, true, true, true);
	}

	public static void renderCrosshair(int i, boolean bl, boolean bl2, boolean bl3) {
		GlStateManager.disableTexture();
		GlStateManager.depthMask(false);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		GL11.glLineWidth(4.0F);
		bufferBuilder.begin(1, DefaultVertexFormat.POSITION_COLOR);
		if (bl) {
			bufferBuilder.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex((double)i, 0.0, 0.0).color(0, 0, 0, 255).endVertex();
		}

		if (bl2) {
			bufferBuilder.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex(0.0, (double)i, 0.0).color(0, 0, 0, 255).endVertex();
		}

		if (bl3) {
			bufferBuilder.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex(0.0, 0.0, (double)i).color(0, 0, 0, 255).endVertex();
		}

		tesselator.end();
		GL11.glLineWidth(2.0F);
		bufferBuilder.begin(1, DefaultVertexFormat.POSITION_COLOR);
		if (bl) {
			bufferBuilder.vertex(0.0, 0.0, 0.0).color(255, 0, 0, 255).endVertex();
			bufferBuilder.vertex((double)i, 0.0, 0.0).color(255, 0, 0, 255).endVertex();
		}

		if (bl2) {
			bufferBuilder.vertex(0.0, 0.0, 0.0).color(0, 255, 0, 255).endVertex();
			bufferBuilder.vertex(0.0, (double)i, 0.0).color(0, 255, 0, 255).endVertex();
		}

		if (bl3) {
			bufferBuilder.vertex(0.0, 0.0, 0.0).color(127, 127, 255, 255).endVertex();
			bufferBuilder.vertex(0.0, 0.0, (double)i).color(127, 127, 255, 255).endVertex();
		}

		tesselator.end();
		GL11.glLineWidth(1.0F);
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture();
	}

	public static String getErrorString(int i) {
		return (String)LOOKUP_MAP.get(i);
	}

	public static <T> T make(Supplier<T> supplier) {
		return (T)supplier.get();
	}

	public static <T> T make(T object, Consumer<T> consumer) {
		consumer.accept(object);
		return object;
	}

	@Environment(EnvType.CLIENT)
	static enum FboMode {
		BASE,
		ARB,
		EXT;
	}
}
