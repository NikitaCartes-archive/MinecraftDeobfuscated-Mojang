/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.loot.ItemModifierManager;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ResourceLocationArgument
implements ArgumentType<ResourceLocation> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ADVANCEMENT = new DynamicCommandExceptionType(object -> Component.translatable("advancement.advancementNotFound", object));
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_RECIPE = new DynamicCommandExceptionType(object -> Component.translatable("recipe.notFound", object));
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_PREDICATE = new DynamicCommandExceptionType(object -> Component.translatable("predicate.unknown", object));
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM_MODIFIER = new DynamicCommandExceptionType(object -> Component.translatable("item_modifier.unknown", object));

    public static ResourceLocationArgument id() {
        return new ResourceLocationArgument();
    }

    public static Advancement getAdvancement(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        ResourceLocation resourceLocation = ResourceLocationArgument.getId(commandContext, string);
        Advancement advancement = commandContext.getSource().getServer().getAdvancements().getAdvancement(resourceLocation);
        if (advancement == null) {
            throw ERROR_UNKNOWN_ADVANCEMENT.create(resourceLocation);
        }
        return advancement;
    }

    public static Recipe<?> getRecipe(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        RecipeManager recipeManager = commandContext.getSource().getServer().getRecipeManager();
        ResourceLocation resourceLocation = ResourceLocationArgument.getId(commandContext, string);
        return recipeManager.byKey(resourceLocation).orElseThrow(() -> ERROR_UNKNOWN_RECIPE.create(resourceLocation));
    }

    public static LootItemCondition getPredicate(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        ResourceLocation resourceLocation = ResourceLocationArgument.getId(commandContext, string);
        PredicateManager predicateManager = commandContext.getSource().getServer().getPredicateManager();
        LootItemCondition lootItemCondition = predicateManager.get(resourceLocation);
        if (lootItemCondition == null) {
            throw ERROR_UNKNOWN_PREDICATE.create(resourceLocation);
        }
        return lootItemCondition;
    }

    public static LootItemFunction getItemModifier(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        ResourceLocation resourceLocation = ResourceLocationArgument.getId(commandContext, string);
        ItemModifierManager itemModifierManager = commandContext.getSource().getServer().getItemModifierManager();
        LootItemFunction lootItemFunction = itemModifierManager.get(resourceLocation);
        if (lootItemFunction == null) {
            throw ERROR_UNKNOWN_ITEM_MODIFIER.create(resourceLocation);
        }
        return lootItemFunction;
    }

    public static ResourceLocation getId(CommandContext<CommandSourceStack> commandContext, String string) {
        return commandContext.getArgument(string, ResourceLocation.class);
    }

    @Override
    public ResourceLocation parse(StringReader stringReader) throws CommandSyntaxException {
        return ResourceLocation.read(stringReader);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }
}

