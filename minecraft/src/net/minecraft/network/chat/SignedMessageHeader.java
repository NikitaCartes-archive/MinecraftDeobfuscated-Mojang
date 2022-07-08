package net.minecraft.network.chat;

import java.security.SignatureException;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureUpdater;

public record SignedMessageHeader(@Nullable MessageSignature previousSignature, UUID sender) {
	public SignedMessageHeader(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readNullable(MessageSignature::new), friendlyByteBuf.readUUID());
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeNullable(this.previousSignature, (friendlyByteBufx, messageSignature) -> messageSignature.write(friendlyByteBufx));
		friendlyByteBuf.writeUUID(this.sender);
	}

	public void updateSignature(SignatureUpdater.Output output, byte[] bs) throws SignatureException {
		if (this.previousSignature != null) {
			output.update(this.previousSignature.bytes());
		}

		output.update(UUIDUtil.uuidToByteArray(this.sender));
		output.update(bs);
	}
}
