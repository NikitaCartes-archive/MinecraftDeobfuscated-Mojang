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
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public class SeedCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, boolean bl) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("seed").requires(commandSourceStack -> !bl || commandSourceStack.hasPermission(2))).executes(commandContext -> {
            long l = ((CommandSourceStack)commandContext.getSource()).getLevel().getSeed();
            MutableComponent component = ComponentUtils.wrapInSquareBrackets(Component.literal(String.valueOf(l)).withStyle(style -> style.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(l))).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click"))).withInsertion(String.valueOf(l))));
            ((CommandSourceStack)commandContext.getSource()).sendSuccess(Component.translatable("commands.seed.success", component), false);
            return (int)l;
        }));
    }
}

