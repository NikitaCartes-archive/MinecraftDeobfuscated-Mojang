/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class DimensionDataStorage {
    private static final Logger LOGGER = LogManager.getLogger();
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

    public <T extends SavedData> T computeIfAbsent(Supplier<T> supplier, String string) {
        T savedData = this.get(supplier, string);
        if (savedData != null) {
            return savedData;
        }
        SavedData savedData2 = (SavedData)supplier.get();
        this.set(savedData2);
        return (T)savedData2;
    }

    @Nullable
    public <T extends SavedData> T get(Supplier<T> supplier, String string) {
        SavedData savedData = this.cache.get(string);
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
                SavedData savedData = (SavedData)supplier.get();
                CompoundTag compoundTag = this.readTagFromDisk(string, SharedConstants.getCurrentVersion().getWorldVersion());
                savedData.load(compoundTag.getCompound("data"));
                return (T)savedData;
            }
        } catch (Exception exception) {
            LOGGER.error("Error loading saved data: {}", (Object)string, (Object)exception);
        }
        return null;
    }

    public void set(SavedData savedData) {
        this.cache.put(savedData.getId(), savedData);
    }

    public CompoundTag readTagFromDisk(String string, int i) throws IOException {
        File file = this.getDataFile(string);
        try (PushbackInputStream pushbackInputStream = new PushbackInputStream(new FileInputStream(file), 2);){
            Object object;
            CompoundTag compoundTag;
            if (this.isGzip(pushbackInputStream)) {
                compoundTag = NbtIo.readCompressed(pushbackInputStream);
            } else {
                DataInputStream dataInputStream = new DataInputStream(pushbackInputStream);
                object = null;
                try {
                    compoundTag = NbtIo.read(dataInputStream);
                } catch (Throwable throwable) {
                    object = throwable;
                    throw throwable;
                } finally {
                    if (dataInputStream != null) {
                        if (object != null) {
                            try {
                                dataInputStream.close();
                            } catch (Throwable throwable) {
                                ((Throwable)object).addSuppressed(throwable);
                            }
                        } else {
                            dataInputStream.close();
                        }
                    }
                }
            }
            int j = compoundTag.contains("DataVersion", 99) ? compoundTag.getInt("DataVersion") : 1343;
            object = NbtUtils.update(this.fixerUpper, DataFixTypes.SAVED_DATA, compoundTag, j, i);
            return object;
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
        for (SavedData savedData : this.cache.values()) {
            if (savedData == null) continue;
            savedData.save(this.getDataFile(savedData.getId()));
        }
    }
}

