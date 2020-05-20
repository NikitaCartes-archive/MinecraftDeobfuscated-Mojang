/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.PeekingIterator;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
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
import org.jetbrains.annotations.Nullable;

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
    public <U> U convertTo(DynamicOps<U> dynamicOps, Tag tag) {
        switch (tag.getId()) {
            case 0: {
                return dynamicOps.empty();
            }
            case 1: {
                return dynamicOps.createByte(((NumericTag)tag).getAsByte());
            }
            case 2: {
                return dynamicOps.createShort(((NumericTag)tag).getAsShort());
            }
            case 3: {
                return dynamicOps.createInt(((NumericTag)tag).getAsInt());
            }
            case 4: {
                return dynamicOps.createLong(((NumericTag)tag).getAsLong());
            }
            case 5: {
                return dynamicOps.createFloat(((NumericTag)tag).getAsFloat());
            }
            case 6: {
                return dynamicOps.createDouble(((NumericTag)tag).getAsDouble());
            }
            case 7: {
                return dynamicOps.createByteList(ByteBuffer.wrap(((ByteArrayTag)tag).getAsByteArray()));
            }
            case 8: {
                return dynamicOps.createString(tag.getAsString());
            }
            case 9: {
                return this.convertList(dynamicOps, tag);
            }
            case 10: {
                return this.convertMap(dynamicOps, tag);
            }
            case 11: {
                return dynamicOps.createIntList(Arrays.stream(((IntArrayTag)tag).getAsIntArray()));
            }
            case 12: {
                return dynamicOps.createLongList(Arrays.stream(((LongArrayTag)tag).getAsLongArray()));
            }
        }
        throw new IllegalStateException("Unknown tag type: " + tag);
    }

    @Override
    public DataResult<Number> getNumberValue(Tag tag) {
        if (tag instanceof NumericTag) {
            return DataResult.success(((NumericTag)tag).getAsNumber());
        }
        return DataResult.error("Not a number");
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
    public DataResult<String> getStringValue(Tag tag) {
        if (tag instanceof StringTag) {
            return DataResult.success(tag.getAsString());
        }
        return DataResult.error("Not a string");
    }

    @Override
    public Tag createString(String string) {
        return StringTag.valueOf(string);
    }

    private static CollectionTag<?> createGenericList(byte b, byte c) {
        if (NbtOps.typesMatch(b, c, (byte)4)) {
            return new LongArrayTag(new long[0]);
        }
        if (NbtOps.typesMatch(b, c, (byte)1)) {
            return new ByteArrayTag(new byte[0]);
        }
        if (NbtOps.typesMatch(b, c, (byte)3)) {
            return new IntArrayTag(new int[0]);
        }
        return new ListTag();
    }

    private static boolean typesMatch(byte b, byte c, byte d) {
        return !(b != d && b != 0 || c != d && c != 0);
    }

    private static <T extends Tag> void fillOne(CollectionTag<T> collectionTag, Tag tag2, Tag tag22) {
        if (tag2 instanceof CollectionTag) {
            CollectionTag collectionTag2 = (CollectionTag)tag2;
            collectionTag2.forEach(tag -> collectionTag.add(tag));
        }
        collectionTag.add(tag22);
    }

    private static <T extends Tag> void fillMany(CollectionTag<T> collectionTag, Tag tag2, List<Tag> list) {
        if (tag2 instanceof CollectionTag) {
            CollectionTag collectionTag2 = (CollectionTag)tag2;
            collectionTag2.forEach(tag -> collectionTag.add(tag));
        }
        list.forEach(tag -> collectionTag.add(tag));
    }

    @Override
    public DataResult<Tag> mergeToList(Tag tag, Tag tag2) {
        if (!(tag instanceof CollectionTag) && !(tag instanceof EndTag)) {
            return DataResult.error("mergeToList called with not a list: " + tag, tag);
        }
        CollectionTag<?> collectionTag = NbtOps.createGenericList(tag instanceof CollectionTag ? ((CollectionTag)tag).getElementType() : (byte)0, tag2.getId());
        NbtOps.fillOne(collectionTag, tag, tag2);
        return DataResult.success(collectionTag);
    }

    @Override
    public DataResult<Tag> mergeToList(Tag tag, List<Tag> list) {
        if (!(tag instanceof CollectionTag) && !(tag instanceof EndTag)) {
            return DataResult.error("mergeToList called with not a list: " + tag, tag);
        }
        CollectionTag<?> collectionTag = NbtOps.createGenericList(tag instanceof CollectionTag ? ((CollectionTag)tag).getElementType() : (byte)0, list.stream().findFirst().map(Tag::getId).orElse((byte)0));
        NbtOps.fillMany(collectionTag, tag, list);
        return DataResult.success(collectionTag);
    }

    @Override
    public DataResult<Tag> mergeToMap(Tag tag, Tag tag2, Tag tag3) {
        if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
            return DataResult.error("mergeToMap called with not a map: " + tag, tag);
        }
        if (!(tag2 instanceof StringTag)) {
            return DataResult.error("key is not a string: " + tag2, tag);
        }
        CompoundTag compoundTag = new CompoundTag();
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag2 = (CompoundTag)tag;
            compoundTag2.getAllKeys().forEach(string -> compoundTag.put((String)string, compoundTag2.get((String)string)));
        }
        compoundTag.put(tag2.getAsString(), tag3);
        return DataResult.success(compoundTag);
    }

    @Override
    public DataResult<Tag> mergeToMap(Tag tag, MapLike<Tag> mapLike) {
        if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
            return DataResult.error("mergeToMap called with not a map: " + tag, tag);
        }
        CompoundTag compoundTag = new CompoundTag();
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag2 = (CompoundTag)tag;
            compoundTag2.getAllKeys().forEach(string -> compoundTag.put((String)string, compoundTag2.get((String)string)));
        }
        ArrayList list = Lists.newArrayList();
        mapLike.entries().forEach(pair -> {
            Tag tag = (Tag)pair.getFirst();
            if (!(tag instanceof StringTag)) {
                list.add(tag);
                return;
            }
            compoundTag.put(tag.getAsString(), (Tag)pair.getSecond());
        });
        if (!list.isEmpty()) {
            return DataResult.error("some keys are not strings: " + list, compoundTag);
        }
        return DataResult.success(compoundTag);
    }

    @Override
    public DataResult<Stream<Pair<Tag, Tag>>> getMapValues(Tag tag) {
        if (!(tag instanceof CompoundTag)) {
            return DataResult.error("Not a map: " + tag);
        }
        CompoundTag compoundTag = (CompoundTag)tag;
        return DataResult.success(compoundTag.getAllKeys().stream().map(string -> Pair.of(this.createString((String)string), compoundTag.get((String)string))));
    }

    @Override
    public DataResult<Consumer<BiConsumer<Tag, Tag>>> getMapEntries(Tag tag) {
        if (!(tag instanceof CompoundTag)) {
            return DataResult.error("Not a map: " + tag);
        }
        CompoundTag compoundTag = (CompoundTag)tag;
        return DataResult.success(biConsumer -> compoundTag.getAllKeys().forEach(string -> biConsumer.accept(this.createString((String)string), compoundTag.get((String)string))));
    }

    @Override
    public DataResult<MapLike<Tag>> getMap(Tag tag) {
        if (!(tag instanceof CompoundTag)) {
            return DataResult.error("Not a map: " + tag);
        }
        final CompoundTag compoundTag = (CompoundTag)tag;
        return DataResult.success(new MapLike<Tag>(){

            @Override
            @Nullable
            public Tag get(Tag tag) {
                return compoundTag.get(tag.getAsString());
            }

            @Override
            @Nullable
            public Tag get(String string) {
                return compoundTag.get(string);
            }

            @Override
            public Stream<Pair<Tag, Tag>> entries() {
                return compoundTag.getAllKeys().stream().map(string -> Pair.of(NbtOps.this.createString((String)string), compoundTag.get((String)string)));
            }

            public String toString() {
                return "MapLike[" + compoundTag + "]";
            }

            @Override
            @Nullable
            public /* synthetic */ Object get(String string) {
                return this.get(string);
            }

            @Override
            @Nullable
            public /* synthetic */ Object get(Object object) {
                return this.get((Tag)object);
            }
        });
    }

    @Override
    public Tag createMap(Stream<Pair<Tag, Tag>> stream) {
        CompoundTag compoundTag = new CompoundTag();
        stream.forEach(pair -> compoundTag.put(((Tag)pair.getFirst()).getAsString(), (Tag)pair.getSecond()));
        return compoundTag;
    }

    @Override
    public DataResult<Stream<Tag>> getStream(Tag tag2) {
        if (tag2 instanceof CollectionTag) {
            return DataResult.success(((CollectionTag)tag2).stream().map(tag -> tag));
        }
        return DataResult.error("Not a list");
    }

    @Override
    public DataResult<Consumer<Consumer<Tag>>> getList(Tag tag) {
        if (tag instanceof CollectionTag) {
            CollectionTag collectionTag = (CollectionTag)tag;
            return DataResult.success(collectionTag::forEach);
        }
        return DataResult.error("Not a list: " + tag);
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(Tag tag) {
        if (tag instanceof ByteArrayTag) {
            return DataResult.success(ByteBuffer.wrap(((ByteArrayTag)tag).getAsByteArray()));
        }
        return DynamicOps.super.getByteBuffer(tag);
    }

    @Override
    public Tag createByteList(ByteBuffer byteBuffer) {
        return new ByteArrayTag(DataFixUtils.toArray(byteBuffer));
    }

    @Override
    public DataResult<IntStream> getIntStream(Tag tag) {
        if (tag instanceof IntArrayTag) {
            return DataResult.success(Arrays.stream(((IntArrayTag)tag).getAsIntArray()));
        }
        return DynamicOps.super.getIntStream(tag);
    }

    @Override
    public Tag createIntList(IntStream intStream) {
        return new IntArrayTag(intStream.toArray());
    }

    @Override
    public DataResult<LongStream> getLongStream(Tag tag) {
        if (tag instanceof LongArrayTag) {
            return DataResult.success(Arrays.stream(((LongArrayTag)tag).getAsLongArray()));
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
    public RecordBuilder<Tag> mapBuilder() {
        return new NbtRecordBuilder();
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
    public /* synthetic */ DataResult getLongStream(Object object) {
        return this.getLongStream((Tag)object);
    }

    @Override
    public /* synthetic */ Object createIntList(IntStream intStream) {
        return this.createIntList(intStream);
    }

    @Override
    public /* synthetic */ DataResult getIntStream(Object object) {
        return this.getIntStream((Tag)object);
    }

    @Override
    public /* synthetic */ Object createByteList(ByteBuffer byteBuffer) {
        return this.createByteList(byteBuffer);
    }

    @Override
    public /* synthetic */ DataResult getByteBuffer(Object object) {
        return this.getByteBuffer((Tag)object);
    }

    @Override
    public /* synthetic */ Object createList(Stream stream) {
        return this.createList(stream);
    }

    @Override
    public /* synthetic */ DataResult getList(Object object) {
        return this.getList((Tag)object);
    }

    @Override
    public /* synthetic */ DataResult getStream(Object object) {
        return this.getStream((Tag)object);
    }

    @Override
    public /* synthetic */ DataResult getMap(Object object) {
        return this.getMap((Tag)object);
    }

    @Override
    public /* synthetic */ Object createMap(Stream stream) {
        return this.createMap(stream);
    }

    @Override
    public /* synthetic */ DataResult getMapEntries(Object object) {
        return this.getMapEntries((Tag)object);
    }

    @Override
    public /* synthetic */ DataResult getMapValues(Object object) {
        return this.getMapValues((Tag)object);
    }

    @Override
    public /* synthetic */ DataResult mergeToMap(Object object, MapLike mapLike) {
        return this.mergeToMap((Tag)object, (MapLike<Tag>)mapLike);
    }

    @Override
    public /* synthetic */ DataResult mergeToMap(Object object, Object object2, Object object3) {
        return this.mergeToMap((Tag)object, (Tag)object2, (Tag)object3);
    }

    @Override
    public /* synthetic */ DataResult mergeToList(Object object, List list) {
        return this.mergeToList((Tag)object, (List<Tag>)list);
    }

    @Override
    public /* synthetic */ DataResult mergeToList(Object object, Object object2) {
        return this.mergeToList((Tag)object, (Tag)object2);
    }

    @Override
    public /* synthetic */ Object createString(String string) {
        return this.createString(string);
    }

    @Override
    public /* synthetic */ DataResult getStringValue(Object object) {
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
    public /* synthetic */ DataResult getNumberValue(Object object) {
        return this.getNumberValue((Tag)object);
    }

    @Override
    public /* synthetic */ Object convertTo(DynamicOps dynamicOps, Object object) {
        return this.convertTo(dynamicOps, (Tag)object);
    }

    @Override
    public /* synthetic */ Object empty() {
        return this.empty();
    }

    class NbtRecordBuilder
    extends RecordBuilder.AbstractStringBuilder<Tag, CompoundTag> {
        protected NbtRecordBuilder() {
            super(NbtOps.this);
        }

        @Override
        protected CompoundTag initBuilder() {
            return new CompoundTag();
        }

        @Override
        protected CompoundTag append(String string, Tag tag, CompoundTag compoundTag) {
            compoundTag.put(string, tag);
            return compoundTag;
        }

        @Override
        protected DataResult<Tag> build(CompoundTag compoundTag, Tag tag) {
            if (tag == null || tag == EndTag.INSTANCE) {
                return DataResult.success(compoundTag);
            }
            if (tag instanceof CompoundTag) {
                CompoundTag compoundTag2 = new CompoundTag(Maps.newHashMap(((CompoundTag)tag).entries()));
                for (Map.Entry<String, Tag> entry : compoundTag.entries().entrySet()) {
                    compoundTag2.put(entry.getKey(), entry.getValue());
                }
                return DataResult.success(compoundTag2);
            }
            return DataResult.error("mergeToMap called with not a map: " + tag, tag);
        }

        @Override
        protected /* synthetic */ Object append(String string, Object object, Object object2) {
            return this.append(string, (Tag)object, (CompoundTag)object2);
        }

        @Override
        protected /* synthetic */ DataResult build(Object object, Object object2) {
            return this.build((CompoundTag)object, (Tag)object2);
        }

        @Override
        protected /* synthetic */ Object initBuilder() {
            return this.initBuilder();
        }
    }
}

