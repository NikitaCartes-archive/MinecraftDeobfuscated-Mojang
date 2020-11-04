/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;

public interface TagVisitor {
    public void visitString(StringTag var1);

    public void visitByte(ByteTag var1);

    public void visitShort(ShortTag var1);

    public void visitInt(IntTag var1);

    public void visitLong(LongTag var1);

    public void visitFloat(FloatTag var1);

    public void visitDouble(DoubleTag var1);

    public void visitByteArray(ByteArrayTag var1);

    public void visitIntArray(IntArrayTag var1);

    public void visitLongArray(LongArrayTag var1);

    public void visitList(ListTag var1);

    public void visitCompound(CompoundTag var1);

    public void visitEnd(EndTag var1);
}

