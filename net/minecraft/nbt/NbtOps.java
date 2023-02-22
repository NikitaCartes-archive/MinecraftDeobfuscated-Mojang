/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    private static final String WRAPPER_MARKER = "";

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
            NumericTag numericTag = (NumericTag)tag;
            return DataResult.success(numericTag.getAsNumber());
        }
        return DataResult.error(() -> "Not a number");
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
            StringTag stringTag = (StringTag)tag;
            return DataResult.success(stringTag.getAsString());
        }
        return DataResult.error(() -> "Not a string");
    }

    @Override
    public Tag createString(String string) {
        return StringTag.valueOf(string);
    }

    @Override
    public DataResult<Tag> mergeToList(Tag tag, Tag tag2) {
        return NbtOps.createCollector(tag).map(listCollector -> DataResult.success(listCollector.accept(tag2).result())).orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + tag, tag));
    }

    @Override
    public DataResult<Tag> mergeToList(Tag tag, List<Tag> list) {
        return NbtOps.createCollector(tag).map(listCollector -> DataResult.success(listCollector.acceptAll(list).result())).orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + tag, tag));
    }

    @Override
    public DataResult<Tag> mergeToMap(Tag tag, Tag tag2, Tag tag3) {
        if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + tag, tag);
        }
        if (!(tag2 instanceof StringTag)) {
            return DataResult.error(() -> "key is not a string: " + tag2, tag);
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
            return DataResult.error(() -> "mergeToMap called with not a map: " + tag, tag);
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
            return DataResult.error(() -> "some keys are not strings: " + list, compoundTag);
        }
        return DataResult.success(compoundTag);
    }

    @Override
    public DataResult<Stream<Pair<Tag, Tag>>> getMapValues(Tag tag) {
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            return DataResult.success(compoundTag.getAllKeys().stream().map(string -> Pair.of(this.createString((String)string), compoundTag.get((String)string))));
        }
        return DataResult.error(() -> "Not a map: " + tag);
    }

    @Override
    public DataResult<Consumer<BiConsumer<Tag, Tag>>> getMapEntries(Tag tag) {
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            return DataResult.success(biConsumer -> compoundTag.getAllKeys().forEach(string -> biConsumer.accept(this.createString((String)string), compoundTag.get((String)string))));
        }
        return DataResult.error(() -> "Not a map: " + tag);
    }

    @Override
    public DataResult<MapLike<Tag>> getMap(Tag tag) {
        if (tag instanceof CompoundTag) {
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
        return DataResult.error(() -> "Not a map: " + tag);
    }

    @Override
    public Tag createMap(Stream<Pair<Tag, Tag>> stream) {
        CompoundTag compoundTag = new CompoundTag();
        stream.forEach(pair -> compoundTag.put(((Tag)pair.getFirst()).getAsString(), (Tag)pair.getSecond()));
        return compoundTag;
    }

    private static Tag tryUnwrap(CompoundTag compoundTag) {
        Tag tag;
        if (compoundTag.size() == 1 && (tag = compoundTag.get(WRAPPER_MARKER)) != null) {
            return tag;
        }
        return compoundTag;
    }

    @Override
    public DataResult<Stream<Tag>> getStream(Tag tag2) {
        if (tag2 instanceof ListTag) {
            ListTag listTag = (ListTag)tag2;
            if (listTag.getElementType() == 10) {
                return DataResult.success(listTag.stream().map(tag -> NbtOps.tryUnwrap((CompoundTag)tag)));
            }
            return DataResult.success(listTag.stream());
        }
        if (tag2 instanceof CollectionTag) {
            CollectionTag collectionTag = (CollectionTag)tag2;
            return DataResult.success(collectionTag.stream().map(tag -> tag));
        }
        return DataResult.error(() -> "Not a list");
    }

    @Override
    public DataResult<Consumer<Consumer<Tag>>> getList(Tag tag) {
        if (tag instanceof ListTag) {
            ListTag listTag = (ListTag)tag;
            if (listTag.getElementType() == 10) {
                return DataResult.success(consumer -> listTag.forEach(tag -> consumer.accept(NbtOps.tryUnwrap((CompoundTag)tag))));
            }
            return DataResult.success(listTag::forEach);
        }
        if (tag instanceof CollectionTag) {
            CollectionTag collectionTag = (CollectionTag)tag;
            return DataResult.success(collectionTag::forEach);
        }
        return DataResult.error(() -> "Not a list: " + tag);
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(Tag tag) {
        if (tag instanceof ByteArrayTag) {
            ByteArrayTag byteArrayTag = (ByteArrayTag)tag;
            return DataResult.success(ByteBuffer.wrap(byteArrayTag.getAsByteArray()));
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
            IntArrayTag intArrayTag = (IntArrayTag)tag;
            return DataResult.success(Arrays.stream(intArrayTag.getAsIntArray()));
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
            LongArrayTag longArrayTag = (LongArrayTag)tag;
            return DataResult.success(Arrays.stream(longArrayTag.getAsLongArray()));
        }
        return DynamicOps.super.getLongStream(tag);
    }

    @Override
    public Tag createLongList(LongStream longStream) {
        return new LongArrayTag(longStream.toArray());
    }

    @Override
    public Tag createList(Stream<Tag> stream) {
        return InitialListCollector.INSTANCE.acceptAll(stream).result();
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

    private static Optional<ListCollector> createCollector(Tag tag) {
        if (tag instanceof EndTag) {
            return Optional.of(InitialListCollector.INSTANCE);
        }
        if (tag instanceof CollectionTag) {
            CollectionTag collectionTag = (CollectionTag)tag;
            if (collectionTag.isEmpty()) {
                return Optional.of(InitialListCollector.INSTANCE);
            }
            if (collectionTag instanceof ListTag) {
                ListTag listTag = (ListTag)collectionTag;
                return switch (listTag.getElementType()) {
                    case 0 -> Optional.of(InitialListCollector.INSTANCE);
                    case 10 -> Optional.of(new HeterogenousListCollector(listTag));
                    default -> Optional.of(new HomogenousListCollector(listTag));
                };
            }
            if (collectionTag instanceof ByteArrayTag) {
                ByteArrayTag byteArrayTag = (ByteArrayTag)collectionTag;
                return Optional.of(new ByteListCollector(byteArrayTag.getAsByteArray()));
            }
            if (collectionTag instanceof IntArrayTag) {
                IntArrayTag intArrayTag = (IntArrayTag)collectionTag;
                return Optional.of(new IntListCollector(intArrayTag.getAsIntArray()));
            }
            if (collectionTag instanceof LongArrayTag) {
                LongArrayTag longArrayTag = (LongArrayTag)collectionTag;
                return Optional.of(new LongListCollector(longArrayTag.getAsLongArray()));
            }
        }
        return Optional.empty();
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

    static class InitialListCollector
    implements ListCollector {
        public static final InitialListCollector INSTANCE = new InitialListCollector();

        private InitialListCollector() {
        }

        @Override
        public ListCollector accept(Tag tag) {
            if (tag instanceof CompoundTag) {
                CompoundTag compoundTag = (CompoundTag)tag;
                return new HeterogenousListCollector().accept(compoundTag);
            }
            if (tag instanceof ByteTag) {
                ByteTag byteTag = (ByteTag)tag;
                return new ByteListCollector(byteTag.getAsByte());
            }
            if (tag instanceof IntTag) {
                IntTag intTag = (IntTag)tag;
                return new IntListCollector(intTag.getAsInt());
            }
            if (tag instanceof LongTag) {
                LongTag longTag = (LongTag)tag;
                return new LongListCollector(longTag.getAsLong());
            }
            return new HomogenousListCollector(tag);
        }

        @Override
        public Tag result() {
            return new ListTag();
        }
    }

    static interface ListCollector {
        public ListCollector accept(Tag var1);

        default public ListCollector acceptAll(Iterable<Tag> iterable) {
            ListCollector listCollector = this;
            for (Tag tag : iterable) {
                listCollector = listCollector.accept(tag);
            }
            return listCollector;
        }

        default public ListCollector acceptAll(Stream<Tag> stream) {
            return this.acceptAll(stream::iterator);
        }

        public Tag result();
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
                CompoundTag compoundTag2 = (CompoundTag)tag;
                CompoundTag compoundTag3 = new CompoundTag(Maps.newHashMap(compoundTag2.entries()));
                for (Map.Entry<String, Tag> entry : compoundTag.entries().entrySet()) {
                    compoundTag3.put(entry.getKey(), entry.getValue());
                }
                return DataResult.success(compoundTag3);
            }
            return DataResult.error(() -> "mergeToMap called with not a map: " + tag, tag);
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

    static class HeterogenousListCollector
    implements ListCollector {
        private final ListTag result = new ListTag();

        public HeterogenousListCollector() {
        }

        public HeterogenousListCollector(Collection<Tag> collection) {
            this.result.addAll(collection);
        }

        public HeterogenousListCollector(IntArrayList intArrayList) {
            intArrayList.forEach(i -> this.result.add(HeterogenousListCollector.wrapElement(IntTag.valueOf(i))));
        }

        public HeterogenousListCollector(ByteArrayList byteArrayList) {
            byteArrayList.forEach(b -> this.result.add(HeterogenousListCollector.wrapElement(ByteTag.valueOf(b))));
        }

        public HeterogenousListCollector(LongArrayList longArrayList) {
            longArrayList.forEach(l -> this.result.add(HeterogenousListCollector.wrapElement(LongTag.valueOf(l))));
        }

        private static boolean isWrapper(CompoundTag compoundTag) {
            return compoundTag.size() == 1 && compoundTag.contains(NbtOps.WRAPPER_MARKER);
        }

        private static Tag wrapIfNeeded(Tag tag) {
            CompoundTag compoundTag;
            if (tag instanceof CompoundTag && !HeterogenousListCollector.isWrapper(compoundTag = (CompoundTag)tag)) {
                return compoundTag;
            }
            return HeterogenousListCollector.wrapElement(tag);
        }

        private static CompoundTag wrapElement(Tag tag) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.put(NbtOps.WRAPPER_MARKER, tag);
            return compoundTag;
        }

        @Override
        public ListCollector accept(Tag tag) {
            this.result.add(HeterogenousListCollector.wrapIfNeeded(tag));
            return this;
        }

        @Override
        public Tag result() {
            return this.result;
        }
    }

    static class HomogenousListCollector
    implements ListCollector {
        private final ListTag result = new ListTag();

        HomogenousListCollector(Tag tag) {
            this.result.add(tag);
        }

        HomogenousListCollector(ListTag listTag) {
            this.result.addAll(listTag);
        }

        @Override
        public ListCollector accept(Tag tag) {
            if (tag.getId() != this.result.getElementType()) {
                return new HeterogenousListCollector().acceptAll(this.result).accept(tag);
            }
            this.result.add(tag);
            return this;
        }

        @Override
        public Tag result() {
            return this.result;
        }
    }

    static class ByteListCollector
    implements ListCollector {
        private final ByteArrayList values = new ByteArrayList();

        public ByteListCollector(byte b) {
            this.values.add(b);
        }

        public ByteListCollector(byte[] bs) {
            this.values.addElements(0, bs);
        }

        @Override
        public ListCollector accept(Tag tag) {
            if (tag instanceof ByteTag) {
                ByteTag byteTag = (ByteTag)tag;
                this.values.add(byteTag.getAsByte());
                return this;
            }
            return new HeterogenousListCollector(this.values).accept(tag);
        }

        @Override
        public Tag result() {
            return new ByteArrayTag(this.values.toByteArray());
        }
    }

    static class IntListCollector
    implements ListCollector {
        private final IntArrayList values = new IntArrayList();

        public IntListCollector(int i) {
            this.values.add(i);
        }

        public IntListCollector(int[] is) {
            this.values.addElements(0, is);
        }

        @Override
        public ListCollector accept(Tag tag) {
            if (tag instanceof IntTag) {
                IntTag intTag = (IntTag)tag;
                this.values.add(intTag.getAsInt());
                return this;
            }
            return new HeterogenousListCollector(this.values).accept(tag);
        }

        @Override
        public Tag result() {
            return new IntArrayTag(this.values.toIntArray());
        }
    }

    static class LongListCollector
    implements ListCollector {
        private final LongArrayList values = new LongArrayList();

        public LongListCollector(long l) {
            this.values.add(l);
        }

        public LongListCollector(long[] ls) {
            this.values.addElements(0, ls);
        }

        @Override
        public ListCollector accept(Tag tag) {
            if (tag instanceof LongTag) {
                LongTag longTag = (LongTag)tag;
                this.values.add(longTag.getAsLong());
                return this;
            }
            return new HeterogenousListCollector(this.values).accept(tag);
        }

        @Override
        public Tag result() {
            return new LongArrayTag(this.values.toLongArray());
        }
    }
}

