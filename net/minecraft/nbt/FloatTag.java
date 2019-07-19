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
import net.minecraft.util.Mth;

public class FloatTag
extends NumericTag {
    private float data;

    FloatTag() {
    }

    public FloatTag(float f) {
        this.data = f;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeFloat(this.data);
    }

    @Override
    public void load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
        nbtAccounter.accountBits(96L);
        this.data = dataInput.readFloat();
    }

    @Override
    public byte getId() {
        return 5;
    }

    @Override
    public String toString() {
        return this.data + "f";
    }

    @Override
    public FloatTag copy() {
        return new FloatTag(this.data);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof FloatTag && this.data == ((FloatTag)object).data;
    }

    public int hashCode() {
        return Float.floatToIntBits(this.data);
    }

    @Override
    public Component getPrettyDisplay(String string, int i) {
        Component component = new TextComponent("f").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
        return new TextComponent(String.valueOf(this.data)).append(component).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
    }

    @Override
    public long getAsLong() {
        return (long)this.data;
    }

    @Override
    public int getAsInt() {
        return Mth.floor(this.data);
    }

    @Override
    public short getAsShort() {
        return (short)(Mth.floor(this.data) & 0xFFFF);
    }

    @Override
    public byte getAsByte() {
        return (byte)(Mth.floor(this.data) & 0xFF);
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
        return Float.valueOf(this.data);
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }
}

