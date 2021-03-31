package net.minecraft.world.level.storage;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.SharedConstants;

public class LevelVersion {
	private final int levelDataVersion;
	private final long lastPlayed;
	private final String minecraftVersionName;
	private final int minecraftVersion;
	private final boolean snapshot;

	public LevelVersion(int i, long l, String string, int j, boolean bl) {
		this.levelDataVersion = i;
		this.lastPlayed = l;
		this.minecraftVersionName = string;
		this.minecraftVersion = j;
		this.snapshot = bl;
	}

	public static LevelVersion parse(Dynamic<?> dynamic) {
		int i = dynamic.get("version").asInt(0);
		long l = dynamic.get("LastPlayed").asLong(0L);
		OptionalDynamic<?> optionalDynamic = dynamic.get("Version");
		return optionalDynamic.result().isPresent()
			? new LevelVersion(
				i,
				l,
				optionalDynamic.get("Name").asString(SharedConstants.getCurrentVersion().getName()),
				optionalDynamic.get("Id").asInt(SharedConstants.getCurrentVersion().getWorldVersion()),
				optionalDynamic.get("Snapshot").asBoolean(!SharedConstants.getCurrentVersion().isStable())
			)
			: new LevelVersion(i, l, "", 0, false);
	}

	public int levelDataVersion() {
		return this.levelDataVersion;
	}

	public long lastPlayed() {
		return this.lastPlayed;
	}

	public String minecraftVersionName() {
		return this.minecraftVersionName;
	}

	public int minecraftVersion() {
		return this.minecraftVersion;
	}

	public boolean snapshot() {
		return this.snapshot;
	}
}
