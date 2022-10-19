package net.minecraft.commands.arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignableCommand;

public record ArgumentSignatures(List<ArgumentSignatures.Entry> entries) {
	public static final ArgumentSignatures EMPTY = new ArgumentSignatures(List.of());
	private static final int MAX_ARGUMENT_COUNT = 8;
	private static final int MAX_ARGUMENT_NAME_LENGTH = 16;

	public ArgumentSignatures(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 8), ArgumentSignatures.Entry::new));
	}

	@Nullable
	public MessageSignature get(String string) {
		for (ArgumentSignatures.Entry entry : this.entries) {
			if (entry.name.equals(string)) {
				return entry.signature;
			}
		}

		return null;
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeCollection(this.entries, (friendlyByteBufx, entry) -> entry.write(friendlyByteBufx));
	}

	public static ArgumentSignatures signCommand(SignableCommand<?> signableCommand, ArgumentSignatures.Signer signer) {
		List<ArgumentSignatures.Entry> list = signableCommand.arguments().stream().map(argument -> {
			MessageSignature messageSignature = signer.sign(argument.value());
			return messageSignature != null ? new ArgumentSignatures.Entry(argument.name(), messageSignature) : null;
		}).filter(Objects::nonNull).toList();
		return new ArgumentSignatures(list);
	}

	public static record Entry(String name, MessageSignature signature) {

		public Entry(FriendlyByteBuf friendlyByteBuf) {
			this(friendlyByteBuf.readUtf(16), MessageSignature.read(friendlyByteBuf));
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeUtf(this.name, 16);
			MessageSignature.write(friendlyByteBuf, this.signature);
		}
	}

	@FunctionalInterface
	public interface Signer {
		@Nullable
		MessageSignature sign(String string);
	}
}
