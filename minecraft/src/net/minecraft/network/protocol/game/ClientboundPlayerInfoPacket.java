package net.minecraft.network.protocol.game;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.GameType;

public class ClientboundPlayerInfoPacket implements Packet<ClientGamePacketListener> {
	private final ClientboundPlayerInfoPacket.Action action;
	private final List<ClientboundPlayerInfoPacket.PlayerUpdate> entries;

	public ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action action, ServerPlayer... serverPlayers) {
		this.action = action;
		this.entries = Lists.<ClientboundPlayerInfoPacket.PlayerUpdate>newArrayListWithCapacity(serverPlayers.length);

		for (ServerPlayer serverPlayer : serverPlayers) {
			this.entries.add(createPlayerUpdate(serverPlayer));
		}
	}

	public ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action action, Collection<ServerPlayer> collection) {
		this.action = action;
		this.entries = Lists.<ClientboundPlayerInfoPacket.PlayerUpdate>newArrayListWithCapacity(collection.size());

		for (ServerPlayer serverPlayer : collection) {
			this.entries.add(createPlayerUpdate(serverPlayer));
		}
	}

	public ClientboundPlayerInfoPacket(FriendlyByteBuf friendlyByteBuf) {
		this.action = friendlyByteBuf.readEnum(ClientboundPlayerInfoPacket.Action.class);
		this.entries = friendlyByteBuf.readList(this.action::read);
	}

	private static ClientboundPlayerInfoPacket.PlayerUpdate createPlayerUpdate(ServerPlayer serverPlayer) {
		ProfilePublicKey profilePublicKey = serverPlayer.getProfilePublicKey();
		ProfilePublicKey.Data data = profilePublicKey != null ? profilePublicKey.data() : null;
		return new ClientboundPlayerInfoPacket.PlayerUpdate(
			serverPlayer.getGameProfile(), serverPlayer.latency, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer.getTabListDisplayName(), data
		);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeEnum(this.action);
		friendlyByteBuf.writeCollection(this.entries, this.action::write);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlayerInfo(this);
	}

	public List<ClientboundPlayerInfoPacket.PlayerUpdate> getEntries() {
		return this.entries;
	}

	public ClientboundPlayerInfoPacket.Action getAction() {
		return this.action;
	}

	public String toString() {
		return MoreObjects.toStringHelper(this).add("action", this.action).add("entries", this.entries).toString();
	}

	public static enum Action {
		ADD_PLAYER {
			@Override
			protected ClientboundPlayerInfoPacket.PlayerUpdate read(FriendlyByteBuf friendlyByteBuf) {
				GameProfile gameProfile = friendlyByteBuf.readGameProfile();
				GameType gameType = GameType.byId(friendlyByteBuf.readVarInt());
				int i = friendlyByteBuf.readVarInt();
				Component component = friendlyByteBuf.readNullable(FriendlyByteBuf::readComponent);
				ProfilePublicKey.Data data = friendlyByteBuf.readNullable(ProfilePublicKey.Data::new);
				return new ClientboundPlayerInfoPacket.PlayerUpdate(gameProfile, i, gameType, component, data);
			}

			@Override
			protected void write(FriendlyByteBuf friendlyByteBuf, ClientboundPlayerInfoPacket.PlayerUpdate playerUpdate) {
				friendlyByteBuf.writeGameProfile(playerUpdate.getProfile());
				friendlyByteBuf.writeVarInt(playerUpdate.getGameMode().getId());
				friendlyByteBuf.writeVarInt(playerUpdate.getLatency());
				friendlyByteBuf.writeNullable(playerUpdate.getDisplayName(), FriendlyByteBuf::writeComponent);
				friendlyByteBuf.writeNullable(playerUpdate.getProfilePublicKey(), (friendlyByteBufx, data) -> data.write(friendlyByteBufx));
			}
		},
		UPDATE_GAME_MODE {
			@Override
			protected ClientboundPlayerInfoPacket.PlayerUpdate read(FriendlyByteBuf friendlyByteBuf) {
				GameProfile gameProfile = new GameProfile(friendlyByteBuf.readUUID(), null);
				GameType gameType = GameType.byId(friendlyByteBuf.readVarInt());
				return new ClientboundPlayerInfoPacket.PlayerUpdate(gameProfile, 0, gameType, null, null);
			}

			@Override
			protected void write(FriendlyByteBuf friendlyByteBuf, ClientboundPlayerInfoPacket.PlayerUpdate playerUpdate) {
				friendlyByteBuf.writeUUID(playerUpdate.getProfile().getId());
				friendlyByteBuf.writeVarInt(playerUpdate.getGameMode().getId());
			}
		},
		UPDATE_LATENCY {
			@Override
			protected ClientboundPlayerInfoPacket.PlayerUpdate read(FriendlyByteBuf friendlyByteBuf) {
				GameProfile gameProfile = new GameProfile(friendlyByteBuf.readUUID(), null);
				int i = friendlyByteBuf.readVarInt();
				return new ClientboundPlayerInfoPacket.PlayerUpdate(gameProfile, i, null, null, null);
			}

			@Override
			protected void write(FriendlyByteBuf friendlyByteBuf, ClientboundPlayerInfoPacket.PlayerUpdate playerUpdate) {
				friendlyByteBuf.writeUUID(playerUpdate.getProfile().getId());
				friendlyByteBuf.writeVarInt(playerUpdate.getLatency());
			}
		},
		UPDATE_DISPLAY_NAME {
			@Override
			protected ClientboundPlayerInfoPacket.PlayerUpdate read(FriendlyByteBuf friendlyByteBuf) {
				GameProfile gameProfile = new GameProfile(friendlyByteBuf.readUUID(), null);
				Component component = friendlyByteBuf.readNullable(FriendlyByteBuf::readComponent);
				return new ClientboundPlayerInfoPacket.PlayerUpdate(gameProfile, 0, null, component, null);
			}

			@Override
			protected void write(FriendlyByteBuf friendlyByteBuf, ClientboundPlayerInfoPacket.PlayerUpdate playerUpdate) {
				friendlyByteBuf.writeUUID(playerUpdate.getProfile().getId());
				friendlyByteBuf.writeNullable(playerUpdate.getDisplayName(), FriendlyByteBuf::writeComponent);
			}
		},
		REMOVE_PLAYER {
			@Override
			protected ClientboundPlayerInfoPacket.PlayerUpdate read(FriendlyByteBuf friendlyByteBuf) {
				GameProfile gameProfile = new GameProfile(friendlyByteBuf.readUUID(), null);
				return new ClientboundPlayerInfoPacket.PlayerUpdate(gameProfile, 0, null, null, null);
			}

			@Override
			protected void write(FriendlyByteBuf friendlyByteBuf, ClientboundPlayerInfoPacket.PlayerUpdate playerUpdate) {
				friendlyByteBuf.writeUUID(playerUpdate.getProfile().getId());
			}
		};

		protected abstract ClientboundPlayerInfoPacket.PlayerUpdate read(FriendlyByteBuf friendlyByteBuf);

		protected abstract void write(FriendlyByteBuf friendlyByteBuf, ClientboundPlayerInfoPacket.PlayerUpdate playerUpdate);
	}

	public static class PlayerUpdate {
		private final int latency;
		private final GameType gameMode;
		private final GameProfile profile;
		@Nullable
		private final Component displayName;
		@Nullable
		private final ProfilePublicKey.Data profilePublicKey;

		public PlayerUpdate(GameProfile gameProfile, int i, @Nullable GameType gameType, @Nullable Component component, @Nullable ProfilePublicKey.Data data) {
			this.profile = gameProfile;
			this.latency = i;
			this.gameMode = gameType;
			this.displayName = component;
			this.profilePublicKey = data;
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

		@Nullable
		public ProfilePublicKey.Data getProfilePublicKey() {
			return this.profilePublicKey;
		}

		public String toString() {
			return MoreObjects.toStringHelper(this)
				.add("latency", this.latency)
				.add("gameMode", this.gameMode)
				.add("profile", this.profile)
				.add("displayName", this.displayName == null ? null : Component.Serializer.toJson(this.displayName))
				.add("profilePublicKey", this.profilePublicKey)
				.toString();
		}
	}
}
