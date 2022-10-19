package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureUpdater;

public record LastSeenMessages(List<MessageSignature> entries) {
	public static LastSeenMessages EMPTY = new LastSeenMessages(List.of());
	public static final int LAST_SEEN_MESSAGES_MAX_LENGTH = 20;

	public void updateSignature(SignatureUpdater.Output output) throws SignatureException {
		output.update(Ints.toByteArray(this.entries.size()));

		for (MessageSignature messageSignature : this.entries) {
			output.update(messageSignature.bytes());
		}
	}

	public LastSeenMessages.Packed pack(MessageSignature.Packer packer) {
		return new LastSeenMessages.Packed(this.entries.stream().map(messageSignature -> messageSignature.pack(packer)).toList());
	}

	public static record Packed(List<MessageSignature.Packed> entries) {
		public static final LastSeenMessages.Packed EMPTY = new LastSeenMessages.Packed(List.of());

		public Packed(FriendlyByteBuf friendlyByteBuf) {
			this(friendlyByteBuf.readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 20), MessageSignature.Packed::read));
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeCollection(this.entries, MessageSignature.Packed::write);
		}

		public Optional<LastSeenMessages> unpack(MessageSignature.Unpacker unpacker) {
			List<MessageSignature> list = new ArrayList(this.entries.size());

			for (MessageSignature.Packed packed : this.entries) {
				Optional<MessageSignature> optional = packed.unpack(unpacker);
				if (optional.isEmpty()) {
					return Optional.empty();
				}

				list.add((MessageSignature)optional.get());
			}

			return Optional.of(new LastSeenMessages(list));
		}
	}

	public static record Update(int offset, BitSet acknowledged) {
		public Update(FriendlyByteBuf friendlyByteBuf) {
			this(friendlyByteBuf.readVarInt(), friendlyByteBuf.readFixedBitSet(20));
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeVarInt(this.offset);
			friendlyByteBuf.writeFixedBitSet(this.acknowledged, 20);
		}
	}
}
