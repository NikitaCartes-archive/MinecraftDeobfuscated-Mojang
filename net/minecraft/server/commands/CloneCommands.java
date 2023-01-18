/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.ticks.LevelTicks;
import org.jetbrains.annotations.Nullable;

public class CloneCommands {
    private static final SimpleCommandExceptionType ERROR_OVERLAP = new SimpleCommandExceptionType(Component.translatable("commands.clone.overlap"));
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType((object, object2) -> Component.translatable("commands.clone.toobig", object, object2));
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.clone.failed"));
    public static final Predicate<BlockInWorld> FILTER_AIR = blockInWorld -> !blockInWorld.getState().isAir();

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("clone").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(CloneCommands.beginEndDestinationAndModeSuffix(commandBuildContext, commandContext -> ((CommandSourceStack)commandContext.getSource()).getLevel()))).then(Commands.literal("from").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("sourceDimension", DimensionArgument.dimension()).then(CloneCommands.beginEndDestinationAndModeSuffix(commandBuildContext, commandContext -> DimensionArgument.getDimension(commandContext, "sourceDimension"))))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> beginEndDestinationAndModeSuffix(CommandBuildContext commandBuildContext, CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> commandFunction) {
        return Commands.argument("begin", BlockPosArgument.blockPos()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("end", BlockPosArgument.blockPos()).then(CloneCommands.destinationAndModeSuffix(commandBuildContext, commandFunction, commandContext -> ((CommandSourceStack)commandContext.getSource()).getLevel()))).then(Commands.literal("to").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targetDimension", DimensionArgument.dimension()).then(CloneCommands.destinationAndModeSuffix(commandBuildContext, commandFunction, commandContext -> DimensionArgument.getDimension(commandContext, "targetDimension"))))));
    }

    private static DimensionAndPosition getLoadedDimensionAndPosition(CommandContext<CommandSourceStack> commandContext, ServerLevel serverLevel, String string) throws CommandSyntaxException {
        BlockPos blockPos = BlockPosArgument.getLoadedBlockPos(commandContext, serverLevel, string);
        return new DimensionAndPosition(serverLevel, blockPos);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> destinationAndModeSuffix(CommandBuildContext commandBuildContext, CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> commandFunction, CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> commandFunction2) {
        CommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> commandFunction3 = commandContext -> CloneCommands.getLoadedDimensionAndPosition(commandContext, (ServerLevel)commandFunction.apply((CommandContext<CommandSourceStack>)commandContext), "begin");
        CommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> commandFunction4 = commandContext -> CloneCommands.getLoadedDimensionAndPosition(commandContext, (ServerLevel)commandFunction.apply((CommandContext<CommandSourceStack>)commandContext), "end");
        CommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> commandFunction5 = commandContext -> CloneCommands.getLoadedDimensionAndPosition(commandContext, (ServerLevel)commandFunction2.apply((CommandContext<CommandSourceStack>)commandContext), "destination");
        return ((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("destination", BlockPosArgument.blockPos()).executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), (DimensionAndPosition)commandFunction3.apply(commandContext), (DimensionAndPosition)commandFunction4.apply(commandContext), (DimensionAndPosition)commandFunction5.apply(commandContext), blockInWorld -> true, Mode.NORMAL))).then(CloneCommands.wrapWithCloneMode(commandFunction3, commandFunction4, commandFunction5, commandContext -> blockInWorld -> true, Commands.literal("replace").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), (DimensionAndPosition)commandFunction3.apply(commandContext), (DimensionAndPosition)commandFunction4.apply(commandContext), (DimensionAndPosition)commandFunction5.apply(commandContext), blockInWorld -> true, Mode.NORMAL))))).then(CloneCommands.wrapWithCloneMode(commandFunction3, commandFunction4, commandFunction5, commandContext -> FILTER_AIR, Commands.literal("masked").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), (DimensionAndPosition)commandFunction3.apply(commandContext), (DimensionAndPosition)commandFunction4.apply(commandContext), (DimensionAndPosition)commandFunction5.apply(commandContext), FILTER_AIR, Mode.NORMAL))))).then(Commands.literal("filtered").then(CloneCommands.wrapWithCloneMode(commandFunction3, commandFunction4, commandFunction5, commandContext -> BlockPredicateArgument.getBlockPredicate(commandContext, "filter"), Commands.argument("filter", BlockPredicateArgument.blockPredicate(commandBuildContext)).executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), (DimensionAndPosition)commandFunction3.apply(commandContext), (DimensionAndPosition)commandFunction4.apply(commandContext), (DimensionAndPosition)commandFunction5.apply(commandContext), BlockPredicateArgument.getBlockPredicate(commandContext, "filter"), Mode.NORMAL)))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> wrapWithCloneMode(CommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> commandFunction, CommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> commandFunction2, CommandFunction<CommandContext<CommandSourceStack>, DimensionAndPosition> commandFunction3, CommandFunction<CommandContext<CommandSourceStack>, Predicate<BlockInWorld>> commandFunction4, ArgumentBuilder<CommandSourceStack, ?> argumentBuilder) {
        return ((ArgumentBuilder)((ArgumentBuilder)argumentBuilder.then((ArgumentBuilder<CommandSourceStack, ?>)Commands.literal("force").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), (DimensionAndPosition)commandFunction.apply(commandContext), (DimensionAndPosition)commandFunction2.apply(commandContext), (DimensionAndPosition)commandFunction3.apply(commandContext), (Predicate)commandFunction4.apply(commandContext), Mode.FORCE)))).then(Commands.literal("move").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), (DimensionAndPosition)commandFunction.apply(commandContext), (DimensionAndPosition)commandFunction2.apply(commandContext), (DimensionAndPosition)commandFunction3.apply(commandContext), (Predicate)commandFunction4.apply(commandContext), Mode.MOVE)))).then(Commands.literal("normal").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), (DimensionAndPosition)commandFunction.apply(commandContext), (DimensionAndPosition)commandFunction2.apply(commandContext), (DimensionAndPosition)commandFunction3.apply(commandContext), (Predicate)commandFunction4.apply(commandContext), Mode.NORMAL)));
    }

    private static int clone(CommandSourceStack commandSourceStack, DimensionAndPosition dimensionAndPosition, DimensionAndPosition dimensionAndPosition2, DimensionAndPosition dimensionAndPosition3, Predicate<BlockInWorld> predicate, Mode mode) throws CommandSyntaxException {
        int j;
        BlockPos blockPos = dimensionAndPosition.position();
        BlockPos blockPos2 = dimensionAndPosition2.position();
        BoundingBox boundingBox = BoundingBox.fromCorners(blockPos, blockPos2);
        BlockPos blockPos3 = dimensionAndPosition3.position();
        BlockPos blockPos4 = blockPos3.offset(boundingBox.getLength());
        BoundingBox boundingBox2 = BoundingBox.fromCorners(blockPos3, blockPos4);
        ServerLevel serverLevel = dimensionAndPosition.dimension();
        ServerLevel serverLevel2 = dimensionAndPosition3.dimension();
        if (!mode.canOverlap() && serverLevel == serverLevel2 && boundingBox2.intersects(boundingBox)) {
            throw ERROR_OVERLAP.create();
        }
        int i = boundingBox.getXSpan() * boundingBox.getYSpan() * boundingBox.getZSpan();
        if (i > (j = commandSourceStack.getLevel().getGameRules().getInt(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT))) {
            throw ERROR_AREA_TOO_LARGE.create(j, i);
        }
        if (!serverLevel.hasChunksAt(blockPos, blockPos2) || !serverLevel2.hasChunksAt(blockPos3, blockPos4)) {
            throw BlockPosArgument.ERROR_NOT_LOADED.create();
        }
        ArrayList<CloneBlockInfo> list = Lists.newArrayList();
        ArrayList<CloneBlockInfo> list2 = Lists.newArrayList();
        ArrayList<CloneBlockInfo> list3 = Lists.newArrayList();
        LinkedList<BlockPos> deque = Lists.newLinkedList();
        BlockPos blockPos5 = new BlockPos(boundingBox2.minX() - boundingBox.minX(), boundingBox2.minY() - boundingBox.minY(), boundingBox2.minZ() - boundingBox.minZ());
        for (int k = boundingBox.minZ(); k <= boundingBox.maxZ(); ++k) {
            for (int l = boundingBox.minY(); l <= boundingBox.maxY(); ++l) {
                for (int m = boundingBox.minX(); m <= boundingBox.maxX(); ++m) {
                    BlockPos blockPos6 = new BlockPos(m, l, k);
                    BlockPos blockPos7 = blockPos6.offset(blockPos5);
                    BlockInWorld blockInWorld = new BlockInWorld(serverLevel, blockPos6, false);
                    BlockState blockState = blockInWorld.getState();
                    if (!predicate.test(blockInWorld)) continue;
                    BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos6);
                    if (blockEntity != null) {
                        CompoundTag compoundTag = blockEntity.saveWithoutMetadata();
                        list2.add(new CloneBlockInfo(blockPos7, blockState, compoundTag));
                        deque.addLast(blockPos6);
                        continue;
                    }
                    if (blockState.isSolidRender(serverLevel, blockPos6) || blockState.isCollisionShapeFullBlock(serverLevel, blockPos6)) {
                        list.add(new CloneBlockInfo(blockPos7, blockState, null));
                        deque.addLast(blockPos6);
                        continue;
                    }
                    list3.add(new CloneBlockInfo(blockPos7, blockState, null));
                    deque.addFirst(blockPos6);
                }
            }
        }
        if (mode == Mode.MOVE) {
            for (BlockPos blockPos8 : deque) {
                BlockEntity blockEntity2 = serverLevel.getBlockEntity(blockPos8);
                Clearable.tryClear(blockEntity2);
                serverLevel.setBlock(blockPos8, Blocks.BARRIER.defaultBlockState(), 2);
            }
            for (BlockPos blockPos8 : deque) {
                serverLevel.setBlock(blockPos8, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        ArrayList<CloneBlockInfo> list4 = Lists.newArrayList();
        list4.addAll(list);
        list4.addAll(list2);
        list4.addAll(list3);
        List<CloneBlockInfo> list5 = Lists.reverse(list4);
        for (CloneBlockInfo cloneBlockInfo : list5) {
            BlockEntity blockEntity3 = serverLevel2.getBlockEntity(cloneBlockInfo.pos);
            Clearable.tryClear(blockEntity3);
            serverLevel2.setBlock(cloneBlockInfo.pos, Blocks.BARRIER.defaultBlockState(), 2);
        }
        int m = 0;
        for (CloneBlockInfo cloneBlockInfo2 : list4) {
            if (!serverLevel2.setBlock(cloneBlockInfo2.pos, cloneBlockInfo2.state, 2)) continue;
            ++m;
        }
        for (CloneBlockInfo cloneBlockInfo2 : list2) {
            BlockEntity blockEntity4 = serverLevel2.getBlockEntity(cloneBlockInfo2.pos);
            if (cloneBlockInfo2.tag != null && blockEntity4 != null) {
                blockEntity4.load(cloneBlockInfo2.tag);
                blockEntity4.setChanged();
            }
            serverLevel2.setBlock(cloneBlockInfo2.pos, cloneBlockInfo2.state, 2);
        }
        for (CloneBlockInfo cloneBlockInfo2 : list5) {
            serverLevel2.blockUpdated(cloneBlockInfo2.pos, cloneBlockInfo2.state.getBlock());
        }
        ((LevelTicks)serverLevel2.getBlockTicks()).copyAreaFrom(serverLevel.getBlockTicks(), boundingBox, blockPos5);
        if (m == 0) {
            throw ERROR_FAILED.create();
        }
        commandSourceStack.sendSuccess(Component.translatable("commands.clone.success", m), true);
        return m;
    }

    @FunctionalInterface
    static interface CommandFunction<T, R> {
        public R apply(T var1) throws CommandSyntaxException;
    }

    record DimensionAndPosition(ServerLevel dimension, BlockPos position) {
    }

    static enum Mode {
        FORCE(true),
        MOVE(true),
        NORMAL(false);

        private final boolean canOverlap;

        private Mode(boolean bl) {
            this.canOverlap = bl;
        }

        public boolean canOverlap() {
            return this.canOverlap;
        }
    }

    static class CloneBlockInfo {
        public final BlockPos pos;
        public final BlockState state;
        @Nullable
        public final CompoundTag tag;

        public CloneBlockInfo(BlockPos blockPos, BlockState blockState, @Nullable CompoundTag compoundTag) {
            this.pos = blockPos;
            this.state = blockState;
            this.tag = compoundTag;
        }
    }
}

