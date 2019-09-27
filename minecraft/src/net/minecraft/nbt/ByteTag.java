package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class ByteTag extends NumericTag {
	public static final TagType<ByteTag> TYPE = new TagType<ByteTag>() {
		public ByteTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
			nbtAccounter.accountBits(72L);
			return ByteTag.valueOf(dataInput.readByte());
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

	private ByteTag(byte b) {
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
	public byte getId() {
		return 1;
	}

	@Override
	public TagType<ByteTag> getType() {
		return TYPE;
	}

	@Override
	public String toString() {
		return this.data + "b";
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

	static class Cache {
		private static final ByteTag[] cache = new ByteTag[256];

		static {
			for (int i = 0; i < cache.length; i++) {
				cache[i] = new ByteTag((byte)(i - 128));
			}
		}
	}
}
