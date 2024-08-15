package net.minecraft.world.level.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public class DimensionDataStorage implements AutoCloseable {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Map<String, Optional<SavedData>> cache = new HashMap();
	private final DataFixer fixerUpper;
	private final HolderLookup.Provider registries;
	private final Path dataFolder;
	private CompletableFuture<?> pendingWriteFuture = CompletableFuture.completedFuture(null);

	public DimensionDataStorage(Path path, DataFixer dataFixer, HolderLookup.Provider provider) {
		this.fixerUpper = dataFixer;
		this.dataFolder = path;
		this.registries = provider;
	}

	private Path getDataFile(String string) {
		return this.dataFolder.resolve(string + ".dat");
	}

	public <T extends SavedData> T computeIfAbsent(SavedData.Factory<T> factory, String string) {
		T savedData = this.get(factory, string);
		if (savedData != null) {
			return savedData;
		} else {
			T savedData2 = (T)factory.constructor().get();
			this.set(string, savedData2);
			return savedData2;
		}
	}

	@Nullable
	public <T extends SavedData> T get(SavedData.Factory<T> factory, String string) {
		Optional<SavedData> optional = (Optional<SavedData>)this.cache.get(string);
		if (optional == null) {
			optional = Optional.ofNullable(this.readSavedData(factory.deserializer(), factory.type(), string));
			this.cache.put(string, optional);
		}

		return (T)optional.orElse(null);
	}

	@Nullable
	private <T extends SavedData> T readSavedData(BiFunction<CompoundTag, HolderLookup.Provider, T> biFunction, DataFixTypes dataFixTypes, String string) {
		try {
			Path path = this.getDataFile(string);
			if (Files.exists(path, new LinkOption[0])) {
				CompoundTag compoundTag = this.readTagFromDisk(string, dataFixTypes, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
				return (T)biFunction.apply(compoundTag.getCompound("data"), this.registries);
			}
		} catch (Exception var6) {
			LOGGER.error("Error loading saved data: {}", string, var6);
		}

		return null;
	}

	public void set(String string, SavedData savedData) {
		this.cache.put(string, Optional.of(savedData));
		savedData.setDirty();
	}

	public CompoundTag readTagFromDisk(String string, DataFixTypes dataFixTypes, int i) throws IOException {
		InputStream inputStream = Files.newInputStream(this.getDataFile(string));

		CompoundTag var8;
		try {
			PushbackInputStream pushbackInputStream = new PushbackInputStream(new FastBufferedInputStream(inputStream), 2);

			try {
				CompoundTag compoundTag;
				if (this.isGzip(pushbackInputStream)) {
					compoundTag = NbtIo.readCompressed(pushbackInputStream, NbtAccounter.unlimitedHeap());
				} else {
					DataInputStream dataInputStream = new DataInputStream(pushbackInputStream);

					try {
						compoundTag = NbtIo.read(dataInputStream);
					} catch (Throwable var13) {
						try {
							dataInputStream.close();
						} catch (Throwable var12) {
							var13.addSuppressed(var12);
						}

						throw var13;
					}

					dataInputStream.close();
				}

				int j = NbtUtils.getDataVersion(compoundTag, 1343);
				var8 = dataFixTypes.update(this.fixerUpper, compoundTag, j, i);
			} catch (Throwable var14) {
				try {
					pushbackInputStream.close();
				} catch (Throwable var11) {
					var14.addSuppressed(var11);
				}

				throw var14;
			}

			pushbackInputStream.close();
		} catch (Throwable var15) {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Throwable var10) {
					var15.addSuppressed(var10);
				}
			}

			throw var15;
		}

		if (inputStream != null) {
			inputStream.close();
		}

		return var8;
	}

	private boolean isGzip(PushbackInputStream pushbackInputStream) throws IOException {
		byte[] bs = new byte[2];
		boolean bl = false;
		int i = pushbackInputStream.read(bs, 0, 2);
		if (i == 2) {
			int j = (bs[1] & 255) << 8 | bs[0] & 255;
			if (j == 35615) {
				bl = true;
			}
		}

		if (i != 0) {
			pushbackInputStream.unread(bs, 0, i);
		}

		return bl;
	}

	public CompletableFuture<?> scheduleSave() {
		Map<Path, CompoundTag> map = this.collectDirtyTagsToSave();
		if (map.isEmpty()) {
			return CompletableFuture.completedFuture(null);
		} else {
			this.pendingWriteFuture = this.pendingWriteFuture
				.thenCompose(
					object -> CompletableFuture.allOf(
							(CompletableFuture[])map.entrySet()
								.stream()
								.map(entry -> tryWriteAsync((Path)entry.getKey(), (CompoundTag)entry.getValue()))
								.toArray(CompletableFuture[]::new)
						)
				);
			return this.pendingWriteFuture;
		}
	}

	private Map<Path, CompoundTag> collectDirtyTagsToSave() {
		Map<Path, CompoundTag> map = new Object2ObjectArrayMap<>();
		this.cache
			.forEach(
				(string, optional) -> optional.filter(SavedData::isDirty).ifPresent(savedData -> map.put(this.getDataFile(string), savedData.save(this.registries)))
			);
		return map;
	}

	private static CompletableFuture<Void> tryWriteAsync(Path path, CompoundTag compoundTag) {
		return CompletableFuture.runAsync(() -> {
			try {
				NbtIo.writeCompressed(compoundTag, path);
			} catch (IOException var3) {
				LOGGER.error("Could not save data to {}", path.getFileName(), var3);
			}
		}, Util.ioPool());
	}

	public void saveAndJoin() {
		this.scheduleSave().join();
	}

	public void close() {
		this.saveAndJoin();
	}
}
