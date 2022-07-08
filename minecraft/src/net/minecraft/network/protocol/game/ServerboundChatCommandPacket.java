package net.minecraft.network.protocol.game;

import java.time.Instant;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;

public record ServerboundChatCommandPacket(String command, Instant timeStamp, long salt, ArgumentSignatures argumentSignatures, boolean signedPreview)
	implements Packet<ServerGamePacketListener> {
	public ServerboundChatCommandPacket(String command, Instant timeStamp, long salt, ArgumentSignatures argumentSignatures, boolean signedPreview) {
		command = StringUtil.trimChatMessage(command);
		this.command = command;
		this.timeStamp = timeStamp;
		this.salt = salt;
		this.argumentSignatures = argumentSignatures;
		this.signedPreview = signedPreview;
	}

	public ServerboundChatCommandPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readUtf(256),
			friendlyByteBuf.readInstant(),
			friendlyByteBuf.readLong(),
			new ArgumentSignatures(friendlyByteBuf),
			friendlyByteBuf.readBoolean()
		);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.command, 256);
		friendlyByteBuf.writeInstant(this.timeStamp);
		friendlyByteBuf.writeLong(this.salt);
		this.argumentSignatures.write(friendlyByteBuf);
		friendlyByteBuf.writeBoolean(this.signedPreview);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleChatCommand(this);
	}

	public CommandSigningContext signingContext(ServerPlayer serverPlayer) {
		MessageSigner messageSigner = new MessageSigner(serverPlayer.getUUID(), this.timeStamp, this.salt);
		return new CommandSigningContext.SignedArguments(serverPlayer.connection.signedMessageDecoder(), messageSigner, this.argumentSignatures, this.signedPreview);
	}
}
