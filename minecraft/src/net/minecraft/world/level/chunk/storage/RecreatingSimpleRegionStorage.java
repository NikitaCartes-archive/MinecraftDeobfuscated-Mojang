package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import org.apache.commons.io.FileUtils;

public class RecreatingSimpleRegionStorage extends SimpleRegionStorage {
	private final IOWorker writeWorker;
	private final Path writeFolder;

	public RecreatingSimpleRegionStorage(Path path, Path path2, DataFixer dataFixer, boolean bl, String string, DataFixTypes dataFixTypes) {
		super(path, dataFixer, bl, string, dataFixTypes);
		this.writeFolder = path2;
		this.writeWorker = new IOWorker(path2, bl, string + "-recreating");
	}

	@Override
	public CompletableFuture<Void> write(ChunkPos chunkPos, @Nullable CompoundTag compoundTag) {
		return this.writeWorker.store(chunkPos, compoundTag);
	}

	@Override
	public void close() throws IOException {
		super.close();
		this.writeWorker.close();
		if (this.writeFolder.toFile().exists()) {
			FileUtils.deleteDirectory(this.writeFolder.toFile());
		}
	}
}
