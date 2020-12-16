package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemPredicateArgument implements ArgumentType<ItemPredicateArgument.Result> {
	private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "#stick", "#stick{foo=bar}");
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("arguments.item.tag.unknown", object)
	);

	public static ItemPredicateArgument itemPredicate() {
		return new ItemPredicateArgument();
	}

	public ItemPredicateArgument.Result parse(StringReader stringReader) throws CommandSyntaxException {
		ItemParser itemParser = new ItemParser(stringReader, true).parse();
		if (itemParser.getItem() != null) {
			ItemPredicateArgument.ItemPredicate itemPredicate = new ItemPredicateArgument.ItemPredicate(itemParser.getItem(), itemParser.getNbt());
			return commandContext -> itemPredicate;
		} else {
			ResourceLocation resourceLocation = itemParser.getTag();
			return commandContext -> {
				Tag<Item> tag = commandContext.getSource()
					.getServer()
					.getTags()
					.getTagOrThrow(Registry.ITEM_REGISTRY, resourceLocation, resourceLocationxx -> ERROR_UNKNOWN_TAG.create(resourceLocationxx.toString()));
				return new ItemPredicateArgument.TagPredicate(tag, itemParser.getNbt());
			};
		}
	}

	public static Predicate<ItemStack> getItemPredicate(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		return commandContext.<ItemPredicateArgument.Result>getArgument(string, ItemPredicateArgument.Result.class).create(commandContext);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		StringReader stringReader = new StringReader(suggestionsBuilder.getInput());
		stringReader.setCursor(suggestionsBuilder.getStart());
		ItemParser itemParser = new ItemParser(stringReader, true);

		try {
			itemParser.parse();
		} catch (CommandSyntaxException var6) {
		}

		return itemParser.fillSuggestions(suggestionsBuilder, ItemTags.getAllTags());
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	static class ItemPredicate implements Predicate<ItemStack> {
		private final Item item;
		@Nullable
		private final CompoundTag nbt;

		public ItemPredicate(Item item, @Nullable CompoundTag compoundTag) {
			this.item = item;
			this.nbt = compoundTag;
		}

		public boolean test(ItemStack itemStack) {
			return itemStack.is(this.item) && NbtUtils.compareNbt(this.nbt, itemStack.getTag(), true);
		}
	}

	public interface Result {
		Predicate<ItemStack> create(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException;
	}

	static class TagPredicate implements Predicate<ItemStack> {
		private final Tag<Item> tag;
		@Nullable
		private final CompoundTag nbt;

		public TagPredicate(Tag<Item> tag, @Nullable CompoundTag compoundTag) {
			this.tag = tag;
			this.nbt = compoundTag;
		}

		public boolean test(ItemStack itemStack) {
			return itemStack.is(this.tag) && NbtUtils.compareNbt(this.nbt, itemStack.getTag(), true);
		}
	}
}
