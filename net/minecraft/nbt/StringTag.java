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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class StringTag
implements Tag {
    private String data;

    public StringTag() {
        this("");
    }

    public StringTag(String string) {
        Objects.requireNonNull(string, "Null string not allowed");
        this.data = string;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(this.data);
    }

    @Override
    public void load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
        nbtAccounter.accountBits(288L);
        this.data = dataInput.readUTF();
        nbtAccounter.accountBits(16 * this.data.length());
    }

    @Override
    public byte getId() {
        return 8;
    }

    @Override
    public String toString() {
        return StringTag.quoteAndEscape(this.data);
    }

    @Override
    public StringTag copy() {
        return new StringTag(this.data);
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
    public Component getPrettyDisplay(String string, int i) {
        String string2 = StringTag.quoteAndEscape(this.data);
        String string3 = string2.substring(0, 1);
        Component component = new TextComponent(string2.substring(1, string2.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_STRING);
        return new TextComponent(string3).append(component).append(string3);
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

