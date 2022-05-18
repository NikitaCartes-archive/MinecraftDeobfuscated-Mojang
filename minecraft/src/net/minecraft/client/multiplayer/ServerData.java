package net.minecraft.client.multiplayer;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ServerData {
	private static final Logger LOGGER = LogUtils.getLogger();
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
	@Nullable
	private ServerData.ChatPreview chatPreview;
	private boolean chatPreviewEnabled = true;

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

		if (this.chatPreview != null) {
			ServerData.ChatPreview.CODEC.encodeStart(NbtOps.INSTANCE, this.chatPreview).result().ifPresent(tag -> compoundTag.put("chatPreview", tag));
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

		if (compoundTag.contains("chatPreview", 10)) {
			ServerData.ChatPreview.CODEC
				.parse(NbtOps.INSTANCE, compoundTag.getCompound("chatPreview"))
				.resultOrPartial(LOGGER::error)
				.ifPresent(chatPreview -> serverData.chatPreview = chatPreview);
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

	public void setPreviewsChat(boolean bl) {
		if (bl && this.chatPreview == null) {
			this.chatPreview = new ServerData.ChatPreview(false, false);
		} else if (!bl && this.chatPreview != null) {
			this.chatPreview = null;
		}
	}

	@Nullable
	public ServerData.ChatPreview getChatPreview() {
		return this.chatPreview;
	}

	public void setChatPreviewEnabled(boolean bl) {
		this.chatPreviewEnabled = bl;
	}

	public boolean previewsChat() {
		return this.chatPreviewEnabled && this.chatPreview != null;
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
		this.chatPreview = Util.mapNullable(serverData.chatPreview, ServerData.ChatPreview::copy);
	}

	@Environment(EnvType.CLIENT)
	public static class ChatPreview {
		public static final Codec<ServerData.ChatPreview> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.BOOL.optionalFieldOf("acknowledged", Boolean.valueOf(false)).forGetter(chatPreview -> chatPreview.acknowledged),
						Codec.BOOL.optionalFieldOf("toastShown", Boolean.valueOf(false)).forGetter(chatPreview -> chatPreview.toastShown)
					)
					.apply(instance, ServerData.ChatPreview::new)
		);
		private boolean acknowledged;
		private boolean toastShown;

		ChatPreview(boolean bl, boolean bl2) {
			this.acknowledged = bl;
			this.toastShown = bl2;
		}

		public void acknowledge() {
			this.acknowledged = true;
		}

		public boolean showToast() {
			if (!this.toastShown) {
				this.toastShown = true;
				return true;
			} else {
				return false;
			}
		}

		public boolean isAcknowledged() {
			return this.acknowledged;
		}

		private ServerData.ChatPreview copy() {
			return new ServerData.ChatPreview(this.acknowledged, this.toastShown);
		}
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
