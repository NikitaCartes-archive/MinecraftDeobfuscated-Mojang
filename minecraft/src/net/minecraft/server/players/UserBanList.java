package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;

public class UserBanList extends StoredUserList<GameProfile, UserBanListEntry> {
	public UserBanList(File file) {
		super(file);
	}

	@Override
	protected StoredUserEntry<GameProfile> createEntry(JsonObject jsonObject) {
		return new UserBanListEntry(jsonObject);
	}

	public boolean isBanned(GameProfile gameProfile) {
		return this.contains(gameProfile);
	}

	@Override
	public String[] getUserList() {
		String[] strings = new String[this.getEntries().size()];
		int i = 0;

		for (StoredUserEntry<GameProfile> storedUserEntry : this.getEntries()) {
			strings[i++] = storedUserEntry.getUser().getName();
		}

		return strings;
	}

	protected String getKeyForUser(GameProfile gameProfile) {
		return gameProfile.getId().toString();
	}
}
