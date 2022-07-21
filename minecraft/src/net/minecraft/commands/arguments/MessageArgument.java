package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.ChatMessageContent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.players.PlayerList;
import org.slf4j.Logger;

public class MessageArgument implements SignedArgument<MessageArgument.Message> {
	private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");
	private static final Logger LOGGER = LogUtils.getLogger();

	public static MessageArgument message() {
		return new MessageArgument();
	}

	public static Component getMessage(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		MessageArgument.Message message = commandContext.getArgument(string, MessageArgument.Message.class);
		return message.resolveComponent(commandContext.getSource());
	}

	public static MessageArgument.ChatMessage getChatMessage(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		MessageArgument.Message message = commandContext.getArgument(string, MessageArgument.Message.class);
		Component component = message.resolveComponent(commandContext.getSource());
		CommandSigningContext commandSigningContext = commandContext.getSource().getSigningContext();
		PlayerChatMessage playerChatMessage = (PlayerChatMessage)Objects.requireNonNullElseGet(commandSigningContext.getArgument(string), () -> {
			ChatMessageContent chatMessageContent = new ChatMessageContent(message.text);
			return PlayerChatMessage.system(chatMessageContent);
		});
		return new MessageArgument.ChatMessage(component, playerChatMessage);
	}

	public MessageArgument.Message parse(StringReader stringReader) throws CommandSyntaxException {
		return MessageArgument.Message.parseText(stringReader, true);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public String getSignableText(MessageArgument.Message message) {
		return message.getText();
	}

	public CompletableFuture<Component> resolvePreview(CommandSourceStack commandSourceStack, MessageArgument.Message message) throws CommandSyntaxException {
		return message.resolveDecoratedComponent(commandSourceStack);
	}

	@Override
	public Class<MessageArgument.Message> getValueType() {
		return MessageArgument.Message.class;
	}

	static void logResolutionFailure(CommandSourceStack commandSourceStack, CompletableFuture<?> completableFuture) {
		completableFuture.exceptionally(throwable -> {
			LOGGER.error("Encountered unexpected exception while resolving chat message argument from '{}'", commandSourceStack.getDisplayName().getString(), throwable);
			return null;
		});
	}

	public static record ChatMessage(Component formatted, PlayerChatMessage signedArgument) {
		public void resolve(CommandSourceStack commandSourceStack, Consumer<FilteredText<PlayerChatMessage>> consumer) {
			MinecraftServer minecraftServer = commandSourceStack.getServer();
			String string = this.signedArgument.signedContent().plain();
			commandSourceStack.getChatMessageChainer()
				.append(
					() -> this.filterPlainText(commandSourceStack, string)
							.thenComposeAsync(
								filteredText -> {
									FilteredText<Component> filteredText2 = filteredText.rebuildIfNeeded(
										this.formatted, stringxx -> this.rebuildFilteredMessage(commandSourceStack, stringxx)
									);
									return this.resolveFiltered(commandSourceStack, filteredText, filteredText2);
								},
								minecraftServer
							)
							.thenAcceptAsync(consumer, minecraftServer)
				);
		}

		@Nullable
		private Component rebuildFilteredMessage(CommandSourceStack commandSourceStack, String string) {
			try {
				MessageArgument.Message message = MessageArgument.Message.parseText(new StringReader(string), true);
				return message.resolveComponent(commandSourceStack);
			} catch (CommandSyntaxException var4) {
				return null;
			}
		}

		private CompletableFuture<FilteredText<PlayerChatMessage>> resolveFiltered(
			CommandSourceStack commandSourceStack, FilteredText<String> filteredText, FilteredText<Component> filteredText2
		) {
			MinecraftServer minecraftServer = commandSourceStack.getServer();
			ChatDecorator chatDecorator = minecraftServer.getChatDecorator();
			ServerPlayer serverPlayer = commandSourceStack.getPlayer();
			ChatMessageContent chatMessageContent = this.signedArgument.signedContent();
			return chatMessageContent.isDecorated()
				? chatDecorator.rebuildFiltered(serverPlayer, filteredText2, chatMessageContent.decorated()).thenApply(filteredText2x -> {
					FilteredText<ChatMessageContent> filteredText3 = ChatMessageContent.fromFiltered(filteredText, filteredText2x);
					return this.signedArgument.withFilteredText(filteredText3);
				})
				: chatDecorator.decorate(serverPlayer, filteredText2.raw())
					.thenComposeAsync(component -> chatDecorator.rebuildFiltered(serverPlayer, filteredText2, component), minecraftServer)
					.thenApply(filteredText2x -> {
						FilteredText<ChatMessageContent> filteredText3 = ChatMessageContent.fromFiltered(filteredText);
						FilteredText<PlayerChatMessage> filteredText4 = this.signedArgument.withFilteredText(filteredText3);
						return ChatDecorator.attachUnsignedDecoration(filteredText4, filteredText2x);
					});
		}

		private CompletableFuture<FilteredText<String>> filterPlainText(CommandSourceStack commandSourceStack, String string) {
			ServerPlayer serverPlayer = commandSourceStack.getPlayer();
			return serverPlayer != null && this.signedArgument.hasSignatureFrom(serverPlayer)
				? serverPlayer.getTextFilter().processStreamMessage(string)
				: CompletableFuture.completedFuture(FilteredText.passThrough(string));
		}

		public void consume(CommandSourceStack commandSourceStack) {
			if (!this.signedArgument.signer().isSystem()) {
				this.resolve(commandSourceStack, filteredText -> {
					PlayerList playerList = commandSourceStack.getServer().getPlayerList();
					playerList.broadcastMessageHeader((PlayerChatMessage)filteredText.raw(), Set.of());
				});
			}
		}
	}

	public static class Message {
		final String text;
		private final MessageArgument.Part[] parts;

		public Message(String string, MessageArgument.Part[] parts) {
			this.text = string;
			this.parts = parts;
		}

		public String getText() {
			return this.text;
		}

		public MessageArgument.Part[] getParts() {
			return this.parts;
		}

		CompletableFuture<Component> resolveDecoratedComponent(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
			Component component = this.resolveComponent(commandSourceStack);
			CompletableFuture<Component> completableFuture = commandSourceStack.getServer().getChatDecorator().decorate(commandSourceStack.getPlayer(), component);
			MessageArgument.logResolutionFailure(commandSourceStack, completableFuture);
			return completableFuture;
		}

		Component resolveComponent(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
			return this.toComponent(commandSourceStack, commandSourceStack.hasPermission(2));
		}

		public Component toComponent(CommandSourceStack commandSourceStack, boolean bl) throws CommandSyntaxException {
			if (this.parts.length != 0 && bl) {
				MutableComponent mutableComponent = Component.literal(this.text.substring(0, this.parts[0].getStart()));
				int i = this.parts[0].getStart();

				for (MessageArgument.Part part : this.parts) {
					Component component = part.toComponent(commandSourceStack);
					if (i < part.getStart()) {
						mutableComponent.append(this.text.substring(i, part.getStart()));
					}

					if (component != null) {
						mutableComponent.append(component);
					}

					i = part.getEnd();
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
			String string = stringReader.getString().substring(stringReader.getCursor(), stringReader.getTotalLength());
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

	public static class Part {
		private final int start;
		private final int end;
		private final EntitySelector selector;

		public Part(int i, int j, EntitySelector entitySelector) {
			this.start = i;
			this.end = j;
			this.selector = entitySelector;
		}

		public int getStart() {
			return this.start;
		}

		public int getEnd() {
			return this.end;
		}

		public EntitySelector getSelector() {
			return this.selector;
		}

		@Nullable
		public Component toComponent(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
			return EntitySelector.joinNames(this.selector.findEntities(commandSourceStack));
		}
	}
}
