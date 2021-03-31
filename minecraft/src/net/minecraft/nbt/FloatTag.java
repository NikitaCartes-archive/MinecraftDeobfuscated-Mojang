package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.Mth;

public class FloatTag extends NumericTag {
	private static final int SELF_SIZE_IN_BITS = 96;
	public static final FloatTag ZERO = new FloatTag(0.0F);
	public static final TagType<FloatTag> TYPE = new TagType<FloatTag>() {
		public FloatTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
			nbtAccounter.accountBits(96L);
			return FloatTag.valueOf(dataInput.readFloat());
		}

		@Override
		public String getName() {
			return "FLOAT";
		}

		@Override
		public String getPrettyName() {
			return "TAG_Float";
		}

		@Override
		public boolean isValue() {
			return true;
		}
	};
	private final float data;

	private FloatTag(float f) {
		this.data = f;
	}

	public static FloatTag valueOf(float f) {
		return f == 0.0F ? ZERO : new FloatTag(f);
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		dataOutput.writeFloat(this.data);
	}

	@Override
	public byte getId() {
		return 5;
	}

	@Override
	public TagType<FloatTag> getType() {
		return TYPE;
	}

	public FloatTag copy() {
		return this;
	}

	public boolean equals(Object object) {
		return this == object ? true : object instanceof FloatTag && this.data == ((FloatTag)object).data;
	}

	public int hashCode() {
		return Float.floatToIntBits(this.data);
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitFloat(this);
	}

	@Override
	public long getAsLong() {
		return (long)this.data;
	}

	@Override
	public int getAsInt() {
		return Mth.floor(this.data);
	}

	@Override
	public short getAsShort() {
		return (short)(Mth.floor(this.data) & 65535);
	}

	@Override
	public byte getAsByte() {
		return (byte)(Mth.floor(this.data) & 0xFF);
	}

	@Override
	public double getAsDouble() {
		return (double)this.data;
	}

	@Override
	public float getAsFloat() {
		return this.data;
	}

	@Override
	public Number getAsNumber() {
		return this.data;
	}
}
