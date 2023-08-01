package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import javax.annotation.Nullable;

public class ServerOpListEntry extends StoredUserEntry<GameProfile> {
	private final int level;
	private final boolean bypassesPlayerLimit;

	public ServerOpListEntry(GameProfile gameProfile, int i, boolean bl) {
		super(gameProfile);
		this.level = i;
		this.bypassesPlayerLimit = bl;
	}

	public ServerOpListEntry(JsonObject jsonObject) {
		super(createGameProfile(jsonObject));
		this.level = jsonObject.has("level") ? jsonObject.get("level").getAsInt() : 0;
		this.bypassesPlayerLimit = jsonObject.has("bypassesPlayerLimit") && jsonObject.get("bypassesPlayerLimit").getAsBoolean();
	}

	public int getLevel() {
		return this.level;
	}

	public boolean getBypassesPlayerLimit() {
		return this.bypassesPlayerLimit;
	}

	@Override
	protected void serialize(JsonObject jsonObject) {
		if (this.getUser() != null) {
			jsonObject.addProperty("uuid", this.getUser().getId().toString());
			jsonObject.addProperty("name", this.getUser().getName());
			jsonObject.addProperty("level", this.level);
			jsonObject.addProperty("bypassesPlayerLimit", this.bypassesPlayerLimit);
		}
	}

	@Nullable
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
