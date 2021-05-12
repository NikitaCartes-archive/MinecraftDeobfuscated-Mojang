package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.Map;
import java.util.function.Function;
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

	public <T extends SavedData> T computeIfAbsent(Function<CompoundTag, T> function, Supplier<T> supplier, String string) {
		T savedData = this.get(function, string);
		if (savedData != null) {
			return savedData;
		} else {
			T savedData2 = (T)supplier.get();
			this.set(string, savedData2);
			return savedData2;
		}
	}

	@Nullable
	public <T extends SavedData> T get(Function<CompoundTag, T> function, String string) {
		SavedData savedData = (SavedData)this.cache.get(string);
		if (savedData == null && !this.cache.containsKey(string)) {
			savedData = this.readSavedData(function, string);
			this.cache.put(string, savedData);
		}

		return (T)savedData;
	}

	@Nullable
	private <T extends SavedData> T readSavedData(Function<CompoundTag, T> function, String string) {
		try {
			File file = this.getDataFile(string);
			if (file.exists()) {
				CompoundTag compoundTag = this.readTagFromDisk(string, SharedConstants.getCurrentVersion().getWorldVersion());
				return (T)function.apply(compoundTag.getCompound("data"));
			}
		} catch (Exception var5) {
			LOGGER.error("Error loading saved data: {}", string, var5);
		}

		return null;
	}

	public void set(String string, SavedData savedData) {
		this.cache.put(string, savedData);
	}

	public CompoundTag readTagFromDisk(String string, int i) throws IOException {
		File file = this.getDataFile(string);
		FileInputStream fileInputStream = new FileInputStream(file);

		CompoundTag var8;
		try {
			PushbackInputStream pushbackInputStream = new PushbackInputStream(fileInputStream, 2);

			try {
				CompoundTag compoundTag;
				if (this.isGzip(pushbackInputStream)) {
					compoundTag = NbtIo.readCompressed(pushbackInputStream);
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

				int j = compoundTag.contains("DataVersion", 99) ? compoundTag.getInt("DataVersion") : 1343;
				var8 = NbtUtils.update(this.fixerUpper, DataFixTypes.SAVED_DATA, compoundTag, j, i);
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
			try {
				fileInputStream.close();
			} catch (Throwable var10) {
				var15.addSuppressed(var10);
			}

			throw var15;
		}

		fileInputStream.close();
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

	public void save() {
		this.cache.forEach((string, savedData) -> {
			if (savedData != null) {
				savedData.save(this.getDataFile(string));
			}
		});
	}
}
