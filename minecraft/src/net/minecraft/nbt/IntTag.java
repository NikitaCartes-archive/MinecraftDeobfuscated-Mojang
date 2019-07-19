package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class IntTag extends NumericTag {
	private int data;

	IntTag() {
	}

	public IntTag(int i) {
		this.data = i;
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		dataOutput.writeInt(this.data);
	}

	@Override
	public void load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
		nbtAccounter.accountBits(96L);
		this.data = dataInput.readInt();
	}

	@Override
	public byte getId() {
		return 3;
	}

	@Override
	public String toString() {
		return String.valueOf(this.data);
	}

	public IntTag copy() {
		return new IntTag(this.data);
	}

	public boolean equals(Object object) {
		return this == object ? true : object instanceof IntTag && this.data == ((IntTag)object).data;
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
		return (long)this.data;
	}

	@Override
	public int getAsInt() {
		return this.data;
	}

	@Override
	public short getAsShort() {
		return (short)(this.data & 65535);
	}

	@Override
	public byte getAsByte() {
		return (byte)(this.data & 0xFF);
	}

	@Override
	public double getAsDouble() {
		return (double)this.data;
	}

	@Override
	public float getAsFloat() {
		return (float)this.data;
	}

	@Override
	public Number getAsNumber() {
		return this.data;
	}
}
