/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class NbtOps
implements DynamicOps<Tag> {
    public static final NbtOps INSTANCE = new NbtOps();

    protected NbtOps() {
    }

    @Override
    public Tag empty() {
        return EndTag.INSTANCE;
    }

    @Override
    public Type<?> getType(Tag tag) {
        switch (tag.getId()) {
            case 0: {
                return DSL.nilType();
            }
            case 1: {
                return DSL.byteType();
            }
            case 2: {
                return DSL.shortType();
            }
            case 3: {
                return DSL.intType();
            }
            case 4: {
                return DSL.longType();
            }
            case 5: {
                return DSL.floatType();
            }
            case 6: {
                return DSL.doubleType();
            }
            case 7: {
                return DSL.list(DSL.byteType());
            }
            case 8: {
                return DSL.string();
            }
            case 9: {
                return DSL.list(DSL.remainderType());
            }
            case 10: {
                return DSL.compoundList(DSL.remainderType(), DSL.remainderType());
            }
            case 11: {
                return DSL.list(DSL.intType());
            }
            case 12: {
                return DSL.list(DSL.longType());
            }
        }
        return DSL.remainderType();
    }

    @Override
    public Optional<Number> getNumberValue(Tag tag) {
        if (tag instanceof NumericTag) {
            return Optional.of(((NumericTag)tag).getAsNumber());
        }
        return Optional.empty();
    }

    @Override
    public Tag createNumeric(Number number) {
        return DoubleTag.valueOf(number.doubleValue());
    }

    @Override
    public Tag createByte(byte b) {
        return ByteTag.valueOf(b);
    }

    @Override
    public Tag createShort(short s) {
        return ShortTag.valueOf(s);
    }

    @Override
    public Tag createInt(int i) {
        return IntTag.valueOf(i);
    }

    @Override
    public Tag createLong(long l) {
        return LongTag.valueOf(l);
    }

    @Override
    public Tag createFloat(float f) {
        return FloatTag.valueOf(f);
    }

    @Override
    public Tag createDouble(double d) {
        return DoubleTag.valueOf(d);
    }

    @Override
    public Tag createBoolean(boolean bl) {
        return ByteTag.valueOf(bl);
    }

    @Override
    public Optional<String> getStringValue(Tag tag) {
        if (tag instanceof StringTag) {
            return Optional.of(tag.getAsString());
        }
        return Optional.empty();
    }

    @Override
    public Tag createString(String string) {
        return StringTag.valueOf(string);
    }

    @Override
    public Tag mergeInto(Tag tag, Tag tag2) {
        if (tag2 instanceof EndTag) {
            return tag;
        }
        if (tag instanceof CompoundTag) {
            if (tag2 instanceof CompoundTag) {
                CompoundTag compoundTag = new CompoundTag();
                CompoundTag compoundTag2 = (CompoundTag)tag;
                for (String string : compoundTag2.getAllKeys()) {
                    compoundTag.put(string, compoundTag2.get(string));
                }
                CompoundTag compoundTag3 = (CompoundTag)tag2;
                for (String string2 : compoundTag3.getAllKeys()) {
                    compoundTag.put(string2, compoundTag3.get(string2));
                }
                return compoundTag;
            }
            return tag;
        }
        if (tag instanceof EndTag) {
            throw new IllegalArgumentException("mergeInto called with a null input.");
        }
        if (!(tag instanceof CollectionTag)) {
            return tag;
        }
        ListTag collectionTag = new ListTag();
        CollectionTag collectionTag2 = (CollectionTag)tag;
        collectionTag.addAll(collectionTag2);
        collectionTag.add(tag2);
        return collectionTag;
    }

    @Override
    public Tag mergeInto(Tag tag, Tag tag2, Tag tag3) {
        CompoundTag compoundTag;
        if (tag instanceof EndTag) {
            compoundTag = new CompoundTag();
        } else if (tag instanceof CompoundTag) {
            CompoundTag compoundTag2 = (CompoundTag)tag;
            compoundTag = new CompoundTag();
            compoundTag2.getAllKeys().forEach(string -> compoundTag.put((String)string, compoundTag2.get((String)string)));
        } else {
            return tag;
        }
        compoundTag.put(tag2.getAsString(), tag3);
        return compoundTag;
    }

    @Override
    public Tag merge(Tag tag, Tag tag2) {
        if (tag instanceof EndTag) {
            return tag2;
        }
        if (tag2 instanceof EndTag) {
            return tag;
        }
        if (tag instanceof CompoundTag && tag2 instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            CompoundTag compoundTag2 = (CompoundTag)tag2;
            CompoundTag compoundTag3 = new CompoundTag();
            compoundTag.getAllKeys().forEach(string -> compoundTag3.put((String)string, compoundTag.get((String)string)));
            compoundTag2.getAllKeys().forEach(string -> compoundTag3.put((String)string, compoundTag2.get((String)string)));
            return compoundTag3;
        }
        if (tag instanceof CollectionTag && tag2 instanceof CollectionTag) {
            ListTag listTag = new ListTag();
            listTag.addAll((CollectionTag)tag);
            listTag.addAll((CollectionTag)tag2);
            return listTag;
        }
        throw new IllegalArgumentException("Could not merge " + tag + " and " + tag2);
    }

    @Override
    public Optional<Map<Tag, Tag>> getMapValues(Tag tag) {
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            return Optional.of(compoundTag.getAllKeys().stream().map(string -> Pair.of(this.createString((String)string), compoundTag.get((String)string))).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
        }
        return Optional.empty();
    }

    @Override
    public Tag createMap(Map<Tag, Tag> map) {
        CompoundTag compoundTag = new CompoundTag();
        for (Map.Entry<Tag, Tag> entry : map.entrySet()) {
            compoundTag.put(entry.getKey().getAsString(), entry.getValue());
        }
        return compoundTag;
    }

    @Override
    public Optional<Stream<Tag>> getStream(Tag tag2) {
        if (tag2 instanceof CollectionTag) {
            return Optional.of(((CollectionTag)tag2).stream().map(tag -> tag));
        }
        return Optional.empty();
    }

    @Override
    public Optional<ByteBuffer> getByteBuffer(Tag tag) {
        if (tag instanceof ByteArrayTag) {
            return Optional.of(ByteBuffer.wrap(((ByteArrayTag)tag).getAsByteArray()));
        }
        return DynamicOps.super.getByteBuffer(tag);
    }

    @Override
    public Tag createByteList(ByteBuffer byteBuffer) {
        return new ByteArrayTag(DataFixUtils.toArray(byteBuffer));
    }

    @Override
    public Optional<IntStream> getIntStream(Tag tag) {
        if (tag instanceof IntArrayTag) {
            return Optional.of(Arrays.stream(((IntArrayTag)tag).getAsIntArray()));
        }
        return DynamicOps.super.getIntStream(tag);
    }

    @Override
    public Tag createIntList(IntStream intStream) {
        return new IntArrayTag(intStream.toArray());
    }

    @Override
    public Optional<LongStream> getLongStream(Tag tag) {
        if (tag instanceof LongArrayTag) {
            return Optional.of(Arrays.stream(((LongArrayTag)tag).getAsLongArray()));
        }
        return DynamicOps.super.getLongStream(tag);
    }

    @Override
    public Tag createLongList(LongStream longStream) {
        return new LongArrayTag(longStream.toArray());
    }

    @Override
    public Tag createList(Stream<Tag> stream) {
        PeekingIterator peekingIterator = Iterators.peekingIterator(stream.iterator());
        if (!peekingIterator.hasNext()) {
            return new ListTag();
        }
        Tag tag2 = (Tag)peekingIterator.peek();
        if (tag2 instanceof ByteTag) {
            ArrayList<Byte> list = Lists.newArrayList(Iterators.transform(peekingIterator, tag -> ((ByteTag)tag).getAsByte()));
            return new ByteArrayTag(list);
        }
        if (tag2 instanceof IntTag) {
            ArrayList<Integer> list = Lists.newArrayList(Iterators.transform(peekingIterator, tag -> ((IntTag)tag).getAsInt()));
            return new IntArrayTag(list);
        }
        if (tag2 instanceof LongTag) {
            ArrayList<Long> list = Lists.newArrayList(Iterators.transform(peekingIterator, tag -> ((LongTag)tag).getAsLong()));
            return new LongArrayTag(list);
        }
        ListTag listTag = new ListTag();
        while (peekingIterator.hasNext()) {
            Tag tag22 = (Tag)peekingIterator.next();
            if (tag22 instanceof EndTag) continue;
            listTag.add(tag22);
        }
        return listTag;
    }

    @Override
    public Tag remove(Tag tag, String string3) {
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            CompoundTag compoundTag2 = new CompoundTag();
            compoundTag.getAllKeys().stream().filter(string2 -> !Objects.equals(string2, string3)).forEach(string -> compoundTag2.put((String)string, compoundTag.get((String)string)));
            return compoundTag2;
        }
        return tag;
    }

    public String toString() {
        return "NBT";
    }

    @Override
    public /* synthetic */ Object remove(Object object, String string) {
        return this.remove((Tag)object, string);
    }

    @Override
    public /* synthetic */ Object createLongList(LongStream longStream) {
        return this.createLongList(longStream);
    }

    @Override
    public /* synthetic */ Optional getLongStream(Object object) {
        return this.getLongStream((Tag)object);
    }

    @Override
    public /* synthetic */ Object createIntList(IntStream intStream) {
        return this.createIntList(intStream);
    }

    @Override
    public /* synthetic */ Optional getIntStream(Object object) {
        return this.getIntStream((Tag)object);
    }

    @Override
    public /* synthetic */ Object createByteList(ByteBuffer byteBuffer) {
        return this.createByteList(byteBuffer);
    }

    @Override
    public /* synthetic */ Optional getByteBuffer(Object object) {
        return this.getByteBuffer((Tag)object);
    }

    @Override
    public /* synthetic */ Object createList(Stream stream) {
        return this.createList(stream);
    }

    @Override
    public /* synthetic */ Optional getStream(Object object) {
        return this.getStream((Tag)object);
    }

    @Override
    public /* synthetic */ Object createMap(Map map) {
        return this.createMap(map);
    }

    @Override
    public /* synthetic */ Optional getMapValues(Object object) {
        return this.getMapValues((Tag)object);
    }

    @Override
    public /* synthetic */ Object merge(Object object, Object object2) {
        return this.merge((Tag)object, (Tag)object2);
    }

    @Override
    public /* synthetic */ Object mergeInto(Object object, Object object2, Object object3) {
        return this.mergeInto((Tag)object, (Tag)object2, (Tag)object3);
    }

    @Override
    public /* synthetic */ Object mergeInto(Object object, Object object2) {
        return this.mergeInto((Tag)object, (Tag)object2);
    }

    @Override
    public /* synthetic */ Object createString(String string) {
        return this.createString(string);
    }

    @Override
    public /* synthetic */ Optional getStringValue(Object object) {
        return this.getStringValue((Tag)object);
    }

    @Override
    public /* synthetic */ Object createBoolean(boolean bl) {
        return this.createBoolean(bl);
    }

    @Override
    public /* synthetic */ Object createDouble(double d) {
        return this.createDouble(d);
    }

    @Override
    public /* synthetic */ Object createFloat(float f) {
        return this.createFloat(f);
    }

    @Override
    public /* synthetic */ Object createLong(long l) {
        return this.createLong(l);
    }

    @Override
    public /* synthetic */ Object createInt(int i) {
        return this.createInt(i);
    }

    @Override
    public /* synthetic */ Object createShort(short s) {
        return this.createShort(s);
    }

    @Override
    public /* synthetic */ Object createByte(byte b) {
        return this.createByte(b);
    }

    @Override
    public /* synthetic */ Object createNumeric(Number number) {
        return this.createNumeric(number);
    }

    @Override
    public /* synthetic */ Optional getNumberValue(Object object) {
        return this.getNumberValue((Tag)object);
    }

    @Override
    public /* synthetic */ Type getType(Object object) {
        return this.getType((Tag)object);
    }

    @Override
    public /* synthetic */ Object empty() {
        return this.empty();
    }
}

