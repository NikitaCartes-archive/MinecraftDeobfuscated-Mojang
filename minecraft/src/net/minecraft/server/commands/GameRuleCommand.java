package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameRules;

public class GameRuleCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		final LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("gamerule")
			.requires(commandSourceStack -> commandSourceStack.hasPermission(2));
		GameRules.visitGameRuleTypes(
			new GameRules.GameRuleTypeVisitor() {
				@Override
				public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
					literalArgumentBuilder.then(
						Commands.literal(key.getId())
							.executes(commandContext -> GameRuleCommand.queryRule(commandContext.getSource(), key))
							.then(type.createArgument("value").executes(commandContext -> GameRuleCommand.setRule(commandContext, key)))
					);
				}
			}
		);
		commandDispatcher.register(literalArgumentBuilder);
	}

	static <T extends GameRules.Value<T>> int setRule(CommandContext<CommandSourceStack> commandContext, GameRules.Key<T> key) {
		CommandSourceStack commandSourceStack = commandContext.getSource();
		T value = commandSourceStack.getServer().getGameRules().getRule(key);
		value.setFromArgument(commandContext, "value");
		commandSourceStack.sendSuccess(Component.translatable("commands.gamerule.set", key.getId(), value.toString()), true);
		return value.getCommandResult();
	}

	static <T extends GameRules.Value<T>> int queryRule(CommandSourceStack commandSourceStack, GameRules.Key<T> key) {
		T value = commandSourceStack.getServer().getGameRules().getRule(key);
		commandSourceStack.sendSuccess(Component.translatable("commands.gamerule.query", key.getId(), value.toString()), false);
		return value.getCommandResult();
	}
}
