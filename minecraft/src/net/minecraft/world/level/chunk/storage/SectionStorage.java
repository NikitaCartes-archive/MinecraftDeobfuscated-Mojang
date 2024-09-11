package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import org.slf4j.Logger;

public class SectionStorage<R, P> implements AutoCloseable {
	static final Logger LOGGER = LogUtils.getLogger();
	private static final String SECTIONS_TAG = "Sections";
	private final SimpleRegionStorage simpleRegionStorage;
	private final Long2ObjectMap<Optional<R>> storage = new Long2ObjectOpenHashMap<>();
	private final LongLinkedOpenHashSet dirtyChunks = new LongLinkedOpenHashSet();
	private final Codec<P> codec;
	private final Function<R, P> packer;
	private final BiFunction<P, Runnable, R> unpacker;
	private final Function<Runnable, R> factory;
	private final RegistryAccess registryAccess;
	private final ChunkIOErrorReporter errorReporter;
	protected final LevelHeightAccessor levelHeightAccessor;
	private final LongSet loadedChunks = new LongOpenHashSet();
	private final Long2ObjectMap<CompletableFuture<Optional<SectionStorage.PackedChunk<P>>>> pendingLoads = new Long2ObjectOpenHashMap<>();
	private final Object loadLock = new Object();

	public SectionStorage(
		SimpleRegionStorage simpleRegionStorage,
		Codec<P> codec,
		Function<R, P> function,
		BiFunction<P, Runnable, R> biFunction,
		Function<Runnable, R> function2,
		RegistryAccess registryAccess,
		ChunkIOErrorReporter chunkIOErrorReporter,
		LevelHeightAccessor levelHeightAccessor
	) {
		this.simpleRegionStorage = simpleRegionStorage;
		this.codec = codec;
		this.packer = function;
		this.unpacker = biFunction;
		this.factory = function2;
		this.registryAccess = registryAccess;
		this.errorReporter = chunkIOErrorReporter;
		this.levelHeightAccessor = levelHeightAccessor;
	}

	protected void tick(BooleanSupplier booleanSupplier) {
		LongIterator longIterator = this.dirtyChunks.iterator();

		while (longIterator.hasNext() && booleanSupplier.getAsBoolean()) {
			ChunkPos chunkPos = new ChunkPos(longIterator.nextLong());
			longIterator.remove();
			this.writeChunk(chunkPos);
		}

		this.unpackPendingLoads();
	}

	private void unpackPendingLoads() {
		synchronized (this.loadLock) {
			Iterator<Entry<CompletableFuture<Optional<SectionStorage.PackedChunk<P>>>>> iterator = Long2ObjectMaps.fastIterator(this.pendingLoads);

			while (iterator.hasNext()) {
				Entry<CompletableFuture<Optional<SectionStorage.PackedChunk<P>>>> entry = (Entry<CompletableFuture<Optional<SectionStorage.PackedChunk<P>>>>)iterator.next();
				Optional<SectionStorage.PackedChunk<P>> optional = (Optional<SectionStorage.PackedChunk<P>>)((CompletableFuture)entry.getValue()).getNow(null);
				if (optional != null) {
					long l = entry.getLongKey();
					this.unpackChunk(new ChunkPos(l), (SectionStorage.PackedChunk<P>)optional.orElse(null));
					iterator.remove();
					this.loadedChunks.add(l);
				}
			}
		}
	}

	public void flushAll() {
		if (!this.dirtyChunks.isEmpty()) {
			this.dirtyChunks.forEach(l -> this.writeChunk(new ChunkPos(l)));
			this.dirtyChunks.clear();
		}
	}

	public boolean hasWork() {
		return !this.dirtyChunks.isEmpty();
	}

	@Nullable
	protected Optional<R> get(long l) {
		return this.storage.get(l);
	}

	protected Optional<R> getOrLoad(long l) {
		if (this.outsideStoredRange(l)) {
			return Optional.empty();
		} else {
			Optional<R> optional = this.get(l);
			if (optional != null) {
				return optional;
			} else {
				this.unpackChunk(SectionPos.of(l).chunk());
				optional = this.get(l);
				if (optional == null) {
					throw (IllegalStateException)Util.pauseInIde(new IllegalStateException());
				} else {
					return optional;
				}
			}
		}
	}

	protected boolean outsideStoredRange(long l) {
		int i = SectionPos.sectionToBlockCoord(SectionPos.y(l));
		return this.levelHeightAccessor.isOutsideBuildHeight(i);
	}

	protected R getOrCreate(long l) {
		if (this.outsideStoredRange(l)) {
			throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("sectionPos out of bounds"));
		} else {
			Optional<R> optional = this.getOrLoad(l);
			if (optional.isPresent()) {
				return (R)optional.get();
			} else {
				R object = (R)this.factory.apply((Runnable)() -> this.setDirty(l));
				this.storage.put(l, Optional.of(object));
				return object;
			}
		}
	}

	public CompletableFuture<?> prefetch(ChunkPos chunkPos) {
		synchronized (this.loadLock) {
			long l = chunkPos.toLong();
			return this.loadedChunks.contains(l)
				? CompletableFuture.completedFuture(null)
				: this.pendingLoads
					.computeIfAbsent(l, (Long2ObjectFunction<? extends CompletableFuture<Optional<SectionStorage.PackedChunk<P>>>>)(lx -> this.tryRead(chunkPos)));
		}
	}

	private void unpackChunk(ChunkPos chunkPos) {
		long l = chunkPos.toLong();
		CompletableFuture<Optional<SectionStorage.PackedChunk<P>>> completableFuture;
		synchronized (this.loadLock) {
			if (!this.loadedChunks.add(l)) {
				return;
			}

			completableFuture = this.pendingLoads
				.computeIfAbsent(l, (Long2ObjectFunction<? extends CompletableFuture<Optional<SectionStorage.PackedChunk<P>>>>)(lx -> this.tryRead(chunkPos)));
		}

		this.unpackChunk(chunkPos, (SectionStorage.PackedChunk<P>)((Optional)completableFuture.join()).orElse(null));
		synchronized (this.loadLock) {
			this.pendingLoads.remove(l);
		}
	}

	private CompletableFuture<Optional<SectionStorage.PackedChunk<P>>> tryRead(ChunkPos chunkPos) {
		RegistryOps<Tag> registryOps = this.registryAccess.createSerializationContext(NbtOps.INSTANCE);
		return this.simpleRegionStorage
			.read(chunkPos)
			.thenApplyAsync(
				optional -> optional.map(
						compoundTag -> SectionStorage.PackedChunk.parse(this.codec, registryOps, compoundTag, this.simpleRegionStorage, this.levelHeightAccessor)
					),
				Util.backgroundExecutor().forName("parseSection")
			)
			.exceptionally(throwable -> {
				if (throwable instanceof IOException iOException) {
					LOGGER.error("Error reading chunk {} data from disk", chunkPos, iOException);
					this.errorReporter.reportChunkLoadFailure(iOException, this.simpleRegionStorage.storageInfo(), chunkPos);
					return Optional.empty();
				} else {
					throw new CompletionException(throwable);
				}
			});
	}

	private void unpackChunk(ChunkPos chunkPos, @Nullable SectionStorage.PackedChunk<P> packedChunk) {
		if (packedChunk == null) {
			for (int i = this.levelHeightAccessor.getMinSectionY(); i <= this.levelHeightAccessor.getMaxSectionY(); i++) {
				this.storage.put(getKey(chunkPos, i), Optional.empty());
			}
		} else {
			boolean bl = packedChunk.versionChanged();

			for (int j = this.levelHeightAccessor.getMinSectionY(); j <= this.levelHeightAccessor.getMaxSectionY(); j++) {
				long l = getKey(chunkPos, j);
				Optional<R> optional = Optional.ofNullable(packedChunk.sectionsByY.get(j)).map(object -> this.unpacker.apply(object, (Runnable)() -> this.setDirty(l)));
				this.storage.put(l, optional);
				optional.ifPresent(object -> {
					this.onSectionLoad(l);
					if (bl) {
						this.setDirty(l);
					}
				});
			}
		}
	}

	private void writeChunk(ChunkPos chunkPos) {
		RegistryOps<Tag> registryOps = this.registryAccess.createSerializationContext(NbtOps.INSTANCE);
		Dynamic<Tag> dynamic = this.writeChunk(chunkPos, registryOps);
		Tag tag = dynamic.getValue();
		if (tag instanceof CompoundTag) {
			this.simpleRegionStorage.write(chunkPos, (CompoundTag)tag).exceptionally(throwable -> {
				this.errorReporter.reportChunkSaveFailure(throwable, this.simpleRegionStorage.storageInfo(), chunkPos);
				return null;
			});
		} else {
			LOGGER.error("Expected compound tag, got {}", tag);
		}
	}

	private <T> Dynamic<T> writeChunk(ChunkPos chunkPos, DynamicOps<T> dynamicOps) {
		Map<T, T> map = Maps.<T, T>newHashMap();

		for (int i = this.levelHeightAccessor.getMinSectionY(); i <= this.levelHeightAccessor.getMaxSectionY(); i++) {
			long l = getKey(chunkPos, i);
			Optional<R> optional = this.storage.get(l);
			if (optional != null && !optional.isEmpty()) {
				DataResult<T> dataResult = this.codec.encodeStart(dynamicOps, (P)this.packer.apply(optional.get()));
				String string = Integer.toString(i);
				dataResult.resultOrPartial(LOGGER::error).ifPresent(object -> map.put(dynamicOps.createString(string), object));
			}
		}

		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("Sections"),
					dynamicOps.createMap(map),
					dynamicOps.createString("DataVersion"),
					dynamicOps.createInt(SharedConstants.getCurrentVersion().getDataVersion().getVersion())
				)
			)
		);
	}

	private static long getKey(ChunkPos chunkPos, int i) {
		return SectionPos.asLong(chunkPos.x, i, chunkPos.z);
	}

	protected void onSectionLoad(long l) {
	}

	protected void setDirty(long l) {
		Optional<R> optional = this.storage.get(l);
		if (optional != null && !optional.isEmpty()) {
			this.dirtyChunks.add(ChunkPos.asLong(SectionPos.x(l), SectionPos.z(l)));
		} else {
			LOGGER.warn("No data for position: {}", SectionPos.of(l));
		}
	}

	static int getVersion(Dynamic<?> dynamic) {
		return dynamic.get("DataVersion").asInt(1945);
	}

	public void flush(ChunkPos chunkPos) {
		if (this.dirtyChunks.remove(chunkPos.toLong())) {
			this.writeChunk(chunkPos);
		}
	}

	public void close() throws IOException {
		this.simpleRegionStorage.close();
	}

	static record PackedChunk<T>(Int2ObjectMap<T> sectionsByY, boolean versionChanged) {

		public static <T> SectionStorage.PackedChunk<T> parse(
			Codec<T> codec, DynamicOps<Tag> dynamicOps, Tag tag, SimpleRegionStorage simpleRegionStorage, LevelHeightAccessor levelHeightAccessor
		) {
			Dynamic<Tag> dynamic = new Dynamic<>(dynamicOps, tag);
			int i = SectionStorage.getVersion(dynamic);
			int j = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
			boolean bl = i != j;
			Dynamic<Tag> dynamic2 = simpleRegionStorage.upgradeChunkTag(dynamic, i);
			OptionalDynamic<Tag> optionalDynamic = dynamic2.get("Sections");
			Int2ObjectMap<T> int2ObjectMap = new Int2ObjectOpenHashMap<>();

			for (int k = levelHeightAccessor.getMinSectionY(); k <= levelHeightAccessor.getMaxSectionY(); k++) {
				Optional<T> optional = optionalDynamic.get(Integer.toString(k))
					.result()
					.flatMap(dynamicx -> codec.parse(dynamicx).resultOrPartial(SectionStorage.LOGGER::error));
				if (optional.isPresent()) {
					int2ObjectMap.put(k, (T)optional.get());
				}
			}

			return new SectionStorage.PackedChunk<>(int2ObjectMap, bl);
		}
	}
}
