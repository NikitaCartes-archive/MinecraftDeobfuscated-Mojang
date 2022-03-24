package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
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
	private LegacyStructureDataHandler legacyStructureHandler;

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
			compoundTag = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, compoundTag, i, 1493);
			if (compoundTag.getCompound("Level").getBoolean("hasLegacyStructureData")) {
				if (this.legacyStructureHandler == null) {
					this.legacyStructureHandler = LegacyStructureDataHandler.getLegacyStructureHandler(resourceKey, (DimensionDataStorage)supplier.get());
				}

				compoundTag = this.legacyStructureHandler.updateFromLegacy(compoundTag);
			}
		}

		injectDatafixingContext(compoundTag, resourceKey, optional);
		compoundTag = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, compoundTag, Math.max(1493, i));
		if (i < SharedConstants.getCurrentVersion().getWorldVersion()) {
			compoundTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
		}

		compoundTag.remove("__context");
		return compoundTag;
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
		return compoundTag.contains("DataVersion", 99) ? compoundTag.getInt("DataVersion") : -1;
	}

	@Nullable
	public CompoundTag read(ChunkPos chunkPos) throws IOException {
		return this.worker.load(chunkPos);
	}

	public void write(ChunkPos chunkPos, CompoundTag compoundTag) {
		this.worker.store(chunkPos, compoundTag);
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
