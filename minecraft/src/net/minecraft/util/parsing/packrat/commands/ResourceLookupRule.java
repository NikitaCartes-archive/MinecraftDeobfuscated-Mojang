package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;

public abstract class ResourceLookupRule<C, V> implements Rule<StringReader, V>, ResourceSuggestion {
	private final Atom<ResourceLocation> idParser;
	protected final C context;

	protected ResourceLookupRule(Atom<ResourceLocation> atom, C object) {
		this.idParser = atom;
		this.context = object;
	}

	@Override
	public Optional<V> parse(ParseState<StringReader> parseState) {
		parseState.input().skipWhitespace();
		int i = parseState.mark();
		Optional<ResourceLocation> optional = parseState.parse(this.idParser);
		if (optional.isPresent()) {
			try {
				return Optional.of(this.validateElement(parseState.input(), (ResourceLocation)optional.get()));
			} catch (Exception var5) {
				parseState.errorCollector().store(i, this, var5);
				return Optional.empty();
			}
		} else {
			parseState.errorCollector().store(i, this, ResourceLocation.ERROR_INVALID.createWithContext(parseState.input()));
			return Optional.empty();
		}
	}

	protected abstract V validateElement(ImmutableStringReader immutableStringReader, ResourceLocation resourceLocation) throws Exception;
}
