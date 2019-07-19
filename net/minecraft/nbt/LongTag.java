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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class LongTag
extends NumericTag {
    private long data;

    LongTag() {
    }

    public LongTag(long l) {
        this.data = l;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeLong(this.data);
    }

    @Override
    public void load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
        nbtAccounter.accountBits(128L);
        this.data = dataInput.readLong();
    }

    @Override
    public byte getId() {
        return 4;
    }

    @Override
    public String toString() {
        return this.data + "L";
    }

    @Override
    public LongTag copy() {
        return new LongTag(this.data);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof LongTag && this.data == ((LongTag)object).data;
    }

    public int hashCode() {
        return (int)(this.data ^ this.data >>> 32);
    }

    @Override
    public Component getPrettyDisplay(String string, int i) {
        Component component = new TextComponent("L").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        return new TextComponent(String.valueOf(this.data)).append(component).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public long getAsLong() {
        return this.data;
    }

    @Override
    public int getAsInt() {
        return (int)(this.data & 0xFFFFFFFFFFFFFFFFL);
    }

    @Override
    public short getAsShort() {
        return (short)(this.data & 0xFFFFL);
    }

    @Override
    public byte getAsByte() {
        return (byte)(this.data & 0xFFL);
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
}

