/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import net.minecraft.nbt.TagType;

public interface StreamTagVisitor {
    public ValueResult visitEnd();

    public ValueResult visit(String var1);

    public ValueResult visit(byte var1);

    public ValueResult visit(short var1);

    public ValueResult visit(int var1);

    public ValueResult visit(long var1);

    public ValueResult visit(float var1);

    public ValueResult visit(double var1);

    public ValueResult visit(byte[] var1);

    public ValueResult visit(int[] var1);

    public ValueResult visit(long[] var1);

    public ValueResult visitList(TagType<?> var1, int var2);

    public EntryResult visitEntry(TagType<?> var1);

    public EntryResult visitEntry(TagType<?> var1, String var2);

    public EntryResult visitElement(TagType<?> var1, int var2);

    public ValueResult visitContainerEnd();

    public ValueResult visitRootEntry(TagType<?> var1);

    public static enum EntryResult {
        ENTER,
        SKIP,
        BREAK,
        HALT;

    }

    public static enum ValueResult {
        CONTINUE,
        BREAK,
        HALT;

    }
}

