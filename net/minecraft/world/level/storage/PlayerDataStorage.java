/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.File;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class PlayerDataStorage {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final File playerDir;
    protected final DataFixer fixerUpper;

    public PlayerDataStorage(LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer) {
        this.fixerUpper = dataFixer;
        this.playerDir = levelStorageAccess.getLevelPath(LevelResource.PLAYER_DATA_DIR).toFile();
        this.playerDir.mkdirs();
    }

    public void save(Player player) {
        try {
            CompoundTag compoundTag = player.saveWithoutId(new CompoundTag());
            File file = File.createTempFile(player.getStringUUID() + "-", ".dat", this.playerDir);
            NbtIo.writeCompressed(compoundTag, file);
            File file2 = new File(this.playerDir, player.getStringUUID() + ".dat");
            File file3 = new File(this.playerDir, player.getStringUUID() + ".dat_old");
            Util.safeReplaceFile(file2, file, file3);
        } catch (Exception exception) {
            LOGGER.warn("Failed to save player data for {}", (Object)player.getName().getString());
        }
    }

    @Nullable
    public CompoundTag load(Player player) {
        CompoundTag compoundTag = null;
        try {
            File file = new File(this.playerDir, player.getStringUUID() + ".dat");
            if (file.exists() && file.isFile()) {
                compoundTag = NbtIo.readCompressed(file);
            }
        } catch (Exception exception) {
            LOGGER.warn("Failed to load player data for {}", (Object)player.getName().getString());
        }
        if (compoundTag != null) {
            int i = compoundTag.contains("DataVersion", 3) ? compoundTag.getInt("DataVersion") : -1;
            player.load(NbtUtils.update(this.fixerUpper, DataFixTypes.PLAYER, compoundTag, i));
        }
        return compoundTag;
    }

    public String[] getSeenPlayers() {
        String[] strings = this.playerDir.list();
        if (strings == null) {
            strings = new String[]{};
        }
        for (int i = 0; i < strings.length; ++i) {
            if (!strings[i].endsWith(".dat")) continue;
            strings[i] = strings[i].substring(0, strings[i].length() - 4);
        }
        return strings;
    }
}

