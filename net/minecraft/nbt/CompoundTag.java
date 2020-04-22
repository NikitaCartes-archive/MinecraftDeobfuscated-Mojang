/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class CompoundTag
implements Tag {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
    public static final TagType<CompoundTag> TYPE = new TagType<CompoundTag>(){

        @Override
        public CompoundTag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            byte b;
            nbtAccounter.accountBits(384L);
            if (i > 512) {
                throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
            }
            HashMap<String, Tag> map = Maps.newHashMap();
            while ((b = CompoundTag.readNamedTagType(dataInput, nbtAccounter)) != 0) {
                String string = CompoundTag.readNamedTagName(dataInput, nbtAccounter);
                nbtAccounter.accountBits(224 + 16 * string.length());
                Tag tag = CompoundTag.readNamedTagData(TagTypes.getType(b), string, dataInput, i + 1, nbtAccounter);
                if (map.put(string, tag) == null) continue;
                nbtAccounter.accountBits(288L);
            }
            return new CompoundTag(map);
        }

        @Override
        public String getName() {
            return "COMPOUND";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Compound";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, i, nbtAccounter);
        }
    };
    private final Map<String, Tag> tags;

    private CompoundTag(Map<String, Tag> map) {
        this.tags = map;
    }

    public CompoundTag() {
        this(Maps.newHashMap());
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        for (String string : this.tags.keySet()) {
            Tag tag = this.tags.get(string);
            CompoundTag.writeNamedTag(string, tag, dataOutput);
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

    public TagType<CompoundTag> getType() {
        return TYPE;
    }

    public int size() {
        return this.tags.size();
    }

    @Nullable
    public Tag put(String string, Tag tag) {
        return this.tags.put(string, tag);
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
        return this.tags.get(string);
    }

    public byte getTagType(String string) {
        Tag tag = this.tags.get(string);
        if (tag == null) {
            return 0;
        }
        return tag.getId();
    }

    public boolean contains(String string) {
        return this.tags.containsKey(string);
    }

    public boolean contains(String string, int i) {
        byte j = this.getTagType(string);
        if (j == i) {
            return true;
        }
        if (i == 99) {
            return j == 1 || j == 2 || j == 3 || j == 4 || j == 5 || j == 6;
        }
        return false;
    }

    public byte getByte(String string) {
        try {
            if (this.contains(string, 99)) {
                return ((NumericTag)this.tags.get(string)).getAsByte();
            }
        } catch (ClassCastException classCastException) {
            // empty catch block
        }
        return 0;
    }

    public short getShort(String string) {
        try {
            if (this.contains(string, 99)) {
                return ((NumericTag)this.tags.get(string)).getAsShort();
            }
        } catch (ClassCastException classCastException) {
            // empty catch block
        }
        return 0;
    }

    public int getInt(String string) {
        try {
            if (this.contains(string, 99)) {
                return ((NumericTag)this.tags.get(string)).getAsInt();
            }
        } catch (ClassCastException classCastException) {
            // empty catch block
        }
        return 0;
    }

    public long getLong(String string) {
        try {
            if (this.contains(string, 99)) {
                return ((NumericTag)this.tags.get(string)).getAsLong();
            }
        } catch (ClassCastException classCastException) {
            // empty catch block
        }
        return 0L;
    }

    public float getFloat(String string) {
        try {
            if (this.contains(string, 99)) {
                return ((NumericTag)this.tags.get(string)).getAsFloat();
            }
        } catch (ClassCastException classCastException) {
            // empty catch block
        }
        return 0.0f;
    }

    public double getDouble(String string) {
        try {
            if (this.contains(string, 99)) {
                return ((NumericTag)this.tags.get(string)).getAsDouble();
            }
        } catch (ClassCastException classCastException) {
            // empty catch block
        }
        return 0.0;
    }

    public String getString(String string) {
        try {
            if (this.contains(string, 8)) {
                return this.tags.get(string).getAsString();
            }
        } catch (ClassCastException classCastException) {
            // empty catch block
        }
        return "";
    }

    public byte[] getByteArray(String string) {
        try {
            if (this.contains(string, 7)) {
                return ((ByteArrayTag)this.tags.get(string)).getAsByteArray();
            }
        } catch (ClassCastException classCastException) {
            throw new ReportedException(this.createReport(string, ByteArrayTag.TYPE, classCastException));
        }
        return new byte[0];
    }

    public int[] getIntArray(String string) {
        try {
            if (this.contains(string, 11)) {
                return ((IntArrayTag)this.tags.get(string)).getAsIntArray();
            }
        } catch (ClassCastException classCastException) {
            throw new ReportedException(this.createReport(string, IntArrayTag.TYPE, classCastException));
        }
        return new int[0];
    }

    public long[] getLongArray(String string) {
        try {
            if (this.contains(string, 12)) {
                return ((LongArrayTag)this.tags.get(string)).getAsLongArray();
            }
        } catch (ClassCastException classCastException) {
            throw new ReportedException(this.createReport(string, LongArrayTag.TYPE, classCastException));
        }
        return new long[0];
    }

    public CompoundTag getCompound(String string) {
        try {
            if (this.contains(string, 10)) {
                return (CompoundTag)this.tags.get(string);
            }
        } catch (ClassCastException classCastException) {
            throw new ReportedException(this.createReport(string, TYPE, classCastException));
        }
        return new CompoundTag();
    }

    public ListTag getList(String string, int i) {
        try {
            if (this.getTagType(string) == 9) {
                ListTag listTag = (ListTag)this.tags.get(string);
                if (listTag.isEmpty() || listTag.getElementType() == i) {
                    return listTag;
                }
                return new ListTag();
            }
        } catch (ClassCastException classCastException) {
            throw new ReportedException(this.createReport(string, ListTag.TYPE, classCastException));
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
            ArrayList<String> list = Lists.newArrayList(this.tags.keySet());
            Collections.sort(list);
            collection = list;
        }
        for (String string : collection) {
            if (stringBuilder.length() != 1) {
                stringBuilder.append(',');
            }
            stringBuilder.append(CompoundTag.handleEscape(string)).append(':').append(this.tags.get(string));
        }
        return stringBuilder.append('}').toString();
    }

    public boolean isEmpty() {
        return this.tags.isEmpty();
    }

    private CrashReport createReport(String string, TagType<?> tagType, ClassCastException classCastException) {
        CrashReport crashReport = CrashReport.forThrowable(classCastException, "Reading NBT data");
        CrashReportCategory crashReportCategory = crashReport.addCategory("Corrupt NBT tag", 1);
        crashReportCategory.setDetail("Tag type found", () -> this.tags.get(string).getType().getName());
        crashReportCategory.setDetail("Tag type expected", tagType::getName);
        crashReportCategory.setDetail("Tag name", string);
        return crashReport;
    }

    @Override
    public CompoundTag copy() {
        HashMap<String, Tag> map = Maps.newHashMap(Maps.transformValues(this.tags, Tag::copy));
        return new CompoundTag(map);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof CompoundTag && Objects.equals(this.tags, ((CompoundTag)object).tags);
    }

    public int hashCode() {
        return this.tags.hashCode();
    }

    private static void writeNamedTag(String string, Tag tag, DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(tag.getId());
        if (tag.getId() == 0) {
            return;
        }
        dataOutput.writeUTF(string);
        tag.write(dataOutput);
    }

    private static byte readNamedTagType(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
        return dataInput.readByte();
    }

    private static String readNamedTagName(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
        return dataInput.readUTF();
    }

    private static Tag readNamedTagData(TagType<?> tagType, String string, DataInput dataInput, int i, NbtAccounter nbtAccounter) {
        try {
            return tagType.load(dataInput, i, nbtAccounter);
        } catch (IOException iOException) {
            CrashReport crashReport = CrashReport.forThrowable(iOException, "Loading NBT data");
            CrashReportCategory crashReportCategory = crashReport.addCategory("NBT Tag");
            crashReportCategory.setDetail("Tag name", string);
            crashReportCategory.setDetail("Tag type", tagType.getName());
            throw new ReportedException(crashReport);
        }
    }

    public CompoundTag merge(CompoundTag compoundTag) {
        for (String string : compoundTag.tags.keySet()) {
            Tag tag = compoundTag.tags.get(string);
            if (tag.getId() == 10) {
                if (this.contains(string, 10)) {
                    CompoundTag compoundTag2 = this.getCompound(string);
                    compoundTag2.merge((CompoundTag)tag);
                    continue;
                }
                this.put(string, tag.copy());
                continue;
            }
            this.put(string, tag.copy());
        }
        return this;
    }

    protected static String handleEscape(String string) {
        if (SIMPLE_VALUE.matcher(string).matches()) {
            return string;
        }
        return StringTag.quoteAndEscape(string);
    }

    protected static Component handleEscapePretty(String string) {
        if (SIMPLE_VALUE.matcher(string).matches()) {
            return new TextComponent(string).withStyle(SYNTAX_HIGHLIGHTING_KEY);
        }
        String string2 = StringTag.quoteAndEscape(string);
        String string3 = string2.substring(0, 1);
        MutableComponent component = new TextComponent(string2.substring(1, string2.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_KEY);
        return new TextComponent(string3).append(component).append(string3);
    }

    @Override
    public Component getPrettyDisplay(String string, int i) {
        if (this.tags.isEmpty()) {
            return new TextComponent("{}");
        }
        TextComponent mutableComponent = new TextComponent("{");
        Collection<String> collection = this.tags.keySet();
        if (LOGGER.isDebugEnabled()) {
            ArrayList<String> list = Lists.newArrayList(this.tags.keySet());
            Collections.sort(list);
            collection = list;
        }
        if (!string.isEmpty()) {
            mutableComponent.append("\n");
        }
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            String string2 = (String)iterator.next();
            MutableComponent mutableComponent2 = new TextComponent(Strings.repeat(string, i + 1)).append(CompoundTag.handleEscapePretty(string2)).append(String.valueOf(':')).append(" ").append(this.tags.get(string2).getPrettyDisplay(string, i + 1));
            if (iterator.hasNext()) {
                mutableComponent2.append(String.valueOf(',')).append(string.isEmpty() ? " " : "\n");
            }
            mutableComponent.append(mutableComponent2);
        }
        if (!string.isEmpty()) {
            mutableComponent.append("\n").append(Strings.repeat(string, i));
        }
        mutableComponent.append("}");
        return mutableComponent;
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }
}

