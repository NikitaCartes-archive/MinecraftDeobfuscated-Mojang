package com.mojang.blaze3d.platform;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MemoryTracker {
	public static synchronized ByteBuffer createByteBuffer(int i) {
		return ByteBuffer.allocateDirect(i).order(ByteOrder.nativeOrder());
	}

	public static ShortBuffer createShortBuffer(int i) {
		return createByteBuffer(i << 1).asShortBuffer();
	}

	public static CharBuffer createCharBuffer(int i) {
		return createByteBuffer(i << 1).asCharBuffer();
	}

	public static IntBuffer createIntBuffer(int i) {
		return createByteBuffer(i << 2).asIntBuffer();
	}

	public static LongBuffer createLongBuffer(int i) {
		return createByteBuffer(i << 3).asLongBuffer();
	}

	public static FloatBuffer createFloatBuffer(int i) {
		return createByteBuffer(i << 2).asFloatBuffer();
	}

	public static DoubleBuffer createDoubleBuffer(int i) {
		return createByteBuffer(i << 3).asDoubleBuffer();
	}
}
