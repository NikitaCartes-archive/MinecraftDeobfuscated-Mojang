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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagContainer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockPredicateArgument implements ArgumentType<BlockPredicateArgument.Result> {
	private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]", "#stone", "#stone[foo=bar]{baz=nbt}");
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("arguments.block.tag.unknown", object)
	);

	public static BlockPredicateArgument blockPredicate() {
		return new BlockPredicateArgument();
	}

	public BlockPredicateArgument.Result parse(StringReader stringReader) throws CommandSyntaxException {
		BlockStateParser blockStateParser = new BlockStateParser(stringReader, true).parse(true);
		if (blockStateParser.getState() != null) {
			BlockPredicateArgument.BlockPredicate blockPredicate = new BlockPredicateArgument.BlockPredicate(
				blockStateParser.getState(), blockStateParser.getProperties().keySet(), blockStateParser.getNbt()
			);
			return tagContainer -> blockPredicate;
		} else {
			ResourceLocation resourceLocation = blockStateParser.getTag();
			return tagContainer -> {
				Tag<Block> tag = tagContainer.getTagOrThrow(
					Registry.BLOCK_REGISTRY, resourceLocation, resourceLocationxx -> ERROR_UNKNOWN_TAG.create(resourceLocationxx.toString())
				);
				return new BlockPredicateArgument.TagPredicate(tag, blockStateParser.getVagueProperties(), blockStateParser.getNbt());
			};
		}
	}

	public static Predicate<BlockInWorld> getBlockPredicate(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return commandContext.<BlockPredicateArgument.Result>getArgument(string, BlockPredicateArgument.Result.class)
			.create(commandContext.getSource().getServer().getTags());
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

		return blockStateParser.fillSuggestions(suggestionsBuilder, BlockTags.getAllTags());
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
					return blockEntity != null && NbtUtils.compareNbt(this.nbt, blockEntity.save(new CompoundTag()), true);
				}
			}
		}
	}

	public interface Result {
		Predicate<BlockInWorld> create(TagContainer tagContainer) throws CommandSyntaxException;
	}

	static class TagPredicate implements Predicate<BlockInWorld> {
		private final Tag<Block> tag;
		@Nullable
		private final CompoundTag nbt;
		private final Map<String, String> vagueProperties;

		TagPredicate(Tag<Block> tag, Map<String, String> map, @Nullable CompoundTag compoundTag) {
			this.tag = tag;
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
					return blockEntity != null && NbtUtils.compareNbt(this.nbt, blockEntity.save(new CompoundTag()), true);
				}
			}
		}
	}
}
