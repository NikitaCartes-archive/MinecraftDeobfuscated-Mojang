/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.apache.commons.lang3.ArrayUtils;

public class LongArrayTag
extends CollectionTag<LongTag> {
    public static final TagType<LongArrayTag> TYPE = new TagType<LongArrayTag>(){

        @Override
        public LongArrayTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBits(192L);
            int j = dataInput.readInt();
            nbtAccounter.accountBits(64L * (long)j);
            long[] ls = new long[j];
            for (int k = 0; k < j; ++k) {
                ls[k] = dataInput.readLong();
            }
            return new LongArrayTag(ls);
        }

        @Override
        public String getName() {
            return "LONG[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Long_Array";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, i, nbtAccounter);
        }
    };
    private long[] data;

    public LongArrayTag(long[] ls) {
        this.data = ls;
    }

    public LongArrayTag(LongSet longSet) {
        this.data = longSet.toLongArray();
    }

    public LongArrayTag(List<Long> list) {
        this(LongArrayTag.toArray(list));
    }

    private static long[] toArray(List<Long> list) {
        long[] ls = new long[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            Long long_ = list.get(i);
            ls[i] = long_ == null ? 0L : long_;
        }
        return ls;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.data.length);
        for (long l : this.data) {
            dataOutput.writeLong(l);
        }
    }

    @Override
    public byte getId() {
        return 12;
    }

    public TagType<LongArrayTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("[L;");
        for (int i = 0; i < this.data.length; ++i) {
            if (i != 0) {
                stringBuilder.append(',');
            }
            stringBuilder.append(this.data[i]).append('L');
        }
        return stringBuilder.append(']').toString();
    }

    @Override
    public LongArrayTag copy() {
        long[] ls = new long[this.data.length];
        System.arraycopy(this.data, 0, ls, 0, this.data.length);
        return new LongArrayTag(ls);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof LongArrayTag && Arrays.equals(this.data, ((LongArrayTag)object).data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    @Override
    public Component getPrettyDisplay(String string, int i) {
        MutableComponent component = new TextComponent("L").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        MutableComponent mutableComponent = new TextComponent("[").append(component).append(";");
        for (int j = 0; j < this.data.length; ++j) {
            MutableComponent mutableComponent2 = new TextComponent(String.valueOf(this.data[j])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
            mutableComponent.append(" ").append(mutableComponent2).append(component);
            if (j == this.data.length - 1) continue;
            mutableComponent.append(",");
        }
        mutableComponent.append("]");
        return mutableComponent;
    }

    public long[] getAsLongArray() {
        return this.data;
    }

    @Override
    public int size() {
        return this.data.length;
    }

    @Override
    public LongTag get(int i) {
        return LongTag.valueOf(this.data[i]);
    }

    @Override
    public LongTag set(int i, LongTag longTag) {
        long l = this.data[i];
        this.data[i] = longTag.getAsLong();
        return LongTag.valueOf(l);
    }

    @Override
    public void add(int i, LongTag longTag) {
        this.data = ArrayUtils.add(this.data, i, longTag.getAsLong());
    }

    @Override
    public boolean setTag(int i, Tag tag) {
        if (tag instanceof NumericTag) {
            this.data[i] = ((NumericTag)tag).getAsLong();
            return true;
        }
        return false;
    }

    @Override
    public boolean addTag(int i, Tag tag) {
        if (tag instanceof NumericTag) {
            this.data = ArrayUtils.add(this.data, i, ((NumericTag)tag).getAsLong());
            return true;
        }
        return false;
    }

    @Override
    public LongTag remove(int i) {
        long l = this.data[i];
        this.data = ArrayUtils.remove(this.data, i);
        return LongTag.valueOf(l);
    }

    @Override
    public void clear() {
        this.data = new long[0];
    }

    @Override
    public /* synthetic */ Tag remove(int i) {
        return this.remove(i);
    }

    @Override
    public /* synthetic */ void add(int i, Tag tag) {
        this.add(i, (LongTag)tag);
    }

    @Override
    public /* synthetic */ Tag set(int i, Tag tag) {
        return this.set(i, (LongTag)tag);
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
        this.add(i, (LongTag)object);
    }

    @Override
    public /* synthetic */ Object set(int i, Object object) {
        return this.set(i, (LongTag)object);
    }

    @Override
    public /* synthetic */ Object get(int i) {
        return this.get(i);
    }
}

