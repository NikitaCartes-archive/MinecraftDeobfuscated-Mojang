/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.SetBlockCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

public class FillCommand {
    private static final int MAX_FILL_AREA = 32768;
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType((object, object2) -> Component.translatable("commands.fill.toobig", object, object2));
    static final BlockInput HOLLOW_CORE = new BlockInput(Blocks.AIR.defaultBlockState(), Collections.emptySet(), null);
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.fill.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("fill").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("from", BlockPosArgument.blockPos()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("to", BlockPosArgument.blockPos()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("block", BlockStateArgument.block(commandBuildContext)).executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandContext, "from"), BlockPosArgument.getLoadedBlockPos(commandContext, "to")), BlockStateArgument.getBlock(commandContext, "block"), Mode.REPLACE, null))).then(((LiteralArgumentBuilder)Commands.literal("replace").executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandContext, "from"), BlockPosArgument.getLoadedBlockPos(commandContext, "to")), BlockStateArgument.getBlock(commandContext, "block"), Mode.REPLACE, null))).then(Commands.argument("filter", BlockPredicateArgument.blockPredicate(commandBuildContext)).executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandContext, "from"), BlockPosArgument.getLoadedBlockPos(commandContext, "to")), BlockStateArgument.getBlock(commandContext, "block"), Mode.REPLACE, BlockPredicateArgument.getBlockPredicate(commandContext, "filter")))))).then(Commands.literal("keep").executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandContext, "from"), BlockPosArgument.getLoadedBlockPos(commandContext, "to")), BlockStateArgument.getBlock(commandContext, "block"), Mode.REPLACE, blockInWorld -> blockInWorld.getLevel().isEmptyBlock(blockInWorld.getPos()))))).then(Commands.literal("outline").executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandContext, "from"), BlockPosArgument.getLoadedBlockPos(commandContext, "to")), BlockStateArgument.getBlock(commandContext, "block"), Mode.OUTLINE, null)))).then(Commands.literal("hollow").executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandContext, "from"), BlockPosArgument.getLoadedBlockPos(commandContext, "to")), BlockStateArgument.getBlock(commandContext, "block"), Mode.HOLLOW, null)))).then(Commands.literal("destroy").executes(commandContext -> FillCommand.fillBlocks((CommandSourceStack)commandContext.getSource(), BoundingBox.fromCorners(BlockPosArgument.getLoadedBlockPos(commandContext, "from"), BlockPosArgument.getLoadedBlockPos(commandContext, "to")), BlockStateArgument.getBlock(commandContext, "block"), Mode.DESTROY, null)))))));
    }

    private static int fillBlocks(CommandSourceStack commandSourceStack, BoundingBox boundingBox, BlockInput blockInput, Mode mode, @Nullable Predicate<BlockInWorld> predicate) throws CommandSyntaxException {
        int i = boundingBox.getXSpan() * boundingBox.getYSpan() * boundingBox.getZSpan();
        if (i > 32768) {
            throw ERROR_AREA_TOO_LARGE.create(32768, i);
        }
        ArrayList<BlockPos> list = Lists.newArrayList();
        ServerLevel serverLevel = commandSourceStack.getLevel();
        int j = 0;
        for (BlockPos blockPos : BlockPos.betweenClosed(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(), boundingBox.maxX(), boundingBox.maxY(), boundingBox.maxZ())) {
            BlockInput blockInput2;
            if (predicate != null && !predicate.test(new BlockInWorld(serverLevel, blockPos, true)) || (blockInput2 = mode.filter.filter(boundingBox, blockPos, blockInput, serverLevel)) == null) continue;
            BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
            Clearable.tryClear(blockEntity);
            if (!blockInput2.place(serverLevel, blockPos, 2)) continue;
            list.add(blockPos.immutable());
            ++j;
        }
        for (BlockPos blockPos : list) {
            Block block = serverLevel.getBlockState(blockPos).getBlock();
            serverLevel.blockUpdated(blockPos, block);
        }
        if (j == 0) {
            throw ERROR_FAILED.create();
        }
        commandSourceStack.sendSuccess(Component.translatable("commands.fill.success", j), true);
        return j;
    }

    static enum Mode {
        REPLACE((boundingBox, blockPos, blockInput, serverLevel) -> blockInput),
        OUTLINE((boundingBox, blockPos, blockInput, serverLevel) -> {
            if (blockPos.getX() == boundingBox.minX() || blockPos.getX() == boundingBox.maxX() || blockPos.getY() == boundingBox.minY() || blockPos.getY() == boundingBox.maxY() || blockPos.getZ() == boundingBox.minZ() || blockPos.getZ() == boundingBox.maxZ()) {
                return blockInput;
            }
            return null;
        }),
        HOLLOW((boundingBox, blockPos, blockInput, serverLevel) -> {
            if (blockPos.getX() == boundingBox.minX() || blockPos.getX() == boundingBox.maxX() || blockPos.getY() == boundingBox.minY() || blockPos.getY() == boundingBox.maxY() || blockPos.getZ() == boundingBox.minZ() || blockPos.getZ() == boundingBox.maxZ()) {
                return blockInput;
            }
            return HOLLOW_CORE;
        }),
        DESTROY((boundingBox, blockPos, blockInput, serverLevel) -> {
            serverLevel.destroyBlock(blockPos, true);
            return blockInput;
        });

        public final SetBlockCommand.Filter filter;

        private Mode(SetBlockCommand.Filter filter) {
            this.filter = filter;
        }
    }
}

