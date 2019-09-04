package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CompoundTag implements Tag {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
	private final Map<String, Tag> tags = Maps.<String, Tag>newHashMap();

	@Override
	public void write(DataOutput dataOutput) throws IOException {
		for (String string : this.tags.keySet()) {
			Tag tag = (Tag)this.tags.get(string);
			writeNamedTag(string, tag, dataOutput);
		}

		dataOutput.writeByte(0);
	}

	@Override
	public void load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
		nbtAccounter.accountBits(384L);
		if (i > 512) {
			throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
		} else {
			this.tags.clear();

			byte b;
			while ((b = readNamedTagType(dataInput, nbtAccounter)) != 0) {
				String string = readNamedTagName(dataInput, nbtAccounter);
				nbtAccounter.accountBits((long)(224 + 16 * string.length()));
				Tag tag = readNamedTagData(b, string, dataInput, i + 1, nbtAccounter);
				if (this.tags.put(string, tag) != null) {
					nbtAccounter.accountBits(288L);
				}
			}
		}
	}

	public Set<String> getAllKeys() {
		return this.tags.keySet();
	}

	@Override
	public byte getId() {
		return 10;
	}

	public int size() {
		return this.tags.size();
	}

	@Nullable
	public Tag put(String string, Tag tag) {
		return (Tag)this.tags.put(string, tag);
	}

	public void putByte(String string, byte b) {
		this.tags.put(string, new ByteTag(b));
	}

	public void putShort(String string, short s) {
		this.tags.put(string, new ShortTag(s));
	}

	public void putInt(String string, int i) {
		this.tags.put(string, new IntTag(i));
	}

	public void putLong(String string, long l) {
		this.tags.put(string, new LongTag(l));
	}

	public void putUUID(String string, UUID uUID) {
		this.putLong(string + "Most", uUID.getMostSignificantBits());
		this.putLong(string + "Least", uUID.getLeastSignificantBits());
	}

	public UUID getUUID(String string) {
		return new UUID(this.getLong(string + "Most"), this.getLong(string + "Least"));
	}

	public boolean hasUUID(String string) {
		return this.contains(string + "Most", 99) && this.contains(string + "Least", 99);
	}

	public void removeUUID(String string) {
		this.remove(string + "Most");
		this.remove(string + "Least");
	}

	public void putFloat(String string, float f) {
		this.tags.put(string, new FloatTag(f));
	}

	public void putDouble(String string, double d) {
		this.tags.put(string, new DoubleTag(d));
	}

	public void putString(String string, String string2) {
		this.tags.put(string, new StringTag(string2));
	}

	public void putByteArray(String string, byte[] bs) {
		this.tags.put(string, new ByteArrayTag(bs));
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
		this.putByte(string, (byte)(bl ? 1 : 0));
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
			throw new ReportedException(this.createReport(string, 7, var3));
		}

		return new byte[0];
	}

	public int[] getIntArray(String string) {
		try {
			if (this.contains(string, 11)) {
				return ((IntArrayTag)this.tags.get(string)).getAsIntArray();
			}
		} catch (ClassCastException var3) {
			throw new ReportedException(this.createReport(string, 11, var3));
		}

		return new int[0];
	}

	public long[] getLongArray(String string) {
		try {
			if (this.contains(string, 12)) {
				return ((LongArrayTag)this.tags.get(string)).getAsLongArray();
			}
		} catch (ClassCastException var3) {
			throw new ReportedException(this.createReport(string, 12, var3));
		}

		return new long[0];
	}

	public CompoundTag getCompound(String string) {
		try {
			if (this.contains(string, 10)) {
				return (CompoundTag)this.tags.get(string);
			}
		} catch (ClassCastException var3) {
			throw new ReportedException(this.createReport(string, 10, var3));
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
			throw new ReportedException(this.createReport(string, 9, var4));
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
		StringBuilder stringBuilder = new StringBuilder("{");
		Collection<String> collection = this.tags.keySet();
		if (LOGGER.isDebugEnabled()) {
			List<String> list = Lists.<String>newArrayList(this.tags.keySet());
			Collections.sort(list);
			collection = list;
		}

		for (String string : collection) {
			if (stringBuilder.length() != 1) {
				stringBuilder.append(',');
			}

			stringBuilder.append(handleEscape(string)).append(':').append(this.tags.get(string));
		}

		return stringBuilder.append('}').toString();
	}

	public boolean isEmpty() {
		return this.tags.isEmpty();
	}

	private CrashReport createReport(String string, int i, ClassCastException classCastException) {
		CrashReport crashReport = CrashReport.forThrowable(classCastException, "Reading NBT data");
		CrashReportCategory crashReportCategory = crashReport.addCategory("Corrupt NBT tag", 1);
		crashReportCategory.setDetail("Tag type found", (CrashReportDetail<String>)(() -> TAG_NAMES[((Tag)this.tags.get(string)).getId()]));
		crashReportCategory.setDetail("Tag type expected", (CrashReportDetail<String>)(() -> TAG_NAMES[i]));
		crashReportCategory.setDetail("Tag name", string);
		return crashReport;
	}

	public CompoundTag copy() {
		CompoundTag compoundTag = new CompoundTag();

		for (String string : this.tags.keySet()) {
			compoundTag.put(string, ((Tag)this.tags.get(string)).copy());
		}

		return compoundTag;
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

	private static byte readNamedTagType(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
		return dataInput.readByte();
	}

	private static String readNamedTagName(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
		return dataInput.readUTF();
	}

	static Tag readNamedTagData(byte b, String string, DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
		Tag tag = Tag.newTag(b);

		try {
			tag.load(dataInput, i, nbtAccounter);
			return tag;
		} catch (IOException var9) {
			CrashReport crashReport = CrashReport.forThrowable(var9, "Loading NBT data");
			CrashReportCategory crashReportCategory = crashReport.addCategory("NBT Tag");
			crashReportCategory.setDetail("Tag name", string);
			crashReportCategory.setDetail("Tag type", b);
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

	protected static String handleEscape(String string) {
		return SIMPLE_VALUE.matcher(string).matches() ? string : StringTag.quoteAndEscape(string);
	}

	protected static Component handleEscapePretty(String string) {
		if (SIMPLE_VALUE.matcher(string).matches()) {
			return new TextComponent(string).withStyle(SYNTAX_HIGHLIGHTING_KEY);
		} else {
			String string2 = StringTag.quoteAndEscape(string);
			String string3 = string2.substring(0, 1);
			Component component = new TextComponent(string2.substring(1, string2.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_KEY);
			return new TextComponent(string3).append(component).append(string3);
		}
	}

	@Override
	public Component getPrettyDisplay(String string, int i) {
		if (this.tags.isEmpty()) {
			return new TextComponent("{}");
		} else {
			Component component = new TextComponent("{");
			Collection<String> collection = this.tags.keySet();
			if (LOGGER.isDebugEnabled()) {
				List<String> list = Lists.<String>newArrayList(this.tags.keySet());
				Collections.sort(list);
				collection = list;
			}

			if (!string.isEmpty()) {
				component.append("\n");
			}

			Iterator<String> iterator = collection.iterator();

			while (iterator.hasNext()) {
				String string2 = (String)iterator.next();
				Component component2 = new TextComponent(Strings.repeat(string, i + 1))
					.append(handleEscapePretty(string2))
					.append(String.valueOf(':'))
					.append(" ")
					.append(((Tag)this.tags.get(string2)).getPrettyDisplay(string, i + 1));
				if (iterator.hasNext()) {
					component2.append(String.valueOf(',')).append(string.isEmpty() ? " " : "\n");
				}

				component.append(component2);
			}

			if (!string.isEmpty()) {
				component.append("\n").append(Strings.repeat(string, i));
			}

			component.append("}");
			return component;
		}
	}
}
