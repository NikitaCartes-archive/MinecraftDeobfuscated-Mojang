package net.minecraft.network.protocol.game;

import java.io.IOException;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.inventory.MenuType;

public class ClientboundOpenScreenPacket implements Packet<ClientGamePacketListener> {
	private int containerId;
	private int type;
	private Component title;

	public ClientboundOpenScreenPacket() {
	}

	public ClientboundOpenScreenPacket(int i, MenuType<?> menuType, Component component) {
		this.containerId = i;
		this.type = Registry.MENU.getId(menuType);
		this.title = component;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.containerId = friendlyByteBuf.readVarInt();
		this.type = friendlyByteBuf.readVarInt();
		this.title = friendlyByteBuf.readComponent();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.containerId);
		friendlyByteBuf.writeVarInt(this.type);
		friendlyByteBuf.writeComponent(this.title);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleOpenScreen(this);
	}

	@Environment(EnvType.CLIENT)
	public int getContainerId() {
		return this.containerId;
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public MenuType<?> getType() {
		return Registry.MENU.byId(this.type);
	}

	@Environment(EnvType.CLIENT)
	public Component getTitle() {
		return this.title;
	}
}
