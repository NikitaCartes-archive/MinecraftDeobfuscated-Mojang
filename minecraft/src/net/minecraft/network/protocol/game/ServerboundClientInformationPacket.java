package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;

public record ServerboundClientInformationPacket(
	String language,
	int viewDistance,
	ChatVisiblity chatVisibility,
	boolean chatColors,
	int modelCustomisation,
	HumanoidArm mainHand,
	boolean textFilteringEnabled,
	boolean allowsListing
) implements Packet<ServerGamePacketListener> {
	public static final int MAX_LANGUAGE_LENGTH = 16;

	public ServerboundClientInformationPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readUtf(16),
			friendlyByteBuf.readByte(),
			friendlyByteBuf.readEnum(ChatVisiblity.class),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readUnsignedByte(),
			friendlyByteBuf.readEnum(HumanoidArm.class),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readBoolean()
		);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.language);
		friendlyByteBuf.writeByte(this.viewDistance);
		friendlyByteBuf.writeEnum(this.chatVisibility);
		friendlyByteBuf.writeBoolean(this.chatColors);
		friendlyByteBuf.writeByte(this.modelCustomisation);
		friendlyByteBuf.writeEnum(this.mainHand);
		friendlyByteBuf.writeBoolean(this.textFilteringEnabled);
		friendlyByteBuf.writeBoolean(this.allowsListing);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleClientInformation(this);
	}
}
