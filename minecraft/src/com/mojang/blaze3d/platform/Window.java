package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Optional;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWImage.Buffer;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public final class Window implements AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	private final GLFWErrorCallback defaultErrorCallback = GLFWErrorCallback.create(this::defaultErrorCallback);
	private final WindowEventHandler minecraft;
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
	private double lastDrawTime = Double.MIN_VALUE;
	private int framerateLimit;
	private boolean vsync;

	public Window(WindowEventHandler windowEventHandler, ScreenManager screenManager, DisplayData displayData, String string, String string2) {
		this.screenManager = screenManager;
		this.setBootErrorCallback();
		this.setErrorSection("Pre startup");
		this.minecraft = windowEventHandler;
		Optional<VideoMode> optional = VideoMode.read(string);
		if (optional.isPresent()) {
			this.preferredFullscreenVideoMode = optional;
		} else if (displayData.fullscreenWidth.isPresent() && displayData.fullscreenHeight.isPresent()) {
			this.preferredFullscreenVideoMode = Optional.of(new VideoMode(displayData.fullscreenWidth.getAsInt(), displayData.fullscreenHeight.getAsInt(), 8, 8, 8, 60));
		} else {
			this.preferredFullscreenVideoMode = Optional.empty();
		}

		this.actuallyFullscreen = this.fullscreen = displayData.isFullscreen;
		Monitor monitor = screenManager.getMonitor(GLFW.glfwGetPrimaryMonitor());
		this.windowedWidth = this.width = displayData.width > 0 ? displayData.width : 1;
		this.windowedHeight = this.height = displayData.height > 0 ? displayData.height : 1;
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(139265, 196609);
		GLFW.glfwWindowHint(139275, 221185);
		GLFW.glfwWindowHint(139266, 2);
		GLFW.glfwWindowHint(139267, 0);
		GLFW.glfwWindowHint(139272, 0);
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
		GL.createCapabilities();
		this.setMode();
		this.refreshFramebufferSize();
		GLFW.glfwSetFramebufferSizeCallback(this.window, this::onFramebufferResize);
		GLFW.glfwSetWindowPosCallback(this.window, this::onMove);
		GLFW.glfwSetWindowSizeCallback(this.window, this::onResize);
		GLFW.glfwSetWindowFocusCallback(this.window, this::onFocus);
	}

	public int getRefreshRate() {
		return GLX._getRefreshRate(this);
	}

	public boolean shouldClose() {
		return GLX._shouldClose(this);
	}

	public static void checkGlfwError(BiConsumer<Integer, String> biConsumer) {
		try (MemoryStack memoryStack = MemoryStack.stackPush()) {
			PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
			int i = GLFW.glfwGetError(pointerBuffer);
			if (i != 0) {
				long l = pointerBuffer.get();
				String string = l == 0L ? "" : MemoryUtil.memUTF8(l);
				biConsumer.accept(i, string);
			}
		}
	}

	public void setupGuiState(boolean bl) {
		RenderSystem.clear(256, bl);
		RenderSystem.matrixMode(5889);
		RenderSystem.loadIdentity();
		RenderSystem.ortho(0.0, (double)this.getWidth() / this.getGuiScale(), (double)this.getHeight() / this.getGuiScale(), 0.0, 1000.0, 3000.0);
		RenderSystem.matrixMode(5888);
		RenderSystem.loadIdentity();
		RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
	}

	public void setIcon(InputStream inputStream, InputStream inputStream2) {
		try (MemoryStack memoryStack = MemoryStack.stackPush()) {
			if (inputStream == null) {
				throw new FileNotFoundException("icons/icon_16x16.png");
			}

			if (inputStream2 == null) {
				throw new FileNotFoundException("icons/icon_32x32.png");
			}

			IntBuffer intBuffer = memoryStack.mallocInt(1);
			IntBuffer intBuffer2 = memoryStack.mallocInt(1);
			IntBuffer intBuffer3 = memoryStack.mallocInt(1);
			Buffer buffer = GLFWImage.mallocStack(2, memoryStack);
			ByteBuffer byteBuffer = this.readIconPixels(inputStream, intBuffer, intBuffer2, intBuffer3);
			if (byteBuffer == null) {
				throw new IllegalStateException("Could not load icon: " + STBImage.stbi_failure_reason());
			}

			buffer.position(0);
			buffer.width(intBuffer.get(0));
			buffer.height(intBuffer2.get(0));
			buffer.pixels(byteBuffer);
			ByteBuffer byteBuffer2 = this.readIconPixels(inputStream2, intBuffer, intBuffer2, intBuffer3);
			if (byteBuffer2 == null) {
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
		} catch (IOException var21) {
			LOGGER.error("Couldn't set icon", (Throwable)var21);
		}
	}

	@Nullable
	private ByteBuffer readIconPixels(InputStream inputStream, IntBuffer intBuffer, IntBuffer intBuffer2, IntBuffer intBuffer3) throws IOException {
		ByteBuffer byteBuffer = null;

		ByteBuffer var6;
		try {
			byteBuffer = TextureUtil.readResource(inputStream);
			byteBuffer.rewind();
			var6 = STBImage.stbi_load_from_memory(byteBuffer, intBuffer, intBuffer2, intBuffer3, 0);
		} finally {
			if (byteBuffer != null) {
				MemoryUtil.memFree(byteBuffer);
			}
		}

		return var6;
	}

	public void setErrorSection(String string) {
		this.errorSection = string;
	}

	private void setBootErrorCallback() {
		GLFW.glfwSetErrorCallback(Window::bootCrash);
	}

	private static void bootCrash(int i, long l) {
		throw new IllegalStateException("GLFW error " + i + ": " + MemoryUtil.memUTF8(l));
	}

	public void defaultErrorCallback(int i, long l) {
		String string = MemoryUtil.memUTF8(l);
		LOGGER.error("########## GL ERROR ##########");
		LOGGER.error("@ {}", this.errorSection);
		LOGGER.error("{}: {}", i, string);
	}

	public void setDefaultErrorCallback() {
		GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback(this.defaultErrorCallback);
		if (gLFWErrorCallback != null) {
			gLFWErrorCallback.free();
		}
	}

	public void updateVsync(boolean bl) {
		this.vsync = bl;
		GLFW.glfwSwapInterval(bl ? 1 : 0);
	}

	public void close() {
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
		if (l == this.window) {
			int k = this.getWidth();
			int m = this.getHeight();
			if (i != 0 && j != 0) {
				this.framebufferWidth = i;
				this.framebufferHeight = j;
				if (this.getWidth() != k || this.getHeight() != m) {
					this.minecraft.resizeDisplay();
				}
			}
		}
	}

	private void refreshFramebufferSize() {
		int[] is = new int[1];
		int[] js = new int[1];
		GLFW.glfwGetFramebufferSize(this.window, is, js);
		this.framebufferWidth = is[0];
		this.framebufferHeight = js[0];
	}

	private void onResize(long l, int i, int j) {
		this.width = i;
		this.height = j;
	}

	private void onFocus(long l, boolean bl) {
		if (l == this.window) {
			this.minecraft.setWindowActive(bl);
		}
	}

	public void setFramerateLimit(int i) {
		this.framerateLimit = i;
	}

	public int getFramerateLimit() {
		return this.framerateLimit;
	}

	public void updateDisplay(boolean bl) {
		GLFW.glfwSwapBuffers(this.window);
		pollEventQueue();
		if (this.fullscreen != this.actuallyFullscreen) {
			this.actuallyFullscreen = this.fullscreen;
			this.updateFullscreen(this.vsync);
		}
	}

	public void limitDisplayFPS() {
		double d = this.lastDrawTime + 1.0 / (double)this.getFramerateLimit();

		double e;
		for (e = GLFW.glfwGetTime(); e < d; e = GLFW.glfwGetTime()) {
			GLFW.glfwWaitEventsTimeout(d - e);
		}

		this.lastDrawTime = e;
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
			this.minecraft.resizeDisplay();
		}
	}

	private void setMode() {
		boolean bl = GLFW.glfwGetWindowMonitor(this.window) != 0L;
		if (this.fullscreen) {
			Monitor monitor = this.screenManager.findBestMonitor(this);
			if (monitor == null) {
				LOGGER.warn("Failed to find suitable monitor for fullscreen mode");
				this.fullscreen = false;
			} else {
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

	private void updateFullscreen(boolean bl) {
		try {
			this.setMode();
			this.minecraft.resizeDisplay();
			this.updateVsync(bl);
			this.minecraft.updateDisplay(false);
		} catch (Exception var3) {
			LOGGER.error("Couldn't toggle fullscreen", (Throwable)var3);
		}
	}

	public int calculateScale(int i, boolean bl) {
		int j = 1;

		while (j != i && j < this.framebufferWidth && j < this.framebufferHeight && this.framebufferWidth / (j + 1) >= 320 && this.framebufferHeight / (j + 1) >= 240) {
			j++;
		}

		if (bl && j % 2 != 0) {
			j++;
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

	public static void pollEventQueue() {
		GLFW.glfwPollEvents();
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
}
