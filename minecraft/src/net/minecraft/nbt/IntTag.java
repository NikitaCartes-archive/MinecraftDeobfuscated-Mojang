package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class IntTag extends NumericTag {
	public static final TagType<IntTag> TYPE = new TagType<IntTag>() {
		public IntTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
			nbtAccounter.accountBits(96L);
			return IntTag.valueOf(dataInput.readInt());
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

	private IntTag(int i) {
		this.data = i;
	}

	public static IntTag valueOf(int i) {
		return i >= -128 && i <= 1024 ? IntTag.Cache.cache[i + 128] : new IntTag(i);
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		dataOutput.writeInt(this.data);
	}

	@Override
	public byte getId() {
		return 3;
	}

	@Override
	public TagType<IntTag> getType() {
		return TYPE;
	}

	@Override
	public String toString() {
		return String.valueOf(this.data);
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
	public Component getPrettyDisplay(String string, int i) {
		return new TextComponent(String.valueOf(this.data)).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
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

	static class Cache {
		static final IntTag[] cache = new IntTag[1153];

		static {
			for (int i = 0; i < cache.length; i++) {
				cache[i] = new IntTag(-128 + i);
			}
		}
	}
}
