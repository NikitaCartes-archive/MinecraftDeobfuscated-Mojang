package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.apache.commons.lang3.ArrayUtils;

public class ByteArrayTag extends CollectionTag<ByteTag> {
	private byte[] data;

	ByteArrayTag() {
	}

	public ByteArrayTag(byte[] bs) {
		this.data = bs;
	}

	public ByteArrayTag(List<Byte> list) {
		this(toArray(list));
	}

	private static byte[] toArray(List<Byte> list) {
		byte[] bs = new byte[list.size()];

		for (int i = 0; i < list.size(); i++) {
			Byte byte_ = (Byte)list.get(i);
			bs[i] = byte_ == null ? 0 : byte_;
		}

		return bs;
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		dataOutput.writeInt(this.data.length);
		dataOutput.write(this.data);
	}

	@Override
	public void load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
		nbtAccounter.accountBits(192L);
		int j = dataInput.readInt();
		nbtAccounter.accountBits((long)(8 * j));
		this.data = new byte[j];
		dataInput.readFully(this.data);
	}

	@Override
	public byte getId() {
		return 7;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder("[B;");

		for (int i = 0; i < this.data.length; i++) {
			if (i != 0) {
				stringBuilder.append(',');
			}

			stringBuilder.append(this.data[i]).append('B');
		}

		return stringBuilder.append(']').toString();
	}

	@Override
	public Tag copy() {
		byte[] bs = new byte[this.data.length];
		System.arraycopy(this.data, 0, bs, 0, this.data.length);
		return new ByteArrayTag(bs);
	}

	public boolean equals(Object object) {
		return this == object ? true : object instanceof ByteArrayTag && Arrays.equals(this.data, ((ByteArrayTag)object).data);
	}

	public int hashCode() {
		return Arrays.hashCode(this.data);
	}

	@Override
	public Component getPrettyDisplay(String string, int i) {
		Component component = new TextComponent("B").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
		Component component2 = new TextComponent("[").append(component).append(";");

		for (int j = 0; j < this.data.length; j++) {
			Component component3 = new TextComponent(String.valueOf(this.data[j])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
			component2.append(" ").append(component3).append(component);
			if (j != this.data.length - 1) {
				component2.append(",");
			}
		}

		component2.append("]");
		return component2;
	}

	public byte[] getAsByteArray() {
		return this.data;
	}

	public int size() {
		return this.data.length;
	}

	public ByteTag get(int i) {
		return new ByteTag(this.data[i]);
	}

	public ByteTag set(int i, ByteTag byteTag) {
		byte b = this.data[i];
		this.data[i] = byteTag.getAsByte();
		return new ByteTag(b);
	}

	public void add(int i, ByteTag byteTag) {
		this.data = ArrayUtils.add(this.data, i, byteTag.getAsByte());
	}

	@Override
	public boolean setTag(int i, Tag tag) {
		if (tag instanceof NumericTag) {
			this.data[i] = ((NumericTag)tag).getAsByte();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean addTag(int i, Tag tag) {
		if (tag instanceof NumericTag) {
			this.data = ArrayUtils.add(this.data, i, ((NumericTag)tag).getAsByte());
			return true;
		} else {
			return false;
		}
	}

	public ByteTag remove(int i) {
		byte b = this.data[i];
		this.data = ArrayUtils.remove(this.data, i);
		return new ByteTag(b);
	}

	public void clear() {
		this.data = new byte[0];
	}
}
