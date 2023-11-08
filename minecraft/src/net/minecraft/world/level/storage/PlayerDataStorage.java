package net.minecraft.world.level.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
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
			Path path = this.playerDir.toPath();
			Path path2 = Files.createTempFile(path, player.getStringUUID() + "-", ".dat");
			NbtIo.writeCompressed(compoundTag, path2);
			Path path3 = path.resolve(player.getStringUUID() + ".dat");
			Path path4 = path.resolve(player.getStringUUID() + ".dat_old");
			Util.safeReplaceFile(path3, path2, path4);
		} catch (Exception var7) {
			LOGGER.warn("Failed to save player data for {}", player.getName().getString());
		}
	}

	@Nullable
	public CompoundTag load(Player player) {
		CompoundTag compoundTag = null;

		try {
			File file = new File(this.playerDir, player.getStringUUID() + ".dat");
			if (file.exists() && file.isFile()) {
				compoundTag = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
			}
		} catch (Exception var4) {
			LOGGER.warn("Failed to load player data for {}", player.getName().getString());
		}

		if (compoundTag != null) {
			int i = NbtUtils.getDataVersion(compoundTag, -1);
			compoundTag = DataFixTypes.PLAYER.updateToCurrentVersion(this.fixerUpper, compoundTag, i);
			player.load(compoundTag);
		}

		return compoundTag;
	}

	public String[] getSeenPlayers() {
		String[] strings = this.playerDir.list();
		if (strings == null) {
			strings = new String[0];
		}

		for (int i = 0; i < strings.length; i++) {
			if (strings[i].endsWith(".dat")) {
				strings[i] = strings[i].substring(0, strings[i].length() - 4);
			}
		}

		return strings;
	}
}
