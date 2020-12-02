package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Objects;

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
		return (String[])this.getEntries().stream().map(StoredUserEntry::getUser).filter(Objects::nonNull).map(GameProfile::getName).toArray(String[]::new);
	}

	protected String getKeyForUser(GameProfile gameProfile) {
		return gameProfile.getId().toString();
	}
}
