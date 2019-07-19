/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Either;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.FunctionCommand;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.timers.FunctionCallback;
import net.minecraft.world.level.timers.FunctionTagCallback;

public class ScheduleCommand {
    private static final SimpleCommandExceptionType ERROR_SAME_TICK = new SimpleCommandExceptionType(new TranslatableComponent("commands.schedule.same_tick", new Object[0]));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("schedule").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.literal("function").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("function", FunctionArgument.functions()).suggests(FunctionCommand.SUGGEST_FUNCTION).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("time", TimeArgument.time()).executes(commandContext -> ScheduleCommand.schedule((CommandSourceStack)commandContext.getSource(), FunctionArgument.getFunctionOrTag(commandContext, "function"), IntegerArgumentType.getInteger(commandContext, "time")))))));
    }

    private static int schedule(CommandSourceStack commandSourceStack, Either<CommandFunction, Tag<CommandFunction>> either, int i) throws CommandSyntaxException {
        if (i == 0) {
            throw ERROR_SAME_TICK.create();
        }
        long l = commandSourceStack.getLevel().getGameTime() + (long)i;
        either.ifLeft(commandFunction -> {
            ResourceLocation resourceLocation = commandFunction.getId();
            commandSourceStack.getLevel().getLevelData().getScheduledEvents().reschedule(resourceLocation.toString(), l, new FunctionCallback(resourceLocation));
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.schedule.created.function", resourceLocation, i, l), true);
        }).ifRight(tag -> {
            ResourceLocation resourceLocation = tag.getId();
            commandSourceStack.getLevel().getLevelData().getScheduledEvents().reschedule("#" + resourceLocation.toString(), l, new FunctionTagCallback(resourceLocation));
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.schedule.created.tag", resourceLocation, i, l), true);
        });
        return (int)Math.floorMod(l, Integer.MAX_VALUE);
    }
}

