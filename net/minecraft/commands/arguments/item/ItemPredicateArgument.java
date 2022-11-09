/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemPredicateArgument
implements ArgumentType<Result> {
    private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "#stick", "#stick{foo=bar}");
    private final HolderLookup<Item> items;

    public ItemPredicateArgument(CommandBuildContext commandBuildContext) {
        this.items = commandBuildContext.holderLookup(Registries.ITEM);
    }

    public static ItemPredicateArgument itemPredicate(CommandBuildContext commandBuildContext) {
        return new ItemPredicateArgument(commandBuildContext);
    }

    @Override
    public Result parse(StringReader stringReader) throws CommandSyntaxException {
        Either<ItemParser.ItemResult, ItemParser.TagResult> either = ItemParser.parseForTesting(this.items, stringReader);
        return either.map(itemResult -> ItemPredicateArgument.createResult(holder -> holder == itemResult.item(), itemResult.nbt()), tagResult -> ItemPredicateArgument.createResult(tagResult.tag()::contains, tagResult.nbt()));
    }

    public static Predicate<ItemStack> getItemPredicate(CommandContext<CommandSourceStack> commandContext, String string) {
        return commandContext.getArgument(string, Result.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        return ItemParser.fillSuggestions(this.items, suggestionsBuilder, true);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static Result createResult(Predicate<Holder<Item>> predicate, @Nullable CompoundTag compoundTag) {
        return compoundTag != null ? itemStack -> itemStack.is(predicate) && NbtUtils.compareNbt(compoundTag, itemStack.getTag(), true) : itemStack -> itemStack.is(predicate);
    }

    @Override
    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static interface Result
    extends Predicate<ItemStack> {
    }
}

