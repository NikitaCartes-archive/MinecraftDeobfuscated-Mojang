package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class GiveCommand {
	public static final int MAX_ALLOWED_ITEMSTACKS = 100;

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("give")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("targets", EntityArgument.players())
						.then(
							Commands.argument("item", ItemArgument.item())
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
		if (i > k) {
			commandSourceStack.sendFailure(new TranslatableComponent("commands.give.failed.toomanyitems", k, itemInput.createItemStack(i, false).getDisplayName()));
			return 0;
		} else {
			for (ServerPlayer serverPlayer : collection) {
				int l = i;

				while (l > 0) {
					int m = Math.min(j, l);
					l -= m;
					ItemStack itemStack = itemInput.createItemStack(m, false);
					boolean bl = serverPlayer.getInventory().add(itemStack);
					if (bl && itemStack.isEmpty()) {
						itemStack.setCount(1);
						ItemEntity itemEntity = serverPlayer.drop(itemStack, false);
						if (itemEntity != null) {
							itemEntity.makeFakeItem();
						}

						serverPlayer.level
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
						ItemEntity itemEntity = serverPlayer.drop(itemStack, false);
						if (itemEntity != null) {
							itemEntity.setNoPickUpDelay();
							itemEntity.setOwner(serverPlayer.getUUID());
						}
					}
				}
			}

			if (collection.size() == 1) {
				commandSourceStack.sendSuccess(
					new TranslatableComponent(
						"commands.give.success.single", i, itemInput.createItemStack(i, false).getDisplayName(), ((ServerPlayer)collection.iterator().next()).getDisplayName()
					),
					true
				);
			} else {
				commandSourceStack.sendSuccess(
					new TranslatableComponent("commands.give.success.single", i, itemInput.createItemStack(i, false).getDisplayName(), collection.size()), true
				);
			}

			return collection.size();
		}
	}
}
