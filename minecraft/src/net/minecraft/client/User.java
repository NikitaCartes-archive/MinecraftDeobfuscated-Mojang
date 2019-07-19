package net.minecraft.client;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class User {
	private final String name;
	private final String uuid;
	private final String accessToken;
	private final User.Type type;

	public User(String string, String string2, String string3, String string4) {
		this.name = string;
		this.uuid = string2;
		this.accessToken = string3;
		this.type = User.Type.byName(string4);
	}

	public String getSessionId() {
		return "token:" + this.accessToken + ":" + this.uuid;
	}

	public String getUuid() {
		return this.uuid;
	}

	public String getName() {
		return this.name;
	}

	public String getAccessToken() {
		return this.accessToken;
	}

	public GameProfile getGameProfile() {
		try {
			UUID uUID = UUIDTypeAdapter.fromString(this.getUuid());
			return new GameProfile(uUID, this.getName());
		} catch (IllegalArgumentException var2) {
			return new GameProfile(null, this.getName());
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum Type {
		LEGACY("legacy"),
		MOJANG("mojang");

		private static final Map<String, User.Type> BY_NAME = (Map<String, User.Type>)Arrays.stream(values())
			.collect(Collectors.toMap(type -> type.name, Function.identity()));
		private final String name;

		private Type(String string2) {
			this.name = string2;
		}

		@Nullable
		public static User.Type byName(String string) {
			return (User.Type)BY_NAME.get(string.toLowerCase(Locale.ROOT));
		}
	}
}
