package com.mojang.realmsclient.util;

import com.google.gson.annotations.SerializedName;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class RealmsPersistence {
	private static final String FILE_NAME = "realms_persistence.json";
	private static final GuardedSerializer GSON = new GuardedSerializer();
	private static final Logger LOGGER = LogUtils.getLogger();

	public RealmsPersistence.RealmsPersistenceData read() {
		return readFile();
	}

	public void save(RealmsPersistence.RealmsPersistenceData realmsPersistenceData) {
		writeFile(realmsPersistenceData);
	}

	public static RealmsPersistence.RealmsPersistenceData readFile() {
		Path path = getPathToData();

		try {
			String string = Files.readString(path, StandardCharsets.UTF_8);
			RealmsPersistence.RealmsPersistenceData realmsPersistenceData = GSON.fromJson(string, RealmsPersistence.RealmsPersistenceData.class);
			if (realmsPersistenceData != null) {
				return realmsPersistenceData;
			}
		} catch (NoSuchFileException var3) {
		} catch (Exception var4) {
			LOGGER.warn("Failed to read Realms storage {}", path, var4);
		}

		return new RealmsPersistence.RealmsPersistenceData();
	}

	public static void writeFile(RealmsPersistence.RealmsPersistenceData realmsPersistenceData) {
		Path path = getPathToData();

		try {
			Files.writeString(path, GSON.toJson(realmsPersistenceData), StandardCharsets.UTF_8);
		} catch (Exception var3) {
		}
	}

	private static Path getPathToData() {
		return Minecraft.getInstance().gameDirectory.toPath().resolve("realms_persistence.json");
	}

	@Environment(EnvType.CLIENT)
	public static class RealmsPersistenceData implements ReflectionBasedSerialization {
		@SerializedName("newsLink")
		public String newsLink;
		@SerializedName("hasUnreadNews")
		public boolean hasUnreadNews;
	}
}
