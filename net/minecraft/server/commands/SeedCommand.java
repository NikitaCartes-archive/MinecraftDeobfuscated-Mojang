/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class SeedCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("seed").requires(commandSourceStack -> commandSourceStack.getServer().isSingleplayer() || commandSourceStack.hasPermission(2))).executes(commandContext -> {
            long l = ((CommandSourceStack)commandContext.getSource()).getLevel().getSeed();
            Component component = ComponentUtils.wrapInSquareBrackets(new TextComponent(String.valueOf(l)).withStyle(style -> style.setColor(ChatFormatting.GREEN).setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.valueOf(l))).setInsertion(String.valueOf(l))));
            ((CommandSourceStack)commandContext.getSource()).sendSuccess(new TranslatableComponent("commands.seed.success", component), false);
            return (int)l;
        }));
    }
}

