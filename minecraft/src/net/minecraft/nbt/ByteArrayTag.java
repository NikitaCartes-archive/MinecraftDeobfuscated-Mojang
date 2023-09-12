package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

public class ByteArrayTag extends CollectionTag<ByteTag> {
	private static final int SELF_SIZE_IN_BYTES = 24;
	public static final TagType<ByteArrayTag> TYPE = new TagType.VariableSize<ByteArrayTag>() {
		public ByteArrayTag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
			return new ByteArrayTag(readAccounted(dataInput, nbtAccounter));
		}

		@Override
		public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
			return streamTagVisitor.visit(readAccounted(dataInput, nbtAccounter));
		}

		private static byte[] readAccounted(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
			nbtAccounter.accountBytes(24L);
			int i = dataInput.readInt();
			nbtAccounter.accountBytes(1L * (long)i);
			byte[] bs = new byte[i];
			dataInput.readFully(bs);
			return bs;
		}

		@Override
		public void skip(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
			dataInput.skipBytes(dataInput.readInt() * 1);
		}

		@Override
		public String getName() {
			return "BYTE[]";
		}

		@Override
		public String getPrettyName() {
			return "TAG_Byte_Array";
		}
	};
	private byte[] data;

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
	public int sizeInBytes() {
		return 24 + 1 * this.data.length;
	}

	@Override
	public byte getId() {
		return 7;
	}

	@Override
	public TagType<ByteArrayTag> getType() {
		return TYPE;
	}

	@Override
	public String toString() {
		return this.getAsString();
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
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitByteArray(this);
	}

	public byte[] getAsByteArray() {
		return this.data;
	}

	public int size() {
		return this.data.length;
	}

	public ByteTag get(int i) {
		return ByteTag.valueOf(this.data[i]);
	}

	public ByteTag set(int i, ByteTag byteTag) {
		byte b = this.data[i];
		this.data[i] = byteTag.getAsByte();
		return ByteTag.valueOf(b);
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
		return ByteTag.valueOf(b);
	}

	@Override
	public byte getElementType() {
		return 1;
	}

	public void clear() {
		this.data = new byte[0];
	}

	@Override
	public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
		return streamTagVisitor.visit(this.data);
	}
}
