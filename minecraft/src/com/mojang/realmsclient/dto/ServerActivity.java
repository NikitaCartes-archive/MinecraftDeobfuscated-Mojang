package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ServerActivity extends ValueObject {
	public String profileUuid;
	public long joinTime;
	public long leaveTime;

	public static ServerActivity parse(JsonObject jsonObject) {
		ServerActivity serverActivity = new ServerActivity();

		try {
			serverActivity.profileUuid = JsonUtils.getStringOr("profileUuid", jsonObject, null);
			serverActivity.joinTime = JsonUtils.getLongOr("joinTime", jsonObject, Long.MIN_VALUE);
			serverActivity.leaveTime = JsonUtils.getLongOr("leaveTime", jsonObject, Long.MIN_VALUE);
		} catch (Exception var3) {
		}

		return serverActivity;
	}
}
