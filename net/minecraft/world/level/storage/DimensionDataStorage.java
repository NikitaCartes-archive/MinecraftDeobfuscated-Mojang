/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class DimensionDataStorage {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, SavedData> cache = Maps.newHashMap();
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
        }
        SavedData savedData2 = (SavedData)supplier.get();
        this.set(string, savedData2);
        return (T)savedData2;
    }

    @Nullable
    public <T extends SavedData> T get(Function<CompoundTag, T> function, String string) {
        SavedData savedData = this.cache.get(string);
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
                return (T)((SavedData)function.apply(compoundTag.getCompound("data")));
            }
        } catch (Exception exception) {
            LOGGER.error("Error loading saved data: {}", (Object)string, (Object)exception);
        }
        return null;
    }

    public void set(String string, SavedData savedData) {
        this.cache.put(string, savedData);
    }

    public CompoundTag readTagFromDisk(String string, int i) throws IOException {
        File file = this.getDataFile(string);
        try (FileInputStream fileInputStream = new FileInputStream(file);){
            CompoundTag compoundTag;
            try (PushbackInputStream pushbackInputStream = new PushbackInputStream(fileInputStream, 2);){
                CompoundTag compoundTag2;
                if (this.isGzip(pushbackInputStream)) {
                    compoundTag2 = NbtIo.readCompressed(pushbackInputStream);
                } else {
                    try (DataInputStream dataInputStream = new DataInputStream(pushbackInputStream);){
                        compoundTag2 = NbtIo.read(dataInputStream);
                    }
                }
                int j = compoundTag2.contains("DataVersion", 99) ? compoundTag2.getInt("DataVersion") : 1343;
                compoundTag = NbtUtils.update(this.fixerUpper, DataFixTypes.SAVED_DATA, compoundTag2, j, i);
            }
            return compoundTag;
        }
    }

    private boolean isGzip(PushbackInputStream pushbackInputStream) throws IOException {
        int j;
        byte[] bs = new byte[2];
        boolean bl = false;
        int i = pushbackInputStream.read(bs, 0, 2);
        if (i == 2 && (j = (bs[1] & 0xFF) << 8 | bs[0] & 0xFF) == 35615) {
            bl = true;
        }
        if (i != 0) {
            pushbackInputStream.unread(bs, 0, i);
        }
        return bl;
    }

    public void save() {
        this.cache.forEach((string, savedData) -> {
            if (savedData != null) {
                savedData.save(this.getDataFile((String)string));
            }
        });
    }
}

