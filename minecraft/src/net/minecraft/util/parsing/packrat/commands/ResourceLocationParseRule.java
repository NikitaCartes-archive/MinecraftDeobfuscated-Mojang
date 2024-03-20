package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;

public class ResourceLocationParseRule implements Rule<StringReader, ResourceLocation> {
	public static final Rule<StringReader, ResourceLocation> INSTANCE = new ResourceLocationParseRule();

	private ResourceLocationParseRule() {
	}

	@Override
	public Optional<ResourceLocation> parse(ParseState<StringReader> parseState) {
		parseState.input().skipWhitespace();

		try {
			return Optional.of(ResourceLocation.readNonEmpty(parseState.input()));
		} catch (CommandSyntaxException var3) {
			return Optional.empty();
		}
	}
}
