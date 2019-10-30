package net.minecraft.world.level.chunk.storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.ChunkPos;

public final class RegionFileStorage implements AutoCloseable {
	private final Long2ObjectLinkedOpenHashMap<RegionFile> regionCache = new Long2ObjectLinkedOpenHashMap<>();
	private final File folder;

	RegionFileStorage(File file) {
		this.folder = file;
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

			if (!this.folder.exists()) {
				this.folder.mkdirs();
			}

			File file = new File(this.folder, "r." + chunkPos.getRegionX() + "." + chunkPos.getRegionZ() + ".mca");
			RegionFile regionFile2 = new RegionFile(file, this.folder);
			this.regionCache.putAndMoveToFirst(l, regionFile2);
			return regionFile2;
		}
	}

	@Nullable
	public CompoundTag read(ChunkPos chunkPos) throws IOException {
		RegionFile regionFile = this.getRegionFile(chunkPos);
		DataInputStream dataInputStream = regionFile.getChunkDataInputStream(chunkPos);
		Throwable var4 = null;

		Object var5;
		try {
			if (dataInputStream != null) {
				return NbtIo.read(dataInputStream);
			}

			var5 = null;
		} catch (Throwable var15) {
			var4 = var15;
			throw var15;
		} finally {
			if (dataInputStream != null) {
				if (var4 != null) {
					try {
						dataInputStream.close();
					} catch (Throwable var14) {
						var4.addSuppressed(var14);
					}
				} else {
					dataInputStream.close();
				}
			}
		}

		return (CompoundTag)var5;
	}

	protected void write(ChunkPos chunkPos, CompoundTag compoundTag) throws IOException {
		RegionFile regionFile = this.getRegionFile(chunkPos);
		DataOutputStream dataOutputStream = regionFile.getChunkDataOutputStream(chunkPos);
		Throwable var5 = null;

		try {
			NbtIo.write(compoundTag, dataOutputStream);
		} catch (Throwable var14) {
			var5 = var14;
			throw var14;
		} finally {
			if (dataOutputStream != null) {
				if (var5 != null) {
					try {
						dataOutputStream.close();
					} catch (Throwable var13) {
						var5.addSuppressed(var13);
					}
				} else {
					dataOutputStream.close();
				}
			}
		}
	}

	public void close() throws IOException {
		for (RegionFile regionFile : this.regionCache.values()) {
			regionFile.close();
		}
	}
}
