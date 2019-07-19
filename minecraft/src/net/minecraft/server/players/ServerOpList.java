package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;

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
		String[] strings = new String[this.getEntries().size()];
		int i = 0;

		for (StoredUserEntry<GameProfile> storedUserEntry : this.getEntries()) {
			strings[i++] = storedUserEntry.getUser().getName();
		}

		return strings;
	}

	public boolean canBypassPlayerLimit(GameProfile gameProfile) {
		ServerOpListEntry serverOpListEntry = this.get(gameProfile);
		return serverOpListEntry != null ? serverOpListEntry.getBypassesPlayerLimit() : false;
	}

	protected String getKeyForUser(GameProfile gameProfile) {
		return gameProfile.getId().toString();
	}
}
