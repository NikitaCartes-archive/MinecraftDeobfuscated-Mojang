package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class IntTag extends NumericTag {
	private static final int SELF_SIZE_IN_BYTES = 12;
	public static final TagType<IntTag> TYPE = new TagType.StaticSize<IntTag>() {
		public IntTag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
			return IntTag.valueOf(readAccounted(dataInput, nbtAccounter));
		}

		@Override
		public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
			return streamTagVisitor.visit(readAccounted(dataInput, nbtAccounter));
		}

		private static int readAccounted(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
			nbtAccounter.accountBytes(12L);
			return dataInput.readInt();
		}

		@Override
		public int size() {
			return 4;
		}

		@Override
		public String getName() {
			return "INT";
		}

		@Override
		public String getPrettyName() {
			return "TAG_Int";
		}

		@Override
		public boolean isValue() {
			return true;
		}
	};
	private final int data;

	IntTag(int i) {
		this.data = i;
	}

	public static IntTag valueOf(int i) {
		return i >= -128 && i <= 1024 ? IntTag.Cache.cache[i - -128] : new IntTag(i);
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		dataOutput.writeInt(this.data);
	}

	@Override
	public int sizeInBytes() {
		return 12;
	}

	@Override
	public byte getId() {
		return 3;
	}

	@Override
	public TagType<IntTag> getType() {
		return TYPE;
	}

	public IntTag copy() {
		return this;
	}

	public boolean equals(Object object) {
		return this == object ? true : object instanceof IntTag && this.data == ((IntTag)object).data;
	}

	public int hashCode() {
		return this.data;
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitInt(this);
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

	@Override
	public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
		return streamTagVisitor.visit(this.data);
	}

	static class Cache {
		private static final int HIGH = 1024;
		private static final int LOW = -128;
		static final IntTag[] cache = new IntTag[1153];

		private Cache() {
		}

		static {
			for (int i = 0; i < cache.length; i++) {
				cache[i] = new IntTag(-128 + i);
			}
		}
	}
}
