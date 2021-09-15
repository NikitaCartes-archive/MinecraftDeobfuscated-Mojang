/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BitStorage;
import net.minecraft.util.DebugBuffer;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.util.ThreadingDetector;
import net.minecraft.util.ZeroBitStorage;
import net.minecraft.world.level.chunk.GlobalPalette;
import net.minecraft.world.level.chunk.HashMapPalette;
import net.minecraft.world.level.chunk.LinearPalette;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PaletteResize;
import net.minecraft.world.level.chunk.SingleValuePalette;
import org.jetbrains.annotations.Nullable;

public class PalettedContainer<T>
implements PaletteResize<T> {
    private static final int MIN_PALETTE_BITS = 0;
    private final PaletteResize<T> dummyPaletteResize = (i, object) -> 0;
    private final IdMap<T> registry;
    private volatile Data<T> data;
    private final Strategy strategy;
    private final Semaphore lock = new Semaphore(1);
    @Nullable
    private final DebugBuffer<Pair<Thread, StackTraceElement[]>> traces = null;

    public void acquire() {
        if (this.traces != null) {
            Thread thread = Thread.currentThread();
            this.traces.push(Pair.of(thread, thread.getStackTrace()));
        }
        ThreadingDetector.checkAndLock(this.lock, this.traces, "PalettedContainer");
    }

    public void release() {
        this.lock.release();
    }

    public static <T> Codec<PalettedContainer<T>> codec(IdMap<T> idMap, Codec<T> codec, Strategy strategy) {
        return RecordCodecBuilder.create(instance -> instance.group(((MapCodec)codec.listOf().fieldOf("palette")).forGetter(DiscData::paletteEntries), Codec.LONG_STREAM.optionalFieldOf("data").forGetter(DiscData::storage)).apply((Applicative<DiscData, ?>)instance, DiscData::new)).comapFlatMap(discData -> PalettedContainer.read(idMap, strategy, discData), palettedContainer -> palettedContainer.write(idMap, strategy));
    }

    public PalettedContainer(IdMap<T> idMap, Strategy strategy, Configuration<T> configuration, BitStorage bitStorage, List<T> list) {
        this.registry = idMap;
        this.strategy = strategy;
        Palette<T> palette = configuration.factory().create(configuration.bits(), idMap, this);
        list.forEach(palette::idFor);
        this.data = new Data(configuration, bitStorage, palette);
    }

    public PalettedContainer(IdMap<T> idMap, T object2, Strategy strategy) {
        this.strategy = strategy;
        this.registry = idMap;
        this.data = this.createOrReuseData(null, 0);
        this.data.palette.idFor(object2);
    }

    private Data<T> createOrReuseData(@Nullable Data<T> data, int i) {
        Configuration<T> configuration = this.strategy.getConfiguration(this.registry, i);
        if (data != null && configuration.equals(data.configuration())) {
            return data;
        }
        return configuration.createData(this.registry, this, this.strategy.size(), null);
    }

    @Override
    public int onResize(int i, T object) {
        Data<T> data = this.data;
        Data<T> data2 = this.createOrReuseData(data, i);
        data2.copyFrom(data.palette, data.storage);
        this.data = data2;
        return data2.palette.idFor(object);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public T getAndSet(int i, int j, int k, T object) {
        this.acquire();
        try {
            T t = this.getAndSet(this.strategy.getIndex(i, j, k), object);
            return t;
        } finally {
            this.release();
        }
    }

    public T getAndSetUnchecked(int i, int j, int k, T object) {
        return this.getAndSet(this.strategy.getIndex(i, j, k), object);
    }

    private T getAndSet(int i, T object) {
        int j = this.data.palette.idFor(object);
        int k = this.data.storage.getAndSet(i, j);
        return this.data.palette.valueFor(k);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void set(int i, int j, int k, T object) {
        this.acquire();
        try {
            this.set(this.strategy.getIndex(i, j, k), object);
        } finally {
            this.release();
        }
    }

    private void set(int i, T object) {
        this.data.set(i, object);
    }

    public T get(int i, int j, int k) {
        return this.get(this.strategy.getIndex(i, j, k));
    }

    protected T get(int i) {
        Data<T> data = this.data;
        return data.palette.valueFor(data.storage.get(i));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void read(FriendlyByteBuf friendlyByteBuf) {
        this.acquire();
        try {
            byte i = friendlyByteBuf.readByte();
            Data<T> data = this.createOrReuseData(this.data, i);
            data.palette.read(friendlyByteBuf);
            friendlyByteBuf.readLongArray(data.storage.getRaw());
            this.data = data;
        } finally {
            this.release();
        }
    }

    public void write(FriendlyByteBuf friendlyByteBuf) {
        this.acquire();
        try {
            this.data.write(friendlyByteBuf);
        } finally {
            this.release();
        }
    }

    private static <T> DataResult<PalettedContainer<T>> read(IdMap<T> idMap, Strategy strategy, DiscData<T> discData) {
        BitStorage bitStorage;
        List list = discData.paletteEntries();
        int i2 = strategy.size();
        int j = strategy.calculateBitsForSerialization(idMap, list.size());
        Configuration<T> configuration = strategy.getConfiguration(idMap, j);
        if (j == 0) {
            bitStorage = new ZeroBitStorage(i2);
        } else {
            Optional<LongStream> optional = discData.storage();
            if (optional.isEmpty()) {
                return DataResult.error("Missing values for non-zero storage");
            }
            long[] ls = optional.get().toArray();
            if (configuration.factory() == Strategy.GLOBAL_PALETTE_FACTORY) {
                HashMapPalette<Object> palette = new HashMapPalette<Object>(idMap, j, (i, object) -> 0, list);
                SimpleBitStorage simpleBitStorage = new SimpleBitStorage(j, strategy.size(), ls);
                IntStream intStream = IntStream.range(0, simpleBitStorage.getSize()).map(i -> idMap.getId(palette.valueFor(simpleBitStorage.get(i))));
                bitStorage = new SimpleBitStorage(configuration.bits(), i2, intStream);
            } else {
                bitStorage = new SimpleBitStorage(configuration.bits(), i2, ls);
            }
        }
        return DataResult.success(new PalettedContainer<T>(idMap, strategy, configuration, bitStorage, list));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private DiscData<T> write(IdMap<T> idMap, Strategy strategy) {
        this.acquire();
        try {
            Optional<LongStream> optional;
            int k;
            HashMapPalette<T> hashMapPalette = new HashMapPalette<T>(idMap, this.data.storage.getBits(), this.dummyPaletteResize);
            Object object = null;
            int i = -1;
            int j = strategy.size();
            int[] is = new int[j];
            for (k = 0; k < j; ++k) {
                T object2 = this.get(k);
                if (object2 != object) {
                    object = object2;
                    i = hashMapPalette.idFor(object2);
                }
                is[k] = i;
            }
            k = strategy.calculateBitsForSerialization(idMap, hashMapPalette.getSize());
            if (k != 0) {
                SimpleBitStorage bitStorage = new SimpleBitStorage(k, j);
                for (int l = 0; l < is.length; ++l) {
                    bitStorage.set(l, is[l]);
                }
                long[] ls = bitStorage.getRaw();
                optional = Optional.of(Arrays.stream(ls));
            } else {
                optional = Optional.empty();
            }
            DiscData discData = new DiscData(hashMapPalette.getEntries(), optional);
            return discData;
        } finally {
            this.release();
        }
    }

    public int getSerializedSize() {
        return this.data.getSerializedSize();
    }

    public boolean maybeHas(Predicate<T> predicate) {
        return this.data.palette.maybeHas(predicate);
    }

    public void count(CountConsumer<T> countConsumer) {
        Int2IntOpenHashMap int2IntMap = new Int2IntOpenHashMap();
        this.data.storage.getAll(i -> int2IntMap.put(i, int2IntMap.get(i) + 1));
        int2IntMap.int2IntEntrySet().forEach(entry -> countConsumer.accept(this.data.palette.valueFor(entry.getIntKey()), entry.getIntValue()));
    }

    public static abstract class Strategy {
        public static final Palette.Factory SINGLE_VALUE_PALETTE_FACTORY = SingleValuePalette::create;
        public static final Palette.Factory LINEAR_PALETTE_FACTORY = LinearPalette::create;
        public static final Palette.Factory HASHMAP_PALETTE_FACTORY = HashMapPalette::create;
        static final Palette.Factory GLOBAL_PALETTE_FACTORY = GlobalPalette::create;
        public static final Strategy SECTION_STATES = new Strategy(4){

            @Override
            public <A> Configuration<A> getConfiguration(IdMap<A> idMap, int i) {
                return switch (i) {
                    case 0 -> new Configuration(SINGLE_VALUE_PALETTE_FACTORY, i);
                    case 1, 2, 3, 4 -> new Configuration(LINEAR_PALETTE_FACTORY, 4);
                    case 5, 6, 7, 8 -> new Configuration(HASHMAP_PALETTE_FACTORY, i);
                    default -> new Configuration(GLOBAL_PALETTE_FACTORY, Mth.ceillog2(idMap.size()));
                };
            }
        };
        public static final Strategy SECTION_BIOMES = new Strategy(2){

            @Override
            public <A> Configuration<A> getConfiguration(IdMap<A> idMap, int i) {
                return switch (i) {
                    case 0 -> new Configuration(SINGLE_VALUE_PALETTE_FACTORY, i);
                    case 1, 2 -> new Configuration(LINEAR_PALETTE_FACTORY, i);
                    default -> new Configuration(GLOBAL_PALETTE_FACTORY, Mth.ceillog2(idMap.size()));
                };
            }
        };
        private final int sizeBits;

        Strategy(int i) {
            this.sizeBits = i;
        }

        public int size() {
            return 1 << this.sizeBits * 3;
        }

        public int getIndex(int i, int j, int k) {
            return (j << this.sizeBits | k) << this.sizeBits | i;
        }

        public abstract <A> Configuration<A> getConfiguration(IdMap<A> var1, int var2);

        <A> int calculateBitsForSerialization(IdMap<A> idMap, int i) {
            int j = Mth.ceillog2(i);
            Configuration<A> configuration = this.getConfiguration(idMap, j);
            return configuration.factory() == GLOBAL_PALETTE_FACTORY ? j : configuration.bits();
        }
    }

    record Configuration<T>(Palette.Factory factory, int bits) {
        public Data<T> createData(IdMap<T> idMap, PaletteResize<T> paletteResize, int i, @Nullable long[] ls) {
            BitStorage bitStorage = this.bits == 0 ? new ZeroBitStorage(i) : new SimpleBitStorage(this.bits, i, ls);
            Palette<T> palette = this.factory.create(this.bits, idMap, paletteResize);
            return new Data(this, bitStorage, palette);
        }
    }

    record Data(Configuration<T> configuration, BitStorage storage, Palette<T> palette) {
        public void copyFrom(Palette<T> palette, BitStorage bitStorage) {
            for (int i = 0; i < bitStorage.getSize(); ++i) {
                this.set(i, palette.valueFor(bitStorage.get(i)));
            }
        }

        public void set(int i, T object) {
            this.storage.set(i, this.palette.idFor(object));
        }

        public int getSerializedSize() {
            return 1 + this.palette.getSerializedSize() + FriendlyByteBuf.getVarIntSize(this.storage.getSize()) + this.storage.getRaw().length * 8;
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeByte(this.storage.getBits());
            this.palette.write(friendlyByteBuf);
            friendlyByteBuf.writeLongArray(this.storage.getRaw());
        }
    }

    record DiscData(List<T> paletteEntries, Optional<LongStream> storage) {
    }

    @FunctionalInterface
    public static interface CountConsumer<T> {
        public void accept(T var1, int var2);
    }
}

