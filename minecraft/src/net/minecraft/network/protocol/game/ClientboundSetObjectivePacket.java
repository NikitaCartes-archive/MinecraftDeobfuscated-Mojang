package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ClientboundSetObjectivePacket implements Packet<ClientGamePacketListener> {
	private String objectiveName;
	private Component displayName;
	private ObjectiveCriteria.RenderType renderType;
	private int method;

	public ClientboundSetObjectivePacket() {
	}

	public ClientboundSetObjectivePacket(Objective objective, int i) {
		this.objectiveName = objective.getName();
		this.displayName = objective.getDisplayName();
		this.renderType = objective.getRenderType();
		this.method = i;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.objectiveName = friendlyByteBuf.readUtf(16);
		this.method = friendlyByteBuf.readByte();
		if (this.method == 0 || this.method == 2) {
			this.displayName = friendlyByteBuf.readComponent();
			this.renderType = friendlyByteBuf.readEnum(ObjectiveCriteria.RenderType.class);
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeUtf(this.objectiveName);
		friendlyByteBuf.writeByte(this.method);
		if (this.method == 0 || this.method == 2) {
			friendlyByteBuf.writeComponent(this.displayName);
			friendlyByteBuf.writeEnum(this.renderType);
		}
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAddObjective(this);
	}

	@Environment(EnvType.CLIENT)
	public String getObjectiveName() {
		return this.objectiveName;
	}

	@Environment(EnvType.CLIENT)
	public Component getDisplayName() {
		return this.displayName;
	}

	@Environment(EnvType.CLIENT)
	public int getMethod() {
		return this.method;
	}

	@Environment(EnvType.CLIENT)
	public ObjectiveCriteria.RenderType getRenderType() {
		return this.renderType;
	}
}
