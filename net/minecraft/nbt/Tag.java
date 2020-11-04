/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;

public interface Tag {
    public void write(DataOutput var1) throws IOException;

    public String toString();

    public byte getId();

    public TagType<?> getType();

    public Tag copy();

    default public String getAsString() {
        return new StringTagVisitor().visit(this);
    }

    public void accept(TagVisitor var1);
}

