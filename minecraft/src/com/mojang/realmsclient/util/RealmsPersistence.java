package com.mojang.realmsclient.util;

import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.FileUtils;

@Environment(EnvType.CLIENT)
public class RealmsPersistence {
	private static final GuardedSerializer GSON = new GuardedSerializer();

	public RealmsPersistence.RealmsPersistenceData read() {
		return readFile();
	}

	public void save(RealmsPersistence.RealmsPersistenceData realmsPersistenceData) {
		writeFile(realmsPersistenceData);
	}

	public static RealmsPersistence.RealmsPersistenceData readFile() {
		File file = getPathToData();

		try {
			String string = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
			RealmsPersistence.RealmsPersistenceData realmsPersistenceData = GSON.fromJson(string, RealmsPersistence.RealmsPersistenceData.class);
			return realmsPersistenceData != null ? realmsPersistenceData : new RealmsPersistence.RealmsPersistenceData();
		} catch (IOException var3) {
			return new RealmsPersistence.RealmsPersistenceData();
		}
	}

	public static void writeFile(RealmsPersistence.RealmsPersistenceData realmsPersistenceData) {
		File file = getPathToData();

		try {
			FileUtils.writeStringToFile(file, GSON.toJson(realmsPersistenceData), StandardCharsets.UTF_8);
		} catch (IOException var3) {
		}
	}

	private static File getPathToData() {
		return new File(Minecraft.getInstance().gameDirectory, "realms_persistence.json");
	}

	@Environment(EnvType.CLIENT)
	public static class RealmsPersistenceData implements ReflectionBasedSerialization {
		@SerializedName("newsLink")
		public String newsLink;
		@SerializedName("hasUnreadNews")
		public boolean hasUnreadNews;
	}
}
