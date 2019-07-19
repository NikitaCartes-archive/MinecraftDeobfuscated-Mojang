package com.mojang.realmsclient.dto;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class PendingInvitesList extends ValueObject {
	private static final Logger LOGGER = LogManager.getLogger();
	public List<PendingInvite> pendingInvites = Lists.<PendingInvite>newArrayList();

	public static PendingInvitesList parse(String string) {
		PendingInvitesList pendingInvitesList = new PendingInvitesList();

		try {
			JsonParser jsonParser = new JsonParser();
			JsonObject jsonObject = jsonParser.parse(string).getAsJsonObject();
			if (jsonObject.get("invites").isJsonArray()) {
				Iterator<JsonElement> iterator = jsonObject.get("invites").getAsJsonArray().iterator();

				while (iterator.hasNext()) {
					pendingInvitesList.pendingInvites.add(PendingInvite.parse(((JsonElement)iterator.next()).getAsJsonObject()));
				}
			}
		} catch (Exception var5) {
			LOGGER.error("Could not parse PendingInvitesList: " + var5.getMessage());
		}

		return pendingInvitesList;
	}
}
