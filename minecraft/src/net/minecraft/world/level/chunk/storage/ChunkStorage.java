package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class ChunkStorage implements AutoCloseable {
	public static final int LAST_MONOLYTH_STRUCTURE_DATA_VERSION = 1493;
	private final IOWorker worker;
	protected final DataFixer fixerUpper;
	@Nullable
	private volatile LegacyStructureDataHandler legacyStructureHandler;

	public ChunkStorage(Path path, DataFixer dataFixer, boolean bl) {
		this.fixerUpper = dataFixer;
		this.worker = new IOWorker(path, bl, "chunk");
	}

	public boolean isOldChunkAround(ChunkPos chunkPos, int i) {
		return this.worker.isOldChunkAround(chunkPos, i);
	}

	public CompoundTag upgradeChunkTag(
		ResourceKey<Level> resourceKey,
		Supplier<DimensionDataStorage> supplier,
		CompoundTag compoundTag,
		Optional<ResourceKey<Codec<? extends ChunkGenerator>>> optional
	) {
		int i = getVersion(compoundTag);
		if (i < 1493) {
			compoundTag = DataFixTypes.CHUNK.update(this.fixerUpper, compoundTag, i, 1493);
			if (compoundTag.getCompound("Level").getBoolean("hasLegacyStructureData")) {
				LegacyStructureDataHandler legacyStructureDataHandler = this.getLegacyStructureHandler(resourceKey, supplier);
				compoundTag = legacyStructureDataHandler.updateFromLegacy(compoundTag);
			}
		}

		injectDatafixingContext(compoundTag, resourceKey, optional);
		compoundTag = DataFixTypes.CHUNK.updateToCurrentVersion(this.fixerUpper, compoundTag, Math.max(1493, i));
		if (i < SharedConstants.getCurrentVersion().getDataVersion().getVersion()) {
			NbtUtils.addCurrentDataVersion(compoundTag);
		}

		compoundTag.remove("__context");
		return compoundTag;
	}

	private LegacyStructureDataHandler getLegacyStructureHandler(ResourceKey<Level> resourceKey, Supplier<DimensionDataStorage> supplier) {
		LegacyStructureDataHandler legacyStructureDataHandler = this.legacyStructureHandler;
		if (legacyStructureDataHandler == null) {
			synchronized (this) {
				legacyStructureDataHandler = this.legacyStructureHandler;
				if (legacyStructureDataHandler == null) {
					this.legacyStructureHandler = legacyStructureDataHandler = LegacyStructureDataHandler.getLegacyStructureHandler(
						resourceKey, (DimensionDataStorage)supplier.get()
					);
				}
			}
		}

		return legacyStructureDataHandler;
	}

	public static void injectDatafixingContext(
		CompoundTag compoundTag, ResourceKey<Level> resourceKey, Optional<ResourceKey<Codec<? extends ChunkGenerator>>> optional
	) {
		CompoundTag compoundTag2 = new CompoundTag();
		compoundTag2.putString("dimension", resourceKey.location().toString());
		optional.ifPresent(resourceKeyx -> compoundTag2.putString("generator", resourceKeyx.location().toString()));
		compoundTag.put("__context", compoundTag2);
	}

	public static int getVersion(CompoundTag compoundTag) {
		return NbtUtils.getDataVersion(compoundTag, -1);
	}

	public CompletableFuture<Optional<CompoundTag>> read(ChunkPos chunkPos) {
		return this.worker.loadAsync(chunkPos);
	}

	public CompletableFuture<Void> write(ChunkPos chunkPos, CompoundTag compoundTag) {
		this.handleLegacyStructureIndex(chunkPos);
		return this.worker.store(chunkPos, compoundTag);
	}

	protected void handleLegacyStructureIndex(ChunkPos chunkPos) {
		if (this.legacyStructureHandler != null) {
			this.legacyStructureHandler.removeIndex(chunkPos.toLong());
		}
	}

	public void flushWorker() {
		this.worker.synchronize(true).join();
	}

	public void close() throws IOException {
		this.worker.close();
	}

	public ChunkScanAccess chunkScanner() {
		return this.worker;
	}
}
