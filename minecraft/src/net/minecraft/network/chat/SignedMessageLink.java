package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.SignatureException;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;

public record SignedMessageLink(int index, UUID sender, UUID sessionId) {
	public static final Codec<SignedMessageLink> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.NON_NEGATIVE_INT.fieldOf("index").forGetter(SignedMessageLink::index),
					UUIDUtil.CODEC.fieldOf("sender").forGetter(SignedMessageLink::sender),
					UUIDUtil.CODEC.fieldOf("session_id").forGetter(SignedMessageLink::sessionId)
				)
				.apply(instance, SignedMessageLink::new)
	);

	public static SignedMessageLink unsigned(UUID uUID) {
		return root(uUID, Util.NIL_UUID);
	}

	public static SignedMessageLink root(UUID uUID, UUID uUID2) {
		return new SignedMessageLink(0, uUID, uUID2);
	}

	public void updateSignature(SignatureUpdater.Output output) throws SignatureException {
		output.update(UUIDUtil.uuidToByteArray(this.sender));
		output.update(UUIDUtil.uuidToByteArray(this.sessionId));
		output.update(Ints.toByteArray(this.index));
	}

	public boolean isDescendantOf(SignedMessageLink signedMessageLink) {
		return this.index > signedMessageLink.index() && this.sender.equals(signedMessageLink.sender()) && this.sessionId.equals(signedMessageLink.sessionId());
	}

	@Nullable
	public SignedMessageLink advance() {
		return this.index == Integer.MAX_VALUE ? null : new SignedMessageLink(this.index + 1, this.sender, this.sessionId);
	}
}
