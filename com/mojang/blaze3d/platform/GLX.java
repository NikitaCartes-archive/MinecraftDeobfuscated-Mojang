/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.platform;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlDebug;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.ArrayList;
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

@Environment(value=EnvType.CLIENT)
public class GLX {
    private static final Logger LOGGER = LogManager.getLogger();
    private static String capsString = "";
    private static String cpuInfo;
    private static final Map<Integer, String> LOOKUP_MAP;

    public static String getOpenGLVersionString() {
        if (GLFW.glfwGetCurrentContext() == 0L) {
            return "NO CONTEXT";
        }
        return RenderSystem.getString(7937) + " GL version " + RenderSystem.getString(7938) + ", " + RenderSystem.getString(7936);
    }

    public static int _getRefreshRate(Window window) {
        long l = GLFW.glfwGetWindowMonitor(window.getWindow());
        if (l == 0L) {
            l = GLFW.glfwGetPrimaryMonitor();
        }
        GLFWVidMode gLFWVidMode = l == 0L ? null : GLFW.glfwGetVideoMode(l);
        return gLFWVidMode == null ? 0 : gLFWVidMode.refreshRate();
    }

    public static String _getLWJGLVersion() {
        return Version.getVersion();
    }

    public static LongSupplier _initGlfw() {
        LongSupplier longSupplier;
        Window.checkGlfwError((integer, string) -> {
            throw new IllegalStateException(String.format("GLFW error before init: [0x%X]%s", integer, string));
        });
        ArrayList<String> list = Lists.newArrayList();
        GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback((i, l) -> list.add(String.format("GLFW error during init: [0x%X]%s", i, l)));
        if (GLFW.glfwInit()) {
            longSupplier = () -> (long)(GLFW.glfwGetTime() * 1.0E9);
            for (String string2 : list) {
                LOGGER.error("GLFW error collected during initialization: {}", (Object)string2);
            }
        } else {
            throw new IllegalStateException("Failed to initialize GLFW, errors: " + Joiner.on(",").join(list));
        }
        RenderSystem.setErrorCallback(gLFWErrorCallback);
        return longSupplier;
    }

    public static void _setGlfwErrorCallback(GLFWErrorCallbackI gLFWErrorCallbackI) {
        GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback(gLFWErrorCallbackI);
        if (gLFWErrorCallback != null) {
            gLFWErrorCallback.free();
        }
    }

    public static boolean _shouldClose(Window window) {
        return GLFW.glfwWindowShouldClose(window.getWindow());
    }

    public static void _pollEvents() {
        GLFW.glfwPollEvents();
    }

    public static void _setupNvFogDistance() {
        if (GL.getCapabilities().GL_NV_fog_distance) {
            RenderSystem.fogi(34138, 34139);
        }
    }

    public static void _init(int i, boolean bl) {
        GLCapabilities gLCapabilities = GL.getCapabilities();
        capsString = "Using framebuffer using " + GlStateManager._init_fbo(gLCapabilities);
        try {
            Processor[] processors = new SystemInfo().getHardware().getProcessors();
            cpuInfo = String.format("%dx %s", processors.length, processors[0]).replaceAll("\\s+", " ");
        } catch (Throwable throwable) {
            // empty catch block
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
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        GL11.glLineWidth(4.0f);
        bufferBuilder.begin(1, DefaultVertexFormat.POSITION_COLOR);
        if (bl) {
            bufferBuilder.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(i, 0.0, 0.0).color(0, 0, 0, 255).endVertex();
        }
        if (bl2) {
            bufferBuilder.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(0.0, i, 0.0).color(0, 0, 0, 255).endVertex();
        }
        if (bl3) {
            bufferBuilder.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(0.0, 0.0, i).color(0, 0, 0, 255).endVertex();
        }
        tesselator.end();
        GL11.glLineWidth(2.0f);
        bufferBuilder.begin(1, DefaultVertexFormat.POSITION_COLOR);
        if (bl) {
            bufferBuilder.vertex(0.0, 0.0, 0.0).color(255, 0, 0, 255).endVertex();
            bufferBuilder.vertex(i, 0.0, 0.0).color(255, 0, 0, 255).endVertex();
        }
        if (bl2) {
            bufferBuilder.vertex(0.0, 0.0, 0.0).color(0, 255, 0, 255).endVertex();
            bufferBuilder.vertex(0.0, i, 0.0).color(0, 255, 0, 255).endVertex();
        }
        if (bl3) {
            bufferBuilder.vertex(0.0, 0.0, 0.0).color(127, 127, 255, 255).endVertex();
            bufferBuilder.vertex(0.0, 0.0, i).color(127, 127, 255, 255).endVertex();
        }
        tesselator.end();
        GL11.glLineWidth(1.0f);
        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
    }

    public static String getErrorString(int i) {
        return LOOKUP_MAP.get(i);
    }

    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public static <T> T make(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }

    static {
        LOOKUP_MAP = GLX.make(Maps.newHashMap(), hashMap -> {
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
    }
}

