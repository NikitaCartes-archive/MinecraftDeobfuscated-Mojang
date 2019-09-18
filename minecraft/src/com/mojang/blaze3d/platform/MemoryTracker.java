package com.mojang.blaze3d.platform;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MemoryTracker {
	public static synchronized ByteBuffer createByteBuffer(int i) {
		return ByteBuffer.allocateDirect(i).order(ByteOrder.nativeOrder());
	}

	public static FloatBuffer createFloatBuffer(int i) {
		return createByteBuffer(i << 2).asFloatBuffer();
	}
}
