package net.minecraft.world.level.storage;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
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
	private static final DateTimeFormatter FORMATTER = FileNameDateFormatter.create();

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

	private void backup(Player player, String string) {
		Path path = this.playerDir.toPath();
		Path path2 = path.resolve(player.getStringUUID() + string);
		Path path3 = path.resolve(player.getStringUUID() + "_corrupted_" + LocalDateTime.now().format(FORMATTER) + string);
		if (Files.isRegularFile(path2, new LinkOption[0])) {
			try {
				Files.copy(path2, path3, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
			} catch (Exception var7) {
				LOGGER.warn("Failed to copy the player.dat file for {}", player.getName().getString(), var7);
			}
		}
	}

	private Optional<CompoundTag> load(Player player, String string) {
		File file = new File(this.playerDir, player.getStringUUID() + string);
		if (file.exists() && file.isFile()) {
			try {
				return Optional.of(NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap()));
			} catch (Exception var5) {
				LOGGER.warn("Failed to load player data for {}", player.getName().getString());
			}
		}

		return Optional.empty();
	}

	public Optional<CompoundTag> load(Player player) {
		Optional<CompoundTag> optional = this.load(player, ".dat");
		if (optional.isEmpty()) {
			this.backup(player, ".dat");
		}

		return optional.or(() -> this.load(player, ".dat_old")).map(compoundTag -> {
			int i = NbtUtils.getDataVersion(compoundTag, -1);
			compoundTag = DataFixTypes.PLAYER.updateToCurrentVersion(this.fixerUpper, compoundTag, i);
			player.load(compoundTag);
			return compoundTag;
		});
	}
}
