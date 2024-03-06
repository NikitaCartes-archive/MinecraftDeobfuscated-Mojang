package net.minecraft.commands.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ObjectiveCriteriaArgument implements ArgumentType<ObjectiveCriteria> {
	private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo.bar.baz", "minecraft:foo");
	public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("argument.criteria.invalid", object)
	);

	private ObjectiveCriteriaArgument() {
	}

	public static ObjectiveCriteriaArgument criteria() {
		return new ObjectiveCriteriaArgument();
	}

	public static ObjectiveCriteria getCriteria(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.getArgument(string, ObjectiveCriteria.class);
	}

	public ObjectiveCriteria parse(StringReader stringReader) throws CommandSyntaxException {
		int i = stringReader.getCursor();

		while (stringReader.canRead() && stringReader.peek() != ' ') {
			stringReader.skip();
		}

		String string = stringReader.getString().substring(i, stringReader.getCursor());
		return (ObjectiveCriteria)ObjectiveCriteria.byName(string).orElseThrow(() -> {
			stringReader.setCursor(i);
			return ERROR_INVALID_VALUE.createWithContext(stringReader, string);
		});
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		List<String> list = Lists.<String>newArrayList(ObjectiveCriteria.getCustomCriteriaNames());

		for (StatType<?> statType : BuiltInRegistries.STAT_TYPE) {
			for (Object object : statType.getRegistry()) {
				String string = this.getName(statType, object);
				list.add(string);
			}
		}

		return SharedSuggestionProvider.suggest(list, suggestionsBuilder);
	}

	public <T> String getName(StatType<T> statType, Object object) {
		return Stat.buildName(statType, (T)object);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
