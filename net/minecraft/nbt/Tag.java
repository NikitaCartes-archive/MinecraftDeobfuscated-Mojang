/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;

public interface Tag {
    public static final int OBJECT_HEADER = 64;
    public static final int ARRAY_HEADER = 96;
    public static final int OBJECT_REFERENCE = 32;
    public static final int STRING_SIZE = 224;
    public static final byte TAG_END = 0;
    public static final byte TAG_BYTE = 1;
    public static final byte TAG_SHORT = 2;
    public static final byte TAG_INT = 3;
    public static final byte TAG_LONG = 4;
    public static final byte TAG_FLOAT = 5;
    public static final byte TAG_DOUBLE = 6;
    public static final byte TAG_BYTE_ARRAY = 7;
    public static final byte TAG_STRING = 8;
    public static final byte TAG_LIST = 9;
    public static final byte TAG_COMPOUND = 10;
    public static final byte TAG_INT_ARRAY = 11;
    public static final byte TAG_LONG_ARRAY = 12;
    public static final byte TAG_ANY_NUMERIC = 99;
    public static final int MAX_DEPTH = 512;

    public void write(DataOutput var1) throws IOException;

    public String toString();

    public byte getId();

    public TagType<?> getType();

    public Tag copy();

    default public String getAsString() {
        return new StringTagVisitor().visit(this);
    }

    public void accept(TagVisitor var1);

    public StreamTagVisitor.ValueResult accept(StreamTagVisitor var1);

    default public void acceptAsRoot(StreamTagVisitor streamTagVisitor) {
        StreamTagVisitor.ValueResult valueResult = streamTagVisitor.visitRootEntry(this.getType());
        if (valueResult == StreamTagVisitor.ValueResult.CONTINUE) {
            this.accept(streamTagVisitor);
        }
    }
}

