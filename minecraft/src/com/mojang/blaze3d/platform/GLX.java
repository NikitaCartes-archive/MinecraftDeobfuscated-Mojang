package com.mojang.blaze3d.platform;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
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
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import oshi.SystemInfo;
import oshi.hardware.Processor;

@Environment(EnvType.CLIENT)
public class GLX {
	private static final Logger LOGGER = LogManager.getLogger();
	private static String capsString = "";
	private static String cpuInfo;
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

	public static String getOpenGLVersionString() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		return GLFW.glfwGetCurrentContext() == 0L
			? "NO CONTEXT"
			: GlStateManager._getString(7937) + " GL version " + GlStateManager._getString(7938) + ", " + GlStateManager._getString(7936);
	}

	public static int _getRefreshRate(Window window) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		long l = GLFW.glfwGetWindowMonitor(window.getWindow());
		if (l == 0L) {
			l = GLFW.glfwGetPrimaryMonitor();
		}

		GLFWVidMode gLFWVidMode = l == 0L ? null : GLFW.glfwGetVideoMode(l);
		return gLFWVidMode == null ? 0 : gLFWVidMode.refreshRate();
	}

	public static String _getLWJGLVersion() {
		RenderSystem.assertThread(RenderSystem::isInInitPhase);
		return Version.getVersion();
	}

	public static LongSupplier _initGlfw() {
		RenderSystem.assertThread(RenderSystem::isInInitPhase);
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

			RenderSystem.setErrorCallback(gLFWErrorCallback);
			return longSupplier;
		}
	}

	public static void _setGlfwErrorCallback(GLFWErrorCallbackI gLFWErrorCallbackI) {
		RenderSystem.assertThread(RenderSystem::isInInitPhase);
		GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback(gLFWErrorCallbackI);
		if (gLFWErrorCallback != null) {
			gLFWErrorCallback.free();
		}
	}

	public static boolean _shouldClose(Window window) {
		return GLFW.glfwWindowShouldClose(window.getWindow());
	}

	public static void _setupNvFogDistance() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		if (GL.getCapabilities().GL_NV_fog_distance) {
			GlStateManager._fogi(34138, 34139);
		}
	}

	public static void _init(int i, boolean bl) {
		RenderSystem.assertThread(RenderSystem::isInInitPhase);
		GLCapabilities gLCapabilities = GL.getCapabilities();
		capsString = "Using framebuffer using " + GlStateManager._init_fbo(gLCapabilities);

		try {
			Processor[] processors = new SystemInfo().getHardware().getProcessors();
			cpuInfo = String.format("%dx %s", processors.length, processors[0]).replaceAll("\\s+", " ");
		} catch (Throwable var4) {
		}

		GlDebug.enableDebugCallback(i, bl);
	}

	public static String _getCapsString() {
		return capsString;
	}

	public static String _getCpuInfo() {
		return cpuInfo == null ? "<unknown>" : cpuInfo;
	}

	public static void _renderCrosshair(int i, boolean bl, boolean bl2, boolean bl3) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GlStateManager._disableTexture();
		GlStateManager._depthMask(false);
		Tesselator tesselator = RenderSystem.renderThreadTesselator();
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
		GlStateManager._depthMask(true);
		GlStateManager._enableTexture();
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
}
