package com.mojang.blaze3d;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class Blaze3D {
	public static void process(RenderPipeline renderPipeline, float f) {
		ConcurrentLinkedQueue<RenderCall> concurrentLinkedQueue = renderPipeline.getRecordingQueue();
	}

	public static void render(RenderPipeline renderPipeline, float f) {
		ConcurrentLinkedQueue<RenderCall> concurrentLinkedQueue = renderPipeline.getProcessedQueue();
	}

	public static void youJustLostTheGame() {
		MemoryUtil.memSet(0L, 0, 1L);
	}

	public static double getTime() {
		return GLFW.glfwGetTime();
	}
}
