/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.platform;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlDebug;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.GameRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import oshi.SystemInfo;
import oshi.hardware.Processor;

@Environment(value=EnvType.CLIENT)
public class GLX {
    private static final Logger LOGGER = LogManager.getLogger();
    private static String cpuInfo;

    public static String getOpenGLVersionString() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        if (GLFW.glfwGetCurrentContext() == 0L) {
            return "NO CONTEXT";
        }
        return GlStateManager._getString(7937) + " GL version " + GlStateManager._getString(7938) + ", " + GlStateManager._getString(7936);
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
        LongSupplier longSupplier;
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
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
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback(gLFWErrorCallbackI);
        if (gLFWErrorCallback != null) {
            gLFWErrorCallback.free();
        }
    }

    public static boolean _shouldClose(Window window) {
        return GLFW.glfwWindowShouldClose(window.getWindow());
    }

    public static void _init(int i, boolean bl) {
        RenderSystem.assertThread(RenderSystem::isInInitPhase);
        try {
            Processor[] processors = new SystemInfo().getHardware().getProcessors();
            cpuInfo = String.format("%dx %s", processors.length, processors[0]).replaceAll("\\s+", " ");
        } catch (Throwable throwable) {
            // empty catch block
        }
        GlDebug.enableDebugCallback(i, bl);
    }

    public static String _getCpuInfo() {
        return cpuInfo == null ? "<unknown>" : cpuInfo;
    }

    public static void _renderCrosshair(int i, boolean bl, boolean bl2, boolean bl3) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        GlStateManager._disableTexture();
        GlStateManager._depthMask(false);
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        Tesselator tesselator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.lineWidth(4.0f);
        bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        if (bl) {
            bufferBuilder.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).normal(1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(i, 0.0, 0.0).color(0, 0, 0, 255).normal(1.0f, 0.0f, 0.0f).endVertex();
        }
        if (bl2) {
            bufferBuilder.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).normal(0.0f, 1.0f, 0.0f).endVertex();
            bufferBuilder.vertex(0.0, i, 0.0).color(0, 0, 0, 255).normal(0.0f, 1.0f, 0.0f).endVertex();
        }
        if (bl3) {
            bufferBuilder.vertex(0.0, 0.0, 0.0).color(0, 0, 0, 255).normal(0.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(0.0, 0.0, i).color(0, 0, 0, 255).normal(0.0f, 0.0f, 1.0f).endVertex();
        }
        tesselator.end();
        RenderSystem.lineWidth(2.0f);
        bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        if (bl) {
            bufferBuilder.vertex(0.0, 0.0, 0.0).color(255, 0, 0, 255).normal(1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(i, 0.0, 0.0).color(255, 0, 0, 255).normal(1.0f, 0.0f, 0.0f).endVertex();
        }
        if (bl2) {
            bufferBuilder.vertex(0.0, 0.0, 0.0).color(0, 255, 0, 255).normal(0.0f, 1.0f, 0.0f).endVertex();
            bufferBuilder.vertex(0.0, i, 0.0).color(0, 255, 0, 255).normal(0.0f, 1.0f, 0.0f).endVertex();
        }
        if (bl3) {
            bufferBuilder.vertex(0.0, 0.0, 0.0).color(127, 127, 255, 255).normal(0.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(0.0, 0.0, i).color(127, 127, 255, 255).normal(0.0f, 0.0f, 1.0f).endVertex();
        }
        tesselator.end();
        RenderSystem.lineWidth(1.0f);
        GlStateManager._depthMask(true);
        GlStateManager._enableTexture();
    }

    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public static <T> T make(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }
}

