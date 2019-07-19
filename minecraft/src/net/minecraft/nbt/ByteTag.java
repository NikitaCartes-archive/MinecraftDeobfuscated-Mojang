package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class ByteTag extends NumericTag {
	private byte data;

	ByteTag() {
	}

	public ByteTag(byte b) {
		this.data = b;
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		dataOutput.writeByte(this.data);
	}

	@Override
	public void load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
		nbtAccounter.accountBits(72L);
		this.data = dataInput.readByte();
	}

	@Override
	public byte getId() {
		return 1;
	}

	@Override
	public String toString() {
		return this.data + "b";
	}

	public ByteTag copy() {
		return new ByteTag(this.data);
	}

	public boolean equals(Object object) {
		return this == object ? true : object instanceof ByteTag && this.data == ((ByteTag)object).data;
	}

	public int hashCode() {
		return this.data;
	}

	@Override
	public Component getPrettyDisplay(String string, int i) {
		Component component = new TextComponent("b").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
		return new TextComponent(String.valueOf(this.data)).append(component).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
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
		return (short)this.data;
	}

	@Override
	public byte getAsByte() {
		return this.data;
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
