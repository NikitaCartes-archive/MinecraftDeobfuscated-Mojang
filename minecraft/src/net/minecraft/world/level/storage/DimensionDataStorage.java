package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public class DimensionDataStorage {
	private static final Logger LOGGER = LogUtils.getLogger();
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
		SavedData savedData = (SavedData)this.cache.get(string);
		if (savedData == null && !this.cache.containsKey(string)) {
			savedData = this.readSavedData(factory.deserializer(), factory.type(), string);
			this.cache.put(string, savedData);
		}

		return (T)savedData;
	}

	@Nullable
	private <T extends SavedData> T readSavedData(Function<CompoundTag, T> function, DataFixTypes dataFixTypes, String string) {
		try {
			File file = this.getDataFile(string);
			if (file.exists()) {
				CompoundTag compoundTag = this.readTagFromDisk(string, dataFixTypes, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
				return (T)function.apply(compoundTag.getCompound("data"));
			}
		} catch (Exception var6) {
			LOGGER.error("Error loading saved data: {}", string, var6);
		}

		return null;
	}

	public void set(String string, SavedData savedData) {
		this.cache.put(string, savedData);
	}

	public CompoundTag readTagFromDisk(String string, DataFixTypes dataFixTypes, int i) throws IOException {
		File file = this.getDataFile(string);
		FileInputStream fileInputStream = new FileInputStream(file);

		CompoundTag var9;
		try {
			PushbackInputStream pushbackInputStream = new PushbackInputStream(fileInputStream, 2);

			try {
				CompoundTag compoundTag;
				if (this.isGzip(pushbackInputStream)) {
					compoundTag = NbtIo.readCompressed(pushbackInputStream, NbtAccounter.unlimitedHeap());
				} else {
					DataInputStream dataInputStream = new DataInputStream(pushbackInputStream);

					try {
						compoundTag = NbtIo.read(dataInputStream);
					} catch (Throwable var14) {
						try {
							dataInputStream.close();
						} catch (Throwable var13) {
							var14.addSuppressed(var13);
						}

						throw var14;
					}

					dataInputStream.close();
				}

				int j = NbtUtils.getDataVersion(compoundTag, 1343);
				var9 = dataFixTypes.update(this.fixerUpper, compoundTag, j, i);
			} catch (Throwable var15) {
				try {
					pushbackInputStream.close();
				} catch (Throwable var12) {
					var15.addSuppressed(var12);
				}

				throw var15;
			}

			pushbackInputStream.close();
		} catch (Throwable var16) {
			try {
				fileInputStream.close();
			} catch (Throwable var11) {
				var16.addSuppressed(var11);
			}

			throw var16;
		}

		fileInputStream.close();
		return var9;
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
