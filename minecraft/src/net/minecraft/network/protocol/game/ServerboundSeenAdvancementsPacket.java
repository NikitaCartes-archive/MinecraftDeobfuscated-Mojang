package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;

public class ServerboundSeenAdvancementsPacket implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundSeenAdvancementsPacket> STREAM_CODEC = Packet.codec(
		ServerboundSeenAdvancementsPacket::write, ServerboundSeenAdvancementsPacket::new
	);
	private final ServerboundSeenAdvancementsPacket.Action action;
	@Nullable
	private final ResourceLocation tab;

	public ServerboundSeenAdvancementsPacket(ServerboundSeenAdvancementsPacket.Action action, @Nullable ResourceLocation resourceLocation) {
		this.action = action;
		this.tab = resourceLocation;
	}

	public static ServerboundSeenAdvancementsPacket openedTab(AdvancementHolder advancementHolder) {
		return new ServerboundSeenAdvancementsPacket(ServerboundSeenAdvancementsPacket.Action.OPENED_TAB, advancementHolder.id());
	}

	public static ServerboundSeenAdvancementsPacket closedScreen() {
		return new ServerboundSeenAdvancementsPacket(ServerboundSeenAdvancementsPacket.Action.CLOSED_SCREEN, null);
	}

	private ServerboundSeenAdvancementsPacket(FriendlyByteBuf friendlyByteBuf) {
		this.action = friendlyByteBuf.readEnum(ServerboundSeenAdvancementsPacket.Action.class);
		if (this.action == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
			this.tab = friendlyByteBuf.readResourceLocation();
		} else {
			this.tab = null;
		}
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeEnum(this.action);
		if (this.action == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
			friendlyByteBuf.writeResourceLocation(this.tab);
		}
	}

	@Override
	public PacketType<ServerboundSeenAdvancementsPacket> type() {
		return GamePacketTypes.SERVERBOUND_SEEN_ADVANCEMENTS;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleSeenAdvancements(this);
	}

	public ServerboundSeenAdvancementsPacket.Action getAction() {
		return this.action;
	}

	@Nullable
	public ResourceLocation getTab() {
		return this.tab;
	}

	public static enum Action {
		OPENED_TAB,
		CLOSED_SCREEN;
	}
}
