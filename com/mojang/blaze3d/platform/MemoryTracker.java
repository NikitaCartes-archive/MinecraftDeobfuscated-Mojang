/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.platform;

import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
public class MemoryTracker {
    private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);

    public static ByteBuffer create(int i) {
        long l = ALLOCATOR.malloc(i);
        if (l == 0L) {
            throw new OutOfMemoryError("Failed to allocate " + i + " bytes");
        }
        return MemoryUtil.memByteBuffer(l, i);
    }

    public static ByteBuffer resize(ByteBuffer byteBuffer, int i) {
        long l = ALLOCATOR.realloc(MemoryUtil.memAddress0(byteBuffer), i);
        if (l == 0L) {
            throw new OutOfMemoryError("Failed to resize buffer from " + byteBuffer.capacity() + " bytes to " + i + " bytes");
        }
        return MemoryUtil.memByteBuffer(l, i);
    }
}

