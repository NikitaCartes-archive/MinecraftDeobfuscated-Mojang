package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.ErrorCollector;
import net.minecraft.util.parsing.packrat.ErrorEntry;
import net.minecraft.util.parsing.packrat.ParseState;

public record Grammar<T>(Dictionary<StringReader> rules, Atom<T> top) {
	public Optional<T> parse(ParseState<StringReader> parseState) {
		return parseState.parseTopRule(this.top);
	}

	public T parseForCommands(StringReader stringReader) throws CommandSyntaxException {
		ErrorCollector.LongestOnly<StringReader> longestOnly = new ErrorCollector.LongestOnly<>();
		StringReaderParserState stringReaderParserState = new StringReaderParserState(this.rules(), longestOnly, stringReader);
		Optional<T> optional = this.parse(stringReaderParserState);
		if (optional.isPresent()) {
			return (T)optional.get();
		} else {
			List<Exception> list = longestOnly.entries().stream().mapMulti((errorEntry, consumer) -> {
				if (errorEntry.reason() instanceof Exception exceptionx) {
					consumer.accept(exceptionx);
				}
			}).toList();

			for (Exception exception : list) {
				if (exception instanceof CommandSyntaxException commandSyntaxException) {
					throw commandSyntaxException;
				}
			}

			if (list.size() == 1 && list.get(0) instanceof RuntimeException runtimeException) {
				throw runtimeException;
			} else {
				throw new IllegalStateException("Failed to parse: " + (String)longestOnly.entries().stream().map(ErrorEntry::toString).collect(Collectors.joining(", ")));
			}
		}
	}

	public CompletableFuture<Suggestions> parseForSuggestions(SuggestionsBuilder suggestionsBuilder) {
		StringReader stringReader = new StringReader(suggestionsBuilder.getInput());
		stringReader.setCursor(suggestionsBuilder.getStart());
		ErrorCollector.LongestOnly<StringReader> longestOnly = new ErrorCollector.LongestOnly<>();
		StringReaderParserState stringReaderParserState = new StringReaderParserState(this.rules(), longestOnly, stringReader);
		this.parse(stringReaderParserState);
		List<ErrorEntry<StringReader>> list = longestOnly.entries();
		if (list.isEmpty()) {
			return suggestionsBuilder.buildFuture();
		} else {
			SuggestionsBuilder suggestionsBuilder2 = suggestionsBuilder.createOffset(longestOnly.cursor());

			for (ErrorEntry<StringReader> errorEntry : list) {
				if (errorEntry.suggestions() instanceof ResourceSuggestion resourceSuggestion) {
					SharedSuggestionProvider.suggestResource(resourceSuggestion.possibleResources(), suggestionsBuilder2);
				} else {
					SharedSuggestionProvider.suggest(errorEntry.suggestions().possibleValues(stringReaderParserState), suggestionsBuilder2);
				}
			}

			return suggestionsBuilder2.buildFuture();
		}
	}
}
