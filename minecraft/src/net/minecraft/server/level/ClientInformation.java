package net.minecraft.server.level;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Player;

public record ClientInformation(
	String language,
	int viewDistance,
	ChatVisiblity chatVisibility,
	boolean chatColors,
	int modelCustomisation,
	HumanoidArm mainHand,
	boolean textFilteringEnabled,
	boolean allowsListing,
	ParticleStatus particleStatus
) {
	public static final int MAX_LANGUAGE_LENGTH = 16;

	public ClientInformation(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readUtf(16),
			friendlyByteBuf.readByte(),
			friendlyByteBuf.readEnum(ChatVisiblity.class),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readUnsignedByte(),
			friendlyByteBuf.readEnum(HumanoidArm.class),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readEnum(ParticleStatus.class)
		);
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.language);
		friendlyByteBuf.writeByte(this.viewDistance);
		friendlyByteBuf.writeEnum(this.chatVisibility);
		friendlyByteBuf.writeBoolean(this.chatColors);
		friendlyByteBuf.writeByte(this.modelCustomisation);
		friendlyByteBuf.writeEnum(this.mainHand);
		friendlyByteBuf.writeBoolean(this.textFilteringEnabled);
		friendlyByteBuf.writeBoolean(this.allowsListing);
		friendlyByteBuf.writeEnum(this.particleStatus);
	}

	public static ClientInformation createDefault() {
		return new ClientInformation("en_us", 2, ChatVisiblity.FULL, true, 0, Player.DEFAULT_MAIN_HAND, false, false, ParticleStatus.ALL);
	}
}
