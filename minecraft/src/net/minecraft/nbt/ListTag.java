package net.minecraft.nbt;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ListTag extends CollectionTag<Tag> {
	private static final int SELF_SIZE_IN_BITS = 296;
	public static final TagType<ListTag> TYPE = new TagType<ListTag>() {
		public ListTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
			nbtAccounter.accountBits(296L);
			if (i > 512) {
				throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
			} else {
				byte b = dataInput.readByte();
				int j = dataInput.readInt();
				if (b == 0 && j > 0) {
					throw new RuntimeException("Missing type on ListTag");
				} else {
					nbtAccounter.accountBits(32L * (long)j);
					TagType<?> tagType = TagTypes.getType(b);
					List<Tag> list = Lists.<Tag>newArrayListWithCapacity(j);

					for (int k = 0; k < j; k++) {
						list.add(tagType.load(dataInput, i + 1, nbtAccounter));
					}

					return new ListTag(list, b);
				}
			}
		}

		@Override
		public String getName() {
			return "LIST";
		}

		@Override
		public String getPrettyName() {
			return "TAG_List";
		}
	};
	private final List<Tag> list;
	private byte type;

	ListTag(List<Tag> list, byte b) {
		this.list = list;
		this.type = b;
	}

	public ListTag() {
		this(Lists.<Tag>newArrayList(), (byte)0);
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		if (this.list.isEmpty()) {
			this.type = 0;
		} else {
			this.type = ((Tag)this.list.get(0)).getId();
		}

		dataOutput.writeByte(this.type);
		dataOutput.writeInt(this.list.size());

		for (Tag tag : this.list) {
			tag.write(dataOutput);
		}
	}

	@Override
	public byte getId() {
		return 9;
	}

	@Override
	public TagType<ListTag> getType() {
		return TYPE;
	}

	@Override
	public String toString() {
		return this.getAsString();
	}

	private void updateTypeAfterRemove() {
		if (this.list.isEmpty()) {
			this.type = 0;
		}
	}

	@Override
	public Tag remove(int i) {
		Tag tag = (Tag)this.list.remove(i);
		this.updateTypeAfterRemove();
		return tag;
	}

	public boolean isEmpty() {
		return this.list.isEmpty();
	}

	public CompoundTag getCompound(int i) {
		if (i >= 0 && i < this.list.size()) {
			Tag tag = (Tag)this.list.get(i);
			if (tag.getId() == 10) {
				return (CompoundTag)tag;
			}
		}

		return new CompoundTag();
	}

	public ListTag getList(int i) {
		if (i >= 0 && i < this.list.size()) {
			Tag tag = (Tag)this.list.get(i);
			if (tag.getId() == 9) {
				return (ListTag)tag;
			}
		}

		return new ListTag();
	}

	public short getShort(int i) {
		if (i >= 0 && i < this.list.size()) {
			Tag tag = (Tag)this.list.get(i);
			if (tag.getId() == 2) {
				return ((ShortTag)tag).getAsShort();
			}
		}

		return 0;
	}

	public int getInt(int i) {
		if (i >= 0 && i < this.list.size()) {
			Tag tag = (Tag)this.list.get(i);
			if (tag.getId() == 3) {
				return ((IntTag)tag).getAsInt();
			}
		}

		return 0;
	}

	public int[] getIntArray(int i) {
		if (i >= 0 && i < this.list.size()) {
			Tag tag = (Tag)this.list.get(i);
			if (tag.getId() == 11) {
				return ((IntArrayTag)tag).getAsIntArray();
			}
		}

		return new int[0];
	}

	public long[] getLongArray(int i) {
		if (i >= 0 && i < this.list.size()) {
			Tag tag = (Tag)this.list.get(i);
			if (tag.getId() == 11) {
				return ((LongArrayTag)tag).getAsLongArray();
			}
		}

		return new long[0];
	}

	public double getDouble(int i) {
		if (i >= 0 && i < this.list.size()) {
			Tag tag = (Tag)this.list.get(i);
			if (tag.getId() == 6) {
				return ((DoubleTag)tag).getAsDouble();
			}
		}

		return 0.0;
	}

	public float getFloat(int i) {
		if (i >= 0 && i < this.list.size()) {
			Tag tag = (Tag)this.list.get(i);
			if (tag.getId() == 5) {
				return ((FloatTag)tag).getAsFloat();
			}
		}

		return 0.0F;
	}

	public String getString(int i) {
		if (i >= 0 && i < this.list.size()) {
			Tag tag = (Tag)this.list.get(i);
			return tag.getId() == 8 ? tag.getAsString() : tag.toString();
		} else {
			return "";
		}
	}

	public int size() {
		return this.list.size();
	}

	public Tag get(int i) {
		return (Tag)this.list.get(i);
	}

	@Override
	public Tag set(int i, Tag tag) {
		Tag tag2 = this.get(i);
		if (!this.setTag(i, tag)) {
			throw new UnsupportedOperationException(String.format("Trying to add tag of type %d to list of %d", tag.getId(), this.type));
		} else {
			return tag2;
		}
	}

	@Override
	public void add(int i, Tag tag) {
		if (!this.addTag(i, tag)) {
			throw new UnsupportedOperationException(String.format("Trying to add tag of type %d to list of %d", tag.getId(), this.type));
		}
	}

	@Override
	public boolean setTag(int i, Tag tag) {
		if (this.updateType(tag)) {
			this.list.set(i, tag);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean addTag(int i, Tag tag) {
		if (this.updateType(tag)) {
			this.list.add(i, tag);
			return true;
		} else {
			return false;
		}
	}

	private boolean updateType(Tag tag) {
		if (tag.getId() == 0) {
			return false;
		} else if (this.type == 0) {
			this.type = tag.getId();
			return true;
		} else {
			return this.type == tag.getId();
		}
	}

	public ListTag copy() {
		Iterable<Tag> iterable = (Iterable<Tag>)(TagTypes.getType(this.type).isValue() ? this.list : Iterables.transform(this.list, Tag::copy));
		List<Tag> list = Lists.<Tag>newArrayList(iterable);
		return new ListTag(list, this.type);
	}

	public boolean equals(Object object) {
		return this == object ? true : object instanceof ListTag && Objects.equals(this.list, ((ListTag)object).list);
	}

	public int hashCode() {
		return this.list.hashCode();
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitList(this);
	}

	@Override
	public byte getElementType() {
		return this.type;
	}

	public void clear() {
		this.list.clear();
		this.type = 0;
	}
}
