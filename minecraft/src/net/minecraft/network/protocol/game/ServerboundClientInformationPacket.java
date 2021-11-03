package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;

public record ServerboundClientInformationPacket() implements Packet<ServerGamePacketListener> {
	private final String language;
	private final int viewDistance;
	private final ChatVisiblity chatVisibility;
	private final boolean chatColors;
	private final int modelCustomisation;
	private final HumanoidArm mainHand;
	private final boolean textFilteringEnabled;
	private final boolean allowsListing;
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

	public ServerboundClientInformationPacket(
		String string, int i, ChatVisiblity chatVisiblity, boolean bl, int j, HumanoidArm humanoidArm, boolean bl2, boolean bl3
	) {
		this.language = string;
		this.viewDistance = i;
		this.chatVisibility = chatVisiblity;
		this.chatColors = bl;
		this.modelCustomisation = j;
		this.mainHand = humanoidArm;
		this.textFilteringEnabled = bl2;
		this.allowsListing = bl3;
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
