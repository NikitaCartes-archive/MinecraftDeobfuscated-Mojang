/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.core;

public final class QuartPos {
    public static int fromBlock(int i) {
        return i >> 2;
    }

    public static int toBlock(int i) {
        return i << 2;
    }

    public static int fromSection(int i) {
        return i << 2;
    }

    public static int toSection(int i) {
        return i >> 2;
    }
}

