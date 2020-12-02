package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Objects;

public class UserWhiteList extends StoredUserList<GameProfile, UserWhiteListEntry> {
	public UserWhiteList(File file) {
		super(file);
	}

	@Override
	protected StoredUserEntry<GameProfile> createEntry(JsonObject jsonObject) {
		return new UserWhiteListEntry(jsonObject);
	}

	public boolean isWhiteListed(GameProfile gameProfile) {
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
