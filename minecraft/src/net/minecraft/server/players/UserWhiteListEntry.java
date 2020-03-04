package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.UUID;

public class UserWhiteListEntry extends StoredUserEntry<GameProfile> {
	public UserWhiteListEntry(GameProfile gameProfile) {
		super(gameProfile);
	}

	public UserWhiteListEntry(JsonObject jsonObject) {
		super(createGameProfile(jsonObject));
	}

	@Override
	protected void serialize(JsonObject jsonObject) {
		if (this.getUser() != null) {
			jsonObject.addProperty("uuid", this.getUser().getId() == null ? "" : this.getUser().getId().toString());
			jsonObject.addProperty("name", this.getUser().getName());
		}
	}

	private static GameProfile createGameProfile(JsonObject jsonObject) {
		if (jsonObject.has("uuid") && jsonObject.has("name")) {
			String string = jsonObject.get("uuid").getAsString();

			UUID uUID;
			try {
				uUID = UUID.fromString(string);
			} catch (Throwable var4) {
				return null;
			}

			return new GameProfile(uUID, jsonObject.get("name").getAsString());
		} else {
			return null;
		}
	}
}
