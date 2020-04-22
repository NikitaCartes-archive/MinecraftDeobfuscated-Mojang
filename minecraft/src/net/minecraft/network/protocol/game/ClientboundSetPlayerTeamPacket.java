package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

public class ClientboundSetPlayerTeamPacket implements Packet<ClientGamePacketListener> {
	private String name = "";
	private Component displayName = TextComponent.EMPTY;
	private Component playerPrefix = TextComponent.EMPTY;
	private Component playerSuffix = TextComponent.EMPTY;
	private String nametagVisibility = Team.Visibility.ALWAYS.name;
	private String collisionRule = Team.CollisionRule.ALWAYS.name;
	private ChatFormatting color = ChatFormatting.RESET;
	private final Collection<String> players = Lists.<String>newArrayList();
	private int method;
	private int options;

	public ClientboundSetPlayerTeamPacket() {
	}

	public ClientboundSetPlayerTeamPacket(PlayerTeam playerTeam, int i) {
		this.name = playerTeam.getName();
		this.method = i;
		if (i == 0 || i == 2) {
			this.displayName = playerTeam.getDisplayName();
			this.options = playerTeam.packOptions();
			this.nametagVisibility = playerTeam.getNameTagVisibility().name;
			this.collisionRule = playerTeam.getCollisionRule().name;
			this.color = playerTeam.getColor();
			this.playerPrefix = playerTeam.getPlayerPrefix();
			this.playerSuffix = playerTeam.getPlayerSuffix();
		}

		if (i == 0) {
			this.players.addAll(playerTeam.getPlayers());
		}
	}

	public ClientboundSetPlayerTeamPacket(PlayerTeam playerTeam, Collection<String> collection, int i) {
		if (i != 3 && i != 4) {
			throw new IllegalArgumentException("Method must be join or leave for player constructor");
		} else if (collection != null && !collection.isEmpty()) {
			this.method = i;
			this.name = playerTeam.getName();
			this.players.addAll(collection);
		} else {
			throw new IllegalArgumentException("Players cannot be null/empty");
		}
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.name = friendlyByteBuf.readUtf(16);
		this.method = friendlyByteBuf.readByte();
		if (this.method == 0 || this.method == 2) {
			this.displayName = friendlyByteBuf.readComponent();
			this.options = friendlyByteBuf.readByte();
			this.nametagVisibility = friendlyByteBuf.readUtf(40);
			this.collisionRule = friendlyByteBuf.readUtf(40);
			this.color = friendlyByteBuf.readEnum(ChatFormatting.class);
			this.playerPrefix = friendlyByteBuf.readComponent();
			this.playerSuffix = friendlyByteBuf.readComponent();
		}

		if (this.method == 0 || this.method == 3 || this.method == 4) {
			int i = friendlyByteBuf.readVarInt();

			for (int j = 0; j < i; j++) {
				this.players.add(friendlyByteBuf.readUtf(40));
			}
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeUtf(this.name);
		friendlyByteBuf.writeByte(this.method);
		if (this.method == 0 || this.method == 2) {
			friendlyByteBuf.writeComponent(this.displayName);
			friendlyByteBuf.writeByte(this.options);
			friendlyByteBuf.writeUtf(this.nametagVisibility);
			friendlyByteBuf.writeUtf(this.collisionRule);
			friendlyByteBuf.writeEnum(this.color);
			friendlyByteBuf.writeComponent(this.playerPrefix);
			friendlyByteBuf.writeComponent(this.playerSuffix);
		}

		if (this.method == 0 || this.method == 3 || this.method == 4) {
			friendlyByteBuf.writeVarInt(this.players.size());

			for (String string : this.players) {
				friendlyByteBuf.writeUtf(string);
			}
		}
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetPlayerTeamPacket(this);
	}

	@Environment(EnvType.CLIENT)
	public String getName() {
		return this.name;
	}

	@Environment(EnvType.CLIENT)
	public Component getDisplayName() {
		return this.displayName;
	}

	@Environment(EnvType.CLIENT)
	public Collection<String> getPlayers() {
		return this.players;
	}

	@Environment(EnvType.CLIENT)
	public int getMethod() {
		return this.method;
	}

	@Environment(EnvType.CLIENT)
	public int getOptions() {
		return this.options;
	}

	@Environment(EnvType.CLIENT)
	public ChatFormatting getColor() {
		return this.color;
	}

	@Environment(EnvType.CLIENT)
	public String getNametagVisibility() {
		return this.nametagVisibility;
	}

	@Environment(EnvType.CLIENT)
	public String getCollisionRule() {
		return this.collisionRule;
	}

	@Environment(EnvType.CLIENT)
	public Component getPlayerPrefix() {
		return this.playerPrefix;
	}

	@Environment(EnvType.CLIENT)
	public Component getPlayerSuffix() {
		return this.playerSuffix;
	}
}
