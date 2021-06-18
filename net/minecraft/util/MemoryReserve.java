/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import org.jetbrains.annotations.Nullable;

public class MemoryReserve {
    @Nullable
    private static byte[] reserve = null;

    public static void allocate() {
        reserve = new byte[0xA00000];
    }

    public static void release() {
        reserve = new byte[0];
    }
}

