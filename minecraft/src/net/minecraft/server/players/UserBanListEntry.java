package net.minecraft.server.players;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class UserBanListEntry extends BanListEntry<GameProfile> {
	public UserBanListEntry(GameProfile gameProfile) {
		this(gameProfile, null, null, null, null);
	}

	public UserBanListEntry(GameProfile gameProfile, @Nullable Date date, @Nullable String string, @Nullable Date date2, @Nullable String string2) {
		super(gameProfile, date, string, date2, string2);
	}

	public UserBanListEntry(JsonObject jsonObject) {
		super(createGameProfile(jsonObject), jsonObject);
	}

	@Override
	public Component getDisplayName() {
		GameProfile gameProfile = this.getUser();
		return new TextComponent(gameProfile.getName() != null ? gameProfile.getName() : Objects.toString(gameProfile.getId(), "(Unknown)"));
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
