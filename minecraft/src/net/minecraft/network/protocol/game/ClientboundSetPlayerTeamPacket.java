package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.scores.PlayerTeam;

public class ClientboundSetPlayerTeamPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSetPlayerTeamPacket> STREAM_CODEC = Packet.codec(
		ClientboundSetPlayerTeamPacket::write, ClientboundSetPlayerTeamPacket::new
	);
	private static final int METHOD_ADD = 0;
	private static final int METHOD_REMOVE = 1;
	private static final int METHOD_CHANGE = 2;
	private static final int METHOD_JOIN = 3;
	private static final int METHOD_LEAVE = 4;
	private static final int MAX_VISIBILITY_LENGTH = 40;
	private static final int MAX_COLLISION_LENGTH = 40;
	private final int method;
	private final String name;
	private final Collection<String> players;
	private final Optional<ClientboundSetPlayerTeamPacket.Parameters> parameters;

	private ClientboundSetPlayerTeamPacket(String string, int i, Optional<ClientboundSetPlayerTeamPacket.Parameters> optional, Collection<String> collection) {
		this.name = string;
		this.method = i;
		this.parameters = optional;
		this.players = ImmutableList.<String>copyOf(collection);
	}

	public static ClientboundSetPlayerTeamPacket createAddOrModifyPacket(PlayerTeam playerTeam, boolean bl) {
		return new ClientboundSetPlayerTeamPacket(
			playerTeam.getName(),
			bl ? 0 : 2,
			Optional.of(new ClientboundSetPlayerTeamPacket.Parameters(playerTeam)),
			(Collection<String>)(bl ? playerTeam.getPlayers() : ImmutableList.<String>of())
		);
	}

	public static ClientboundSetPlayerTeamPacket createRemovePacket(PlayerTeam playerTeam) {
		return new ClientboundSetPlayerTeamPacket(playerTeam.getName(), 1, Optional.empty(), ImmutableList.<String>of());
	}

	public static ClientboundSetPlayerTeamPacket createPlayerPacket(PlayerTeam playerTeam, String string, ClientboundSetPlayerTeamPacket.Action action) {
		return new ClientboundSetPlayerTeamPacket(
			playerTeam.getName(), action == ClientboundSetPlayerTeamPacket.Action.ADD ? 3 : 4, Optional.empty(), ImmutableList.<String>of(string)
		);
	}

	private ClientboundSetPlayerTeamPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		this.name = registryFriendlyByteBuf.readUtf();
		this.method = registryFriendlyByteBuf.readByte();
		if (shouldHaveParameters(this.method)) {
			this.parameters = Optional.of(new ClientboundSetPlayerTeamPacket.Parameters(registryFriendlyByteBuf));
		} else {
			this.parameters = Optional.empty();
		}

		if (shouldHavePlayerList(this.method)) {
			this.players = registryFriendlyByteBuf.<String>readList(FriendlyByteBuf::readUtf);
		} else {
			this.players = ImmutableList.<String>of();
		}
	}

	private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		registryFriendlyByteBuf.writeUtf(this.name);
		registryFriendlyByteBuf.writeByte(this.method);
		if (shouldHaveParameters(this.method)) {
			((ClientboundSetPlayerTeamPacket.Parameters)this.parameters
					.orElseThrow(() -> new IllegalStateException("Parameters not present, but method is" + this.method)))
				.write(registryFriendlyByteBuf);
		}

		if (shouldHavePlayerList(this.method)) {
			registryFriendlyByteBuf.writeCollection(this.players, FriendlyByteBuf::writeUtf);
		}
	}

	private static boolean shouldHavePlayerList(int i) {
		return i == 0 || i == 3 || i == 4;
	}

	private static boolean shouldHaveParameters(int i) {
		return i == 0 || i == 2;
	}

	@Nullable
	public ClientboundSetPlayerTeamPacket.Action getPlayerAction() {
		return switch (this.method) {
			case 0, 3 -> ClientboundSetPlayerTeamPacket.Action.ADD;
			default -> null;
			case 4 -> ClientboundSetPlayerTeamPacket.Action.REMOVE;
		};
	}

	@Nullable
	public ClientboundSetPlayerTeamPacket.Action getTeamAction() {
		return switch (this.method) {
			case 0 -> ClientboundSetPlayerTeamPacket.Action.ADD;
			case 1 -> ClientboundSetPlayerTeamPacket.Action.REMOVE;
			default -> null;
		};
	}

	@Override
	public PacketType<ClientboundSetPlayerTeamPacket> type() {
		return GamePacketTypes.CLIENTBOUND_SET_PLAYER_TEAM;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetPlayerTeamPacket(this);
	}

	public String getName() {
		return this.name;
	}

	public Collection<String> getPlayers() {
		return this.players;
	}

	public Optional<ClientboundSetPlayerTeamPacket.Parameters> getParameters() {
		return this.parameters;
	}

	public static enum Action {
		ADD,
		REMOVE;
	}

	public static class Parameters {
		private final Component displayName;
		private final Component playerPrefix;
		private final Component playerSuffix;
		private final String nametagVisibility;
		private final String collisionRule;
		private final ChatFormatting color;
		private final int options;

		public Parameters(PlayerTeam playerTeam) {
			this.displayName = playerTeam.getDisplayName();
			this.options = playerTeam.packOptions();
			this.nametagVisibility = playerTeam.getNameTagVisibility().name;
			this.collisionRule = playerTeam.getCollisionRule().name;
			this.color = playerTeam.getColor();
			this.playerPrefix = playerTeam.getPlayerPrefix();
			this.playerSuffix = playerTeam.getPlayerSuffix();
		}

		public Parameters(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			this.displayName = ComponentSerialization.STREAM_CODEC.decode(registryFriendlyByteBuf);
			this.options = registryFriendlyByteBuf.readByte();
			this.nametagVisibility = registryFriendlyByteBuf.readUtf(40);
			this.collisionRule = registryFriendlyByteBuf.readUtf(40);
			this.color = registryFriendlyByteBuf.readEnum(ChatFormatting.class);
			this.playerPrefix = ComponentSerialization.STREAM_CODEC.decode(registryFriendlyByteBuf);
			this.playerSuffix = ComponentSerialization.STREAM_CODEC.decode(registryFriendlyByteBuf);
		}

		public Component getDisplayName() {
			return this.displayName;
		}

		public int getOptions() {
			return this.options;
		}

		public ChatFormatting getColor() {
			return this.color;
		}

		public String getNametagVisibility() {
			return this.nametagVisibility;
		}

		public String getCollisionRule() {
			return this.collisionRule;
		}

		public Component getPlayerPrefix() {
			return this.playerPrefix;
		}

		public Component getPlayerSuffix() {
			return this.playerSuffix;
		}

		public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
			ComponentSerialization.STREAM_CODEC.encode(registryFriendlyByteBuf, this.displayName);
			registryFriendlyByteBuf.writeByte(this.options);
			registryFriendlyByteBuf.writeUtf(this.nametagVisibility);
			registryFriendlyByteBuf.writeUtf(this.collisionRule);
			registryFriendlyByteBuf.writeEnum(this.color);
			ComponentSerialization.STREAM_CODEC.encode(registryFriendlyByteBuf, this.playerPrefix);
			ComponentSerialization.STREAM_CODEC.encode(registryFriendlyByteBuf, this.playerSuffix);
		}
	}
}
