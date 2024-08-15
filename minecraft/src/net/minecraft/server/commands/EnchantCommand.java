package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class EnchantCommand {
	private static final DynamicCommandExceptionType ERROR_NOT_LIVING_ENTITY = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("commands.enchant.failed.entity", object)
	);
	private static final DynamicCommandExceptionType ERROR_NO_ITEM = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("commands.enchant.failed.itemless", object)
	);
	private static final DynamicCommandExceptionType ERROR_INCOMPATIBLE = new DynamicCommandExceptionType(
		object -> Component.translatableEscape("commands.enchant.failed.incompatible", object)
	);
	private static final Dynamic2CommandExceptionType ERROR_LEVEL_TOO_HIGH = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatableEscape("commands.enchant.failed.level", object, object2)
	);
	private static final SimpleCommandExceptionType ERROR_NOTHING_HAPPENED = new SimpleCommandExceptionType(Component.translatable("commands.enchant.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
		commandDispatcher.register(
			Commands.literal("enchant")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("targets", EntityArgument.entities())
						.then(
							Commands.argument("enchantment", ResourceArgument.resource(commandBuildContext, Registries.ENCHANTMENT))
								.executes(
									commandContext -> enchant(
											commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), ResourceArgument.getEnchantment(commandContext, "enchantment"), 1
										)
								)
								.then(
									Commands.argument("level", IntegerArgumentType.integer(0))
										.executes(
											commandContext -> enchant(
													commandContext.getSource(),
													EntityArgument.getEntities(commandContext, "targets"),
													ResourceArgument.getEnchantment(commandContext, "enchantment"),
													IntegerArgumentType.getInteger(commandContext, "level")
												)
										)
								)
						)
				)
		);
	}

	private static int enchant(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, Holder<Enchantment> holder, int i) throws CommandSyntaxException {
		Enchantment enchantment = holder.value();
		if (i > enchantment.getMaxLevel()) {
			throw ERROR_LEVEL_TOO_HIGH.create(i, enchantment.getMaxLevel());
		} else {
			int j = 0;

			for (Entity entity : collection) {
				if (entity instanceof LivingEntity) {
					LivingEntity livingEntity = (LivingEntity)entity;
					ItemStack itemStack = livingEntity.getMainHandItem();
					if (!itemStack.isEmpty()) {
						if (enchantment.canEnchant(itemStack)
							&& EnchantmentHelper.isEnchantmentCompatible(EnchantmentHelper.getEnchantmentsForCrafting(itemStack).keySet(), holder)) {
							itemStack.enchant(holder, i);
							j++;
						} else if (collection.size() == 1) {
							throw ERROR_INCOMPATIBLE.create(itemStack.getHoverName().getString());
						}
					} else if (collection.size() == 1) {
						throw ERROR_NO_ITEM.create(livingEntity.getName().getString());
					}
				} else if (collection.size() == 1) {
					throw ERROR_NOT_LIVING_ENTITY.create(entity.getName().getString());
				}
			}

			if (j == 0) {
				throw ERROR_NOTHING_HAPPENED.create();
			} else {
				if (collection.size() == 1) {
					commandSourceStack.sendSuccess(
						() -> Component.translatable(
								"commands.enchant.success.single", Enchantment.getFullname(holder, i), ((Entity)collection.iterator().next()).getDisplayName()
							),
						true
					);
				} else {
					commandSourceStack.sendSuccess(
						() -> Component.translatable("commands.enchant.success.multiple", Enchantment.getFullname(holder, i), collection.size()), true
					);
				}

				return j;
			}
		}
	}
}
