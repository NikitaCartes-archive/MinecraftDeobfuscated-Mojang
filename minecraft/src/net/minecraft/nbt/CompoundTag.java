package net.minecraft.nbt;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;

public class CompoundTag implements Tag {
	public static final Codec<CompoundTag> CODEC = Codec.PASSTHROUGH.comapFlatMap(dynamic -> {
		Tag tag = dynamic.convert(NbtOps.INSTANCE).getValue();
		return tag instanceof CompoundTag ? DataResult.success((CompoundTag)tag) : DataResult.error("Not a compound tag: " + tag);
	}, compoundTag -> new Dynamic<>(NbtOps.INSTANCE, compoundTag));
	private static final int SELF_SIZE_IN_BITS = 384;
	private static final int MAP_ENTRY_SIZE_IN_BITS = 256;
	public static final TagType<CompoundTag> TYPE = new TagType.VariableSize<CompoundTag>() {
		public CompoundTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
			nbtAccounter.accountBits(384L);
			if (i > 512) {
				throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
			} else {
				Map<String, Tag> map = Maps.<String, Tag>newHashMap();

				byte b;
				while ((b = CompoundTag.readNamedTagType(dataInput, nbtAccounter)) != 0) {
					String string = CompoundTag.readNamedTagName(dataInput, nbtAccounter);
					nbtAccounter.accountBits((long)(224 + 16 * string.length()));
					Tag tag = CompoundTag.readNamedTagData(TagTypes.getType(b), string, dataInput, i + 1, nbtAccounter);
					if (map.put(string, tag) != null) {
						nbtAccounter.accountBits(288L);
					}
				}

				return new CompoundTag(map);
			}
		}

		@Override
		public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor) throws IOException {
			byte b;
			label33:
			while ((b = dataInput.readByte()) != 0) {
				TagType<?> tagType = TagTypes.getType(b);
				switch (streamTagVisitor.visitEntry(tagType)) {
					case HALT:
						return StreamTagVisitor.ValueResult.HALT;
					case BREAK:
						StringTag.skipString(dataInput);
						tagType.skip(dataInput);
						break label33;
					case SKIP:
						StringTag.skipString(dataInput);
						tagType.skip(dataInput);
						break;
					default:
						String string = dataInput.readUTF();
						switch (streamTagVisitor.visitEntry(tagType, string)) {
							case HALT:
								return StreamTagVisitor.ValueResult.HALT;
							case BREAK:
								tagType.skip(dataInput);
								break label33;
							case SKIP:
								tagType.skip(dataInput);
								break;
							default:
								switch (tagType.parse(dataInput, streamTagVisitor)) {
									case HALT:
										return StreamTagVisitor.ValueResult.HALT;
									case BREAK:
								}
						}
				}
			}

			if (b != 0) {
				while ((b = dataInput.readByte()) != 0) {
					StringTag.skipString(dataInput);
					TagTypes.getType(b).skip(dataInput);
				}
			}

			return streamTagVisitor.visitContainerEnd();
		}

		@Override
		public void skip(DataInput dataInput) throws IOException {
			byte b;
			while ((b = dataInput.readByte()) != 0) {
				StringTag.skipString(dataInput);
				TagTypes.getType(b).skip(dataInput);
			}
		}

		@Override
		public String getName() {
			return "COMPOUND";
		}

		@Override
		public String getPrettyName() {
			return "TAG_Compound";
		}
	};
	private final Map<String, Tag> tags;

	protected CompoundTag(Map<String, Tag> map) {
		this.tags = map;
	}

	public CompoundTag() {
		this(Maps.<String, Tag>newHashMap());
	}

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		for (String string : this.tags.keySet()) {
			Tag tag = (Tag)this.tags.get(string);
			writeNamedTag(string, tag, dataOutput);
		}

		dataOutput.writeByte(0);
	}

	public Set<String> getAllKeys() {
		return this.tags.keySet();
	}

	@Override
	public byte getId() {
		return 10;
	}

	@Override
	public TagType<CompoundTag> getType() {
		return TYPE;
	}

	public int size() {
		return this.tags.size();
	}

	@Nullable
	public Tag put(String string, Tag tag) {
		return (Tag)this.tags.put(string, tag);
	}

	public void putByte(String string, byte b) {
		this.tags.put(string, ByteTag.valueOf(b));
	}

	public void putShort(String string, short s) {
		this.tags.put(string, ShortTag.valueOf(s));
	}

	public void putInt(String string, int i) {
		this.tags.put(string, IntTag.valueOf(i));
	}

	public void putLong(String string, long l) {
		this.tags.put(string, LongTag.valueOf(l));
	}

	public void putUUID(String string, UUID uUID) {
		this.tags.put(string, NbtUtils.createUUID(uUID));
	}

	public UUID getUUID(String string) {
		return NbtUtils.loadUUID(this.get(string));
	}

	public boolean hasUUID(String string) {
		Tag tag = this.get(string);
		return tag != null && tag.getType() == IntArrayTag.TYPE && ((IntArrayTag)tag).getAsIntArray().length == 4;
	}

	public void putFloat(String string, float f) {
		this.tags.put(string, FloatTag.valueOf(f));
	}

	public void putDouble(String string, double d) {
		this.tags.put(string, DoubleTag.valueOf(d));
	}

	public void putString(String string, String string2) {
		this.tags.put(string, StringTag.valueOf(string2));
	}

	public void putByteArray(String string, byte[] bs) {
		this.tags.put(string, new ByteArrayTag(bs));
	}

	public void putByteArray(String string, List<Byte> list) {
		this.tags.put(string, new ByteArrayTag(list));
	}

	public void putIntArray(String string, int[] is) {
		this.tags.put(string, new IntArrayTag(is));
	}

	public void putIntArray(String string, List<Integer> list) {
		this.tags.put(string, new IntArrayTag(list));
	}

	public void putLongArray(String string, long[] ls) {
		this.tags.put(string, new LongArrayTag(ls));
	}

	public void putLongArray(String string, List<Long> list) {
		this.tags.put(string, new LongArrayTag(list));
	}

	public void putBoolean(String string, boolean bl) {
		this.tags.put(string, ByteTag.valueOf(bl));
	}

	@Nullable
	public Tag get(String string) {
		return (Tag)this.tags.get(string);
	}

	public byte getTagType(String string) {
		Tag tag = (Tag)this.tags.get(string);
		return tag == null ? 0 : tag.getId();
	}

	public boolean contains(String string) {
		return this.tags.containsKey(string);
	}

	public boolean contains(String string, int i) {
		int j = this.getTagType(string);
		if (j == i) {
			return true;
		} else {
			return i != 99 ? false : j == 1 || j == 2 || j == 3 || j == 4 || j == 5 || j == 6;
		}
	}

	public byte getByte(String string) {
		try {
			if (this.contains(string, 99)) {
				return ((NumericTag)this.tags.get(string)).getAsByte();
			}
		} catch (ClassCastException var3) {
		}

		return 0;
	}

	public short getShort(String string) {
		try {
			if (this.contains(string, 99)) {
				return ((NumericTag)this.tags.get(string)).getAsShort();
			}
		} catch (ClassCastException var3) {
		}

		return 0;
	}

	public int getInt(String string) {
		try {
			if (this.contains(string, 99)) {
				return ((NumericTag)this.tags.get(string)).getAsInt();
			}
		} catch (ClassCastException var3) {
		}

		return 0;
	}

	public long getLong(String string) {
		try {
			if (this.contains(string, 99)) {
				return ((NumericTag)this.tags.get(string)).getAsLong();
			}
		} catch (ClassCastException var3) {
		}

		return 0L;
	}

	public float getFloat(String string) {
		try {
			if (this.contains(string, 99)) {
				return ((NumericTag)this.tags.get(string)).getAsFloat();
			}
		} catch (ClassCastException var3) {
		}

		return 0.0F;
	}

	public double getDouble(String string) {
		try {
			if (this.contains(string, 99)) {
				return ((NumericTag)this.tags.get(string)).getAsDouble();
			}
		} catch (ClassCastException var3) {
		}

		return 0.0;
	}

	public String getString(String string) {
		try {
			if (this.contains(string, 8)) {
				return ((Tag)this.tags.get(string)).getAsString();
			}
		} catch (ClassCastException var3) {
		}

		return "";
	}

	public byte[] getByteArray(String string) {
		try {
			if (this.contains(string, 7)) {
				return ((ByteArrayTag)this.tags.get(string)).getAsByteArray();
			}
		} catch (ClassCastException var3) {
			throw new ReportedException(this.createReport(string, ByteArrayTag.TYPE, var3));
		}

		return new byte[0];
	}

	public int[] getIntArray(String string) {
		try {
			if (this.contains(string, 11)) {
				return ((IntArrayTag)this.tags.get(string)).getAsIntArray();
			}
		} catch (ClassCastException var3) {
			throw new ReportedException(this.createReport(string, IntArrayTag.TYPE, var3));
		}

		return new int[0];
	}

	public long[] getLongArray(String string) {
		try {
			if (this.contains(string, 12)) {
				return ((LongArrayTag)this.tags.get(string)).getAsLongArray();
			}
		} catch (ClassCastException var3) {
			throw new ReportedException(this.createReport(string, LongArrayTag.TYPE, var3));
		}

		return new long[0];
	}

	public CompoundTag getCompound(String string) {
		try {
			if (this.contains(string, 10)) {
				return (CompoundTag)this.tags.get(string);
			}
		} catch (ClassCastException var3) {
			throw new ReportedException(this.createReport(string, TYPE, var3));
		}

		return new CompoundTag();
	}

	public ListTag getList(String string, int i) {
		try {
			if (this.getTagType(string) == 9) {
				ListTag listTag = (ListTag)this.tags.get(string);
				if (!listTag.isEmpty() && listTag.getElementType() != i) {
					return new ListTag();
				}

				return listTag;
			}
		} catch (ClassCastException var4) {
			throw new ReportedException(this.createReport(string, ListTag.TYPE, var4));
		}

		return new ListTag();
	}

	public boolean getBoolean(String string) {
		return this.getByte(string) != 0;
	}

	public void remove(String string) {
		this.tags.remove(string);
	}

	@Override
	public String toString() {
		return this.getAsString();
	}

	public boolean isEmpty() {
		return this.tags.isEmpty();
	}

	private CrashReport createReport(String string, TagType<?> tagType, ClassCastException classCastException) {
		CrashReport crashReport = CrashReport.forThrowable(classCastException, "Reading NBT data");
		CrashReportCategory crashReportCategory = crashReport.addCategory("Corrupt NBT tag", 1);
		crashReportCategory.setDetail("Tag type found", (CrashReportDetail<String>)(() -> ((Tag)this.tags.get(string)).getType().getName()));
		crashReportCategory.setDetail("Tag type expected", tagType::getName);
		crashReportCategory.setDetail("Tag name", string);
		return crashReport;
	}

	public CompoundTag copy() {
		Map<String, Tag> map = Maps.<String, Tag>newHashMap(Maps.transformValues(this.tags, Tag::copy));
		return new CompoundTag(map);
	}

	public boolean equals(Object object) {
		return this == object ? true : object instanceof CompoundTag && Objects.equals(this.tags, ((CompoundTag)object).tags);
	}

	public int hashCode() {
		return this.tags.hashCode();
	}

	private static void writeNamedTag(String string, Tag tag, DataOutput dataOutput) throws IOException {
		dataOutput.writeByte(tag.getId());
		if (tag.getId() != 0) {
			dataOutput.writeUTF(string);
			tag.write(dataOutput);
		}
	}

	static byte readNamedTagType(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
		return dataInput.readByte();
	}

	static String readNamedTagName(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
		return dataInput.readUTF();
	}

	static Tag readNamedTagData(TagType<?> tagType, String string, DataInput dataInput, int i, NbtAccounter nbtAccounter) {
		try {
			return tagType.load(dataInput, i, nbtAccounter);
		} catch (IOException var8) {
			CrashReport crashReport = CrashReport.forThrowable(var8, "Loading NBT data");
			CrashReportCategory crashReportCategory = crashReport.addCategory("NBT Tag");
			crashReportCategory.setDetail("Tag name", string);
			crashReportCategory.setDetail("Tag type", tagType.getName());
			throw new ReportedException(crashReport);
		}
	}

	public CompoundTag merge(CompoundTag compoundTag) {
		for (String string : compoundTag.tags.keySet()) {
			Tag tag = (Tag)compoundTag.tags.get(string);
			if (tag.getId() == 10) {
				if (this.contains(string, 10)) {
					CompoundTag compoundTag2 = this.getCompound(string);
					compoundTag2.merge((CompoundTag)tag);
				} else {
					this.put(string, tag.copy());
				}
			} else {
				this.put(string, tag.copy());
			}
		}

		return this;
	}

	@Override
	public void accept(TagVisitor tagVisitor) {
		tagVisitor.visitCompound(this);
	}

	protected Map<String, Tag> entries() {
		return Collections.unmodifiableMap(this.tags);
	}

	@Override
	public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
		for (Entry<String, Tag> entry : this.tags.entrySet()) {
			Tag tag = (Tag)entry.getValue();
			TagType<?> tagType = tag.getType();
			StreamTagVisitor.EntryResult entryResult = streamTagVisitor.visitEntry(tagType);
			switch (entryResult) {
				case HALT:
					return StreamTagVisitor.ValueResult.HALT;
				case BREAK:
					return streamTagVisitor.visitContainerEnd();
				case SKIP:
					break;
				default:
					entryResult = streamTagVisitor.visitEntry(tagType, (String)entry.getKey());
					switch (entryResult) {
						case HALT:
							return StreamTagVisitor.ValueResult.HALT;
						case BREAK:
							return streamTagVisitor.visitContainerEnd();
						case SKIP:
							break;
						default:
							StreamTagVisitor.ValueResult valueResult = tag.accept(streamTagVisitor);
							switch (valueResult) {
								case HALT:
									return StreamTagVisitor.ValueResult.HALT;
								case BREAK:
									return streamTagVisitor.visitContainerEnd();
							}
					}
			}
		}

		return streamTagVisitor.visitContainerEnd();
	}
}
