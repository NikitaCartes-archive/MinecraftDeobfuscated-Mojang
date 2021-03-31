package com.mojang.blaze3d.platform;

import com.google.common.base.Charsets;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.StringDecomposer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class ClipboardManager {
	public static final int FORMAT_UNAVAILABLE = 65545;
	private final ByteBuffer clipboardScratchBuffer = BufferUtils.createByteBuffer(8192);

	public String getClipboard(long l, GLFWErrorCallbackI gLFWErrorCallbackI) {
		GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback(gLFWErrorCallbackI);
		String string = GLFW.glfwGetClipboardString(l);
		string = string != null ? StringDecomposer.filterBrokenSurrogates(string) : "";
		GLFWErrorCallback gLFWErrorCallback2 = GLFW.glfwSetErrorCallback(gLFWErrorCallback);
		if (gLFWErrorCallback2 != null) {
			gLFWErrorCallback2.free();
		}

		return string;
	}

	private static void pushClipboard(long l, ByteBuffer byteBuffer, byte[] bs) {
		byteBuffer.clear();
		byteBuffer.put(bs);
		byteBuffer.put((byte)0);
		byteBuffer.flip();
		GLFW.glfwSetClipboardString(l, byteBuffer);
	}

	public void setClipboard(long l, String string) {
		byte[] bs = string.getBytes(Charsets.UTF_8);
		int i = bs.length + 1;
		if (i < this.clipboardScratchBuffer.capacity()) {
			pushClipboard(l, this.clipboardScratchBuffer, bs);
		} else {
			ByteBuffer byteBuffer = MemoryUtil.memAlloc(i);

			try {
				pushClipboard(l, byteBuffer, bs);
			} finally {
				MemoryUtil.memFree(byteBuffer);
			}
		}
	}
}
