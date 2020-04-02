package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class ChunkStorage implements AutoCloseable {
	private final IOWorker worker;
	protected final DataFixer fixerUpper;
	@Nullable
	private LegacyStructureDataHandler legacyStructureHandler;

	public ChunkStorage(File file, DataFixer dataFixer, boolean bl) {
		this.fixerUpper = dataFixer;
		this.worker = new IOWorker(new RegionFileStorage(file, bl), "chunk");
	}

	public CompoundTag upgradeChunkTag(DimensionType dimensionType, Supplier<DimensionDataStorage> supplier, CompoundTag compoundTag) {
		int i = getVersion(compoundTag);
		int j = 1493;
		if (i < 1493) {
			compoundTag = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, compoundTag, i, 1493);
			if (compoundTag.getCompound("Level").getBoolean("hasLegacyStructureData")) {
				if (this.legacyStructureHandler == null) {
					this.legacyStructureHandler = LegacyStructureDataHandler.getLegacyStructureHandler(dimensionType, (DimensionDataStorage)supplier.get());
				}

				compoundTag = this.legacyStructureHandler.updateFromLegacy(compoundTag);
			}
		}

		compoundTag = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, compoundTag, Math.max(1493, i));
		if (i < SharedConstants.getCurrentVersion().getWorldVersion()) {
			compoundTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
		}

		return compoundTag;
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
		this.worker.synchronize().join();
	}

	public void close() throws IOException {
		this.worker.close();
	}
}
