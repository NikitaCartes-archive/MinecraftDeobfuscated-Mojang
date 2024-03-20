package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.SuggestionSupplier;

public interface ResourceSuggestion extends SuggestionSupplier<StringReader> {
	Stream<ResourceLocation> possibleResources();

	@Override
	default Stream<String> possibleValues(ParseState<StringReader> parseState) {
		return this.possibleResources().map(ResourceLocation::toString);
	}
}
