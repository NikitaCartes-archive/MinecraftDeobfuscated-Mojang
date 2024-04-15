package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;

public class MessageArgument implements SignedArgument<MessageArgument.Message> {
	private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");
	static final Dynamic2CommandExceptionType TOO_LONG = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatableEscape("argument.message.too_long", object, object2)
	);

	public static MessageArgument message() {
		return new MessageArgument();
	}

	public static Component getMessage(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		MessageArgument.Message message = commandContext.getArgument(string, MessageArgument.Message.class);
		return message.resolveComponent(commandContext.getSource());
	}

	public static void resolveChatMessage(CommandContext<CommandSourceStack> commandContext, String string, Consumer<PlayerChatMessage> consumer) throws CommandSyntaxException {
		MessageArgument.Message message = commandContext.getArgument(string, MessageArgument.Message.class);
		CommandSourceStack commandSourceStack = commandContext.getSource();
		Component component = message.resolveComponent(commandSourceStack);
		CommandSigningContext commandSigningContext = commandSourceStack.getSigningContext();
		PlayerChatMessage playerChatMessage = commandSigningContext.getArgument(string);
		if (playerChatMessage != null) {
			resolveSignedMessage(consumer, commandSourceStack, playerChatMessage.withUnsignedContent(component));
		} else {
			resolveDisguisedMessage(consumer, commandSourceStack, PlayerChatMessage.system(message.text).withUnsignedContent(component));
		}
	}

	private static void resolveSignedMessage(Consumer<PlayerChatMessage> consumer, CommandSourceStack commandSourceStack, PlayerChatMessage playerChatMessage) {
		MinecraftServer minecraftServer = commandSourceStack.getServer();
		CompletableFuture<FilteredText> completableFuture = filterPlainText(commandSourceStack, playerChatMessage);
		Component component = minecraftServer.getChatDecorator().decorate(commandSourceStack.getPlayer(), playerChatMessage.decoratedContent());
		commandSourceStack.getChatMessageChainer().append(completableFuture, filteredText -> {
			PlayerChatMessage playerChatMessage2 = playerChatMessage.withUnsignedContent(component).filter(filteredText.mask());
			consumer.accept(playerChatMessage2);
		});
	}

	private static void resolveDisguisedMessage(Consumer<PlayerChatMessage> consumer, CommandSourceStack commandSourceStack, PlayerChatMessage playerChatMessage) {
		ChatDecorator chatDecorator = commandSourceStack.getServer().getChatDecorator();
		Component component = chatDecorator.decorate(commandSourceStack.getPlayer(), playerChatMessage.decoratedContent());
		consumer.accept(playerChatMessage.withUnsignedContent(component));
	}

	private static CompletableFuture<FilteredText> filterPlainText(CommandSourceStack commandSourceStack, PlayerChatMessage playerChatMessage) {
		ServerPlayer serverPlayer = commandSourceStack.getPlayer();
		return serverPlayer != null && playerChatMessage.hasSignatureFrom(serverPlayer.getUUID())
			? serverPlayer.getTextFilter().processStreamMessage(playerChatMessage.signedContent())
			: CompletableFuture.completedFuture(FilteredText.passThrough(playerChatMessage.signedContent()));
	}

	public MessageArgument.Message parse(StringReader stringReader) throws CommandSyntaxException {
		return MessageArgument.Message.parseText(stringReader, true);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public static record Message(String text, MessageArgument.Part[] parts) {

		Component resolveComponent(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
			return this.toComponent(commandSourceStack, commandSourceStack.hasPermission(2));
		}

		public Component toComponent(CommandSourceStack commandSourceStack, boolean bl) throws CommandSyntaxException {
			if (this.parts.length != 0 && bl) {
				MutableComponent mutableComponent = Component.literal(this.text.substring(0, this.parts[0].start()));
				int i = this.parts[0].start();

				for (MessageArgument.Part part : this.parts) {
					Component component = part.toComponent(commandSourceStack);
					if (i < part.start()) {
						mutableComponent.append(this.text.substring(i, part.start()));
					}

					mutableComponent.append(component);
					i = part.end();
				}

				if (i < this.text.length()) {
					mutableComponent.append(this.text.substring(i));
				}

				return mutableComponent;
			} else {
				return Component.literal(this.text);
			}
		}

		public static MessageArgument.Message parseText(StringReader stringReader, boolean bl) throws CommandSyntaxException {
			if (stringReader.getRemainingLength() > 256) {
				throw MessageArgument.TOO_LONG.create(stringReader.getRemainingLength(), 256);
			} else {
				String string = stringReader.getRemaining();
				if (!bl) {
					stringReader.setCursor(stringReader.getTotalLength());
					return new MessageArgument.Message(string, new MessageArgument.Part[0]);
				} else {
					List<MessageArgument.Part> list = Lists.<MessageArgument.Part>newArrayList();
					int i = stringReader.getCursor();

					while (true) {
						int j;
						EntitySelector entitySelector;
						while (true) {
							if (!stringReader.canRead()) {
								return new MessageArgument.Message(string, (MessageArgument.Part[])list.toArray(new MessageArgument.Part[0]));
							}

							if (stringReader.peek() == '@') {
								j = stringReader.getCursor();

								try {
									EntitySelectorParser entitySelectorParser = new EntitySelectorParser(stringReader);
									entitySelector = entitySelectorParser.parse();
									break;
								} catch (CommandSyntaxException var8) {
									if (var8.getType() != EntitySelectorParser.ERROR_MISSING_SELECTOR_TYPE && var8.getType() != EntitySelectorParser.ERROR_UNKNOWN_SELECTOR_TYPE) {
										throw var8;
									}

									stringReader.setCursor(j + 1);
								}
							} else {
								stringReader.skip();
							}
						}

						list.add(new MessageArgument.Part(j - i, stringReader.getCursor() - i, entitySelector));
					}
				}
			}
		}
	}

	public static record Part(int start, int end, EntitySelector selector) {
		public Component toComponent(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
			return EntitySelector.joinNames(this.selector.findEntities(commandSourceStack));
		}
	}
}
