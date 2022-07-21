package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.bytes.ByteArrays;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureValidator;

public record MessageSignature(byte[] bytes) {
	public static final MessageSignature EMPTY = new MessageSignature(ByteArrays.EMPTY_ARRAY);

	public MessageSignature(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readByteArray());
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByteArray(this.bytes);
	}

	public boolean verify(SignatureValidator signatureValidator, SignedMessageHeader signedMessageHeader, SignedMessageBody signedMessageBody) {
		if (!this.isEmpty()) {
			byte[] bs = signedMessageBody.hash().asBytes();
			return signatureValidator.validate(output -> signedMessageHeader.updateSignature(output, bs), this.bytes);
		} else {
			return false;
		}
	}

	public boolean verify(SignatureValidator signatureValidator, SignedMessageHeader signedMessageHeader, byte[] bs) {
		return !this.isEmpty() ? signatureValidator.validate(output -> signedMessageHeader.updateSignature(output, bs), this.bytes) : false;
	}

	public boolean isEmpty() {
		return this.bytes.length == 0;
	}

	@Nullable
	public ByteBuffer asByteBuffer() {
		return !this.isEmpty() ? ByteBuffer.wrap(this.bytes) : null;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			if (object instanceof MessageSignature messageSignature && Arrays.equals(this.bytes, messageSignature.bytes)) {
				return true;
			}

			return false;
		}
	}

	public int hashCode() {
		return Arrays.hashCode(this.bytes);
	}

	public String toString() {
		return !this.isEmpty() ? Base64.getEncoder().encodeToString(this.bytes) : "empty";
	}
}
