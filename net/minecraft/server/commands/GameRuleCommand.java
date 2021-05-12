/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.GameRules;

public class GameRuleCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        final LiteralArgumentBuilder literalArgumentBuilder = (LiteralArgumentBuilder)Commands.literal("gamerule").requires(commandSourceStack -> commandSourceStack.hasPermission(2));
        GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor(){

            @Override
            public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                literalArgumentBuilder.then(((LiteralArgumentBuilder)Commands.literal(key.getId()).executes(commandContext -> GameRuleCommand.queryRule((CommandSourceStack)commandContext.getSource(), key))).then(type.createArgument("value").executes(commandContext -> GameRuleCommand.setRule(commandContext, key))));
            }
        });
        commandDispatcher.register(literalArgumentBuilder);
    }

    static <T extends GameRules.Value<T>> int setRule(CommandContext<CommandSourceStack> commandContext, GameRules.Key<T> key) {
        CommandSourceStack commandSourceStack = commandContext.getSource();
        T value = commandSourceStack.getServer().getGameRules().getRule(key);
        ((GameRules.Value)value).setFromArgument(commandContext, "value");
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.gamerule.set", key.getId(), ((GameRules.Value)value).toString()), true);
        return ((GameRules.Value)value).getCommandResult();
    }

    static <T extends GameRules.Value<T>> int queryRule(CommandSourceStack commandSourceStack, GameRules.Key<T> key) {
        T value = commandSourceStack.getServer().getGameRules().getRule(key);
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.gamerule.query", key.getId(), ((GameRules.Value)value).toString()), false);
        return ((GameRules.Value)value).getCommandResult();
    }
}

