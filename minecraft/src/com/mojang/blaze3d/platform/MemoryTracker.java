package com.mojang.blaze3d.platform;

import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.MemoryUtil.MemoryAllocator;

@Environment(EnvType.CLIENT)
public class MemoryTracker {
	private static final MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);

	public static ByteBuffer create(int i) {
		long l = ALLOCATOR.malloc((long)i);
		if (l == 0L) {
			throw new OutOfMemoryError("Failed to allocate " + i + " bytes");
		} else {
			return MemoryUtil.memByteBuffer(l, i);
		}
	}

	public static ByteBuffer resize(ByteBuffer byteBuffer, int i) {
		long l = ALLOCATOR.realloc(MemoryUtil.memAddress0(byteBuffer), (long)i);
		if (l == 0L) {
			throw new OutOfMemoryError("Failed to resize buffer from " + byteBuffer.capacity() + " bytes to " + i + " bytes");
		} else {
			return MemoryUtil.memByteBuffer(l, i);
		}
	}

	public static void free(ByteBuffer byteBuffer) {
		ALLOCATOR.free(MemoryUtil.memAddress0(byteBuffer));
	}
}
