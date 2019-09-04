package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MemoryTracker {
	public static synchronized int genLists(int i) {
		int j = RenderSystem.genLists(i);
		if (j == 0) {
			int k = RenderSystem.getError();
			String string = "No error code reported";
			if (k != 0) {
				string = GLX.getErrorString(k);
			}

			throw new IllegalStateException("glGenLists returned an ID of 0 for a count of " + i + ", GL error (" + k + "): " + string);
		} else {
			return j;
		}
	}

	public static synchronized void releaseLists(int i, int j) {
		RenderSystem.deleteLists(i, j);
	}

	public static synchronized void releaseList(int i) {
		releaseLists(i, 1);
	}

	public static synchronized ByteBuffer createByteBuffer(int i) {
		return ByteBuffer.allocateDirect(i).order(ByteOrder.nativeOrder());
	}

	public static FloatBuffer createFloatBuffer(int i) {
		return createByteBuffer(i << 2).asFloatBuffer();
	}
}
