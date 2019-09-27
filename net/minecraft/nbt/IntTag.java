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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class IntTag
extends NumericTag {
    public static final TagType<IntTag> TYPE = new TagType<IntTag>(){

        @Override
        public IntTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBits(96L);
            return IntTag.valueOf(dataInput.readInt());
        }

        @Override
        public String getName() {
            return "INT";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Int";
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
    private final int data;

    private IntTag(int i) {
        this.data = i;
    }

    public static IntTag valueOf(int i) {
        if (i >= -128 && i <= 1024) {
            return Cache.cache[i + 128];
        }
        return new IntTag(i);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.data);
    }

    @Override
    public byte getId() {
        return 3;
    }

    public TagType<IntTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return String.valueOf(this.data);
    }

    @Override
    public IntTag copy() {
        return this;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof IntTag && this.data == ((IntTag)object).data;
    }

    public int hashCode() {
        return this.data;
    }

    @Override
    public Component getPrettyDisplay(String string, int i) {
        return new TextComponent(String.valueOf(this.data)).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
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
        return (short)(this.data & 0xFFFF);
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
        static final IntTag[] cache = new IntTag[1153];

        static {
            for (int i = 0; i < cache.length; ++i) {
                Cache.cache[i] = new IntTag(-128 + i);
            }
        }
    }
}

