package net.minecraft.commands.arguments.blocks;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.tags.BlockTags;

public class BlockStateArgument implements ArgumentType<BlockInput> {
	private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "foo{bar=baz}");

	public static BlockStateArgument block() {
		return new BlockStateArgument();
	}

	public BlockInput parse(StringReader stringReader) throws CommandSyntaxException {
		BlockStateParser blockStateParser = new BlockStateParser(stringReader, false).parse(true);
		return new BlockInput(blockStateParser.getState(), blockStateParser.getProperties().keySet(), blockStateParser.getNbt());
	}

	public static BlockInput getBlock(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.getArgument(string, BlockInput.class);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		StringReader stringReader = new StringReader(suggestionsBuilder.getInput());
		stringReader.setCursor(suggestionsBuilder.getStart());
		BlockStateParser blockStateParser = new BlockStateParser(stringReader, false);

		try {
			blockStateParser.parse(true);
		} catch (CommandSyntaxException var6) {
		}

		return blockStateParser.fillSuggestions(suggestionsBuilder, BlockTags.getAllTags());
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
