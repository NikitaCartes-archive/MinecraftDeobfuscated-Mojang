package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

public class IntArrayTag extends CollectionTag<IntTag> {
	private static final int SELF_SIZE_IN_BITS = 192;
	public static final TagType<IntArrayTag> TYPE = new TagType<IntArrayTag>() {
		public IntArrayTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
			nbtAccounter.accountBits(192L);
			int j = dataInput.readInt();
			nbtAccounter.accountBits(32L * (long)j);
			int[] is = new int[j];

			for (int k = 0; k < j; k++) {
				is[k] = dataInput.readInt();
			}

			return new IntArrayTag(is);
		}

		@Override
		public String getName() {
			return "INT[]";
		}

		@Override
		public String getPrettyName() {
			return "TAG_Int_Array";
		}
	};
	private int[] data;

	public IntArrayTag(int[] is) {
		this.data = is;
	}

	public IntArrayTag(List<Integer> list) {
		this(toArray(list));
	}

	private static int[] toArray(List<Integer> list) {
		int[] is = new int[list.size()];

		for (int i = 0; i < list.size(); i++) {
			Integer integer = (Integer)list.get(i);
			is[i] = integer == null ? 0 : integer;
		}

		return is;
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		dataOutput.writeInt(this.data.length);

		for (int i : this.data) {
			dataOutput.writeInt(i);
		}
	}

	@Override
	public byte getId() {
		return 11;
	}

	@Override
	public TagType<IntArrayTag> getType() {
		return TYPE;
	}

	@Override
	public String toString() {
		return this.getAsString();
	}

	public IntArrayTag copy() {
		int[] is = new int[this.data.length];
		System.arraycopy(this.data, 0, is, 0, this.data.length);
		return new IntArrayTag(is);
	}

	public boolean equals(Object object) {
		return this == object ? true : object instanceof IntArrayTag && Arrays.equals(this.data, ((IntArrayTag)object).data);
	}

	public int hashCode() {
		return Arrays.hashCode(this.data);
	}

	public int[] getAsIntArray() {
		return this.data;
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitIntArray(this);
	}

	public int size() {
		return this.data.length;
	}

	public IntTag get(int i) {
		return IntTag.valueOf(this.data[i]);
	}

	public IntTag set(int i, IntTag intTag) {
		int j = this.data[i];
		this.data[i] = intTag.getAsInt();
		return IntTag.valueOf(j);
	}

	public void add(int i, IntTag intTag) {
		this.data = ArrayUtils.add(this.data, i, intTag.getAsInt());
	}

	@Override
	public boolean setTag(int i, Tag tag) {
		if (tag instanceof NumericTag) {
			this.data[i] = ((NumericTag)tag).getAsInt();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean addTag(int i, Tag tag) {
		if (tag instanceof NumericTag) {
			this.data = ArrayUtils.add(this.data, i, ((NumericTag)tag).getAsInt());
			return true;
		} else {
			return false;
		}
	}

	public IntTag remove(int i) {
		int j = this.data[i];
		this.data = ArrayUtils.remove(this.data, i);
		return IntTag.valueOf(j);
	}

	@Override
	public byte getElementType() {
		return 3;
	}

	public void clear() {
		this.data = new int[0];
	}
}
