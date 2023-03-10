/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;
import org.apache.commons.lang3.ArrayUtils;

public class ByteArrayTag
extends CollectionTag<ByteTag> {
    private static final int SELF_SIZE_IN_BYTES = 24;
    public static final TagType<ByteArrayTag> TYPE = new TagType.VariableSize<ByteArrayTag>(){

        @Override
        public ByteArrayTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBytes(24L);
            int j = dataInput.readInt();
            nbtAccounter.accountBytes(1L * (long)j);
            byte[] bs = new byte[j];
            dataInput.readFully(bs);
            return new ByteArrayTag(bs);
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor) throws IOException {
            int i = dataInput.readInt();
            byte[] bs = new byte[i];
            dataInput.readFully(bs);
            return streamTagVisitor.visit(bs);
        }

        @Override
        public void skip(DataInput dataInput) throws IOException {
            dataInput.skipBytes(dataInput.readInt() * 1);
        }

        @Override
        public String getName() {
            return "BYTE[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Byte_Array";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, i, nbtAccounter);
        }
    };
    private byte[] data;

    public ByteArrayTag(byte[] bs) {
        this.data = bs;
    }

    public ByteArrayTag(List<Byte> list) {
        this(ByteArrayTag.toArray(list));
    }

    private static byte[] toArray(List<Byte> list) {
        byte[] bs = new byte[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            Byte byte_ = list.get(i);
            bs[i] = byte_ == null ? (byte)0 : byte_;
        }
        return bs;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.data.length);
        dataOutput.write(this.data);
    }

    @Override
    public int sizeInBytes() {
        return 24 + 1 * this.data.length;
    }

    @Override
    public byte getId() {
        return 7;
    }

    public TagType<ByteArrayTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return this.getAsString();
    }

    @Override
    public Tag copy() {
        byte[] bs = new byte[this.data.length];
        System.arraycopy(this.data, 0, bs, 0, this.data.length);
        return new ByteArrayTag(bs);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof ByteArrayTag && Arrays.equals(this.data, ((ByteArrayTag)object).data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitByteArray(this);
    }

    public byte[] getAsByteArray() {
        return this.data;
    }

    @Override
    public int size() {
        return this.data.length;
    }

    @Override
    public ByteTag get(int i) {
        return ByteTag.valueOf(this.data[i]);
    }

    @Override
    public ByteTag set(int i, ByteTag byteTag) {
        byte b = this.data[i];
        this.data[i] = byteTag.getAsByte();
        return ByteTag.valueOf(b);
    }

    @Override
    public void add(int i, ByteTag byteTag) {
        this.data = ArrayUtils.add(this.data, i, byteTag.getAsByte());
    }

    @Override
    public boolean setTag(int i, Tag tag) {
        if (tag instanceof NumericTag) {
            this.data[i] = ((NumericTag)tag).getAsByte();
            return true;
        }
        return false;
    }

    @Override
    public boolean addTag(int i, Tag tag) {
        if (tag instanceof NumericTag) {
            this.data = ArrayUtils.add(this.data, i, ((NumericTag)tag).getAsByte());
            return true;
        }
        return false;
    }

    @Override
    public ByteTag remove(int i) {
        byte b = this.data[i];
        this.data = ArrayUtils.remove(this.data, i);
        return ByteTag.valueOf(b);
    }

    @Override
    public byte getElementType() {
        return 1;
    }

    @Override
    public void clear() {
        this.data = new byte[0];
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
        return streamTagVisitor.visit(this.data);
    }

    @Override
    public /* synthetic */ Tag remove(int i) {
        return this.remove(i);
    }

    @Override
    public /* synthetic */ void add(int i, Tag tag) {
        this.add(i, (ByteTag)tag);
    }

    @Override
    public /* synthetic */ Tag set(int i, Tag tag) {
        return this.set(i, (ByteTag)tag);
    }

    @Override
    public /* synthetic */ Object remove(int i) {
        return this.remove(i);
    }

    @Override
    public /* synthetic */ void add(int i, Object object) {
        this.add(i, (ByteTag)object);
    }

    @Override
    public /* synthetic */ Object set(int i, Object object) {
        return this.set(i, (ByteTag)object);
    }

    @Override
    public /* synthetic */ Object get(int i) {
        return this.get(i);
    }
}

