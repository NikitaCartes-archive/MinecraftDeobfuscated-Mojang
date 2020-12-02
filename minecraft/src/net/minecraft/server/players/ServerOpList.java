package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;
import java.util.Objects;

public class ServerOpList extends StoredUserList<GameProfile, ServerOpListEntry> {
	public ServerOpList(File file) {
		super(file);
	}

	@Override
	protected StoredUserEntry<GameProfile> createEntry(JsonObject jsonObject) {
		return new ServerOpListEntry(jsonObject);
	}

	@Override
	public String[] getUserList() {
		return (String[])this.getEntries().stream().map(StoredUserEntry::getUser).filter(Objects::nonNull).map(GameProfile::getName).toArray(String[]::new);
	}

	public boolean canBypassPlayerLimit(GameProfile gameProfile) {
		ServerOpListEntry serverOpListEntry = this.get(gameProfile);
		return serverOpListEntry != null ? serverOpListEntry.getBypassesPlayerLimit() : false;
	}

	protected String getKeyForUser(GameProfile gameProfile) {
		return gameProfile.getId().toString();
	}
}
