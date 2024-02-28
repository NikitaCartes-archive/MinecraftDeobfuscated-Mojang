package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class GiveCommand {
	public static final int MAX_ALLOWED_ITEMSTACKS = 100;

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
		commandDispatcher.register(
			Commands.literal("give")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("targets", EntityArgument.players())
						.then(
							Commands.argument("item", ItemArgument.item(commandBuildContext))
								.executes(
									commandContext -> giveItem(
											commandContext.getSource(), ItemArgument.getItem(commandContext, "item"), EntityArgument.getPlayers(commandContext, "targets"), 1
										)
								)
								.then(
									Commands.argument("count", IntegerArgumentType.integer(1))
										.executes(
											commandContext -> giveItem(
													commandContext.getSource(),
													ItemArgument.getItem(commandContext, "item"),
													EntityArgument.getPlayers(commandContext, "targets"),
													IntegerArgumentType.getInteger(commandContext, "count")
												)
										)
								)
						)
				)
		);
	}

	private static int giveItem(CommandSourceStack commandSourceStack, ItemInput itemInput, Collection<ServerPlayer> collection, int i) throws CommandSyntaxException {
		int j = itemInput.getItem().getMaxStackSize();
		int k = j * 100;
		ItemStack itemStack = itemInput.createItemStack(1, false);
		if (i > k) {
			commandSourceStack.sendFailure(Component.translatable("commands.give.failed.toomanyitems", k, itemStack.getDisplayName()));
			return 0;
		} else {
			for (ServerPlayer serverPlayer : collection) {
				int l = i;

				while (l > 0) {
					int m = Math.min(j, l);
					l -= m;
					ItemStack itemStack2 = itemInput.createItemStack(m, false);
					boolean bl = serverPlayer.getInventory().add(itemStack2);
					if (bl && itemStack2.isEmpty()) {
						ItemEntity itemEntity = serverPlayer.drop(itemStack, false);
						if (itemEntity != null) {
							itemEntity.makeFakeItem();
						}

						serverPlayer.level()
							.playSound(
								null,
								serverPlayer.getX(),
								serverPlayer.getY(),
								serverPlayer.getZ(),
								SoundEvents.ITEM_PICKUP,
								SoundSource.PLAYERS,
								0.2F,
								((serverPlayer.getRandom().nextFloat() - serverPlayer.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F
							);
						serverPlayer.containerMenu.broadcastChanges();
					} else {
						ItemEntity itemEntity = serverPlayer.drop(itemStack2, false);
						if (itemEntity != null) {
							itemEntity.setNoPickUpDelay();
							itemEntity.setTarget(serverPlayer.getUUID());
						}
					}
				}
			}

			if (collection.size() == 1) {
				commandSourceStack.sendSuccess(
					() -> Component.translatable("commands.give.success.single", i, itemStack.getDisplayName(), ((ServerPlayer)collection.iterator().next()).getDisplayName()),
					true
				);
			} else {
				commandSourceStack.sendSuccess(() -> Component.translatable("commands.give.success.single", i, itemStack.getDisplayName(), collection.size()), true);
			}

			return collection.size();
		}
	}
}
