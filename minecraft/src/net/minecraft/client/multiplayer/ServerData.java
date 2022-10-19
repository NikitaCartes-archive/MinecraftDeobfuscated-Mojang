package net.minecraft.client.multiplayer;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ServerData {
	public String name;
	public String ip;
	public Component status;
	public Component motd;
	public long ping;
	public int protocol = SharedConstants.getCurrentVersion().getProtocolVersion();
	public Component version = Component.literal(SharedConstants.getCurrentVersion().getName());
	public boolean pinged;
	public List<Component> playerList = Collections.emptyList();
	private ServerData.ServerPackStatus packStatus = ServerData.ServerPackStatus.PROMPT;
	@Nullable
	private String iconB64;
	private boolean lan;
	private boolean enforcesSecureChat;

	public ServerData(String string, String string2, boolean bl) {
		this.name = string;
		this.ip = string2;
		this.lan = bl;
	}

	public CompoundTag write() {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("name", this.name);
		compoundTag.putString("ip", this.ip);
		if (this.iconB64 != null) {
			compoundTag.putString("icon", this.iconB64);
		}

		if (this.packStatus == ServerData.ServerPackStatus.ENABLED) {
			compoundTag.putBoolean("acceptTextures", true);
		} else if (this.packStatus == ServerData.ServerPackStatus.DISABLED) {
			compoundTag.putBoolean("acceptTextures", false);
		}

		return compoundTag;
	}

	public ServerData.ServerPackStatus getResourcePackStatus() {
		return this.packStatus;
	}

	public void setResourcePackStatus(ServerData.ServerPackStatus serverPackStatus) {
		this.packStatus = serverPackStatus;
	}

	public static ServerData read(CompoundTag compoundTag) {
		ServerData serverData = new ServerData(compoundTag.getString("name"), compoundTag.getString("ip"), false);
		if (compoundTag.contains("icon", 8)) {
			serverData.setIconB64(compoundTag.getString("icon"));
		}

		if (compoundTag.contains("acceptTextures", 1)) {
			if (compoundTag.getBoolean("acceptTextures")) {
				serverData.setResourcePackStatus(ServerData.ServerPackStatus.ENABLED);
			} else {
				serverData.setResourcePackStatus(ServerData.ServerPackStatus.DISABLED);
			}
		} else {
			serverData.setResourcePackStatus(ServerData.ServerPackStatus.PROMPT);
		}

		return serverData;
	}

	@Nullable
	public String getIconB64() {
		return this.iconB64;
	}

	public static String parseFavicon(String string) throws ParseException {
		if (string.startsWith("data:image/png;base64,")) {
			return string.substring("data:image/png;base64,".length());
		} else {
			throw new ParseException("Unknown format", 0);
		}
	}

	public void setIconB64(@Nullable String string) {
		this.iconB64 = string;
	}

	public boolean isLan() {
		return this.lan;
	}

	public void setEnforcesSecureChat(boolean bl) {
		this.enforcesSecureChat = bl;
	}

	public boolean enforcesSecureChat() {
		return this.enforcesSecureChat;
	}

	public void copyNameIconFrom(ServerData serverData) {
		this.ip = serverData.ip;
		this.name = serverData.name;
		this.iconB64 = serverData.iconB64;
	}

	public void copyFrom(ServerData serverData) {
		this.copyNameIconFrom(serverData);
		this.setResourcePackStatus(serverData.getResourcePackStatus());
		this.lan = serverData.lan;
		this.enforcesSecureChat = serverData.enforcesSecureChat;
	}

	@Environment(EnvType.CLIENT)
	public static enum ServerPackStatus {
		ENABLED("enabled"),
		DISABLED("disabled"),
		PROMPT("prompt");

		private final Component name;

		private ServerPackStatus(String string2) {
			this.name = Component.translatable("addServer.resourcePack." + string2);
		}

		public Component getName() {
			return this.name;
		}
	}
}
