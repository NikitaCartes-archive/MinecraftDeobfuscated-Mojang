package net.minecraft.commands.arguments.blocks;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockPredicateArgument implements ArgumentType<BlockPredicateArgument.Result> {
	private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "#stone", "#stone[foo=bar]{baz=nbt}");
	private final HolderLookup<Block> blocks;

	public BlockPredicateArgument(CommandBuildContext commandBuildContext) {
		this.blocks = commandBuildContext.holderLookup(Registry.BLOCK_REGISTRY);
	}

	public static BlockPredicateArgument blockPredicate(CommandBuildContext commandBuildContext) {
		return new BlockPredicateArgument(commandBuildContext);
	}

	public BlockPredicateArgument.Result parse(StringReader stringReader) throws CommandSyntaxException {
		return parse(this.blocks, stringReader);
	}

	public static BlockPredicateArgument.Result parse(HolderLookup<Block> holderLookup, StringReader stringReader) throws CommandSyntaxException {
		return BlockStateParser.parseForTesting(holderLookup, stringReader, true)
			.map(
				blockResult -> new BlockPredicateArgument.BlockPredicate(blockResult.blockState(), blockResult.properties().keySet(), blockResult.nbt()),
				tagResult -> new BlockPredicateArgument.TagPredicate(tagResult.tag(), tagResult.vagueProperties(), tagResult.nbt())
			);
	}

	public static Predicate<BlockInWorld> getBlockPredicate(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return commandContext.getArgument(string, BlockPredicateArgument.Result.class);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		return BlockStateParser.fillSuggestions(this.blocks, suggestionsBuilder, true, true);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	static class BlockPredicate implements BlockPredicateArgument.Result {
		private final BlockState state;
		private final Set<Property<?>> properties;
		@Nullable
		private final CompoundTag nbt;

		public BlockPredicate(BlockState blockState, Set<Property<?>> set, @Nullable CompoundTag compoundTag) {
			this.state = blockState;
			this.properties = set;
			this.nbt = compoundTag;
		}

		public boolean test(BlockInWorld blockInWorld) {
			BlockState blockState = blockInWorld.getState();
			if (!blockState.is(this.state.getBlock())) {
				return false;
			} else {
				for (Property<?> property : this.properties) {
					if (blockState.getValue(property) != this.state.getValue(property)) {
						return false;
					}
				}

				if (this.nbt == null) {
					return true;
				} else {
					BlockEntity blockEntity = blockInWorld.getEntity();
					return blockEntity != null && NbtUtils.compareNbt(this.nbt, blockEntity.saveWithFullMetadata(), true);
				}
			}
		}

		@Override
		public boolean requiresNbt() {
			return this.nbt != null;
		}
	}

	public interface Result extends Predicate<BlockInWorld> {
		boolean requiresNbt();
	}

	static class TagPredicate implements BlockPredicateArgument.Result {
		private final HolderSet<Block> tag;
		@Nullable
		private final CompoundTag nbt;
		private final Map<String, String> vagueProperties;

		TagPredicate(HolderSet<Block> holderSet, Map<String, String> map, @Nullable CompoundTag compoundTag) {
			this.tag = holderSet;
			this.vagueProperties = map;
			this.nbt = compoundTag;
		}

		public boolean test(BlockInWorld blockInWorld) {
			BlockState blockState = blockInWorld.getState();
			if (!blockState.is(this.tag)) {
				return false;
			} else {
				for (Entry<String, String> entry : this.vagueProperties.entrySet()) {
					Property<?> property = blockState.getBlock().getStateDefinition().getProperty((String)entry.getKey());
					if (property == null) {
						return false;
					}

					Comparable<?> comparable = (Comparable<?>)property.getValue((String)entry.getValue()).orElse(null);
					if (comparable == null) {
						return false;
					}

					if (blockState.getValue(property) != comparable) {
						return false;
					}
				}

				if (this.nbt == null) {
					return true;
				} else {
					BlockEntity blockEntity = blockInWorld.getEntity();
					return blockEntity != null && NbtUtils.compareNbt(this.nbt, blockEntity.saveWithFullMetadata(), true);
				}
			}
		}

		@Override
		public boolean requiresNbt() {
			return this.nbt != null;
		}
	}
}
