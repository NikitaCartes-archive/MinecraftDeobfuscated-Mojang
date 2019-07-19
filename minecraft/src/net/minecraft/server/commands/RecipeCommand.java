package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeCommand {
	private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.recipe.give.failed"));
	private static final SimpleCommandExceptionType ERROR_TAKE_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.recipe.take.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("recipe")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("give")
						.then(
							Commands.argument("targets", EntityArgument.players())
								.then(
									Commands.argument("recipe", ResourceLocationArgument.id())
										.suggests(SuggestionProviders.ALL_RECIPES)
										.executes(
											commandContext -> giveRecipes(
													commandContext.getSource(),
													EntityArgument.getPlayers(commandContext, "targets"),
													Collections.singleton(ResourceLocationArgument.getRecipe(commandContext, "recipe"))
												)
										)
								)
								.then(
									Commands.literal("*")
										.executes(
											commandContext -> giveRecipes(
													commandContext.getSource(),
													EntityArgument.getPlayers(commandContext, "targets"),
													commandContext.getSource().getServer().getRecipeManager().getRecipes()
												)
										)
								)
						)
				)
				.then(
					Commands.literal("take")
						.then(
							Commands.argument("targets", EntityArgument.players())
								.then(
									Commands.argument("recipe", ResourceLocationArgument.id())
										.suggests(SuggestionProviders.ALL_RECIPES)
										.executes(
											commandContext -> takeRecipes(
													commandContext.getSource(),
													EntityArgument.getPlayers(commandContext, "targets"),
													Collections.singleton(ResourceLocationArgument.getRecipe(commandContext, "recipe"))
												)
										)
								)
								.then(
									Commands.literal("*")
										.executes(
											commandContext -> takeRecipes(
													commandContext.getSource(),
													EntityArgument.getPlayers(commandContext, "targets"),
													commandContext.getSource().getServer().getRecipeManager().getRecipes()
												)
										)
								)
						)
				)
		);
	}

	private static int giveRecipes(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, Collection<Recipe<?>> collection2) throws CommandSyntaxException {
		int i = 0;

		for (ServerPlayer serverPlayer : collection) {
			i += serverPlayer.awardRecipes(collection2);
		}

		if (i == 0) {
			throw ERROR_GIVE_FAILED.create();
		} else {
			if (collection.size() == 1) {
				commandSourceStack.sendSuccess(
					new TranslatableComponent("commands.recipe.give.success.single", collection2.size(), ((ServerPlayer)collection.iterator().next()).getDisplayName()), true
				);
			} else {
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.recipe.give.success.multiple", collection2.size(), collection.size()), true);
			}

			return i;
		}
	}

	private static int takeRecipes(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, Collection<Recipe<?>> collection2) throws CommandSyntaxException {
		int i = 0;

		for (ServerPlayer serverPlayer : collection) {
			i += serverPlayer.resetRecipes(collection2);
		}

		if (i == 0) {
			throw ERROR_TAKE_FAILED.create();
		} else {
			if (collection.size() == 1) {
				commandSourceStack.sendSuccess(
					new TranslatableComponent("commands.recipe.take.success.single", collection2.size(), ((ServerPlayer)collection.iterator().next()).getDisplayName()), true
				);
			} else {
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.recipe.take.success.multiple", collection2.size(), collection.size()), true);
			}

			return i;
		}
	}
}
