package net.minecraft.network.chat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.security.SignatureException;
import java.time.Instant;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.util.Crypt;

public record MessageSignature(UUID sender, Instant timeStamp, Crypt.SaltSignaturePair saltSignature) {
	public static MessageSignature unsigned() {
		return new MessageSignature(Util.NIL_UUID, Instant.now(), Crypt.SaltSignaturePair.EMPTY);
	}

	public boolean verify(Signature signature, Component component) throws SignatureException {
		updateSignature(signature, component, this.sender, this.timeStamp, this.saltSignature.salt());
		return signature.verify(this.saltSignature.signature());
	}

	public boolean verify(Signature signature, String string) throws SignatureException {
		return this.verify(signature, Component.literal(string));
	}

	public static void updateSignature(Signature signature, Component component, UUID uUID, Instant instant, long l) throws SignatureException {
		byte[] bs = encodeContent(component);
		int i = 32 + bs.length;
		ByteBuffer byteBuffer = ByteBuffer.allocate(i).order(ByteOrder.BIG_ENDIAN);
		byteBuffer.putLong(l);
		byteBuffer.putLong(uUID.getMostSignificantBits()).putLong(uUID.getLeastSignificantBits());
		byteBuffer.putLong(instant.getEpochSecond());
		byteBuffer.put(bs);
		signature.update(byteBuffer.flip());
	}

	private static byte[] encodeContent(Component component) {
		String string = Component.Serializer.toStableJson(component);
		return string.getBytes(StandardCharsets.UTF_8);
	}
}
