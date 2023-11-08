package net.minecraft.util;

import java.io.DataOutput;
import java.io.IOException;

public class DelegateDataOutput implements DataOutput {
	private final DataOutput parent;

	public DelegateDataOutput(DataOutput dataOutput) {
		this.parent = dataOutput;
	}

	public void write(int i) throws IOException {
		this.parent.write(i);
	}

	public void write(byte[] bs) throws IOException {
		this.parent.write(bs);
	}

	public void write(byte[] bs, int i, int j) throws IOException {
		this.parent.write(bs, i, j);
	}

	public void writeBoolean(boolean bl) throws IOException {
		this.parent.writeBoolean(bl);
	}

	public void writeByte(int i) throws IOException {
		this.parent.writeByte(i);
	}

	public void writeShort(int i) throws IOException {
		this.parent.writeShort(i);
	}

	public void writeChar(int i) throws IOException {
		this.parent.writeChar(i);
	}

	public void writeInt(int i) throws IOException {
		this.parent.writeInt(i);
	}

	public void writeLong(long l) throws IOException {
		this.parent.writeLong(l);
	}

	public void writeFloat(float f) throws IOException {
		this.parent.writeFloat(f);
	}

	public void writeDouble(double d) throws IOException {
		this.parent.writeDouble(d);
	}

	public void writeBytes(String string) throws IOException {
		this.parent.writeBytes(string);
	}

	public void writeChars(String string) throws IOException {
		this.parent.writeChars(string);
	}

	public void writeUTF(String string) throws IOException {
		this.parent.writeUTF(string);
	}
}
