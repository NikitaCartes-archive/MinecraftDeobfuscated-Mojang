package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class MessageArgument implements ArgumentType<MessageArgument.Message> {
	private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");

	public static MessageArgument message() {
		return new MessageArgument();
	}

	public static Component getMessage(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return commandContext.<MessageArgument.Message>getArgument(string, MessageArgument.Message.class)
			.toComponent(commandContext.getSource(), commandContext.getSource().hasPermission(2));
	}

	public MessageArgument.Message parse(StringReader stringReader) throws CommandSyntaxException {
		return MessageArgument.Message.parseText(stringReader, true);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public static class Message {
		private final String text;
		private final MessageArgument.Part[] parts;

		public Message(String string, MessageArgument.Part[] parts) {
			this.text = string;
			this.parts = parts;
		}

		public Component toComponent(CommandSourceStack commandSourceStack, boolean bl) throws CommandSyntaxException {
			if (this.parts.length != 0 && bl) {
				Component component = new TextComponent(this.text.substring(0, this.parts[0].getStart()));
				int i = this.parts[0].getStart();

				for (MessageArgument.Part part : this.parts) {
					Component component2 = part.toComponent(commandSourceStack);
					if (i < part.getStart()) {
						component.append(this.text.substring(i, part.getStart()));
					}

					if (component2 != null) {
						component.append(component2);
					}

					i = part.getEnd();
				}

				if (i < this.text.length()) {
					component.append(this.text.substring(i, this.text.length()));
				}

				return component;
			} else {
				return new TextComponent(this.text);
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
							return new MessageArgument.Message(string, (MessageArgument.Part[])list.toArray(new MessageArgument.Part[list.size()]));
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

		@Nullable
		public Component toComponent(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
			return EntitySelector.joinNames(this.selector.findEntities(commandSourceStack));
		}
	}
}
