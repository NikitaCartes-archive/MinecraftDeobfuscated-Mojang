package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;

public class SimpleRegionStorage implements AutoCloseable {
	private final IOWorker worker;
	private final DataFixer fixerUpper;
	private final DataFixTypes dataFixType;

	public SimpleRegionStorage(RegionStorageInfo regionStorageInfo, Path path, DataFixer dataFixer, boolean bl, DataFixTypes dataFixTypes) {
		this.fixerUpper = dataFixer;
		this.dataFixType = dataFixTypes;
		this.worker = new IOWorker(regionStorageInfo, path, bl);
	}

	public CompletableFuture<Optional<CompoundTag>> read(ChunkPos chunkPos) {
		return this.worker.loadAsync(chunkPos);
	}

	public CompletableFuture<Void> write(ChunkPos chunkPos, @Nullable CompoundTag compoundTag) {
		return this.worker.store(chunkPos, compoundTag);
	}

	public CompoundTag upgradeChunkTag(CompoundTag compoundTag, int i) {
		int j = NbtUtils.getDataVersion(compoundTag, i);
		return this.dataFixType.updateToCurrentVersion(this.fixerUpper, compoundTag, j);
	}

	public Dynamic<Tag> upgradeChunkTag(Dynamic<Tag> dynamic, int i) {
		return this.dataFixType.updateToCurrentVersion(this.fixerUpper, dynamic, i);
	}

	public CompletableFuture<Void> synchronize(boolean bl) {
		return this.worker.synchronize(bl);
	}

	public void close() throws IOException {
		this.worker.close();
	}
}
