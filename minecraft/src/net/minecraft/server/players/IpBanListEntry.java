package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.util.Date;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class IpBanListEntry extends BanListEntry<String> {
	public IpBanListEntry(String string) {
		this(string, null, null, null, null);
	}

	public IpBanListEntry(String string, @Nullable Date date, @Nullable String string2, @Nullable Date date2, @Nullable String string3) {
		super(string, date, string2, date2, string3);
	}

	@Override
	public Component getDisplayName() {
		return new TextComponent(this.getUser());
	}

	public IpBanListEntry(JsonObject jsonObject) {
		super(createIpInfo(jsonObject), jsonObject);
	}

	private static String createIpInfo(JsonObject jsonObject) {
		return jsonObject.has("ip") ? jsonObject.get("ip").getAsString() : null;
	}
}
