package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DimensionDataStorage {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Map<String, SavedData> cache = Maps.<String, SavedData>newHashMap();
	private final DataFixer fixerUpper;
	private final File dataFolder;

	public DimensionDataStorage(File file, DataFixer dataFixer) {
		this.fixerUpper = dataFixer;
		this.dataFolder = file;
	}

	private File getDataFile(String string) {
		return new File(this.dataFolder, string + ".dat");
	}

	public <T extends SavedData> T computeIfAbsent(Supplier<T> supplier, String string) {
		T savedData = this.get(supplier, string);
		if (savedData != null) {
			return savedData;
		} else {
			T savedData2 = (T)supplier.get();
			this.set(savedData2);
			return savedData2;
		}
	}

	@Nullable
	public <T extends SavedData> T get(Supplier<T> supplier, String string) {
		SavedData savedData = (SavedData)this.cache.get(string);
		if (savedData == null && !this.cache.containsKey(string)) {
			savedData = this.readSavedData(supplier, string);
			this.cache.put(string, savedData);
		}

		return (T)savedData;
	}

	@Nullable
	private <T extends SavedData> T readSavedData(Supplier<T> supplier, String string) {
		try {
			File file = this.getDataFile(string);
			if (file.exists()) {
				T savedData = (T)supplier.get();
				CompoundTag compoundTag = this.readTagFromDisk(string, SharedConstants.getCurrentVersion().getWorldVersion());
				savedData.load(compoundTag.getCompound("data"));
				return savedData;
			}
		} catch (Exception var6) {
			LOGGER.error("Error loading saved data: {}", string, var6);
		}

		return null;
	}

	public void set(SavedData savedData) {
		this.cache.put(savedData.getId(), savedData);
	}

	public CompoundTag readTagFromDisk(String string, int i) throws IOException {
		File file = this.getDataFile(string);
		PushbackInputStream pushbackInputStream = new PushbackInputStream(new FileInputStream(file), 2);
		Throwable var5 = null;

		CompoundTag var36;
		try {
			CompoundTag compoundTag;
			if (this.isGzip(pushbackInputStream)) {
				compoundTag = NbtIo.readCompressed(pushbackInputStream);
			} else {
				DataInputStream dataInputStream = new DataInputStream(pushbackInputStream);
				Throwable var8 = null;

				try {
					compoundTag = NbtIo.read(dataInputStream);
				} catch (Throwable var31) {
					var8 = var31;
					throw var31;
				} finally {
					if (dataInputStream != null) {
						if (var8 != null) {
							try {
								dataInputStream.close();
							} catch (Throwable var30) {
								var8.addSuppressed(var30);
							}
						} else {
							dataInputStream.close();
						}
					}
				}
			}

			int j = compoundTag.contains("DataVersion", 99) ? compoundTag.getInt("DataVersion") : 1343;
			var36 = NbtUtils.update(this.fixerUpper, DataFixTypes.SAVED_DATA, compoundTag, j, i);
		} catch (Throwable var33) {
			var5 = var33;
			throw var33;
		} finally {
			if (pushbackInputStream != null) {
				if (var5 != null) {
					try {
						pushbackInputStream.close();
					} catch (Throwable var29) {
						var5.addSuppressed(var29);
					}
				} else {
					pushbackInputStream.close();
				}
			}
		}

		return var36;
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

	public void save() {
		for (SavedData savedData : this.cache.values()) {
			if (savedData != null) {
				savedData.save(this.getDataFile(savedData.getId()));
			}
		}
	}
}
