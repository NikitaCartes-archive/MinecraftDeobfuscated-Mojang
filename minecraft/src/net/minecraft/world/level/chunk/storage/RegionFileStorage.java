package net.minecraft.world.level.chunk.storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.util.ExceptionCollector;
import net.minecraft.world.level.ChunkPos;

public final class RegionFileStorage implements AutoCloseable {
	public static final String ANVIL_EXTENSION = ".mca";
	private static final int MAX_CACHE_SIZE = 256;
	private final Long2ObjectLinkedOpenHashMap<RegionFile> regionCache = new Long2ObjectLinkedOpenHashMap<>();
	private final Path folder;
	private final boolean sync;

	RegionFileStorage(Path path, boolean bl) {
		this.folder = path;
		this.sync = bl;
	}

	private RegionFile getRegionFile(ChunkPos chunkPos) throws IOException {
		long l = ChunkPos.asLong(chunkPos.getRegionX(), chunkPos.getRegionZ());
		RegionFile regionFile = this.regionCache.getAndMoveToFirst(l);
		if (regionFile != null) {
			return regionFile;
		} else {
			if (this.regionCache.size() >= 256) {
				this.regionCache.removeLast().close();
			}

			FileUtil.createDirectoriesSafe(this.folder);
			Path path = this.folder.resolve("r." + chunkPos.getRegionX() + "." + chunkPos.getRegionZ() + ".mca");
			RegionFile regionFile2 = new RegionFile(path, this.folder, this.sync);
			this.regionCache.putAndMoveToFirst(l, regionFile2);
			return regionFile2;
		}
	}

	@Nullable
	public CompoundTag read(ChunkPos chunkPos) throws IOException {
		RegionFile regionFile = this.getRegionFile(chunkPos);
		DataInputStream dataInputStream = regionFile.getChunkDataInputStream(chunkPos);

		CompoundTag var8;
		label43: {
			try {
				if (dataInputStream == null) {
					var8 = null;
					break label43;
				}

				var8 = NbtIo.read(dataInputStream);
			} catch (Throwable var7) {
				if (dataInputStream != null) {
					try {
						dataInputStream.close();
					} catch (Throwable var6) {
						var7.addSuppressed(var6);
					}
				}

				throw var7;
			}

			if (dataInputStream != null) {
				dataInputStream.close();
			}

			return var8;
		}

		if (dataInputStream != null) {
			dataInputStream.close();
		}

		return var8;
	}

	public void scanChunk(ChunkPos chunkPos, StreamTagVisitor streamTagVisitor) throws IOException {
		RegionFile regionFile = this.getRegionFile(chunkPos);
		DataInputStream dataInputStream = regionFile.getChunkDataInputStream(chunkPos);

		try {
			if (dataInputStream != null) {
				NbtIo.parse(dataInputStream, streamTagVisitor, NbtAccounter.unlimitedHeap());
			}
		} catch (Throwable var8) {
			if (dataInputStream != null) {
				try {
					dataInputStream.close();
				} catch (Throwable var7) {
					var8.addSuppressed(var7);
				}
			}

			throw var8;
		}

		if (dataInputStream != null) {
			dataInputStream.close();
		}
	}

	protected void write(ChunkPos chunkPos, @Nullable CompoundTag compoundTag) throws IOException {
		RegionFile regionFile = this.getRegionFile(chunkPos);
		if (compoundTag == null) {
			regionFile.clear(chunkPos);
		} else {
			DataOutputStream dataOutputStream = regionFile.getChunkDataOutputStream(chunkPos);

			try {
				NbtIo.write(compoundTag, dataOutputStream);
			} catch (Throwable var8) {
				if (dataOutputStream != null) {
					try {
						dataOutputStream.close();
					} catch (Throwable var7) {
						var8.addSuppressed(var7);
					}
				}

				throw var8;
			}

			if (dataOutputStream != null) {
				dataOutputStream.close();
			}
		}
	}

	public void close() throws IOException {
		ExceptionCollector<IOException> exceptionCollector = new ExceptionCollector();

		for (RegionFile regionFile : this.regionCache.values()) {
			try {
				regionFile.close();
			} catch (IOException var5) {
				exceptionCollector.add(var5);
			}
		}

		exceptionCollector.throwIfPresent();
	}

	public void flush() throws IOException {
		for (RegionFile regionFile : this.regionCache.values()) {
			regionFile.flush();
		}
	}
}
