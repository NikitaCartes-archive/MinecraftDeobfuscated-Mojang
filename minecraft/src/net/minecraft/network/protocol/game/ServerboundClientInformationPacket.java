package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;

public class ServerboundClientInformationPacket implements Packet<ServerGamePacketListener> {
	public static final int MAX_LANGUAGE_LENGTH = 16;
	private final String language;
	private final int viewDistance;
	private final ChatVisiblity chatVisibility;
	private final boolean chatColors;
	private final int modelCustomisation;
	private final HumanoidArm mainHand;
	private final boolean textFilteringEnabled;

	public ServerboundClientInformationPacket(String string, int i, ChatVisiblity chatVisiblity, boolean bl, int j, HumanoidArm humanoidArm, boolean bl2) {
		this.language = string;
		this.viewDistance = i;
		this.chatVisibility = chatVisiblity;
		this.chatColors = bl;
		this.modelCustomisation = j;
		this.mainHand = humanoidArm;
		this.textFilteringEnabled = bl2;
	}

	public ServerboundClientInformationPacket(FriendlyByteBuf friendlyByteBuf) {
		this.language = friendlyByteBuf.readUtf(16);
		this.viewDistance = friendlyByteBuf.readByte();
		this.chatVisibility = friendlyByteBuf.readEnum(ChatVisiblity.class);
		this.chatColors = friendlyByteBuf.readBoolean();
		this.modelCustomisation = friendlyByteBuf.readUnsignedByte();
		this.mainHand = friendlyByteBuf.readEnum(HumanoidArm.class);
		this.textFilteringEnabled = friendlyByteBuf.readBoolean();
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
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleClientInformation(this);
	}

	public String getLanguage() {
		return this.language;
	}

	public int getViewDistance() {
		return this.viewDistance;
	}

	public ChatVisiblity getChatVisibility() {
		return this.chatVisibility;
	}

	public boolean getChatColors() {
		return this.chatColors;
	}

	public int getModelCustomisation() {
		return this.modelCustomisation;
	}

	public HumanoidArm getMainHand() {
		return this.mainHand;
	}

	public boolean isTextFilteringEnabled() {
		return this.textFilteringEnabled;
	}
}
