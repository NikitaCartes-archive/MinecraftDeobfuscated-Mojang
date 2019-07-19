package com.mojang.blaze3d.platform;

import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.MemoryUtil;

@Environment(EnvType.CLIENT)
public class ClipboardManager {
	private final ByteBuffer clipboardScratchBuffer = ByteBuffer.allocateDirect(1024);

	public String getClipboard(long l, GLFWErrorCallbackI gLFWErrorCallbackI) {
		GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback(gLFWErrorCallbackI);
		String string = GLFW.glfwGetClipboardString(l);
		string = string != null ? SharedConstants.filterUnicodeSupplementary(string) : "";
		GLFWErrorCallback gLFWErrorCallback2 = GLFW.glfwSetErrorCallback(gLFWErrorCallback);
		if (gLFWErrorCallback2 != null) {
			gLFWErrorCallback2.free();
		}

		return string;
	}

	private void setClipboard(long l, ByteBuffer byteBuffer, String string) {
		MemoryUtil.memUTF8(string, true, byteBuffer);
		GLFW.glfwSetClipboardString(l, byteBuffer);
	}

	public void setClipboard(long l, String string) {
		int i = MemoryUtil.memLengthUTF8(string, true);
		if (i < this.clipboardScratchBuffer.capacity()) {
			this.setClipboard(l, this.clipboardScratchBuffer, string);
			this.clipboardScratchBuffer.clear();
		} else {
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(i);
			this.setClipboard(l, byteBuffer, string);
		}
	}
}
