/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.platform;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.DebugMemoryUntracker;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLDebugMessageARBCallback;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.opengl.KHRDebug;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class GlDebug {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CIRCULAR_LOG_SIZE = 10;
    private static final Queue<LogEntry> MESSAGE_BUFFER = EvictingQueue.create(10);
    @Nullable
    private static volatile LogEntry lastEntry;
    private static final List<Integer> DEBUG_LEVELS;
    private static final List<Integer> DEBUG_LEVELS_ARB;
    private static boolean debugEnabled;

    private static String printUnknownToken(int i) {
        return "Unknown (0x" + Integer.toHexString(i).toUpperCase() + ")";
    }

    public static String sourceToString(int i) {
        switch (i) {
            case 33350: {
                return "API";
            }
            case 33351: {
                return "WINDOW SYSTEM";
            }
            case 33352: {
                return "SHADER COMPILER";
            }
            case 33353: {
                return "THIRD PARTY";
            }
            case 33354: {
                return "APPLICATION";
            }
            case 33355: {
                return "OTHER";
            }
        }
        return GlDebug.printUnknownToken(i);
    }

    public static String typeToString(int i) {
        switch (i) {
            case 33356: {
                return "ERROR";
            }
            case 33357: {
                return "DEPRECATED BEHAVIOR";
            }
            case 33358: {
                return "UNDEFINED BEHAVIOR";
            }
            case 33359: {
                return "PORTABILITY";
            }
            case 33360: {
                return "PERFORMANCE";
            }
            case 33361: {
                return "OTHER";
            }
            case 33384: {
                return "MARKER";
            }
        }
        return GlDebug.printUnknownToken(i);
    }

    public static String severityToString(int i) {
        switch (i) {
            case 37190: {
                return "HIGH";
            }
            case 37191: {
                return "MEDIUM";
            }
            case 37192: {
                return "LOW";
            }
            case 33387: {
                return "NOTIFICATION";
            }
        }
        return GlDebug.printUnknownToken(i);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void printDebugLog(int i, int j, int k, int l, int m, long n, long o) {
        LogEntry logEntry;
        String string = GLDebugMessageCallback.getMessage(m, n);
        Queue<LogEntry> queue = MESSAGE_BUFFER;
        synchronized (queue) {
            logEntry = lastEntry;
            if (logEntry == null || !logEntry.isSame(i, j, k, l, string)) {
                logEntry = new LogEntry(i, j, k, l, string);
                MESSAGE_BUFFER.add(logEntry);
                lastEntry = logEntry;
            } else {
                ++logEntry.count;
            }
        }
        LOGGER.info("OpenGL debug message: {}", (Object)logEntry);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static List<String> getLastOpenGlDebugMessages() {
        Queue<LogEntry> queue = MESSAGE_BUFFER;
        synchronized (queue) {
            ArrayList<String> list = Lists.newArrayListWithCapacity(MESSAGE_BUFFER.size());
            for (LogEntry logEntry : MESSAGE_BUFFER) {
                list.add(logEntry + " x " + logEntry.count);
            }
            return list;
        }
    }

    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static void enableDebugCallback(int i, boolean bl) {
        RenderSystem.assertInInitPhase();
        if (i <= 0) {
            return;
        }
        GLCapabilities gLCapabilities = GL.getCapabilities();
        if (gLCapabilities.GL_KHR_debug) {
            debugEnabled = true;
            GL11.glEnable(37600);
            if (bl) {
                GL11.glEnable(33346);
            }
            for (int j = 0; j < DEBUG_LEVELS.size(); ++j) {
                boolean bl2 = j < i;
                KHRDebug.glDebugMessageControl(4352, 4352, (int)DEBUG_LEVELS.get(j), (int[])null, bl2);
            }
            KHRDebug.glDebugMessageCallback(GLX.make(GLDebugMessageCallback.create(GlDebug::printDebugLog), DebugMemoryUntracker::untrack), 0L);
        } else if (gLCapabilities.GL_ARB_debug_output) {
            debugEnabled = true;
            if (bl) {
                GL11.glEnable(33346);
            }
            for (int j = 0; j < DEBUG_LEVELS_ARB.size(); ++j) {
                boolean bl2 = j < i;
                ARBDebugOutput.glDebugMessageControlARB(4352, 4352, (int)DEBUG_LEVELS_ARB.get(j), (int[])null, bl2);
            }
            ARBDebugOutput.glDebugMessageCallbackARB(GLX.make(GLDebugMessageARBCallback.create(GlDebug::printDebugLog), DebugMemoryUntracker::untrack), 0L);
        }
    }

    static {
        DEBUG_LEVELS = ImmutableList.of(Integer.valueOf(37190), Integer.valueOf(37191), Integer.valueOf(37192), Integer.valueOf(33387));
        DEBUG_LEVELS_ARB = ImmutableList.of(Integer.valueOf(37190), Integer.valueOf(37191), Integer.valueOf(37192));
    }

    @Environment(value=EnvType.CLIENT)
    static class LogEntry {
        private final int id;
        private final int source;
        private final int type;
        private final int severity;
        private final String message;
        int count = 1;

        LogEntry(int i, int j, int k, int l, String string) {
            this.id = k;
            this.source = i;
            this.type = j;
            this.severity = l;
            this.message = string;
        }

        boolean isSame(int i, int j, int k, int l, String string) {
            return j == this.type && i == this.source && k == this.id && l == this.severity && string.equals(this.message);
        }

        public String toString() {
            return "id=" + this.id + ", source=" + GlDebug.sourceToString(this.source) + ", type=" + GlDebug.typeToString(this.type) + ", severity=" + GlDebug.severityToString(this.severity) + ", message='" + this.message + "'";
        }
    }
}

