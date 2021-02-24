package net.minecraft.network.protocol.game;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.util.Collection;
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
	private final ClientboundPlayerInfoPacket.Action action;
	private final List<ClientboundPlayerInfoPacket.PlayerUpdate> entries;

	public ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action action, ServerPlayer... serverPlayers) {
		this.action = action;
		this.entries = Lists.<ClientboundPlayerInfoPacket.PlayerUpdate>newArrayListWithCapacity(serverPlayers.length);

		for (ServerPlayer serverPlayer : serverPlayers) {
			this.entries
				.add(
					new ClientboundPlayerInfoPacket.PlayerUpdate(
						serverPlayer.getGameProfile(), serverPlayer.latency, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer.getTabListDisplayName()
					)
				);
		}
	}

	public ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action action, Collection<ServerPlayer> collection) {
		this.action = action;
		this.entries = Lists.<ClientboundPlayerInfoPacket.PlayerUpdate>newArrayListWithCapacity(collection.size());

		for (ServerPlayer serverPlayer : collection) {
			this.entries
				.add(
					new ClientboundPlayerInfoPacket.PlayerUpdate(
						serverPlayer.getGameProfile(), serverPlayer.latency, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer.getTabListDisplayName()
					)
				);
		}
	}

	public ClientboundPlayerInfoPacket(FriendlyByteBuf friendlyByteBuf) {
		this.action = friendlyByteBuf.readEnum(ClientboundPlayerInfoPacket.Action.class);
		this.entries = friendlyByteBuf.readList(this.action::read);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeEnum(this.action);
		friendlyByteBuf.writeCollection(this.entries, this.action::write);
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

	@Nullable
	private static Component readDisplayName(FriendlyByteBuf friendlyByteBuf) {
		return friendlyByteBuf.readBoolean() ? friendlyByteBuf.readComponent() : null;
	}

	private static void writeDisplayName(FriendlyByteBuf friendlyByteBuf, @Nullable Component component) {
		if (component == null) {
			friendlyByteBuf.writeBoolean(false);
		} else {
			friendlyByteBuf.writeBoolean(true);
			friendlyByteBuf.writeComponent(component);
		}
	}

	public String toString() {
		return MoreObjects.toStringHelper(this).add("action", this.action).add("entries", this.entries).toString();
	}

	public static enum Action {
		ADD_PLAYER {
			@Override
			protected ClientboundPlayerInfoPacket.PlayerUpdate read(FriendlyByteBuf friendlyByteBuf) {
				GameProfile gameProfile = new GameProfile(friendlyByteBuf.readUUID(), friendlyByteBuf.readUtf(16));
				PropertyMap propertyMap = gameProfile.getProperties();
				friendlyByteBuf.readWithCount(friendlyByteBufx -> {
					String string = friendlyByteBufx.readUtf();
					String string2 = friendlyByteBufx.readUtf();
					if (friendlyByteBufx.readBoolean()) {
						String string3 = friendlyByteBufx.readUtf();
						propertyMap.put(string, new Property(string, string2, string3));
					} else {
						propertyMap.put(string, new Property(string, string2));
					}
				});
				GameType gameType = GameType.byId(friendlyByteBuf.readVarInt());
				int i = friendlyByteBuf.readVarInt();
				Component component = ClientboundPlayerInfoPacket.readDisplayName(friendlyByteBuf);
				return new ClientboundPlayerInfoPacket.PlayerUpdate(gameProfile, i, gameType, component);
			}

			@Override
			protected void write(FriendlyByteBuf friendlyByteBuf, ClientboundPlayerInfoPacket.PlayerUpdate playerUpdate) {
				friendlyByteBuf.writeUUID(playerUpdate.getProfile().getId());
				friendlyByteBuf.writeUtf(playerUpdate.getProfile().getName());
				friendlyByteBuf.writeCollection(playerUpdate.getProfile().getProperties().values(), (friendlyByteBufx, property) -> {
					friendlyByteBufx.writeUtf(property.getName());
					friendlyByteBufx.writeUtf(property.getValue());
					if (property.hasSignature()) {
						friendlyByteBufx.writeBoolean(true);
						friendlyByteBufx.writeUtf(property.getSignature());
					} else {
						friendlyByteBufx.writeBoolean(false);
					}
				});
				friendlyByteBuf.writeVarInt(playerUpdate.getGameMode().getId());
				friendlyByteBuf.writeVarInt(playerUpdate.getLatency());
				ClientboundPlayerInfoPacket.writeDisplayName(friendlyByteBuf, playerUpdate.getDisplayName());
			}
		},
		UPDATE_GAME_MODE {
			@Override
			protected ClientboundPlayerInfoPacket.PlayerUpdate read(FriendlyByteBuf friendlyByteBuf) {
				GameProfile gameProfile = new GameProfile(friendlyByteBuf.readUUID(), null);
				GameType gameType = GameType.byId(friendlyByteBuf.readVarInt());
				return new ClientboundPlayerInfoPacket.PlayerUpdate(gameProfile, 0, gameType, null);
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
				return new ClientboundPlayerInfoPacket.PlayerUpdate(gameProfile, i, null, null);
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
				Component component = ClientboundPlayerInfoPacket.readDisplayName(friendlyByteBuf);
				return new ClientboundPlayerInfoPacket.PlayerUpdate(gameProfile, 0, null, component);
			}

			@Override
			protected void write(FriendlyByteBuf friendlyByteBuf, ClientboundPlayerInfoPacket.PlayerUpdate playerUpdate) {
				friendlyByteBuf.writeUUID(playerUpdate.getProfile().getId());
				ClientboundPlayerInfoPacket.writeDisplayName(friendlyByteBuf, playerUpdate.getDisplayName());
			}
		},
		REMOVE_PLAYER {
			@Override
			protected ClientboundPlayerInfoPacket.PlayerUpdate read(FriendlyByteBuf friendlyByteBuf) {
				GameProfile gameProfile = new GameProfile(friendlyByteBuf.readUUID(), null);
				return new ClientboundPlayerInfoPacket.PlayerUpdate(gameProfile, 0, null, null);
			}

			@Override
			protected void write(FriendlyByteBuf friendlyByteBuf, ClientboundPlayerInfoPacket.PlayerUpdate playerUpdate) {
				friendlyByteBuf.writeUUID(playerUpdate.getProfile().getId());
			}
		};

		private Action() {
		}

		protected abstract ClientboundPlayerInfoPacket.PlayerUpdate read(FriendlyByteBuf friendlyByteBuf);

		protected abstract void write(FriendlyByteBuf friendlyByteBuf, ClientboundPlayerInfoPacket.PlayerUpdate playerUpdate);
	}

	public static class PlayerUpdate {
		private final int latency;
		private final GameType gameMode;
		private final GameProfile profile;
		@Nullable
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
