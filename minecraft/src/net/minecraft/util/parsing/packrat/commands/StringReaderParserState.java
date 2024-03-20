package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.ErrorCollector;
import net.minecraft.util.parsing.packrat.ParseState;

public class StringReaderParserState extends ParseState<StringReader> {
	private final StringReader input;

	public StringReaderParserState(Dictionary<StringReader> dictionary, ErrorCollector<StringReader> errorCollector, StringReader stringReader) {
		super(dictionary, errorCollector);
		this.input = stringReader;
	}

	public StringReader input() {
		return this.input;
	}

	@Override
	public int mark() {
		return this.input.getCursor();
	}

	@Override
	public void restore(int i) {
		this.input.setCursor(i);
	}
}
