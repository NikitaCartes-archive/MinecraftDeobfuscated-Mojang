/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;

public class StringTag
implements Tag {
    private static final int SELF_SIZE_IN_BITS = 288;
    public static final TagType<StringTag> TYPE = new TagType<StringTag>(){

        @Override
        public StringTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBits(288L);
            String string = dataInput.readUTF();
            nbtAccounter.accountBits(16 * string.length());
            return StringTag.valueOf(string);
        }

        @Override
        public String getName() {
            return "STRING";
        }

        @Override
        public String getPrettyName() {
            return "TAG_String";
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
    private static final StringTag EMPTY = new StringTag("");
    private static final char DOUBLE_QUOTE = '\"';
    private static final char SINGLE_QUOTE = '\'';
    private static final char ESCAPE = '\\';
    private static final char NOT_SET = '\u0000';
    private final String data;

    private StringTag(String string) {
        Objects.requireNonNull(string, "Null string not allowed");
        this.data = string;
    }

    public static StringTag valueOf(String string) {
        if (string.isEmpty()) {
            return EMPTY;
        }
        return new StringTag(string);
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(this.data);
    }

    @Override
    public byte getId() {
        return 8;
    }

    public TagType<StringTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return Tag.super.getAsString();
    }

    @Override
    public StringTag copy() {
        return this;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof StringTag && Objects.equals(this.data, ((StringTag)object).data);
    }

    public int hashCode() {
        return this.data.hashCode();
    }

    @Override
    public String getAsString() {
        return this.data;
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitString(this);
    }

    public static String quoteAndEscape(String string) {
        StringBuilder stringBuilder = new StringBuilder(" ");
        int c = 0;
        for (int i = 0; i < string.length(); ++i) {
            int d = string.charAt(i);
            if (d == 92) {
                stringBuilder.append('\\');
            } else if (d == 34 || d == 39) {
                if (c == 0) {
                    int n = c = d == 34 ? 39 : 34;
                }
                if (c == d) {
                    stringBuilder.append('\\');
                }
            }
            stringBuilder.append((char)d);
        }
        if (c == 0) {
            c = 34;
        }
        stringBuilder.setCharAt(0, (char)c);
        stringBuilder.append((char)c);
        return stringBuilder.toString();
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }
}

