package net.minecraft.server.commands;

import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;

public class TagCommand {
	private static final SimpleCommandExceptionType ERROR_ADD_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.tag.add.failed"));
	private static final SimpleCommandExceptionType ERROR_REMOVE_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.tag.remove.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("tag")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("targets", EntityArgument.entities())
						.then(
							Commands.literal("add")
								.then(
									Commands.argument("name", StringArgumentType.word())
										.executes(
											commandContext -> addTag(
													commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), StringArgumentType.getString(commandContext, "name")
												)
										)
								)
						)
						.then(
							Commands.literal("remove")
								.then(
									Commands.argument("name", StringArgumentType.word())
										.suggests(
											(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(
													getTags(EntityArgument.getEntities(commandContext, "targets")), suggestionsBuilder
												)
										)
										.executes(
											commandContext -> removeTag(
													commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), StringArgumentType.getString(commandContext, "name")
												)
										)
								)
						)
						.then(Commands.literal("list").executes(commandContext -> listTags(commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"))))
				)
		);
	}

	private static Collection<String> getTags(Collection<? extends Entity> collection) {
		Set<String> set = Sets.<String>newHashSet();

		for (Entity entity : collection) {
			set.addAll(entity.getTags());
		}

		return set;
	}

	private static int addTag(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, String string) throws CommandSyntaxException {
		int i = 0;

		for (Entity entity : collection) {
			if (entity.addTag(string)) {
				i++;
			}
		}

		if (i == 0) {
			throw ERROR_ADD_FAILED.create();
		} else {
			if (collection.size() == 1) {
				commandSourceStack.sendSuccess(
					new TranslatableComponent("commands.tag.add.success.single", string, ((Entity)collection.iterator().next()).getDisplayName()), true
				);
			} else {
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.tag.add.success.multiple", string, collection.size()), true);
			}

			return i;
		}
	}

	private static int removeTag(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, String string) throws CommandSyntaxException {
		int i = 0;

		for (Entity entity : collection) {
			if (entity.removeTag(string)) {
				i++;
			}
		}

		if (i == 0) {
			throw ERROR_REMOVE_FAILED.create();
		} else {
			if (collection.size() == 1) {
				commandSourceStack.sendSuccess(
					new TranslatableComponent("commands.tag.remove.success.single", string, ((Entity)collection.iterator().next()).getDisplayName()), true
				);
			} else {
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.tag.remove.success.multiple", string, collection.size()), true);
			}

			return i;
		}
	}

	private static int listTags(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection) {
		Set<String> set = Sets.<String>newHashSet();

		for (Entity entity : collection) {
			set.addAll(entity.getTags());
		}

		if (collection.size() == 1) {
			Entity entity2 = (Entity)collection.iterator().next();
			if (set.isEmpty()) {
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.tag.list.single.empty", entity2.getDisplayName()), false);
			} else {
				commandSourceStack.sendSuccess(
					new TranslatableComponent("commands.tag.list.single.success", entity2.getDisplayName(), set.size(), ComponentUtils.formatList(set)), false
				);
			}
		} else if (set.isEmpty()) {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.tag.list.multiple.empty", collection.size()), false);
		} else {
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.tag.list.multiple.success", collection.size(), set.size(), ComponentUtils.formatList(set)), false
			);
		}

		return set.size();
	}
}
