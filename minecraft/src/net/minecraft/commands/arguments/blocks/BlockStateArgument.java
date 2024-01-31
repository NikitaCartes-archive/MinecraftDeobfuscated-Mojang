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
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

public class BlockStateArgument implements ArgumentType<BlockInput> {
	private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "foo{bar=baz}");
	private final HolderLookup<Block> blocks;

	public BlockStateArgument(CommandBuildContext commandBuildContext) {
		this.blocks = commandBuildContext.lookupOrThrow(Registries.BLOCK);
	}

	public static BlockStateArgument block(CommandBuildContext commandBuildContext) {
		return new BlockStateArgument(commandBuildContext);
	}

	public BlockInput parse(StringReader stringReader) throws CommandSyntaxException {
		BlockStateParser.BlockResult blockResult = BlockStateParser.parseForBlock(this.blocks, stringReader, true);
		return new BlockInput(blockResult.blockState(), blockResult.properties().keySet(), blockResult.nbt());
	}

	public static BlockInput getBlock(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.getArgument(string, BlockInput.class);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		return BlockStateParser.fillSuggestions(this.blocks, suggestionsBuilder, false, true);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
