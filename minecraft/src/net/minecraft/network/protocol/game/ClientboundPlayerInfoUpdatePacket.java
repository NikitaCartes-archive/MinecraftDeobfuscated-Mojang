package net.minecraft.network.protocol.game;

import com.google.common.base.MoreObjects;
import com.mojang.authlib.GameProfile;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class ClientboundPlayerInfoUpdatePacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundPlayerInfoUpdatePacket> STREAM_CODEC = Packet.codec(
		ClientboundPlayerInfoUpdatePacket::write, ClientboundPlayerInfoUpdatePacket::new
	);
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

	private ClientboundPlayerInfoUpdatePacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		this.actions = registryFriendlyByteBuf.readEnumSet(ClientboundPlayerInfoUpdatePacket.Action.class);
		this.entries = registryFriendlyByteBuf.readList(friendlyByteBuf -> {
			ClientboundPlayerInfoUpdatePacket.EntryBuilder entryBuilder = new ClientboundPlayerInfoUpdatePacket.EntryBuilder(friendlyByteBuf.readUUID());

			for (ClientboundPlayerInfoUpdatePacket.Action action : this.actions) {
				action.reader.read(entryBuilder, (RegistryFriendlyByteBuf)friendlyByteBuf);
			}

			return entryBuilder.build();
		});
	}

	private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		registryFriendlyByteBuf.writeEnumSet(this.actions, ClientboundPlayerInfoUpdatePacket.Action.class);
		registryFriendlyByteBuf.writeCollection(this.entries, (friendlyByteBuf, entry) -> {
			friendlyByteBuf.writeUUID(entry.profileId());

			for (ClientboundPlayerInfoUpdatePacket.Action action : this.actions) {
				action.writer.write((RegistryFriendlyByteBuf)friendlyByteBuf, entry);
			}
		});
	}

	@Override
	public PacketType<ClientboundPlayerInfoUpdatePacket> type() {
		return GamePacketTypes.CLIENTBOUND_PLAYER_INFO_UPDATE;
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
		ADD_PLAYER((entryBuilder, registryFriendlyByteBuf) -> {
			GameProfile gameProfile = new GameProfile(entryBuilder.profileId, registryFriendlyByteBuf.readUtf(16));
			gameProfile.getProperties().putAll(registryFriendlyByteBuf.readGameProfileProperties());
			entryBuilder.profile = gameProfile;
		}, (registryFriendlyByteBuf, entry) -> {
			GameProfile gameProfile = (GameProfile)Objects.requireNonNull(entry.profile());
			registryFriendlyByteBuf.writeUtf(gameProfile.getName(), 16);
			registryFriendlyByteBuf.writeGameProfileProperties(gameProfile.getProperties());
		}),
		INITIALIZE_CHAT(
			(entryBuilder, registryFriendlyByteBuf) -> entryBuilder.chatSession = registryFriendlyByteBuf.readNullable(RemoteChatSession.Data::read),
			(registryFriendlyByteBuf, entry) -> registryFriendlyByteBuf.writeNullable(entry.chatSession, RemoteChatSession.Data::write)
		),
		UPDATE_GAME_MODE(
			(entryBuilder, registryFriendlyByteBuf) -> entryBuilder.gameMode = GameType.byId(registryFriendlyByteBuf.readVarInt()),
			(registryFriendlyByteBuf, entry) -> registryFriendlyByteBuf.writeVarInt(entry.gameMode().getId())
		),
		UPDATE_LISTED(
			(entryBuilder, registryFriendlyByteBuf) -> entryBuilder.listed = registryFriendlyByteBuf.readBoolean(),
			(registryFriendlyByteBuf, entry) -> registryFriendlyByteBuf.writeBoolean(entry.listed())
		),
		UPDATE_LATENCY(
			(entryBuilder, registryFriendlyByteBuf) -> entryBuilder.latency = registryFriendlyByteBuf.readVarInt(),
			(registryFriendlyByteBuf, entry) -> registryFriendlyByteBuf.writeVarInt(entry.latency())
		),
		UPDATE_DISPLAY_NAME(
			(entryBuilder, registryFriendlyByteBuf) -> entryBuilder.displayName = FriendlyByteBuf.readNullable(
					registryFriendlyByteBuf, ComponentSerialization.STREAM_CODEC
				),
			(registryFriendlyByteBuf, entry) -> FriendlyByteBuf.writeNullable(registryFriendlyByteBuf, entry.displayName(), ComponentSerialization.STREAM_CODEC)
		);

		final ClientboundPlayerInfoUpdatePacket.Action.Reader reader;
		final ClientboundPlayerInfoUpdatePacket.Action.Writer writer;

		private Action(ClientboundPlayerInfoUpdatePacket.Action.Reader reader, ClientboundPlayerInfoUpdatePacket.Action.Writer writer) {
			this.reader = reader;
			this.writer = writer;
		}

		public interface Reader {
			void read(ClientboundPlayerInfoUpdatePacket.EntryBuilder entryBuilder, RegistryFriendlyByteBuf registryFriendlyByteBuf);
		}

		public interface Writer {
			void write(RegistryFriendlyByteBuf registryFriendlyByteBuf, ClientboundPlayerInfoUpdatePacket.Entry entry);
		}
	}

	public static record Entry(
		UUID profileId,
		@Nullable GameProfile profile,
		boolean listed,
		int latency,
		GameType gameMode,
		@Nullable Component displayName,
		@Nullable RemoteChatSession.Data chatSession
	) {

		Entry(ServerPlayer serverPlayer) {
			this(
				serverPlayer.getUUID(),
				serverPlayer.getGameProfile(),
				true,
				serverPlayer.connection.latency(),
				serverPlayer.gameMode.getGameModeForPlayer(),
				serverPlayer.getTabListDisplayName(),
				Optionull.map(serverPlayer.getChatSession(), RemoteChatSession::asData)
			);
		}
	}

	static class EntryBuilder {
		final UUID profileId;
		@Nullable
		GameProfile profile;
		boolean listed;
		int latency;
		GameType gameMode = GameType.DEFAULT_MODE;
		@Nullable
		Component displayName;
		@Nullable
		RemoteChatSession.Data chatSession;

		EntryBuilder(UUID uUID) {
			this.profileId = uUID;
		}

		ClientboundPlayerInfoUpdatePacket.Entry build() {
			return new ClientboundPlayerInfoUpdatePacket.Entry(
				this.profileId, this.profile, this.listed, this.latency, this.gameMode, this.displayName, this.chatSession
			);
		}
	}
}
