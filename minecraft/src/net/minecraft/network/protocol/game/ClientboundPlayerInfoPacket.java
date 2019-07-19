package net.minecraft.network.protocol.game;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class ClientboundPlayerInfoPacket implements Packet<ClientGamePacketListener> {
	private ClientboundPlayerInfoPacket.Action action;
	private final List<ClientboundPlayerInfoPacket.PlayerUpdate> entries = Lists.<ClientboundPlayerInfoPacket.PlayerUpdate>newArrayList();

	public ClientboundPlayerInfoPacket() {
	}

	public ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action action, ServerPlayer... serverPlayers) {
		this.action = action;

		for (ServerPlayer serverPlayer : serverPlayers) {
			this.entries
				.add(
					new ClientboundPlayerInfoPacket.PlayerUpdate(
						serverPlayer.getGameProfile(), serverPlayer.latency, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer.getTabListDisplayName()
					)
				);
		}
	}

	public ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action action, Iterable<ServerPlayer> iterable) {
		this.action = action;

		for (ServerPlayer serverPlayer : iterable) {
			this.entries
				.add(
					new ClientboundPlayerInfoPacket.PlayerUpdate(
						serverPlayer.getGameProfile(), serverPlayer.latency, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer.getTabListDisplayName()
					)
				);
		}
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.action = friendlyByteBuf.readEnum(ClientboundPlayerInfoPacket.Action.class);
		int i = friendlyByteBuf.readVarInt();

		for (int j = 0; j < i; j++) {
			GameProfile gameProfile = null;
			int k = 0;
			GameType gameType = null;
			Component component = null;
			switch (this.action) {
				case ADD_PLAYER:
					gameProfile = new GameProfile(friendlyByteBuf.readUUID(), friendlyByteBuf.readUtf(16));
					int l = friendlyByteBuf.readVarInt();
					int m = 0;

					for (; m < l; m++) {
						String string = friendlyByteBuf.readUtf(32767);
						String string2 = friendlyByteBuf.readUtf(32767);
						if (friendlyByteBuf.readBoolean()) {
							gameProfile.getProperties().put(string, new Property(string, string2, friendlyByteBuf.readUtf(32767)));
						} else {
							gameProfile.getProperties().put(string, new Property(string, string2));
						}
					}

					gameType = GameType.byId(friendlyByteBuf.readVarInt());
					k = friendlyByteBuf.readVarInt();
					if (friendlyByteBuf.readBoolean()) {
						component = friendlyByteBuf.readComponent();
					}
					break;
				case UPDATE_GAME_MODE:
					gameProfile = new GameProfile(friendlyByteBuf.readUUID(), null);
					gameType = GameType.byId(friendlyByteBuf.readVarInt());
					break;
				case UPDATE_LATENCY:
					gameProfile = new GameProfile(friendlyByteBuf.readUUID(), null);
					k = friendlyByteBuf.readVarInt();
					break;
				case UPDATE_DISPLAY_NAME:
					gameProfile = new GameProfile(friendlyByteBuf.readUUID(), null);
					if (friendlyByteBuf.readBoolean()) {
						component = friendlyByteBuf.readComponent();
					}
					break;
				case REMOVE_PLAYER:
					gameProfile = new GameProfile(friendlyByteBuf.readUUID(), null);
			}

			this.entries.add(new ClientboundPlayerInfoPacket.PlayerUpdate(gameProfile, k, gameType, component));
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeEnum(this.action);
		friendlyByteBuf.writeVarInt(this.entries.size());

		for (ClientboundPlayerInfoPacket.PlayerUpdate playerUpdate : this.entries) {
			switch (this.action) {
				case ADD_PLAYER:
					friendlyByteBuf.writeUUID(playerUpdate.getProfile().getId());
					friendlyByteBuf.writeUtf(playerUpdate.getProfile().getName());
					friendlyByteBuf.writeVarInt(playerUpdate.getProfile().getProperties().size());

					for (Property property : playerUpdate.getProfile().getProperties().values()) {
						friendlyByteBuf.writeUtf(property.getName());
						friendlyByteBuf.writeUtf(property.getValue());
						if (property.hasSignature()) {
							friendlyByteBuf.writeBoolean(true);
							friendlyByteBuf.writeUtf(property.getSignature());
						} else {
							friendlyByteBuf.writeBoolean(false);
						}
					}

					friendlyByteBuf.writeVarInt(playerUpdate.getGameMode().getId());
					friendlyByteBuf.writeVarInt(playerUpdate.getLatency());
					if (playerUpdate.getDisplayName() == null) {
						friendlyByteBuf.writeBoolean(false);
					} else {
						friendlyByteBuf.writeBoolean(true);
						friendlyByteBuf.writeComponent(playerUpdate.getDisplayName());
					}
					break;
				case UPDATE_GAME_MODE:
					friendlyByteBuf.writeUUID(playerUpdate.getProfile().getId());
					friendlyByteBuf.writeVarInt(playerUpdate.getGameMode().getId());
					break;
				case UPDATE_LATENCY:
					friendlyByteBuf.writeUUID(playerUpdate.getProfile().getId());
					friendlyByteBuf.writeVarInt(playerUpdate.getLatency());
					break;
				case UPDATE_DISPLAY_NAME:
					friendlyByteBuf.writeUUID(playerUpdate.getProfile().getId());
					if (playerUpdate.getDisplayName() == null) {
						friendlyByteBuf.writeBoolean(false);
					} else {
						friendlyByteBuf.writeBoolean(true);
						friendlyByteBuf.writeComponent(playerUpdate.getDisplayName());
					}
					break;
				case REMOVE_PLAYER:
					friendlyByteBuf.writeUUID(playerUpdate.getProfile().getId());
			}
		}
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlayerInfo(this);
	}

	@Environment(EnvType.CLIENT)
	public List<ClientboundPlayerInfoPacket.PlayerUpdate> getEntries() {
		return this.entries;
	}

	@Environment(EnvType.CLIENT)
	public ClientboundPlayerInfoPacket.Action getAction() {
		return this.action;
	}

	public String toString() {
		return MoreObjects.toStringHelper(this).add("action", this.action).add("entries", this.entries).toString();
	}

	public static enum Action {
		ADD_PLAYER,
		UPDATE_GAME_MODE,
		UPDATE_LATENCY,
		UPDATE_DISPLAY_NAME,
		REMOVE_PLAYER;
	}

	public class PlayerUpdate {
		private final int latency;
		private final GameType gameMode;
		private final GameProfile profile;
		private final Component displayName;

		public PlayerUpdate(GameProfile gameProfile, int i, @Nullable GameType gameType, @Nullable Component component) {
			this.profile = gameProfile;
			this.latency = i;
			this.gameMode = gameType;
			this.displayName = component;
		}

		public GameProfile getProfile() {
			return this.profile;
		}

		public int getLatency() {
			return this.latency;
		}

		public GameType getGameMode() {
			return this.gameMode;
		}

		@Nullable
		public Component getDisplayName() {
			return this.displayName;
		}

		public String toString() {
			return MoreObjects.toStringHelper(this)
				.add("latency", this.latency)
				.add("gameMode", this.gameMode)
				.add("profile", this.profile)
				.add("displayName", this.displayName == null ? null : Component.Serializer.toJson(this.displayName))
				.toString();
		}
	}
}
