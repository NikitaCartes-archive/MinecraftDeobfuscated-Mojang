/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemPredicateArgument
implements ArgumentType<Result> {
    private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "#stick", "#stick{foo=bar}");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(object -> new TranslatableComponent("arguments.item.tag.unknown", object));

    public static ItemPredicateArgument itemPredicate() {
        return new ItemPredicateArgument();
    }

    @Override
    public Result parse(StringReader stringReader) throws CommandSyntaxException {
        ItemParser itemParser = new ItemParser(stringReader, true).parse();
        if (itemParser.getItem() != null) {
            ItemPredicate itemPredicate = new ItemPredicate(itemParser.getItem(), itemParser.getNbt());
            return commandContext -> itemPredicate;
        }
        ResourceLocation resourceLocation = itemParser.getTag();
        return commandContext -> {
            Tag<Item> tag = ((CommandSourceStack)commandContext.getSource()).getServer().getTags().getTagOrThrow(Registry.ITEM_REGISTRY, resourceLocation, resourceLocation -> ERROR_UNKNOWN_TAG.create(resourceLocation.toString()));
            return new TagPredicate(tag, itemParser.getNbt());
        };
    }

    public static Predicate<ItemStack> getItemPredicate(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return commandContext.getArgument(string, Result.class).create(commandContext);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        StringReader stringReader = new StringReader(suggestionsBuilder.getInput());
        stringReader.setCursor(suggestionsBuilder.getStart());
        ItemParser itemParser = new ItemParser(stringReader, true);
        try {
            itemParser.parse();
        } catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        return itemParser.fillSuggestions(suggestionsBuilder, ItemTags.getAllTags());
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    static class ItemPredicate
    implements Predicate<ItemStack> {
        private final Item item;
        @Nullable
        private final CompoundTag nbt;

        public ItemPredicate(Item item, @Nullable CompoundTag compoundTag) {
            this.item = item;
            this.nbt = compoundTag;
        }

        @Override
        public boolean test(ItemStack itemStack) {
            return itemStack.is(this.item) && NbtUtils.compareNbt(this.nbt, itemStack.getTag(), true);
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((ItemStack)object);
        }
    }

    public static interface Result {
        public Predicate<ItemStack> create(CommandContext<CommandSourceStack> var1) throws CommandSyntaxException;
    }

    static class TagPredicate
    implements Predicate<ItemStack> {
        private final Tag<Item> tag;
        @Nullable
        private final CompoundTag nbt;

        public TagPredicate(Tag<Item> tag, @Nullable CompoundTag compoundTag) {
            this.tag = tag;
            this.nbt = compoundTag;
        }

        @Override
        public boolean test(ItemStack itemStack) {
            return itemStack.is(this.tag) && NbtUtils.compareNbt(this.nbt, itemStack.getTag(), true);
        }

        @Override
        public /* synthetic */ boolean test(Object object) {
            return this.test((ItemStack)object);
        }
    }
}

