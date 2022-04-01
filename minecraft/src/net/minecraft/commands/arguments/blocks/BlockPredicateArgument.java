package net.minecraft.commands.arguments.blocks;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockPredicateArgument implements ArgumentType<BlockPredicateArgument.Result> {
	private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "#stone", "#stone[foo=bar]{baz=nbt}");
	static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("arguments.block.tag.unknown", object)
	);

	public static BlockPredicateArgument blockPredicate() {
		return new BlockPredicateArgument();
	}

	public BlockPredicateArgument.Result parse(StringReader stringReader) throws CommandSyntaxException {
		final BlockStateParser blockStateParser = new BlockStateParser(stringReader, true).parse(true);
		if (blockStateParser.getState() != null) {
			final BlockPredicateArgument.BlockPredicate blockPredicate = new BlockPredicateArgument.BlockPredicate(
				blockStateParser.getState(), blockStateParser.getProperties().keySet(), blockStateParser.getNbt()
			);
			return new BlockPredicateArgument.Result() {
				@Override
				public Predicate<BlockInWorld> create(Registry<Block> registry) {
					return blockPredicate;
				}

				@Override
				public boolean requiresNbt() {
					return blockPredicate.requiresNbt();
				}
			};
		} else {
			final TagKey<Block> tagKey = blockStateParser.getTag();
			return new BlockPredicateArgument.Result() {
				@Override
				public Predicate<BlockInWorld> create(Registry<Block> registry) throws CommandSyntaxException {
					if (!registry.isKnownTagName(tagKey)) {
						throw BlockPredicateArgument.ERROR_UNKNOWN_TAG.create(tagKey);
					} else {
						return new BlockPredicateArgument.TagPredicate(tagKey, blockStateParser.getVagueProperties(), blockStateParser.getNbt());
					}
				}

				@Override
				public boolean requiresNbt() {
					return blockStateParser.getNbt() != null;
				}
			};
		}
	}

	public static Predicate<BlockInWorld> getBlockPredicate(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return commandContext.<BlockPredicateArgument.Result>getArgument(string, BlockPredicateArgument.Result.class)
			.create(commandContext.getSource().getServer().registryAccess().registryOrThrow(Registry.BLOCK_REGISTRY));
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		StringReader stringReader = new StringReader(suggestionsBuilder.getInput());
		stringReader.setCursor(suggestionsBuilder.getStart());
		BlockStateParser blockStateParser = new BlockStateParser(stringReader, true);

		try {
			blockStateParser.parse(true);
		} catch (CommandSyntaxException var6) {
		}

		return blockStateParser.fillSuggestions(suggestionsBuilder, Registry.BLOCK);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	static class BlockPredicate implements Predicate<BlockInWorld> {
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

		public boolean requiresNbt() {
			return this.nbt != null;
		}
	}

	public interface Result {
		Predicate<BlockInWorld> create(Registry<Block> registry) throws CommandSyntaxException;

		boolean requiresNbt();
	}

	static class TagPredicate implements Predicate<BlockInWorld> {
		private final TagKey<Block> tag;
		@Nullable
		private final CompoundTag nbt;
		private final Map<String, String> vagueProperties;

		TagPredicate(TagKey<Block> tagKey, Map<String, String> map, @Nullable CompoundTag compoundTag) {
			this.tag = tagKey;
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
	}
}
