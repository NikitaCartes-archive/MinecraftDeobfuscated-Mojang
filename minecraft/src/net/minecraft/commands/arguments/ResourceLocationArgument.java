package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

public class ResourceLocationArgument implements ArgumentType<ResourceLocation> {
	private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_ADVANCEMENT = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("advancement.advancementNotFound", object)
	);
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_RECIPE = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("recipe.notFound", object)
	);

	public static ResourceLocationArgument id() {
		return new ResourceLocationArgument();
	}

	public static AdvancementHolder getAdvancement(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		ResourceLocation resourceLocation = getId(commandContext, string);
		AdvancementHolder advancementHolder = commandContext.getSource().getServer().getAdvancements().get(resourceLocation);
		if (advancementHolder == null) {
			throw ERROR_UNKNOWN_ADVANCEMENT.create(resourceLocation);
		} else {
			return advancementHolder;
		}
	}

	public static RecipeHolder<?> getRecipe(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		RecipeManager recipeManager = commandContext.getSource().getServer().getRecipeManager();
		ResourceLocation resourceLocation = getId(commandContext, string);
		return (RecipeHolder<?>)recipeManager.byKey(resourceLocation).orElseThrow(() -> ERROR_UNKNOWN_RECIPE.create(resourceLocation));
	}

	public static ResourceLocation getId(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.getArgument(string, ResourceLocation.class);
	}

	public ResourceLocation parse(StringReader stringReader) throws CommandSyntaxException {
		return ResourceLocation.read(stringReader);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
