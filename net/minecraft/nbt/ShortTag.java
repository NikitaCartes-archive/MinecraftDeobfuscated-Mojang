/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;

public class ShortTag
extends NumericTag {
    public static final TagType<ShortTag> TYPE = new TagType<ShortTag>(){

        @Override
        public ShortTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBits(80L);
            return ShortTag.valueOf(dataInput.readShort());
        }

        @Override
        public String getName() {
            return "SHORT";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Short";
        }

        @Override
        public boolean isValue() {
            return true;
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, i, nbtAccounter);
        }
    };
    private final short data;

    private ShortTag(short s) {
        this.data = s;
    }

    public static ShortTag valueOf(short s) {
        if (s >= -128 && s <= 1024) {
            return Cache.cache[s - -128];
        }
        return new ShortTag(s);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeShort(this.data);
    }

    @Override
    public byte getId() {
        return 2;
    }

    public TagType<ShortTag> getType() {
        return TYPE;
    }

    @Override
    public ShortTag copy() {
        return this;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof ShortTag && this.data == ((ShortTag)object).data;
    }

    public int hashCode() {
        return this.data;
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitShort(this);
    }

    @Override
    public long getAsLong() {
        return this.data;
    }

    @Override
    public int getAsInt() {
        return this.data;
    }

    @Override
    public short getAsShort() {
        return this.data;
    }

    @Override
    public byte getAsByte() {
        return (byte)(this.data & 0xFF);
    }

    @Override
    public double getAsDouble() {
        return this.data;
    }

    @Override
    public float getAsFloat() {
        return this.data;
    }

    @Override
    public Number getAsNumber() {
        return this.data;
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }

    static class Cache {
        static final ShortTag[] cache = new ShortTag[1153];

        static {
            for (int i = 0; i < cache.length; ++i) {
                Cache.cache[i] = new ShortTag((short)(-128 + i));
            }
        }
    }
}

