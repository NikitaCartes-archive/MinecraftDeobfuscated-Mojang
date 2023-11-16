package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ClientboundSetObjectivePacket implements Packet<ClientGamePacketListener> {
	public static final int METHOD_ADD = 0;
	public static final int METHOD_REMOVE = 1;
	public static final int METHOD_CHANGE = 2;
	private final String objectiveName;
	private final Component displayName;
	private final ObjectiveCriteria.RenderType renderType;
	@Nullable
	private final NumberFormat numberFormat;
	private final int method;

	public ClientboundSetObjectivePacket(Objective objective, int i) {
		this.objectiveName = objective.getName();
		this.displayName = objective.getDisplayName();
		this.renderType = objective.getRenderType();
		this.numberFormat = objective.numberFormat();
		this.method = i;
	}

	public ClientboundSetObjectivePacket(FriendlyByteBuf friendlyByteBuf) {
		this.objectiveName = friendlyByteBuf.readUtf();
		this.method = friendlyByteBuf.readByte();
		if (this.method != 0 && this.method != 2) {
			this.displayName = CommonComponents.EMPTY;
			this.renderType = ObjectiveCriteria.RenderType.INTEGER;
			this.numberFormat = null;
		} else {
			this.displayName = friendlyByteBuf.readComponentTrusted();
			this.renderType = friendlyByteBuf.readEnum(ObjectiveCriteria.RenderType.class);
			this.numberFormat = friendlyByteBuf.readNullable(NumberFormatTypes::readFromStream);
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.objectiveName);
		friendlyByteBuf.writeByte(this.method);
		if (this.method == 0 || this.method == 2) {
			friendlyByteBuf.writeComponent(this.displayName);
			friendlyByteBuf.writeEnum(this.renderType);
			friendlyByteBuf.writeNullable(this.numberFormat, NumberFormatTypes::writeToStream);
		}
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleAddObjective(this);
	}

	public String getObjectiveName() {
		return this.objectiveName;
	}

	public Component getDisplayName() {
		return this.displayName;
	}

	public int getMethod() {
		return this.method;
	}

	public ObjectiveCriteria.RenderType getRenderType() {
		return this.renderType;
	}

	@Nullable
	public NumberFormat getNumberFormat() {
		return this.numberFormat;
	}
}
