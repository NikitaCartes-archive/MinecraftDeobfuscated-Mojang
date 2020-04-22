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
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

public class CloneCommands {
    private static final SimpleCommandExceptionType ERROR_OVERLAP = new SimpleCommandExceptionType(new TranslatableComponent("commands.clone.overlap"));
    private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType((object, object2) -> new TranslatableComponent("commands.clone.toobig", object, object2));
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.clone.failed"));
    public static final Predicate<BlockInWorld> FILTER_AIR = blockInWorld -> !blockInWorld.getState().isAir();

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("clone").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("begin", BlockPosArgument.blockPos()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("end", BlockPosArgument.blockPos()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("destination", BlockPosArgument.blockPos()).executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "begin"), BlockPosArgument.getLoadedBlockPos(commandContext, "end"), BlockPosArgument.getLoadedBlockPos(commandContext, "destination"), blockInWorld -> true, Mode.NORMAL))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("replace").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "begin"), BlockPosArgument.getLoadedBlockPos(commandContext, "end"), BlockPosArgument.getLoadedBlockPos(commandContext, "destination"), blockInWorld -> true, Mode.NORMAL))).then(Commands.literal("force").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "begin"), BlockPosArgument.getLoadedBlockPos(commandContext, "end"), BlockPosArgument.getLoadedBlockPos(commandContext, "destination"), blockInWorld -> true, Mode.FORCE)))).then(Commands.literal("move").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "begin"), BlockPosArgument.getLoadedBlockPos(commandContext, "end"), BlockPosArgument.getLoadedBlockPos(commandContext, "destination"), blockInWorld -> true, Mode.MOVE)))).then(Commands.literal("normal").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "begin"), BlockPosArgument.getLoadedBlockPos(commandContext, "end"), BlockPosArgument.getLoadedBlockPos(commandContext, "destination"), blockInWorld -> true, Mode.NORMAL))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("masked").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "begin"), BlockPosArgument.getLoadedBlockPos(commandContext, "end"), BlockPosArgument.getLoadedBlockPos(commandContext, "destination"), FILTER_AIR, Mode.NORMAL))).then(Commands.literal("force").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "begin"), BlockPosArgument.getLoadedBlockPos(commandContext, "end"), BlockPosArgument.getLoadedBlockPos(commandContext, "destination"), FILTER_AIR, Mode.FORCE)))).then(Commands.literal("move").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "begin"), BlockPosArgument.getLoadedBlockPos(commandContext, "end"), BlockPosArgument.getLoadedBlockPos(commandContext, "destination"), FILTER_AIR, Mode.MOVE)))).then(Commands.literal("normal").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "begin"), BlockPosArgument.getLoadedBlockPos(commandContext, "end"), BlockPosArgument.getLoadedBlockPos(commandContext, "destination"), FILTER_AIR, Mode.NORMAL))))).then(Commands.literal("filtered").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("filter", BlockPredicateArgument.blockPredicate()).executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "begin"), BlockPosArgument.getLoadedBlockPos(commandContext, "end"), BlockPosArgument.getLoadedBlockPos(commandContext, "destination"), BlockPredicateArgument.getBlockPredicate(commandContext, "filter"), Mode.NORMAL))).then(Commands.literal("force").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "begin"), BlockPosArgument.getLoadedBlockPos(commandContext, "end"), BlockPosArgument.getLoadedBlockPos(commandContext, "destination"), BlockPredicateArgument.getBlockPredicate(commandContext, "filter"), Mode.FORCE)))).then(Commands.literal("move").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "begin"), BlockPosArgument.getLoadedBlockPos(commandContext, "end"), BlockPosArgument.getLoadedBlockPos(commandContext, "destination"), BlockPredicateArgument.getBlockPredicate(commandContext, "filter"), Mode.MOVE)))).then(Commands.literal("normal").executes(commandContext -> CloneCommands.clone((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "begin"), BlockPosArgument.getLoadedBlockPos(commandContext, "end"), BlockPosArgument.getLoadedBlockPos(commandContext, "destination"), BlockPredicateArgument.getBlockPredicate(commandContext, "filter"), Mode.NORMAL)))))))));
    }

    private static int clone(CommandSourceStack commandSourceStack, BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, Predicate<BlockInWorld> predicate, Mode mode) throws CommandSyntaxException {
        BoundingBox boundingBox = new BoundingBox(blockPos, blockPos2);
        BlockPos blockPos4 = blockPos3.offset(boundingBox.getLength());
        BoundingBox boundingBox2 = new BoundingBox(blockPos3, blockPos4);
        if (!mode.canOverlap() && boundingBox2.intersects(boundingBox)) {
            throw ERROR_OVERLAP.create();
        }
        int i = boundingBox.getXSpan() * boundingBox.getYSpan() * boundingBox.getZSpan();
        if (i > 32768) {
            throw ERROR_AREA_TOO_LARGE.create(32768, i);
        }
        ServerLevel serverLevel = commandSourceStack.getLevel();
        if (!serverLevel.hasChunksAt(blockPos, blockPos2) || !serverLevel.hasChunksAt(blockPos3, blockPos4)) {
            throw BlockPosArgument.ERROR_NOT_LOADED.create();
        }
        ArrayList<CloneBlockInfo> list = Lists.newArrayList();
        ArrayList<CloneBlockInfo> list2 = Lists.newArrayList();
        ArrayList<CloneBlockInfo> list3 = Lists.newArrayList();
        LinkedList<BlockPos> deque = Lists.newLinkedList();
        BlockPos blockPos5 = new BlockPos(boundingBox2.x0 - boundingBox.x0, boundingBox2.y0 - boundingBox.y0, boundingBox2.z0 - boundingBox.z0);
        for (int j = boundingBox.z0; j <= boundingBox.z1; ++j) {
            for (int k = boundingBox.y0; k <= boundingBox.y1; ++k) {
                for (int l = boundingBox.x0; l <= boundingBox.x1; ++l) {
                    BlockPos blockPos6 = new BlockPos(l, k, j);
                    BlockPos blockPos7 = blockPos6.offset(blockPos5);
                    BlockInWorld blockInWorld = new BlockInWorld(serverLevel, blockPos6, false);
                    BlockState blockState = blockInWorld.getState();
                    if (!predicate.test(blockInWorld)) continue;
                    BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos6);
                    if (blockEntity != null) {
                        CompoundTag compoundTag = blockEntity.save(new CompoundTag());
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
            BlockEntity blockEntity3 = serverLevel.getBlockEntity(cloneBlockInfo.pos);
            Clearable.tryClear(blockEntity3);
            serverLevel.setBlock(cloneBlockInfo.pos, Blocks.BARRIER.defaultBlockState(), 2);
        }
        int l = 0;
        for (CloneBlockInfo cloneBlockInfo2 : list4) {
            if (!serverLevel.setBlock(cloneBlockInfo2.pos, cloneBlockInfo2.state, 2)) continue;
            ++l;
        }
        for (CloneBlockInfo cloneBlockInfo2 : list2) {
            BlockEntity blockEntity4 = serverLevel.getBlockEntity(cloneBlockInfo2.pos);
            if (cloneBlockInfo2.tag != null && blockEntity4 != null) {
                cloneBlockInfo2.tag.putInt("x", cloneBlockInfo2.pos.getX());
                cloneBlockInfo2.tag.putInt("y", cloneBlockInfo2.pos.getY());
                cloneBlockInfo2.tag.putInt("z", cloneBlockInfo2.pos.getZ());
                blockEntity4.load(cloneBlockInfo2.state, cloneBlockInfo2.tag);
                blockEntity4.setChanged();
            }
            serverLevel.setBlock(cloneBlockInfo2.pos, cloneBlockInfo2.state, 2);
        }
        for (CloneBlockInfo cloneBlockInfo2 : list5) {
            serverLevel.blockUpdated(cloneBlockInfo2.pos, cloneBlockInfo2.state.getBlock());
        }
        ((ServerTickList)serverLevel.getBlockTicks()).copy(boundingBox, blockPos5);
        if (l == 0) {
            throw ERROR_FAILED.create();
        }
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.clone.success", l), true);
        return l;
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
}

