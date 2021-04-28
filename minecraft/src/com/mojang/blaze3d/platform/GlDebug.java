package com.mojang.blaze3d.platform;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Queue;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLDebugMessageARBCallback;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.opengl.KHRDebug;

@Environment(EnvType.CLIENT)
public class GlDebug {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final int CIRCULAR_LOG_SIZE = 10;
	private static final Queue<GlDebug.LogEntry> MESSAGE_BUFFER = EvictingQueue.create(10);
	@Nullable
	private static volatile GlDebug.LogEntry lastEntry;
	private static final List<Integer> DEBUG_LEVELS = ImmutableList.of(37190, 37191, 37192, 33387);
	private static final List<Integer> DEBUG_LEVELS_ARB = ImmutableList.of(37190, 37191, 37192);
	private static boolean debugEnabled;

	private static String printUnknownToken(int i) {
		return "Unknown (0x" + Integer.toHexString(i).toUpperCase() + ")";
	}

	public static String sourceToString(int i) {
		switch (i) {
			case 33350:
				return "API";
			case 33351:
				return "WINDOW SYSTEM";
			case 33352:
				return "SHADER COMPILER";
			case 33353:
				return "THIRD PARTY";
			case 33354:
				return "APPLICATION";
			case 33355:
				return "OTHER";
			default:
				return printUnknownToken(i);
		}
	}

	public static String typeToString(int i) {
		switch (i) {
			case 33356:
				return "ERROR";
			case 33357:
				return "DEPRECATED BEHAVIOR";
			case 33358:
				return "UNDEFINED BEHAVIOR";
			case 33359:
				return "PORTABILITY";
			case 33360:
				return "PERFORMANCE";
			case 33361:
				return "OTHER";
			case 33384:
				return "MARKER";
			default:
				return printUnknownToken(i);
		}
	}

	public static String severityToString(int i) {
		switch (i) {
			case 33387:
				return "NOTIFICATION";
			case 37190:
				return "HIGH";
			case 37191:
				return "MEDIUM";
			case 37192:
				return "LOW";
			default:
				return printUnknownToken(i);
		}
	}

	private static void printDebugLog(int i, int j, int k, int l, int m, long n, long o) {
		String string = GLDebugMessageCallback.getMessage(m, n);
		GlDebug.LogEntry logEntry;
		synchronized (MESSAGE_BUFFER) {
			logEntry = lastEntry;
			if (logEntry != null && logEntry.isSame(i, j, k, l, string)) {
				logEntry.count = logEntry.count + 1;
			} else {
				logEntry = new GlDebug.LogEntry(i, j, k, l, string);
				MESSAGE_BUFFER.add(logEntry);
				lastEntry = logEntry;
			}
		}

		LOGGER.info("OpenGL debug message: {}", logEntry);
	}

	public static List<String> getLastOpenGlDebugMessages() {
		synchronized (MESSAGE_BUFFER) {
			List<String> list = Lists.<String>newArrayListWithCapacity(MESSAGE_BUFFER.size());

			for (GlDebug.LogEntry logEntry : MESSAGE_BUFFER) {
				list.add(logEntry + " x " + logEntry.count);
			}

			return list;
		}
	}

	public static boolean isDebugEnabled() {
		return debugEnabled;
	}

	public static void enableDebugCallback(int i, boolean bl) {
		RenderSystem.assertThread(RenderSystem::isInInitPhase);
		if (i > 0) {
			GLCapabilities gLCapabilities = GL.getCapabilities();
			if (gLCapabilities.GL_KHR_debug) {
				debugEnabled = true;
				GL11.glEnable(37600);
				if (bl) {
					GL11.glEnable(33346);
				}

				for (int j = 0; j < DEBUG_LEVELS.size(); j++) {
					boolean bl2 = j < i;
					KHRDebug.glDebugMessageControl(4352, 4352, (Integer)DEBUG_LEVELS.get(j), (int[])null, bl2);
				}

				KHRDebug.glDebugMessageCallback(GLX.make(GLDebugMessageCallback.create(GlDebug::printDebugLog), DebugMemoryUntracker::untrack), 0L);
			} else if (gLCapabilities.GL_ARB_debug_output) {
				debugEnabled = true;
				if (bl) {
					GL11.glEnable(33346);
				}

				for (int j = 0; j < DEBUG_LEVELS_ARB.size(); j++) {
					boolean bl2 = j < i;
					ARBDebugOutput.glDebugMessageControlARB(4352, 4352, (Integer)DEBUG_LEVELS_ARB.get(j), (int[])null, bl2);
				}

				ARBDebugOutput.glDebugMessageCallbackARB(GLX.make(GLDebugMessageARBCallback.create(GlDebug::printDebugLog), DebugMemoryUntracker::untrack), 0L);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static class LogEntry {
		private final int id;
		private final int source;
		private final int type;
		private final int severity;
		private final String message;
		private int count = 1;

		private LogEntry(int i, int j, int k, int l, String string) {
			this.id = k;
			this.source = i;
			this.type = j;
			this.severity = l;
			this.message = string;
		}

		private boolean isSame(int i, int j, int k, int l, String string) {
			return j == this.type && i == this.source && k == this.id && l == this.severity && string.equals(this.message);
		}

		public String toString() {
			return "id="
				+ this.id
				+ ", source="
				+ GlDebug.sourceToString(this.source)
				+ ", type="
				+ GlDebug.typeToString(this.type)
				+ ", severity="
				+ GlDebug.severityToString(this.severity)
				+ ", message='"
				+ this.message
				+ "'";
		}
	}
}
