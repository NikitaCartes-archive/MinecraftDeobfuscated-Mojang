package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class BackupList extends ValueObject {
	private static final Logger LOGGER = LogManager.getLogger();
	public List<Backup> backups;

	public static BackupList parse(String string) {
		JsonParser jsonParser = new JsonParser();
		BackupList backupList = new BackupList();
		backupList.backups = Lists.<Backup>newArrayList();

		try {
			JsonElement jsonElement = jsonParser.parse(string).getAsJsonObject().get("backups");
			if (jsonElement.isJsonArray()) {
				Iterator<JsonElement> iterator = jsonElement.getAsJsonArray().iterator();

				while (iterator.hasNext()) {
					backupList.backups.add(Backup.parse((JsonElement)iterator.next()));
				}
			}
		} catch (Exception var5) {
			LOGGER.error("Could not parse BackupList: " + var5.getMessage());
		}

		return backupList;
	}
}
