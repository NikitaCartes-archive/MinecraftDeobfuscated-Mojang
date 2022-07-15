package net.minecraft.commands.arguments;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;

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

	public static boolean hasSignableArguments(ParseResults<?> parseResults) {
		CommandContextBuilder<?> commandContextBuilder = parseResults.getContext().getLastChild();

		for (ParsedCommandNode<?> parsedCommandNode : commandContextBuilder.getNodes()) {
			CommandNode parsedArgument = parsedCommandNode.getNode();
			if (parsedArgument instanceof ArgumentCommandNode) {
				ArgumentCommandNode<?, ?> argumentCommandNode = (ArgumentCommandNode<?, ?>)parsedArgument;
				if (argumentCommandNode.getType() instanceof SignedArgument) {
					ParsedArgument<?, ?> parsedArgumentx = (ParsedArgument<?, ?>)commandContextBuilder.getArguments().get(argumentCommandNode.getName());
					if (parsedArgumentx != null) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public static ArgumentSignatures signCommand(CommandContextBuilder<?> commandContextBuilder, ArgumentSignatures.Signer signer) {
		List<ArgumentSignatures.Entry> list = collectLastChildPlainSignableArguments(commandContextBuilder).stream().map(pair -> {
			MessageSignature messageSignature = signer.sign((String)pair.getFirst(), (String)pair.getSecond());
			return new ArgumentSignatures.Entry((String)pair.getFirst(), messageSignature);
		}).toList();
		return new ArgumentSignatures(list);
	}

	private static List<Pair<String, String>> collectLastChildPlainSignableArguments(CommandContextBuilder<?> commandContextBuilder) {
		CommandContextBuilder<?> commandContextBuilder2 = commandContextBuilder.getLastChild();
		List<Pair<String, String>> list = new ArrayList();

		for (ParsedCommandNode<?> parsedCommandNode : commandContextBuilder2.getNodes()) {
			CommandNode parsedArgument = parsedCommandNode.getNode();
			if (parsedArgument instanceof ArgumentCommandNode) {
				ArgumentCommandNode<?, ?> argumentCommandNode = (ArgumentCommandNode<?, ?>)parsedArgument;
				ArgumentType var9 = argumentCommandNode.getType();
				if (var9 instanceof SignedArgument) {
					SignedArgument<?> signedArgument = (SignedArgument<?>)var9;
					ParsedArgument<?, ?> parsedArgumentx = (ParsedArgument<?, ?>)commandContextBuilder2.getArguments().get(argumentCommandNode.getName());
					if (parsedArgumentx != null) {
						String string = getSignableText(signedArgument, parsedArgumentx);
						list.add(Pair.of(argumentCommandNode.getName(), string));
					}
				}
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
