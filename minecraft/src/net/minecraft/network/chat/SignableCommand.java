package net.minecraft.network.chat;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.arguments.SignedArgument;

public record SignableCommand<S>(List<SignableCommand.Argument<S>> arguments) {
	public static <S> SignableCommand<S> of(ParseResults<S> parseResults) {
		String string = parseResults.getReader().getString();
		CommandContextBuilder<S> commandContextBuilder = parseResults.getContext();
		CommandContextBuilder<S> commandContextBuilder2 = commandContextBuilder;
		List<SignableCommand.Argument<S>> list = collectArguments(string, commandContextBuilder);

		CommandContextBuilder<S> commandContextBuilder3;
		while ((commandContextBuilder3 = commandContextBuilder2.getChild()) != null) {
			boolean bl = commandContextBuilder3.getRootNode() != commandContextBuilder.getRootNode();
			if (!bl) {
				break;
			}

			list.addAll(collectArguments(string, commandContextBuilder3));
			commandContextBuilder2 = commandContextBuilder3;
		}

		return new SignableCommand<>(list);
	}

	private static <S> List<SignableCommand.Argument<S>> collectArguments(String string, CommandContextBuilder<S> commandContextBuilder) {
		List<SignableCommand.Argument<S>> list = new ArrayList();

		for (ParsedCommandNode<S> parsedCommandNode : commandContextBuilder.getNodes()) {
			CommandNode parsedArgument = parsedCommandNode.getNode();
			if (parsedArgument instanceof ArgumentCommandNode) {
				ArgumentCommandNode<S, ?> argumentCommandNode = (ArgumentCommandNode<S, ?>)parsedArgument;
				if (argumentCommandNode.getType() instanceof SignedArgument) {
					ParsedArgument<S, ?> parsedArgumentx = (ParsedArgument<S, ?>)commandContextBuilder.getArguments().get(argumentCommandNode.getName());
					if (parsedArgumentx != null) {
						String string2 = parsedArgumentx.getRange().get(string);
						list.add(new SignableCommand.Argument<>(argumentCommandNode, string2));
					}
				}
			}
		}

		return list;
	}

	public static record Argument<S>(ArgumentCommandNode<S, ?> node, String value) {
		public String name() {
			return this.node.getName();
		}
	}
}
