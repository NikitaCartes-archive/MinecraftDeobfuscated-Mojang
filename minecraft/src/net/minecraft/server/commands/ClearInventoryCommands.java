package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemPredicateArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ClearInventoryCommands {
	private static final DynamicCommandExceptionType ERROR_SINGLE = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("clear.failed.single", object)
	);
	private static final DynamicCommandExceptionType ERROR_MULTIPLE = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("clear.failed.multiple", object)
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
		commandDispatcher.register(
			Commands.literal("clear")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.executes(
					commandContext -> clearUnlimited(commandContext.getSource(), Collections.singleton(commandContext.getSource().getPlayerOrException()), itemStack -> true)
				)
				.then(
					Commands.argument("targets", EntityArgument.players())
						.executes(commandContext -> clearUnlimited(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), itemStack -> true))
						.then(
							Commands.argument("item", ItemPredicateArgument.itemPredicate(commandBuildContext))
								.executes(
									commandContext -> clearUnlimited(
											commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), ItemPredicateArgument.getItemPredicate(commandContext, "item")
										)
								)
								.then(
									Commands.argument("maxCount", IntegerArgumentType.integer(0))
										.executes(
											commandContext -> clearInventory(
													commandContext.getSource(),
													EntityArgument.getPlayers(commandContext, "targets"),
													ItemPredicateArgument.getItemPredicate(commandContext, "item"),
													IntegerArgumentType.getInteger(commandContext, "maxCount")
												)
										)
								)
						)
				)
		);
	}

	private static int clearUnlimited(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, Predicate<ItemStack> predicate) throws CommandSyntaxException {
		return clearInventory(commandSourceStack, collection, predicate, -1);
	}

	private static int clearInventory(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, Predicate<ItemStack> predicate, int i) throws CommandSyntaxException {
		int j = 0;

		for (ServerPlayer serverPlayer : collection) {
			j += serverPlayer.getInventory().clearOrCountMatchingItems(predicate, i, serverPlayer.inventoryMenu.getCraftSlots());
			serverPlayer.containerMenu.broadcastChanges();
			serverPlayer.inventoryMenu.slotsChanged(serverPlayer.getInventory());
		}

		if (j == 0) {
			if (collection.size() == 1) {
				throw ERROR_SINGLE.create(((ServerPlayer)collection.iterator().next()).getName());
			} else {
				throw ERROR_MULTIPLE.create(collection.size());
			}
		} else {
			int k = j;
			if (i == 0) {
				if (collection.size() == 1) {
					commandSourceStack.sendSuccess(
						() -> Component.translatable("commands.clear.test.single", k, ((ServerPlayer)collection.iterator().next()).getDisplayName()), true
					);
				} else {
					commandSourceStack.sendSuccess(() -> Component.translatable("commands.clear.test.multiple", k, collection.size()), true);
				}
			} else if (collection.size() == 1) {
				commandSourceStack.sendSuccess(
					() -> Component.translatable("commands.clear.success.single", k, ((ServerPlayer)collection.iterator().next()).getDisplayName()), true
				);
			} else {
				commandSourceStack.sendSuccess(() -> Component.translatable("commands.clear.success.multiple", k, collection.size()), true);
			}

			return j;
		}
	}
}
