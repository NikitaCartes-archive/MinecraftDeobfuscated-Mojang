/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

public class SetBlockCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.setblock.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("setblock").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("pos", BlockPosArgument.blockPos()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("block", BlockStateArgument.block(commandBuildContext)).executes(commandContext -> SetBlockCommand.setBlock((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), BlockStateArgument.getBlock(commandContext, "block"), Mode.REPLACE, null))).then(Commands.literal("destroy").executes(commandContext -> SetBlockCommand.setBlock((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), BlockStateArgument.getBlock(commandContext, "block"), Mode.DESTROY, null)))).then(Commands.literal("keep").executes(commandContext -> SetBlockCommand.setBlock((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), BlockStateArgument.getBlock(commandContext, "block"), Mode.REPLACE, blockInWorld -> blockInWorld.getLevel().isEmptyBlock(blockInWorld.getPos()))))).then(Commands.literal("replace").executes(commandContext -> SetBlockCommand.setBlock((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "pos"), BlockStateArgument.getBlock(commandContext, "block"), Mode.REPLACE, null))))));
    }

    private static int setBlock(CommandSourceStack commandSourceStack, BlockPos blockPos, BlockInput blockInput, Mode mode, @Nullable Predicate<BlockInWorld> predicate) throws CommandSyntaxException {
        boolean bl;
        ServerLevel serverLevel = commandSourceStack.getLevel();
        if (predicate != null && !predicate.test(new BlockInWorld(serverLevel, blockPos, true))) {
            throw ERROR_FAILED.create();
        }
        if (mode == Mode.DESTROY) {
            serverLevel.destroyBlock(blockPos, true);
            bl = !blockInput.getState().isAir() || !serverLevel.getBlockState(blockPos).isAir();
        } else {
            BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
            Clearable.tryClear(blockEntity);
            bl = true;
        }
        if (bl && !blockInput.place(serverLevel, blockPos, 2)) {
            throw ERROR_FAILED.create();
        }
        serverLevel.blockUpdated(blockPos, blockInput.getState().getBlock());
        commandSourceStack.sendSuccess(Component.translatable("commands.setblock.success", blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
        return 1;
    }

    public static enum Mode {
        REPLACE,
        DESTROY;

    }

    public static interface Filter {
        @Nullable
        public BlockInput filter(BoundingBox var1, BlockPos var2, BlockInput var3, ServerLevel var4);
    }
}

