/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.storage.IOWorker;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SectionStorage<R>
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String SECTIONS_TAG = "Sections";
    private final IOWorker worker;
    private final Long2ObjectMap<Optional<R>> storage = new Long2ObjectOpenHashMap<Optional<R>>();
    private final LongLinkedOpenHashSet dirty = new LongLinkedOpenHashSet();
    private final Function<Runnable, Codec<R>> codec;
    private final Function<Runnable, R> factory;
    private final DataFixer fixerUpper;
    private final DataFixTypes type;
    private final RegistryAccess registryAccess;
    protected final LevelHeightAccessor levelHeightAccessor;

    public SectionStorage(Path path, Function<Runnable, Codec<R>> function, Function<Runnable, R> function2, DataFixer dataFixer, DataFixTypes dataFixTypes, boolean bl, RegistryAccess registryAccess, LevelHeightAccessor levelHeightAccessor) {
        this.codec = function;
        this.factory = function2;
        this.fixerUpper = dataFixer;
        this.type = dataFixTypes;
        this.registryAccess = registryAccess;
        this.levelHeightAccessor = levelHeightAccessor;
        this.worker = new IOWorker(path, bl, path.getFileName().toString());
    }

    protected void tick(BooleanSupplier booleanSupplier) {
        while (this.hasWork() && booleanSupplier.getAsBoolean()) {
            ChunkPos chunkPos = SectionPos.of(this.dirty.firstLong()).chunk();
            this.writeColumn(chunkPos);
        }
    }

    public boolean hasWork() {
        return !this.dirty.isEmpty();
    }

    @Nullable
    protected Optional<R> get(long l) {
        return (Optional)this.storage.get(l);
    }

    protected Optional<R> getOrLoad(long l) {
        if (this.outsideStoredRange(l)) {
            return Optional.empty();
        }
        Optional<R> optional = this.get(l);
        if (optional != null) {
            return optional;
        }
        this.readColumn(SectionPos.of(l).chunk());
        optional = this.get(l);
        if (optional == null) {
            throw Util.pauseInIde(new IllegalStateException());
        }
        return optional;
    }

    protected boolean outsideStoredRange(long l) {
        int i = SectionPos.sectionToBlockCoord(SectionPos.y(l));
        return this.levelHeightAccessor.isOutsideBuildHeight(i);
    }

    protected R getOrCreate(long l) {
        if (this.outsideStoredRange(l)) {
            throw Util.pauseInIde(new IllegalArgumentException("sectionPos out of bounds"));
        }
        Optional<R> optional = this.getOrLoad(l);
        if (optional.isPresent()) {
            return optional.get();
        }
        R object = this.factory.apply(() -> this.setDirty(l));
        this.storage.put(l, Optional.of(object));
        return object;
    }

    private void readColumn(ChunkPos chunkPos) {
        Optional<CompoundTag> optional = this.tryRead(chunkPos).join();
        RegistryOps<Tag> registryOps = RegistryOps.create(NbtOps.INSTANCE, this.registryAccess);
        this.readColumn(chunkPos, registryOps, optional.orElse(null));
    }

    private CompletableFuture<Optional<CompoundTag>> tryRead(ChunkPos chunkPos) {
        return this.worker.loadAsync(chunkPos).exceptionally(throwable -> {
            if (throwable instanceof IOException) {
                IOException iOException = (IOException)throwable;
                LOGGER.error("Error reading chunk {} data from disk", (Object)chunkPos, (Object)iOException);
                return Optional.empty();
            }
            throw new CompletionException((Throwable)throwable);
        });
    }

    private <T> void readColumn(ChunkPos chunkPos, DynamicOps<T> dynamicOps, @Nullable T object2) {
        if (object2 == null) {
            for (int i = this.levelHeightAccessor.getMinSection(); i < this.levelHeightAccessor.getMaxSection(); ++i) {
                this.storage.put(SectionStorage.getKey(chunkPos, i), (Optional<R>)Optional.empty());
            }
        } else {
            int k;
            Dynamic<T> dynamic2 = new Dynamic<T>(dynamicOps, object2);
            int j = SectionStorage.getVersion(dynamic2);
            boolean bl = j != (k = SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            Dynamic<T> dynamic22 = this.type.update(this.fixerUpper, dynamic2, j, k);
            OptionalDynamic<T> optionalDynamic = dynamic22.get(SECTIONS_TAG);
            for (int l = this.levelHeightAccessor.getMinSection(); l < this.levelHeightAccessor.getMaxSection(); ++l) {
                long m = SectionStorage.getKey(chunkPos, l);
                Optional optional = optionalDynamic.get(Integer.toString(l)).result().flatMap(dynamic -> this.codec.apply(() -> this.setDirty(m)).parse(dynamic).resultOrPartial(LOGGER::error));
                this.storage.put(m, (Optional<R>)optional);
                optional.ifPresent(object -> {
                    this.onSectionLoad(m);
                    if (bl) {
                        this.setDirty(m);
                    }
                });
            }
        }
    }

    private void writeColumn(ChunkPos chunkPos) {
        RegistryOps<Tag> registryOps = RegistryOps.create(NbtOps.INSTANCE, this.registryAccess);
        Dynamic<Tag> dynamic = this.writeColumn(chunkPos, registryOps);
        Tag tag = dynamic.getValue();
        if (tag instanceof CompoundTag) {
            this.worker.store(chunkPos, (CompoundTag)tag);
        } else {
            LOGGER.error("Expected compound tag, got {}", (Object)tag);
        }
    }

    private <T> Dynamic<T> writeColumn(ChunkPos chunkPos, DynamicOps<T> dynamicOps) {
        HashMap map = Maps.newHashMap();
        for (int i = this.levelHeightAccessor.getMinSection(); i < this.levelHeightAccessor.getMaxSection(); ++i) {
            long l = SectionStorage.getKey(chunkPos, i);
            this.dirty.remove(l);
            Optional optional = (Optional)this.storage.get(l);
            if (optional == null || !optional.isPresent()) continue;
            DataResult<T> dataResult = this.codec.apply(() -> this.setDirty(l)).encodeStart(dynamicOps, optional.get());
            String string = Integer.toString(i);
            dataResult.resultOrPartial(LOGGER::error).ifPresent(object -> map.put(dynamicOps.createString(string), object));
        }
        return new Dynamic<T>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString(SECTIONS_TAG), dynamicOps.createMap(map), dynamicOps.createString("DataVersion"), dynamicOps.createInt(SharedConstants.getCurrentVersion().getDataVersion().getVersion()))));
    }

    private static long getKey(ChunkPos chunkPos, int i) {
        return SectionPos.asLong(chunkPos.x, i, chunkPos.z);
    }

    protected void onSectionLoad(long l) {
    }

    protected void setDirty(long l) {
        Optional optional = (Optional)this.storage.get(l);
        if (optional == null || !optional.isPresent()) {
            LOGGER.warn("No data for position: {}", (Object)SectionPos.of(l));
            return;
        }
        this.dirty.add(l);
    }

    private static int getVersion(Dynamic<?> dynamic) {
        return dynamic.get("DataVersion").asInt(1945);
    }

    public void flush(ChunkPos chunkPos) {
        if (this.hasWork()) {
            for (int i = this.levelHeightAccessor.getMinSection(); i < this.levelHeightAccessor.getMaxSection(); ++i) {
                long l = SectionStorage.getKey(chunkPos, i);
                if (!this.dirty.contains(l)) continue;
                this.writeColumn(chunkPos);
                return;
            }
        }
    }

    @Override
    public void close() throws IOException {
        this.worker.close();
    }
}

