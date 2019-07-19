/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.MonitorCreator;
import com.mojang.blaze3d.platform.Window;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMonitorCallback;

@Environment(value=EnvType.CLIENT)
public class ScreenManager {
    private final Long2ObjectMap<Monitor> monitors = new Long2ObjectOpenHashMap<Monitor>();
    private final MonitorCreator monitorCreator;

    public ScreenManager(MonitorCreator monitorCreator) {
        this.monitorCreator = monitorCreator;
        GLFW.glfwSetMonitorCallback(this::onMonitorChange);
        PointerBuffer pointerBuffer = GLFW.glfwGetMonitors();
        if (pointerBuffer != null) {
            for (int i = 0; i < pointerBuffer.limit(); ++i) {
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
        return (Monitor)this.monitors.get(l);
    }

    @Nullable
    public Monitor findBestMonitor(Window window) {
        long l = GLFW.glfwGetWindowMonitor(window.getWindow());
        if (l != 0L) {
            return this.getMonitor(l);
        }
        int i = window.getX();
        int j = i + window.getScreenWidth();
        int k = window.getY();
        int m = k + window.getScreenHeight();
        int n = -1;
        Monitor monitor = null;
        for (Monitor monitor2 : this.monitors.values()) {
            int x;
            int o = monitor2.getX();
            int p = o + monitor2.getCurrentMode().getWidth();
            int q = monitor2.getY();
            int r = q + monitor2.getCurrentMode().getHeight();
            int s = ScreenManager.clamp(i, o, p);
            int t = ScreenManager.clamp(j, o, p);
            int u = ScreenManager.clamp(k, q, r);
            int v = ScreenManager.clamp(m, q, r);
            int w = Math.max(0, t - s);
            int y = w * (x = Math.max(0, v - u));
            if (y <= n) continue;
            monitor = monitor2;
            n = y;
        }
        return monitor;
    }

    public static int clamp(int i, int j, int k) {
        if (i < j) {
            return j;
        }
        if (i > k) {
            return k;
        }
        return i;
    }

    public void shutdown() {
        GLFWMonitorCallback gLFWMonitorCallback = GLFW.glfwSetMonitorCallback(null);
        if (gLFWMonitorCallback != null) {
            gLFWMonitorCallback.free();
        }
    }
}

