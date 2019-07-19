package net.minecraft.network.protocol.game;

import java.io.IOException;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ServerboundSeenAdvancementsPacket implements Packet<ServerGamePacketListener> {
	private ServerboundSeenAdvancementsPacket.Action action;
	private ResourceLocation tab;

	public ServerboundSeenAdvancementsPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundSeenAdvancementsPacket(ServerboundSeenAdvancementsPacket.Action action, @Nullable ResourceLocation resourceLocation) {
		this.action = action;
		this.tab = resourceLocation;
	}

	@Environment(EnvType.CLIENT)
	public static ServerboundSeenAdvancementsPacket openedTab(Advancement advancement) {
		return new ServerboundSeenAdvancementsPacket(ServerboundSeenAdvancementsPacket.Action.OPENED_TAB, advancement.getId());
	}

	@Environment(EnvType.CLIENT)
	public static ServerboundSeenAdvancementsPacket closedScreen() {
		return new ServerboundSeenAdvancementsPacket(ServerboundSeenAdvancementsPacket.Action.CLOSED_SCREEN, null);
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.action = friendlyByteBuf.readEnum(ServerboundSeenAdvancementsPacket.Action.class);
		if (this.action == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
			this.tab = friendlyByteBuf.readResourceLocation();
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
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

	public ResourceLocation getTab() {
		return this.tab;
	}

	public static enum Action {
		OPENED_TAB,
		CLOSED_SCREEN;
	}
}
