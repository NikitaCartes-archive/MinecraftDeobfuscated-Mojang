package net.minecraft.client.multiplayer;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.util.PngInfo;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ServerData {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int MAX_ICON_SIZE = 1024;
	public String name;
	public String ip;
	public Component status;
	public Component motd;
	@Nullable
	public ServerStatus.Players players;
	public long ping;
	public int protocol = SharedConstants.getCurrentVersion().getProtocolVersion();
	public Component version = Component.literal(SharedConstants.getCurrentVersion().getName());
	public List<Component> playerList = Collections.emptyList();
	private ServerData.ServerPackStatus packStatus = ServerData.ServerPackStatus.PROMPT;
	@Nullable
	private byte[] iconBytes;
	private ServerData.Type type;
	private ServerData.State state = ServerData.State.INITIAL;
	private boolean enforcesSecureChat;

	public ServerData(String string, String string2, ServerData.Type type) {
		this.name = string;
		this.ip = string2;
		this.type = type;
	}

	public CompoundTag write() {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("name", this.name);
		compoundTag.putString("ip", this.ip);
		if (this.iconBytes != null) {
			compoundTag.putString("icon", Base64.getEncoder().encodeToString(this.iconBytes));
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
		ServerData serverData = new ServerData(compoundTag.getString("name"), compoundTag.getString("ip"), ServerData.Type.OTHER);
		if (compoundTag.contains("icon", 8)) {
			try {
				byte[] bs = Base64.getDecoder().decode(compoundTag.getString("icon"));
				serverData.setIconBytes(validateIcon(bs));
			} catch (IllegalArgumentException var3) {
				LOGGER.warn("Malformed base64 server icon", (Throwable)var3);
			}
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
	public byte[] getIconBytes() {
		return this.iconBytes;
	}

	public void setIconBytes(@Nullable byte[] bs) {
		this.iconBytes = bs;
	}

	public boolean isLan() {
		return this.type == ServerData.Type.LAN;
	}

	public boolean isRealm() {
		return this.type == ServerData.Type.REALM;
	}

	public ServerData.Type type() {
		return this.type;
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
		this.iconBytes = serverData.iconBytes;
	}

	public void copyFrom(ServerData serverData) {
		this.copyNameIconFrom(serverData);
		this.setResourcePackStatus(serverData.getResourcePackStatus());
		this.type = serverData.type;
		this.enforcesSecureChat = serverData.enforcesSecureChat;
	}

	public ServerData.State state() {
		return this.state;
	}

	public void setState(ServerData.State state) {
		this.state = state;
	}

	@Nullable
	public static byte[] validateIcon(@Nullable byte[] bs) {
		if (bs != null) {
			try {
				PngInfo pngInfo = PngInfo.fromBytes(bs);
				if (pngInfo.width() <= 1024 && pngInfo.height() <= 1024) {
					return bs;
				}
			} catch (IOException var2) {
				LOGGER.warn("Failed to decode server icon", (Throwable)var2);
			}
		}

		return null;
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

	@Environment(EnvType.CLIENT)
	public static enum State {
		INITIAL,
		PINGING,
		UNREACHABLE,
		INCOMPATIBLE,
		SUCCESSFUL;
	}

	@Environment(EnvType.CLIENT)
	public static enum Type {
		LAN,
		REALM,
		OTHER;
	}
}
