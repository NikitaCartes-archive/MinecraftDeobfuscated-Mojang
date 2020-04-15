/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;

public class LocateCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.locate.failed", new Object[0]));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("locate").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.literal("Pillager_Outpost").executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), "Pillager_Outpost")))).then(Commands.literal("Mineshaft").executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), "Mineshaft")))).then(Commands.literal("Mansion").executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), "Mansion")))).then(Commands.literal("Igloo").executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), "Igloo")))).then(Commands.literal("Ruined_Portal").executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), "Ruined_Portal")))).then(Commands.literal("Desert_Pyramid").executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), "Desert_Pyramid")))).then(Commands.literal("Jungle_Pyramid").executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), "Jungle_Pyramid")))).then(Commands.literal("Swamp_Hut").executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), "Swamp_Hut")))).then(Commands.literal("Stronghold").executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), "Stronghold")))).then(Commands.literal("Monument").executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), "Monument")))).then(Commands.literal("Fortress").executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), "Fortress")))).then(Commands.literal("EndCity").executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), "EndCity")))).then(Commands.literal("Ocean_Ruin").executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), "Ocean_Ruin")))).then(Commands.literal("Buried_Treasure").executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), "Buried_Treasure")))).then(Commands.literal("Shipwreck").executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), "Shipwreck")))).then(Commands.literal("Village").executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), "Village")))).then(Commands.literal("Nether_Fossil").executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), "Nether_Fossil")))).then(Commands.literal("Bastion_Remnant").executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), "Bastion_Remnant"))));
    }

    private static int locate(CommandSourceStack commandSourceStack, String string) throws CommandSyntaxException {
        BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
        BlockPos blockPos2 = commandSourceStack.getLevel().findNearestMapFeature(string, blockPos, 100, false);
        if (blockPos2 == null) {
            throw ERROR_FAILED.create();
        }
        return LocateCommand.showLocateResult(commandSourceStack, string, blockPos, blockPos2, "commands.locate.success");
    }

    public static int showLocateResult(CommandSourceStack commandSourceStack, String string, BlockPos blockPos, BlockPos blockPos2, String string2) {
        int i = Mth.floor(LocateCommand.dist(blockPos.getX(), blockPos.getZ(), blockPos2.getX(), blockPos2.getZ()));
        Component component = ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("chat.coordinates", blockPos2.getX(), "~", blockPos2.getZ())).withStyle(style -> style.setColor(ChatFormatting.GREEN).setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + blockPos2.getX() + " ~ " + blockPos2.getZ())).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.coordinates.tooltip", new Object[0]))));
        commandSourceStack.sendSuccess(new TranslatableComponent(string2, string, component, i), false);
        return i;
    }

    private static float dist(int i, int j, int k, int l) {
        int m = k - i;
        int n = l - j;
        return Mth.sqrt(m * m + n * n);
    }
}

