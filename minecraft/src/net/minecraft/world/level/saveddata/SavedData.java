package net.minecraft.world.level.saveddata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class SavedData {
	private static final Logger LOGGER = LogManager.getLogger();
	private final String id;
	private boolean dirty;

	public SavedData(String string) {
		this.id = string;
	}

	public abstract void load(CompoundTag compoundTag);

	public abstract CompoundTag save(CompoundTag compoundTag);

	public void setDirty() {
		this.setDirty(true);
	}

	public void setDirty(boolean bl) {
		this.dirty = bl;
	}

	public boolean isDirty() {
		return this.dirty;
	}

	public String getId() {
		return this.id;
	}

	public void save(File file) {
		if (this.isDirty()) {
			CompoundTag compoundTag = new CompoundTag();
			compoundTag.put("data", this.save(new CompoundTag()));
			compoundTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());

			try {
				FileOutputStream fileOutputStream = new FileOutputStream(file);
				Throwable var4 = null;

				try {
					NbtIo.writeCompressed(compoundTag, fileOutputStream);
				} catch (Throwable var14) {
					var4 = var14;
					throw var14;
				} finally {
					if (fileOutputStream != null) {
						if (var4 != null) {
							try {
								fileOutputStream.close();
							} catch (Throwable var13) {
								var4.addSuppressed(var13);
							}
						} else {
							fileOutputStream.close();
						}
					}
				}
			} catch (IOException var16) {
				LOGGER.error("Could not save data {}", this, var16);
			}

			this.setDirty(false);
		}
	}
}
