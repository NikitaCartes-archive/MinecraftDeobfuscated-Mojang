package net.minecraft.network.protocol.game;

import com.google.common.base.MoreObjects;
import com.mojang.authlib.GameProfile;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class ClientboundPlayerInfoUpdatePacket implements Packet<ClientGamePacketListener> {
	private final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions;
	private final List<ClientboundPlayerInfoUpdatePacket.Entry> entries;

	public ClientboundPlayerInfoUpdatePacket(EnumSet<ClientboundPlayerInfoUpdatePacket.Action> enumSet, Collection<ServerPlayer> collection) {
		this.actions = enumSet;
		this.entries = collection.stream().map(ClientboundPlayerInfoUpdatePacket.Entry::new).toList();
	}

	public ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action action, ServerPlayer serverPlayer) {
		this.actions = EnumSet.of(action);
		this.entries = List.of(new ClientboundPlayerInfoUpdatePacket.Entry(serverPlayer));
	}

	public static ClientboundPlayerInfoUpdatePacket createPlayerInitializing(Collection<ServerPlayer> collection) {
		EnumSet<ClientboundPlayerInfoUpdatePacket.Action> enumSet = EnumSet.of(
			ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
			ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT,
			ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE,
			ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
			ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY,
			ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME
		);
		return new ClientboundPlayerInfoUpdatePacket(enumSet, collection);
	}

	public ClientboundPlayerInfoUpdatePacket(FriendlyByteBuf friendlyByteBuf) {
		this.actions = friendlyByteBuf.readEnumSet(ClientboundPlayerInfoUpdatePacket.Action.class);
		this.entries = friendlyByteBuf.readList(friendlyByteBufx -> {
			ClientboundPlayerInfoUpdatePacket.EntryBuilder entryBuilder = new ClientboundPlayerInfoUpdatePacket.EntryBuilder(friendlyByteBufx.readUUID());

			for (ClientboundPlayerInfoUpdatePacket.Action action : this.actions) {
				action.reader.read(entryBuilder, friendlyByteBufx);
			}

			return entryBuilder.build();
		});
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeEnumSet(this.actions, ClientboundPlayerInfoUpdatePacket.Action.class);
		friendlyByteBuf.writeCollection(this.entries, (friendlyByteBufx, entry) -> {
			friendlyByteBufx.writeUUID(entry.profileId());

			for (ClientboundPlayerInfoUpdatePacket.Action action : this.actions) {
				action.writer.write(friendlyByteBufx, entry);
			}
		});
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlayerInfoUpdate(this);
	}

	public EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions() {
		return this.actions;
	}

	public List<ClientboundPlayerInfoUpdatePacket.Entry> entries() {
		return this.entries;
	}

	public List<ClientboundPlayerInfoUpdatePacket.Entry> newEntries() {
		return this.actions.contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER) ? this.entries : List.of();
	}

	public String toString() {
		return MoreObjects.toStringHelper(this).add("actions", this.actions).add("entries", this.entries).toString();
	}

	public static enum Action {
		ADD_PLAYER((entryBuilder, friendlyByteBuf) -> {
			GameProfile gameProfile = new GameProfile(entryBuilder.profileId, friendlyByteBuf.readUtf(16));
			gameProfile.getProperties().putAll(friendlyByteBuf.readGameProfileProperties());
			entryBuilder.profile = gameProfile;
		}, (friendlyByteBuf, entry) -> {
			friendlyByteBuf.writeUtf(entry.profile().getName(), 16);
			friendlyByteBuf.writeGameProfileProperties(entry.profile().getProperties());
		}),
		INITIALIZE_CHAT(
			(entryBuilder, friendlyByteBuf) -> entryBuilder.chatSession = RemoteChatSession.Data.read(friendlyByteBuf),
			(friendlyByteBuf, entry) -> RemoteChatSession.Data.write(friendlyByteBuf, entry.chatSession())
		),
		UPDATE_GAME_MODE(
			(entryBuilder, friendlyByteBuf) -> entryBuilder.gameMode = GameType.byId(friendlyByteBuf.readVarInt()),
			(friendlyByteBuf, entry) -> friendlyByteBuf.writeVarInt(entry.gameMode().getId())
		),
		UPDATE_LISTED(
			(entryBuilder, friendlyByteBuf) -> entryBuilder.listed = friendlyByteBuf.readBoolean(),
			(friendlyByteBuf, entry) -> friendlyByteBuf.writeBoolean(entry.listed())
		),
		UPDATE_LATENCY(
			(entryBuilder, friendlyByteBuf) -> entryBuilder.latency = friendlyByteBuf.readVarInt(),
			(friendlyByteBuf, entry) -> friendlyByteBuf.writeVarInt(entry.latency())
		),
		UPDATE_DISPLAY_NAME(
			(entryBuilder, friendlyByteBuf) -> entryBuilder.displayName = friendlyByteBuf.readNullable(FriendlyByteBuf::readComponent),
			(friendlyByteBuf, entry) -> friendlyByteBuf.writeNullable(entry.displayName(), FriendlyByteBuf::writeComponent)
		);

		final ClientboundPlayerInfoUpdatePacket.Action.Reader reader;
		final ClientboundPlayerInfoUpdatePacket.Action.Writer writer;

		private Action(ClientboundPlayerInfoUpdatePacket.Action.Reader reader, ClientboundPlayerInfoUpdatePacket.Action.Writer writer) {
			this.reader = reader;
			this.writer = writer;
		}

		public interface Reader {
			void read(ClientboundPlayerInfoUpdatePacket.EntryBuilder entryBuilder, FriendlyByteBuf friendlyByteBuf);
		}

		public interface Writer {
			void write(FriendlyByteBuf friendlyByteBuf, ClientboundPlayerInfoUpdatePacket.Entry entry);
		}
	}

	public static record Entry(
		UUID profileId, GameProfile profile, boolean listed, int latency, GameType gameMode, @Nullable Component displayName, RemoteChatSession.Data chatSession
	) {
		Entry(ServerPlayer serverPlayer) {
			this(
				serverPlayer.getUUID(),
				serverPlayer.getGameProfile(),
				true,
				serverPlayer.latency,
				serverPlayer.gameMode.getGameModeForPlayer(),
				serverPlayer.getTabListDisplayName(),
				serverPlayer.getChatSession().asData()
			);
		}
	}

	static class EntryBuilder {
		final UUID profileId;
		GameProfile profile;
		boolean listed;
		int latency;
		GameType gameMode = GameType.DEFAULT_MODE;
		@Nullable
		Component displayName;
		RemoteChatSession.Data chatSession;

		EntryBuilder(UUID uUID) {
			this.profileId = uUID;
			this.profile = new GameProfile(uUID, null);
		}

		ClientboundPlayerInfoUpdatePacket.Entry build() {
			return new ClientboundPlayerInfoUpdatePacket.Entry(
				this.profileId, this.profile, this.listed, this.latency, this.gameMode, this.displayName, this.chatSession
			);
		}
	}
}
