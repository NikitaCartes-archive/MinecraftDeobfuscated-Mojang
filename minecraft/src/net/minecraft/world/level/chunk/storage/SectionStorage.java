package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SectionStorage<R> implements AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	private final IOWorker worker;
	private final Long2ObjectMap<Optional<R>> storage = new Long2ObjectOpenHashMap<>();
	private final LongLinkedOpenHashSet dirty = new LongLinkedOpenHashSet();
	private final Function<Runnable, Codec<R>> codec;
	private final Function<Runnable, R> factory;
	private final DataFixer fixerUpper;
	private final DataFixTypes type;
	protected final LevelHeightAccessor levelHeightAccessor;

	public SectionStorage(
		File file,
		Function<Runnable, Codec<R>> function,
		Function<Runnable, R> function2,
		DataFixer dataFixer,
		DataFixTypes dataFixTypes,
		boolean bl,
		LevelHeightAccessor levelHeightAccessor
	) {
		this.codec = function;
		this.factory = function2;
		this.fixerUpper = dataFixer;
		this.type = dataFixTypes;
		this.levelHeightAccessor = levelHeightAccessor;
		this.worker = new IOWorker(file, bl, file.getName());
	}

	protected void tick(BooleanSupplier booleanSupplier) {
		while (!this.dirty.isEmpty() && booleanSupplier.getAsBoolean()) {
			ChunkPos chunkPos = SectionPos.of(this.dirty.firstLong()).chunk();
			this.writeColumn(chunkPos);
		}
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
		this.readColumn(chunkPos, NbtOps.INSTANCE, this.tryRead(chunkPos));
	}

	@Nullable
	private CompoundTag tryRead(ChunkPos chunkPos) {
		try {
			return this.worker.load(chunkPos);
		} catch (IOException var3) {
			LOGGER.error("Error reading chunk {} data from disk", chunkPos, var3);
			return null;
		}
	}

	private <T> void readColumn(ChunkPos chunkPos, DynamicOps<T> dynamicOps, @Nullable T object) {
		if (object == null) {
			for (int i = this.levelHeightAccessor.getMinSection(); i < this.levelHeightAccessor.getMaxSection(); i++) {
				this.storage.put(getKey(chunkPos, i), Optional.empty());
			}
		} else {
			Dynamic<T> dynamic = new Dynamic<>(dynamicOps, object);
			int j = getVersion(dynamic);
			int k = SharedConstants.getCurrentVersion().getWorldVersion();
			boolean bl = j != k;
			Dynamic<T> dynamic2 = this.fixerUpper.update(this.type.getType(), dynamic, j, k);
			OptionalDynamic<T> optionalDynamic = dynamic2.get("Sections");

			for (int l = this.levelHeightAccessor.getMinSection(); l < this.levelHeightAccessor.getMaxSection(); l++) {
				long m = getKey(chunkPos, l);
				Optional<R> optional = optionalDynamic.get(Integer.toString(l))
					.result()
					.flatMap(dynamicx -> ((Codec)this.codec.apply((Runnable)() -> this.setDirty(m))).parse(dynamicx).resultOrPartial(LOGGER::error));
				this.storage.put(m, optional);
				optional.ifPresent(objectx -> {
					this.onSectionLoad(m);
					if (bl) {
						this.setDirty(m);
					}
				});
			}
		}
	}

	private void writeColumn(ChunkPos chunkPos) {
		Dynamic<Tag> dynamic = this.writeColumn(chunkPos, NbtOps.INSTANCE);
		Tag tag = dynamic.getValue();
		if (tag instanceof CompoundTag) {
			this.worker.store(chunkPos, (CompoundTag)tag);
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
			if (optional != null && optional.isPresent()) {
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
					dynamicOps.createInt(SharedConstants.getCurrentVersion().getWorldVersion())
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
		if (optional != null && optional.isPresent()) {
			this.dirty.add(l);
		} else {
			LOGGER.warn("No data for position: {}", SectionPos.of(l));
		}
	}

	private static int getVersion(Dynamic<?> dynamic) {
		return dynamic.get("DataVersion").asInt(1945);
	}

	public void flush(ChunkPos chunkPos) {
		if (!this.dirty.isEmpty()) {
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
		this.worker.close();
	}
}
