package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ServerboundSeenAdvancementsPacket implements Packet<ServerGamePacketListener> {
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

	public ServerboundSeenAdvancementsPacket(FriendlyByteBuf friendlyByteBuf) {
		this.action = friendlyByteBuf.readEnum(ServerboundSeenAdvancementsPacket.Action.class);
		if (this.action == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
			this.tab = friendlyByteBuf.readResourceLocation();
		} else {
			this.tab = null;
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeEnum(this.action);
		if (this.action == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
			friendlyByteBuf.writeResourceLocation(this.tab);
		}
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
