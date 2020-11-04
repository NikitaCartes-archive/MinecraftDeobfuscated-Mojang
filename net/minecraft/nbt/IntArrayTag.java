/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;
import org.apache.commons.lang3.ArrayUtils;

public class IntArrayTag
extends CollectionTag<IntTag> {
    public static final TagType<IntArrayTag> TYPE = new TagType<IntArrayTag>(){

        @Override
        public IntArrayTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBits(192L);
            int j = dataInput.readInt();
            nbtAccounter.accountBits(32L * (long)j);
            int[] is = new int[j];
            for (int k = 0; k < j; ++k) {
                is[k] = dataInput.readInt();
            }
            return new IntArrayTag(is);
        }

        @Override
        public String getName() {
            return "INT[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Int_Array";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, i, nbtAccounter);
        }
    };
    private int[] data;

    public IntArrayTag(int[] is) {
        this.data = is;
    }

    public IntArrayTag(List<Integer> list) {
        this(IntArrayTag.toArray(list));
    }

    private static int[] toArray(List<Integer> list) {
        int[] is = new int[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            Integer integer = list.get(i);
            is[i] = integer == null ? 0 : integer;
        }
        return is;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.data.length);
        for (int i : this.data) {
            dataOutput.writeInt(i);
        }
    }

    @Override
    public byte getId() {
        return 11;
    }

    public TagType<IntArrayTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return this.getAsString();
    }

    @Override
    public IntArrayTag copy() {
        int[] is = new int[this.data.length];
        System.arraycopy(this.data, 0, is, 0, this.data.length);
        return new IntArrayTag(is);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof IntArrayTag && Arrays.equals(this.data, ((IntArrayTag)object).data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    public int[] getAsIntArray() {
        return this.data;
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitIntArray(this);
    }

    @Override
    public int size() {
        return this.data.length;
    }

    @Override
    public IntTag get(int i) {
        return IntTag.valueOf(this.data[i]);
    }

    @Override
    public IntTag set(int i, IntTag intTag) {
        int j = this.data[i];
        this.data[i] = intTag.getAsInt();
        return IntTag.valueOf(j);
    }

    @Override
    public void add(int i, IntTag intTag) {
        this.data = ArrayUtils.add(this.data, i, intTag.getAsInt());
    }

    @Override
    public boolean setTag(int i, Tag tag) {
        if (tag instanceof NumericTag) {
            this.data[i] = ((NumericTag)tag).getAsInt();
            return true;
        }
        return false;
    }

    @Override
    public boolean addTag(int i, Tag tag) {
        if (tag instanceof NumericTag) {
            this.data = ArrayUtils.add(this.data, i, ((NumericTag)tag).getAsInt());
            return true;
        }
        return false;
    }

    @Override
    public IntTag remove(int i) {
        int j = this.data[i];
        this.data = ArrayUtils.remove(this.data, i);
        return IntTag.valueOf(j);
    }

    @Override
    public byte getElementType() {
        return 3;
    }

    @Override
    public void clear() {
        this.data = new int[0];
    }

    @Override
    public /* synthetic */ Tag remove(int i) {
        return this.remove(i);
    }

    @Override
    public /* synthetic */ void add(int i, Tag tag) {
        this.add(i, (IntTag)tag);
    }

    @Override
    public /* synthetic */ Tag set(int i, Tag tag) {
        return this.set(i, (IntTag)tag);
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }

    @Override
    public /* synthetic */ Object remove(int i) {
        return this.remove(i);
    }

    @Override
    public /* synthetic */ void add(int i, Object object) {
        this.add(i, (IntTag)object);
    }

    @Override
    public /* synthetic */ Object set(int i, Object object) {
        return this.set(i, (IntTag)object);
    }

    @Override
    public /* synthetic */ Object get(int i) {
        return this.get(i);
    }
}

