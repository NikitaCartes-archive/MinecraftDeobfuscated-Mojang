package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;

public class ScoreHolderArgument implements ArgumentType<ScoreHolderArgument.Result> {
	public static final SuggestionProvider<CommandSourceStack> SUGGEST_SCORE_HOLDERS = (commandContext, suggestionsBuilder) -> {
		StringReader stringReader = new StringReader(suggestionsBuilder.getInput());
		stringReader.setCursor(suggestionsBuilder.getStart());
		EntitySelectorParser entitySelectorParser = new EntitySelectorParser(stringReader);

		try {
			entitySelectorParser.parse();
		} catch (CommandSyntaxException var5) {
		}

		return entitySelectorParser.fillSuggestions(
			suggestionsBuilder, suggestionsBuilderx -> SharedSuggestionProvider.suggest(commandContext.getSource().getOnlinePlayerNames(), suggestionsBuilderx)
		);
	};
	private static final Collection<String> EXAMPLES = Arrays.asList("Player", "0123", "*", "@e");
	private static final SimpleCommandExceptionType ERROR_NO_RESULTS = new SimpleCommandExceptionType(new TranslatableComponent("argument.scoreHolder.empty"));
	final boolean multiple;

	public ScoreHolderArgument(boolean bl) {
		this.multiple = bl;
	}

	public static String getName(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return (String)getNames(commandContext, string).iterator().next();
	}

	public static Collection<String> getNames(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return getNames(commandContext, string, Collections::emptyList);
	}

	public static Collection<String> getNamesWithDefaultWildcard(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return getNames(commandContext, string, commandContext.getSource().getServer().getScoreboard()::getTrackedPlayers);
	}

	public static Collection<String> getNames(CommandContext<CommandSourceStack> commandContext, String string, Supplier<Collection<String>> supplier) throws CommandSyntaxException {
		Collection<String> collection = commandContext.<ScoreHolderArgument.Result>getArgument(string, ScoreHolderArgument.Result.class)
			.getNames(commandContext.getSource(), supplier);
		if (collection.isEmpty()) {
			throw EntityArgument.NO_ENTITIES_FOUND.create();
		} else {
			return collection;
		}
	}

	public static ScoreHolderArgument scoreHolder() {
		return new ScoreHolderArgument(false);
	}

	public static ScoreHolderArgument scoreHolders() {
		return new ScoreHolderArgument(true);
	}

	public ScoreHolderArgument.Result parse(StringReader stringReader) throws CommandSyntaxException {
		if (stringReader.canRead() && stringReader.peek() == '@') {
			EntitySelectorParser entitySelectorParser = new EntitySelectorParser(stringReader);
			EntitySelector entitySelector = entitySelectorParser.parse();
			if (!this.multiple && entitySelector.getMaxResults() > 1) {
				throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
			} else {
				return new ScoreHolderArgument.SelectorResult(entitySelector);
			}
		} else {
			int i = stringReader.getCursor();

			while (stringReader.canRead() && stringReader.peek() != ' ') {
				stringReader.skip();
			}

			String string = stringReader.getString().substring(i, stringReader.getCursor());
			if (string.equals("*")) {
				return (commandSourceStack, supplier) -> {
					Collection<String> collectionx = (Collection<String>)supplier.get();
					if (collectionx.isEmpty()) {
						throw ERROR_NO_RESULTS.create();
					} else {
						return collectionx;
					}
				};
			} else {
				Collection<String> collection = Collections.singleton(string);
				return (commandSourceStack, supplier) -> collection;
			}
		}
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	public static class Info implements ArgumentTypeInfo<ScoreHolderArgument, ScoreHolderArgument.Info.Template> {
		private static final byte FLAG_MULTIPLE = 1;

		public void serializeToNetwork(ScoreHolderArgument.Info.Template template, FriendlyByteBuf friendlyByteBuf) {
			int i = 0;
			if (template.multiple) {
				i |= 1;
			}

			friendlyByteBuf.writeByte(i);
		}

		public ScoreHolderArgument.Info.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
			byte b = friendlyByteBuf.readByte();
			boolean bl = (b & 1) != 0;
			return new ScoreHolderArgument.Info.Template(bl);
		}

		public void serializeToJson(ScoreHolderArgument.Info.Template template, JsonObject jsonObject) {
			jsonObject.addProperty("amount", template.multiple ? "multiple" : "single");
		}

		public ScoreHolderArgument.Info.Template unpack(ScoreHolderArgument scoreHolderArgument) {
			return new ScoreHolderArgument.Info.Template(scoreHolderArgument.multiple);
		}

		public final class Template implements ArgumentTypeInfo.Template<ScoreHolderArgument> {
			final boolean multiple;

			Template(boolean bl) {
				this.multiple = bl;
			}

			public ScoreHolderArgument instantiate(CommandBuildContext commandBuildContext) {
				return new ScoreHolderArgument(this.multiple);
			}

			@Override
			public ArgumentTypeInfo<ScoreHolderArgument, ?> type() {
				return Info.this;
			}
		}
	}

	@FunctionalInterface
	public interface Result {
		Collection<String> getNames(CommandSourceStack commandSourceStack, Supplier<Collection<String>> supplier) throws CommandSyntaxException;
	}

	public static class SelectorResult implements ScoreHolderArgument.Result {
		private final EntitySelector selector;

		public SelectorResult(EntitySelector entitySelector) {
			this.selector = entitySelector;
		}

		@Override
		public Collection<String> getNames(CommandSourceStack commandSourceStack, Supplier<Collection<String>> supplier) throws CommandSyntaxException {
			List<? extends Entity> list = this.selector.findEntities(commandSourceStack);
			if (list.isEmpty()) {
				throw EntityArgument.NO_ENTITIES_FOUND.create();
			} else {
				List<String> list2 = Lists.<String>newArrayList();

				for (Entity entity : list) {
					list2.add(entity.getScoreboardName());
				}

				return list2;
			}
		}
	}
}
