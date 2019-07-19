package net.minecraft.client;

import com.mojang.datafixers.DataFixer;
import java.io.File;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class HotbarManager {
	private static final Logger LOGGER = LogManager.getLogger();
	private final File optionsFile;
	private final DataFixer fixerUpper;
	private final Hotbar[] hotbars = new Hotbar[9];
	private boolean loaded;

	public HotbarManager(File file, DataFixer dataFixer) {
		this.optionsFile = new File(file, "hotbar.nbt");
		this.fixerUpper = dataFixer;

		for (int i = 0; i < 9; i++) {
			this.hotbars[i] = new Hotbar();
		}
	}

	private void load() {
		try {
			CompoundTag compoundTag = NbtIo.read(this.optionsFile);
			if (compoundTag == null) {
				return;
			}

			if (!compoundTag.contains("DataVersion", 99)) {
				compoundTag.putInt("DataVersion", 1343);
			}

			compoundTag = NbtUtils.update(this.fixerUpper, DataFixTypes.HOTBAR, compoundTag, compoundTag.getInt("DataVersion"));

			for (int i = 0; i < 9; i++) {
				this.hotbars[i].fromTag(compoundTag.getList(String.valueOf(i), 10));
			}
		} catch (Exception var3) {
			LOGGER.error("Failed to load creative mode options", (Throwable)var3);
		}
	}

	public void save() {
		try {
			CompoundTag compoundTag = new CompoundTag();
			compoundTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());

			for (int i = 0; i < 9; i++) {
				compoundTag.put(String.valueOf(i), this.get(i).createTag());
			}

			NbtIo.write(compoundTag, this.optionsFile);
		} catch (Exception var3) {
			LOGGER.error("Failed to save creative mode options", (Throwable)var3);
		}
	}

	public Hotbar get(int i) {
		if (!this.loaded) {
			this.load();
			this.loaded = true;
		}

		return this.hotbars[i];
	}
}
