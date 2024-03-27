package net.minecraft.client;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class HotbarManager {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final int NUM_HOTBAR_GROUPS = 9;
	private final Path optionsFile;
	private final DataFixer fixerUpper;
	private final Hotbar[] hotbars = new Hotbar[9];
	private boolean loaded;

	public HotbarManager(Path path, DataFixer dataFixer) {
		this.optionsFile = path.resolve("hotbar.nbt");
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

			int i = NbtUtils.getDataVersion(compoundTag, 1343);
			compoundTag = DataFixTypes.HOTBAR.updateToCurrentVersion(this.fixerUpper, compoundTag, i);

			for (int j = 0; j < 9; j++) {
				this.hotbars[j] = (Hotbar)Hotbar.CODEC
					.parse(NbtOps.INSTANCE, compoundTag.get(String.valueOf(j)))
					.resultOrPartial(string -> LOGGER.warn("Failed to parse hotbar: {}", string))
					.orElseGet(Hotbar::new);
			}
		} catch (Exception var4) {
			LOGGER.error("Failed to load creative mode options", (Throwable)var4);
		}
	}

	public void save() {
		try {
			CompoundTag compoundTag = NbtUtils.addCurrentDataVersion(new CompoundTag());

			for (int i = 0; i < 9; i++) {
				Hotbar hotbar = this.get(i);
				DataResult<Tag> dataResult = Hotbar.CODEC.encodeStart(NbtOps.INSTANCE, hotbar);
				compoundTag.put(String.valueOf(i), dataResult.getOrThrow());
			}

			NbtIo.write(compoundTag, this.optionsFile);
		} catch (Exception var5) {
			LOGGER.error("Failed to save creative mode options", (Throwable)var5);
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
