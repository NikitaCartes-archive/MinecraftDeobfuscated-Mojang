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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

public class ResourceLocationArgument implements ArgumentType<ResourceLocation> {
	private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_ID = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("argument.id.unknown", object)
	);
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_ADVANCEMENT = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("advancement.advancementNotFound", object)
	);
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_RECIPE = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("recipe.notFound", object)
	);

	public static ResourceLocationArgument id() {
		return new ResourceLocationArgument();
	}

	public static Advancement getAdvancement(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		ResourceLocation resourceLocation = commandContext.getArgument(string, ResourceLocation.class);
		Advancement advancement = commandContext.getSource().getServer().getAdvancements().getAdvancement(resourceLocation);
		if (advancement == null) {
			throw ERROR_UNKNOWN_ADVANCEMENT.create(resourceLocation);
		} else {
			return advancement;
		}
	}

	public static Recipe<?> getRecipe(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
		RecipeManager recipeManager = commandContext.getSource().getServer().getRecipeManager();
		ResourceLocation resourceLocation = commandContext.getArgument(string, ResourceLocation.class);
		return (Recipe<?>)recipeManager.byKey(resourceLocation).orElseThrow(() -> ERROR_UNKNOWN_RECIPE.create(resourceLocation));
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
