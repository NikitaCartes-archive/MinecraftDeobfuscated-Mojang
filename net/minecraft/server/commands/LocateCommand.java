/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

public class LocateCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.locate.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        LiteralArgumentBuilder literalArgumentBuilder = (LiteralArgumentBuilder)Commands.literal("locate").requires(commandSourceStack -> commandSourceStack.hasPermission(2));
        for (Map.Entry entry : StructureFeature.STRUCTURES_REGISTRY.entrySet()) {
            literalArgumentBuilder = (LiteralArgumentBuilder)literalArgumentBuilder.then(Commands.literal((String)entry.getKey()).executes(commandContext -> LocateCommand.locate((CommandSourceStack)commandContext.getSource(), (StructureFeature)entry.getValue())));
        }
        commandDispatcher.register(literalArgumentBuilder);
    }

    private static int locate(CommandSourceStack commandSourceStack, StructureFeature<?> structureFeature) throws CommandSyntaxException {
        BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
        BlockPos blockPos2 = commandSourceStack.getLevel().findNearestMapFeature(structureFeature, blockPos, 100, false);
        if (blockPos2 == null) {
            throw ERROR_FAILED.create();
        }
        return LocateCommand.showLocateResult(commandSourceStack, structureFeature.getFeatureName(), blockPos, blockPos2, "commands.locate.success");
    }

    public static int showLocateResult(CommandSourceStack commandSourceStack, String string, BlockPos blockPos, BlockPos blockPos2, String string2) {
        int i = Mth.floor(LocateCommand.dist(blockPos.getX(), blockPos.getZ(), blockPos2.getX(), blockPos2.getZ()));
        MutableComponent component = ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("chat.coordinates", blockPos2.getX(), "~", blockPos2.getZ())).withStyle(style -> style.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + blockPos2.getX() + " ~ " + blockPos2.getZ())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("chat.coordinates.tooltip"))));
        commandSourceStack.sendSuccess(new TranslatableComponent(string2, string, component, i), false);
        return i;
    }

    private static float dist(int i, int j, int k, int l) {
        int m = k - i;
        int n = l - j;
        return Mth.sqrt(m * m + n * n);
    }
}

