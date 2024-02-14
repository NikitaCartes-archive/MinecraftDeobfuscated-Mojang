package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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

public class SectionStorage<R> implements AutoCloseable {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String SECTIONS_TAG = "Sections";
	private final SimpleRegionStorage simpleRegionStorage;
	private final Long2ObjectMap<Optional<R>> storage = new Long2ObjectOpenHashMap<>();
	private final LongLinkedOpenHashSet dirty = new LongLinkedOpenHashSet();
	private final Function<Runnable, Codec<R>> codec;
	private final Function<Runnable, R> factory;
	private final RegistryAccess registryAccess;
	protected final LevelHeightAccessor levelHeightAccessor;

	public SectionStorage(
		SimpleRegionStorage simpleRegionStorage,
		Function<Runnable, Codec<R>> function,
		Function<Runnable, R> function2,
		RegistryAccess registryAccess,
		LevelHeightAccessor levelHeightAccessor
	) {
		this.simpleRegionStorage = simpleRegionStorage;
		this.codec = function;
		this.factory = function2;
		this.registryAccess = registryAccess;
		this.levelHeightAccessor = levelHeightAccessor;
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
				this.readColumn(SectionPos.of(l).chunk());
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

	private void readColumn(ChunkPos chunkPos) {
		Optional<CompoundTag> optional = (Optional<CompoundTag>)this.tryRead(chunkPos).join();
		RegistryOps<Tag> registryOps = this.registryAccess.createSerializationContext(NbtOps.INSTANCE);
		this.readColumn(chunkPos, registryOps, (CompoundTag)optional.orElse(null));
	}

	private CompletableFuture<Optional<CompoundTag>> tryRead(ChunkPos chunkPos) {
		return this.simpleRegionStorage.read(chunkPos).exceptionally(throwable -> {
			if (throwable instanceof IOException iOException) {
				LOGGER.error("Error reading chunk {} data from disk", chunkPos, iOException);
				return Optional.empty();
			} else {
				throw new CompletionException(throwable);
			}
		});
	}

	private void readColumn(ChunkPos chunkPos, RegistryOps<Tag> registryOps, @Nullable CompoundTag compoundTag) {
		if (compoundTag == null) {
			for (int i = this.levelHeightAccessor.getMinSection(); i < this.levelHeightAccessor.getMaxSection(); i++) {
				this.storage.put(getKey(chunkPos, i), Optional.empty());
			}
		} else {
			Dynamic<Tag> dynamic = new Dynamic<>(registryOps, compoundTag);
			int j = getVersion(dynamic);
			int k = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
			boolean bl = j != k;
			Dynamic<Tag> dynamic2 = this.simpleRegionStorage.upgradeChunkTag(dynamic, j);
			OptionalDynamic<Tag> optionalDynamic = dynamic2.get("Sections");

			for (int l = this.levelHeightAccessor.getMinSection(); l < this.levelHeightAccessor.getMaxSection(); l++) {
				long m = getKey(chunkPos, l);
				Optional<R> optional = optionalDynamic.get(Integer.toString(l))
					.result()
					.flatMap(dynamicx -> ((Codec)this.codec.apply((Runnable)() -> this.setDirty(m))).parse(dynamicx).resultOrPartial(LOGGER::error));
				this.storage.put(m, optional);
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
		RegistryOps<Tag> registryOps = this.registryAccess.createSerializationContext(NbtOps.INSTANCE);
		Dynamic<Tag> dynamic = this.writeColumn(chunkPos, registryOps);
		Tag tag = dynamic.getValue();
		if (tag instanceof CompoundTag) {
			this.simpleRegionStorage.write(chunkPos, (CompoundTag)tag);
		} else {
			LOGGER.error("Expected compound tag, got {}", tag);
		}
	}

	private <T> Dynamic<T> writeColumn(ChunkPos chunkPos, DynamicOps<T> dynamicOps) {
		Map<T, T> map = Maps.<T, T>newHashMap();

		for (int i = this.levelHeightAccessor.getMinSection(); i < this.levelHeightAccessor.getMaxSection(); i++) {
			long l = getKey(chunkPos, i);
			this.dirty.remove(l);
			Optional<R> optional = this.storage.get(l);
			if (optional != null && !optional.isEmpty()) {
				DataResult<T> dataResult = ((Codec)this.codec.apply((Runnable)() -> this.setDirty(l))).encodeStart(dynamicOps, optional.get());
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
			this.dirty.add(l);
		} else {
			LOGGER.warn("No data for position: {}", SectionPos.of(l));
		}
	}

	private static int getVersion(Dynamic<?> dynamic) {
		return dynamic.get("DataVersion").asInt(1945);
	}

	public void flush(ChunkPos chunkPos) {
		if (this.hasWork()) {
			for (int i = this.levelHeightAccessor.getMinSection(); i < this.levelHeightAccessor.getMaxSection(); i++) {
				long l = getKey(chunkPos, i);
				if (this.dirty.contains(l)) {
					this.writeColumn(chunkPos);
					return;
				}
			}
		}
	}

	public void close() throws IOException {
		this.simpleRegionStorage.close();
	}
}
