package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class ShortTag extends NumericTag {
	private short data;

	public ShortTag() {
	}

	public ShortTag(short s) {
		this.data = s;
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		dataOutput.writeShort(this.data);
	}

	@Override
	public void load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
		nbtAccounter.accountBits(80L);
		this.data = dataInput.readShort();
	}

	@Override
	public byte getId() {
		return 2;
	}

	@Override
	public String toString() {
		return this.data + "s";
	}

	public ShortTag copy() {
		return new ShortTag(this.data);
	}

	public boolean equals(Object object) {
		return this == object ? true : object instanceof ShortTag && this.data == ((ShortTag)object).data;
	}

	public int hashCode() {
		return this.data;
	}

	@Override
	public Component getPrettyDisplay(String string, int i) {
		Component component = new TextComponent("s").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
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
		return this.data;
	}

	@Override
	public byte getAsByte() {
		return (byte)(this.data & 255);
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
