package net.minecraft.network.protocol.game;

import java.time.Instant;
import java.util.UUID;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.StringUtil;

public record ServerboundChatCommandPacket(String command, Instant timeStamp, ArgumentSignatures argumentSignatures, boolean signedPreview)
	implements Packet<ServerGamePacketListener> {
	public ServerboundChatCommandPacket(String command, Instant timeStamp, ArgumentSignatures argumentSignatures, boolean signedPreview) {
		command = StringUtil.trimChatMessage(command);
		this.command = command;
		this.timeStamp = timeStamp;
		this.argumentSignatures = argumentSignatures;
		this.signedPreview = signedPreview;
	}

	public ServerboundChatCommandPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readUtf(256), friendlyByteBuf.readInstant(), new ArgumentSignatures(friendlyByteBuf), friendlyByteBuf.readBoolean());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.command, 256);
		friendlyByteBuf.writeInstant(this.timeStamp);
		this.argumentSignatures.write(friendlyByteBuf);
		friendlyByteBuf.writeBoolean(this.signedPreview);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleChatCommand(this);
	}

	public CommandSigningContext signingContext(UUID uUID) {
		return new CommandSigningContext.SignedArguments(uUID, this.timeStamp, this.argumentSignatures, this.signedPreview);
	}
}
