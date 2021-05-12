package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LongTag extends NumericTag {
	private static final int SELF_SIZE_IN_BITS = 128;
	public static final TagType<LongTag> TYPE = new TagType<LongTag>() {
		public LongTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
			nbtAccounter.accountBits(128L);
			return LongTag.valueOf(dataInput.readLong());
		}

		@Override
		public String getName() {
			return "LONG";
		}

		@Override
		public String getPrettyName() {
			return "TAG_Long";
		}

		@Override
		public boolean isValue() {
			return true;
		}
	};
	private final long data;

	LongTag(long l) {
		this.data = l;
	}

	public static LongTag valueOf(long l) {
		return l >= -128L && l <= 1024L ? LongTag.Cache.cache[(int)l - -128] : new LongTag(l);
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		dataOutput.writeLong(this.data);
	}

	@Override
	public byte getId() {
		return 4;
	}

	@Override
	public TagType<LongTag> getType() {
		return TYPE;
	}

	public LongTag copy() {
		return this;
	}

	public boolean equals(Object object) {
		return this == object ? true : object instanceof LongTag && this.data == ((LongTag)object).data;
	}

	public int hashCode() {
		return (int)(this.data ^ this.data >>> 32);
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitLong(this);
	}

	@Override
	public long getAsLong() {
		return this.data;
	}

	@Override
	public int getAsInt() {
		return (int)(this.data & -1L);
	}

	@Override
	public short getAsShort() {
		return (short)((int)(this.data & 65535L));
	}

	@Override
	public byte getAsByte() {
		return (byte)((int)(this.data & 255L));
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

	static class Cache {
		private static final int HIGH = 1024;
		private static final int LOW = -128;
		static final LongTag[] cache = new LongTag[1153];

		private Cache() {
		}

		static {
			for (int i = 0; i < cache.length; i++) {
				cache[i] = new LongTag((long)(-128 + i));
			}
		}
	}
}
