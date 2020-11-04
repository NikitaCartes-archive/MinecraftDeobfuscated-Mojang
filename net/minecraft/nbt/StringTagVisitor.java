/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;
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
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagVisitor;

public class StringTagVisitor
implements TagVisitor {
    private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
    private final StringBuilder builder = new StringBuilder();

    public String visit(Tag tag) {
        tag.accept(this);
        return this.builder.toString();
    }

    @Override
    public void visitString(StringTag stringTag) {
        this.builder.append(StringTag.quoteAndEscape(stringTag.getAsString()));
    }

    @Override
    public void visitByte(ByteTag byteTag) {
        this.builder.append(byteTag.getAsNumber()).append('b');
    }

    @Override
    public void visitShort(ShortTag shortTag) {
        this.builder.append(shortTag.getAsNumber()).append('s');
    }

    @Override
    public void visitInt(IntTag intTag) {
        this.builder.append(intTag.getAsNumber());
    }

    @Override
    public void visitLong(LongTag longTag) {
        this.builder.append(longTag.getAsNumber()).append('L');
    }

    @Override
    public void visitFloat(FloatTag floatTag) {
        this.builder.append(floatTag.getAsFloat()).append('f');
    }

    @Override
    public void visitDouble(DoubleTag doubleTag) {
        this.builder.append(doubleTag.getAsDouble()).append('d');
    }

    @Override
    public void visitByteArray(ByteArrayTag byteArrayTag) {
        this.builder.append("[B;");
        byte[] bs = byteArrayTag.getAsByteArray();
        for (int i = 0; i < bs.length; ++i) {
            if (i != 0) {
                this.builder.append(',');
            }
            this.builder.append(bs[i]).append('B');
        }
        this.builder.append(']');
    }

    @Override
    public void visitIntArray(IntArrayTag intArrayTag) {
        this.builder.append("[I;");
        int[] is = intArrayTag.getAsIntArray();
        for (int i = 0; i < is.length; ++i) {
            if (i != 0) {
                this.builder.append(',');
            }
            this.builder.append(is[i]);
        }
        this.builder.append(']');
    }

    @Override
    public void visitLongArray(LongArrayTag longArrayTag) {
        this.builder.append("[L;");
        long[] ls = longArrayTag.getAsLongArray();
        for (int i = 0; i < ls.length; ++i) {
            if (i != 0) {
                this.builder.append(',');
            }
            this.builder.append(ls[i]).append('L');
        }
        this.builder.append(']');
    }

    @Override
    public void visitList(ListTag listTag) {
        this.builder.append('[');
        for (int i = 0; i < listTag.size(); ++i) {
            if (i != 0) {
                this.builder.append(',');
            }
            this.builder.append(new StringTagVisitor().visit(listTag.get(i)));
        }
        this.builder.append(']');
    }

    @Override
    public void visitCompound(CompoundTag compoundTag) {
        this.builder.append('{');
        ArrayList<String> list = Lists.newArrayList(compoundTag.getAllKeys());
        Collections.sort(list);
        for (String string : list) {
            if (this.builder.length() != 1) {
                this.builder.append(',');
            }
            this.builder.append(StringTagVisitor.handleEscape(string)).append(':').append(new StringTagVisitor().visit(compoundTag.get(string)));
        }
        this.builder.append('}');
    }

    protected static String handleEscape(String string) {
        if (SIMPLE_VALUE.matcher(string).matches()) {
            return string;
        }
        return StringTag.quoteAndEscape(string);
    }

    @Override
    public void visitEnd(EndTag endTag) {
        this.builder.append("END");
    }
}

