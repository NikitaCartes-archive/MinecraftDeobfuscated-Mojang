package com.mojang.blaze3d.platform;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMonitorCallback;

@Environment(EnvType.CLIENT)
public class ScreenManager {
	private final Long2ObjectMap<Monitor> monitors = new Long2ObjectOpenHashMap<>();
	private final MonitorCreator monitorCreator;

	public ScreenManager(MonitorCreator monitorCreator) {
		this.monitorCreator = monitorCreator;
		GLFW.glfwSetMonitorCallback(this::onMonitorChange);
		PointerBuffer pointerBuffer = GLFW.glfwGetMonitors();
		if (pointerBuffer != null) {
			for (int i = 0; i < pointerBuffer.limit(); i++) {
				long l = pointerBuffer.get(i);
				this.monitors.put(l, monitorCreator.createMonitor(l));
			}
		}
	}

	private void onMonitorChange(long l, int i) {
		if (i == 262145) {
			this.monitors.put(l, this.monitorCreator.createMonitor(l));
		} else if (i == 262146) {
			this.monitors.remove(l);
		}
	}

	@Nullable
	public Monitor getMonitor(long l) {
		return this.monitors.get(l);
	}

	@Nullable
	public Monitor findBestMonitor(Window window) {
		long l = GLFW.glfwGetWindowMonitor(window.getWindow());
		if (l != 0L) {
			return this.getMonitor(l);
		} else {
			int i = window.getX();
			int j = i + window.getScreenWidth();
			int k = window.getY();
			int m = k + window.getScreenHeight();
			int n = -1;
			Monitor monitor = null;

			for (Monitor monitor2 : this.monitors.values()) {
				int o = monitor2.getX();
				int p = o + monitor2.getCurrentMode().getWidth();
				int q = monitor2.getY();
				int r = q + monitor2.getCurrentMode().getHeight();
				int s = clamp(i, o, p);
				int t = clamp(j, o, p);
				int u = clamp(k, q, r);
				int v = clamp(m, q, r);
				int w = Math.max(0, t - s);
				int x = Math.max(0, v - u);
				int y = w * x;
				if (y > n) {
					monitor = monitor2;
					n = y;
				}
			}

			return monitor;
		}
	}

	public static int clamp(int i, int j, int k) {
		if (i < j) {
			return j;
		} else {
			return i > k ? k : i;
		}
	}

	public void shutdown() {
		GLFWMonitorCallback gLFWMonitorCallback = GLFW.glfwSetMonitorCallback(null);
		if (gLFWMonitorCallback != null) {
			gLFWMonitorCallback.free();
		}
	}
}
