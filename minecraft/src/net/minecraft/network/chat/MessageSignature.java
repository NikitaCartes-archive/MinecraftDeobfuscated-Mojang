package net.minecraft.network.chat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.util.Crypt;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.SignatureValidator;

public record MessageSignature(UUID sender, Instant timeStamp, Crypt.SaltSignaturePair saltSignature) {
	public static MessageSignature unsigned() {
		return new MessageSignature(Util.NIL_UUID, Instant.now(), Crypt.SaltSignaturePair.EMPTY);
	}

	public boolean verify(SignatureValidator signatureValidator, Component component) {
		return this.isValid()
			? signatureValidator.validate(
				output -> updateSignature(output, component, this.sender, this.timeStamp, this.saltSignature.salt()), this.saltSignature.signature()
			)
			: false;
	}

	public boolean verify(SignatureValidator signatureValidator, String string) throws SignatureException {
		return this.verify(signatureValidator, Component.literal(string));
	}

	public static void updateSignature(SignatureUpdater.Output output, Component component, UUID uUID, Instant instant, long l) throws SignatureException {
		byte[] bs = new byte[32];
		ByteBuffer byteBuffer = ByteBuffer.wrap(bs).order(ByteOrder.BIG_ENDIAN);
		byteBuffer.putLong(l);
		byteBuffer.putLong(uUID.getMostSignificantBits()).putLong(uUID.getLeastSignificantBits());
		byteBuffer.putLong(instant.getEpochSecond());
		output.update(bs);
		output.update(encodeContent(component));
	}

	private static byte[] encodeContent(Component component) {
		String string = Component.Serializer.toStableJson(component);
		return string.getBytes(StandardCharsets.UTF_8);
	}

	public boolean isValid() {
		return this.sender != Util.NIL_UUID && this.saltSignature.isValid();
	}

	public boolean isValid(UUID uUID) {
		return this.isValid() && uUID.equals(this.sender);
	}
}
