package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class TimeArgument implements ArgumentType<Integer> {
	private static final Collection<String> EXAMPLES = Arrays.asList("0d", "0s", "0t", "0");
	private static final SimpleCommandExceptionType ERROR_INVALID_UNIT = new SimpleCommandExceptionType(Component.translatable("argument.time.invalid_unit"));
	private static final Dynamic2CommandExceptionType ERROR_TICK_COUNT_TOO_LOW = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatable("argument.time.tick_count_too_low", object2, object)
	);
	private static final Object2IntMap<String> UNITS = new Object2IntOpenHashMap<>();
	final int minimum;

	private TimeArgument(int i) {
		this.minimum = i;
	}

	public static TimeArgument time() {
		return new TimeArgument(0);
	}

	public static TimeArgument time(int i) {
		return new TimeArgument(i);
	}

	public Integer parse(StringReader stringReader) throws CommandSyntaxException {
		float f = stringReader.readFloat();
		String string = stringReader.readUnquotedString();
		int i = UNITS.getOrDefault(string, 0);
		if (i == 0) {
			throw ERROR_INVALID_UNIT.create();
		} else {
			int j = Math.round(f * (float)i);
			if (j < this.minimum) {
				throw ERROR_TICK_COUNT_TOO_LOW.create(j, this.minimum);
			} else {
				return j;
			}
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		StringReader stringReader = new StringReader(suggestionsBuilder.getRemaining());

		try {
			stringReader.readFloat();
		} catch (CommandSyntaxException var5) {
			return suggestionsBuilder.buildFuture();
		}

		return SharedSuggestionProvider.suggest(UNITS.keySet(), suggestionsBuilder.createOffset(suggestionsBuilder.getStart() + stringReader.getCursor()));
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	static {
		UNITS.put("d", 24000);
		UNITS.put("s", 20);
		UNITS.put("t", 1);
		UNITS.put("", 1);
	}

	public static class Info implements ArgumentTypeInfo<TimeArgument, TimeArgument.Info.Template> {
		public void serializeToNetwork(TimeArgument.Info.Template template, FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeInt(template.min);
		}

		public TimeArgument.Info.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
			int i = friendlyByteBuf.readInt();
			return new TimeArgument.Info.Template(i);
		}

		public void serializeToJson(TimeArgument.Info.Template template, JsonObject jsonObject) {
			jsonObject.addProperty("min", template.min);
		}

		public TimeArgument.Info.Template unpack(TimeArgument timeArgument) {
			return new TimeArgument.Info.Template(timeArgument.minimum);
		}

		public final class Template implements ArgumentTypeInfo.Template<TimeArgument> {
			final int min;

			Template(int i) {
				this.min = i;
			}

			public TimeArgument instantiate(CommandBuildContext commandBuildContext) {
				return TimeArgument.time(this.min);
			}

			@Override
			public ArgumentTypeInfo<TimeArgument, ?> type() {
				return Info.this;
			}
		}
	}
}
