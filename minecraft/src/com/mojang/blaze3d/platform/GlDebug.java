package com.mojang.blaze3d.platform;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
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
	private static final List<Integer> DEBUG_LEVELS = ImmutableList.of(37190, 37191, 37192, 33387);
	private static final List<Integer> DEBUG_LEVELS_ARB = ImmutableList.of(37190, 37191, 37192);

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
		LOGGER.info(
			"OpenGL debug message, id={}, source={}, type={}, severity={}, message={}",
			k,
			sourceToString(i),
			typeToString(j),
			severityToString(l),
			GLDebugMessageCallback.getMessage(m, n)
		);
	}

	public static void enableDebugCallback(int i, boolean bl) {
		RenderSystem.assertThread(RenderSystem::isInInitPhase);
		if (i > 0) {
			GLCapabilities gLCapabilities = GL.getCapabilities();
			if (gLCapabilities.GL_KHR_debug) {
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
}
