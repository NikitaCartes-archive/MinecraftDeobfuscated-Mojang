package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ShortTag extends NumericTag {
	private static final int SELF_SIZE_IN_BITS = 80;
	public static final TagType<ShortTag> TYPE = new TagType<ShortTag>() {
		public ShortTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
			nbtAccounter.accountBits(80L);
			return ShortTag.valueOf(dataInput.readShort());
		}

		@Override
		public String getName() {
			return "SHORT";
		}

		@Override
		public String getPrettyName() {
			return "TAG_Short";
		}

		@Override
		public boolean isValue() {
			return true;
		}
	};
	private final short data;

	ShortTag(short s) {
		this.data = s;
	}

	public static ShortTag valueOf(short s) {
		return s >= -128 && s <= 1024 ? ShortTag.Cache.cache[s - -128] : new ShortTag(s);
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		dataOutput.writeShort(this.data);
	}

	@Override
	public byte getId() {
		return 2;
	}

	@Override
	public TagType<ShortTag> getType() {
		return TYPE;
	}

	public ShortTag copy() {
		return this;
	}

	public boolean equals(Object object) {
		return this == object ? true : object instanceof ShortTag && this.data == ((ShortTag)object).data;
	}

	public int hashCode() {
		return this.data;
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitShort(this);
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

	static class Cache {
		private static final int HIGH = 1024;
		private static final int LOW = -128;
		static final ShortTag[] cache = new ShortTag[1153];

		private Cache() {
		}

		static {
			for (int i = 0; i < cache.length; i++) {
				cache[i] = new ShortTag((short)(-128 + i));
			}
		}
	}
}
