package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ByteTag extends NumericTag {
	private static final int SELF_SIZE_IN_BYTES = 9;
	public static final TagType<ByteTag> TYPE = new TagType.StaticSize<ByteTag>() {
		public ByteTag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
			return ByteTag.valueOf(readAccounted(dataInput, nbtAccounter));
		}

		@Override
		public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
			return streamTagVisitor.visit(readAccounted(dataInput, nbtAccounter));
		}

		private static byte readAccounted(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
			nbtAccounter.accountBytes(9L);
			return dataInput.readByte();
		}

		@Override
		public int size() {
			return 1;
		}

		@Override
		public String getName() {
			return "BYTE";
		}

		@Override
		public String getPrettyName() {
			return "TAG_Byte";
		}

		@Override
		public boolean isValue() {
			return true;
		}
	};
	public static final ByteTag ZERO = valueOf((byte)0);
	public static final ByteTag ONE = valueOf((byte)1);
	private final byte data;

	ByteTag(byte b) {
		this.data = b;
	}

	public static ByteTag valueOf(byte b) {
		return ByteTag.Cache.cache[128 + b];
	}

	public static ByteTag valueOf(boolean bl) {
		return bl ? ONE : ZERO;
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		dataOutput.writeByte(this.data);
	}

	@Override
	public int sizeInBytes() {
		return 9;
	}

	@Override
	public byte getId() {
		return 1;
	}

	@Override
	public TagType<ByteTag> getType() {
		return TYPE;
	}

	public ByteTag copy() {
		return this;
	}

	public boolean equals(Object object) {
		return this == object ? true : object instanceof ByteTag && this.data == ((ByteTag)object).data;
	}

	public int hashCode() {
		return this.data;
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitByte(this);
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

	@Override
	public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
		return streamTagVisitor.visit(this.data);
	}

	static class Cache {
		static final ByteTag[] cache = new ByteTag[256];

		private Cache() {
		}

		static {
			for (int i = 0; i < cache.length; i++) {
				cache[i] = new ByteTag((byte)(i - 128));
			}
		}
	}
}
