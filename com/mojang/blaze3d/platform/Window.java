/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.MacosUtil;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.SilentInitException;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public final class Window
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final GLFWErrorCallback defaultErrorCallback = GLFWErrorCallback.create(this::defaultErrorCallback);
    private final WindowEventHandler eventHandler;
    private final ScreenManager screenManager;
    private final long window;
    private int windowedX;
    private int windowedY;
    private int windowedWidth;
    private int windowedHeight;
    private Optional<VideoMode> preferredFullscreenVideoMode;
    private boolean fullscreen;
    private boolean actuallyFullscreen;
    private int x;
    private int y;
    private int width;
    private int height;
    private int framebufferWidth;
    private int framebufferHeight;
    private int guiScaledWidth;
    private int guiScaledHeight;
    private double guiScale;
    private String errorSection = "";
    private boolean dirty;
    private int framerateLimit;
    private boolean vsync;

    public Window(WindowEventHandler windowEventHandler, ScreenManager screenManager, DisplayData displayData, @Nullable String string, String string2) {
        RenderSystem.assertInInitPhase();
        this.screenManager = screenManager;
        this.setBootErrorCallback();
        this.setErrorSection("Pre startup");
        this.eventHandler = windowEventHandler;
        Optional<VideoMode> optional = VideoMode.read(string);
        this.preferredFullscreenVideoMode = optional.isPresent() ? optional : (displayData.fullscreenWidth.isPresent() && displayData.fullscreenHeight.isPresent() ? Optional.of(new VideoMode(displayData.fullscreenWidth.getAsInt(), displayData.fullscreenHeight.getAsInt(), 8, 8, 8, 60)) : Optional.empty());
        this.actuallyFullscreen = this.fullscreen = displayData.isFullscreen;
        Monitor monitor = screenManager.getMonitor(GLFW.glfwGetPrimaryMonitor());
        this.width = displayData.width > 0 ? displayData.width : 1;
        this.windowedWidth = this.width;
        this.height = displayData.height > 0 ? displayData.height : 1;
        this.windowedHeight = this.height;
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(139265, 196609);
        GLFW.glfwWindowHint(139275, 221185);
        GLFW.glfwWindowHint(139266, 3);
        GLFW.glfwWindowHint(139267, 2);
        GLFW.glfwWindowHint(139272, 204801);
        GLFW.glfwWindowHint(139270, 1);
        this.window = GLFW.glfwCreateWindow(this.width, this.height, string2, this.fullscreen && monitor != null ? monitor.getMonitor() : 0L, 0L);
        if (monitor != null) {
            VideoMode videoMode = monitor.getPreferredVidMode(this.fullscreen ? this.preferredFullscreenVideoMode : Optional.empty());
            this.windowedX = this.x = monitor.getX() + videoMode.getWidth() / 2 - this.width / 2;
            this.windowedY = this.y = monitor.getY() + videoMode.getHeight() / 2 - this.height / 2;
        } else {
            int[] is = new int[1];
            int[] js = new int[1];
            GLFW.glfwGetWindowPos(this.window, is, js);
            this.windowedX = this.x = is[0];
            this.windowedY = this.y = js[0];
        }
        GLFW.glfwMakeContextCurrent(this.window);
        Locale locale = Locale.getDefault(Locale.Category.FORMAT);
        Locale.setDefault(Locale.Category.FORMAT, Locale.ROOT);
        GL.createCapabilities();
        Locale.setDefault(Locale.Category.FORMAT, locale);
        this.setMode();
        this.refreshFramebufferSize();
        GLFW.glfwSetFramebufferSizeCallback(this.window, this::onFramebufferResize);
        GLFW.glfwSetWindowPosCallback(this.window, this::onMove);
        GLFW.glfwSetWindowSizeCallback(this.window, this::onResize);
        GLFW.glfwSetWindowFocusCallback(this.window, this::onFocus);
        GLFW.glfwSetCursorEnterCallback(this.window, this::onEnter);
    }

    public int getRefreshRate() {
        RenderSystem.assertOnRenderThread();
        return GLX._getRefreshRate(this);
    }

    public boolean shouldClose() {
        return GLX._shouldClose(this);
    }

    public static void checkGlfwError(BiConsumer<Integer, String> biConsumer) {
        RenderSystem.assertInInitPhase();
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
            int i = GLFW.glfwGetError(pointerBuffer);
            if (i != 0) {
                long l = pointerBuffer.get();
                String string = l == 0L ? "" : MemoryUtil.memUTF8(l);
                biConsumer.accept(i, string);
            }
        }
    }

    public void setIcon(IoSupplier<InputStream> ioSupplier, IoSupplier<InputStream> ioSupplier2) {
        RenderSystem.assertInInitPhase();
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            IntBuffer intBuffer = memoryStack.mallocInt(1);
            IntBuffer intBuffer2 = memoryStack.mallocInt(1);
            IntBuffer intBuffer3 = memoryStack.mallocInt(1);
            GLFWImage.Buffer buffer = GLFWImage.malloc(2, memoryStack);
            ByteBuffer byteBuffer = this.readIconPixels(ioSupplier, intBuffer, intBuffer2, intBuffer3);
            if (byteBuffer == null) {
                throw new IllegalStateException("Could not load icon: " + STBImage.stbi_failure_reason());
            }
            buffer.position(0);
            buffer.width(intBuffer.get(0));
            buffer.height(intBuffer2.get(0));
            buffer.pixels(byteBuffer);
            ByteBuffer byteBuffer2 = this.readIconPixels(ioSupplier2, intBuffer, intBuffer2, intBuffer3);
            if (byteBuffer2 == null) {
                STBImage.stbi_image_free(byteBuffer);
                throw new IllegalStateException("Could not load icon: " + STBImage.stbi_failure_reason());
            }
            buffer.position(1);
            buffer.width(intBuffer.get(0));
            buffer.height(intBuffer2.get(0));
            buffer.pixels(byteBuffer2);
            buffer.position(0);
            GLFW.glfwSetWindowIcon(this.window, buffer);
            STBImage.stbi_image_free(byteBuffer);
            STBImage.stbi_image_free(byteBuffer2);
        } catch (IOException iOException) {
            LOGGER.error("Couldn't set icon", iOException);
        }
    }

    /*
     * Loose catch block
     */
    @Nullable
    private ByteBuffer readIconPixels(IoSupplier<InputStream> ioSupplier, IntBuffer intBuffer, IntBuffer intBuffer2, IntBuffer intBuffer3) throws IOException {
        ByteBuffer byteBuffer;
        InputStream inputStream;
        ByteBuffer byteBuffer2;
        block10: {
            block9: {
                RenderSystem.assertInInitPhase();
                byteBuffer2 = null;
                inputStream = ioSupplier.get();
                byteBuffer2 = TextureUtil.readResource(inputStream);
                byteBuffer2.rewind();
                byteBuffer = STBImage.stbi_load_from_memory(byteBuffer2, intBuffer, intBuffer2, intBuffer3, 0);
                if (inputStream == null) break block9;
                inputStream.close();
            }
            if (byteBuffer2 == null) break block10;
            MemoryUtil.memFree(byteBuffer2);
        }
        return byteBuffer;
        {
            catch (Throwable throwable) {
                try {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (Throwable throwable3) {
                    if (byteBuffer2 != null) {
                        MemoryUtil.memFree(byteBuffer2);
                    }
                    throw throwable3;
                }
            }
        }
    }

    public void setErrorSection(String string) {
        this.errorSection = string;
    }

    private void setBootErrorCallback() {
        RenderSystem.assertInInitPhase();
        GLFW.glfwSetErrorCallback(Window::bootCrash);
    }

    private static void bootCrash(int i, long l) {
        RenderSystem.assertInInitPhase();
        String string = "GLFW error " + i + ": " + MemoryUtil.memUTF8(l);
        TinyFileDialogs.tinyfd_messageBox("Minecraft", string + ".\n\nPlease make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).", "ok", "error", false);
        throw new WindowInitFailed(string);
    }

    public void defaultErrorCallback(int i, long l) {
        RenderSystem.assertOnRenderThread();
        String string = MemoryUtil.memUTF8(l);
        LOGGER.error("########## GL ERROR ##########");
        LOGGER.error("@ {}", (Object)this.errorSection);
        LOGGER.error("{}: {}", (Object)i, (Object)string);
    }

    public void setDefaultErrorCallback() {
        GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback(this.defaultErrorCallback);
        if (gLFWErrorCallback != null) {
            gLFWErrorCallback.free();
        }
    }

    public void updateVsync(boolean bl) {
        RenderSystem.assertOnRenderThreadOrInit();
        this.vsync = bl;
        GLFW.glfwSwapInterval(bl ? 1 : 0);
    }

    @Override
    public void close() {
        RenderSystem.assertOnRenderThread();
        Callbacks.glfwFreeCallbacks(this.window);
        this.defaultErrorCallback.close();
        GLFW.glfwDestroyWindow(this.window);
        GLFW.glfwTerminate();
    }

    private void onMove(long l, int i, int j) {
        this.x = i;
        this.y = j;
    }

    private void onFramebufferResize(long l, int i, int j) {
        if (l != this.window) {
            return;
        }
        int k = this.getWidth();
        int m = this.getHeight();
        if (i == 0 || j == 0) {
            return;
        }
        this.framebufferWidth = i;
        this.framebufferHeight = j;
        if (this.getWidth() != k || this.getHeight() != m) {
            this.eventHandler.resizeDisplay();
        }
    }

    private void refreshFramebufferSize() {
        RenderSystem.assertInInitPhase();
        int[] is = new int[1];
        int[] js = new int[1];
        GLFW.glfwGetFramebufferSize(this.window, is, js);
        this.framebufferWidth = is[0] > 0 ? is[0] : 1;
        this.framebufferHeight = js[0] > 0 ? js[0] : 1;
    }

    private void onResize(long l, int i, int j) {
        this.width = i;
        this.height = j;
    }

    private void onFocus(long l, boolean bl) {
        if (l == this.window) {
            this.eventHandler.setWindowActive(bl);
        }
    }

    private void onEnter(long l, boolean bl) {
        if (bl) {
            this.eventHandler.cursorEntered();
        }
    }

    public void setFramerateLimit(int i) {
        this.framerateLimit = i;
    }

    public int getFramerateLimit() {
        return this.framerateLimit;
    }

    public void updateDisplay() {
        RenderSystem.flipFrame(this.window);
        if (this.fullscreen != this.actuallyFullscreen) {
            this.actuallyFullscreen = this.fullscreen;
            this.updateFullscreen(this.vsync);
        }
    }

    public Optional<VideoMode> getPreferredFullscreenVideoMode() {
        return this.preferredFullscreenVideoMode;
    }

    public void setPreferredFullscreenVideoMode(Optional<VideoMode> optional) {
        boolean bl = !optional.equals(this.preferredFullscreenVideoMode);
        this.preferredFullscreenVideoMode = optional;
        if (bl) {
            this.dirty = true;
        }
    }

    public void changeFullscreenVideoMode() {
        if (this.fullscreen && this.dirty) {
            this.dirty = false;
            this.setMode();
            this.eventHandler.resizeDisplay();
        }
    }

    private void setMode() {
        boolean bl;
        RenderSystem.assertInInitPhase();
        boolean bl2 = bl = GLFW.glfwGetWindowMonitor(this.window) != 0L;
        if (this.fullscreen) {
            Monitor monitor = this.screenManager.findBestMonitor(this);
            if (monitor == null) {
                LOGGER.warn("Failed to find suitable monitor for fullscreen mode");
                this.fullscreen = false;
            } else {
                if (Minecraft.ON_OSX) {
                    MacosUtil.toggleFullscreen(this.window);
                }
                VideoMode videoMode = monitor.getPreferredVidMode(this.preferredFullscreenVideoMode);
                if (!bl) {
                    this.windowedX = this.x;
                    this.windowedY = this.y;
                    this.windowedWidth = this.width;
                    this.windowedHeight = this.height;
                }
                this.x = 0;
                this.y = 0;
                this.width = videoMode.getWidth();
                this.height = videoMode.getHeight();
                GLFW.glfwSetWindowMonitor(this.window, monitor.getMonitor(), this.x, this.y, this.width, this.height, videoMode.getRefreshRate());
            }
        } else {
            this.x = this.windowedX;
            this.y = this.windowedY;
            this.width = this.windowedWidth;
            this.height = this.windowedHeight;
            GLFW.glfwSetWindowMonitor(this.window, 0L, this.x, this.y, this.width, this.height, -1);
        }
    }

    public void toggleFullScreen() {
        this.fullscreen = !this.fullscreen;
    }

    public void setWindowed(int i, int j) {
        this.windowedWidth = i;
        this.windowedHeight = j;
        this.fullscreen = false;
        this.setMode();
    }

    private void updateFullscreen(boolean bl) {
        RenderSystem.assertOnRenderThread();
        try {
            this.setMode();
            this.eventHandler.resizeDisplay();
            this.updateVsync(bl);
            this.updateDisplay();
        } catch (Exception exception) {
            LOGGER.error("Couldn't toggle fullscreen", exception);
        }
    }

    public int calculateScale(int i, boolean bl) {
        int j;
        for (j = 1; j != i && j < this.framebufferWidth && j < this.framebufferHeight && this.framebufferWidth / (j + 1) >= 320 && this.framebufferHeight / (j + 1) >= 240; ++j) {
        }
        if (bl && j % 2 != 0) {
            ++j;
        }
        return j;
    }

    public void setGuiScale(double d) {
        this.guiScale = d;
        int i = (int)((double)this.framebufferWidth / d);
        this.guiScaledWidth = (double)this.framebufferWidth / d > (double)i ? i + 1 : i;
        int j = (int)((double)this.framebufferHeight / d);
        this.guiScaledHeight = (double)this.framebufferHeight / d > (double)j ? j + 1 : j;
    }

    public void setTitle(String string) {
        GLFW.glfwSetWindowTitle(this.window, string);
    }

    public long getWindow() {
        return this.window;
    }

    public boolean isFullscreen() {
        return this.fullscreen;
    }

    public int getWidth() {
        return this.framebufferWidth;
    }

    public int getHeight() {
        return this.framebufferHeight;
    }

    public void setWidth(int i) {
        this.framebufferWidth = i;
    }

    public void setHeight(int i) {
        this.framebufferHeight = i;
    }

    public int getScreenWidth() {
        return this.width;
    }

    public int getScreenHeight() {
        return this.height;
    }

    public int getGuiScaledWidth() {
        return this.guiScaledWidth;
    }

    public int getGuiScaledHeight() {
        return this.guiScaledHeight;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public double getGuiScale() {
        return this.guiScale;
    }

    @Nullable
    public Monitor findBestMonitor() {
        return this.screenManager.findBestMonitor(this);
    }

    public void updateRawMouseInput(boolean bl) {
        InputConstants.updateRawMouseInput(this.window, bl);
    }

    @Environment(value=EnvType.CLIENT)
    public static class WindowInitFailed
    extends SilentInitException {
        WindowInitFailed(String string) {
            super(string);
        }
    }
}

