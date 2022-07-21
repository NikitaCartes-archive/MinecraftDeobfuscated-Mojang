package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PreviewableCommand;

public record ArgumentSignatures(List<ArgumentSignatures.Entry> entries) {
	public static final ArgumentSignatures EMPTY = new ArgumentSignatures(List.of());
	private static final int MAX_ARGUMENT_COUNT = 8;
	private static final int MAX_ARGUMENT_NAME_LENGTH = 16;

	public ArgumentSignatures(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 8), ArgumentSignatures.Entry::new));
	}

	public MessageSignature get(String string) {
		for (ArgumentSignatures.Entry entry : this.entries) {
			if (entry.name.equals(string)) {
				return entry.signature;
			}
		}

		return MessageSignature.EMPTY;
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeCollection(this.entries, (friendlyByteBufx, entry) -> entry.write(friendlyByteBufx));
	}

	public static boolean hasSignableArguments(PreviewableCommand<?> previewableCommand) {
		return previewableCommand.arguments().stream().anyMatch(argument -> argument.previewType() instanceof SignedArgument);
	}

	public static ArgumentSignatures signCommand(PreviewableCommand<?> previewableCommand, ArgumentSignatures.Signer signer) {
		List<ArgumentSignatures.Entry> list = collectPlainSignableArguments(previewableCommand).stream().map(pair -> {
			MessageSignature messageSignature = signer.sign((String)pair.getFirst(), (String)pair.getSecond());
			return new ArgumentSignatures.Entry((String)pair.getFirst(), messageSignature);
		}).toList();
		return new ArgumentSignatures(list);
	}

	public static List<Pair<String, String>> collectPlainSignableArguments(PreviewableCommand<?> previewableCommand) {
		List<Pair<String, String>> list = new ArrayList();

		for (PreviewableCommand.Argument<?> argument : previewableCommand.arguments()) {
			if (argument.previewType() instanceof SignedArgument<?> signedArgument) {
				String string = getSignableText(signedArgument, argument.parsedValue());
				list.add(Pair.of(argument.name(), string));
			}
		}

		return list;
	}

	private static <T> String getSignableText(SignedArgument<T> signedArgument, ParsedArgument<?, ?> parsedArgument) {
		return signedArgument.getSignableText((T)parsedArgument.getResult());
	}

	public static record Entry(String name, MessageSignature signature) {

		public Entry(FriendlyByteBuf friendlyByteBuf) {
			this(friendlyByteBuf.readUtf(16), new MessageSignature(friendlyByteBuf));
		}

		public void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeUtf(this.name, 16);
			this.signature.write(friendlyByteBuf);
		}
	}

	@FunctionalInterface
	public interface Signer {
		MessageSignature sign(String string, String string2);
	}
}
