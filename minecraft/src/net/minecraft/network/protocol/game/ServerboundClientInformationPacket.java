package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;

public class ServerboundClientInformationPacket implements Packet<ServerGamePacketListener> {
	private String language;
	private int viewDistance;
	private ChatVisiblity chatVisibility;
	private boolean chatColors;
	private int modelCustomisation;
	private HumanoidArm mainHand;

	public ServerboundClientInformationPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundClientInformationPacket(String string, int i, ChatVisiblity chatVisiblity, boolean bl, int j, HumanoidArm humanoidArm) {
		this.language = string;
		this.viewDistance = i;
		this.chatVisibility = chatVisiblity;
		this.chatColors = bl;
		this.modelCustomisation = j;
		this.mainHand = humanoidArm;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.language = friendlyByteBuf.readUtf(16);
		this.viewDistance = friendlyByteBuf.readByte();
		this.chatVisibility = friendlyByteBuf.readEnum(ChatVisiblity.class);
		this.chatColors = friendlyByteBuf.readBoolean();
		this.modelCustomisation = friendlyByteBuf.readUnsignedByte();
		this.mainHand = friendlyByteBuf.readEnum(HumanoidArm.class);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeUtf(this.language);
		friendlyByteBuf.writeByte(this.viewDistance);
		friendlyByteBuf.writeEnum(this.chatVisibility);
		friendlyByteBuf.writeBoolean(this.chatColors);
		friendlyByteBuf.writeByte(this.modelCustomisation);
		friendlyByteBuf.writeEnum(this.mainHand);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleClientInformation(this);
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
}
