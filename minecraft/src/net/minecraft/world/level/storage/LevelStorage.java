package net.minecraft.world.level.storage;

import com.mojang.datafixers.DataFixer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LevelStorage implements PlayerIO {
	private static final Logger LOGGER = LogManager.getLogger();
	private final File worldDir;
	private final File playerDir;
	private final String levelId;
	private final StructureManager structureManager;
	protected final DataFixer fixerUpper;

	public LevelStorage(File file, String string, @Nullable MinecraftServer minecraftServer, DataFixer dataFixer) {
		this.fixerUpper = dataFixer;
		this.worldDir = new File(file, string);
		this.worldDir.mkdirs();
		this.playerDir = new File(this.worldDir, "playerdata");
		this.levelId = string;
		if (minecraftServer != null) {
			this.playerDir.mkdirs();
			this.structureManager = new StructureManager(minecraftServer, this.worldDir, dataFixer);
		} else {
			this.structureManager = null;
		}
	}

	public void saveLevelData(LevelData levelData, @Nullable CompoundTag compoundTag) {
		levelData.setVersion(19133);
		CompoundTag compoundTag2 = levelData.createTag(compoundTag);
		CompoundTag compoundTag3 = new CompoundTag();
		compoundTag3.put("Data", compoundTag2);

		try {
			File file = new File(this.worldDir, "level.dat_new");
			File file2 = new File(this.worldDir, "level.dat_old");
			File file3 = new File(this.worldDir, "level.dat");
			NbtIo.writeCompressed(compoundTag3, new FileOutputStream(file));
			if (file2.exists()) {
				file2.delete();
			}

			file3.renameTo(file2);
			if (file3.exists()) {
				file3.delete();
			}

			file.renameTo(file3);
			if (file.exists()) {
				file.delete();
			}
		} catch (Exception var8) {
			var8.printStackTrace();
		}
	}

	public File getFolder() {
		return this.worldDir;
	}

	@Nullable
	public LevelData prepareLevel() {
		File file = new File(this.worldDir, "level.dat");
		if (file.exists()) {
			LevelData levelData = LevelStorageSource.getLevelData(file, this.fixerUpper);
			if (levelData != null) {
				return levelData;
			}
		}

		file = new File(this.worldDir, "level.dat_old");
		return file.exists() ? LevelStorageSource.getLevelData(file, this.fixerUpper) : null;
	}

	public void saveLevelData(LevelData levelData) {
		this.saveLevelData(levelData, null);
	}

	@Override
	public void save(Player player) {
		try {
			CompoundTag compoundTag = player.saveWithoutId(new CompoundTag());
			File file = new File(this.playerDir, player.getStringUUID() + ".dat.tmp");
			File file2 = new File(this.playerDir, player.getStringUUID() + ".dat");
			NbtIo.writeCompressed(compoundTag, new FileOutputStream(file));
			if (file2.exists()) {
				file2.delete();
			}

			file.renameTo(file2);
		} catch (Exception var5) {
			LOGGER.warn("Failed to save player data for {}", player.getName().getString());
		}
	}

	@Nullable
	@Override
	public CompoundTag load(Player player) {
		CompoundTag compoundTag = null;

		try {
			File file = new File(this.playerDir, player.getStringUUID() + ".dat");
			if (file.exists() && file.isFile()) {
				compoundTag = NbtIo.readCompressed(new FileInputStream(file));
			}
		} catch (Exception var4) {
			LOGGER.warn("Failed to load player data for {}", player.getName().getString());
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
			strings = new String[0];
		}

		for (int i = 0; i < strings.length; i++) {
			if (strings[i].endsWith(".dat")) {
				strings[i] = strings[i].substring(0, strings[i].length() - 4);
			}
		}

		return strings;
	}

	public StructureManager getStructureManager() {
		return this.structureManager;
	}

	public DataFixer getFixerUpper() {
		return this.fixerUpper;
	}
}
