package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.stream.Stream;
import net.minecraft.util.parsing.packrat.Control;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;
import net.minecraft.util.parsing.packrat.Term;

public interface StringReaderTerms {
	static Term<StringReader> word(String string) {
		return new StringReaderTerms.TerminalWord(string);
	}

	static Term<StringReader> character(char c) {
		return new StringReaderTerms.TerminalCharacter(c);
	}

	public static record TerminalCharacter(char value) implements Term<StringReader> {
		@Override
		public boolean parse(ParseState<StringReader> parseState, Scope scope, Control control) {
			parseState.input().skipWhitespace();
			int i = parseState.mark();
			if (parseState.input().canRead() && parseState.input().read() == this.value) {
				return true;
			} else {
				parseState.errorCollector()
					.store(i, parseStatex -> Stream.of(String.valueOf(this.value)), CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(this.value));
				return false;
			}
		}
	}

	public static record TerminalWord(String value) implements Term<StringReader> {
		@Override
		public boolean parse(ParseState<StringReader> parseState, Scope scope, Control control) {
			parseState.input().skipWhitespace();
			int i = parseState.mark();
			String string = parseState.input().readUnquotedString();
			if (!string.equals(this.value)) {
				parseState.errorCollector()
					.store(i, parseStatex -> Stream.of(this.value), CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create(this.value));
				return false;
			} else {
				return true;
			}
		}
	}
}
